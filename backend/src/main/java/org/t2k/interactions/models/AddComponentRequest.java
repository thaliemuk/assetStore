package org.t2k.interactions.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 16/08/2015
 * Time: 14:36
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AddComponentRequest {

    private int position;
    private Component component;

    public AddComponentRequest(Component component, int position) {
        this.component = component;
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public Component getComponent() {
        return component;
    }
}