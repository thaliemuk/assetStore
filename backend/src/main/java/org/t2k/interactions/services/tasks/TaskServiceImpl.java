package org.t2k.interactions.services.tasks;

import org.apache.log4j.Logger;
import org.t2k.interactions.dal.DB;
import org.t2k.interactions.dal.Design;
import org.t2k.interactions.dal.View;
import org.t2k.interactions.models.AddComponentRequest;
import org.t2k.interactions.models.Component;
import org.t2k.interactions.models.Task;
import org.t2k.interactions.models.exceptions.DbException;
import org.t2k.interactions.models.exceptions.EntityNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 05/08/2015
 * Time: 18:47
 */
public class TaskServiceImpl implements TaskService {

    private Logger logger = Logger.getLogger(this.getClass());

    /**
     * Adds a new task with all its components children to tasks collection in DB, and also adds each component to the
     * components collection in DB.
     * @param task the task to be added.
     * @return a map that maps the tempIds of the new task and all of its children components to their server ids.
     * @throws DbException
     */
    @Override
    public Map<String, String> addTaskToDb(Task task) throws DbException {
        logger.debug(String.format("About to add a new task %s and all its components to DB.", task.getId()));
        List<Component> components = new ArrayList<>();
        Map<String, String> clientIdsToServerIdsMap = new HashMap<>();

        String taskId = task.getId();
        clientIdsToServerIdsMap.put(task.getCId(), taskId);
        task.getComponents().forEach(component -> {
            components.addAll(setDetailsToComponentsTree(component, taskId, taskId));
            clientIdsToServerIdsMap.putAll(component.getCIdsToServerIdsMap());
        });

        DB.save(components);
        DB.save(task);
        logger.debug(String.format("Task %s and all its components were successfully added to DB.", task.getId()));
        return clientIdsToServerIdsMap;
    }

    /**
     * Updates the task. Compares the given task object with the task that's already exists in DB, find the differences
     * between the two, such as components that were added/deleted/updated, and updates both the task in the tasks collection
     * in DB and the components collection with all changes made to its components.
     * @param task - the task which will be updated in DB. Will contain all the updated data of the task, include components trees list, to be saved in DB.
     * @throws DbException
     */
    @Override
    public Map<String, String> updateTask(Task task) throws DbException, EntityNotFoundException {
        logger.debug(String.format("About to update taskId %s and all its components in DB.", task.getId()));
        boolean isModified;

        // Set taskId and parentId. We do this because there might be some new components which has no such fields or
        // that there are components which have moved from one parent to another and so their parentId needs to be updated.
        task.getComponents().forEach(component -> setDetailsToComponentsTree(component, task.getId(), task.getId()));

        HashMap<String, Component> componentsFromDb = createMapOfComponentsFromDb(task.getId());
        HashMap<String, Component> componentsFromTask = createMapOfComponentsFromTask(task);

        // Remove components which no longer exists in task from components collection in DB.
        isModified = removeDeletedComponents(task.getId(), componentsFromDb, componentsFromTask);

        // Handle newly added components and updated components. Newly added components' cIds and server ids will be returned as a map.
        HashMap<String, String> cidToServerIdMap = new HashMap<>();
        for (Component component : task.getComponents()) {
            if (handleNewAndUpdatedComponents(component, componentsFromDb, cidToServerIdMap)) {
                isModified = true;
            }
        }

        // Save the task to DB if it was modified
        if (isModified) {
            //TODO: replace component in task to a baseComponents list with no metadata.
            task.updateLastModifiedDate();
            DB.save(task);
        }

        if (!componentsFromDb.isEmpty()) {
            DB.save(new ArrayList<>(componentsFromDb.values()));
        }

        logger.debug(String.format("Task %s and all its components were successfully updated in DB.", task.getId()));
        return cidToServerIdMap;
    }

    /**
     *
     * @param component the component to be handled. Verify whether the component was added/updated and make the required modifications.
     * @param componentsFromDb the list of the components of the task as they're saved in DB.
     * @param cidToServerIdMap the map that will be built in order to map new added components from their cId to their server id.
     * @return true in case component(s) have been added/updated. Otherwise, false.
     */
    private boolean handleNewAndUpdatedComponents(Component component, HashMap<String, Component> componentsFromDb, HashMap<String, String> cidToServerIdMap) {
        boolean isModified = false;
        Component componentFromDb = componentsFromDb.get(component.getId());
        if (componentFromDb == null) { // The component is a newly added component
            componentsFromDb.put(component.getId(), component);
            cidToServerIdMap.put(component.getCId(), component.getId());
            isModified = true;
        } else {
            // Update the componentsIds list if changed
            List<String> childrenIds = new ArrayList<>();
            component.getComponents().forEach(child -> childrenIds.add(child.getId()));
            if (!childrenIds.equals(componentFromDb.getComponentsIds())) {
                componentFromDb.getComponentsIds().clear();
                componentFromDb.getComponentsIds().addAll(childrenIds);
                isModified = true;
            }

            // Update data if the data field was changed. Check it by using SHA1 on both data fields and compare them
            if ((componentFromDb.getData() != null && component.getData() == null)
                    ||  (componentFromDb.getData() == null && component.getData() != null)
                    || (!(componentFromDb.getData() == null && component.getData() == null)
                    && !componentFromDb.getSha1OfData().equals(component.getSha1OfData()))) {
                componentFromDb.setData(component.getData());
                isModified = true;
            }

            // Check if the component's parent had changed
            if (!component.getParentId().equals(componentFromDb.getParentId())) {
                componentFromDb.setParentId(component.getParentId());
                isModified = true;
            }

            if (isModified) {
                componentFromDb.updateLastModifiedDate();
            } else { // The component didn't change, no need to save it in DB later -> remove it from the componentsFromDb map.
                componentsFromDb.remove(componentFromDb.getId());
            }
        }

        for (Component child : component.getComponents()) {
            if (handleNewAndUpdatedComponents(child, componentsFromDb, cidToServerIdMap)) {
                isModified = true;
            }
        }

        return isModified;
    }

    private HashMap<String, Component> createMapOfComponentsFromDb(String taskId) throws DbException {
        HashMap<String, Component> componentsFromDb = new HashMap<>();
        DB.findByView(Design.COMPONENT, View.BY_TASK_ID, Component.class, taskId).forEach(component -> {
            componentsFromDb.put(component.getId(), component);
        });

        return componentsFromDb;
    }

    private HashMap<String, Component> createMapOfComponentsFromTask(Task task) {
        HashMap<String, Component> componentsFromTask = new HashMap<>();
        task.getFlattenComponentsTree().forEach(component -> {
            componentsFromTask.put(component.getId(), component);
        });

        return componentsFromTask;
    }

    /**
     * Find deleted components, remove them from DB, update their parent's componentsIds list and remove related files from FS.
     * @param taskId - the id of the task that contains the components.
     * @param componentsFromDb - a list of components that belongs to the task as they're saved in DB.
     * @param componentsFromTask - the list of the current components from the task.
     * @return true in case component(s) have been removed. Otherwise, false.
     * @throws DbException
     */
    private boolean removeDeletedComponents(String taskId, HashMap<String, Component> componentsFromDb, HashMap<String, Component> componentsFromTask) throws DbException {
        HashMap<String, Component> componentsToBeRemovedFromDbMap = (HashMap<String, Component>) componentsFromDb.clone(); // we don't want to change the actual componentsFromDb list, but we need to create the componentsToBeRemovedFromDb list from it.
        componentsToBeRemovedFromDbMap.keySet().removeAll(componentsFromTask.keySet());
        if (componentsToBeRemovedFromDbMap.isEmpty()) {
            return false;
        }

        List<Component> componentsToBeRemovedFromDb = new ArrayList<>(componentsToBeRemovedFromDbMap.values());
        componentsToBeRemovedFromDb.forEach(componentToBeRemoved -> {
            if (!componentToBeRemoved.getParentId().equals(taskId) && !componentsFromDb.containsKey(componentToBeRemoved.getParentId())) { // no need to update the parent's componentsIds list because it'll also be removed from DB.
                Component parent = componentsFromDb.get(componentToBeRemoved.getParentId());
                parent.getComponentsIds().remove(componentToBeRemoved.getId());
                parent.updateLastModifiedDate();
            }

            //TODO: Remove any files that relates to the component from file system.
        });
        DB.delete(componentsToBeRemovedFromDb);

        // Change the componentsFromDb so it'll hold the remaining components which we didn;t delete
        componentsFromDb.keySet().removeAll(componentsToBeRemovedFromDbMap.keySet());
        return true;
    }

    /**
     * Sets the component with the given taskId and parentId, and all of its children with the given taskId and each one
     * with its own parentId. Also sets each component in the tree with its own componentsIds list by iterating its
     * components children and fetching their ids.
     * @param component The root component to be set.
     * @param taskId The id of the task of the root component.
     * @param parentId The id of the parent of the root component. May be the task id if the component's parent ids the
     *                 task itself.
     */
    private List<Component> setDetailsToComponentsTree(Component component, String taskId, String parentId) {
        List<Component> components = new ArrayList<>();
        component.setParentId(parentId);
        component.setTaskId(taskId);
        component.getComponents().forEach(child -> {
            component.getComponentsIds().add(child.getId());
            components.addAll(setDetailsToComponentsTree(child, taskId, component.getId()));
        });

        // we don't want the component's children to be saved in the components collection in DB, just their ids.
        Component componentWithoutChildrenList = component.copyWithoutChildren();
        components.add(componentWithoutChildrenList);
        return components;
    }

    /**
     * Deletes a task and all of its components from DB. Also removes all linked files that are only connected to this
     * task or its components children from file system.
     * @param id the id of the task to be deleted.
     * @throws DbException
     */
    @Override
    public void deleteTaskFromDb(String id) throws DbException {
        logger.debug(String.format("About to remove task %s and all its components from DB.", id));
        DB.delete(id);
        DB.deleteByView(Design.COMPONENT, View.BY_TASK_ID, Component.class, id);
        //todo: Remove linked files that are only connected to this task
        logger.debug(String.format("Task %s and all its components were successfully removed from DB.", id));
    }

    /**
     * Adds a new component to a task. Saves the new component to DB and also updates the parent component and its task
     * with this new added component id and puts it in the required position within its parent's components list.
     * @param componentRequest an object which holds the new component to be added and
     *                         the position which the component will be inserted to within its parent's components list.
     * @return a map that maps the tempIds of the new component and all of its children components to their server ids.
     * @throws DbException
     */
    @Override
    public Map<String, String> addComponent(AddComponentRequest componentRequest) throws DbException, EntityNotFoundException {
        Component component = componentRequest.getComponent();
        int position = componentRequest.getPosition();
        Task task = DB.find(component.getTaskId(), Task.class);

        // if the component's parent is the task, add the component to the task.
        List<Component> componentsToBeSavedToDb = new ArrayList<>();
        if (component.getParentId().equals(component.getTaskId())) {
            task.addChildComponent(component, position);
            componentsToBeSavedToDb.addAll(setDetailsToComponentsTree(component, component.getTaskId(), component.getTaskId()));
        } else { // the component's parent is one of the task children components - find that parent component and update it with the new component in task and in DB.
            Component parentComponentFromDb = addChildComponentToParentComponentsIdsListInDb(component, position);
            componentsToBeSavedToDb.add(parentComponentFromDb);

            Component parentComponentFromTask = addChildComponentToParentComponentsListInTask(component, position, task);
            componentsToBeSavedToDb.addAll(setDetailsToComponentsTree(component, parentComponentFromTask.getTaskId(), parentComponentFromTask.getId()));
        }

        DB.save(componentsToBeSavedToDb);
        task.updateLastModifiedDate();
        DB.save(task);
        return component.getCIdsToServerIdsMap();
    }

    /**
     * Adds a component to the components list of its parent in a specific position.
     * @param component the component to be added to its parent's components list.
     * @param position the index in which the given component will be inserted in its parent's components list.
     * @param task the task that contains the component as one of its children.
     * @return the updated parent.
     */
    private Component addChildComponentToParentComponentsListInTask(Component component, int position, Task task) {
        Component parentComponentFromTask = task.getComponentById(component.getParentId());
        parentComponentFromTask.getComponents().add(position, component);
        parentComponentFromTask.updateLastModifiedDate();
        return parentComponentFromTask;
    }

    /**
     * Adds a component to the componentsIds list of its parent in a specific position.
     * @param component the component to be added to its parent's componentsIds list.
     * @param position the index in which the given component will be inserted in its parent's componentsIds list.
     * @return the updated parent.
     */
    private Component addChildComponentToParentComponentsIdsListInDb(Component component, int position) throws DbException, EntityNotFoundException {
        Component parentComponentFromDb = DB.find(component.getParentId(), Component.class);
        parentComponentFromDb.addChildComponentIdToComponentsIdsList(component, position);
        parentComponentFromDb.updateLastModifiedDate();
        return parentComponentFromDb;
    }

    /**
     * Adds multiple components trees to task. Saves each new component to DB and also updates the parent components and
     * the task with these new added components ids and puts them in the required position within their parents' components lists.
     * @param componentsRequest a list of objects where each object contains a tree of components and the position in which
     *                          the root component will be added within int parent's components list.
     * @param taskId the id of the task we want to add the components trees to.
     * @return a map that maps the tempIds of the new component and all of its children components to their server ids.
     * @throws Exception
     */
    @Override
    public Map<String, String> addMultipleComponents(List<AddComponentRequest> componentsRequest, String taskId) throws Exception {
        Map<String, String> tempIdsToServerIdsMap = new HashMap<>();
        for (AddComponentRequest componentRequest : componentsRequest) {
            tempIdsToServerIdsMap.putAll(addComponent(componentRequest));
        }

        return tempIdsToServerIdsMap;
    }

    @Override
    public void updateComponentData(Component component) throws DbException, EntityNotFoundException {
        Task task = DB.find(component.getTaskId(), Task.class);
        Component childToBeUpdated = task.getComponentById(component.getId());
        if (childToBeUpdated == null) {
            String errorMsg = String.format("Component of id %s doesn't exist in task %s", component.getId(), component.getTaskId());
            logger.error(errorMsg);
            throw new EntityNotFoundException(errorMsg);
        }

        childToBeUpdated.setData(component.getData());
        childToBeUpdated.updateLastModifiedDate();
        DB.save(childToBeUpdated.copyWithoutChildren());

        task.updateLastModifiedDate();
        DB.save(task);
    }

    /**
     * Deletes a component and all its components children from both its parent component within a task and from the components
     * collection in DB. Also removes all linked files that are only connected to this component or its children from file system.
     * @param componentId the id of the component to be deleted.
     * @param taskId the id of the task that contains the component to delete as one of its children.
     * @throws DbException
     */
    @Override
    public void deleteComponent(String componentId, String taskId) throws DbException, EntityNotFoundException {
        Task task = DB.find(taskId, Task.class);
        Component componentToRemove = task.getComponentById(componentId);
        if (componentToRemove.getParentId().equals(taskId)) {
            task.getComponents().remove(componentToRemove);
        } else {
            Component parentComponent = task.getComponentById(componentToRemove.getParentId());
            parentComponent.getComponents().remove(componentToRemove);
            parentComponent.getComponentsIds().remove(componentToRemove.getId());
            parentComponent.updateLastModifiedDate();
            DB.save(parentComponent);
        }

        //todo: Remove linked files that are only connected to this component and its children
        DB.delete(componentToRemove.getFlattenComponentsTree());
        task.updateLastModifiedDate();
        DB.save(task);
    }
}