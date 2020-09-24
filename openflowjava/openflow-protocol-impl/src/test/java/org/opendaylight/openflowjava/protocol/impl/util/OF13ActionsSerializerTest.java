/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.group._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.pop.mpls._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.mpls._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.pbb._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.vlan._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.mpls.ttl._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.ttl._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.queue._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.port._case.InPortBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for OF13ActionsSerializer.
 *
 * @author michal.polkorab
 */
public class OF13ActionsSerializerTest {

    private SerializerRegistry registry;

    /**
     * Initializes serializer table and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
    }

    /**
     * Testing correct serialization of actions.
     */
    @Test
    public void test() {
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new PortNumber(Uint32.valueOf(42)));
        outputBuilder.setMaxLength(Uint16.valueOf(52));
        caseBuilder.setOutputAction(outputBuilder.build());
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(caseBuilder.build());
        List<Action> actions = new ArrayList<>();
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new CopyTtlOutCaseBuilder().build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new CopyTtlInCaseBuilder().build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetMplsTtlCaseBuilder setMplsTtlCaseBuilder = new SetMplsTtlCaseBuilder();
        SetMplsTtlActionBuilder setMplsTtlBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlBuilder.setMplsTtl(Uint8.valueOf(4));
        setMplsTtlCaseBuilder.setSetMplsTtlAction(setMplsTtlBuilder.build());
        actionBuilder.setActionChoice(setMplsTtlCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new DecMplsTtlCaseBuilder().build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        PushVlanCaseBuilder pushVlanCaseBuilder = new PushVlanCaseBuilder();
        PushVlanActionBuilder pushVlanBuilder = new PushVlanActionBuilder();
        pushVlanBuilder.setEthertype(new EtherType(new EtherType(Uint16.valueOf(16))));
        pushVlanCaseBuilder.setPushVlanAction(pushVlanBuilder.build());
        actionBuilder.setActionChoice(pushVlanCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new PopVlanCaseBuilder().build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        PushMplsCaseBuilder pushMplsCaseBuilder = new PushMplsCaseBuilder();
        PushMplsActionBuilder pushMplsBuilder = new PushMplsActionBuilder();
        pushMplsBuilder.setEthertype(new EtherType(new EtherType(Uint16.valueOf(17))));
        pushMplsCaseBuilder.setPushMplsAction(pushMplsBuilder.build());
        actionBuilder.setActionChoice(pushMplsCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        PopMplsCaseBuilder popMplsCaseBuilder = new PopMplsCaseBuilder();
        PopMplsActionBuilder popMplsBuilder = new PopMplsActionBuilder();
        popMplsBuilder.setEthertype(new EtherType(new EtherType(Uint16.valueOf(18))));
        popMplsCaseBuilder.setPopMplsAction(popMplsBuilder.build());
        actionBuilder.setActionChoice(popMplsCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetQueueCaseBuilder setQueueCaseBuilder = new SetQueueCaseBuilder();
        SetQueueActionBuilder setQueueBuilder = new SetQueueActionBuilder();
        setQueueBuilder.setQueueId(Uint32.valueOf(1234));
        setQueueCaseBuilder.setSetQueueAction(setQueueBuilder.build());
        actionBuilder.setActionChoice(setQueueCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        GroupCaseBuilder groupCaseBuilder = new GroupCaseBuilder();
        GroupActionBuilder groupActionBuilder = new GroupActionBuilder();
        groupActionBuilder.setGroupId(Uint32.valueOf(555));
        groupCaseBuilder.setGroupAction(groupActionBuilder.build());
        actionBuilder.setActionChoice(groupCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetNwTtlCaseBuilder nwTtlCaseBuilder = new SetNwTtlCaseBuilder();
        SetNwTtlActionBuilder nwTtlBuilder = new SetNwTtlActionBuilder();
        nwTtlBuilder.setNwTtl(Uint8.valueOf(8));
        nwTtlCaseBuilder.setSetNwTtlAction(nwTtlBuilder.build());
        actionBuilder.setActionChoice(nwTtlCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new DecNwTtlCaseBuilder().build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
        matchBuilder.setOxmClass(OpenflowBasicClass.class);
        matchBuilder.setOxmMatchField(InPort.class);
        matchBuilder.setHasMask(false);
        InPortCaseBuilder inPortCaseBuilder = new InPortCaseBuilder();
        InPortBuilder inPortBuilder = new InPortBuilder();
        inPortBuilder.setPortNumber(new PortNumber(Uint32.ONE));
        inPortCaseBuilder.setInPort(inPortBuilder.build());
        matchBuilder.setMatchEntryValue(inPortCaseBuilder.build());
        List<MatchEntry> entries = new ArrayList<>();
        entries.add(matchBuilder.build());
        SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();
        setFieldBuilder.setMatchEntry(entries);
        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
        actionBuilder.setActionChoice(setFieldCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        PushPbbCaseBuilder pushPbbCaseBuilder = new PushPbbCaseBuilder();
        PushPbbActionBuilder pushPbbBuilder = new PushPbbActionBuilder();
        pushPbbBuilder.setEthertype(new EtherType(new EtherType(Uint16.valueOf(19))));
        pushPbbCaseBuilder.setPushPbbAction(pushPbbBuilder.build());
        actionBuilder.setActionChoice(pushPbbCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new PopPbbCaseBuilder().build());
        actions.add(actionBuilder.build());

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        ListSerializer.serializeList(actions, TypeKeyMakerFactory
                .createActionKeyMaker(EncodeConstants.OF13_VERSION_ID), registry, out);

        Assert.assertEquals("Wrong action type", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 16, out.readUnsignedShort());
        Assert.assertEquals("Wrong action port", 42, out.readUnsignedInt());
        Assert.assertEquals("Wrong action max-length", 52, out.readUnsignedShort());
        out.skipBytes(6);
        Assert.assertEquals("Wrong action type", 11, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong action type", 12, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong action type", 15, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong action mpls-ttl", 4, out.readUnsignedByte());
        out.skipBytes(3);
        Assert.assertEquals("Wrong action type", 16, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong action type", 17, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong action ethertype", 16, out.readUnsignedShort());
        out.skipBytes(2);
        Assert.assertEquals("Wrong action type", 18, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong action type", 19, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong action ethertype", 17, out.readUnsignedShort());
        out.skipBytes(2);
        Assert.assertEquals("Wrong action type", 20, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong action ethertype", 18, out.readUnsignedShort());
        out.skipBytes(2);
        Assert.assertEquals("Wrong action type", 21, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong action queue-id", 1234, out.readUnsignedInt());
        Assert.assertEquals("Wrong action type", 22, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong action group", 555, out.readUnsignedInt());
        Assert.assertEquals("Wrong action type", 23, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong action nw-ttl", 8, out.readUnsignedByte());
        out.skipBytes(3);
        Assert.assertEquals("Wrong action type", 24, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong action type", 25, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 16, out.readUnsignedShort());
        Assert.assertEquals("Wrong match entry class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong match entry field & mask", 0, out.readUnsignedByte());
        Assert.assertEquals("Wrong match entry length", 4, out.readUnsignedByte());
        Assert.assertEquals("Wrong match entry value", 1, out.readUnsignedInt());
        out.skipBytes(4);
        Assert.assertEquals("Wrong action type", 26, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong action ethertype", 19, out.readUnsignedShort());
        out.skipBytes(2);
        Assert.assertEquals("Wrong action type", 27, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertTrue("Unread data", out.readableBytes() == 0);
    }

    /**
     * Testing correct serialization of actions.
     */
    @Test
    public void testHeaders() {
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new PortNumber(Uint32.valueOf(42)));
        outputBuilder.setMaxLength(Uint16.valueOf(52));
        caseBuilder.setOutputAction(outputBuilder.build());
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(caseBuilder.build());
        List<Action> actions = new ArrayList<>();
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
        matchBuilder.setOxmClass(OpenflowBasicClass.class);
        matchBuilder.setOxmMatchField(InPort.class);
        matchBuilder.setHasMask(false);
        InPortCaseBuilder inPortCaseBuilder = new InPortCaseBuilder();
        InPortBuilder inPortBuilder = new InPortBuilder();
        inPortBuilder.setPortNumber(new PortNumber(Uint32.ONE));
        inPortCaseBuilder.setInPort(inPortBuilder.build());
        matchBuilder.setMatchEntryValue(inPortCaseBuilder.build());
        List<MatchEntry> entries = new ArrayList<>();
        entries.add(matchBuilder.build());
        SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();
        setFieldBuilder.setMatchEntry(entries);
        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
        actionBuilder.setActionChoice(setFieldCaseBuilder.build());
        actions.add(actionBuilder.build());

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        ListSerializer.serializeHeaderList(actions, TypeKeyMakerFactory
                .createActionKeyMaker(EncodeConstants.OF13_VERSION_ID), registry, out);

        Assert.assertEquals("Wrong action type", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong action type", 25, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 4, out.readUnsignedShort());
        Assert.assertTrue("Unread data", out.readableBytes() == 0);
    }
}
