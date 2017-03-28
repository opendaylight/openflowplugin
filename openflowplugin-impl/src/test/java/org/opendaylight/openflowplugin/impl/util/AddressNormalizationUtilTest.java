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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

public class AddressNormalizationUtilTest {

    @Test
    public void normalizeProtocolAgnosticPort() throws Exception {
        final Uri left = new Uri("openflow:1:INPORT");
        final Uri right = new Uri("INPORT");

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

    }

    @Test
    public void normalizeIpv4Prefix() throws Exception {
    }

    @Test
    public void normalizeIpv4Arbitrary() throws Exception {
    }

    @Test
    public void normalizeIpv4Address() throws Exception {
    }

    @Test
    public void normalizeIpv6Address() throws Exception {
    }

    @Test
    public void normalizeIpAddress() throws Exception {
    }

    @Test
    public void normalizeInetAddressWithMask() throws Exception {
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