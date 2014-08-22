/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.LogUtils;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;

import java.util.*;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageType.PORT_STATUS;
import static org.opendaylight.of.lib.msg.PortConfig.*;
import static org.opendaylight.of.lib.msg.PortFeature.*;
import static org.opendaylight.of.lib.msg.PortState.LINK_DOWN;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the PortStateTracker.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public class PortStateTrackerTest {
    private static final String E_FIND_PORT =
            "Failed to find target port in supplied list";

    // === Expected values
    private static final BigPortNumber EXP_PNUM_0 = BigPortNumber.valueOf(258);
    private static final MacAddress EXP_MAC_0 =
            MacAddress.valueOf("114477:112233");
    private static final String EXP_NAME_0 = "Two";
    private static final Set<PortConfig> EXP_CFG_0 =
            new TreeSet<PortConfig>(Arrays.asList(NO_RECV, NO_FWD, NO_PACKET_IN));
    private static final Set<PortState> EXP_STATE_0 =
            new TreeSet<PortState>(Arrays.asList(LINK_DOWN));

    private static final BigPortNumber EXP_PNUM_1 = BigPortNumber.valueOf(259);
    private static final MacAddress EXP_MAC_1 =
            MacAddress.valueOf("114477:22ab42");
    private static final String EXP_NAME_1 = "Three";
    private static final Set<PortConfig> EXP_CFG_1 =
            new TreeSet<PortConfig>(Arrays.asList(NO_FWD));
    private static final Set<PortState> EXP_STATE_1 =
            new TreeSet<PortState>();

    private static final BigPortNumber EXP_PNUM_2 = BigPortNumber.valueOf(257);
    private static final MacAddress EXP_MAC_2 =
            MacAddress.valueOf("114477:22ab17");
    private static final String EXP_NAME_2 = "Four";
    private static final Set<PortConfig> EXP_CFG_2 =
            new TreeSet<PortConfig>(Arrays.asList(NO_FWD));
    private static final Set<PortState> EXP_STATE_2 =
            new TreeSet<PortState>();

    private static final Set<PortFeature> EXP_CURR =
            new TreeSet<PortFeature>(Arrays.asList(RATE_1GB_FD, FIBER, AUTONEG));
    private static final Set<PortFeature> EXP_ADV =
            new TreeSet<PortFeature>(Arrays.asList(RATE_1GB_FD, FIBER));
    private static final Set<PortFeature> EXP_SUPP =
            new TreeSet<PortFeature>(Arrays.asList(RATE_1GB_FD, FIBER, AUTONEG));
    private static final Set<PortFeature> EXP_PEER = null;

    private static final long EXP_CURR_SPEED = 1100000;
    private static final long EXP_MAX_SPEED = 3000000;

    private static final DataPathId DPID_0 =
            DataPathId.valueOf("42/0016b9:006502");
    private static final DataPathId DPID_1 =
            DataPathId.valueOf("42/0016b9:006503");
    private static final DataPathId DPID_2 =
            DataPathId.valueOf("42/0016b9:006504");
    private static final DataPathId DPID_3 =
            DataPathId.valueOf("42/0016b9:006505");

    private PortStateTracker trak;
    private Map<DataPathId, List<Port>> testPortListMap;

    @Before
    public void setUp() {
        trak = new PortStateTracker(null);
        testPortListMap = populatePortMap(DPID_0, DPID_1, DPID_2, DPID_3);

        // hides output of the expected error messages during unit test
        LogUtils.configureJdkLoggingFromProperty();
    }




    @Test
    public void basic() {
        print(EOL + "basic()");
        print(trak);
        assertEquals(AM_UXS, 0, trak.getCache().size());
    }

    @Test
    public void addPortList() {
        print(EOL + "addPortList()");
        initPortsForDpid(DPID_0, DPID_1, DPID_2, DPID_3);
        print(trak);
        assertEquals(AM_UXS, 4, trak.getCache().size());

        PortStateTracker.DpPortState dps = trak.getCache().get(DPID_0);
        assertNotNull(AM_HUH, dps);
        assertEquals(AM_NEQ, DPID_0, dps.getDpid());
        assertEquals(AM_UXS, 2, dps.getPorts().size());
        Port p = findPortInList(dps.getPorts(), EXP_PNUM_0);
        assertEquals(AM_NEQ, EXP_PNUM_0, p.getPortNumber());
        assertEquals(AM_NEQ, EXP_MAC_0, p.getHwAddress());
        assertEquals(AM_NEQ, EXP_NAME_0, p.getName());
        assertEquals(AM_NEQ, EXP_CFG_0, p.getConfig());
        assertEquals(AM_NEQ, EXP_STATE_0, p.getState());

        dps = trak.getCache().get(DPID_1);
        assertNotNull(AM_HUH, dps);
        assertEquals(AM_NEQ, DPID_1, dps.getDpid());
        assertEquals(AM_UXS, 2, dps.getPorts().size());
        p = findPortInList(dps.getPorts(), EXP_PNUM_1);
        assertEquals(AM_NEQ, EXP_PNUM_1, p.getPortNumber());
        assertEquals(AM_NEQ, EXP_MAC_1, p.getHwAddress());
        assertEquals(AM_NEQ, EXP_NAME_1, p.getName());
        assertEquals(AM_NEQ, EXP_CFG_1, p.getConfig());
        assertEquals(AM_NEQ, EXP_STATE_1, p.getState());

        dps = trak.getCache().get(DPID_2);
        assertNotNull(AM_HUH, dps);
        assertEquals(AM_NEQ, DPID_2, dps.getDpid());
        assertEquals(AM_UXS, 2, dps.getPorts().size());
        dps = trak.getCache().get(DPID_3);
        assertNotNull(AM_HUH, dps);
        assertEquals(AM_NEQ, DPID_3, dps.getDpid());
        assertEquals(AM_UXS, 2, dps.getPorts().size());
    }

    @Test
    public void removeDpid() {
        print(EOL + "removeDpid()");
        initPortsForDpid(DPID_1, DPID_3);
        print(trak);
        assertEquals(AM_UXS, 2, trak.getCache().size());
        trak.dpRemoved(DPID_1);
        print(trak);
        assertEquals(AM_UXS, 1, trak.getCache().size());
        PortStateTracker.DpPortState dps = trak.getCache().get(DPID_3);
        assertNotNull(dps);
        assertEquals(AM_NEQ, DPID_3, dps.getDpid());
    }

    @Test
    public void removeUnknownDpid() {
        print(EOL + "removeUnknownDpid()");
        initPortsForDpid(DPID_0, DPID_2, DPID_3);
        print(trak);
        assertEquals(AM_UXS, 3, trak.getCache().size());
        trak.dpRemoved(DPID_1);
        print(trak);
        assertEquals(AM_UXS, 3, trak.getCache().size());
    }

    @Test
    public void getPorts() {
        print(EOL + "getPorts()");
        initPortsForDpid(DPID_0, DPID_1, DPID_2, DPID_3);
        print(trak);
        assertEquals(AM_UXS, 4, trak.getCache().size());

        List<Port> ports = trak.getPorts(DPID_0);
        assertEquals(AM_UXS, 2, ports.size());

        Port p = findPortInList(ports, EXP_PNUM_0);
        assertEquals(AM_NEQ, EXP_PNUM_0, p.getPortNumber());
        assertEquals(AM_NEQ, EXP_MAC_0, p.getHwAddress());
        assertEquals(AM_NEQ, EXP_NAME_0, p.getName());
        assertEquals(AM_NEQ, EXP_CFG_0, p.getConfig());
        assertEquals(AM_NEQ, EXP_STATE_0, p.getState());

        ports = trak.getPorts(DPID_1);
        assertEquals(AM_UXS, 2, ports.size());
        ports = trak.getPorts(DPID_2);
        assertEquals(AM_UXS, 2, ports.size());
        ports = trak.getPorts(DPID_3);
        assertEquals(AM_UXS, 2, ports.size());
    }

    @Test
    public void getPortsUnknownDpid() {
        print(EOL + "getPortsUnknownDpid()");
        initPortsForDpid(DPID_1, DPID_2);
        print(trak);
        assertEquals(AM_UXS, 2, trak.getCache().size());
        List<Port> ports = trak.getPorts(DPID_3);
        assertNull(AM_HUH, ports);
    }

    @Test
    public void processPortAddMessage() throws IncompleteStructureException {
        print(EOL + "processPortAddMessage()");
        initPortsForDpid(DPID_3, DPID_0, DPID_2, DPID_1);
        print(trak);
        assertEquals(AM_UXS, 4, trak.getCache().size());

        List<Port> ports = trak.getPorts(DPID_0);
        assertEquals(AM_UXS, 2, ports.size());
        ports = trak.getPorts(DPID_1);
        assertEquals(AM_UXS, 2, ports.size());
        ports = trak.getPorts(DPID_2);
        assertEquals(AM_UXS, 2, ports.size());
        ports = trak.getPorts(DPID_3);
        assertEquals(AM_UXS, 2, ports.size());

        OfmPortStatus addMsg= createAddMsg();
        trak.portStatus(addMsg, DPID_3);
        trak.portStatus(addMsg, DPID_1);

        ports = trak.getPorts(DPID_3);
        assertEquals(AM_UXS, 3, ports.size());
        ports = trak.getPorts(DPID_1);
        assertEquals(AM_UXS, 3, ports.size());
        ports = trak.getPorts(DPID_2);
        assertEquals(AM_UXS, 2, ports.size());
        ports = trak.getPorts(DPID_0);
        assertEquals(AM_UXS, 2, ports.size());
    }

    @Test
    public void processPortRemoved() throws IncompleteStructureException {
        print(EOL + "processPortRemoved()");
        initPortsForDpid(DPID_1, DPID_2);
        print(trak);
        List<Port> ports = trak.getPorts(DPID_1);
        assertEquals(AM_UXS, 2, ports.size());
        ports = trak.getPorts(DPID_2);
        assertEquals(AM_UXS, 2, ports.size());

        // remove one port
        Port toRemove = findPortInList(testPortListMap.get(DPID_1), EXP_PNUM_0);
        OfmPortStatus removedMsg = createRemovedMsg(toRemove);
        trak.portStatus(removedMsg, DPID_1);

        ports = trak.getPorts(DPID_1);
        assertEquals(AM_UXS, 1, ports.size());
        ports = trak.getPorts(DPID_2);
        assertEquals(AM_UXS, 2, ports.size());

        // try to remove it again
        trak.portStatus(removedMsg, DPID_1);
        ports = trak.getPorts(DPID_1);
        assertEquals(AM_UXS, 1, ports.size());
        ports = trak.getPorts(DPID_2);
        assertEquals(AM_UXS, 2, ports.size());

        // remove the last port
        toRemove = findPortInList(testPortListMap.get(DPID_1), EXP_PNUM_1);
        removedMsg = createRemovedMsg(toRemove);
        trak.portStatus(removedMsg, DPID_1);

        ports = trak.getPorts(DPID_1);
        assertEquals(AM_UXS, 0, ports.size());
        ports = trak.getPorts(DPID_2);
        assertEquals(AM_UXS, 2, ports.size());
    }

    @Test
    public void processedPortModifiedMsg() throws IncompleteStructureException {
        print(EOL + "processedPortModifiedMsg()");
        initPortsForDpid(DPID_0, DPID_1, DPID_2, DPID_3);
        print(trak);

        // verify the port state
        List<Port> portList = trak.getPorts(DPID_1);
        Port target = findPortInList(portList, EXP_PNUM_0);
        print(target);
        assertEquals(AM_NEQ, EXP_STATE_0, target.getState());
        target = findPortInList(portList, EXP_PNUM_1);
        assertEquals(AM_NEQ, EXP_STATE_1, target.getState());

        // modify one port
        Port toModify = findPortInList(testPortListMap.get(DPID_1), EXP_PNUM_0);
        Set<PortState> modState =
                new TreeSet<PortState>(Arrays.asList(PortState.LIVE,
                        PortState.BLOCKED));

        OfmPortStatus modMsg = createModifiedMsg(toModify, modState);
        trak.portStatus(modMsg, DPID_1);

        // verify modification
        portList = trak.getPorts(DPID_1);
        target = findPortInList(portList, EXP_PNUM_0);
        print(target);
        assertEquals(AM_NEQ, modState, target.getState());
        target = findPortInList(portList, EXP_PNUM_1);
        assertEquals(AM_NEQ, EXP_STATE_1, target.getState());
    }

    @Test
    public void processMsgUnknownDpid() throws IncompleteStructureException {
        print(EOL + "processMsgUnknownDpid()");
        initPortsForDpid(DPID_3);
        print(trak);
        assertEquals(AM_UXS, 1, trak.getCache().size());
        assertEquals(AM_UXS, 2, trak.getPorts(DPID_3).size());
        OfmPortStatus addMsg= createAddMsg();

        trak.portStatus(addMsg, DPID_1);
        assertEquals(AM_UXS, 1, trak.getCache().size());
        assertEquals(AM_UXS, 2, trak.getPorts(DPID_3).size());

    }

    // ==================================== helper methods ====================

    // add some ports to the port state tracker
    private void initPortsForDpid(DataPathId... dpids) {
        for (DataPathId dpid : dpids)
            trak.portInit(dpid, testPortListMap.get(dpid));
    }

    // populate ports for each data path
    private Map<DataPathId, List<Port>> populatePortMap(DataPathId... dpids) {
        Map<DataPathId, List<Port>> map = new HashMap<DataPathId, List<Port>>();
        for (DataPathId dpid : dpids) {
            List<Port> ports = new ArrayList<Port>();

            MutablePort mp = PortFactory.createPort(V_1_3);
            mp.portNumber(EXP_PNUM_0).hwAddress(EXP_MAC_0).name(EXP_NAME_0)
                    .config(EXP_CFG_0).state(EXP_STATE_0).current(EXP_CURR)
                    .advertised(EXP_ADV).supported(EXP_SUPP).peer(EXP_PEER)
                    .currentSpeed(EXP_CURR_SPEED).maxSpeed(EXP_MAX_SPEED);
            ports.add((Port) mp.toImmutable());

            mp = PortFactory.createPort(V_1_3);
            mp.portNumber(EXP_PNUM_1).hwAddress(EXP_MAC_1).name(EXP_NAME_1)
                    .config(EXP_CFG_1).state(EXP_STATE_1).current(EXP_CURR)
                    .advertised(EXP_ADV).supported(EXP_SUPP).peer(EXP_PEER)
                    .currentSpeed(EXP_CURR_SPEED).maxSpeed(EXP_MAX_SPEED);
            ports.add((Port) mp.toImmutable());
            map.put(dpid, ports);
        }
        return map;
    }

    // locate the desired port based on its port number
    private Port findPortInList(List<Port> portList, BigPortNumber portNum) {
        Port target = null;
        for (Port p : portList) {
            if (p.getPortNumber() == portNum) {
                target = p;
                break;
            }
        }

        if (target == null)
            fail(E_FIND_PORT);

        return target;
    }

    // create the OpenFlow message to add an OpenFlow port
    private OfmPortStatus createAddMsg() throws IncompleteStructureException {
        MutableMessage mm = MessageFactory.create(V_1_3, PORT_STATUS);
        OfmMutablePortStatus mmPStat = (OfmMutablePortStatus) mm;
        MutablePort mp = PortFactory.createPort(V_1_3);
        mp.portNumber(EXP_PNUM_2).hwAddress(EXP_MAC_2).name(EXP_NAME_2)
                .config(EXP_CFG_2).state(EXP_STATE_2).current(EXP_CURR)
                .advertised(EXP_ADV).supported(EXP_SUPP).peer(EXP_PEER)
                .currentSpeed(EXP_CURR_SPEED).maxSpeed(EXP_MAX_SPEED);

        mmPStat.port((Port) mp.toImmutable()).reason(PortReason.ADD);
        return (OfmPortStatus) mm.toImmutable();
    }

    // create the OpenFlow message to remove an OpenFlow port
    private OfmPortStatus createRemovedMsg(Port removedPort)
            throws IncompleteStructureException {
        MutableMessage mm = MessageFactory.create(V_1_3, PORT_STATUS);
        OfmMutablePortStatus mmPStat = (OfmMutablePortStatus) mm;
        mmPStat.port(removedPort).reason(PortReason.DELETE);
        return (OfmPortStatus) mm.toImmutable();
    }

    // create the OpenFlow message to modify an OpenFlow port
    private OfmPortStatus createModifiedMsg(Port modPort,
                                            Set<PortState> modState)
            throws IncompleteStructureException {

        MutableMessage mm = MessageFactory.create(V_1_3, PORT_STATUS);
        OfmMutablePortStatus mmPStat = (OfmMutablePortStatus) mm;

        MutablePort mp = PortFactory.mutableCopy(modPort);
        mp.state(modState);

        mmPStat.port((Port) mp.toImmutable()).reason(PortReason.MODIFY);
        return (OfmPortStatus) mm.toImmutable();
    }
}
