/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

/**
 * Exception thrown when an attempt to decode an encoded value fails.
 *
 * @author Simon Hunt
 */
public class DecodeException extends OpenflowException {

    private static final long serialVersionUID = -6199618089952873862L;

    /** Constructs a new decode exception with null as its detail
     *  message, and no specific cause.
     */
    public DecodeException() {
        super();
    }

    /** Constructs a new decode exception with the specified detail
     *  message, and the given cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Constructs a new decode exception with the specified detail
     *  message, and no specific cause.
     *
     * @param message the detail message
     */
    public DecodeException(String message) {
        super(message);
    }

    /** Constructs a new decode exception with null as its detail
     *  message, but with the given cause.
     *
     * @param cause the underlying cause
     */
    public DecodeException(Throwable cause) {
        super(cause);
    }
}
