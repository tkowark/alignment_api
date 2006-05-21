/*
 * QueryTypeMismatchException.java
 *
 * Created on March 20, 2006, 11:03 AM
 * 
 */

package fr.inrialpes.exmo.QueryProcessor;

/**
 *
 * @author Arun Sharma
 */
public class QueryTypeMismatchException extends Exception {
    /**
     * Create a new <code>QueryTypeMismatchException</code> with no
     * detail mesage.
     */
    public QueryTypeMismatchException() {
    	super();
    }

    /**
     * Create a new <code>QueryTypeMismatchException</code> with
     * the <code>String</code> specified as an error message.
     *
     * @param message The error message for the exception.
     */
    public QueryTypeMismatchException(String message) {
    	super(message);
    }

    /**
     * Create a new <code>QueryTypeMismatchException</code> with
     * the <code>String</code> specified as an error message and the
     * <code>Throwable</code> that caused this exception to be raised.
     * @param message The error message for the exception
     * @param cause The cause of the exception
     */
    public QueryTypeMismatchException(String message, Throwable cause) {
    	super(message, cause);
    }
}
