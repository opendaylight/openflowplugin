/*
 * Copyright (c) 2015, 2017 Hewlett-Packard Enterprise and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.NxActionCtMarkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.NxActionNatCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.NxActionCtMarkCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.NxActionNatCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.ct.mark._case.NxActionCtMark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.nat._case.NxActionNat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.ct.mark._case.NxActionCtMarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofpact.actions.ofpact.actions.nx.action.nat._case.NxActionNatBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

/**
 * @author Aswin Suryanarayanan.
 */

public class ConntrackCodec extends AbstractActionCodec {
    private static final Logger LOG = LoggerFactory.getLogger(ConntrackCodec.class);
    public static final int CT_LENGTH = 24;
    public static final int NX_NAT_LENGTH = 16;
    public static final int SHORT_LENGTH = 2;
    public static final int INT_LENGTH = 4;
    private static final int NXM_CT_MARK_FIELD_CODE = 107;
    private static final int NXM_FIELD_CODE = 1;
    private static final int SET_FIELD_LENGTH = 16;
    private static final int SET_FIELD_CODE = 25;

    public static final byte NXAST_CONNTRACK_SUBTYPE = 35;
    public static final byte NXAST_NAT_SUBTYPE = 36;
    public static final NiciraActionSerializerKey SERIALIZER_KEY =
            new NiciraActionSerializerKey(EncodeConstants.OF13_VERSION_ID, ActionConntrack.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF13_VERSION_ID, NXAST_CONNTRACK_SUBTYPE);

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        LOG.trace("serialize :conntrack");
        ActionConntrack action = (ActionConntrack) input.getActionChoice();
        int length = getActionLength(action);
        int pad = length % 8;
        serializeHeader(length + pad + CT_LENGTH, NXAST_CONNTRACK_SUBTYPE, outBuffer);

        outBuffer.writeShort(action.getNxActionConntrack().getFlags().shortValue());
        outBuffer.writeInt(action.getNxActionConntrack().getZoneSrc().intValue());
        outBuffer.writeShort(action.getNxActionConntrack().getConntrackZone().shortValue());
        outBuffer.writeByte(action.getNxActionConntrack().getRecircTable().byteValue());
        outBuffer.writeZero(5);
        serializeCtAction(outBuffer,action);
    }

    private int getActionLength(final ActionConntrack action) {
        int length = 0;
        List<CtActions> ctActionsList = action.getNxActionConntrack().getCtActions();
        if (ctActionsList == null) {
            return length;
        }
        for (CtActions ctActions : ctActionsList) {
            if (ctActions.getOfpactActions() instanceof NxActionNatCase) {
                NxActionNatCase nxActionNatCase = (NxActionNatCase)ctActions.getOfpactActions();
                NxActionNat natAction = nxActionNatCase.getNxActionNat();
                int natLength = getNatActionLength(natAction);
                int pad = 8 - (natLength % 8);
                length += natLength + pad;
            } else if (ctActions.getOfpactActions() instanceof NxActionCtMarkCase) {
                length += SET_FIELD_LENGTH;
            }
        }
        LOG.trace("ActionLength :conntrack: length {}",length);
        return length;
    }

    private int getNatActionLength(final NxActionNat natAction) {
        int natLength = NX_NAT_LENGTH;
        short rangePresent = natAction.getRangePresent().shortValue();
        if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MIN.getIntValue())) {
            natLength += INT_LENGTH;
        }
        if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MAX.getIntValue())) {
            natLength += INT_LENGTH;
        }
        if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMIN.getIntValue())) {
            natLength += SHORT_LENGTH;
        }
        if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMAX.getIntValue())) {
            natLength += SHORT_LENGTH;
        }
        return natLength;

    }

    private void serializeCtAction(final ByteBuf outBuffer, final ActionConntrack action) {
        List<CtActions> ctActionsList = action.getNxActionConntrack().getCtActions();
        if (ctActionsList != null) {
            for (CtActions ctActions : ctActionsList) {
                if (ctActions.getOfpactActions() instanceof NxActionNatCase){
                    NxActionNatCase nxActionNatCase = (NxActionNatCase)ctActions.getOfpactActions();
                    NxActionNat natAction = nxActionNatCase.getNxActionNat();
                    int natLength = getNatActionLength(natAction);
                    int pad = 8 - (natLength % 8);
                    serializeHeader(natLength + pad, NXAST_NAT_SUBTYPE, outBuffer);
                    outBuffer.writeZero(2);
                    outBuffer.writeShort(natAction.getFlags().shortValue());
                    short rangePresent = natAction.getRangePresent().shortValue();
                    outBuffer.writeShort(rangePresent);
                    if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MIN.getIntValue())) {
                        if (null != natAction.getIpAddressMin()) {
                            outBuffer.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(natAction
                                    .getIpAddressMin().getIpv4Address()));
                        }
                    }
                    if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MAX.getIntValue())) {
                        if (null != natAction.getIpAddressMax()) {
                            outBuffer.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(natAction
                                    .getIpAddressMax().getIpv4Address()));
                        }
                    }
                    if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMIN.getIntValue())) {
                        outBuffer.writeShort(natAction.getPortMin());
                    }
                    if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMAX.getIntValue())) {
                        outBuffer.writeShort(natAction.getPortMax());
                    }
                    outBuffer.writeZero(pad);
                } else if (ctActions.getOfpactActions() instanceof NxActionCtMarkCase){
                    NxActionCtMarkCase nxActionCtMarkCase = (NxActionCtMarkCase)ctActions.getOfpactActions();
                    NxActionCtMark ctMarkAction = nxActionCtMarkCase.getNxActionCtMark();

                    // structure:
                    // 00 19 - set field code
                    // 00 10 - set field length
                    // 00 01 d6 04
                    // xx xx xx xx FIELD VALUE (4 bytes)
                    // xx xx xx xx PADDING (4 bytes)

                    outBuffer.writeShort(SET_FIELD_CODE);
                    outBuffer.writeShort(SET_FIELD_LENGTH);
                    outBuffer.writeZero(1);
                    outBuffer.writeByte(NXM_FIELD_CODE);
                    outBuffer.writeByte(NXM_CT_MARK_FIELD_CODE << 1);
                    outBuffer.writeByte(INT_LENGTH);
                    outBuffer.writeInt(ctMarkAction.getCtMark().intValue());
                    outBuffer.writeZero(INT_LENGTH);

                    // TODO: ct_mark mask is not supported yet
                }
            }
        }
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        short length = deserializeCtHeader(message);
        NxActionConntrackBuilder nxActionConntrackBuilder = new NxActionConntrackBuilder();
        nxActionConntrackBuilder.setFlags(message.readUnsignedShort());
        nxActionConntrackBuilder.setZoneSrc(message.readUnsignedInt());
        nxActionConntrackBuilder.setConntrackZone(message.readUnsignedShort());
        nxActionConntrackBuilder.setRecircTable(message.readUnsignedByte());
        message.skipBytes(5);
        if  (length > CT_LENGTH) {
            deserializeCtAction(message,nxActionConntrackBuilder, length - CT_LENGTH);
        }
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(getExperimenterId());
        ActionConntrackBuilder actionConntrackBuilder = new ActionConntrackBuilder();
        actionConntrackBuilder.setNxActionConntrack(nxActionConntrackBuilder.build());
        actionBuilder.setActionChoice(actionConntrackBuilder.build());
        return actionBuilder.build();
    }

    private void deserializeCtAction(final ByteBuf message, final NxActionConntrackBuilder nxActionConntrackBuilder,
            final int ctActionsLength) {
        List<CtActions> ctActionsList = new ArrayList<>();
        int processedCtActionsLength = ctActionsLength;

        while (processedCtActionsLength > 0){
            int startReaderIndex = message.readerIndex();

            if (EncodeConstants.EXPERIMENTER_VALUE == message.readUnsignedShort()) {
                // NAT action
                // reset indices
                message.setIndex(startReaderIndex, message.writerIndex());

                int startIndex = message.readerIndex();
                int length = deserializeCtHeader(message);

                processedCtActionsLength = processedCtActionsLength - length;
                NxActionNatBuilder nxActionNatBuilder = new NxActionNatBuilder();
                message.skipBytes(2);
                nxActionNatBuilder.setFlags(message.readUnsignedShort());

                int rangePresent = message.readUnsignedShort();
                nxActionNatBuilder.setRangePresent(rangePresent);
                if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MIN.getIntValue())) {
                    InetAddress address = InetAddresses.fromInteger((int)message.readUnsignedInt());
                    nxActionNatBuilder.setIpAddressMin(new IpAddress(address.getHostAddress().toCharArray()));
                }
                if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MAX.getIntValue())) {
                    InetAddress address = InetAddresses.fromInteger((int)message.readUnsignedInt());
                    nxActionNatBuilder.setIpAddressMax(new IpAddress(address.getHostAddress().toCharArray()));
                }
                if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMIN.getIntValue())) {
                    nxActionNatBuilder.setPortMin(message.readUnsignedShort());
                }
                if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMAX.getIntValue())) {
                    nxActionNatBuilder.setPortMax(message.readUnsignedShort());
                }

                NxActionNatCaseBuilder caseBuilder = new NxActionNatCaseBuilder();
                caseBuilder.setNxActionNat(nxActionNatBuilder.build());
                CtActionsBuilder ctActionsBuilder = new CtActionsBuilder();
                ctActionsBuilder.setOfpactActions(caseBuilder.build());
                ctActionsList.add(ctActionsBuilder.build());
                int pad = length - (message.readerIndex() - startIndex);
                message.skipBytes(pad);
            } else {
                // only other possible action here is currently ct_mark
                // reset indices
                message.setIndex(startReaderIndex, message.writerIndex());
                processedCtActionsLength = processedCtActionsLength - SET_FIELD_LENGTH;

                deserializeCtHeaderWithoutSubtype(message);

                NxActionCtMarkBuilder nxActionCtMarkBuilder = new NxActionCtMarkBuilder();
                nxActionCtMarkBuilder.setCtMark(message.readUnsignedInt());

                NxActionCtMarkCaseBuilder caseBuilder = new NxActionCtMarkCaseBuilder();
                caseBuilder.setNxActionCtMark(nxActionCtMarkBuilder.build());
                CtActionsBuilder ctActionsBuilder = new CtActionsBuilder();
                ctActionsBuilder.setOfpactActions(caseBuilder.build());
                ctActionsList.add(ctActionsBuilder.build());
                // padding
                message.skipBytes(EncodeConstants.SIZE_OF_INT_IN_BYTES);
            }
        }

        nxActionConntrackBuilder.setCtActions(ctActionsList);
    }

    private short deserializeCtHeaderWithoutSubtype(final ByteBuf message) {
        // size of experimenter type / size of set field code (in case of ct_mark)
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        // size of length
        short length = message.readShort();
        // vendor id / 00 01 d6 04 (in case of ct_mark)
        message.skipBytes(EncodeConstants.SIZE_OF_INT_IN_BYTES);
        return length;
    }

    private short deserializeCtHeader(final ByteBuf message) {
        short length = deserializeCtHeaderWithoutSubtype(message);
        // subtype
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        return length;
    }
}
