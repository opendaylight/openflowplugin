/**
 * Copyright (c) 2013, 2015 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;

import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfTcpFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.tcp.flag.grouping.TcpFlagValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TcpFlagCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TcpFlagCaseValueBuilder;

public class TcpFlagCodec extends AbstractMatchCodec {
    private static final int VALUE_LENGTH = 4;
    private static final int NXM_FIELD_CODE = 34;
    public static final MatchEntrySerializerKey<Nxm0Class, NxmOfTcpFlag> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
            EncodeConstants.OF13_VERSION_ID, Nxm0Class.class, NxmOfTcpFlag.class);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_0_CLASS, NXM_FIELD_CODE);

public void serialize(MatchEntry input, ByteBuf outBuffer) {
    serializeHeader(input, outBuffer);
    TcpFlagCaseValue tcpFlagCase = ((TcpFlagCaseValue) input.getMatchEntryValue());
    outBuffer.writeShort(tcpFlagCase.getTcpFlagValues().getTcpFlag());
    }

public MatchEntry deserialize(ByteBuf message) {
    MatchEntryBuilder matchEntryBuilder = deserializeHeader(message);
    matchEntryBuilder.setHasMask(true);
    int tcpFlag = message.readUnsignedShort();
    int mask = message.readUnsignedShort();
    message.readBytes(mask);
    TcpFlagCaseValueBuilder caseBuilder = new TcpFlagCaseValueBuilder();
    TcpFlagValuesBuilder tcpFlagValuesBuilder = new TcpFlagValuesBuilder();
    tcpFlagValuesBuilder.setTcpFlag(tcpFlag);
    tcpFlagValuesBuilder.setMask(tcpFlag);
    caseBuilder.setTcpFlagValues(tcpFlagValuesBuilder.build());
    matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
    return matchEntryBuilder.build();
    }

public int getNxmFieldCode() {
        return NXM_FIELD_CODE;
    }

public int getOxmClassCode() {
        return OxmMatchConstants.NXM_0_CLASS;
    }

public int getValueLength() {
        return VALUE_LENGTH;
    }

public Class<? extends MatchField> getNxmField() {
        return NxmOfTcpFlag.class;
    }

public Class<? extends OxmClassBase> getOxmClass() {
        return Nxm0Class.class;
    }

}



