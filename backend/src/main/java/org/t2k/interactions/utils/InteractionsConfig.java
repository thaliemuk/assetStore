package org.t2k.interactions.utils;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 30/06/2015
 * Time: 19:44
 */
public class InteractionsConfig {

    private Logger logger = Logger.getLogger(InteractionsConfig.class);

    private Properties properties = new Properties();

    private void init() throws IOException {
        try {
            properties.load(GeneralUtils.getResourceAsStream(InteractionsConfig.class, "application.properties"));
        } catch (IOException e) {
            logger.error("Failed to load application.properties file.", e);
            throw e;
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}