/*
 * Copyright (c) 2015, 2017 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionConntrackBuilder;
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
import org.opendaylight.yangtools.yang.common.Uint16;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action codec for conntrack.
 *
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
            new NiciraActionSerializerKey(EncodeConstants.OF_VERSION_1_3, ActionConntrack.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY =
            new NiciraActionDeserializerKey(EncodeConstants.OF_VERSION_1_3, NXAST_CONNTRACK_SUBTYPE);

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

    private static int getActionLength(final ActionConntrack action) {
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
                int pad = 8 - natLength % 8;
                length += natLength + pad;
            } else if (ctActions.getOfpactActions() instanceof NxActionCtMarkCase) {
                length += SET_FIELD_LENGTH;
            }
        }
        LOG.trace("ActionLength :conntrack: length {}",length);
        return length;
    }

    private static int getNatActionLength(final NxActionNat natAction) {
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

    private static void serializeCtAction(final ByteBuf outBuffer, final ActionConntrack action) {
        List<CtActions> ctActionsList = action.getNxActionConntrack().getCtActions();
        if (ctActionsList != null) {
            for (CtActions ctActions : ctActionsList) {
                if (ctActions.getOfpactActions() instanceof NxActionNatCase) {
                    NxActionNatCase nxActionNatCase = (NxActionNatCase)ctActions.getOfpactActions();
                    NxActionNat natAction = nxActionNatCase.getNxActionNat();
                    int natLength = getNatActionLength(natAction);
                    int pad = 8 - natLength % 8;
                    serializeHeader(natLength + pad, NXAST_NAT_SUBTYPE, outBuffer);
                    outBuffer.writeZero(2);
                    outBuffer.writeShort(natAction.getFlags().shortValue());
                    short rangePresent = natAction.getRangePresent().shortValue();
                    outBuffer.writeShort(rangePresent);
                    if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MIN.getIntValue())) {
                        writeIpv4Address(outBuffer, natAction.getIpAddressMin());
                    }
                    if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEIPV4MAX.getIntValue())) {
                        writeIpv4Address(outBuffer, natAction.getIpAddressMax());
                    }
                    if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMIN.getIntValue())) {
                        outBuffer.writeShort(natAction.getPortMin().toJava());
                    }
                    if (0 != (rangePresent & NxActionNatRangePresent.NXNATRANGEPROTOMAX.getIntValue())) {
                        outBuffer.writeShort(natAction.getPortMax().toJava());
                    }
                    outBuffer.writeZero(pad);
                } else if (ctActions.getOfpactActions() instanceof NxActionCtMarkCase) {
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
        final short length = deserializeCtHeader(message);
        final var nxActionConntrackBuilder = new NxActionConntrackBuilder()
            .setFlags(readUint16(message))
            .setZoneSrc(readUint32(message))
            .setConntrackZone(readUint16(message))
            .setRecircTable(readUint8(message));
        message.skipBytes(5);

        if (length > CT_LENGTH) {
            deserializeCtAction(message, nxActionConntrackBuilder, length - CT_LENGTH);
        }
        return new ActionBuilder()
            .setExperimenterId(getExperimenterId())
            .setActionChoice(new ActionConntrackBuilder()
                .setNxActionConntrack(nxActionConntrackBuilder.build())
                .build())
            .build();
    }

    private static void deserializeCtAction(final ByteBuf message,
            final NxActionConntrackBuilder nxActionConntrackBuilder, final int ctActionsLength) {
        List<CtActions> ctActionsList = new ArrayList<>();
        int processedCtActionsLength = ctActionsLength;

        while (processedCtActionsLength > 0) {
            final int startReaderIndex = message.readerIndex();

            if (EncodeConstants.EXPERIMENTER_VALUE == message.readUnsignedShort()) {
                // NAT action
                // reset indices
                message.setIndex(startReaderIndex, message.writerIndex());

                final int startIndex = message.readerIndex();
                final int length = deserializeCtHeader(message);

                processedCtActionsLength = processedCtActionsLength - length;
                message.skipBytes(2);
                final var nxActionNatBuilder = new NxActionNatBuilder()
                    .setFlags(readUint16(message));

                final Uint16 rangePresent = readUint16(message);
                nxActionNatBuilder.setRangePresent(rangePresent);

                final int rangeBits = rangePresent.toJava();
                if ((rangeBits & NxActionNatRangePresent.NXNATRANGEIPV4MIN.getIntValue()) != 0) {
                    nxActionNatBuilder.setIpAddressMin(readIpv4Address(message));
                }
                if ((rangeBits & NxActionNatRangePresent.NXNATRANGEIPV4MAX.getIntValue()) != 0) {
                    nxActionNatBuilder.setIpAddressMax(readIpv4Address(message));
                }
                if ((rangeBits & NxActionNatRangePresent.NXNATRANGEPROTOMIN.getIntValue()) != 0) {
                    nxActionNatBuilder.setPortMin(readUint16(message));
                }
                if ((rangeBits & NxActionNatRangePresent.NXNATRANGEPROTOMAX.getIntValue()) != 0) {
                    nxActionNatBuilder.setPortMax(readUint16(message));
                }

                ctActionsList.add(new CtActionsBuilder()
                    .setOfpactActions(new NxActionNatCaseBuilder()
                    .setNxActionNat(nxActionNatBuilder.build()).build())
                    .build());

                // Padding
                message.skipBytes(length - (message.readerIndex() - startIndex));
            } else {
                // only other possible action here is currently ct_mark
                // reset indices
                message.setIndex(startReaderIndex, message.writerIndex());
                processedCtActionsLength = processedCtActionsLength - SET_FIELD_LENGTH;

                deserializeCtHeaderWithoutSubtype(message);

                ctActionsList.add(new CtActionsBuilder()
                    .setOfpactActions(new NxActionCtMarkCaseBuilder()
                        .setNxActionCtMark(new NxActionCtMarkBuilder()
                            .setCtMark(readUint32(message))
                            .build())
                        .build())
                    .build());

                // Padding
                message.skipBytes(Integer.BYTES);
            }
        }

        nxActionConntrackBuilder.setCtActions(ctActionsList);
    }

    private static IpAddress readIpv4Address(final ByteBuf message) {
        return new IpAddress(IetfInetUtil.ipv4AddressFor(message.readInt()));
    }

    private static void writeIpv4Address(final ByteBuf outBuffer, final IpAddress ipAddress) {
        if (ipAddress != null) {
            outBuffer.writeBytes(IetfInetUtil.ipv4AddressBytes(ipAddress.getIpv4Address()));
        }
    }

    private static short deserializeCtHeaderWithoutSubtype(final ByteBuf message) {
        // size of experimenter type / size of set field code (in case of ct_mark)
        message.skipBytes(Short.BYTES);
        // size of length
        short length = message.readShort();
        // vendor id / 00 01 d6 04 (in case of ct_mark)
        message.skipBytes(Integer.BYTES);
        return length;
    }

    private static short deserializeCtHeader(final ByteBuf message) {
        short length = deserializeCtHeaderWithoutSubtype(message);
        // subtype
        message.skipBytes(Short.BYTES);
        return length;
    }
}
