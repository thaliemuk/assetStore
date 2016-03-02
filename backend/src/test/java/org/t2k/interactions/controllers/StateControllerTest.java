package org.t2k.interactions.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.t2k.interactions.dal.DB;
import org.t2k.interactions.models.State;
import org.t2k.interactions.models.Type;
import org.t2k.interactions.models.exceptions.EntityNotFoundException;
import org.t2k.interactions.utils.GeneralUtils;
import org.t2k.interactions.utils.jsonUtils.JsonWrapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 01/09/2015
 * Time: 09:50
 */
@WebAppConfiguration
@ContextConfiguration("classpath:/springContext/applicationContext-rest.xml")
public class StateControllerTest extends AbstractTestNGSpringContextTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JsonWrapper jsonWrapper;

    private List<State> mockStateAddedToDB;

//    @BeforeClass
//    public void setup() {
//        // Setup web application mock
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//
//        // Initialize mockTasksAddedToDB list
//        mockStateAddedToDB = new ArrayList<>();
//    }
//
//    @AfterClass
//    public void cleanup() {
//        if (mockStateAddedToDB.isEmpty()) {
//            return;
//        }
//
//        try {
//            DB.delete(mockStateAddedToDB);
//        } catch (DbException e) {
//            logger.error(String.format("Failed to delete the test states from DB after %s had finished.", StateControllerTest.class.getSimpleName()));
//        }
//    }

    @Test(enabled = false)
    public void addStateTest() throws Exception {
        State state = createMockState("states/state.json");
        mockStateAddedToDB.add(state);
        String jsonState = jsonWrapper.writeValueAsString(state);

        mockMvc.perform(post("/states")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonState))
                .andExpect(status().isOk())
                .andExpect(content().string(jsonWrapper.writeValueAsString(state.getId())));

        // validate that the state was saved in DB
        State stateFromDb = DB.find(state.getId(), State.class);
        validateState(stateFromDb);
    }

    @Test(enabled = false)
    public void getStateTest() throws Exception {
        State state = createAndSaveStateToDb();
        MvcResult mvcResult = mockMvc.perform(get(String.format("/states/%s", state.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(jsonWrapper.writeValueAsString(state)))
                .andReturn();

        State stateResponse = jsonWrapper.readValue(mvcResult.getResponse().getContentAsString(), State.class);
        validateState(stateResponse);
    }

    @Test(enabled = false)
    public void getStateByUserIdAndTaskIdTest() throws Exception {
        State state1 = createAndSaveStateToDb("states/state1.json");
        State state2 = createAndSaveStateToDb("states/state2.json");
        MvcResult mvcResult = mockMvc.perform(get(String.format("/states?userId=%s&taskId=%s", state1.getUserId(), state1.getTaskId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(jsonWrapper.writeValueAsString(Arrays.asList(state1))))
                .andReturn();

        List<State> stateResponse = jsonWrapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<State>>(){});
        Assert.assertEquals(stateResponse.size(), 1);
        validateState(stateResponse.get(0));
        Assert.assertEquals(stateResponse.get(0).getId(), state1.getId());
    }

    @Test(enabled = false)
    public void updateStateTest() throws Exception {
        State state = createAndSaveStateToDb();

        state.getComponents().clear();
        JsonNode stateNode = jsonWrapper.readTree(jsonWrapper.writeValueAsString(state));

        String newTaskVersion = "2.0";
        ((ObjectNode)stateNode).put("taskVersion", newTaskVersion);

        int newScore = 100;
        int newMaxAttempts = 2;
        int newCurrentAttempt = 1;
        String newProgressState = "in_progress";
        JsonNode dataNode = stateNode.get("data");
        ((ObjectNode)dataNode).put("score", newScore);
        ((ObjectNode)dataNode).put("maxAttempts", newMaxAttempts);
        ((ObjectNode)dataNode).put("currentAttempt", newCurrentAttempt);
        ((ObjectNode)dataNode).put("progressState", newProgressState);

        mockMvc.perform(put(String.format("/states/%s", state.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(stateNode.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        State updatedState = DB.find(state.getId(), State.class);
        validateState(updatedState);
        Assert.assertNotNull(updatedState.getLastModifiedDate());
        Assert.assertEquals(0, updatedState.getComponents().size());
        Assert.assertEquals(dataNode.toString(), jsonWrapper.writeValueAsString(updatedState.getData()));
    }

    @Test(expectedExceptions = EntityNotFoundException.class, enabled = false)
    public void deleteStateTest() throws Exception {
        State state = createAndSaveStateToDb();
        mockMvc.perform(delete(String.format("/states/%s", state.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        DB.find(state.getId(), State.class); // should throw an exception because the state was supposed to be deleted from DB.
        Assert.fail(String.format("The DELETE state API failed to delete state: %s", state.getId()));
    }

    /**
     * Creates a new state.
     *
     * @return a new state with components and data.
     * @throws IOException if failed to read the state's JSON file.
     */
    private State createMockState(String path) throws Exception {
        String stateJson = GeneralUtils.readResourcesAsString(this.getClass(), path);
        return jsonWrapper.readValue(stateJson, State.class);
    }

    private State createAndSaveStateToDb() throws Exception {
        return createAndSaveStateToDb("states/state.json");
    }

    private State createAndSaveStateToDb(String path) throws Exception {
        State state = createMockState(path);
        DB.save(state);
        mockStateAddedToDB.add(state);
        return state;
    }

    private void validateState(State state) {
        Assert.assertNotNull(state);
        Assert.assertNotNull(state.getId());
        Assert.assertEquals(Type.STATE.toString(), state.getType());
        Assert.assertNotNull(state.getUserId());
        Assert.assertNotNull(state.getTaskId());
        Assert.assertNotNull(state.getTaskVersion());
        Assert.assertFalse(state.getTaskVersion().isEmpty());
    }
}