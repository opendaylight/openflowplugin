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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.EnqueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTosCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanPcpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.StripVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.enqueue._case.EnqueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.dl.dst._case.SetDlDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.dl.src._case.SetDlSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.dst._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.src._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.tos._case.SetNwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.tp.dst._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.tp.src._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.vlan.pcp._case.SetVlanPcpActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.vlan.vid._case.SetVlanVidActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueId;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for OF10ActionsSerializer.
 *
 * @author michal.polkorab
 */
public class OF10ActionsSerializerTest {

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
     * Testing correct serialization of actions (OF v1.0).
     */
    @Test
    public void test() {
        final List<Action> actions = new ArrayList<>();
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new PortNumber(Uint32.valueOf(42)));
        outputBuilder.setMaxLength(Uint16.valueOf(32));
        caseBuilder.setOutputAction(outputBuilder.build());
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(caseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetVlanVidCaseBuilder vlanVidCaseBuilder = new SetVlanVidCaseBuilder();
        SetVlanVidActionBuilder vlanVidBuilder = new SetVlanVidActionBuilder();
        vlanVidBuilder.setVlanVid(Uint16.valueOf(15));
        vlanVidCaseBuilder.setSetVlanVidAction(vlanVidBuilder.build());
        actionBuilder.setActionChoice(vlanVidCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetVlanPcpCaseBuilder vlanPcpCaseBuilder = new SetVlanPcpCaseBuilder();
        SetVlanPcpActionBuilder vlanPcpBuilder = new SetVlanPcpActionBuilder();
        vlanPcpBuilder.setVlanPcp(Uint8.valueOf(16));
        vlanPcpCaseBuilder.setSetVlanPcpAction(vlanPcpBuilder.build());
        actionBuilder.setActionChoice(vlanPcpCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new StripVlanCaseBuilder().build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetDlSrcCaseBuilder dlSrcCaseBuilder = new SetDlSrcCaseBuilder();
        SetDlSrcActionBuilder dlSrcBuilder = new SetDlSrcActionBuilder();
        dlSrcBuilder.setDlSrcAddress(new MacAddress("00:00:00:02:03:04"));
        dlSrcCaseBuilder.setSetDlSrcAction(dlSrcBuilder.build());
        actionBuilder.setActionChoice(dlSrcCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetDlDstCaseBuilder dlDstCaseBuilder = new SetDlDstCaseBuilder();
        SetDlDstActionBuilder dlDstBuilder = new SetDlDstActionBuilder();
        dlDstBuilder.setDlDstAddress(new MacAddress("00:00:00:01:02:03"));
        dlDstCaseBuilder.setSetDlDstAction(dlDstBuilder.build());
        actionBuilder.setActionChoice(dlDstCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetNwSrcCaseBuilder nwSrcCaseBuilder = new SetNwSrcCaseBuilder();
        SetNwSrcActionBuilder nwSrcBuilder = new SetNwSrcActionBuilder();
        nwSrcBuilder.setIpAddress(new Ipv4Address("10.0.0.1"));
        nwSrcCaseBuilder.setSetNwSrcAction(nwSrcBuilder.build());
        actionBuilder.setActionChoice(nwSrcCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetNwDstCaseBuilder nwDstCaseBuilder = new SetNwDstCaseBuilder();
        SetNwDstActionBuilder nwDstBuilder = new SetNwDstActionBuilder();
        nwDstBuilder.setIpAddress(new Ipv4Address("10.0.0.3"));
        nwDstCaseBuilder.setSetNwDstAction(nwDstBuilder.build());
        actionBuilder.setActionChoice(nwDstCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetNwTosCaseBuilder tosCaseBuilder = new SetNwTosCaseBuilder();
        SetNwTosActionBuilder tosBuilder = new SetNwTosActionBuilder();
        tosBuilder.setNwTos(Uint8.valueOf(204));
        tosCaseBuilder.setSetNwTosAction(tosBuilder.build());
        actionBuilder.setActionChoice(tosCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetTpSrcCaseBuilder tpSrcCaseBuilder = new SetTpSrcCaseBuilder();
        SetTpSrcActionBuilder tpSrcBuilder = new SetTpSrcActionBuilder();
        tpSrcBuilder.setPort(new PortNumber(Uint32.valueOf(6653)));
        tpSrcCaseBuilder.setSetTpSrcAction(tpSrcBuilder.build());
        actionBuilder.setActionChoice(tpSrcCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetTpDstCaseBuilder tpDstCaseBuilder = new SetTpDstCaseBuilder();
        SetTpDstActionBuilder tpDstBuilder = new SetTpDstActionBuilder();
        tpDstBuilder.setPort(new PortNumber(Uint32.valueOf(6633)));
        tpDstCaseBuilder.setSetTpDstAction(tpDstBuilder.build());
        actionBuilder.setActionChoice(tpDstCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        EnqueueCaseBuilder enqueueCaseBuilder = new EnqueueCaseBuilder();
        EnqueueActionBuilder enqueueBuilder = new EnqueueActionBuilder();
        enqueueBuilder.setPort(new PortNumber(Uint32.valueOf(6613)));
        enqueueBuilder.setQueueId(new QueueId(Uint32.valueOf(400)));
        enqueueCaseBuilder.setEnqueueAction(enqueueBuilder.build());
        actionBuilder.setActionChoice(enqueueCaseBuilder.build());
        actions.add(actionBuilder.build());

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        ListSerializer.serializeList(actions, TypeKeyMakerFactory
                .createActionKeyMaker(EncodeConstants.OF10_VERSION_ID), registry, out);

        Assert.assertEquals("Wrong action type", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong port", 42, out.readUnsignedShort());
        Assert.assertEquals("Wrong max-length", 32, out.readUnsignedShort());
        Assert.assertEquals("Wrong action type", 1, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong vlan-vid", 15, out.readUnsignedShort());
        out.skipBytes(2);
        Assert.assertEquals("Wrong action type", 2, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong vlan-pcp", 16, out.readUnsignedByte());
        out.skipBytes(3);
        Assert.assertEquals("Wrong action type", 3, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        out.skipBytes(4);
        Assert.assertEquals("Wrong action type", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 16, out.readUnsignedShort());
        byte[] data = new byte[EncodeConstants.MAC_ADDRESS_LENGTH];
        out.readBytes(data);
        Assert.assertArrayEquals("Wrong dl-address", new byte[] { 00, 00, 00, 02, 03, 04 }, data);
        out.skipBytes(6);
        Assert.assertEquals("Wrong action type", 5, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 16, out.readUnsignedShort());
        data = new byte[EncodeConstants.MAC_ADDRESS_LENGTH];
        out.readBytes(data);
        Assert.assertArrayEquals("Wrong dl-address", new byte[] { 00, 00, 00, 01, 02, 03 }, data);
        out.skipBytes(6);
        Assert.assertEquals("Wrong action type", 6, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong ip-address(1)", 10, out.readUnsignedByte());
        Assert.assertEquals("Wrong ip-address(2)", 0, out.readUnsignedByte());
        Assert.assertEquals("Wrong ip-address(3)", 0, out.readUnsignedByte());
        Assert.assertEquals("Wrong ip-address(4)", 1, out.readUnsignedByte());
        Assert.assertEquals("Wrong action type", 7, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong ip-address(1)", 10, out.readUnsignedByte());
        Assert.assertEquals("Wrong ip-address(2)", 0, out.readUnsignedByte());
        Assert.assertEquals("Wrong ip-address(3)", 0, out.readUnsignedByte());
        Assert.assertEquals("Wrong ip-address(4)", 3, out.readUnsignedByte());
        Assert.assertEquals("Wrong action type", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong nw-tos", 204, out.readUnsignedByte());
        out.skipBytes(3);
        Assert.assertEquals("Wrong action type", 9, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong port", 6653, out.readUnsignedShort());
        out.skipBytes(2);
        Assert.assertEquals("Wrong action type", 10, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong port", 6633, out.readUnsignedShort());
        out.skipBytes(2);
        Assert.assertEquals("Wrong action type", 11, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 16, out.readUnsignedShort());
        Assert.assertEquals("Wrong port", 6613, out.readUnsignedShort());
        out.skipBytes(6);
        Assert.assertEquals("Wrong queue-id", 400, out.readUnsignedInt());
        Assert.assertTrue("Written more bytes than needed", out.readableBytes() == 0);
    }

}
