/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor;

import static org.junit.Assert.assertEquals;

import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.MatchUtil;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

public class MatchUtilTest {

    private static final Ipv4Address IPV4_ADDRESS = new Ipv4Address("1.2.3.4");
    private static final Long IPV4_LONG = new Long(16909060L);

    @Test
    public void testIpv4toLong() {
        final Long result = MatchUtil.ipv4ToLong(IPV4_ADDRESS);
        assertEquals("Does not match",IPV4_LONG,result);
    }

    @Test
    public void testLongtoIpv4() {
        Ipv4Address result = MatchUtil.longToIpv4Address(16909060L);
        assertEquals("Does not match",IPV4_ADDRESS,result);
    }

}
