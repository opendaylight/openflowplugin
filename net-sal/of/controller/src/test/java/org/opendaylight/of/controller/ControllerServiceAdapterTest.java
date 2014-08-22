/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller;

import org.junit.Test;
import org.opendaylight.of.controller.flow.*;
import org.opendaylight.of.controller.impl.DataPathListenerAdapter;
import org.opendaylight.of.controller.impl.MessageListenerAdapter;
import org.opendaylight.of.controller.pkt.SequencedPacketAdapter;
import org.opendaylight.of.controller.pkt.SequencedPacketListener;
import org.opendaylight.of.controller.pkt.SequencedPacketListenerRole;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.dt.*;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.packet.ProtocolId;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.Arrays.asList;
import static junit.framework.Assert.*;
import static org.opendaylight.of.controller.DatatypeUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;

/**
 * Unit tests for ControllerServiceAdapter.
 *
 * @author Simon Hunt
 */
public class ControllerServiceAdapterTest {

    private static final SequencedPacketListener SPL =
            new SequencedPacketAdapter();
    private static final SequencedPacketListenerRole ROLE = SequencedPacketListenerRole.OBSERVER;
    private static final int ALTITUDE = 0;
    private static final Set<ProtocolId> PROTOCOLS = Collections.emptySet();
    private static final DataPathListener DPL = new DataPathListenerAdapter();
    private static final MessageListener ML = new MessageListenerAdapter();
    private static final Set<MessageType> MSG_TYPES = Collections.emptySet();
    private static final OpenflowMessage OFM =
            create(V_1_0, ECHO_REPLY).toImmutable();
    private static final List<OpenflowMessage> OFM_LIST = asList(OFM);
    private static final DataPathId DPID = dpid("1/112233445566");
    private static final BigPortNumber PORT_NUM = bpn(37);
    private static final TableId TID = tid(1);
    private static final OfmFlowMod OFM_FLOW = (OfmFlowMod)
            create(V_1_0, FLOW_MOD).toImmutable();
    private static final FlowListener FLOW_LISTENER = new FlowListener() {
        @Override public void event(FlowEvent event) { }
    };
    private static final GroupListener GROUP_LISTENER = new GroupListener() {
        @Override public void event(GroupEvent event) { }
    };
    private static final MeterListener METER_LISTENER = new MeterListener() {
        @Override public void event(MeterEvent event) { }
    };
    private static final OfmMeterMod OFM_METER = (OfmMeterMod)
            create(V_1_3, METER_MOD).toImmutable();
    private static final GroupId GID = gid(1);
    private static final MeterId MID = mid(1);
    private static final OfmGroupMod OFM_GROUP = (OfmGroupMod)
            create(V_1_3, GROUP_MOD).toImmutable();
    private static final DataPathInfo DPI = new DataPathInfoAdapter();
    private static final MessageFuture FUTURE = new DefaultMessageFuture(OFM);
    private static final InitialFlowContributor IFC =
            new InitialFlowContributor() {
                @Override public List<OfmFlowMod>
                provideInitialFlows(DataPathInfo info, boolean isHybrid) {
                    return null;
                }
            };


    private static final int EXP_METHOD_COUNT = 44;
    private static final Set<String> EXP_METHOD_NAMES = new HashSet<>(
            Arrays.asList(
                    "addPacketListener",
                    "removePacketListener",
                    "getSplMetrics",
                    "addMessageListener",
                    "removeMessageListener",
                    "addDataPathListener",
                    "removeDataPathListener",
                    "getAllDataPathInfo",
                    "getDataPathInfo",
                    "versionOf",
                    "getStats",
                    "getPortStats",
                    "enablePort",
                    "send",
                    "getControllerMx",
                    "getFlowStats",
                    "registerInitialFlowContributor",
                    "unregisterInitialFlowContributor",
                    "sendFlowMod",
                    "sendConfirmedFlowMod",
                    "addFlowListener",
                    "removeFlowListener",
                    "getGroupDescription",
                    "getGroupStats",
                    "getGroupFeatures",
                    "sendGroupMod",
                    "addGroupListener",
                    "removeGroupListener",
                    "getMeterConfig",
                    "getMeterStats",
                    "getMeterFeatures",
                    "getExperimenter",
                    "sendMeterMod",
                    "addMeterListener",
                    "removeMeterListener",
                    "getPipelineDefinition",
                    "isHybridMode"
            )
    );

    // this unit test will fail if the interface is changed without updating
    // the above constants...
    @Test
    public void methodsDoubleCheck() {
        Method[] methods = ControllerService.class.getDeclaredMethods();
        assertEquals("Wrong number of methods defined", EXP_METHOD_COUNT,
                methods.length);
        Set<String> seen = new HashSet<>();
        for (Method m: methods) {
            String name = m.getName();
            seen.add(name);
            assertTrue("Method not vetted: " + name,
                    EXP_METHOD_NAMES.contains(name));
        }
        Set<String> missing = new HashSet<>(EXP_METHOD_NAMES);
        missing.removeAll(seen);
        assertTrue("Expected methods missing: " + missing, missing.isEmpty());
    }


    @Test
    public void basic() throws OpenflowException {
        ControllerService cs = new ControllerServiceAdapter();

        // call every method - make sure we get to the end of the test
        cs.addPacketListener(SPL, ROLE, ALTITUDE);
        cs.addPacketListener(SPL, ROLE, ALTITUDE, PROTOCOLS);
        cs.removePacketListener(SPL);
        assertNull(AM_HUH, cs.getSplMetrics());

        cs.addMessageListener(ML, MSG_TYPES);
        cs.removeMessageListener(ML);

        cs.addDataPathListener(DPL);
        cs.removeDataPathListener(DPL);
        assertNull(AM_HUH, cs.getAllDataPathInfo());
        assertNull(AM_HUH, cs.getDataPathInfo(DPID));
        assertNull(AM_HUH, cs.versionOf(DPID));
        assertNull(AM_HUH, cs.getStats());
        assertNull(AM_HUH, cs.getPortStats(DPID));
        assertNull(AM_HUH, cs.getPortStats(DPID, PORT_NUM));
        assertNull(AM_HUH, cs.enablePort(DPID, PORT_NUM, true));

        assertNull(AM_HUH, cs.send(OFM, DPID));
        assertNull(AM_HUH, cs.send(OFM_LIST, DPID));

        assertNull(AM_HUH, cs.getControllerMx());

        assertNull(AM_HUH, cs.getFlowStats(DPID, TID));
        cs.registerInitialFlowContributor(IFC);
        cs.unregisterInitialFlowContributor(IFC);

        cs.sendFlowMod(OFM_FLOW, DPID);
        assertNull(cs.sendConfirmedFlowMod(OFM_FLOW, DPID));
        cs.addFlowListener(FLOW_LISTENER);
        cs.removeFlowListener(FLOW_LISTENER);
        assertNull(cs.getPipelineDefinition(DPID));

        assertNull(AM_HUH, cs.getGroupDescription(DPID));
        assertNull(AM_HUH, cs.getGroupDescription(DPID, GID));
        assertNull(AM_HUH, cs.getGroupStats(DPID));
        assertNull(AM_HUH, cs.getGroupStats(DPID, GID));
        assertNull(AM_HUH, cs.getGroupFeatures(DPID));
        cs.sendGroupMod(OFM_GROUP, DPID);
        cs.addGroupListener(GROUP_LISTENER);
        cs.removeGroupListener(GROUP_LISTENER);

        assertNull(AM_HUH, cs.getMeterConfig(DPID));
        assertNull(AM_HUH, cs.getMeterConfig(DPID, MID));
        assertNull(AM_HUH, cs.getMeterStats(DPID));
        assertNull(AM_HUH, cs.getMeterStats(DPID, MID));
        assertNull(AM_HUH, cs.getMeterFeatures(DPID));

        assertNull(AM_HUH, cs.getExperimenter(DPID));
        
        cs.sendMeterMod(OFM_METER, DPID);
        cs.addMeterListener(METER_LISTENER);
        cs.removeMeterListener(METER_LISTENER);

        assertFalse(cs.isHybridMode());

        // we got to the end
        assertTrue(true);
    }

    private static class MyAdapter extends ControllerServiceAdapter {
        @Override
        public DataPathInfo getDataPathInfo(DataPathId dpid) {
            return DPI;
        }

        @Override
        public MessageFuture send(OpenflowMessage msg, DataPathId dpid)
                throws OpenflowException {
            return FUTURE;
        }
    }

    @Test
    public void overrideSomething() throws OpenflowException {
        ControllerServiceAdapter csa = new MyAdapter();
        assertEquals(AM_NEQ, DPI, csa.getDataPathInfo(DPID));
        assertEquals(AM_NEQ, FUTURE, csa.send(OFM, DPID));
        assertNull(AM_HUH, csa.getControllerMx());
        // etc...
    }
}
