package org.t2k.interactions.models;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 01/09/2015
 * Time: 13:38
 */
public class TaskNameResponse {

    private String id;
    private String name;

    private TaskNameResponse() { } // needed for json serialization. private because we don't want to expose it.

    public TaskNameResponse(String id, String name) {
        this.id = id;
        this.name = name;
    }
}