/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.eric.codec.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.eric.api.EricConstants;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EricExpClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.Icmpv6NdOptionsType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.icmpv6.nd.options.type.grouping.Icmpv6NdOptionsTypeValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.ofj.aug.eric.match.options.Icmpv6NdOptionsTypeCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.ofj.aug.eric.match.options.Icmpv6NdOptionsTypeCaseValueBuilder;

public class Icmpv6NDOptionsTypeCodec extends AbstractMatchCodec {
    private static final int VALUE_LENGTH = 1;
    public static final MatchEntrySerializerKey<?, ?> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
            EncodeConstants.OF_VERSION_1_3, EricExpClass.VALUE, Icmpv6NdOptionsType.VALUE);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
            EncodeConstants.OF_VERSION_1_3, EricConstants.ERICOXM_OF_EXPERIMENTER_ID,
            EricConstants.ERICOXM_OF_ICMPV6_ND_OPTIONS_TYPE);
    public static final Icmpv6NDOptionsTypeCodec INSTANCE = new Icmpv6NDOptionsTypeCodec();

    @Override
    public void serialize(final MatchEntry input, final ByteBuf outBuffer) {
        serializeHeader(input, outBuffer);
        Icmpv6NdOptionsTypeCaseValue caseValue = (Icmpv6NdOptionsTypeCaseValue) input.getMatchEntryValue();
        outBuffer.writeByte(caseValue.getIcmpv6NdOptionsTypeValues().getIcmpv6NdOptionsType().toJava());
    }

    @Override
    public MatchEntry deserialize(final ByteBuf message) {
        return deserializeHeaderToBuilder(message)
                .setMatchEntryValue(new Icmpv6NdOptionsTypeCaseValueBuilder()
                    .setIcmpv6NdOptionsTypeValues(new Icmpv6NdOptionsTypeValuesBuilder()
                        .setIcmpv6NdOptionsType(readUint8(message))
                        .build())
                    .build())
                .build();
    }

    @Override
    public int getEricFieldCode() {
        return EricConstants.ERICOXM_OF_ICMPV6_ND_OPTIONS_TYPE;
    }

    @Override
    public int getOxmClassCode() {
        return EricConstants.ERICOXM_OF_EXPERIMENTER_ID;
    }

    @Override
    public int getValueLength() {
        return VALUE_LENGTH;
    }

    @Override
    public MatchField getEricField() {
        return Icmpv6NdOptionsType.VALUE;
    }

    @Override
    public OxmClassBase getOxmClass() {
        return EricExpClass.VALUE;
    }
}
