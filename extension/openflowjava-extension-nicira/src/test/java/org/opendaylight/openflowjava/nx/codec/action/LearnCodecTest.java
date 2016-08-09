/*
 * Copyright (c) 2016 Hewlett-Packard Enterprise and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromValueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyFieldIntoFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModCopyValueIntoFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModOutputToPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.value._case.FlowModAddMatchFromValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.field.into.field._case.FlowModCopyFieldIntoFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.copy.value.into.field._case.FlowModCopyValueIntoFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.output.to.port._case.FlowModOutputToPortBuilder;
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

        Assert.assertEquals(LEARN_HEADER_LEN+56, buffer.readableBytes());
        Assert.assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        Assert.assertEquals(LEARN_HEADER_LEN+56, buffer.readUnsignedShort());
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
        buffer.skipBytes(2);
        Assert.assertEquals(9, buffer.readUnsignedInt());
        Assert.assertEquals(10, buffer.readUnsignedShort());
        Assert.assertEquals(11, buffer.readUnsignedInt());
        Assert.assertEquals(12, buffer.readUnsignedShort());
        buffer.skipBytes(2);
        Assert.assertEquals(9, buffer.readUnsignedShort());
        Assert.assertEquals(10, buffer.readUnsignedInt());
        Assert.assertEquals(11, buffer.readUnsignedShort());
        buffer.skipBytes(2);
        Assert.assertEquals(9, buffer.readUnsignedShort());
        Assert.assertEquals(10, buffer.readUnsignedInt());
        Assert.assertEquals(11, buffer.readUnsignedShort());
        buffer.skipBytes(2);
        Assert.assertEquals(9, buffer.readUnsignedInt());
        Assert.assertEquals(10, buffer.readUnsignedShort());
        Assert.assertEquals(0, buffer.readableBytes());
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
        //length = 14
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

        //length = 14
        FlowModsBuilder flowMod2 = new FlowModsBuilder();
        FlowModCopyFieldIntoFieldBuilder spec2 = new FlowModCopyFieldIntoFieldBuilder();
        spec2.setFlowModNumBits(48);
        spec2.setSrcField((long)9);
        spec2.setSrcOfs(10);
        spec2.setDstField((long) 11);
        spec2.setDstOfs(12);
        FlowModCopyFieldIntoFieldCaseBuilder caseBuilder2 = new FlowModCopyFieldIntoFieldCaseBuilder();
        caseBuilder2.setFlowModCopyFieldIntoField(spec2.build());
        flowMod2.setFlowModSpec(caseBuilder2.build());
        flowMods.add(flowMod2.build());

        //length = 10
        FlowModsBuilder flowMod3 = new FlowModsBuilder();
        FlowModCopyValueIntoFieldBuilder spec3 = new FlowModCopyValueIntoFieldBuilder();
        spec3.setFlowModNumBits(48);
        spec3.setValue(9);
        spec3.setDstField((long) 10);
        spec3.setDstOfs(11);
        FlowModCopyValueIntoFieldCaseBuilder caseBuilder3 = new FlowModCopyValueIntoFieldCaseBuilder();
        caseBuilder3.setFlowModCopyValueIntoField(spec3.build());
        flowMod3.setFlowModSpec(caseBuilder3.build());
        flowMods.add(flowMod3.build());

        //length = 10
        FlowModsBuilder flowMod4 = new FlowModsBuilder();
        FlowModAddMatchFromValueBuilder spec4 = new FlowModAddMatchFromValueBuilder();
        spec4.setFlowModNumBits(48);
        spec4.setValue(9);
        spec4.setSrcField((long) 10);
        spec4.setSrcOfs(11);
        FlowModAddMatchFromValueCaseBuilder caseBuilder4 = new FlowModAddMatchFromValueCaseBuilder();
        caseBuilder4.setFlowModAddMatchFromValue(spec4.build());
        flowMod4.setFlowModSpec(caseBuilder4.build());
        flowMods.add(flowMod4.build());

        //length = 8
        FlowModsBuilder flowMod5 = new FlowModsBuilder();
        FlowModOutputToPortBuilder spec5 = new FlowModOutputToPortBuilder();
        spec5.setFlowModNumBits(48);
        spec5.setSrcField((long) 9);
        spec5.setSrcOfs(10);
        FlowModOutputToPortCaseBuilder caseBuilder5 = new FlowModOutputToPortCaseBuilder();
        caseBuilder5.setFlowModOutputToPort(spec5.build());
        flowMod5.setFlowModSpec(caseBuilder5.build());
        flowMods.add(flowMod5.build());

        return flowMods;
    }

    private void createBufer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LEARN_HEADER_LEN + 56);
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

        toFlowModSpecHeader(message, 0, 0);
        message.writeInt(9);
        message.writeShort(10);
        message.writeInt(11);
        message.writeShort(12);

        toFlowModSpecHeader(message, 0, 1);
        message.writeInt(9);
        message.writeShort(10);
        message.writeInt(11);
        message.writeShort(12);

        toFlowModSpecHeader(message, 1, 1);
        message.writeShort(9);
        message.writeInt(10);
        message.writeShort(11);

        toFlowModSpecHeader(message, 1, 0);
        message.writeShort(9);
        message.writeInt(10);
        message.writeShort(11);

        toFlowModSpecHeader(message, 0, 2);
        message.writeInt(9);
        message.writeShort(10);
    }

    private void toFlowModSpecHeader(ByteBuf message, int src, int dst) {
        short b = 0;
        short bitNum = 48;
        b |= (src << 13);
        b |= (dst << 11);
        b |= bitNum;

        message.writeShort(b);
    }
}