/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

/**
 * Runtime exception thrown when an operation is attempted on a
 * {@link org.opendaylight.of.lib.msg.MutableMessage} that has already
 * had {@link org.opendaylight.of.lib.msg.MutableMessage#toImmutable} invoked on it.
 *
 * @author Simon Hunt
 */
public class InvalidMutableException extends RuntimeException {

    private static final long serialVersionUID = -5067912592150458236L;

    /** Constructs a new runtime exception with null as its detail message,
     * and no specific cause.
     */
    public InvalidMutableException() {
        super();
    }

    /** Constructs a new runtime exception with the specified detail message,
     * and the given cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public InvalidMutableException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Constructs a new runtime exception with the specified detail message,
     * and no specific cause.
     *
     * @param message the detail message
     */
    public InvalidMutableException(String message) {
        super(message);
    }

    /** Constructs a new runtime exception with null as its detail message,
     * but with the given cause.
     *
     * @param cause the underlying cause
     */
    public InvalidMutableException(Throwable cause) {
        super(cause);
    }
}
