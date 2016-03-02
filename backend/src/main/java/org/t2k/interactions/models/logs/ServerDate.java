package org.t2k.interactions.models.logs;

import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 03/09/2015
 * Time: 16:41
 */
public class ServerDate {

    public TimeZone timeZone;
    public String dateFormat;
    public String serverDateTime;

    public ServerDate(TimeZone timeZone, String dateFormat, String serverDateTime) {
        this.timeZone = timeZone;
        this.dateFormat = dateFormat;
        this.serverDateTime = serverDateTime;
    }
}