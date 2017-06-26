/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.EnqueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTosCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanPcpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanVidCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.StripVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * @author michal.polkorab
 *
 */
public class OF10ActionsDeserializerTest {

    private DeserializerRegistry registry;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        registry = new DeserializerRegistryImpl();
        registry.init();
    }

    /**
     * Testing correct deserialization of actions (OF v1.0)
     */
    @Test
    public void test() {
        ByteBuf message = BufferHelper.buildBuffer("00 00 00 08 00 10 20 00 "
                + "00 01 00 08 10 10 00 00 "
                + "00 02 00 08 25 00 00 00 "
                + "00 03 00 08 00 00 00 00 "
                + "00 04 00 10 01 02 03 04 05 06 00 00 00 00 00 00 "
                + "00 05 00 10 02 03 04 05 06 07 00 00 00 00 00 00 "
                + "00 06 00 08 0A 00 00 01 "
                + "00 07 00 08 0B 00 00 02 "
                + "00 08 00 08 01 00 00 00 "
                + "00 09 00 08 00 02 00 00 "
                + "00 0A 00 08 00 03 00 00 "
                + "00 0B 00 10 00 04 00 00 00 00 00 00 00 00 00 30");

        message.skipBytes(4); // skip XID
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF10_VERSION_ID);
        List<Action> actions = ListDeserializer.deserializeList(EncodeConstants.OF10_VERSION_ID,
                message.readableBytes(), message, keyMaker, registry);
        Assert.assertEquals("Wrong number of actions", 12, actions.size());
        Action action1 = actions.get(0);
        Assert.assertTrue("Wrong action type", action1.getActionChoice() instanceof OutputActionCase);
        Assert.assertEquals("Wrong port", 16,
                ((OutputActionCase) action1.getActionChoice()).getOutputAction().getPort().getValue().intValue());
        Assert.assertEquals("Wrong max-length", 8192,
                ((OutputActionCase) action1.getActionChoice()).getOutputAction().getMaxLength().intValue());
        Action action2 = actions.get(1);
        Assert.assertTrue("Wrong action type", action2.getActionChoice() instanceof SetVlanVidCase);
        Assert.assertEquals("Wrong vlan-vid", 4112,
                ((SetVlanVidCase) action2.getActionChoice()).getSetVlanVidAction().getVlanVid().intValue());
        Action action3 = actions.get(2);
        Assert.assertTrue("Wrong action type", action3.getActionChoice() instanceof SetVlanPcpCase);
        Assert.assertEquals("Wrong vlan-pcp", 37,
                ((SetVlanPcpCase) action3.getActionChoice()).getSetVlanPcpAction().getVlanPcp().intValue());
        Action action4 = actions.get(3);
        Assert.assertTrue("Wrong action type", action4.getActionChoice() instanceof StripVlanCase);
        Action action5 = actions.get(4);
        Assert.assertTrue("Wrong action type", action5.getActionChoice() instanceof SetDlSrcCase);
        Assert.assertEquals("Wrong dl-src", "01:02:03:04:05:06",
            ((SetDlSrcCase) action5.getActionChoice()).getSetDlSrcAction().getDlSrcAddress().getValue());
        Action action6 = actions.get(5);
        Assert.assertTrue("Wrong action type", action6.getActionChoice() instanceof SetDlDstCase);
        Assert.assertEquals("Wrong dl-dst", "02:03:04:05:06:07",
            ((SetDlDstCase) action6.getActionChoice()).getSetDlDstAction().getDlDstAddress().getValue());
        Action action7 = actions.get(6);
        Assert.assertTrue("Wrong action type", action7.getActionChoice() instanceof SetNwSrcCase);
        Assert.assertEquals("Wrong nw-src", new Ipv4Address("10.0.0.1"),
                ((SetNwSrcCase) action7.getActionChoice()).getSetNwSrcAction().getIpAddress());
        Action action8 = actions.get(7);
        Assert.assertTrue("Wrong action type", action8.getActionChoice() instanceof SetNwDstCase);
        Assert.assertEquals("Wrong nw-dst", new Ipv4Address("11.0.0.2"),
                ((SetNwDstCase) action8.getActionChoice()).getSetNwDstAction().getIpAddress());
        Action action9 = actions.get(8);
        Assert.assertTrue("Wrong action type", action9.getActionChoice() instanceof SetNwTosCase);
        Assert.assertEquals("Wrong nw-tos", 1, ((SetNwTosCase) action9.getActionChoice())
                .getSetNwTosAction().getNwTos().intValue());
        Action action10 = actions.get(9);
        Assert.assertTrue("Wrong action type", action10.getActionChoice() instanceof SetTpSrcCase);
        Assert.assertEquals("Wrong port", 2, ((SetTpSrcCase) action10.getActionChoice())
                .getSetTpSrcAction().getPort().getValue().intValue());
        Action action11 = actions.get(10);
        Assert.assertTrue("Wrong action type", action11.getActionChoice() instanceof SetTpDstCase);
        Assert.assertEquals("Wrong port", 3, ((SetTpDstCase) action11.getActionChoice())
                .getSetTpDstAction().getPort().getValue().intValue());
        Action action12 = actions.get(11);
        Assert.assertTrue("Wrong action type", action12.getActionChoice() instanceof EnqueueCase);
        Assert.assertEquals("Wrong port", 4, ((EnqueueCase) action12.getActionChoice())
                .getEnqueueAction().getPort().getValue().intValue());
        Assert.assertEquals("Wrong queue-id", 48, ((EnqueueCase) action12.getActionChoice())
                .getEnqueueAction().getQueueId().getValue().intValue());
    }

}
