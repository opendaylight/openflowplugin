/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import static org.junit.Assert.*;
import static org.opendaylight.util.CommonUtils.itemSet;

import java.util.Set;
import java.util.TreeSet;

import org.easymock.EasyMock;
import org.junit.Test;

import org.opendaylight.net.model.Interface.State;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IfType;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

/**
 * Unit tests for {@link org.opendaylight.net.model.DefaultInterfaceInfo}.
 *
 * @author Thomas Vachuska
 */
public class DefaultInterfaceInfoTest {

    private static final State state = State.UP;
    private static Set<State> ifState = new TreeSet<State>(); 
    private static final String name = "ifName";
    private static final MacAddress mac = MacAddress.valueOf("00:11:22:33:44:55");
    private static final IfType type = IfType.ETHERNET_CSMACD;
    private static final ElementId hosted = EasyMock.createMock(ElementId.class);
    private static final ElementId realizedElement = EasyMock.createMock(ElementId.class);
    private static final InterfaceId realizedInterface = EasyMock.createMock(InterfaceId.class);
    private static final Set<IpAddress> ipAddrs = itemSet(IpAddress.valueOf("1.2.3.4"));
    private static final BigPortNumber bpn = BigPortNumber.valueOf(1);

    private DefaultInterfaceInfo dii;

    @Test
    public void basic() {
        dii = new DefaultInterfaceInfo(hosted, bpn);
        assertNotNull(dii.id());
        assertNotNull(dii.hostedBy());
        assertNull(dii.state());
        assertNull(dii.name());
        assertNull(dii.type());
        assertNull(dii.mac());
        assertNull(dii.realizedByElement());
        assertNull(dii.realizedByInterface());
        assertNull(dii.ipAddresses());
        ifState.add(state);
    }
    @Test
    public void state() {
        dii = new DefaultInterfaceInfo(hosted, bpn).state(ifState);
        assertEquals(ifState, dii.state());
        assertTrue(dii.isEnabled());
        assertFalse(dii.isBlocked());
        dii.state(itemSet(State.DOWN, State.BLOCKED));
        assertFalse(dii.isEnabled());
        assertTrue(dii.isBlocked());
    }

    @Test
    public void name() {
        dii = new DefaultInterfaceInfo(hosted, bpn).name(name);
        assertEquals(name, dii.name());
    }

    @Test
    public void mac() {
        dii = new DefaultInterfaceInfo(hosted, bpn).mac(mac);
        assertEquals(mac, dii.mac());
    }

    @Test
    public void type() {
        dii = new DefaultInterfaceInfo(hosted, bpn).type(type);
        assertEquals(type, dii.type());
    }

    @Test
    public void realizedByElement() {
        dii = new DefaultInterfaceInfo(hosted, bpn).realizedByElement(realizedElement);
        assertEquals(realizedElement, dii.realizedByElement());
    }

    @Test
    public void realizedByInterface() {
        dii = new DefaultInterfaceInfo(hosted, bpn).realizedByInterface(realizedInterface);
        assertEquals(realizedInterface, dii.realizedByInterface());
    }

    @Test
    public void ipAddresses() {
        dii = new DefaultInterfaceInfo(hosted, bpn).ipAddresses(ipAddrs);
        assertEquals(ipAddrs, dii.ipAddresses());
    }

    @Test
    public void id() {
        dii = new DefaultInterfaceInfo(hosted, bpn);
        assertEquals(bpn, dii.id());
    }

    @Test
    public void setAll() {
        dii = new DefaultInterfaceInfo(hosted, bpn).name(name).state(ifState).type(type)
                .mac(mac).realizedByElement(realizedElement)
                .realizedByInterface(realizedInterface).ipAddresses(ipAddrs);
        assertEquals(name, dii.name());
        assertEquals(ifState, dii.state());
        assertEquals(type, dii.type());
        assertEquals(mac, dii.mac());
        assertEquals(hosted, dii.hostedBy());
        assertEquals(realizedElement, dii.realizedByElement());
        assertEquals(realizedInterface, dii.realizedByInterface());
        assertEquals(ipAddrs, dii.ipAddresses());
    }
}
