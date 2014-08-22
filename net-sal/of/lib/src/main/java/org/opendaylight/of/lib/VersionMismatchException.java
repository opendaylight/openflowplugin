/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

/**
 * Runtime exception thrown when an OpenFlow version mismatch is detected.
 *
 * @author Simon Hunt
 */
public class VersionMismatchException extends RuntimeException {

    private static final long serialVersionUID = 6705564063054829863L;

    /** Constructs a new runtime exception with null as its detail message,
     * and no specific cause.
     */
    public VersionMismatchException() {
        super();
    }

    /** Constructs a new runtime exception with the specified detail message,
     * and the given cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public VersionMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Constructs a new runtime exception with the specified detail message,
     * and no specific cause.
     *
     * @param message the detail message
     */
    public VersionMismatchException(String message) {
        super(message);
    }

    /** Constructs a new runtime exception with null as its detail message,
     * but with the given cause.
     *
     * @param cause the underlying cause
     */
    public VersionMismatchException(Throwable cause) {
        super(cause);
    }
}
