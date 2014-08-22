/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

/**
 * Generic OpenFlow exception. Thrown when a more specific exception
 * is not available.
 *
 * @author Simon Hunt
 */
public class OpenflowException extends Exception {

    private static final long serialVersionUID = 8743829679445676570L;

    /** Constructs a new OpenFlow exception with null as its detail
     * message, and no specific cause.
     */
    public OpenflowException() {
        super();
    }

    /** Constructs a new OpenFlow exception with the specified detail
     * message, and the given cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public OpenflowException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Constructs a new OpenFlow exception with the specified detail
     * message, and no specific cause.
     *
     * @param message the detail message
     */
    public OpenflowException(String message) {
        super(message);
    }

    /** Constructs a new OpenFlow exception with null as its detail
     * message, but with the given cause.
     *
     * @param cause the underlying cause
     */
    public OpenflowException(Throwable cause) {
        super(cause);
    }
}
