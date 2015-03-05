/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.legacy.sal.compatibility.test;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.legacy.sal.compatibility.ToSalConversionsUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;

/**
 * test of {@link ToSalConversionsUtils}
 */
public class ToSalConversionsUtilsTest {

    /**
     * Test method for {@link org.opendaylight.openflowplugin.legacy.sal.compatibility.ToSalConversionsUtils#tosToNwDscp(int)}.
     */
    @Test
    public void testTosToNwDscp() {
        Assert.assertEquals(0, ToSalConversionsUtils.tosToNwDscp(0));
        Assert.assertEquals(0, ToSalConversionsUtils.tosToNwDscp(1));
        Assert.assertEquals(1, ToSalConversionsUtils.tosToNwDscp(4));
        Assert.assertEquals(63, ToSalConversionsUtils.tosToNwDscp(252));
        Assert.assertEquals(63, ToSalConversionsUtils.tosToNwDscp(253));
        Assert.assertEquals(-1, ToSalConversionsUtils.tosToNwDscp(-1));
    }
    
    @Test
    public void testBytesFrom() {
        byte[] macAddInBytes2 = new byte[6];
        macAddInBytes2[0] = Integer.decode("0x" + "48").byteValue();
        macAddInBytes2[1] = Integer.decode("0x" + "2C").byteValue();
        macAddInBytes2[2] = Integer.decode("0x" + "6A").byteValue();
        macAddInBytes2[3] = Integer.decode("0x" + "1E").byteValue();
        macAddInBytes2[4] = Integer.decode("0x" + "59").byteValue();
        macAddInBytes2[5] = Integer.decode("0x" + "3D").byteValue();
        
        Assert.assertArrayEquals(macAddInBytes2, ToSalConversionsUtils
                                    .bytesFrom(new MacAddress("48:2C:6A:1E:59:3D")));
    }
}
