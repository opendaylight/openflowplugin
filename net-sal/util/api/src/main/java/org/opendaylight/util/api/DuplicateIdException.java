/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Exception representing a condition where an item with a duplicate id was
 * detected.
 * 
 * @author Thomas Vachuska
 */
public class DuplicateIdException extends RuntimeException {

    private static final long serialVersionUID = 1829861898146892091L;

    /**
     * Constructs an exception with no message and no underlying cause.
     */
    public DuplicateIdException() {
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message the message describing the specific nature of the error
     */
    public DuplicateIdException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message the message describing the specific nature of the error
     * @param cause the underlying cause of this error
     */
    public DuplicateIdException(String message, Throwable cause) {
        super(message, cause);
    }

}
