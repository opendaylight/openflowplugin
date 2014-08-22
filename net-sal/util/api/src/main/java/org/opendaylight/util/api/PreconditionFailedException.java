/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.api;

/**
 * Exception representing a precondition not being met.
 * 
 * @author Ankit Kumar
 */
public class PreconditionFailedException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 4190302961462439081L;

    /**
     * Constructs an exception with no message and no underlying cause.
     */
    public PreconditionFailedException() {
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message the message describing the specific nature of the error
     */
    public PreconditionFailedException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified message and underlying
     * cause.
     * 
     * @param message the message describing the specific nature of the error
     * @param cause underlying cause exception
     */
    public PreconditionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

