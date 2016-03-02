package org.t2k.interactions.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.t2k.interactions.utils.GeneralUtils;
import org.t2k.interactions.utils.jsonUtils.JsonWrapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 23/08/2015
 * Time: 09:41
 */
@ContextConfiguration("classpath:/springContext/applicationContext-rest.xml")
public class TaskTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private JsonWrapper jsonWrapper;

    @Test(enabled = false)
    public void mapJsonToTask() throws IOException {
        String taskJson = GeneralUtils.readResourcesAsString(this.getClass(), "tasks/taskWithDate.json");
        Task task = jsonWrapper.readValue(taskJson, Task.class);
        Assert.assertNotNull(task);
    }
}