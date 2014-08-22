/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

/**
 * Exception representing a condition where an expected service is not found.
 * 
 * @author Scott Simes
 * @author Thomas Vachuska
 */
public class ServiceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 4070115954587235639L;

    /**
     * Constructs an exception with no message and no underlying cause.
     */
    public ServiceNotFoundException() {
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message the message describing the specific nature of the error
     */
    public ServiceNotFoundException(String message) {
        super(message);
    }

}
