/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Exception representing a condition where an expected service is not
 * available.
 * 
 * @author Vamsi Krishna Devaki
 */
public class ServiceNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 2540283706716001170L;

    /**
     * Constructs an <code>ServiceNotAvailableException</code> with no detail
     * message.
     */
    public ServiceNotAvailableException() {
        super();
    }

    /**
     * Constructs an <code>ServiceNotAvailableException</code> with the
     * specified detail message.
     * 
     * @param s the detail message.
     */
    public ServiceNotAvailableException(String s) {
        super(s);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this exception's detail
     * message.
     * 
     * @param message the detail message (which is saved for later retrieval
     *        by the {@link Throwable#getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link Throwable#getCause()} method). (A <tt>null</tt> value is
     *        permitted, and indicates that the cause is nonexistent or
     *        unknown.)
     */
    public ServiceNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example,
     * {@link java.security.PrivilegedActionException}).
     * 
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link Throwable#getCause()} method). (A <tt>null</tt> value is
     *        permitted, and indicates that the cause is nonexistent or
     *        unknown.)
     */
    public ServiceNotAvailableException(Throwable cause) {
        super(cause);
    }

}
