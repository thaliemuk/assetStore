package org.t2k.interactions.services.logs;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.Priority;
import org.springframework.beans.factory.annotation.Autowired;
import org.t2k.interactions.models.logs.FrontEndLog;
import org.t2k.interactions.models.logs.LoggingFormatter;
import org.t2k.interactions.models.logs.ServerDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 03/09/2015
 * Time: 16:44
 */
public class LoggingServiceImpl implements LoggingService {

    private Logger logger = Logger.getLogger(this.getClass());

    private String defaultDateFormat = "dd/mm/yy hh:mm:ss:SSS";
    private enum TimeZoneFormat {UTC, GMT}

    @Autowired
    private LoggingFormatter loggingFormatter;

    @Override
    public ServerDate getServerDateTime(String timeZoneStr, String dateFormatStr) {
        String upperCaseTimeZone = timeZoneStr.toUpperCase();
        TimeZone timeZone = TimeZone.getTimeZone(upperCaseTimeZone); // The specified TimeZone, or the GMT zone if the given ID cannot be understood.
        try {
            TimeZoneFormat.valueOf(upperCaseTimeZone); // In order to check that the value exists we use valueOf which throws an exception if value is invalid.
        } catch (Exception e) {
            String msg = String.format("The time zone \"%s\" is invalid. Setting the time zone to default UTC time zone.", upperCaseTimeZone);
            logger.warn(msg, e);
            timeZone = TimeZone.getTimeZone(TimeZoneFormat.UTC.name());
        }

        DateFormat dateFormat;
        try {
            dateFormat = new SimpleDateFormat(dateFormatStr);
        } catch (Exception e) {
            String msg = String.format("Failed to format the given date: %s. Using default date format: %s", dateFormatStr, defaultDateFormat);
            logger.warn(msg);
            dateFormatStr = defaultDateFormat;
            dateFormat = new SimpleDateFormat(dateFormatStr);
        }

        dateFormat.setTimeZone(timeZone);
        String serverDateTime = dateFormat.format(new Date());
        ServerDate serverDate = new ServerDate(timeZone, dateFormatStr, serverDateTime);
        return serverDate;
    }

    /***
     * Get a collection of frontEndLogs and log them
     * @param frontEndLogs
     */
    @Override
    public void logFrontendLogs(List<FrontEndLog> frontEndLogs){
        for (FrontEndLog log : frontEndLogs){
            logFrontendDatum(log);
        }
    }

    /***
     * Log a single front end log
     * @param frontEndLog
     */
    private void logFrontendDatum(FrontEndLog frontEndLog){
        Priority priority = Level.toLevel(frontEndLog.getLogLevel());
        String message = loggingFormatter.getFinalLogString(frontEndLog.getLoggingCategory(),frontEndLog.getData());
        MDC.put("frontEndDate", frontEndLog.getCurrentUtcTime()); // Add the frontEndTime to be accessed by log4j appenders
        MDC.put("frontEndUser",frontEndLog.getUsername());
        MDC.put("frontEndAccountId",frontEndLog.getAccountId());
        logger.log(priority, message);
        MDC.remove("frontEndDate");
        MDC.remove("frontEndUser");
        MDC.remove("frontEndAccountId");
    }
}