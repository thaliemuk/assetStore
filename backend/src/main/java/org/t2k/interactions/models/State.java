package org.t2k.interactions.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 31/08/2015
 * Time: 16:30
 */
public class State extends BaseModel {

    private String userId;
    private String taskId;
    private String taskVersion;
    private Object data;
    private List<Object> components;

    public State() {
        super();
        this.type = Type.STATE.toString();
        this.components = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskVersion() {
        return taskVersion;
    }

    public Object getData() {
        return data;
    }

    public List<Object> getComponents() {
        return components;
    }
}