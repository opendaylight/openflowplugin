/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

/**
 * Default implementation of a node link.
 *
 * @author Thomas Vachuska
 */
public class DefaultHostLink extends DefaultLink implements HostLink {

    private static final String E_NOT_HOST = "Not a host connection point";

    private final HostId hostId;
    private final HostLocation location;

    /**
     * Creates a new node link using the supplied node information.
     *
     * @param hostSide id of the node
     * @param location node location
     * @param isSrc    true of the host side is source; false if destination
     */
    public DefaultHostLink(ConnectionPoint hostSide, HostLocation location,
                           boolean isSrc) {
        super(isSrc ? hostSide : location, isSrc ? location : hostSide, Type.NODE);
        if (!(hostSide.elementId() instanceof HostId))
            throw new IllegalArgumentException(E_NOT_HOST);
        this.hostId = (HostId) hostSide.elementId();
        this.location = location;
    }

    /**
     * Creates a new node link using the supplied node information
     *
     * @param host node from which to create a node link
     * @param isSrc    true of the host side is source; false if destination
     */
    public DefaultHostLink(Host host, boolean isSrc) {
        this(new DefaultConnectionPoint(host.id(), host.netInterface().id()),
             host.location(), isSrc);
    }

    @Override
    public HostId nodeId() {
        return hostId;
    }

    @Override
    public ConnectionPoint connectionPoint() {
        return location;
    }

}
