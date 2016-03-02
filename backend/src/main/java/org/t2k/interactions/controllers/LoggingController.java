package org.t2k.interactions.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.t2k.interactions.models.logs.FrontEndLog;
import org.t2k.interactions.models.logs.ServerDate;
import org.t2k.interactions.services.logs.LoggingService;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 03/09/2015
 * Time: 16:39
 */
@Controller
@RequestMapping("/logs")
public class LoggingController {

    @Autowired
    private LoggingService loggingService;

    @RequestMapping(value = "/logFrontEndData", method = RequestMethod.POST)
    public
    @ResponseBody
    void logFrontEndData(@RequestBody FrontEndLogsList frontEndLogs) throws Exception {
        loggingService.logFrontendLogs(frontEndLogs);
    }

    @RequestMapping(value = "/serverDateTime", method = RequestMethod.GET)
    public
    @ResponseBody
    ServerDate getServerDateTime(@RequestParam(value = "timezone", required = true) String timeZone, @RequestParam(value = "dateformat", required = true) String dateFormat) throws Exception {
        return loggingService.getServerDateTime(timeZone, dateFormat);
    }

    private static class FrontEndLogsList extends ArrayList<FrontEndLog> { }
}