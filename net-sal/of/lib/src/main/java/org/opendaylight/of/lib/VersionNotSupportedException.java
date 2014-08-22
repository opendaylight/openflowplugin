/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

/**
 * Runtime exception thrown if the requested protocol version is not
 * supported by the message library.
 *
 * @author Simon Hunt
 */
public class VersionNotSupportedException extends RuntimeException {

    private static final long serialVersionUID = 7829475376635836228L;

    /** Constructs a new runtime exception with null as its detail message,
     * and no specific cause.
     */
    public VersionNotSupportedException() {
        super();
    }

    /** Constructs a new runtime exception with the specified detail message,
     * and the given cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public VersionNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Constructs a new runtime exception with the specified detail message,
     * and no specific cause.
     *
     * @param message the detail message
     */
    public VersionNotSupportedException(String message) {
        super(message);
    }

    /** Constructs a new runtime exception with null as its detail message,
     * but with the given cause.
     *
     * @param cause the underlying cause
     */
    public VersionNotSupportedException(Throwable cause) {
        super(cause);
    }
}
