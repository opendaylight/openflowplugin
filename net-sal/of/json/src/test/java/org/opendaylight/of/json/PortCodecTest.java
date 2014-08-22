/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.msg.MutablePort;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.of.lib.msg.PortFactory;
import org.opendaylight.util.json.JSON;
import org.opendaylight.util.json.JsonFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link PortCodec}.
 * 
 * @author Liem Nguyen
 * @author Simon Hunt
 */
public class PortCodecTest extends AbstractCodecTest {

    private static final String JSON_PORT_13 = "v13/port";
    private static final String JSON_PORT_10 = "v10/port";
    private static final String JSON_PORT_10_OUT = "v10/portOut";
    private static final String JSON_PORT_10_CTRL = "v10/portCtrl";

    static final String port13 = getJsonContents(JSON_PORT_13);
    static final String port10 = getJsonContents(JSON_PORT_10);
    static final String port10_out = getJsonContents(JSON_PORT_10_OUT);
    static final String port10_ctrl = getJsonContents(JSON_PORT_10_CTRL);

    @BeforeClass
    public static void oneTimeInit() {
        JsonFactory factory = new OfJsonFactory();
        JSON.registerFactory(factory);
    }

    @Test
    public void testPort10() {
        Port port = JSON.fromJson(port10, Port.class);
        String copy = JSON.toJson(port, true);
        assertEquals(AM_NEQ, normalizeEOL(port10_out), normalizeEOL(copy));
        
        // Work with port_out as input as well...
        port = JSON.fromJson(port10_out, Port.class);
        copy = JSON.toJson(port, true);
        assertEquals(AM_NEQ, normalizeEOL(port10_out), normalizeEOL(copy));

        // Schema validate
        validate(copy, PortCodec.ROOT);
    }

    @Test
    public void testPort13() {
        Port port = JSON.fromJson(port13, Port.class);
        String portString = JSON.toJson(port, true);
        print(portString);
        String copy = JSON.toJson(port, true);
        assertEquals(AM_NEQ, normalizeEOL(port13), normalizeEOL(copy));

        // Schema validate
        validate(copy, PortCodec.ROOT);
    }

    @Test
    public void testPorts13() {
        Port port1 = JSON.fromJson(port13, Port.class);
        MutablePort port2 = PortFactory.mutableCopy(port1);
        port2.name("sdn2");
        List<Port> ports = Arrays.asList(port1, port2);
        String portArray = JSON.toJsonList(ports, Port.class, true);
        print(portArray);
        List<Port> actualPorts = JSON.fromJsonList(portArray, Port.class);
        assertEquals(AM_UXS, ports.size(), actualPorts.size());
        assertEquals(AM_NEQ, port1.getName(), actualPorts.get(0).getName());
        assertEquals(AM_NEQ, port2.getName(), actualPorts.get(1).getName());

        // Schema validate
        validate(portArray, PortCodec.ROOTS);
    }
    
    @Test
    public void testExp13() {
        validate(port13, PortCodec.ROOT);
    }

    @Test
    public void testNullList() {
        assertEquals(AM_NEQ, normalizeEOL("{\"" + PortCodec.ROOTS + "\":[]}"),
                normalizeEOL(JSON.toJsonList(null, Port.class)));
    }

    @Test
    public void port10Ctrl() {
        Port p = JSON.fromJson(port10_ctrl, Port.class);
        print(p);
        assertEquals(AM_NEQ, Port.CONTROLLER, p.getPortNumber());
        String toJson = JSON.toJson(p);
        // TODO: fix this once we have 'compact-vs-multiline' JSON comparison
//        assertEquals(AM_NEQ, normalizeEOL(port10_ctrl), normalizeEOL(toJson));
    }

}
