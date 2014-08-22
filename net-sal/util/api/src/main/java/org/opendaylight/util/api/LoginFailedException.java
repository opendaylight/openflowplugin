/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Exception representing failure to login.
 * 
 * @author Thomas Vachuska
 */
public class LoginFailedException extends RuntimeException {

    private static final long serialVersionUID = 1635718824609599777L;

    /**
     * Constructs an exception with no message and no underlying cause.
     */
    public LoginFailedException() {
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message the message describing the specific nature of the error
     */
    public LoginFailedException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message the message describing the specific nature of the error
     * @param cause the underlying cause of this error
     */
    public LoginFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
