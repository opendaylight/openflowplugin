/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfEthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.EthSrcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.EthSrcCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.eth.src.grouping.EthSrcValuesBuilder;

public class EthSrcCodec extends AbstractMatchCodec {
    private static final int VALUE_LENGTH = 6;
    private static final int NXM_FIELD_CODE = 2;
    public static final MatchEntrySerializerKey<Nxm0Class, NxmOfEthSrc> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
            EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfEthSrc.VALUE);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
            EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, NXM_FIELD_CODE);

    @Override
    public void serialize(final MatchEntry input, final ByteBuf outBuffer) {
        serializeHeader(input, outBuffer);
        EthSrcCaseValue ethSrcCase = (EthSrcCaseValue) input.getMatchEntryValue();
        outBuffer.writeBytes(IetfYangUtil.macAddressBytes(ethSrcCase.getEthSrcValues().getMacAddress()));
    }

    @Override
    public MatchEntry deserialize(final ByteBuf message) {
        MatchEntryBuilder matchEntryBuilder = deserializeHeaderToBuilder(message);
        byte[] address = new byte[VALUE_LENGTH];
        message.readBytes(address);
        EthSrcCaseValueBuilder caseBuilder = new EthSrcCaseValueBuilder();
        caseBuilder.setEthSrcValues(new EthSrcValuesBuilder()
            .setMacAddress(IetfYangUtil.macAddressFor(address))
            .build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        matchEntryBuilder.setHasMask(false);
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
    public MatchField getNxmField() {
        return NxmOfEthSrc.VALUE;
    }

    @Override
    public OxmClassBase getOxmClass() {
        return Nxm0Class.VALUE;
    }
}
