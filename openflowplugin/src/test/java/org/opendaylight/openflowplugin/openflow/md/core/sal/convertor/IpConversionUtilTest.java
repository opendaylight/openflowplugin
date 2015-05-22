/*
 * Copyright (c) 2014 Brocade Communications Systems Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.math.BigInteger;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.IpConversionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.openflowjava.util.ByteBufUtils;


/**
 * @author Anton Ivanov aivanov@brocade.com
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
}
