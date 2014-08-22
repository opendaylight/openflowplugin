/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.net.MacAddress;

/**
 * Default implementation of {@link HostInfo}. Any data which is not explicitly
 * specified (even if null) will be considered unspecified.
 *
 * @author Shaun Wackerly
 */
public class DefaultHostInfo implements HostInfo {

    private final Interface netInterface;
    private final MacAddress mac;
    private final HostLocation location;

    /**
     * Constructs a default node info object with all fields unspecified.
     */
    public DefaultHostInfo(Interface netInterface, MacAddress mac, HostLocation location) {
        this.netInterface = netInterface;
        this.mac = mac;
        this.location = location;
    }

    @Override
    public Interface netInterface() {
        return netInterface;
    }

    @Override
    public MacAddress mac() {
        return mac;
    }

    @Override
    public HostLocation location() {
        return location;
    }

}
