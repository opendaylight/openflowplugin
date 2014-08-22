/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * A partial implementation of {@link DeviceLoader}.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public abstract class AbstractDeviceLoader implements DeviceLoader {

    private final AbstractDeviceInfo deviceInfo;

    private final String uid;

    /**
     * Create a device loader for the specified device info and Unique ID.
     *
     * @param deviceInfo device info context
     * @param uid the unique id of the device
     */
    protected AbstractDeviceLoader(AbstractDeviceInfo deviceInfo, String uid) {
        if (deviceInfo == null)
            throw new NullPointerException("DeviceInfo context must be specified");
        if (uid == null)
            throw new NullPointerException("Device unique ID must be specified");
        this.deviceInfo = deviceInfo;
        this.uid = uid;
    }

    @Override
    public AbstractDeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public String getUID() {
        return uid;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("[").append(getClass().getSimpleName())
                .append(": ").append("deviceInfo=").append(deviceInfo)
                .append(", UID=").append(uid).append("]")
                .toString();
    }

}
