/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationQueueWrapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessageBuilder;

/**
 * @author michal.polkorab
 *
 */
public class NotificationQueueWrapperTest {

    /**
     * Test NotificationQueueWrapper creation
     */
    @Test(expected=IllegalArgumentException.class)
    public void test() {
        NotificationQueueWrapper queueWrapper = new NotificationQueueWrapper(null, null);
    }

    /**
     * Test NotificationQueueWrapper creation
     */
    @Test(expected=IllegalArgumentException.class)
    public void test2() {
        EchoRequestMessageBuilder echoBuilder = new EchoRequestMessageBuilder();
        NotificationQueueWrapper queueWrapper = new NotificationQueueWrapper(echoBuilder.build(), null);
    }

    /**
     * Test NotificationQueueWrapper creation
     */
    @Test
    public void test3() {
        EchoRequestMessageBuilder echoBuilder = new EchoRequestMessageBuilder();
        NotificationQueueWrapper queueWrapper = new NotificationQueueWrapper(echoBuilder.build(),
                (short) EncodeConstants.OF13_VERSION_ID);

        Assert.assertEquals("Wrong implemented interface", "org.opendaylight.openflowplugin.api.openflow.md"
                + ".core.NotificationQueueWrapper", queueWrapper.getImplementedInterface().getName());
        Assert.assertEquals("Wrong version", 4, queueWrapper.getVersion().intValue());
        Assert.assertEquals("Wrong notification", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol"
                + ".rev130731.EchoRequestMessage", queueWrapper.getNotification().getImplementedInterface().getName());
        Assert.assertEquals("Wrong xid", -1, queueWrapper.getXid().longValue());
        queueWrapper.setXid(12345L);
        Assert.assertEquals("Wrong xid", 12345, queueWrapper.getXid().intValue());
    }
}