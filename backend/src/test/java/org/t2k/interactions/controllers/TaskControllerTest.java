package org.t2k.interactions.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.t2k.interactions.dal.DB;
import org.t2k.interactions.dal.Design;
import org.t2k.interactions.dal.View;
import org.t2k.interactions.models.AddComponentRequest;
import org.t2k.interactions.models.Component;
import org.t2k.interactions.models.Task;
import org.t2k.interactions.models.exceptions.EntityNotFoundException;
import org.t2k.interactions.services.tasks.TaskService;
import org.t2k.interactions.utils.GeneralUtils;
import org.t2k.interactions.utils.jsonUtils.JsonWrapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 19/07/2015
 * Time: 08:06
 */
@WebAppConfiguration
@ContextConfiguration("classpath:/springContext/applicationContext-rest.xml")
public class TaskControllerTest extends AbstractTestNGSpringContextTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private JsonWrapper jsonWrapper;

    private List<Task> mockTasksAddedToDB;

//    @BeforeClass
//    public void setup() {
//        // Setup web application mock
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//
//        // Initialize mockTasksAddedToDB list
//        mockTasksAddedToDB = new ArrayList<>();
//    }
//
//    @AfterClass
//    public void cleanup() {
//        mockTasksAddedToDB.forEach(task -> {
//            if (task != null) {
//                try {
//                    taskService.deleteTaskFromDb(task.getId());
//                } catch (DbException e) {
//                    logger.error(String.format("Failed to delete the test task: %s from DB after %s had finished.", task.getId(), TaskControllerTest.class));
//                }
//            }
//        });
//    }

    @Test(enabled = false)
    public void addTaskTest() throws Exception {
        Task task = createMockTask("tasks/task.json");
        mockTasksAddedToDB.add(task);
        String jsonTask = jsonWrapper.writeValueAsString(task);

        Map<String, String> cIdsToServerIdsMap = new HashMap<>();
        cIdsToServerIdsMap.put(task.getCId(), task.getId());
        task.getFlattenComponentsTree().forEach(component -> cIdsToServerIdsMap.put(component.getCId(), component.getId()));

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTask))
                .andExpect(status().isOk())
                .andExpect(content().string(jsonWrapper.writeValueAsString(cIdsToServerIdsMap)));

        // validate that the task and all its components were saved in DB
        Task taskFromDb = DB.find(task.getId(), Task.class);
        Assert.assertNotNull(taskFromDb);
        Assert.assertTrue(cIdsToServerIdsMap.containsValue(taskFromDb.getId()));
        int totalNumberOfComponentsInTask = task.getFlattenComponentsTree().size();
        Assert.assertEquals(taskFromDb.getFlattenComponentsTree().size(), totalNumberOfComponentsInTask);
        List<Component> componentsFromDb = DB.findByView(Design.COMPONENT, View.BY_TASK_ID, Component.class, task.getId());
        Assert.assertEquals(componentsFromDb.size(), totalNumberOfComponentsInTask);
        Assert.assertEquals(cIdsToServerIdsMap.size() - 1, totalNumberOfComponentsInTask); // -1 because the task doesn't count
    }

    @Test(enabled = false)
    public void getTaskTest() throws Exception {
        Task task = createAndSaveTaskAndComponentsToDb();
        mockMvc.perform(get(String.format("/tasks/%s", task.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(jsonWrapper.writeValueAsString(task)));
    }

    @Test(enabled = false)
    public void updateTaskWithNoChangesToTaskTest() throws Exception {
        Task task = createAndSaveTaskAndComponentsToDb();
        String jsonTask = jsonWrapper.writeValueAsString(task);

        mockMvc.perform(put(String.format("/tasks/%s", task.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTask))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));

        // Validate nothing changed in the task from DB
        Task updatedTask = DB.find(task.getId(), Task.class);
        Assert.assertNull(updatedTask.getLastModifiedDate());
        Assert.assertEquals(updatedTask.getComponents().size(), 3);
        for (int i = 0 ; i < updatedTask.getComponents().size() ; i++) {
            Assert.assertEquals(task.getComponents().get(i).getId(), updatedTask.getComponents().get(i).getId());
            Assert.assertEquals(updatedTask.getId(), task.getComponents().get(i).getTaskId());
            Assert.assertEquals(updatedTask.getId(), task.getComponents().get(i).getParentId());
        }

        // Validate the components from the collection in DB, is the same
        List<Component> componentsFromDb = DB.findByView(Design.COMPONENT, View.BY_TASK_ID, Component.class, updatedTask.getId());
        Assert.assertEquals(updatedTask.getFlattenComponentsTree().size(), componentsFromDb.size());
    }


    @Test(enabled = false)
    public void updateTaskRemoveComponentTest() throws Exception {
        Task task = createAndSaveTaskAndComponentsToDb();
        Component componentToBeRemoved = task.getComponents().get(1);
        task.getComponents().remove(componentToBeRemoved);
        String jsonTask = jsonWrapper.writeValueAsString(task);

        mockMvc.perform(put(String.format("/tasks/%s", task.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTask))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));

        // Validate the component had been removed from the task in DB and from the components collection in DB
        Task updatedTask = DB.find(task.getId(), Task.class);
        Assert.assertNull(updatedTask.getComponentById(componentToBeRemoved.getId()));

        List<Component> componentsToBeRemoved = componentToBeRemoved.getFlattenComponentsTree();
        List<String> idsOfComponentsToBeRemoved = new ArrayList<>();
        componentsToBeRemoved.forEach(component -> idsOfComponentsToBeRemoved.add(component.getId()));
        try {
            DB.find(idsOfComponentsToBeRemoved, Component.class);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof EntityNotFoundException);
            idsOfComponentsToBeRemoved.forEach(id -> Assert.assertTrue(e.getMessage().contains(id)));
        }

        // Verify that the components which weren't deleted are still bind to the task and are still in DB
        List<String> remainingComponentsIds = Arrays.asList(task.getComponents().get(0).getId(), task.getComponents().get(1).getId());
        Assert.assertNotNull(updatedTask.getComponentById(remainingComponentsIds.get(0)));
        Assert.assertNotNull(updatedTask.getComponentById(remainingComponentsIds.get(1)));

        List<Component> remainingComponentsFromDb = DB.find(remainingComponentsIds, Component.class);
        Assert.assertEquals(remainingComponentsFromDb.size(), 2);
    }

    @Test(enabled = false)
    public void updateTaskAddingNewComponentDirectlyToTaskTest() throws Exception {
        Task task = createAndSaveTaskAndComponentsToDb();
        Component componentToBeAdded = createMockComponent("components/imageComponent.json");
        int componentIndex = 1;
        task.getComponents().add(componentIndex, componentToBeAdded);
        String jsonTask = jsonWrapper.writeValueAsString(task);
        Map<String, String> expectedMap = componentToBeAdded.getCIdsToServerIdsMap();

        mockMvc.perform(put(String.format("/tasks/%s", task.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTask))
                .andExpect(status().isOk())
                .andExpect(content().string(jsonWrapper.writeValueAsString(expectedMap)));

        // Validate the component had been added to the task in DB
        Task updatedTask = DB.find(task.getId(), Task.class);
        Component addedComponent = updatedTask.getComponentById(componentToBeAdded.getId());
        Assert.assertNotNull(addedComponent);
        Assert.assertEquals(updatedTask.getId(), addedComponent.getTaskId());
        Assert.assertEquals(updatedTask.getId(), addedComponent.getParentId());

        // Validate the component had been added to the components collection in DB
        DB.find(componentToBeAdded.getId(), Component.class); // if the components isn't found, EntityNotFoundException would have been thrown
    }

    @Test(enabled = false)
    public void updateTaskAddingNewComponentToTaskComponentTest() throws Exception {
        Task task = createAndSaveTaskAndComponentsToDb();
        Component componentToBeAdded = createMockComponent("components/imageComponent.json");
        int componentIndex = 1;
        int mcComponentIndex = 1;
        Component mcComponent = task.getComponents().get(mcComponentIndex);
        mcComponent.getComponents().add(componentIndex, componentToBeAdded);
        String jsonTask = jsonWrapper.writeValueAsString(task);
        Map<String, String> expectedMap = componentToBeAdded.getCIdsToServerIdsMap();

        mockMvc.perform(put(String.format("/tasks/%s", task.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTask))
                .andExpect(status().isOk())
                .andExpect(content().string(jsonWrapper.writeValueAsString(expectedMap)));

        // Validate the component had been added to the task in DB
        Task updatedTask = DB.find(task.getId(), Task.class);
        Component addedComponent = updatedTask.getComponentById(componentToBeAdded.getId());
        Assert.assertNotNull(addedComponent);
        Assert.assertEquals(updatedTask.getId(), addedComponent.getTaskId());
        Assert.assertEquals(mcComponent.getId(), addedComponent.getParentId());

        // Validate the component's parent contains this new added component in its list of components ion the requested index
        Component addedComponentParent = updatedTask.getComponentById(addedComponent.getParentId());
        Assert.assertEquals(mcComponent.getId(), addedComponentParent.getId());
        Assert.assertEquals(addedComponent.getId(), addedComponentParent.getComponents().get(componentIndex).getId());

        // Validate the component had been added to the components collection in DB
        Component componentFromDb = DB.find(componentToBeAdded.getId(), Component.class);

        // Validate the component's id was added to the parent component's list of componentsIds in the components collection in DB
        Component componentParentFromDb = DB.find(mcComponent.getId(), Component.class);
        Assert.assertEquals(componentFromDb.getId(), componentParentFromDb.getComponentsIds().get(componentIndex));
    }

    @Test(enabled = false)
    public void updateTaskReplaceComponentWithNewComponentTest() throws Exception {
        Task task = createAndSaveTaskAndComponentsToDb();
        Component componentToBeAdded = createMockComponent("components/imageComponent.json");
        int componentIndex = 1;
        int mcComponentIndex = 1;
        Component mcComponent = task.getComponents().get(mcComponentIndex);
        Component componentToBeRemoved = mcComponent.getComponents().remove(componentIndex);
        mcComponent.getComponents().add(componentIndex, componentToBeAdded);
        String jsonTask = jsonWrapper.writeValueAsString(task);
        Map<String, String> expectedMap = componentToBeAdded.getCIdsToServerIdsMap();

        mockMvc.perform(put(String.format("/tasks/%s", task.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTask))
                .andExpect(status().isOk())
                .andExpect(content().string(jsonWrapper.writeValueAsString(expectedMap)));

        // Validate the replaced component was removed from the task
        Task updatedTask = DB.find(task.getId(), Task.class);
        Component removedComponent = updatedTask.getComponentById(componentToBeRemoved.getId());
        Assert.assertNull(removedComponent);

        // Validate the replaced component was removed from the components collection in DB
        try {
            DB.find(componentToBeRemoved.getId(), Component.class);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof EntityNotFoundException);
            Assert.assertTrue(e.getMessage().contains(componentToBeRemoved.getId()));
        }

        // Validate the component had been added to the task in DB
        Component addedComponent = updatedTask.getComponentById(componentToBeAdded.getId());
        Assert.assertNotNull(addedComponent);
        Assert.assertEquals(updatedTask.getId(), addedComponent.getTaskId());
        Assert.assertEquals(mcComponent.getId(), addedComponent.getParentId());

        // Validate the component's parent contains this new added component in its list of components ion the requested index
        Component addedComponentParent = updatedTask.getComponentById(addedComponent.getParentId());
        Assert.assertEquals(mcComponent.getId(), addedComponentParent.getId());
        Assert.assertEquals(addedComponent.getId(), addedComponentParent.getComponents().get(componentIndex).getId());

        // Validate the component had been added to the components collection in DB
        Component componentFromDb = DB.find(componentToBeAdded.getId(), Component.class);

        // Validate the component's id was added to the parent component's list of componentsIds in the components collection in DB
        Component componentParentFromDb = DB.find(mcComponent.getId(), Component.class);
        Assert.assertEquals(componentFromDb.getId(), componentParentFromDb.getComponentsIds().get(componentIndex));
    }

    @Test(enabled = false)
    public void updateTaskMoveComponentFromParentComponentToAnotherParentComponentTest() throws Exception {
        Task task = createAndSaveTaskAndComponentsToDb();
        int oldComponentIndex = 1;
        int newComponentIndex = 0;
        int questionComponentIndex = 0;
        int mcComponentIndex = 1;
        Component questionComponent = task.getComponents().get(questionComponentIndex);
        Component mcComponent = task.getComponents().get(mcComponentIndex);
        Component movingComponent = mcComponent.getComponents().remove(oldComponentIndex);
        questionComponent.getComponents().add(newComponentIndex, movingComponent);
        String jsonTask = jsonWrapper.writeValueAsString(task);

        mockMvc.perform(put(String.format("/tasks/%s", task.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTask))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));

        // Validate the component was moved in the task in DB
        Task updatedTask = DB.find(task.getId(), Task.class);
        Component movedComponent = updatedTask.getComponentById(movingComponent.getId());
        Assert.assertNotNull(movedComponent);
        Assert.assertEquals(updatedTask.getId(), movedComponent.getTaskId());
        Assert.assertEquals(questionComponent.getId(), movedComponent.getParentId());
        // Validate the parent component in the task now contains the moved component in the right index
        Component updatedQuestionComponent = updatedTask.getComponentById(questionComponent.getId());
        Assert.assertEquals(movedComponent.getId(), updatedQuestionComponent.getComponents().get(newComponentIndex).getId());
        // Validate the old parent now doesn't contain the moved component in its components list
        Component updatedMcComponent = updatedTask.getComponentById(mcComponent.getId());
        updatedMcComponent.getComponents().forEach(component -> Assert.assertNotEquals(component.getId(), movedComponent.getId()));


        // Validate the component had been added to the components collection in DB
        Component componentFromDb = DB.find(movedComponent.getId(), Component.class);

        // Validate the moved component's id was added to the new parent component's list of componentsIds in the components collection in DB
        Component questionParentFromDb = DB.find(questionComponent.getId(), Component.class);
        Assert.assertEquals(componentFromDb.getId(), questionParentFromDb.getComponentsIds().get(newComponentIndex));

        // Validate the moved component's id was removed from the old parent component's list of componentsIds in the components collection in DB
        Component mcComponentParentFromDb = DB.find(mcComponent.getId(), Component.class);
        Assert.assertFalse(mcComponentParentFromDb.getComponentsIds().contains(movedComponent.getId()));
    }

    @Test(enabled = false)
    public void updateTaskWithNoComponentsTest() throws Exception {
        Task task = createAndSaveEmptyTaskToDb();
        Component componentToBeAdded = createMockComponent("components/mcOptionComponent.json");
        task.getComponents().add(componentToBeAdded);
        String jsonTask = jsonWrapper.writeValueAsString(task);

        mockMvc.perform(put(String.format("/tasks/%s", task.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTask))
                .andExpect(status().isOk())
                .andExpect(content().string(jsonWrapper.writeValueAsString(componentToBeAdded.getCIdsToServerIdsMap())));

        // Validate the component had been added to the task in DB
        Task updatedTask = DB.find(task.getId(), Task.class);
        Assert.assertEquals(updatedTask.getComponents().size(), 1);
        Component addedComponent = updatedTask.getComponentById(componentToBeAdded.getId());
        Assert.assertNotNull(addedComponent);
        Assert.assertEquals(updatedTask.getId(), addedComponent.getTaskId());
        Assert.assertEquals(updatedTask.getId(), addedComponent.getParentId());

        // Validate the component and its children were added to the components collection in DB
        DB.find(componentToBeAdded.getId(), Component.class);
        Assert.assertEquals(componentToBeAdded.getComponents().size(), 1);
        DB.find(componentToBeAdded.getComponents().get(0).getId(), Component.class);
    }

    @Test(enabled = false)
    public void deleteTaskTest() throws Exception {
        Task task = createAndSaveTaskAndComponentsToDb();
        mockMvc.perform(delete(String.format("/tasks/%s", task.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        try {
            List<Component> componentsFromDb = DB.findByView(Design.COMPONENT, View.BY_TASK_ID, Component.class, task.getId());
            Assert.assertEquals(componentsFromDb.size(), 0);

            DB.find(task.getId(), Task.class); // should throw an exception because the task was supposed to be deleted from DB.
            Assert.fail(String.format("The DELETE task API failed to delete task: %s", task.getId()));
        } catch (EntityNotFoundException e) {
            Assert.assertEquals(String.format("No Task with id: %s was found in DB.", task.getId()), e.getMessage());
        }
    }

    @Test(enabled = false)
    public void addMcOptionComponentToTaskTest() throws Exception {
        Task task = createAndSaveTaskAndComponentsToDb();

        int mcOptionComponentPosition = 2;
        Component mcOptionComponent = createMcOptionComponent(task);
        AddComponentRequest addComponentRequest = new AddComponentRequest(mcOptionComponent, mcOptionComponentPosition);

        Map<String, String> cIdsToServerIdsMap = new HashMap<>();
        List<Component> flattenComponentsTree = mcOptionComponent.getFlattenComponentsTree();
        flattenComponentsTree.forEach(component -> cIdsToServerIdsMap.put(component.getCId(), component.getId()));

        String addComponentRequestJson = jsonWrapper.writeValueAsString(addComponentRequest);
        mockMvc.perform(post(String.format("/tasks/%s/components", task.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(addComponentRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(jsonWrapper.writeValueAsString(cIdsToServerIdsMap)));

        // validate that the task and all its components were saved in DB
        Task taskFromDb = DB.find(task.getId(), Task.class);
        Assert.assertNotNull(taskFromDb);
        int totalNumberOfComponentsInTask = task.getFlattenComponentsTree().size() + flattenComponentsTree.size();
        Assert.assertEquals(taskFromDb.getFlattenComponentsTree().size(), totalNumberOfComponentsInTask);
        List<Component> componentsFromDb = DB.findByView(Design.COMPONENT, View.BY_TASK_ID, Component.class, task.getId());
        Assert.assertEquals(componentsFromDb.size(), totalNumberOfComponentsInTask);
        Assert.assertEquals(cIdsToServerIdsMap.size(), flattenComponentsTree.size());

        // validate the component was added in the requested parent and in the required position in its parent's components list
        Component parentComponentFromTaskFromDb = taskFromDb.getComponentById(mcOptionComponent.getParentId());
        Assert.assertEquals(parentComponentFromTaskFromDb.getComponents().get(mcOptionComponentPosition).getId(), mcOptionComponent.getId());
    }

    private Component createMcOptionComponent(Task task) throws Exception {
        Component mcOptionComponent = createMockComponent("components/mcOptionComponent.json");
        mcOptionComponent.setTaskId(task.getId());
        mcOptionComponent.setParentId(task.getComponents().get(1).getId()); // gets the id of the MC mcOptionComponent
        return mcOptionComponent;
    }

    @Test(enabled = false)
    public void addMultipleComponentsToTaskTest() throws Exception {
        Task task = createAndSaveTaskAndComponentsToDb();
        List<AddComponentRequest> multipleComponentsRequest = new ArrayList<>();

        int mcOptionComponentPosition = 2;
        Component mcOptionComponent = createMcOptionComponent(task);
        multipleComponentsRequest.add(new AddComponentRequest(mcOptionComponent, mcOptionComponentPosition));

        int imageComponentPosition = 1;
        Component imageComponent = createImageComponent(task);
        multipleComponentsRequest.add(new AddComponentRequest(imageComponent, imageComponentPosition));

        Map<String, String> cIdsToServerIdsMap = new HashMap<>();
        List<Component> flattenMcOptionComponentsTree = mcOptionComponent.getFlattenComponentsTree();
        flattenMcOptionComponentsTree.forEach(component -> cIdsToServerIdsMap.put(component.getCId(), component.getId()));
        List<Component> flattenImageComponentsTree = imageComponent.getFlattenComponentsTree();
        flattenImageComponentsTree.forEach(component -> cIdsToServerIdsMap.put(component.getCId(), component.getId()));

        String addMultipleComponentsRequestJson = jsonWrapper.writeValueAsString(multipleComponentsRequest);
        MvcResult mvcResult = mockMvc.perform(post(String.format("/tasks/%s/components/multiple", task.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(addMultipleComponentsRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        // validate that the task and all its components were saved in DB
        Task taskFromDb = DB.find(task.getId(), Task.class);
        Assert.assertNotNull(taskFromDb);
        int totalNumberOfComponentsInTask = task.getFlattenComponentsTree().size() + flattenMcOptionComponentsTree.size() + flattenImageComponentsTree.size();
        Assert.assertEquals(taskFromDb.getFlattenComponentsTree().size(), totalNumberOfComponentsInTask);
        List<Component> componentsFromDb = DB.findByView(Design.COMPONENT, View.BY_TASK_ID, Component.class, task.getId());
        Assert.assertEquals(componentsFromDb.size(), totalNumberOfComponentsInTask);
        Assert.assertEquals(cIdsToServerIdsMap.size(), flattenMcOptionComponentsTree.size() + flattenImageComponentsTree.size());

        // validate the returned map contains all the components that were added
        HashMap result = jsonWrapper.readValue(mvcResult.getResponse().getContentAsString(), HashMap.class);
        result.forEach((cId, serverId) -> {
            Assert.assertTrue(cIdsToServerIdsMap.containsKey(cId));
            Assert.assertTrue(cIdsToServerIdsMap.get(cId).equals(serverId));
        });

        // validate the components were added in their requested parents and in the required positions in their parents' components lists
        // check the mcOption component
        Component parentComponentOfMcOptionFromTaskFromDb = taskFromDb.getComponentById(mcOptionComponent.getParentId());
        Assert.assertEquals(parentComponentOfMcOptionFromTaskFromDb.getComponents().get(mcOptionComponentPosition).getId(), mcOptionComponent.getId());
        // check the image component
        Assert.assertEquals(taskFromDb.getComponents().get(imageComponentPosition).getId(), imageComponent.getId());
    }

    @Test(enabled = false)
    public void updateComponentDataTest() throws Exception {
        Task task = createAndSaveTaskAndComponentsToDb();
        Component componentToBeUpdated = task.getComponents().get(1).getComponents().get(0); // MC Option component

        Map<String, String> dataObject = new HashMap<>();
        dataObject.put("mode", "single");
        dataObject.put("name", "changed property");
        componentToBeUpdated.setData(dataObject);

        String dataObjectJson = jsonWrapper.writeValueAsString(componentToBeUpdated);
        mockMvc.perform(put(String.format("/tasks/%s/components", task.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(dataObjectJson))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        // validate the component had been updated in both task and components collection in DB
        Task taskFromDb = DB.find(task.getId(), Task.class);
        Assert.assertNotNull(taskFromDb);
        Component updatedComponentFromTaskFromDb = taskFromDb.getComponentById(componentToBeUpdated.getId());
        Assert.assertEquals(updatedComponentFromTaskFromDb.getData(), dataObject);

        Component updatedComponentFromDb = DB.find(componentToBeUpdated.getId(), Component.class);
        Assert.assertEquals(updatedComponentFromDb.getData(), dataObject);
    }

    @Test(enabled = false)
    public void deleteComponentFromTaskTest() throws Exception {
        Task task = createAndSaveTaskAndComponentsToDb();
        Component componentToBeDeleted = task.getComponents().get(1).getComponents().get(0); // MC Option component

        mockMvc.perform(delete(String.format("/tasks/%s/components/%s", task.getId(), componentToBeDeleted.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));


        // validate the component was deleted from its task in DB
        Task taskFromDb = DB.find(task.getId(), Task.class);
        Assert.assertNotNull(taskFromDb);
        Component deletedComponent = taskFromDb.getComponentById(componentToBeDeleted.getId());
        Assert.assertNull(deletedComponent);

        // validate the number of components in task and in components collection have been reduced by one.
        List<Component> originalFlattenComponentsTree = task.getFlattenComponentsTree();
        Assert.assertEquals(taskFromDb.getFlattenComponentsTree().size(), originalFlattenComponentsTree.size() - 2); // -2 for the deleted MC Option and the text MC Option Menu component within it

        List<Component> componentsFromDb = DB.findByView(Design.COMPONENT, View.BY_TASK_ID, Component.class, task.getId());
        Assert.assertEquals(componentsFromDb.size(), originalFlattenComponentsTree.size() - 2); // -2 for the deleted MC Option and the text MC Option Menu component within it

        // validate that the deleted component's parent was updated in DB
        Component parentComponent = DB.find(componentToBeDeleted.getParentId(), Component.class);
        Assert.assertFalse(parentComponent.getComponentsIds().contains(componentToBeDeleted.getId()));
    }

    private Component createImageComponent(Task task) throws Exception {
        Component imageComponent = createMockComponent("components/imageComponent.json");
        imageComponent.setTaskId(task.getId());
        imageComponent.setParentId(task.getId()); // set the task to be the image component's parent
        return imageComponent;
    }

    /**
     * Creates a new task with components and data.
     *
     * @return a new task with components and data.
     * @throws IOException if failed to read the task's JSON file.
     */
    private Task createMockTask(String path) throws Exception {
        String taskJson = GeneralUtils.readResourcesAsString(this.getClass(), path);
        return jsonWrapper.readValue(taskJson, Task.class);
    }

    private Task createAndSaveTaskAndComponentsToDb() throws Exception {
        Task task = createMockTask("tasks/task.json");
        taskService.addTaskToDb(task);
        mockTasksAddedToDB.add(task);
        return task;
    }

    private Task createAndSaveEmptyTaskToDb() throws Exception {
        Task task = createMockTask("tasks/taskWithNoComponents.json");
        taskService.addTaskToDb(task);
        mockTasksAddedToDB.add(task);
        return task;
    }

    private Component createMockComponent(String path) throws Exception {
        String componentJson = GeneralUtils.readResourcesAsString(this.getClass(), path);
        return jsonWrapper.readValue(componentJson, Component.class);
    }
}