package org.t2k.interactions.models;

import java.io.File;
import java.util.Date;

import static org.t2k.interactions.utils.GeneralUtils.generateUUID;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 27/07/2015
 * Time: 08:04
 */
public class FileData {

    private String id;
    private String type;
    private String username;
    private String originalName;
    private String path;
    private String url;
    private String sha1;
    private long size;
    private String mediaType;
    private Date creationDate;

    private FileData() { } // needed for json serialization. private because we don't want to expose it.

    public FileData(String username, String originalName, String path, String urlPrefix, String sha1, long size, String mediaType) {
        this.id = generateUUID();
        this.type = Type.FILE_DATA.toString();
        this.username = username;
        this.originalName = originalName;
        this.path = path;
        this.url = String.format("%s%s", urlPrefix, sha1);
        this.sha1 = sha1;
        this.size = size;
        this.mediaType = mediaType;
        this.creationDate = new Date();
    }

    public FileData(String username, File file, String urlPrefix, String originalFileName, String mediaType) {
        this(username, originalFileName, file.getAbsolutePath(), urlPrefix, file.getName(), file.length(), mediaType);
    }

    public String getUsername() {
        return username;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }

    public String getSha1() {
        return sha1;
    }

    @Override
    public String toString() {
        return "FileData{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", username='" + username + '\'' +
                ", originalName='" + originalName + '\'' +
                ", path='" + path + '\'' +
                ", url='" + url + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", size=" + size +
                ", mediaType='" + mediaType + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}