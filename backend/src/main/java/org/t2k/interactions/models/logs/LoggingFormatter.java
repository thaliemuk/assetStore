package org.t2k.interactions.models.logs;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 03/09/2015
 * Time: 16:43
 */
public class LoggingFormatter {

    // Creates the line to be logged based on:
    // UTC-time, logLevel, logSource, accountId, username, ip, loggingCategory, [data]
    public String getFinalLogString(String loggingCategory,String data) {
        StringBuilder logStringBuilder = new StringBuilder("");
        logStringBuilder.append(loggingCategory);
        logStringBuilder.append(": ");
        logStringBuilder.append(data);
        return logStringBuilder.toString();
    }
}