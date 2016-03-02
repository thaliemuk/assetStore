package org.t2k.interactions.utils.jsonUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.t2k.interactions.utils.InteractionsConfig;

import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 23/08/2015
 * Time: 14:53
 */
@Primary
public class JsonWrapper extends ObjectMapper{

    @Autowired
    private JsonWrapper(InteractionsConfig interactionsConfig) {
        super();
        DeserializationProblemHandler errorHandler = new MyErrorHandlerJackson();
        setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        setSerializationInclusion(JsonInclude.Include.NON_NULL);
        setDateFormat(new SimpleDateFormat(interactionsConfig.getProperty("date.format")));
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        addHandler(errorHandler);
    }
}