package org.t2k.interactions.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.SerializationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 04/08/2015
 * Time: 12:18
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Component extends BaseModel {

    private String taskId;
    private String parentId;
    private String componentType;
    private Object data;
    private List<Component> components;
    private List<String> componentsIds;

    public Component() {
        super();
        this.type = Type.COMPONENT.toString();
        this.components = new ArrayList<>();
        this.componentsIds = new ArrayList<>();
    }

    public List<Component> getComponents() {
        return components;
    }

    public List<String> getComponentsIds() {
        return this.componentsIds;
    }

    public Component getChildById(String id) {
        if (this.id.equals(id)) {
            return this;
        }

        for (Component component : this.components) {
            Component child = component.getChildById(id);
            if (child != null) {
                return child;
            }
        }

        return null;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getComponentType() {
        return componentType;
    }

    public Object getData() {
        return data;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Component copyWithoutChildren() {
        Component copy = new Component();
        copy.id = this.id;
        copy.type = this.type;
        copy.taskId = this.taskId;
        copy.parentId = this.parentId;
        copy.componentType = this.componentType;
        copy.data = this.data;
        copy.creationDate = this.creationDate;
        copy.lastModifiedDate = this.lastModifiedDate;
        copy.componentsIds = this.componentsIds;
        copy.components = null;

        return copy;
    }

    public List<Component> getFlattenComponentsTree() {
        List<Component> flattenComponentsList = new ArrayList<>();
        flattenComponentsList.add(this);
        this.components.forEach(component -> flattenComponentsList.addAll(component.getFlattenComponentsTree()));
        return flattenComponentsList;
    }

    public void addChildComponentIdToComponentsIdsList(Component component, int position) {
        if (position >= this.componentsIds.size()) {
            String errorMsg = String.format("The given position: %d is out of bounds.", position);
            throw new IndexOutOfBoundsException(errorMsg);
        }

        component.setParentId(this.getId());
        this.componentsIds.add(position, component.getId());
    }

    public Map<String, String> getCIdsToServerIdsMap() {
        Map<String, String> tempIdsToServerIdsMap = new HashMap<>();
        tempIdsToServerIdsMap.put(this.getCId(), this.getId());
        this.components.forEach(component ->
                        tempIdsToServerIdsMap.putAll(component.getCIdsToServerIdsMap())
        );

        return tempIdsToServerIdsMap;
    }

    public String getSha1OfData() {
        return DigestUtils.sha1Hex(SerializationUtils.serialize(this.data));
    }

    @Override
    public String toString() {
        return "Component{ " +
                super.toString() +
                ", taskId='" + taskId + '\'' +
                ", parentId='" + parentId + '\'' +
                ", componentType='" + componentType + '\'' +
                ", data=" + data +
                ", componentsIds=" + componentsIds +
                " }";
    }
}