/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.match;

import io.netty.buffer.ByteBuf;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;

import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfUdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.udp.src.grouping.UdpSrcValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.UdpSrcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.UdpSrcCaseValueBuilder;

/**
 * @author Aswin Suryanarayanan.
 */

public class UdpSrcCodec extends AbstractMatchCodec {

    private static final int VALUE_LENGTH = 4;
    private static final int NXM_FIELD_CODE = 11;
    public static final MatchEntrySerializerKey<Nxm0Class, NxmOfUdpSrc> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
            EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfUdpSrc.class);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, NXM_FIELD_CODE);

    @Override
    public void serialize(MatchEntry input, ByteBuf outBuffer) {
        serializeHeader(input, outBuffer);
        UdpSrcCaseValue udpSrcCase = ((UdpSrcCaseValue) input.getMatchEntryValue());
        outBuffer.writeShort(udpSrcCase.getUdpSrcValues().getPort().getValue());
        outBuffer.writeShort(udpSrcCase.getUdpSrcValues().getMask());
    }

    @Override
    public MatchEntry deserialize(ByteBuf message) {
        MatchEntryBuilder matchEntryBuilder = deserializeHeader(message);
        matchEntryBuilder.setHasMask(true);
        int portNo = message.readUnsignedShort();
        int mask = message.readUnsignedShort();
        message.readBytes(mask);
        UdpSrcCaseValueBuilder caseBuilder = new UdpSrcCaseValueBuilder();
        UdpSrcValuesBuilder udpSrcValuesBuilder = new UdpSrcValuesBuilder();
        udpSrcValuesBuilder.setPort(new PortNumber(portNo));
        udpSrcValuesBuilder.setMask(portNo);
        caseBuilder.setUdpSrcValues(udpSrcValuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        return matchEntryBuilder.build();
    }

    @Override
    public int getNxmFieldCode() {
        return NXM_FIELD_CODE;
    }

    @Override
    public int getOxmClassCode() {
        return OxmMatchConstants.NXM_0_CLASS;
    }

    @Override
    public int getValueLength() {
        return VALUE_LENGTH;
    }

    @Override
    public Class<? extends MatchField> getNxmField() {
        return NxmOfUdpSrc.class;
    }

    @Override
    public Class<? extends OxmClassBase> getOxmClass() {
        return Nxm0Class.class;
    }

}
