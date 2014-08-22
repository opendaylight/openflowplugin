/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import org.opendaylight.net.model.Interface.State;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IfType;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;


public class DefaultInterfaceTest {

    private static final State state = State.UP;
    private static Set<State> ifState = new TreeSet<State>(); 
    private static final String name = "ifName";
    private static final MacAddress mac = MacAddress.valueOf("00:11:22:33:44:55");
    private static final IfType type = IfType.ETHERNET_CSMACD;
    private static final ElementId hosted = EasyMock.createMock(ElementId.class);
    private static final ElementId realizedElement = EasyMock.createMock(ElementId.class);
    private static final InterfaceId realizedInterface = EasyMock.createMock(InterfaceId.class);
    private static final Set<IpAddress> ipAddrs = new HashSet<>();
    private static final BigPortNumber bpn = BigPortNumber.valueOf(1);
    private static final InterfaceId id = InterfaceId.valueOf(bpn);

    private DefaultInterface dif;
    private DefaultInterfaceInfo info;

    @Before
    public void setup() {
        ipAddrs.add(IpAddress.valueOf("1.2.3.4"));
        ifState.add(state);
        info = new DefaultInterfaceInfo(hosted, bpn).state(ifState).name(name)
                .mac(mac).type(type).realizedByElement(realizedElement)
                .realizedByInterface(realizedInterface).ipAddresses(ipAddrs);
    }

    @Test
    public void basic() {
        dif = new DefaultInterface(id, info);
        assertEquals(id, dif.id());
        assertEquals(ifState, dif.state());
        assertTrue(dif.isEnabled());
        assertFalse(dif.isBlocked());
        assertEquals(name, dif.name());
        assertEquals(mac, dif.mac());
        assertEquals(type, dif.type());
        assertEquals(hosted, dif.hostedBy());
        assertEquals(realizedElement, dif.realizedByElement());
        assertEquals(realizedInterface, dif.realizedByInterface());
        assertEquals(ipAddrs, dif.ipAddresses());
        assertNotNull(dif.toString());
//        assertTrue(dif.getId().getValue().startsWith(hosted.toString()));
//        assertTrue(dif.getId().getValue().endsWith(id.toString()));
    }

    @Test (expected = NullPointerException.class)
    public void nullId() {
        new DefaultInterface(null, info);
    }
    @Test (expected = NullPointerException.class)
    public void nullInfo() {
        new DefaultInterface(id, null);
    }
}
