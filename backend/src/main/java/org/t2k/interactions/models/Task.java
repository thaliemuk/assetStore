package org.t2k.interactions.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 30/06/2015
 * Time: 19:59
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Task extends BaseModel {

    private String name;
    private List<Component> components;

    public Task() {
        super();
        this.type = Type.TASK.toString();
        this.components = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public List<Component> getComponents() {
        return components;
    }

    public Component getComponentById(String componentId) {
        for(Component component : this.components){
            Component child = component.getChildById(componentId);
            if (child != null) {
                return child;
            }
        }

        return null;
    }

    public List<Component> getFlattenComponentsTree() {
        List<Component> flattenComponentsList = new ArrayList<>();
        this.getComponents().forEach(component -> {
            flattenComponentsList.addAll(component.getFlattenComponentsTree());
        });

        return flattenComponentsList;
    }

    public void addChildComponent(Component component, int position) {
        if (position > this.components.size()) {
            String errorMsg = String.format("Cannot add component %s into task %s. The given position: %d is out of bounds.", component.getId(), this.id, position);
            throw new IndexOutOfBoundsException(errorMsg);
        }

        component.setParentId(this.getId());
        this.components.add(position, component);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                super.toString() +
                "}";
    }
}