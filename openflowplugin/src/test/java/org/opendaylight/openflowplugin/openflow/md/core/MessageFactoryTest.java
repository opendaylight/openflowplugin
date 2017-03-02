/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;


import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloElementType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;

public class MessageFactoryTest {

    @Test
    public void testCreateHelloInputWoElements() {
        short highestVersion = (short) 0x04;
        long xid = 42L;
        
        HelloInput helloMsg = MessageFactory.createHelloInput(highestVersion, xid);
        Assert.assertEquals(highestVersion, helloMsg.getVersion().shortValue());
        Assert.assertEquals(xid, helloMsg.getXid().longValue());
        Assert.assertNull(helloMsg.getElements());
    }

    @Test
    public void testCreateHelloInputWithElements() {
        short highestVersion = (short) 0x04;
        long xid = 42L;
        Boolean[] expectedVersionBitmap = new Boolean[]{
                false, true, false, false, true};
        
        HelloInput helloMsg = MessageFactory.createHelloInput(highestVersion, xid, 
                ConnectionConductor.VERSION_ORDER);
        Assert.assertEquals(highestVersion, helloMsg.getVersion().shortValue());
        Assert.assertEquals(xid, helloMsg.getXid().longValue());
        Assert.assertEquals(1, helloMsg.getElements().size());
        Elements actualElement = helloMsg.getElements().get(0);
        Assert.assertEquals(HelloElementType.VERSIONBITMAP, actualElement.getType());
        Assert.assertArrayEquals(expectedVersionBitmap, actualElement.getVersionBitmap().toArray(new Boolean[0]));
    }

}
