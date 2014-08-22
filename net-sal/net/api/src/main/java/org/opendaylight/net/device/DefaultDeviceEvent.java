/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.device;

import org.opendaylight.net.model.AbstractModelEvent;
import org.opendaylight.net.model.Device;
import org.opendaylight.net.model.Interface;

/**
 * Default implementation of {@link DeviceEvent}
 *
 * @author Steve Dean
 */
public class DefaultDeviceEvent extends AbstractModelEvent<Device, DeviceEvent.Type>
        implements DeviceEvent {

    private final Interface netInterface;
    
    /**
     * Constructs a device event with the given type, device and optionally
     * network interface.
     *
     * @param type the type of event
     * @param subject the device associated with the event
     * @param netInterface optional network interface associated with the event
     */
    public DefaultDeviceEvent(Type type, Device subject, Interface netInterface) {
        super(type, subject);
        this.netInterface = netInterface;
    }

    @Override
    public Interface netInterface() {
        return netInterface;
    }

}
