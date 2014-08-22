/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Exception representing a condition where an expected item was not found.
 *
 * @author Thomas Vachuska
 */
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 5966373708329157779L;

    /**
     * Constructs an exception with no message and no underlying cause.
     */
    public NotFoundException() {
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message the message describing the specific nature of the error
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message the message describing the specific nature of the error
     * @param cause the underlying cause of this error
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
