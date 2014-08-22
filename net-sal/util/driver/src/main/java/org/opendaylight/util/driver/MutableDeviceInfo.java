/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * A view of {@link DeviceInfo} that facilitates the changing of its internal state.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public interface MutableDeviceInfo extends DeviceInfo {

    /** Returns the device type that this device info instance is backed by.
     *
     * @return the device type
     */
    public DeviceType getDeviceType();

    /** Sets the device type for this device info instance.
     *
     * @param type the device type
     */
    public void setDeviceType(DeviceType type);
}
