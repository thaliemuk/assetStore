package org.t2k.interactions.models.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 23/07/2015
 * Time: 10:57
 */
public class EmptyFileException extends Exception  {

    public EmptyFileException(String message) {
        super(message);
    }

    public EmptyFileException(Throwable cause) {
        super(cause);
    }

    public EmptyFileException(String message, Throwable cause) {
        super(message, cause);
    }
}