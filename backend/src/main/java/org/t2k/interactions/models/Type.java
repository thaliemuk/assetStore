package org.t2k.interactions.models;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 03/09/2015
 * Time: 09:45
 */
public enum Type {
    TASK ("Task"),
    COMPONENT ("Component"),
    STATE ("State"),
    FILE_DATA ("FileData");

    private final String name;

    private Type(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}