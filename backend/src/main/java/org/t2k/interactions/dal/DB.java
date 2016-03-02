package org.t2k.interactions.dal;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.t2k.interactions.models.exceptions.DbException;
import org.t2k.interactions.models.exceptions.EntityNotFoundException;
import org.t2k.interactions.utils.InteractionsConfig;
import org.t2k.interactions.utils.jsonUtils.JsonWrapper;
import rx.Observable;

import javax.annotation.PreDestroy;
import java.beans.Introspector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 02/07/2015
 * Time: 12:17
 */
public final class DB {

    private static Logger logger = Logger.getLogger(DB.class);

    private static Bucket bucket;
    private static Cluster cluster;
    private static ObjectMapper jsonMapper;

    @Autowired
    private InteractionsConfig interactionsConfig;

    @Autowired
    private JsonWrapper jsonWrapper;

    @Autowired
    private CouchbaseBucketProperties bucketProperties;

    private void init() {
        // connect to the cluster and open the configured bucket
        cluster = CouchbaseCluster.create(interactionsConfig.getProperty("couchbase.nodes"));
        bucket = cluster.openBucket(interactionsConfig.getProperty("couchbase.bucket"), interactionsConfig.getProperty("couchbase.password"));
        addDesignToBucket(bucketProperties.getTaskDesign());
        addDesignToBucket(bucketProperties.getFileDataDesign());
        addDesignToBucket(bucketProperties.getComponentDesign());
        addDesignToBucket(bucketProperties.getStateDesign());

        jsonMapper = jsonWrapper;
    }

    private void addDesignToBucket(DesignDocument designDocument) {
        bucket.bucketManager().upsertDesignDocument(designDocument, true);
        bucket.bucketManager().publishDesignDocument(designDocument.name(), true);
    }

    @PreDestroy
    public void preDestroy() {
        if (cluster != null) {
            cluster.disconnect();
        }
    }

    /**
     * Prepare a new JsonDocument with the content of the given t object
     *
     * @param t - the object to create the document from.
     * @return a JsonDocument represents the given t object.
     */
    private static <T> JsonDocument createDocument(T t) throws DbException {
        JsonObject jsonObject = serializeObject(t);
        String id = jsonObject.get("id").toString();
        return JsonDocument.create(id, jsonObject);
    }

    /**
     * Insert or replace a Document with a custom timeout.
     * If the given Document (identified by its unique ID) already exists, it will be overridden by the current one.
     * The returned Document contains original properties, but has the refreshed CAS value set.
     * This operation will return successfully if the Document has been acknowledged in the managed cache layer on the master server node.
     * If increased data durability is a concern, upsert(Document, PersistTo, ReplicateTo) should be used instead.
     *
     * @param doc - the JsonDocument to be saved in the database.
     * @return the created document, with up to date metadata.
     * @throws DbException - in case the save operation failed.
     */
    private static JsonDocument save(JsonDocument doc) throws DbException {
        try {
            return bucket.upsert(doc);
        } catch (CouchbaseException e) {
            String errorMsg = String.format("Failed to save %s in DB.", doc.id());
            logger.error(errorMsg, e);
            throw new DbException(errorMsg, e);
        }
    }

    /**
     * Serializes any POJO to a JsonObject.
     *
     * @param t - the POJO to serialize.
     * @return the converted jsonObject.
     */
    private static <T> JsonObject serializeObject(T t) throws DbException {
        String json;
        try {
            json = jsonMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            String errorMsg = String.format("Failed to serialize object of type %s to JSON.", t.getClass().getSimpleName());
            logger.error(errorMsg, e);
            throw new DbException(errorMsg, e);
        }
        return JsonObject.fromJson(json);
    }

    /**
     * Returns an object of the class from the given jsonObject.
     *
     * @param jsonObject - the object to deserialize.
     * @return an object of class tClass from the given jsonObject.
     */
    private static <T> T deserializeObject(JsonObject jsonObject, Class<T> clazz) throws DbException {
        try {
            return jsonMapper.readValue(jsonObject.toString(), clazz);
        } catch (IOException e) {
            String errorMsg = String.format("Failed to deserialize JSON content to class %s", clazz.getSimpleName());
            logger.error(errorMsg, e);
            throw new DbException(errorMsg, e);
        }
    }

    /**
     * Gets an object from database by id.
     *
     * @param id - the id of the object we wish to get.
     * @return an object of class tClass from the database that matches the given id.
     */
    public static <T> T find(String id, Class<T> clazz) throws DbException, EntityNotFoundException {
        JsonDocument jsonDocument;
        try {
            jsonDocument = bucket.get(id);
        } catch (CouchbaseException e) {
            String errorMsg = String.format("Failed while trying to find %s in DB.", id);
            logger.error(errorMsg, e);
            throw new DbException(errorMsg, e);
        }

        if (jsonDocument == null) {
            throw new EntityNotFoundException(String.format("No %s with id: %s was found in DB.", clazz.getSimpleName(), id));
        }

        return deserializeObject(jsonDocument.content(), clazz);
    }

    /**
     * Gets a list of objects from database by list of ids.
     *
     * @param ids - a list of ids of the objects we wish to get.
     * @return a list of objects of class clazz from the database that matches the given list of ids.
     */
    public static <T> List<T> find(List<String> ids, Class<T> clazz) throws DbException, EntityNotFoundException {
        // Get them in one batch, waiting until the last one is done.
        List<JsonDocument> foundDocs;
        try {
            foundDocs = Observable
                    .from(ids)
                    .flatMap(id -> bucket.async().get(id))
                    .toList()
                    .toBlocking()
                    .single();
        } catch (CouchbaseException e) {
            String errorMsg = String.format("Failed while trying to find list of ids %s in DB.", ids);
            logger.error(errorMsg, e);
            throw new DbException(errorMsg, e);
        }

        if (foundDocs == null || foundDocs.isEmpty()) {
            throw new EntityNotFoundException(String.format("No %ss with ids: %s were found in DB.", clazz.getSimpleName(), ids));
        }

        if (foundDocs.size() < ids.size()) {
            List<String> foundDocsIds = new ArrayList<>();
            foundDocs.forEach(jsonDocument -> foundDocsIds.add(jsonDocument.id()));
            ids.removeAll(foundDocsIds);
            throw new EntityNotFoundException(String.format("No %ss with ids: %s were found in DB.", clazz.getSimpleName(), ids));
        }

        List<T> objects = new ArrayList<>();
        for (JsonDocument jsonDocument : foundDocs) {
            objects.add(deserializeObject(jsonDocument.content(), clazz));
        }

        return objects;
    }

    /**
     * Gets all the objects from the database.
     *
     * @param design   - the name of the design which includes our view.
     * @param view - the name of the view we want to query.
     * @param clazz    - the class of the desired objects we want to return.
     * @return a list of objects from the database.
     * @throws DbException in case the query to the database failed.
     */
    public static <T> List<T> findAllByView(Design design, View view, Class<T> clazz) throws DbException {
        return findByView(design, view, clazz, null, null);
    }

    /**
     * Gets all the objects that matches the given view's definition from the database.
     *
     * @param design   - the name of the design which includes our view.
     * @param view - the name of the view we want to query.
     * @param clazz    - the class of the desired objects we want to return.
     * @param key      - the key we want to look for.
     * @return a list of objects that matches the given view's definition from the database.
     * @throws DbException in case the query to the database failed.
     */
    public static <T> List<T> findByView(Design design, View view, Class<T> clazz, String key) throws DbException {
        return findByView(design, view, clazz, key, key);
    }

    /**
     * Gets all the objects that matches the given view's definition from the database.
     *
     * @param design   - the name of the design which includes our view.
     * @param view - the name of the view we want to query.
     * @param clazz    - the class of the desired objects we want to return.
     * @param startKey - the first key we want to look for.
     * @param endKey   - the last key we want to look for.
     * @return a list of objects that matches the given view's definition from the database.
     * @throws DbException in case the query to the database failed.
     */
    public static <T> List<T> findByView(Design design, View view, Class<T> clazz, Object startKey, Object endKey) throws DbException {
        List<T> ts = new ArrayList<>();
        ViewQuery query = ViewQuery.from(design.toString(), view.toString()).stale(Stale.FALSE);

        if (startKey != null) {
            query.startKey(String.valueOf(startKey));
        }

        if (endKey != null) {
            query.endKey(String.valueOf(endKey));
        }

        ViewResult result = bucket.query(query);
        if (!result.success()) {
            throw new DbException(result.error().toString());
        } else {
            for (ViewRow viewRow : result) {
                T t = deserializeObject(JsonObject.fromJson(viewRow.value().toString()), clazz);
                ts.add(t);
            }
            return ts;
        }
    }

    public static <T> List<T> findByCompoundIndexView(Design design, View view, Class<T> clazz, List<?> startKeys) throws DbException {
        return findByCompoundIndexView(design, view, clazz, startKeys, startKeys);
    }

    /**
     * Gets all the objects that matches the given view's definition from the database.
     *
     * @param design   - the name of the design which includes our view.
     * @param view - the name of the view we want to query.
     * @param clazz    - the class of the desired objects we want to return.
     * @param startKeys - the first compound keys we want to look for.
     * @param endKeys   - the last compound keys we want to look for.
     * @return a list of objects that matches the given view's definition from the database.
     * @throws DbException in case the query to the database failed.
     */
    public static <T> List<T> findByCompoundIndexView(Design design, View view, Class<T> clazz, List<?> startKeys, List<?> endKeys) throws DbException {
        List<T> ts = new ArrayList<>();
        ViewQuery query = ViewQuery.from(design.toString(), view.toString()).stale(Stale.FALSE);

        if (startKeys != null) {
            query.startKey(JsonArray.from(startKeys));
        }

        if (endKeys != null) {
            query.endKey(JsonArray.from(endKeys));
        }

        ViewResult result = bucket.query(query);
        if (!result.success()) {
            throw new DbException(result.error().toString());
        } else {
            for (ViewRow viewRow : result) {
                T t = deserializeObject(JsonObject.fromJson(viewRow.value().toString()), clazz);
                ts.add(t);
            }
            return ts;
        }
    }

    public static <T> void deleteByView(Design design, View view, Class<T> clazz, String key) throws DbException {
        List<T> ts = findByView(design, view, clazz, key);
        if (!ts.isEmpty()) {
            delete(ts);
        }
    }

    public static <T> void deleteByView(Design design, View view, Class<T> clazz, String startKey, String endKey) throws DbException {
        List<T> ts = findByView(design, view, clazz, startKey, endKey);
        if (!ts.isEmpty()) {
            delete(ts);
        }
    }

    /**
     * Checks whether specific key exists in the database.
     *
     * @param view - the name of the view we want to query.
     * @param clazz    - the class of the desired objects we want to return.
     * @param key      - the key we want to check if exists.
     * @return a list of objects that matches the given view's definition from the database.
     * @throws DbException in case the query to the database failed.
     */
    public static <T> boolean isExistByView(View view, Class<T> clazz, String key) throws DbException {
        return isExistByView(view, clazz, key, key);
    }

    /**
     * Checks whether specific keys exist in the database.
     *
     * @param view - the name of the view we want to query.
     * @param clazz    - the class of the desired objects we want to return.
     * @param startKey - the first key we want to look for.
     * @param endKey   - the last key we want to look for.
     * @return a list of objects that matches the given view's definition from the database.
     * @throws DbException in case the query to the database failed.
     */
    public static <T> boolean isExistByView(View view, Class<T> clazz, String startKey, String endKey) throws DbException {
        ViewQuery query = ViewQuery.from(Introspector.decapitalize(clazz.getSimpleName()), view.toString()).stale(Stale.FALSE);

        if (startKey != null) {
            query.startKey(startKey);
        }

        if (endKey != null) {
            query.endKey(endKey);
        }

        ViewResult result = bucket.query(query);
        if (!result.success()) {
            throw new DbException(result.error().toString());
        } else {
            if (result.totalRows() > 0) {
                return true;
            }

            return false;
        }
    }

    /**
     * Saves the object to the database.
     *
     * @param t - the object we wish to insert to the database.
     */
    public static <T> T save(T t) throws DbException {
        JsonDocument document = createDocument(t);
        JsonDocument savedDocument = save(document);
        return deserializeObject(savedDocument.content(), (Class<T>) t.getClass());
    }

    /**
     * Saves the given list of objects in the database.
     *
     * @param ts - a list of objects to be saved in the database.
     */
    public static <T> void save(List<T> ts) throws DbException {
        if (ts.isEmpty()) {
            return;
        }

        List<JsonDocument> documents = new ArrayList<>();
        for (T t : ts) {
            documents.add(createDocument(t));
        }

        // Insert them in one batch, waiting until the last one is done.
        try {
            Observable
                    .from(documents)
                    .flatMap(docToInsert -> bucket.async().upsert(docToInsert))
                    .last()
                    .toBlocking()
                    .single();
        } catch (CouchbaseException e) {
            String errorMsg = "Failed to save the given list in DB.";
            logger.error(errorMsg, e);
            throw new DbException(errorMsg, e);
        }
    }

    /**
     * Deletes the document that matches the given id from the database.
     *
     * @param id - the id of the document to be deleted from the database.
     */
    public static void delete(String id) throws DbException {
        try {
            bucket.remove(id);
        } catch (CouchbaseException e) {
            String errorMsg = String.format("Failed to delete %s from DB.", id);
            logger.error(errorMsg, e);
            throw new DbException(errorMsg, e);
        }
    }

    /**
     * Deletes the document that matches the given object from the database.
     *
     * @param t - the object to deleted from the database.
     */
    public static <T> void delete(T t) throws DbException {
        JsonDocument document = createDocument(t);
        try {
            bucket.remove(document);
        } catch (CouchbaseException e) {
            String errorMsg = String.format("Failed to save %s in DB.", document.id());
            logger.error(errorMsg, e);
            throw new DbException(errorMsg, e);
        }
    }

    /**
     * Deletes all the given objects from the database.
     *
     * @param ts - a list of objects to deleted from the database.
     */
    public static <T> void delete(List<T> ts) throws DbException {
        if (ts.isEmpty()) {
            return;
        }

        List<JsonDocument> documents = new ArrayList<>();
        for (T t : ts) {
            documents.add(createDocument(t));
        }

        // Insert them in one batch, waiting until the last one is done.
        try {
            Observable
                    .from(documents)
                    .flatMap(docToRemove -> bucket.async().remove(docToRemove))
                    .last()
                    .toBlocking()
                    .single();
        } catch (Exception e) {
            String errorMsg = "Failed to delete the given list of objects from DB.";
            logger.error(errorMsg, e);
            throw new DbException(errorMsg, e);
        }
    }

//    /**
//     * Uses a view query to find all beers. Possibly use an offset and a limit of the
//     * number of beers to retrieve.
//     *
//     * @param offset the number of beers to skip, null or < 1 to ignore
//     * @param limit the limit of beers to retrieve, null or < 1 to ignore
//     */
//    public ViewResult findAllBeers(Integer offset, Integer limit) {
//        ViewQuery query = ViewQuery.from("beer", "by_name");
//        if (limit != null && limit > 0) {
//            query.limit(limit);
//        }
//        if (offset != null && offset > 0) {
//            query.skip(offset);
//        }
//        ViewResult result = bucket.query(query);
//        return result;
//    }
//
//    /**
//     * Retrieves all the beers using a view query, returning the result asynchronously.
//     */
//    public Observable<AsyncViewResult> findAllBeersAsync() {
//        ViewQuery allBeers = ViewQuery.from("beer", "by_name");
//        return bucket.async().query(allBeers);
//    }
//
//    /**
//     * READ the document asynchronously from database.
//     */
//    public Observable<JsonDocument> asyncRead(String id) {
//        return bucket.async().get(id);
//    }
//
//    /**
//     * Create a ViewQuery to retrieve all the beers for one single brewery.
//     * The "\uefff" character (the largest UTF8 char) can be used to put an
//     * upper limit to the brewery key retrieved by the view (which otherwise
//     * would return all beers for all breweries).
//     *
//     * @param breweryId the brewery key for which to retrieve associated beers.
//     */
//    public static ViewQuery createQueryBeersForBrewery(String breweryId) {
//        ViewQuery forBrewery = ViewQuery.from("beer", "brewery_beers");
//        forBrewery.startKey(JsonArray.from(breweryId));
//        //the trick here is that sorting is UTF8 based, uefff is the largest UTF8 char
//        forBrewery.endKey(JsonArray.from(breweryId, "\uefff"));
//        return forBrewery;
//    }
//
//    /**
//     * Asynchronously query the database for all beers associated to a brewery.
//     *
//     * @param breweryId the brewery key for which to retrieve associated beers.
//     * @see #createQueryBeersForBrewery(String)
//     */
//    public Observable<AsyncViewResult> findBeersForBreweryAsync(String breweryId) {
//        return bucket.async().query(createQueryBeersForBrewery(breweryId));
//    }
//
//    /**
//     * From a brewery document and a list of documents for its associated beers,
//     * both asynchronously represented, prepare a stream of JSON documents concatenating
//     * the data.
//     *
//     * Each returned document is similar to the brewery document, but with a JSON array
//     * of beer info under the "beers" attribute. Each beer info is a JSON object with an "id"
//     * attribute (the key for the beer) and "beer" attribute (the original whole beer data).
//     */
//    public static Observable<JsonDocument> concatBeerInfoToBrewery(Observable<JsonDocument> brewery,
//                                                                   Observable<List<JsonDocument>> beers) {
//        return Observable.zip(brewery, beers,
//                new Func2<JsonDocument, List<JsonDocument>, JsonDocument>() {
//                    @Override
//                    public JsonDocument call(JsonDocument breweryDoc, List<JsonDocument> beersDoc) {
//                        JsonArray beers = JsonArray.create();
//                        for (JsonDocument beerDoc : beersDoc) {
//                            JsonObject beer = JsonObject.create()
//                                    .put("id", beerDoc.id())
//                                    .put("beer", beerDoc.content());
//                            beers.add(beer);
//                        }
//                        breweryDoc.content().put("beers", beers);
//                        return breweryDoc;
//                    }
//                });
//    }
//
//    //===== Here is a more advanced example, using Async API to search in Beer names =====
//
//    /**
//     * From an async stream of all the beers and a search token, returns a stream
//     * emitting a single JSON array. The array contains data for all matching beers,
//     * each represented by three attributes: "id" (the beer's key), "name" (the beer's name)
//     * and "detail" (the beers whole document content).
//     */
//    public Observable<JsonArray> searchBeer(Observable<AsyncViewRow> allBeers, final String token) {
//        return allBeers
//                //extract the document from the row and carve a result object using its content and id
//                .flatMap(new Func1<AsyncViewRow, Observable<JsonObject>>() {
//                    @Override
//                    public Observable<JsonObject> call(AsyncViewRow row) {
//                        return row.document().map(new Func1<JsonDocument, JsonObject>() {
//                            @Override
//                            public JsonObject call(JsonDocument jsonDocument) {
//                                return JsonObject.create()
//                                        .put("id", jsonDocument.id())
//                                        .put("name", jsonDocument.content().getString("name"))
//                                        .put("detail", jsonDocument.content());
//                            }
//                        });
//                    }
//                })
//                        //reject beers that don't match the partial name
//                .filter(new Func1<JsonObject, Boolean>() {
//                    @Override
//                    public Boolean call(JsonObject jsonObject) {
//                        String name = jsonObject.getString("name");
//                        return name != null && name.toLowerCase().contains(token.toLowerCase());
//                    }
//                })
//                        //collect results into a JSON array (one could also just use toList() since a List would be
//                        // transcoded into a JSON array)
//                .collect(new Func0<JsonArray>() { //this creates the array (once)
//                    @Override
//                    public JsonArray call() {
//                        return JsonArray.empty();
//                    }
//                }, new Action2<JsonArray, JsonObject>() { //this populates the array (each item)
//                    @Override
//                    public void call(JsonArray objects, JsonObject jsonObject) {
//                        objects.add(jsonObject);
//                    }
//                });
//    }
}