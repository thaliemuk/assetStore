package org.t2k.interactions.models.exceptions;

import static org.t2k.interactions.utils.GeneralUtils.generateUUID;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 05/08/2015
 * Time: 16:06
 */
public class InteractionsException extends Exception {

    private String errorId;

    public InteractionsException(String message) {
        super(message);
        this.errorId = generateUUID();
    }

    public InteractionsException(Throwable cause) {
        super(cause);
        this.errorId = generateUUID();
    }

    public InteractionsException(String message, Throwable cause) {
        super(message, cause);
        this.errorId = generateUUID();
    }
}