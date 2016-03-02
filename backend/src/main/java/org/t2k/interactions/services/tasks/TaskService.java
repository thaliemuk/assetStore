package org.t2k.interactions.services.tasks;

import org.t2k.interactions.models.Component;
import org.t2k.interactions.models.AddComponentRequest;
import org.t2k.interactions.models.Task;
import org.t2k.interactions.models.exceptions.DbException;
import org.t2k.interactions.models.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 05/08/2015
 * Time: 18:43
 */
public interface TaskService {

    Map<String, String> addTaskToDb(Task task) throws DbException;

    void deleteTaskFromDb(String id) throws DbException;

    Map<String, String> addComponent(AddComponentRequest componentRequest) throws DbException, EntityNotFoundException;

    Map<String,String> addMultipleComponents(List<AddComponentRequest> componentsRequest, String taskId) throws Exception;

    void updateComponentData(Component component) throws DbException, EntityNotFoundException;

    void deleteComponent(String componentId, String taskId) throws DbException, EntityNotFoundException;

    Map<String, String> updateTask(Task task) throws DbException, EntityNotFoundException;
}