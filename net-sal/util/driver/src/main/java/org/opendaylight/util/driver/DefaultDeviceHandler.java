/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import org.opendaylight.util.net.IpAddress;

import java.util.Set;

/**
 * A partial implementation of {@link DeviceHandler} that has default
 * implementations of a couple of {@link FacetProvider} methods.
 * 
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public class DefaultDeviceHandler implements DeviceHandler {

    private final AbstractDeviceInfo deviceInfo;

    private final IpAddress ip;

    /**
     * Create a device handler for the specified device info and IP address.
     * 
     * @param deviceInfo device info context
     * @param ip IP address of the target device
     */
    public DefaultDeviceHandler(AbstractDeviceInfo deviceInfo, IpAddress ip) {
        if (deviceInfo == null)
            throw new NullPointerException("DeviceInfo context must be specified");
        if (ip == null)
            throw new NullPointerException("Device IP address must be specified");
        this.deviceInfo = deviceInfo;
        this.ip = ip;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public IpAddress getIpAddress() {
        return ip;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("[").append(getClass().getSimpleName())
                .append(": ").append("deviceInfo=").append(deviceInfo)
                .append(", IP=").append(ip).append("]")
                .toString();
    }

    // FacetProvider methods
    
    @Override
    public Set<Class<? extends Facet>> getFacetClasses() {
        return deviceInfo.getType().getFacetClasses(true);
    }

    @Override
    public Set<String> getFacetClassNames() {
        return deviceInfo.getType().getFacetClassNames(true);
    }

    @Override
    public boolean isSupported(Class<? extends Facet> facetClass) {
        return deviceInfo.getType().isSupported(true, facetClass);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public <T extends Facet> T getFacet(Class<T> facetClass) {
        HandlerFacet hf = (HandlerFacet) 
                deviceInfo.getType().getFacet(true, deviceInfo, facetClass);
        hf.setIpAddress(ip);
        return (T) hf;
    }

}
