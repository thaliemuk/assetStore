package org.t2k.interactions.models.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 14/07/2015
 * Time: 18:12
 */
public class DbException extends InteractionsException {

    public DbException(String message) {
        super(message);
    }

    public DbException(Throwable cause) {
        super(cause);
    }

    public DbException(String message, Throwable cause) {
        super(message, cause);
    }
}