/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.lang.reflect.Constructor;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class MatchConvertorUtilTest {
    
    private static Logger LOG = LoggerFactory
            .getLogger(MatchConvertorUtilTest.class);

    /**
     * Test method for {@link MatchConvertorUtil#ipv6ExthdrFlagsToInt(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags)}.
     * @throws Exception 
     */
    @Test
    public void testIpv6ExthdrFlagsToInt() throws Exception {
        Ipv6ExthdrFlags pField;
        Constructor<Ipv6ExthdrFlags> ctor = Ipv6ExthdrFlags.class.getConstructor(
                Boolean.class, Boolean.class, Boolean.class, Boolean.class, 
                Boolean.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class);
        
        int[] expectedFlagCumulants = new int[] {
                4, 8, 2, 16, 64, 1, 32, 128, 256
        };
        
        for (int i = 0; i < 9; i++) {
            pField = ctor.newInstance(createIpv6ExthdrFlagsCtorParams(i));
            int intResult = MatchConvertorUtil.ipv6ExthdrFlagsToInt(pField);
            LOG.debug("{}:Ipv6ExthdrFlags[{}] as int = {}", i, pField, intResult);
            Assert.assertEquals(expectedFlagCumulants[i], intResult);
        }
        
        pField = new Ipv6ExthdrFlags(
                false, false, false, false, false, false, false, false, false);
        Assert.assertEquals(0, MatchConvertorUtil.ipv6ExthdrFlagsToInt(pField).intValue());
        
        pField = new Ipv6ExthdrFlags(
                true, true, true, true, true, true, true, true, true);
        Assert.assertEquals(511, MatchConvertorUtil.ipv6ExthdrFlagsToInt(pField).intValue());
    }

    /**
     * @return
     */
    private static Object[] createIpv6ExthdrFlagsCtorParams(int trueIndex) {
        Boolean[] flags = new Boolean[]{false, false, false, false, false, false, false, false, false};
        flags[trueIndex] = true;
        return flags;
    }

    /**
     * Test method for {@link MatchConvertorUtil#ipv6NetmaskArrayToCIDRValue(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry)}.
     * @throws Exception
     */
    @Test
    public void testIpv6NetmaskArrayToCIDRValue() throws Exception {
        BigInteger maskSeed = new BigInteger("1ffffffffffffffffffffffffffffffff", 16);
        byte[] maskArray = new byte[16];
        LOG.debug("maskSeed= {}", ByteBufUtils.bytesToHexString(maskSeed.toByteArray()));

        for (int i = 0; i <= 128; i++) {
            System.arraycopy(maskSeed.toByteArray(), 1, maskArray, 0, 16);
            LOG.debug("maskHex[{}] = {}", i, ByteBufUtils.bytesToHexString(maskArray));
            int cidr = MatchConvertorUtil.ipv6NetmaskArrayToCIDRValue(maskArray);
            LOG.debug("cidr = {}", cidr);
            Assert.assertEquals(128-i, cidr);

            maskSeed = maskSeed.clearBit(i);
        }
    }
}
