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
 * A partial implementation of {@link Facet} that maintains a device info context.
 * Remember, a Facet is the projection of a particular "view" onto a {@link DeviceInfo} instance.
 *
 * @author Simon Hunt
 */
public abstract class AbstractFacet implements Facet {

    private final DeviceInfo context;
    private IpAddress ip;

    /** Constructs a facet that is projected onto the specified device info context.
     *
     * @param context the device info of which this is a facet
     */
    public AbstractFacet(DeviceInfo context) {
        this.context = context;
    }

    @Override
    public DeviceInfo getContext() {
        return context;
    }

    @Override
    public String getTypeName() {
        // delegate to the context
        return context.getTypeName();
    }

    // TODO - elaborate on this field being here for HandlerFacet implementations' use.

    /** Get the IP address associated with this facet, if any.
     *
     * @return the IP address
     */
    public IpAddress getIpAddress() {
        return ip;
    }

    /** Set the IP address associated with this facet, if needed.
     *
     * @param ip the IP address
     */
    public void setIpAddress(IpAddress ip) {
        this.ip = ip;
    }
}
