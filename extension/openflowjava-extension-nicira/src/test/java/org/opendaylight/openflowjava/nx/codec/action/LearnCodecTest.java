/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionLearn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionLearnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.FlowModSpec;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.NxActionLearnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowMods;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowModsBuilder;

public class LearnCodecTest {

    private LearnCodec learnCodec;

    private ByteBuf buffer;
    private Action action;

    private final byte LEARN_HEADER_LEN = 32;
    private final byte NXAST_LEARN_SUBTYPE = 16;
    private static final short SRC_MASK = 0x2000;
    private static final short DST_MASK = 0x1800;
    private static final short NUM_BITS_MASK = 0x07FF;

    @Before
    public void setUp() {
        learnCodec = new LearnCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        action = createAction();
        learnCodec.serialize(action, buffer);

        Assert.assertEquals(LEARN_HEADER_LEN+14, buffer.readableBytes());
        Assert.assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        Assert.assertEquals(LEARN_HEADER_LEN+14, buffer.readUnsignedShort());
        Assert.assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        Assert.assertEquals(NXAST_LEARN_SUBTYPE, buffer.readUnsignedShort());
        Assert.assertEquals(1, buffer.readUnsignedShort());
        Assert.assertEquals(2, buffer.readUnsignedShort());
        Assert.assertEquals(3, buffer.readUnsignedShort());
        Assert.assertEquals(4, buffer.readLong());
        Assert.assertEquals(5, buffer.readUnsignedShort());
        Assert.assertEquals(6, buffer.readUnsignedByte());
        buffer.skipBytes(1);
        Assert.assertEquals(7, buffer.readUnsignedShort());
        Assert.assertEquals(8, buffer.readUnsignedShort());
        
        assertFlowMods();
    }
    
    @Test
    public void deserializeTest() {
        createBufer(buffer);
        action = learnCodec.deserialize(buffer);

        ActionLearn result = (ActionLearn) action.getActionChoice();
        Action act= createAction();
        ActionLearn expResult = (ActionLearn)act.getActionChoice();
        
        Assert.assertEquals(expResult, result);
    }

    private void assertFlowMods() {
        short header = buffer.readShort(); 
        short src = (short) ((header & SRC_MASK) >> 13);
        short dst = (short) ((header & DST_MASK) >> 11);
        short len = (short) (header & NUM_BITS_MASK);
        
        Assert.assertEquals(0, src);
        Assert.assertEquals(0, dst);
        Assert.assertEquals(48, len);
        Assert.assertEquals(9, buffer.readUnsignedInt());
        Assert.assertEquals(10, buffer.readUnsignedShort());
        Assert.assertEquals(11, buffer.readUnsignedInt());
        Assert.assertEquals(12, buffer.readUnsignedShort());
        
    }

    

    private Action createAction() {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        ActionLearnBuilder actionLearnBuilder = new ActionLearnBuilder();

        NxActionLearnBuilder nxActionLearnBuilder = new NxActionLearnBuilder();
        nxActionLearnBuilder.setIdleTimeout(1);
        nxActionLearnBuilder.setHardTimeout(2);
        nxActionLearnBuilder.setPriority(3);
        nxActionLearnBuilder.setCookie(BigInteger.valueOf(4));
        nxActionLearnBuilder.setFlags(5);
        nxActionLearnBuilder.setTableId((short)6);
        nxActionLearnBuilder.setFinIdleTimeout(7);
        nxActionLearnBuilder.setFinHardTimeout(8);
        nxActionLearnBuilder.setFlowMods(createFlowMods());
        actionLearnBuilder.setNxActionLearn(nxActionLearnBuilder.build());
        actionBuilder.setActionChoice(actionLearnBuilder.build());

        return actionBuilder.build();
    }

    private List<FlowMods> createFlowMods() {
        List<FlowMods> flowMods = new ArrayList<FlowMods>();
        FlowModsBuilder flowMod = new FlowModsBuilder();
        FlowModAddMatchFromFieldBuilder spec = new FlowModAddMatchFromFieldBuilder();
        spec.setFlowModNumBits(48);
        spec.setSrcField((long)9);
        spec.setSrcOfs(10);
        spec.setDstField((long) 11);
        spec.setDstOfs(12);
        FlowModAddMatchFromFieldCaseBuilder caseBuilder = new FlowModAddMatchFromFieldCaseBuilder();
        caseBuilder.setFlowModAddMatchFromField(spec.build());
        flowMod.setFlowModSpec(caseBuilder.build());
        flowMods.add(flowMod.build());
        return flowMods;
    }

    private void createBufer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LEARN_HEADER_LEN + 14);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(NXAST_LEARN_SUBTYPE);
        
        
        message.writeShort(1);
        message.writeShort(2);
        message.writeShort(3);
        message.writeLong(4);
        message.writeShort(5);
        message.writeByte(6);
        message.writeZero(1);
        message.writeShort(7);
        message.writeShort(8);
        
        toFlowModSpecHeader(message);
        message.writeInt(9);
        message.writeShort(10);
        message.writeInt(11);
        message.writeShort(12);
    }
    
    private void toFlowModSpecHeader(ByteBuf message) {
        short b = 0;
        short src = 0;
        short dst = 0;
        short bitNum = 48;
        b |= (src << 13);
        b |= (dst << 11);
        b |= bitNum;
        
        message.writeShort(b);
    }
}