/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.MatchUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.yang.common.Uint32;

public class MatchUtilTest {
    private static final Ipv4Address IPV4_ADDRESS = new Ipv4Address("1.2.3.4");
    private static final Uint32 IPV4_LONG = Uint32.valueOf(16909060);

    @Test
    public void testIpv4toLong() {
        assertEquals(IPV4_LONG, MatchUtil.ipv4ToUint32(IPV4_ADDRESS));
    }

    @Test
    public void testLongtoIpv4() {
        assertEquals(IPV4_ADDRESS, MatchUtil.uint32ToIpv4Address(IPV4_LONG));
    }
}
