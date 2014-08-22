/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.device;

/**
 * Entity capable of receiving asynchronous notifications about changes in
 * infrastructure device information model.
 *
 * @author Thomas Vachuska
 * @author Shaun Humphress
 * @author Julie Britt
 * @author Steve Dean
 */
public interface DeviceListener {
    
    /**
     * Receives a notification about a change in device information model.
     *
     * @param event the device event
     */
    void event(DeviceEvent event);

}
