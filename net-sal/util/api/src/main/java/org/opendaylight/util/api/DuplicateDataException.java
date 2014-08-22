/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Exception representing a condition where an item with a duplicate data was
 * detected.
 * 
 * @author Feroz Ahmed
 */
public class DuplicateDataException extends RuntimeException {

    private static final long serialVersionUID = -990164411340073190L;

    /**
     * Constructs an exception with no message and no underlying cause.
     */
    public DuplicateDataException() {
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message the message describing the specific nature of the error
     */
    public DuplicateDataException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message the message describing the specific nature of the error
     * @param cause the underlying cause of this error
     */
    public DuplicateDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
