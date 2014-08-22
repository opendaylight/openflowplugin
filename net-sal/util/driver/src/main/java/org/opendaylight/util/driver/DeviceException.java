/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * Run-time exception representing issues encountered by the device driver
 * facilities.
 * 
 * @author Thomas Vachuska
 * @author John Green
 * @author Simon Hunt
 */
public class DeviceException extends RuntimeException {
    
    // TODO: Determine whether this ought to be made checked exception instead.
    
    private static final long serialVersionUID = -4827153796236948486L;

    /**
     * Creates a new device exception.
     */
    public DeviceException() { 
    }
    
    /**
     * Creates a new device exception using the supplied message.
     * 
     * @param message error message
     */
    public DeviceException(String message) {
        super(message);
    }

    /**
     * Creates a new device exception using the supplied message and
     * underlying cause.
     * 
     * @param message error message
     * @param cause underlying exception cause
     */
    public DeviceException(String message, Throwable cause) {
        super(message, cause);
    }

}
