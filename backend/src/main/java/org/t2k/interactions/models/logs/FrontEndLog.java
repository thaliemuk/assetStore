package org.t2k.interactions.models.logs;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 03/09/2015
 * Time: 16:42
 */
public class FrontEndLog {

    private String defaultValue="unavailable";
    private String loggingCategory= defaultValue;
    private String data= defaultValue;
    private String currentUtcTime = defaultValue;
    private String logLevel = defaultValue;
    private String accountId = defaultValue;
    private String username = defaultValue;

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getCurrentUtcTime() {
        return currentUtcTime;
    }

    public void setCurrentUtcTime(String currentUtcTime) {
        this.currentUtcTime = currentUtcTime;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getLoggingCategory() {
        return loggingCategory;
    }

    public void setLoggingCategory(String loggingCategory) {
        this.loggingCategory = loggingCategory;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}