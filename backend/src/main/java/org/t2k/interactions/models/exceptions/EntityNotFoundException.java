package org.t2k.interactions.models.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 05/08/2015
 * Time: 16:03
 */
public class EntityNotFoundException extends InteractionsException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}