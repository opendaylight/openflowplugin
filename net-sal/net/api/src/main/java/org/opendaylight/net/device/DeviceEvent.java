/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.device;

import org.opendaylight.net.model.ModelEvent;
import org.opendaylight.net.model.Device;
import org.opendaylight.net.model.Interface;

/**
 * Represents an event in the infrastructure device information model.
 *
 * @author Thomas Vachuska
 * @author Sean Humphress
 * @author Julie Britt
 * @author Steve Dean
 * @author Anuradha Musunuri
 */
public interface DeviceEvent extends ModelEvent<Device, DeviceEvent.Type> {

    /** Device event types. */
    enum Type {
        /** Event represents device addition. */
        DEVICE_ADDED,

        /** Event represents device removal. */
        DEVICE_REMOVED,

        /** Event represents device update. */
        DEVICE_UPDATED,

        /** Event represents change in device availability. */
        DEVICE_AVAILABILITY_CHANGED,

        /** Event represents change in device interface/port state. */
        INTERFACE_STATE_CHANGED,
        
        /** Event represents device interface/port added. */
        INTERFACE_ADDED,

        /** Event represents device interface/port updated. */
        INTERFACE_UPDATED,

        /** Event represents device interface/port removed. */
        INTERFACE_REMOVED
    }

    /**
     * Returns the interface related to the event, if applicable.
     *
     * @return interface related to the event
     */
    Interface netInterface();

}
