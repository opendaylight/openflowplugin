/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

public class IpConverterTest {

    @Test
    public void testIpv4AddressToLong() {
        Ipv4Address ipAddress = new Ipv4Address("192.168.1.2");
        long address = 0xc0a80102L;
        assertEquals(address, IpConverter.Ipv4AddressToLong(ipAddress));
    }

    @Test
    public void testIpv4AddressToLong2() {
        Ipv4Address ipAddress = new Ipv4Address("10.168.1.2");
        long address = 0x0aa80102L;
        assertEquals(address, IpConverter.Ipv4AddressToLong(ipAddress));
    }

}
