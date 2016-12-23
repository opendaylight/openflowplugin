/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.NxActionNatRangePresent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.NxActionConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.nx.action.conntrack.CtActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.nx.action.conntrack.CtActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.NxActionNatCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.NxActionNatCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.nat._case.NxActionNat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.nat._case.NxActionNatBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

/**
 * @author Aswin Suryanarayanan.
 */

public class ConntrackCodec extends AbstractActionCodec {
    private static final Logger LOG = LoggerFactory.getLogger( ConntrackCodec.class);
    public static final int CT_LENGTH = 24;
    public static final int NX_NAT_LENGTH = 16;
    public static final int SHORT_LENGTH = 2;
    public static final int INT_LENGTH = 4;

    public static final byte NXAST_CONNTRACK_SUBTYPE = 35;
    public static final byte NXAST_NAT_SUBTYPE = 36;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionConntrack.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_CONNTRACK_SUBTYPE);

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        LOG.error("serialize :conntrack");
        ActionConntrack action = (ActionConntrack) input.getActionChoice();
        int length = getActionLength(action);
        int pad = length % 8;
        serializeHeader(length + pad + CT_LENGTH, NXAST_CONNTRACK_SUBTYPE, outBuffer);

        outBuffer.writeShort(action.getNxActionConntrack().getFlags().shortValue());
        outBuffer.writeInt(action.getNxActionConntrack().getZoneSrc().intValue());
        outBuffer.writeShort(action.getNxActionConntrack().getConntrackZone().shortValue());
        outBuffer.writeByte(action.getNxActionConntrack().getRecircTable().byteValue());
        outBuffer.writeZero(5);
        serializeCtAction(outBuffer,action, length);
    }

    private int getActionLength(ActionConntrack action) {
        int length = 0;
        List<CtActions> ctActionsList = action.getNxActionConntrack().getCtActions();
        if(ctActionsList == null){
            return length;
        }
        for(CtActions ctActions : ctActionsList){
            if(ctActions.getOfpactActions() instanceof NxActionNatCase){
                length += NX_NAT_LENGTH;
                NxActionNatCase nxActionNatCase= (NxActionNatCase)ctActions.getOfpactActions();
                NxActionNat natAction = nxActionNatCase.getNxActionNat();
                short rangePresent = natAction.getRangePresent().shortValue();
                if(0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MIN.getIntValue())) {
                    length += INT_LENGTH;
                }
                if(0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MAX.getIntValue())) {
                    length += INT_LENGTH;
                }
                if(0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMIN.getIntValue())) {
                    length += SHORT_LENGTH;
                }
                if(0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMAX.getIntValue())) {
                    length += SHORT_LENGTH;
                }
            }
        }
        LOG.error("serialize :conntrack: length {}",length);
        return length;
     }

    private void serializeCtAction(final ByteBuf outBuffer, ActionConntrack action, int length) {
        List<CtActions> ctActionsList = action.getNxActionConntrack().getCtActions();
        if(ctActionsList != null){
            for(CtActions ctActions : ctActionsList){
                if(ctActions.getOfpactActions() instanceof NxActionNatCase){
                    NxActionNatCase nxActionNatCase= (NxActionNatCase)ctActions.getOfpactActions();
                    NxActionNat natAction = nxActionNatCase.getNxActionNat();
                    int pad = length % 8;
                    serializeHeader(length + pad, NXAST_NAT_SUBTYPE, outBuffer);
                    outBuffer.writeZero(2);
                    outBuffer.writeShort(natAction.getFlags().shortValue());
                    short rangePresent = natAction.getRangePresent().shortValue();
                    outBuffer.writeShort(rangePresent);
                    if(0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MIN.getIntValue())) {
                        outBuffer.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(natAction
                                .getIpAddressMin().getIpv4Address()));
                    }
                    if(0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MAX.getIntValue())) {
                        outBuffer.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(natAction
                                .getIpAddressMax().getIpv4Address()));
                    }
                    if(0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMIN.getIntValue())) {
                        outBuffer.writeShort(natAction.getPortMin());
                    }
                    if(0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMAX.getIntValue())) {
                        outBuffer.writeShort(natAction.getPortMax());
                    }
                    outBuffer.writeZero(pad);
                }
            }
        }
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionConntrackBuilder actionConntrackBuilder = new ActionConntrackBuilder();

        NxActionConntrackBuilder nxActionConntrackBuilder = new NxActionConntrackBuilder();
        nxActionConntrackBuilder.setFlags(message.readUnsignedShort());
        nxActionConntrackBuilder.setZoneSrc(message.readUnsignedInt());
        nxActionConntrackBuilder.setConntrackZone(message.readUnsignedShort());
        nxActionConntrackBuilder.setRecircTable(message.readUnsignedByte());
        message.skipBytes(5);
        dserializeCtAction(message,nxActionConntrackBuilder);
        actionConntrackBuilder.setNxActionConntrack(nxActionConntrackBuilder.build());
        actionBuilder.setActionChoice(actionConntrackBuilder.build());

        return actionBuilder.build();
    }

    private void dserializeCtAction(final ByteBuf message, NxActionConntrackBuilder nxActionConntrackBuilder) {
        deserializeHeader(message);

        NxActionNatBuilder nxActionNatBuilder = new NxActionNatBuilder();
        message.skipBytes(2);
        nxActionNatBuilder.setFlags(message.readUnsignedShort());

        int rangePresent = message.readUnsignedShort();
        nxActionNatBuilder.setRangePresent(rangePresent);
        if(0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MIN.getIntValue())) {
            InetAddress address = InetAddresses.fromInteger((int)message.readUnsignedInt());
            nxActionNatBuilder.setIpAddressMin(new IpAddress(address.getHostAddress().toCharArray()));
        }
        if(0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MAX.getIntValue())) {
            InetAddress address = InetAddresses.fromInteger((int)message.readUnsignedInt());
            nxActionNatBuilder.setIpAddressMax(new IpAddress(address.getHostAddress().toCharArray()));
        }
        if(0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMIN.getIntValue())) {
            nxActionNatBuilder.setPortMin(message.readUnsignedShort());
        }
        if(0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMAX.getIntValue())) {
            nxActionNatBuilder.setPortMax(message.readUnsignedShort());
        }
        NxActionNatCaseBuilder caseBuilder = new NxActionNatCaseBuilder();
        caseBuilder.setNxActionNat(nxActionNatBuilder.build());
        CtActionsBuilder ctActionsBuilder = new CtActionsBuilder();
        ctActionsBuilder.setOfpactActions(caseBuilder.build());
        List<CtActions> ctActionsList = new ArrayList<>();
        ctActionsList.add(ctActionsBuilder.build());
        nxActionConntrackBuilder.setCtActions(ctActionsList);
    }
 }