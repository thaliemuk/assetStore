package org.t2k.interactions.dal;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 01/09/2015
 * Time: 16:33
 */
public enum Design {

    FILE_DATA ("fileData"),
    TASK ("task"),
    COMPONENT ("component"),
    STATE ("state");

    private final String name;

    private Design(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}