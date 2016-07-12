/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;

/**
 * @author sai.marapareddy@gmail.com (arbitrary masks)
 */

/**
 * test of {@link MatchComparatorHelper}
 */
public class MatchComparatorHelperTest {

    /**
     * mask for /32
     */
    private static final int DEFAULT_IPV4_MASK = 0xffffffff;

    /**
     * mask for /30
     */
    private static final int IPV4_30_MASK = 0xfffffffc;
    private static final int IP_ADDRESS = 0xC0A80101;

    /**
     * The test of conversion valid IP addres without mask to binary form.
     */
    @Test
    public void validIpWithoutMaskTest() {
        IntegerIpAddress intIp = MatchComparatorHelper.strIpToIntIp("192.168.1.1");
        assertEquals(IP_ADDRESS, intIp.getIp());
        assertEquals(DEFAULT_IPV4_MASK, intIp.getMask());
    }

    /**
     * The test of conversion of valid IP address with valid mask to binary form.
     */
    @Test
    public void validIpWithValidMaskTest() {
        IntegerIpAddress intIp = MatchComparatorHelper.strIpToIntIp("192.168.1.1/30");
        assertEquals(IP_ADDRESS, intIp.getIp());
        assertEquals(IPV4_30_MASK, intIp.getMask());
    }

    /**
     * The test of conversion of valid IP address invalid mask to binary form.
     */
    @Test
    public void validIpWithInvalidMaskTest() {
        try {
            MatchComparatorHelper.strIpToIntIp("192.168.1.1/40");
        } catch (IllegalStateException e) {
            assertEquals("Valid values for mask are from range 0 - 32. Value 40 is invalid.", e.getMessage());
            return;
        }
        fail("IllegalStateException was awaited (40 subnet is invalid)");
    }

    /**
     * The test of conversion invalid IP address with valid mask to binary form.
     */
    @Test
    public void invalidIpWithValidMaskTest() {
        try {
            MatchComparatorHelper.strIpToIntIp("257.168.1.1/25");
        } catch (IllegalArgumentException e) {
            assertEquals("'257.168.1.1' is not an IP string literal.", e.getMessage());
        }
    }

    @Test
    public void ethernetMatchEqualsTest() {
        final EthernetMatchBuilder statsEthernetBuilder = new EthernetMatchBuilder();
        final EthernetMatchBuilder storedEthernetBuilder = new EthernetMatchBuilder();

        assertEquals(true, MatchComparatorHelper.ethernetMatchEquals(null, null));

        statsEthernetBuilder.setEthernetSource(new EthernetSourceBuilder().setAddress(
                new MacAddress("11:22:33:44:55:66")).build());
        storedEthernetBuilder.setEthernetSource(new EthernetSourceBuilder().setAddress(
                new MacAddress("11:22:33:44:55:77")).build());
        assertEquals(false,
                MatchComparatorHelper.ethernetMatchEquals(statsEthernetBuilder.build(), storedEthernetBuilder.build()));

        storedEthernetBuilder.setEthernetSource(new EthernetSourceBuilder().setAddress(
                new MacAddress("11:22:33:44:55:66")).build());
        statsEthernetBuilder.setEthernetDestination(new EthernetDestinationBuilder().setAddress(
                new MacAddress("66:55:44:33:22:11")).build());
        storedEthernetBuilder.setEthernetDestination(new EthernetDestinationBuilder().setAddress(
                new MacAddress("77:55:44:33:22:11")).build());
        assertEquals(false,
                MatchComparatorHelper.ethernetMatchEquals(statsEthernetBuilder.build(), storedEthernetBuilder.build()));

        storedEthernetBuilder.setEthernetDestination(new EthernetDestinationBuilder().setAddress(
                new MacAddress("66:55:44:33:22:11")).build());
        statsEthernetBuilder.setEthernetType(new EthernetTypeBuilder().setType(new EtherType((long) 1)).build());
        storedEthernetBuilder.setEthernetType(new EthernetTypeBuilder().setType(new EtherType((long) 1)).build());
        assertEquals(true,
                MatchComparatorHelper.ethernetMatchEquals(statsEthernetBuilder.build(), storedEthernetBuilder.build()));

        statsEthernetBuilder.setEthernetType(null).build();
        assertEquals(false,
                MatchComparatorHelper.ethernetMatchEquals(statsEthernetBuilder.build(), storedEthernetBuilder.build()));

        storedEthernetBuilder.setEthernetType(null).build();
        assertEquals(true,
                MatchComparatorHelper.ethernetMatchEquals(statsEthernetBuilder.build(), storedEthernetBuilder.build()));

    }

    @Test
    public void ethernetMatchFieldsEqualsTest() {
        final EthernetSourceBuilder statsBuilder = new EthernetSourceBuilder();
        final EthernetSourceBuilder storedBuilder = new EthernetSourceBuilder();

        assertEquals(true, MatchComparatorHelper.ethernetMatchFieldsEquals(null, null));

        statsBuilder.setAddress(new MacAddress("11:22:33:44:55:66"));
        storedBuilder.setAddress(new MacAddress("11:22:33:44:55:77"));
        assertEquals(false,
                MatchComparatorHelper.ethernetMatchFieldsEquals(statsBuilder.build(), storedBuilder.build()));

        storedBuilder.setAddress(new MacAddress("11:22:33:44:55:66"));
        assertEquals(true, MatchComparatorHelper.ethernetMatchFieldsEquals(statsBuilder.build(), storedBuilder.build()));
    }

    @Test
    public void macAddressEqualsTest() {
        assertEquals(true, MatchComparatorHelper.macAddressEquals(null, null));
        assertEquals(true, MatchComparatorHelper.macAddressEquals(new MacAddress("11:22:33:44:55:66"), new MacAddress(
                "11:22:33:44:55:66")));
        assertEquals(false, MatchComparatorHelper.macAddressEquals(new MacAddress("11:22:33:44:55:66"), new MacAddress(
                "11:22:33:44:55:77")));
    }

    @Test
    public void checkNullValuesTest() {
        assertEquals(false, MatchComparatorHelper.checkNullValues(null, ""));
        assertEquals(false, MatchComparatorHelper.checkNullValues("", null));
        assertEquals(true, MatchComparatorHelper.checkNullValues(null, null));
        assertTrue(MatchComparatorHelper.checkNullValues("", "") == null);
    }

    @Test
    public void compareIpv4PrefixNullSafeTest() {
        assertEquals(true, MatchComparatorHelper.compareIpv4PrefixNullSafe(null, null));
        assertEquals(true, MatchComparatorHelper.compareIpv4PrefixNullSafe(new Ipv4Prefix("192.168.1.1/31"),
                new Ipv4Prefix("192.168.1.1/31")));

        assertEquals(false, MatchComparatorHelper.compareIpv4PrefixNullSafe(new Ipv4Prefix("192.168.1.1/31"),
                new Ipv4Prefix("191.168.1.1/31")));
    }

    @Test
    public void compareStringNullSafeTest() {
        assertEquals(true, MatchComparatorHelper.compareStringNullSafe(null,null));
        assertEquals(true, MatchComparatorHelper.compareStringNullSafe("Hello", "Hello"));
        assertEquals(false, MatchComparatorHelper.compareStringNullSafe("Hello", "hello"));
    }

    private static final int ip_192_168_1_1 = 0xC0A80101;
    private static final int ip_192_168_1_4 = 0xC0A80104;

    @Test
    public void ipBasedMatchTest() {
        // are equals because only IP address is compared
        assertEquals(true, MatchComparatorHelper.ipBasedMatch(new IntegerIpAddress(ip_192_168_1_1, 32),
                new IntegerIpAddress(ip_192_168_1_1, 16)));
    }

    @Test
    public void ipAndMaskBasedMatchTest() {
        // true because both cases are network 192.168.1.0
        assertEquals(true, MatchComparatorHelper.ipBasedMatch(new IntegerIpAddress(ip_192_168_1_1, 31),
                new IntegerIpAddress(ip_192_168_1_1, 30)));

        // false because first is network 192.168.1.0 and second is 192.168.1.4
        assertEquals(false, MatchComparatorHelper.ipBasedMatch(new IntegerIpAddress(ip_192_168_1_1, 31),
                new IntegerIpAddress(ip_192_168_1_4, 30)));
    }

    @Test
    public void layer3MatchEqualsTest() {
        final Ipv4MatchBuilder statsBuilder = new Ipv4MatchBuilder();
        final Ipv4MatchBuilder storedBuilder = new Ipv4MatchBuilder();
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        statsBuilder.setIpv4Destination(new Ipv4Prefix("192.168.1.1/30"));
        storedBuilder.setIpv4Destination(new Ipv4Prefix("191.168.1.1/30"));
        assertEquals(false, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(null, null));
        assertEquals(true,
                MatchComparatorHelper.layer3MatchEquals(new ArpMatchBuilder().build(), new ArpMatchBuilder().build()));
    }

    @Test
    public void layer3MatchEqualsIpv6Test() {
        final Ipv6MatchBuilder statsBuilder = new Ipv6MatchBuilder();
        final Ipv6MatchBuilder storedBuilder = new Ipv6MatchBuilder();
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));

        statsBuilder.setIpv6Destination(new Ipv6Prefix("AABB:1234:2ACF:000D:0000:0000:0000:5D99/64"));
        storedBuilder.setIpv6Destination(new Ipv6Prefix("AABB:1234:2ACF:000D:0000:0000:0000:4D99/64"));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));

        statsBuilder.setIpv6Destination(new Ipv6Prefix("aabb:1234:2acf:000d:0000:0000:0000:5d99/64"));
        storedBuilder.setIpv6Destination(new Ipv6Prefix("AABB:1234:2ACF:000D:0000:0000:0000:4D99/64"));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));

        statsBuilder.setIpv6Destination(new Ipv6Prefix("AABB:1234:2ACF:000C:0000:0000:0000:5D99/64"));
        storedBuilder.setIpv6Destination(new Ipv6Prefix("AABB:1234:2ACF:000D:0000:0000:0000:4D99/64"));
        assertEquals(false, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));

        statsBuilder.setIpv6Destination(new Ipv6Prefix("AABB:1234:2ACF:000C:0000:0000:0000:5D99/63"));
        storedBuilder.setIpv6Destination(new Ipv6Prefix("AABB:1234:2ACF:000D:0000:0000:0000:4D99/63"));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));

        statsBuilder.setIpv6Destination(new Ipv6Prefix("AABB:1234:2ACF:000D:0000:0000:0000:5D99/63"));
        storedBuilder.setIpv6Destination(new Ipv6Prefix("AABB:1234:2ACF:000E:0000:0000:0000:4D99/63"));
        assertEquals(false, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
    }

    @Test
    public void layer3MatchEqualsIpv4ArbitraryMaskTest(){
        final Ipv4MatchBuilder statsBuilder = new Ipv4MatchBuilder();
        final Ipv4MatchArbitraryBitMaskBuilder storedBuilder = new Ipv4MatchArbitraryBitMaskBuilder();
        assertEquals(true,MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(),storedBuilder.build()));
        statsBuilder.setIpv4Destination(new Ipv4Prefix("192.168.1.1/24"));
        storedBuilder.setIpv4DestinationAddressNoMask(new Ipv4Address("192.168.1.1"));
        storedBuilder.setIpv4DestinationArbitraryBitmask(new DottedQuad("255.255.255.0"));
        statsBuilder.setIpv4Source(new Ipv4Prefix("192.168.1.1/24"));
        storedBuilder.setIpv4SourceAddressNoMask(new Ipv4Address("192.168.1.1"));
        storedBuilder.setIpv4SourceArbitraryBitmask(new DottedQuad("255.255.255.0"));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(null, null));

    }

    @Test
    public void layer3MatchEqualsIpv4ArbitraryMaskRandomTest() {
        final Ipv4MatchArbitraryBitMaskBuilder statsBuilder = new Ipv4MatchArbitraryBitMaskBuilder();
        final Ipv4MatchArbitraryBitMaskBuilder storedBuilder = new Ipv4MatchArbitraryBitMaskBuilder();
        assertEquals(true,MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(),storedBuilder.build()));
        statsBuilder.setIpv4DestinationAddressNoMask(new Ipv4Address("192.168.0.1"));
        statsBuilder.setIpv4DestinationArbitraryBitmask(new DottedQuad("255.255.0.255"));
        storedBuilder.setIpv4DestinationAddressNoMask(new Ipv4Address("192.168.1.1"));
        storedBuilder.setIpv4DestinationArbitraryBitmask(new DottedQuad("255.255.0.255"));
        statsBuilder.setIpv4SourceAddressNoMask(new Ipv4Address("192.0.0.1"));
        statsBuilder.setIpv4SourceArbitraryBitmask(new DottedQuad("255.0.0.255"));
        storedBuilder.setIpv4SourceAddressNoMask(new Ipv4Address("192.7.1.1"));
        storedBuilder.setIpv4SourceArbitraryBitmask(new DottedQuad("255.0.0.255"));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(null, null));
    }

    @Test
    public void layer3MatchEqualsIpv4ArbitraryMaskEqualsNullTest() {
        final Ipv4MatchBuilder statsBuilder = new Ipv4MatchBuilder();
        final Ipv4MatchArbitraryBitMaskBuilder storedBuilder = new Ipv4MatchArbitraryBitMaskBuilder();
        assertEquals(true,MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(),storedBuilder.build()));
        statsBuilder.setIpv4Source(new Ipv4Prefix("192.168.0.1/32"));
        storedBuilder.setIpv4DestinationAddressNoMask(new Ipv4Address("192.168.0.1"));
        statsBuilder.setIpv4Destination(new Ipv4Prefix("192.1.0.0/32"));
        storedBuilder.setIpv4SourceAddressNoMask(new Ipv4Address("192.1.0.0"));
        assertEquals(false, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(null, null));
    }

    @Test
    public void layer3MatchEqualsIpv4ArbitraryEmptyBitMaskTest(){
        final Ipv4MatchBuilder statsBuilder = new Ipv4MatchBuilder();
        final Ipv4MatchArbitraryBitMaskBuilder storedBuilder = new Ipv4MatchArbitraryBitMaskBuilder();
        assertEquals(true,MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(),storedBuilder.build()));
        statsBuilder.setIpv4Destination(new Ipv4Prefix("192.168.1.1/32"));
        storedBuilder.setIpv4DestinationAddressNoMask(new Ipv4Address("192.168.1.1"));
        statsBuilder.setIpv4Source(new Ipv4Prefix("192.168.1.1/32"));
        storedBuilder.setIpv4SourceAddressNoMask(new Ipv4Address("192.168.1.1"));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(null, null));
    }

    @Test
    public void extractIpv4AddressTest() {
        Ipv4Address ipAddress = new Ipv4Address("1.1.1.1");
        DottedQuad netMask = new DottedQuad("255.255.255.0");
        String extractedIpAddress;
        extractedIpAddress = MatchComparatorHelper.normalizeIpv4Address(ipAddress,netMask);
        assertEquals(extractedIpAddress,"1.1.1.0");
    }

    @Test
    public void convertArbitraryMaskToByteArrayTest() {
        int value = 0xffffffff;
        byte[] bytes = new byte[]{
                (byte)(value >>> 24), (byte)(value >> 16 & 0xff), (byte)(value >> 8 & 0xff), (byte)(value & 0xff) };
        byte[] maskBytes;
        maskBytes = MatchComparatorHelper.convertArbitraryMaskToByteArray(new DottedQuad("255.255.255.255"));
        for (int i=0; i<bytes.length;i++) {
            int mask = maskBytes[i];
            assertEquals(bytes[i],mask);
        }
    }

    @Test
    public void isArbitraryBitMaskTest() {
        boolean arbitraryBitMask;
        arbitraryBitMask = MatchComparatorHelper.isArbitraryBitMask(new byte[] {1,1,1,1});
        assertEquals(arbitraryBitMask,true);
        arbitraryBitMask = MatchComparatorHelper.isArbitraryBitMask(new byte[] {-1,-1,-1,-1});
        assertEquals(arbitraryBitMask,false);
        arbitraryBitMask = MatchComparatorHelper.isArbitraryBitMask(new byte[] {-1,-1,0,-1});
        assertEquals(arbitraryBitMask,true);
        arbitraryBitMask = MatchComparatorHelper.isArbitraryBitMask(null);
        assertEquals(arbitraryBitMask,false);
    }

    @Test
    public void createPrefixTest() {
        Ipv4Address ipv4Address = new Ipv4Address("1.1.1.1");
        byte [] byteMask = new byte[] {-1,-1,-1,-1};
        Ipv4Prefix ipv4Prefix = MatchComparatorHelper.createPrefix(ipv4Address,byteMask);
        assertEquals(ipv4Prefix,new Ipv4Prefix("1.1.1.1/32"));
        String nullMask = "";
        Ipv4Prefix ipv4PrefixNullMask = MatchComparatorHelper.createPrefix(ipv4Address,nullMask);
        assertEquals(ipv4PrefixNullMask,new Ipv4Prefix("1.1.1.1/32"));
        Ipv4Prefix ipv4PrefixNoMask = MatchComparatorHelper.createPrefix(ipv4Address);
        assertEquals(ipv4PrefixNoMask,new Ipv4Prefix("1.1.1.1/32"));
    }

    @Test
    public void layer3MatchEqualsIpv6ArbitraryMaskTest(){
        final Ipv6MatchBuilder statsBuilder = new Ipv6MatchBuilder();
        final Ipv6MatchArbitraryBitMaskBuilder storedBuilder = new Ipv6MatchArbitraryBitMaskBuilder();
        assertEquals(true,MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(),storedBuilder.build()));
        statsBuilder.setIpv6Destination(new Ipv6Prefix("1:2:3:4:5:6:7:8/16"));
        storedBuilder.setIpv6DestinationAddressNoMask(new Ipv6Address("1:2:3:4:5:6:7:8"));
        storedBuilder.setIpv6DestinationArbitraryBitmask(new Ipv6ArbitraryMask("FFFF:0000:0000:0000:0000:0000:0000:0000"));
        statsBuilder.setIpv6Source(new Ipv6Prefix("1:2:3:4:5:6:7:8/32"));
        storedBuilder.setIpv6SourceAddressNoMask(new Ipv6Address("1:2:3:4:5:6:7:8"));
        storedBuilder.setIpv6SourceArbitraryBitmask(new Ipv6ArbitraryMask("FFFF:FFFF::"));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(null, null));
    }


    @Test
    public void layer3MatchEqualsIpv6ArbitraryMaskRandomTest() {
        final Ipv6MatchArbitraryBitMaskBuilder statsBuilder = new Ipv6MatchArbitraryBitMaskBuilder();
        final Ipv6MatchArbitraryBitMaskBuilder storedBuilder = new Ipv6MatchArbitraryBitMaskBuilder();
        assertEquals(true,MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(),storedBuilder.build()));
        statsBuilder.setIpv6DestinationAddressNoMask(new Ipv6Address("1::8"));
        statsBuilder.setIpv6DestinationArbitraryBitmask(new Ipv6ArbitraryMask("FFFF::FFFF"));
        storedBuilder.setIpv6DestinationAddressNoMask(new Ipv6Address("1:92:93:94:95:96:97:8"));
        storedBuilder.setIpv6DestinationArbitraryBitmask(new Ipv6ArbitraryMask("FFFF::FFFF"));
        statsBuilder.setIpv6SourceAddressNoMask(new Ipv6Address("1::8"));
        statsBuilder.setIpv6SourceArbitraryBitmask(new Ipv6ArbitraryMask("FFFF::FFFF"));
        storedBuilder.setIpv6SourceAddressNoMask(new Ipv6Address("1:92:93:94:95:96:97:8"));
        storedBuilder.setIpv6SourceArbitraryBitmask(new Ipv6ArbitraryMask("FFFF::FFFF"));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(null, null));
    }

    @Test
    public void layer3MatchEqualsIpv6ArbitraryMaskEqualsNullTest() {
        final Ipv6MatchBuilder statsBuilder = new Ipv6MatchBuilder();
        final Ipv6MatchArbitraryBitMaskBuilder storedBuilder = new Ipv6MatchArbitraryBitMaskBuilder();
        assertEquals(true,MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(),storedBuilder.build()));
        statsBuilder.setIpv6Source(new Ipv6Prefix("1:2:3:4:5:6:7:8/128"));
        storedBuilder.setIpv6DestinationAddressNoMask(new Ipv6Address("1:2:3:4:5:6:7:8"));
        statsBuilder.setIpv6Destination(new Ipv6Prefix("1:2:3:4:5:6::/128"));
        storedBuilder.setIpv6SourceAddressNoMask(new Ipv6Address("1:2:3:4:5:6::"));
        assertEquals(false, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(null, null));
    }

    @Test
    public void layer3MatchEqualsIpv6ArbitraryEmptyBitMaskTest(){
        final Ipv6MatchBuilder statsBuilder = new Ipv6MatchBuilder();
        final Ipv6MatchArbitraryBitMaskBuilder storedBuilder = new Ipv6MatchArbitraryBitMaskBuilder();
        assertEquals(true,MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(),storedBuilder.build()));
        statsBuilder.setIpv6Destination(new Ipv6Prefix("1:2:3:4:5:6:7:8/128"));
        storedBuilder.setIpv6DestinationAddressNoMask(new Ipv6Address("1:2:3:4:5:6:7:8"));
        statsBuilder.setIpv6Source(new Ipv6Prefix("1:2:3:4:5:6::/128"));
        storedBuilder.setIpv6SourceAddressNoMask(new Ipv6Address("1:2:3:4:5:6::"));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(null, null));
    }
}
