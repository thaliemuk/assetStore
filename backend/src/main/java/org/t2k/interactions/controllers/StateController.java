package org.t2k.interactions.controllers;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.t2k.interactions.dal.DB;
import org.t2k.interactions.dal.Design;
import org.t2k.interactions.dal.View;
import org.t2k.interactions.models.State;
import org.t2k.interactions.models.exceptions.DbException;
import org.t2k.interactions.models.exceptions.EntityNotFoundException;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 01/09/2015
 * Time: 09:03
 */
@Controller
@RequestMapping("/states")
public class StateController {

    private Logger logger = Logger.getLogger(this.getClass());

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    State getState(@PathVariable String id) throws DbException, EntityNotFoundException {
        logger.debug(String.format("About to get state %s.", id));
        State state = DB.find(id, State.class);
        logger.debug(String.format("State %s had been successfully fetched.", state.getId()));
        return state;
    }

    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    List<State> getStatesByUserIdAndTaskId(@RequestParam String userId, @RequestParam String taskId) throws DbException, EntityNotFoundException {
        logger.debug(String.format("About to get all states of userId: %s and taskId: %s.", userId, taskId));
        List<State> states = DB.findByCompoundIndexView(Design.STATE, View.BY_USER_ID_TASK_ID, State.class, Arrays.asList(userId, taskId));
        logger.debug(String.format("%s states of userId: %s and taskId: %s have been successfully fetched.", states.size(), userId, taskId));
        return states;
    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    String addState(@RequestBody State state) throws DbException {
        logger.debug(String.format("About to add a new state. id: %s, client id: %s.", state.getId(), state.getCId()));
        DB.save(state);
        logger.debug(String.format("State %s had been successfully added.", state.getId()));
        return state.getId();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public
    @ResponseBody
    void updateState(@RequestBody State state, @PathVariable String id) throws Exception {
        logger.debug(String.format("About to update state: %s.", id));
        if (!id.equals(state.getId())) {
            String errorMsg = String.format("The state's id %s isn't equal to the stateId %s from the request's path", state.getId(), id);
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }

        state.updateLastModifiedDate();
        DB.save(state);
        logger.debug(String.format("State %s had been successfully updated.", state.getId()));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    void deleteState(@PathVariable String id) throws DbException {
        logger.debug(String.format("About to delete state %s.", id));
        DB.delete(id);
        logger.debug(String.format("State %s had been successfully deleted.", id));
    }
}