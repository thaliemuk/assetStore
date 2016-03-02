package org.t2k.interactions.dal;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 01/09/2015
 * Time: 16:26
 */
public enum View {

    BY_SHA_1 ("by_sha1"),
    BY_USERNAME ("by_username"),
    BY_TASK_ID ("by_taskId"),
    ALL_TASKS_NAMES ("all_tasks_names"),
    BY_USER_ID_TASK_ID ("by_user_id_task_id");

    private final String name;

    private View(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}