/*
 * Copyright (c) 2014 Brocade Communications Systems Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anton Ivanov aivanov@brocade.com
 * @author Sai MarapaReddy sai.marapareddy@gmail.com
 *
 */
public class IpConversionUtilTest {

    private static Logger LOG = LoggerFactory
            .getLogger(IpConversionUtilTest.class);



    /*
     * Test canonicalBinaryV6Address
     */
    @Test
    public void canonicalBinaryV6AddressTest() {

        byte [] ipv6binary = IpConversionUtil.canonicalBinaryV6Address(new Ipv6Address("0000:0000:0000:0000:0000:0000:0000:0001"));
        byte [] expected = {0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,1};

        Assert.assertTrue("Incorrect canonicalization - binary", Arrays.equals(ipv6binary, expected));
        try {
            Assert.assertEquals("Incorrect canonicalization - string", "::1", IpConversionUtil.byteArrayV6AddressToString(ipv6binary));
        } catch (java.net.UnknownHostException e) {
            Assert.assertTrue("Incorrect canonicalization - wrong length of byte[]", false);
        }
    }

    /*
     * Test canonicalBinaryV6Prefix
     */
    @Test
    public void canonicalBinaryV6AddressPrefixTest() {

        byte [] ipv6binary = IpConversionUtil.canonicalBinaryV6Prefix(new Ipv6Prefix("0000:0000:0000:0000:0000:0000:0000:0001/64"));
        byte [] expected = {0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 64};

        Assert.assertTrue("Incorrect canonicalization - binary", Arrays.equals(ipv6binary, expected));
        try {
            Assert.assertEquals("Incorrect canonicalization - string", "::/64", IpConversionUtil.byteArrayV6PrefixToString(ipv6binary));
        } catch (java.net.UnknownHostException e){
            Assert.assertTrue("Incorrect canonicalization - wrong length of byte[]", false);
        }
    }

    @Test
    public void testCountBitsAsCIDRReplacement() throws Exception {
        BigInteger maskSeed = new BigInteger("1ffffffffffffffffffffffffffffffff", 16);
        byte[] maskArray = new byte[16];
        LOG.debug("maskSeed= {}", ByteBufUtils.bytesToHexString(maskSeed.toByteArray()));

        for (int i = 0; i <= 128; i++) {
            System.arraycopy(maskSeed.toByteArray(), 1, maskArray, 0, 16);
            LOG.debug("maskHex[{}] = {}", i, ByteBufUtils.bytesToHexString(maskArray));
            int cidr = IpConversionUtil.countBits(maskArray);
            LOG.debug("cidr = {}", cidr);
            Assert.assertEquals(128 - i, cidr);

            maskSeed = maskSeed.clearBit(i);
        }
    }


    /**
     * Test method for {@link MatchConvertorUtil#getIpv4Mask(byte[])}.
     *
     * @throws Exception
     */

    @Test
    public void testcountBitsAsGetIpv4Mask() {
        byte[][] maskInputs = new byte[][]{
                {(byte) 255, (byte) 255, (byte) 255, (byte) 255},
                {(byte) 255, (byte) 255, (byte) 254, 0},
                {(byte) 128, 0, 0, 0},
                {0, 0, 0, 0},
        };

        int[] maskOutputs = new int[]{
                32, 23, 1, 0
        };

        for (int i = 0; i < maskInputs.length; i++) {
            int mask = IpConversionUtil.countBits(maskInputs[i]);
            Assert.assertEquals(maskOutputs[i], mask);
        }
    }

    @Test
    public void convertArbitraryMaskToByteArrayTest() {
        int value = 0xffffffff;
        byte[] bytes = new byte[]{
                (byte)(value >>> 24), (byte)(value >> 16 & 0xff), (byte)(value >> 8 & 0xff), (byte)(value & 0xff) };
        byte[] maskBytes;
        maskBytes = IpConversionUtil.convertArbitraryMaskToByteArray(new DottedQuad("255.255.255.255"));
        for (int i=0; i<bytes.length;i++) {
            int mask = maskBytes[i];
            Assert.assertEquals(bytes[i],mask);
        }
    }

    @Test
    public void isArbitraryBitMaskTest() {
        boolean arbitraryBitMask;
        arbitraryBitMask = IpConversionUtil.isArbitraryBitMask(new byte[] {1,1,1,1});
        Assert.assertEquals(arbitraryBitMask,true);
        arbitraryBitMask = IpConversionUtil.isArbitraryBitMask(new byte[] {-1,-1,-1,-1});
        Assert.assertEquals(arbitraryBitMask,false);
        arbitraryBitMask = IpConversionUtil.isArbitraryBitMask(new byte[] {-1,-1,0,-1});
        Assert.assertEquals(arbitraryBitMask,true);
        arbitraryBitMask = IpConversionUtil.isArbitraryBitMask(null);
        Assert.assertEquals(arbitraryBitMask,false);
    }

    @Test
    public void extractIpv4AddressTest() {
        Ipv4Address ipv4Address;
        ipv4Address = IpConversionUtil.extractIpv4Address(new Ipv4Prefix("1.0.1.0/16"));
        Assert.assertEquals(ipv4Address.getValue(),"1.0.1.0");
    }

    @Test
    public void extractIpv4AddressMaskTest() {
        DottedQuad dottedQuad;
        dottedQuad = IpConversionUtil.extractIpv4AddressMask(new Ipv4Prefix("1.1.1.1/24"));
        Assert.assertEquals(dottedQuad.getValue(),"255.255.255.0");
    }

    @Test
    public void convertipv6ArbitraryMaskToByteArrayTest() {
        byte[] bytes = {-5,-96,-1,-74,-1,-16,-1,-16, -1,-16,-1,-16,-1,-16,-91,85};
        byte[] maskBytes = IpConversionUtil.convertIpv6ArbitraryMaskToByteArray(new Ipv6ArbitraryMask("fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:A555"));
        for(int i=0; i<bytes.length;i++){
            int mask = maskBytes[i];
            Assert.assertEquals(bytes[i],mask);
        }
    }

    @Test
    public void createArbitraryBitMaskTest() {
        byte[] bytes = {-1,-1,-1,0};
        DottedQuad dottedQuad;
        dottedQuad = IpConversionUtil.createArbitraryBitMask(bytes);
        Assert.assertEquals(dottedQuad.getValue(),"255.255.255.0");
        DottedQuad dottedQuadNull;
        dottedQuadNull = IpConversionUtil.createArbitraryBitMask(null);
        Assert.assertEquals(dottedQuadNull.getValue(),"255.255.255.255");
    }

    @Test
    public void createIpv6ArbitraryBitMaskTest() {
        byte[] bytes = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        Ipv6ArbitraryMask ipv6ArbitraryMask;
        ipv6ArbitraryMask = IpConversionUtil.createIpv6ArbitraryBitMask(bytes);
        Assert.assertEquals(ipv6ArbitraryMask.getValue(),"ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
        Ipv6ArbitraryMask ipv6ArbitraryMaskNull;
        ipv6ArbitraryMaskNull = IpConversionUtil.createIpv6ArbitraryBitMask(null);
        Assert.assertEquals(ipv6ArbitraryMaskNull.getValue(),"ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
    }

    @Test
    public void extractIpv6AddressMaskTest() {
        Ipv6ArbitraryMask ipv6IpAddressMask;
        Ipv6Prefix ipv6Prefix = new Ipv6Prefix("1:2:3:4:5:6:7:8/16");
        ipv6IpAddressMask = IpConversionUtil.extractIpv6AddressMask(ipv6Prefix);
        Assert.assertEquals(ipv6IpAddressMask.getValue(),"ffff:0:0:0:0:0:0:0");
    }

    @Test
    public void isIpv6ArbitraryBitMaskTest() {
        byte[] bytes = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        boolean falseCase =  false;
        boolean trueCase =  true;
        Assert.assertEquals(falseCase,IpConversionUtil.isIpv6ArbitraryBitMask(bytes));
        byte[] bytesArbitraryMask = {-1,-1,-1,-1,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        Assert.assertEquals(trueCase,IpConversionUtil.isIpv6ArbitraryBitMask(bytesArbitraryMask));
        Assert.assertEquals(falseCase,IpConversionUtil.isIpv6ArbitraryBitMask(null));
        byte[] bytesMask = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0};
        Assert.assertEquals(falseCase,IpConversionUtil.isIpv6ArbitraryBitMask(bytesMask));
        byte[] bytesArbMask = {0,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        Assert.assertEquals(trueCase,IpConversionUtil.isIpv6ArbitraryBitMask(bytesArbMask));
    }

    @Test
    public void extractIpv6AddressTest() {
        Ipv6Address ipv6Address;
        ipv6Address = IpConversionUtil.extractIpv6Address(new Ipv6Prefix("1:2:3:4:5:6:7:8/16"));
        Assert.assertEquals(ipv6Address.getValue(),"1:2:3:4:5:6:7:8");
    }

    @Test
    public void extractIpv6PrefixTest() {
        int ipv6Address;
        ipv6Address = IpConversionUtil.extractIpv6Prefix(new Ipv6Prefix("1:2:3:4:5:6:7:8/16"));
        Assert.assertEquals(ipv6Address,16);
    }

    @Test
    public void compressedIpv6MaskFormatTest() {
        Ipv6ArbitraryMask compressedIpv6IpAddressMask;
        Ipv6ArbitraryMask ipv6IpAddressMask = new Ipv6ArbitraryMask("FFFF:0000:0000:0:0:0:1001:1000");
        compressedIpv6IpAddressMask = IpConversionUtil.compressedIpv6MaskFormat(ipv6IpAddressMask);
        Assert.assertEquals(compressedIpv6IpAddressMask.getValue(),"FFFF::1001:1000");
    }
}
