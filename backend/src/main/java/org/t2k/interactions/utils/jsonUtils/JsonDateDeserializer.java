package org.t2k.interactions.utils.jsonUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 20/08/2015
 * Time: 16:08
 */
public class JsonDateDeserializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String date = jsonParser.getText();
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}