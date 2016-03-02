package org.t2k.interactions.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.t2k.interactions.utils.GeneralUtils;
import org.t2k.interactions.utils.jsonUtils.JsonDateDeserializer;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 14/07/2015
 * Time: 17:57
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class BaseModel {

    protected String id;
    protected String cId;
    protected String type;
    @JsonDeserialize(using = JsonDateDeserializer.class)
    protected Date creationDate;
    @JsonDeserialize(using = JsonDateDeserializer.class)
    protected Date lastModifiedDate;

    public BaseModel() {
        this.id = GeneralUtils.generateUUID();
        this.creationDate = new Date();
    }

    public String getId() {
        return id;
    }

    public String getCId() {
        return cId;
    }

    public String getType() {
        return type;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void updateLastModifiedDate() {
        this.lastModifiedDate = new Date();
    }

    @Override
    public String toString() {
        return "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", creationDate='" + creationDate + '\'';
    }
}