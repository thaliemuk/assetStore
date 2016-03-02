package org.t2k.interactions.dal;

import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.DesignDocument;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 30/07/2015
 * Time: 11:14
 */
public class CouchbaseBucketProperties {

    private com.couchbase.client.java.view.View bySha1View;
    private com.couchbase.client.java.view.View byUsernameView;
    private com.couchbase.client.java.view.View byTaskIdView;
    private com.couchbase.client.java.view.View allView;
    private com.couchbase.client.java.view.View byUserIdAndTaskId;
    private DesignDocument fileDataDesign;
    private DesignDocument taskDesign;
    private DesignDocument componentDesign;
    private DesignDocument stateDesign;

    public CouchbaseBucketProperties() {
        createBySha1View();
        createByUsernameView();
        createByTaskIdView();
        createAllTasksView();
        createByUserIdAndTaskId();
        createFileDataDesign();
        createTaskDesign();
        createComponentDesign();
        createStateDesign();
    }

    private void createBySha1View() {
        bySha1View = DefaultView.create(View.BY_SHA_1.toString(),
                "function (doc, meta) {\n" +
                        "  if (doc.type && doc.type == \"FileData\") {\n" +
                        "  emit(doc.sha1, doc);\n" +
                        "  }\n" +
                        "}");
    }

    private void createByUsernameView() {
        byUsernameView = DefaultView.create(View.BY_USERNAME.toString(),
                "function (doc, meta) {\n" +
                        "  emit(doc.username, doc);\n" +
                        "}");
    }

    private void createByTaskIdView() {
        byTaskIdView = DefaultView.create(View.BY_TASK_ID.toString(),
                "function (doc, meta) {\n" +
                        "  emit(doc.taskId, doc);\n" +
                        "}");
    }

    private void createAllTasksView() {
        allView = DefaultView.create(View.ALL_TASKS_NAMES.toString(),
                "function (doc, meta) {\n" +
                        "  if (doc.type && doc.type == \"Task\") {\n" +
                        "    emit(meta.id, {\"id\": doc.id, \"name\": doc.name});\n" +
                        "  }\n" +
                        "}");
    }

    private void createByUserIdAndTaskId() {
        byUserIdAndTaskId = DefaultView.create(View.BY_USER_ID_TASK_ID.toString(),
                "function (doc, meta) {\n" +
                        "  if (doc.type && doc.type == \"State\") {\n" +
                        "    emit([doc.userId, doc.taskId], doc);\n" +
                        "  }\n" +
                        "}");
    }

    private void createFileDataDesign() {
        fileDataDesign = DesignDocument.create(Design.FILE_DATA.toString(), Arrays.asList(bySha1View, byUsernameView));
    }

    private void createTaskDesign() {
        taskDesign = DesignDocument.create(Design.TASK.toString(), Arrays.asList(allView));
    }

    private void createComponentDesign() {
        componentDesign = DesignDocument.create(Design.COMPONENT.toString(), Arrays.asList(byTaskIdView));
    }

    private void createStateDesign() {
        stateDesign = DesignDocument.create(Design.STATE.toString(), Arrays.asList(byUserIdAndTaskId));
    }

    public DesignDocument getFileDataDesign() {
        return fileDataDesign;
    }

    public DesignDocument getTaskDesign() {
        return taskDesign;
    }

    public DesignDocument getComponentDesign() {
        return componentDesign;
    }

    public DesignDocument getStateDesign() {
        return stateDesign;
    }
}