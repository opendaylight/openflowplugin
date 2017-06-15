/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;

public class AddressNormalizationUtilTest {

    @Test
    public void normalizeProtocolAgnosticPortOF10() throws Exception {
        final Uri left = new Uri("openflow:1:INPORT");
        final Uri right = new Uri("IN_PORT");

        assertEquals(
                right,
                AddressNormalizationUtil.normalizeProtocolAgnosticPort(left, OFConstants.OFP_VERSION_1_0)
        );
    }

    @Test
    public void normalizeProtocolAgnosticPortOF13() throws Exception {
        final Uri left = new Uri("openflow:1:ANY");
        final Uri right = new Uri("ANY");

        assertEquals(
                right,
                AddressNormalizationUtil.normalizeProtocolAgnosticPort(left, OFConstants.OFP_VERSION_1_3)
        );
    }

    @Test
    public void normalizeIpv6Prefix() throws Exception {
        final Ipv6Prefix left = new Ipv6Prefix("1E3D:5678:9ABC::/24");
        final Ipv6Prefix right = new Ipv6Prefix("1e3d:5600:0:0:0:0:0:0/24");

        assertEquals(
                right,
                AddressNormalizationUtil.normalizeIpv6Prefix(left)
        );
    }

    @Test
    public void normalizeIpv6Arbitrary() throws Exception {
        final Ipv6Address leftAddress = new Ipv6Address("1E3D:5678:9ABC::");
        final Ipv6ArbitraryMask leftMask = new Ipv6ArbitraryMask("FFFF:FF00::");
        final Ipv6Prefix right = new Ipv6Prefix("1e3d:5600:0:0:0:0:0:0/24");

        assertEquals(
                right,
                AddressNormalizationUtil.normalizeIpv6Arbitrary(leftAddress, leftMask)
        );
    }

    @Test
    public void normalizeIpv6AddressWithoutMask() throws Exception {
        final Ipv6Address left = new Ipv6Address("1E3D:5678:9ABC::");
        final Ipv6Address right = new Ipv6Address("1e3d:5678:9abc:0:0:0:0:0");

        assertEquals(
                right,
                AddressNormalizationUtil.normalizeIpv6AddressWithoutMask(left)
        );
    }

    @Test
    public void normalizeIpv4Prefix() throws Exception {
        final Ipv4Prefix left = new Ipv4Prefix("192.168.72.1/16");
        final Ipv4Prefix right = new Ipv4Prefix("192.168.0.0/16");

        assertEquals(
                right,
                AddressNormalizationUtil.normalizeIpv4Prefix(left)
        );
    }

    @Test
    public void normalizeIpv4Arbitrary() throws Exception {
        final Ipv4Address leftAddress = new Ipv4Address("192.168.72.1");
        final DottedQuad leftMask = new DottedQuad("255.255.0.0");
        final Ipv4Prefix right = new Ipv4Prefix("192.168.0.0/16");

        assertEquals(
                right,
                AddressNormalizationUtil.normalizeIpv4Arbitrary(leftAddress, leftMask)
        );
    }

    @Test
    public void normalizeMacAddress() throws Exception {
        final MacAddress left = new MacAddress("01:23:45:AB:CD:EF");
        final MacAddress right = new MacAddress("01:23:45:ab:cd:ef");

        assertEquals(
                right,
                AddressNormalizationUtil.normalizeMacAddress(left)
        );
    }

    @Test
    public void normalizeMacAddressMask() throws Exception {
        final MacAddress left = new MacAddress("FF:FF:FF:FF:FF:FF");
        final MacAddress right = null;

        assertEquals(
                right,
                AddressNormalizationUtil.normalizeMacAddressMask(left)
        );
    }

}