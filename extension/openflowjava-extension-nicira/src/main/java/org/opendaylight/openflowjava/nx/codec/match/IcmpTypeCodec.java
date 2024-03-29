/*
 * Copyright (c) 2016 Red Hat, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfIcmpType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.IcmpTypeCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.IcmpTypeCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.icmp.type.grouping.IcmpTypeValuesBuilder;

/**
 * Codec for the Icmp type message.
 *
 * @author Josh Hershberg (jhershbe@redhat.com)
 */
public class IcmpTypeCodec extends AbstractMatchCodec {
    private static final int VALUE_LENGTH = 1;
    private static final int NXM_FIELD_CODE = 13;
    public static final MatchEntrySerializerKey<Nxm0Class, NxmOfIcmpType> SERIALIZER_KEY =
            new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, Nxm0Class.VALUE, NxmOfIcmpType.VALUE);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
            EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_0_CLASS, NXM_FIELD_CODE);

    @Override
    public MatchEntry deserialize(final ByteBuf message) {
        return deserializeHeaderToBuilder(message)
                .setMatchEntryValue(new IcmpTypeCaseValueBuilder()
                    .setIcmpTypeValues(new IcmpTypeValuesBuilder().setValue(readUint8(message)).build())
                    .build())
                .setHasMask(false)
                .build();
    }

    @Override
    public void serialize(final MatchEntry input, final ByteBuf outBuffer) {
        serializeHeader(input, outBuffer);
        IcmpTypeCaseValue icmpTypeValue = (IcmpTypeCaseValue) input.getMatchEntryValue();
        outBuffer.writeByte(icmpTypeValue.getIcmpTypeValues().getValue().toJava());
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
        return NxmOfIcmpType.VALUE;
    }

    @Override
    public OxmClassBase getOxmClass() {
        return Nxm0Class.VALUE;
    }
}
