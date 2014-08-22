/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.api.auth;

/**
 * Exception representing an authentication failure.
 * 
 * @author Liem Nguyen
 * @author Steve Britt
 */
public class AuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 2287073516214658461L;

    private String token;

    /**
     * Constructs an exception with no message and no underlying cause.
     */
    public AuthenticationException() {
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message the message describing the specific nature of the error
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified message, token, and
     * underlying cause.
     * 
     * @param message the message describing the specific nature of the error
     * @param token the token used when the error was encountered
     */
    public AuthenticationException(String message, String token) {
        super(message);
        this.token = token;
    }

    /**
     * Constructs an exception with the specified message and underlying
     * cause.
     * 
     * @param message the message describing the specific nature of the error
     * @param cause underlying cause exception
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an exception with the specified message, token, and
     * underlying cause.
     * 
     * @param message the message describing the specific nature of the error
     * @param token the token used when the error was encountered
     * @param cause underlying cause exception
     */
    public AuthenticationException(String message, String token, Throwable cause) {
        super(message, cause);
        this.token = token;
    }

    /**
     * Get the token.
     * 
     * @return token
     */
    public String getToken() {
        return token;
    }
}