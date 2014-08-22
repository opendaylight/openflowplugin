/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.net.model.*;

import java.util.Iterator;
import java.util.Set;

/**
 * Adapter implementation of the {@link HostService}.
 *
 * @author Shaun Wackerly
 */
public class HostServiceAdapter implements HostService {
    @Override public Iterator<Host> getHosts() { return null; }
    @Override public Iterator<Host> getHosts(SegmentId netId) { return null; }
    @Override public Set<Host> getHosts(IpAddress ip) { return null; }
	@Override public Set<Host> getHosts(MacAddress mac, SegmentId segId) { return null; }
    @Override public Set<Host> getHosts(ConnectionPoint cp) { return null; }
    @Override public Set<Host> getHosts(DeviceId device) { return null; }
    @Override public Iterator<Host> getHosts(HostFilter filter) { return null; }
    @Override public Host getHost(HostId hostId) { return null; }
    @Override public void addListener(HostListener listener) { }
    @Override public void removeListener(HostListener listener) { }
    @Override public Set<HostListener> getListeners() { return null; }

}
