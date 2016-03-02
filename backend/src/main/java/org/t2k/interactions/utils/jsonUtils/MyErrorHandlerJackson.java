package org.t2k.interactions.utils.jsonUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 25/08/2015
 * Time: 12:26
 */
public class MyErrorHandlerJackson extends DeserializationProblemHandler {

    private Logger logger = Logger.getLogger(MyErrorHandlerJackson.class);

    public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser jp, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {
        JsonParser jsonParser = ctxt.getParser();
        // very simple, just to verify that we do see correct token type
        logger.error(String.format("Unknown property: %s:%s in class: %s", propertyName, jsonParser.getCurrentToken().toString(), beanOrClass.toString()));
        // Yup, we are good to go; must skip whatever value we'd have:
        jp.skipChildren();
        return true;
    }
}