package org.t2k.interactions.services.logs;

import org.t2k.interactions.models.logs.FrontEndLog;
import org.t2k.interactions.models.logs.ServerDate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 03/09/2015
 * Time: 16:40
 */
public interface LoggingService {

    /**
     * Gets the server's date time in a requested format by a specific time zone.
     * @param timeZoneStr - such as UTC, GMT ect.
     * @param dateFormatStr - the format in which the date should be set to.
     * @return a ServerDate object that contains the requested time zone, date format and the server's date time
     * according to the given time zone and date format
     */
    public ServerDate getServerDateTime(String timeZoneStr, String dateFormatStr);

    /***
     * Get a collection of frontEndLogs and log them
     * @param frontEndLogs - log received from client
     */
    public void logFrontendLogs(List<FrontEndLog> frontEndLogs);
}