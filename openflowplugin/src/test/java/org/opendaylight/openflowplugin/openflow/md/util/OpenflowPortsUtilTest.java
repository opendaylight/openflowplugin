/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;

/**
 * @author: Kamal Rameshan (kramesha@cisco.com)
 * @since : 6/2/14
 */
public class OpenflowPortsUtilTest {
    private static Map<String, Long> mapOF10Ports;
    private static Map<String, Long> mapOF13Ports;
    private static Map<OpenflowVersion, Map<String, Long>> mapVersionToPorts;

    /**
     * initiation before testing - once for all
     */
    @BeforeClass
    public static void setupClass() {
        mapOF10Ports = new HashMap<String, Long>();
        mapOF10Ports.put(OutputPortValues.MAX.getName(), 65280L);
        mapOF10Ports.put(OutputPortValues.INPORT.getName(), 65528L);
        mapOF10Ports.put(OutputPortValues.TABLE.getName(), 65529L);
        mapOF10Ports.put(OutputPortValues.NORMAL.getName(), 65530L);
        mapOF10Ports.put(OutputPortValues.FLOOD.getName(), 65531L);
        mapOF10Ports.put(OutputPortValues.ALL.getName(), 65532L);
        mapOF10Ports.put(OutputPortValues.CONTROLLER.getName(), 65533L);
        mapOF10Ports.put(OutputPortValues.LOCAL.getName(), 65534L);
        mapOF10Ports.put(OutputPortValues.NONE.getName(), 65535L);

        mapOF13Ports = new HashMap<String, Long>();
        mapOF13Ports.put(OutputPortValues.MAX.getName(), 4294967040L);
        mapOF13Ports.put(OutputPortValues.INPORT.getName(), 4294967288L);
        mapOF13Ports.put(OutputPortValues.TABLE.getName(), 4294967289L);
        mapOF13Ports.put(OutputPortValues.NORMAL.getName(), 4294967290L);
        mapOF13Ports.put(OutputPortValues.FLOOD.getName(), 4294967291L);
        mapOF13Ports.put(OutputPortValues.ALL.getName(), 4294967292L);
        mapOF13Ports.put(OutputPortValues.CONTROLLER.getName(), 4294967293L);
        mapOF13Ports.put(OutputPortValues.LOCAL.getName(), 4294967294L);
        mapOF13Ports.put(OutputPortValues.ANY.getName(), 4294967295L);

        mapVersionToPorts = new HashMap<OpenflowVersion, Map<String, Long>>();
        mapVersionToPorts.put(OpenflowVersion.OF10, mapOF10Ports);
        mapVersionToPorts.put(OpenflowVersion.OF13, mapOF13Ports);

    }

    /**
     * tearing down initiated values after all tests done
     */
    @AfterClass
    public static void tearDownClass() {
        mapOF10Ports.clear();
        mapOF13Ports.clear();
        mapVersionToPorts.clear();
    }

    //helper
    private static void matchGetLogicalName(final OpenflowVersion version, final String logicalName) {
        Assert.assertEquals("Controller reserve port not matching to logical-name for "+ version,
                logicalName,
                OpenflowPortsUtil.getPortLogicalName(version, mapVersionToPorts.get(version).get(logicalName)));
    }

    //helper
    private static void matchGetPortfromLogicalName(final OpenflowVersion version, final String logicalName) {
        Assert.assertEquals("Controller reserve port not matching to logical-name for "+ version,
                mapVersionToPorts.get(version).get(logicalName), OpenflowPortsUtil.getPortFromLogicalName(version, logicalName));
    }

    /**
     * test for method {@link OpenflowPortsUtil#getPortLogicalName(OpenflowVersion, Long)}
     */
    @Test
    public void testGetPortLogicalName() {

        String s = OutputPortValues.INPORT.getName();
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.MAX.getName());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.INPORT.getName());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.TABLE.getName());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.NORMAL.getName());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.FLOOD.getName());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.ALL.getName());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.CONTROLLER.getName());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.LOCAL.getName());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.NONE.getName());

        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.MAX.getName());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.INPORT.getName());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.TABLE.getName());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.NORMAL.getName());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.FLOOD.getName());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.ALL.getName());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.CONTROLLER.getName());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.LOCAL.getName());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.ANY.getName());

        Assert.assertNull("Invalid port number should return a null",
                OpenflowPortsUtil.getPortLogicalName(OpenflowVersion.OF10, 99999L));

        Assert.assertNull("Invalid port number should return a null",
                OpenflowPortsUtil.getPortLogicalName(OpenflowVersion.OF13, 99999L));
        Assert.assertFalse(s.equals("a"));
    }



    /**
     * test for method {@link OpenflowPortsUtil#getPortFromLogicalName(OpenflowVersion, String)}
     */
    @Test
    public void testGetPortFromLogicalName() {

        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.MAX.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.INPORT.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.TABLE.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.NORMAL.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.FLOOD.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.ALL.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.CONTROLLER.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.LOCAL.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.NONE.getName());

        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.MAX.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.INPORT.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.TABLE.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.NORMAL.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.FLOOD.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.ALL.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.CONTROLLER.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.LOCAL.getName());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.ANY.getName());

        Assert.assertNull("Invalid port logical name should return a null",
                OpenflowPortsUtil.getPortFromLogicalName(OpenflowVersion.OF10, "abc"));

        Assert.assertNull("Invalid port logical name should return a null",
                OpenflowPortsUtil.getPortFromLogicalName(OpenflowVersion.OF13, "abc"));

    }

    /**
     * test for method {@link OpenflowPortsUtil#checkPortValidity(OpenflowVersion, Long)} - OF-1.0
     */
    @Test
    public void testCheckPortValidity10() {
        Assert.assertFalse(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF10 , -1L));
        Assert.assertTrue(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF10 , 0L));
        Assert.assertTrue(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF10 , 0xFF00L));
        Assert.assertTrue(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF10 , 0xFFF8L));
        Assert.assertFalse(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF10 , 0xFFF0L));
        Assert.assertTrue(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF10 , 0xFFFFL));
        Assert.assertFalse(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF10 , 0x1FFFFL));
    }

    /**
     * test for method {@link OpenflowPortsUtil#checkPortValidity(OpenflowVersion, Long)} - OF-1.3
     */
    @Test
    public void testCheckPortValidity13() {
        Assert.assertFalse(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF13 , -1L));
        Assert.assertTrue(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF13 , 0L));
        Assert.assertTrue(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF13 , 0xFFFFFF00L));
        Assert.assertTrue(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF13 , 0xFFFFFFF8L));
        Assert.assertFalse(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF13 , 0xFFFFFFF0L));
        Assert.assertTrue(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF13 , 0xFFFFFFFFL));
        Assert.assertFalse(OpenflowPortsUtil.checkPortValidity(OpenflowVersion.OF13 , 0x1FFFFFFFFL));
    }

    /**
     * test for method {@link OpenflowPortsUtil}
     */
    @Test
    public void testPortNumberToString() {
        PortNumberUni portNumber;

        portNumber = new PortNumberUni(42L);
        Assert.assertEquals("42", OpenflowPortsUtil.portNumberToString(portNumber));

        portNumber = new PortNumberUni(OutputPortValues.FLOOD.toString());
        Assert.assertEquals("FLOOD", OpenflowPortsUtil.portNumberToString(portNumber));


        portNumber = new PortNumberUni((String) null);
        Assert.assertNotNull(portNumber);

    }

}
