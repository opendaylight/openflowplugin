/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.msg.Capability;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.json.JSON;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.PortNumber;

import java.util.*;

import static java.util.Collections.emptySet;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.Capability.*;
import static org.opendaylight.util.net.IpAddress.ip;

/**
 * {@link DataPathInfo} fixture to support the unit testing of JSON codecs.
 *
 * @author Liem Nguyen
 * @author Simon Hunt
 */
// FIXME: Extend DataPathInfoAdapter, and fix codec factory lookup algorithm
public class MockDataPathInfo implements DataPathInfo {

    private static final Set<Capability> CAP_SET_13 = 
            EnumSet.of(FLOW_STATS, TABLE_STATS, PORT_STATS, GROUP_STATS, 
                    IP_REASM, QUEUE_STATS, PORT_BLOCKED);
    private static final Set<Capability> EMPTY_CAPS_SET = emptySet();
    
    private static final String DPID_STR = "45067/000036:006502"; 
    private static final DataPathId DPID = DataPathId.valueOf(DPID_STR);

    private static final int NBUFF = 256;
    private static final int NTAB = 20;

    private static final String MFR_DESC = "Acme Switch Co.";
    private static final String HW_DESC = "Coyote WB10";
    private static final String SW_DESC = "OF-Sneaky 1.0";
    private static final String SER_NUM = "WB-11937-TAF";
    private static final String DP_DESC = "WB10 at top of Mesa, Top Shelf";

    private static final IpAddress REMOTE_IP = ip("15.1.2.3");
    private static final PortNumber REMOTE_PORT = PortNumber.valueOf(65530);

    private static final long ts;
    static {
        GregorianCalendar cal = new GregorianCalendar(2013, 4, 23, 10, 9, 8);
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        ts = cal.getTimeInMillis();
    }

    private final ProtocolVersion version;

    public MockDataPathInfo(ProtocolVersion version) {
        this.version = version;
    }

    @Override public DataPathId dpid() { return DPID; }
    @Override public ProtocolVersion negotiated() { return version; }
    @Override public long readyAt() { return ts; }
    @Override public long lastMessageAt() { return ts; }

    @Override
    public List<Port> ports() {
        return (version.ge(V_1_3))
            ? Arrays.asList(JSON.fromJson(PortCodecTest.port13, Port.class))
            : Arrays.asList(JSON.fromJson(PortCodecTest.port10, Port.class));
    }

    @Override public long numBuffers() { return NBUFF; }
    @Override public int numTables() { return NTAB; }

    @Override public Set<Capability> capabilities() { 
        return version.ge(V_1_3) ? CAP_SET_13 : EMPTY_CAPS_SET; 
    }

    @Override public IpAddress remoteAddress() { return REMOTE_IP; }
    @Override public PortNumber remotePort() { return REMOTE_PORT; }

    @Override public String datapathDescription() { return DP_DESC; } 
    @Override public String manufacturerDescription() { return MFR_DESC; } 
    @Override public String hardwareDescription() { return HW_DESC; } 
    @Override public String softwareDescription() { return SW_DESC; } 
    @Override public String serialNumber() { return SER_NUM; }
    @Override public String deviceTypeName() { return null; }
}
