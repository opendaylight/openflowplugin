/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.msg.Capability;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.PortNumber;

import java.util.List;
import java.util.Set;

/**
 * An adapter for the {@link DataPathInfo} API, provided specifically for
 * unit tests to use, to insulate themselves from changes in the API.
 *
 * @author Simon Hunt
 */
public class DataPathInfoAdapter implements DataPathInfo {
    @Override public DataPathId dpid() { return null; }
    @Override public ProtocolVersion negotiated() { return null; }
    @Override public long readyAt() { return 0; }
    @Override public long lastMessageAt() { return 0; }
    @Override public List<Port> ports() { return null; }
    @Override public long numBuffers() { return 0; }
    @Override public int numTables() { return 0; }
    @Override public Set<Capability> capabilities() { return null; }
    @Override public IpAddress remoteAddress() { return null; }
    @Override public PortNumber remotePort() { return null; }
    @Override public String datapathDescription() { return null; } 
    @Override public String manufacturerDescription() { return null; } 
    @Override public String hardwareDescription() { return null; }
    @Override public String softwareDescription() { return null; }
    @Override public String serialNumber() { return null; }
    @Override public String deviceTypeName() { return null;
    }
}
