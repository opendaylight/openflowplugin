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
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.AbstractActionDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopPbbCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPhyPortCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for ActionsDeserializer.
 *
 * @author michal.polkorab
 */
public class ActionsDeserializerTest {

    private static final Logger LOG = LoggerFactory
            .getLogger(ActionsDeserializerTest.class);
    private DeserializerRegistry registry;

    /**
     * Initializes deserializer registry and lookups correct deserializer.
     */
    @Before
    public void startUp() {
        registry = new DeserializerRegistryImpl();
        registry.init();
    }

    /**
     * Testing actions deserialization.
     */
    @Test
    public void test() {
        ByteBuf message = BufferHelper.buildBuffer("00 00 00 10 00 00 00 01 00 02 00 00 00 00 00 00 "
                + "00 0B 00 08 00 00 00 00 "
                + "00 0C 00 08 00 00 00 00 "
                + "00 0F 00 08 03 00 00 00 "
                + "00 10 00 08 00 00 00 00 "
                + "00 11 00 08 00 04 00 00 "
                + "00 12 00 08 00 00 00 00 "
                + "00 13 00 08 00 05 00 00 "
                + "00 14 00 08 00 06 00 00 "
                + "00 15 00 08 00 00 00 07 "
                + "00 16 00 08 00 00 00 08 "
                + "00 17 00 08 09 00 00 00 "
                + "00 18 00 08 00 00 00 00 "
                + "00 19 00 10 80 00 02 04 00 00 00 0B 00 00 00 00 "
                + "00 1A 00 08 00 0A 00 00 "
                + "00 1B 00 08 00 00 00 00");

        message.skipBytes(4); // skip XID
        LOG.info("bytes: {}", message.readableBytes());

        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
        List<Action> actions = ListDeserializer.deserializeList(EncodeConstants.OF13_VERSION_ID,
                message.readableBytes(), message, keyMaker, registry);
        Assert.assertTrue("Wrong action type", actions.get(0).getActionChoice() instanceof OutputActionCase);
        Assert.assertEquals("Wrong action port", 1,
                ((OutputActionCase) actions.get(0).getActionChoice()).getOutputAction()
                .getPort().getValue().intValue());
        Assert.assertEquals("Wrong action max-length", 2,
                ((OutputActionCase) actions.get(0).getActionChoice()).getOutputAction()
                .getMaxLength().intValue());
        Assert.assertTrue("Wrong action type", actions.get(1).getActionChoice() instanceof CopyTtlOutCase);
        Assert.assertTrue("Wrong action type", actions.get(2).getActionChoice() instanceof CopyTtlInCase);
        Assert.assertTrue("Wrong action type", actions.get(3).getActionChoice() instanceof SetMplsTtlCase);
        Assert.assertEquals("Wrong action value", 3,
                ((SetMplsTtlCase) actions.get(3).getActionChoice()).getSetMplsTtlAction()
                .getMplsTtl().shortValue());
        Assert.assertTrue("Wrong action type", actions.get(4).getActionChoice() instanceof DecMplsTtlCase);
        Assert.assertTrue("Wrong action type", actions.get(5).getActionChoice() instanceof PushVlanCase);
        Assert.assertEquals("Wrong action value", 4,
                ((PushVlanCase) actions.get(5).getActionChoice()).getPushVlanAction()
                .getEthertype().getValue().intValue());
        Assert.assertTrue("Wrong action type", actions.get(6).getActionChoice() instanceof PopVlanCase);
        Assert.assertTrue("Wrong action type", actions.get(7).getActionChoice() instanceof PushMplsCase);
        Assert.assertEquals("Wrong action value", 5,
                ((PushMplsCase) actions.get(7).getActionChoice()).getPushMplsAction()
                .getEthertype().getValue().intValue());
        Assert.assertTrue("Wrong action type", actions.get(8).getActionChoice() instanceof PopMplsCase);
        Assert.assertEquals("Wrong action value", 6,
                ((PopMplsCase) actions.get(8).getActionChoice()).getPopMplsAction()
                .getEthertype().getValue().intValue());
        Assert.assertTrue("Wrong action type", actions.get(9).getActionChoice() instanceof SetQueueCase);
        Assert.assertEquals("Wrong action value", 7,
                ((SetQueueCase) actions.get(9).getActionChoice()).getSetQueueAction()
                .getQueueId().intValue());
        Assert.assertTrue("Wrong action type", actions.get(10).getActionChoice() instanceof GroupCase);
        Assert.assertEquals("Wrong action value", 8,
                ((GroupCase) actions.get(10).getActionChoice()).getGroupAction().getGroupId().intValue());
        Assert.assertTrue("Wrong action type", actions.get(11).getActionChoice() instanceof SetNwTtlCase);
        Assert.assertEquals("Wrong action value", 9,
                ((SetNwTtlCase) actions.get(11).getActionChoice()).getSetNwTtlAction().getNwTtl().intValue());
        Assert.assertTrue("Wrong action type", actions.get(12).getActionChoice() instanceof DecNwTtlCase);
        Assert.assertTrue("Wrong action type", actions.get(13).getActionChoice() instanceof SetFieldCase);
        List<MatchEntry> entries = ((SetFieldCase) actions.get(13).getActionChoice())
                .getSetFieldAction().getMatchEntry();
        Assert.assertEquals("Wrong number of fields", 1, entries.size());
        Assert.assertEquals("Wrong match entry class", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow."
                + "oxm.rev150225.OpenflowBasicClass", entries.get(0).getOxmClass().getName());
        Assert.assertEquals("Wrong match entry field", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow."
                + "oxm.rev150225.InPhyPort", entries.get(0).getOxmMatchField().getName());
        Assert.assertEquals("Wrong match entry mask", false, entries.get(0).getHasMask());
        Assert.assertEquals("Wrong match entry value", 11,
                ((InPhyPortCase) entries.get(0).getMatchEntryValue()).getInPhyPort().getPortNumber()
                .getValue().intValue());
        Assert.assertTrue("Wrong action type", actions.get(14).getActionChoice() instanceof PushPbbCase);
        Assert.assertEquals("Wrong action value", 10,
                ((PushPbbCase) actions.get(14).getActionChoice()).getPushPbbAction()
                .getEthertype().getValue().intValue());
        Assert.assertTrue("Wrong action type", actions.get(15).getActionChoice() instanceof PopPbbCase);
        Assert.assertTrue("Unread data in message", message.readableBytes() == 0);
    }

    /**
     * Tests {@link AbstractActionDeserializer#deserializeHeader(ByteBuf)}.
     */
    @Test
    public void testDeserializeHeader() {
        ByteBuf message = BufferHelper.buildBuffer("00 00 00 04 00 19 00 04");

        message.skipBytes(4); // skip XID
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF13_VERSION_ID);
        List<Action> actions = ListDeserializer.deserializeHeaders(EncodeConstants.OF13_VERSION_ID,
                message.readableBytes(), message, keyMaker, registry);

        Assert.assertTrue("Wrong action type", actions.get(0).getActionChoice() instanceof OutputActionCase);
        Assert.assertNull("Wrong action port", ((OutputActionCase) actions.get(0).getActionChoice()).getOutputAction());
        Assert.assertNull("Wrong action max-length",
                ((OutputActionCase) actions.get(0).getActionChoice()).getOutputAction());
        Assert.assertTrue("Wrong action type", actions.get(1).getActionChoice() instanceof SetFieldCase);
        Assert.assertNull("Wrong action oxm field",
                ((SetFieldCase) actions.get(1).getActionChoice()).getSetFieldAction());
    }
}
