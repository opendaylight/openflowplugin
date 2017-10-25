/**
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.NxActionConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.nx.action.conntrack.CtActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.nx.action.conntrack.CtActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.NxActionCtMarkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.NxActionCtMarkCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.NxActionNatCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.NxActionNatCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.ct.mark._case.NxActionCtMark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.ct.mark._case.NxActionCtMarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.nat._case.NxActionNat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.nat._case.NxActionNatBuilder;

public class ConntrackCodecTest {

    private ConntrackCodec conntrackCodec;

    private ByteBuf buffer;
    private Action action;

    private final int length = 24;
    private final byte nxastConntrackSubtype = 35;
    private final int nxNatLengthAction1 = 32;
    private final int nxNatLengthAction2 = 24;
    private final byte nxastNatSubtype = 36;

    private final byte nxmCtMarkFieldCode = 107;
    private final byte setFieldCode = 25;
    private final int setFieldLength = 16;

    @Before
    public void setUp() {
        conntrackCodec = new ConntrackCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        action = createAction();
        conntrackCodec.serialize(action, buffer);

        Assert.assertEquals(length + nxNatLengthAction1 + nxNatLengthAction2 + setFieldLength, buffer.readableBytes());
        Assert.assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        Assert.assertEquals(length + nxNatLengthAction1 + nxNatLengthAction2 + setFieldLength, buffer.readUnsignedShort());
        Assert.assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        Assert.assertEquals(nxastConntrackSubtype, buffer.readUnsignedShort());
        Assert.assertEquals(1, buffer.readUnsignedShort());
        Assert.assertEquals(2, buffer.readUnsignedInt());
        Assert.assertEquals(3, buffer.readUnsignedShort());
        Assert.assertEquals(4, buffer.readByte());
        buffer.skipBytes(5);
        Assert.assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        Assert.assertEquals(nxNatLengthAction1, buffer.readUnsignedShort());
        Assert.assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        Assert.assertEquals(nxastNatSubtype, buffer.readUnsignedShort());
        buffer.skipBytes(2);
        Assert.assertEquals(5, buffer.readUnsignedShort());
        Assert.assertEquals(0x3F, buffer.readUnsignedShort());
        Assert.assertEquals(3232235520L, buffer.readUnsignedInt());
        Assert.assertEquals(3232238080L, buffer.readUnsignedInt());
        Assert.assertEquals(3000, buffer.readUnsignedShort());
        Assert.assertEquals(4000, buffer.readUnsignedShort());
        buffer.skipBytes(4);
        Assert.assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        Assert.assertEquals(nxNatLengthAction2, buffer.readUnsignedShort());
        Assert.assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        Assert.assertEquals(nxastNatSubtype, buffer.readUnsignedShort());
        buffer.skipBytes(2);
        Assert.assertEquals(5, buffer.readUnsignedShort());
        Assert.assertEquals(0x21, buffer.readUnsignedShort());
        Assert.assertEquals(3232235520L, buffer.readUnsignedInt());
        Assert.assertEquals(4000, buffer.readUnsignedShort());
        buffer.skipBytes(2);
        Assert.assertEquals(setFieldCode, buffer.readUnsignedShort());
        Assert.assertEquals(setFieldLength, buffer.readUnsignedShort());
        buffer.skipBytes(2);
        Assert.assertEquals(nxmCtMarkFieldCode << 1, buffer.readByte() & 0xFF);
        buffer.skipBytes(1);
        Assert.assertEquals(36, buffer.readUnsignedInt());
        buffer.skipBytes(4);
    }

    @Test
    public void serializeTestWithoutCtAction() {
        action = createActionWithoutCtAction();
        conntrackCodec.serialize(action, buffer);

        Assert.assertEquals(24, buffer.readableBytes());
        Assert.assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        Assert.assertEquals(length, buffer.readUnsignedShort());
        Assert.assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        Assert.assertEquals(nxastConntrackSubtype, buffer.readUnsignedShort());
        Assert.assertEquals(1, buffer.readUnsignedShort());
        Assert.assertEquals(2, buffer.readUnsignedInt());
        Assert.assertEquals(3, buffer.readUnsignedShort());
        Assert.assertEquals(4, buffer.readByte());
        buffer.skipBytes(5);
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);
        action = conntrackCodec.deserialize(buffer);
        ActionConntrack result = (ActionConntrack) action.getActionChoice();

        Assert.assertEquals(1, result.getNxActionConntrack().getFlags().shortValue());
        Assert.assertEquals(2, result.getNxActionConntrack().getZoneSrc().intValue());
        Assert.assertEquals(3, result.getNxActionConntrack().getConntrackZone().shortValue());
        Assert.assertEquals(4, result.getNxActionConntrack().getRecircTable().byteValue());
        List<CtActions> ctActions = result.getNxActionConntrack().getCtActions();
        NxActionNatCase nxActionNatCase = (NxActionNatCase) ctActions.get(0).getOfpactActions();
        NxActionNat natAction = nxActionNatCase.getNxActionNat();
        Assert.assertEquals(5, natAction.getFlags().shortValue());
        Assert.assertEquals(0x3F, natAction.getRangePresent().intValue());
        Assert.assertEquals("192.168.0.0", natAction.getIpAddressMin().getIpv4Address().getValue());
        Assert.assertEquals("192.168.10.0", natAction.getIpAddressMax().getIpv4Address().getValue());
        Assert.assertEquals(3000, natAction.getPortMin().shortValue());
        Assert.assertEquals(4000, natAction.getPortMax().shortValue());
        nxActionNatCase = (NxActionNatCase) ctActions.get(1).getOfpactActions();
        natAction = nxActionNatCase.getNxActionNat();
        Assert.assertEquals(5, natAction.getFlags().shortValue());
        Assert.assertEquals(0x21, natAction.getRangePresent().intValue());
        Assert.assertEquals("192.168.0.0", natAction.getIpAddressMin().getIpv4Address().getValue());
        Assert.assertEquals(4000, natAction.getPortMax().shortValue());
        NxActionCtMarkCase nxActionCtMarkCase = (NxActionCtMarkCase) ctActions.get(2).getOfpactActions();
        NxActionCtMark ctMarkAction = nxActionCtMarkCase.getNxActionCtMark();
        Assert.assertEquals((long) 36, ctMarkAction.getCtMark().longValue());
    }

    @Test
    public void deserializeTestWithoutCtAction() {
        createBufferWIthoutCtAction(buffer);
        action = conntrackCodec.deserialize(buffer);

        ActionConntrack result = (ActionConntrack) action.getActionChoice();

        Assert.assertEquals(1, result.getNxActionConntrack().getFlags().shortValue());
        Assert.assertEquals(2, result.getNxActionConntrack().getZoneSrc().intValue());
        Assert.assertEquals(3, result.getNxActionConntrack().getConntrackZone().shortValue());
        Assert.assertEquals(4, result.getNxActionConntrack().getRecircTable().byteValue());
    }

    private Action createAction() {

        NxActionConntrackBuilder nxActionConntrackBuilder = new NxActionConntrackBuilder();
        nxActionConntrackBuilder.setFlags(1);
        nxActionConntrackBuilder.setZoneSrc((long) 2);
        nxActionConntrackBuilder.setConntrackZone(3);
        nxActionConntrackBuilder.setRecircTable((short) 4);

        NxActionNatBuilder nxActionNatBuilder = new NxActionNatBuilder();
        nxActionNatBuilder.setFlags(5);
        nxActionNatBuilder.setRangePresent(0x3F);
        nxActionNatBuilder.setIpAddressMin(new IpAddress("192.168.0.0".toCharArray()));
        nxActionNatBuilder.setIpAddressMax(new IpAddress("192.168.10.0".toCharArray()));
        nxActionNatBuilder.setPortMin(3000);
        nxActionNatBuilder.setPortMax(4000);
        NxActionNatCaseBuilder nxActionNatCaseBuilder = new NxActionNatCaseBuilder();
        nxActionNatCaseBuilder.setNxActionNat(nxActionNatBuilder.build());
        CtActionsBuilder ctActionsBuilder = new CtActionsBuilder();
        ctActionsBuilder.setOfpactActions(nxActionNatCaseBuilder.build());
        List<CtActions> ctActionsList = new  ArrayList<>();
        ctActionsList.add(ctActionsBuilder.build());
        nxActionNatBuilder = new NxActionNatBuilder();
        nxActionNatBuilder.setFlags(5);
        nxActionNatBuilder.setRangePresent(0x21);
        nxActionNatBuilder.setIpAddressMin(new IpAddress("192.168.0.0".toCharArray()));
        nxActionNatBuilder.setPortMax(4000);
        nxActionNatCaseBuilder = new NxActionNatCaseBuilder();
        nxActionNatCaseBuilder.setNxActionNat(nxActionNatBuilder.build());
        ctActionsBuilder = new CtActionsBuilder();
        ctActionsBuilder.setOfpactActions(nxActionNatCaseBuilder.build());
        ctActionsList.add(ctActionsBuilder.build());
        nxActionConntrackBuilder.setCtActions(ctActionsList);

        NxActionCtMarkBuilder nxActionCtMarkBuilder = new NxActionCtMarkBuilder();
        nxActionCtMarkBuilder.setCtMark((long) 36);
        NxActionCtMarkCaseBuilder nxActionCtMarkCaseBuilder = new NxActionCtMarkCaseBuilder();
        nxActionCtMarkCaseBuilder.setNxActionCtMark(nxActionCtMarkBuilder.build());
        ctActionsBuilder = new CtActionsBuilder();
        ctActionsBuilder.setOfpactActions(nxActionCtMarkCaseBuilder.build());
        ctActionsList.add(ctActionsBuilder.build());
        nxActionConntrackBuilder.setCtActions(ctActionsList);

        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        ActionConntrackBuilder actionConntrackBuilder = new ActionConntrackBuilder();
        actionConntrackBuilder.setNxActionConntrack(nxActionConntrackBuilder.build());
        actionBuilder.setActionChoice(actionConntrackBuilder.build());

        return actionBuilder.build();
    }

    private Action createActionWithoutCtAction() {

        NxActionConntrackBuilder nxActionConntrackBuilder = new NxActionConntrackBuilder();
        nxActionConntrackBuilder.setFlags(1);
        nxActionConntrackBuilder.setZoneSrc((long) 2);
        nxActionConntrackBuilder.setConntrackZone(3);
        nxActionConntrackBuilder.setRecircTable((short) 4);

        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        ActionConntrackBuilder actionConntrackBuilder = new ActionConntrackBuilder();
        actionConntrackBuilder.setNxActionConntrack(nxActionConntrackBuilder.build());
        actionBuilder.setActionChoice(actionConntrackBuilder.build());

        return actionBuilder.build();
    }

    private void createBuffer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(length + nxNatLengthAction1 + nxNatLengthAction2 + setFieldLength);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(nxastConntrackSubtype);
        //FLAG = 1
        message.writeShort(1);
        //ZONE_SRC = 2
        message.writeInt(2);
        //CONNTRACK_ZONE = 3
        message.writeShort(3);
        //RECIRC_TABLE = 4
        message.writeByte(4);
        //ADDS 5 empty bytes
        message.writeZero(5);
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(nxNatLengthAction1);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(nxastNatSubtype);
        message.writeZero(2);
        //NAT FLAG
        message.writeShort(5);
        //RANGE PRESENT
        message.writeShort(0x3F);
        //IP ADDRESS MIN
        message.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(new Ipv4Address("192.168.0.0")));
        //IP ADDRESS MAX
        message.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(new Ipv4Address("192.168.10.0")));
        //PORT MIN
        message.writeShort(3000);
        //PORT MAX
        message.writeShort(4000);
        message.writeZero(4);

        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(nxNatLengthAction2);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(nxastNatSubtype);
        message.writeZero(2);
        //NAT FLAG
        message.writeShort(5);
        //RANGE PRESENT
        message.writeShort(0x21);
        //IP ADDRESS MIN
        message.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(new Ipv4Address("192.168.0.0")));
        //PORT MAX
        message.writeShort(4000);
        message.writeZero(2);

        message.writeShort(setFieldCode);
        message.writeShort(setFieldLength);
        message.writeZero(1);
        message.writeByte(1);
        message.writeByte(nxmCtMarkFieldCode << 1);
        message.writeByte(4);
        // value of ct_mark
        message.writeInt(36);
        message.writeZero(4);
    }

    private void createBufferWIthoutCtAction(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(length);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(nxastConntrackSubtype);
        //FLAG = 1
        message.writeShort(1);
        //ZONE_SRC = 2
        message.writeInt(2);
        //CONNTRACK_ZONE = 3
        message.writeShort(3);
        //RECIRC_TABLE = 4
        message.writeByte(4);
        //ADDS 5 empty bytes
        message.writeZero(5);
    }
}
