/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import org.opendaylight.util.net.IpAddress;

/**
 * A default implementation of the device identity handler facet. This
 * implementation makes the assumption that the context is a
 * {@link DefaultDeviceInfo} implementation.
 * 
 * @author Simon Hunt
 */
public class DefaultDeviceIdentityHandler extends DefaultDeviceIdentity 
                            implements DeviceIdentityHandler {
    
    /**
     * Constructs a device identity configure facet that is projected onto the
     * specified device info context.
     * 
     * @param context the device info of which this is a facet
     */
    public DefaultDeviceIdentityHandler(DeviceInfo context) {
        super(context);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation injects the IP address into the backing device info context, in addition to
     * setting the field via super.
     */
    @Override
    public void setIpAddress(IpAddress ip) {
        getContext().set(CoreDevicePropertyKeys.IP_ADDRESS, ip.toString());
        super.setIpAddress(ip);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing.
     */
    @Override
    public void fetch() { }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing.
     */
    @Override
    public void apply() {
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing.
     */
    @Override
    public void digest(Object memento) {
    }

    @Override
    public void generateUniqueId() {
        // TODO: For now use IP, but later use mash-up of other stuff.
        String uid = "UID-" + getIpAddress().toString();
        getContext().set(CoreDevicePropertyKeys.UNIQUE_ID, uid);
    }

    @Override
    public void setName(String name) {
        getContext().set(CoreDevicePropertyKeys.NAME, name);
    }

    @Override
    public void setLocation(String location) {
        getContext().set(CoreDevicePropertyKeys.LOCATION, location);
    }

    @Override
    public void setContact(String contact) {
        getContext().set(CoreDevicePropertyKeys.CONTACT, contact);
    }

    @Override
    public DeviceInfo evolve() {
        return getContext();
    }

}
