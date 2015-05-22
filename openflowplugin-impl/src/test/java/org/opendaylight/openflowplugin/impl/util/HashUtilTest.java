/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashUtilTest {

    private static final Logger LOG = LoggerFactory.getLogger(HashUtilTest.class);

    private static final MacAddress[] MAC_ADDRESSES = {
            new MacAddress("00:00:00:00:00:00"),
            new MacAddress("AA:BB:CC:DD:EE:FF"),
            new MacAddress("FF:EE:DD:CC:BB:AA"),
            new MacAddress("0A:37:C1:00:AB:FF"),
            new MacAddress("0A:37:C1:00:AB:FE"),
            new MacAddress("0A:37:C1:00:FE:AB")
    };

    private static final Ipv4Prefix[] IPV_4_PREFIXES = {
            new Ipv4Prefix("10.0.0.1/24"),
            new Ipv4Prefix("10.0.1.0/24"),
            new Ipv4Prefix("10.0.1.0/31"),
            new Ipv4Prefix("0.0.0.0/32"),
            new Ipv4Prefix("4.3.2.1/32"),
            new Ipv4Prefix("1.2.3.4/32")
    };

    private static final Ipv6Prefix[] IPV_6_PREFIXES = {
            new Ipv6Prefix("FFFF:0DB8:0000:0000:0000:0000:1428:57ab/128"),
            new Ipv6Prefix("2001:0DB8:0000:0000:0000:0000:1428:57ab/128"),
            new Ipv6Prefix("0DB8:2001:0000:0000:0000:0000:1428:57ab/128"),
            new Ipv6Prefix("2001:0DB8:0000:0000:0000:0000:57ab:1428/128")
    };

    private static final Ipv6Prefix shortIpv6 = new Ipv6Prefix("fe80::2acf:e9ff:fe21:6431/128");
    private static final Ipv6Prefix fullIpv6 = new Ipv6Prefix("fe80:0000:2acf:e9ff:fe21:6431:0000:0000/128");
    private static final Ipv6Prefix[] IPV_6_PREFIXES_WITH_MASK = {
            new Ipv6Prefix("FFFF:0DB8:0000:0000:0000:0000:1428:57ab/72"),
            new Ipv6Prefix("2001:0DB8:0000:0000:0000:0000:1428:57ab/94"),
            new Ipv6Prefix("0DB8:2001:0000:0000:0000:0000:1428:57ab/32"),
            new Ipv6Prefix("2001:0DB8:0000:0000:0000:0000:57ab:1428/64")
    };


    @Test
    public void trivialLongTest() {

        final Long longA = new Long(42);
        final Long longB = new Long(42);
        Assert.assertNotSame(longA, longB);
        final Long longC = Long.valueOf(42);
        final Long longD = Long.valueOf(42);
        Assert.assertSame(longC, longD);

    }


    @Test
    public void testCalculateMatchHash() throws Exception {
        final long hashShort = HashUtil.calculateIpv6PrefixHash(shortIpv6);
        final long hashLong = HashUtil.calculateIpv6PrefixHash(fullIpv6);
        Assert.assertEquals(hashShort, hashLong);
    }

    @Test
    public void testCalculateMacAddressHash() {
        for (int i = 0; i < MAC_ADDRESSES.length - 1; i++) {
            final long hash = HashUtil.calculateMacAddressHash(MAC_ADDRESSES[i]);
            final long otherHash = HashUtil.calculateMacAddressHash(MAC_ADDRESSES[i + 1]);
            Assert.assertNotEquals(hash, otherHash);
        }
    }

    @Test
    public void testCalculateIpv4PrefixHash() {
        for (int i = 0; i < IPV_4_PREFIXES.length - 1; i++) {
            final Ipv4Prefix prefixA = IPV_4_PREFIXES[i];
            final Ipv4Prefix prefixB = IPV_4_PREFIXES[i + 1];
            final long hash = HashUtil.calculateIpv4PrefixHash(prefixA);
            final long hash_n = HashUtil.calculateIpv4PrefixHash(prefixB);
            LOG.info("Comparing {} vs. {} (hash {} vs. hash {})", prefixA, prefixB, hash, hash_n);
            Assert.assertNotEquals(hash, hash_n);
        }

    }

    @Test
    public void testCalculateIpv6PrefixHash() {
        for (int i = 0; i < IPV_6_PREFIXES.length - 1; i++) {
            final long hash_n = HashUtil.calculateIpv6PrefixHash(IPV_6_PREFIXES[i]);
            final long hash_n1 = HashUtil.calculateIpv6PrefixHash(IPV_6_PREFIXES[i + 1]);
            Assert.assertNotNull(hash_n);
            Assert.assertNotNull(hash_n1);
            Assert.assertNotEquals(hash_n, hash_n1);
        }
    }

    @Test
    public void testCalculateIpv6PrefixHashWithMask() {
        for (int i = 0; i < IPV_6_PREFIXES.length - 1; i++) {
            final long hash_n = HashUtil.calculateIpv6PrefixHash(IPV_6_PREFIXES_WITH_MASK[i]);
            final long hash_n1 = HashUtil.calculateIpv6PrefixHash(IPV_6_PREFIXES_WITH_MASK[i + 1]);
            Assert.assertNotNull(hash_n);
            Assert.assertNotNull(hash_n1);
            Assert.assertNotEquals(hash_n, hash_n1);
        }
    }

    @Test
    public void calculateIpMatchHash() {
        final IpMatchBuilder ipMatchBuilder = new IpMatchBuilder();
        ipMatchBuilder.setIpEcn((short) 42);
        HashUtil.calculateIpMatchHash(ipMatchBuilder.build());
    }

}