/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.eric.codec.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.eric.api.EricConstants;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EricExpClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.Icmpv6NdReserved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.icmpv6.nd.reserved.grouping.Icmpv6NdReservedValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.ofj.aug.eric.match.reserved.Icmpv6NdReservedCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.ofj.aug.eric.match.reserved.Icmpv6NdReservedCaseValueBuilder;

public class Icmpv6NDReservedCodec extends AbstractMatchCodec {
    public static final Icmpv6NDReservedCodec INSTANCE = new Icmpv6NDReservedCodec();

    private static final int VALUE_LENGTH = 4;
    public static final MatchEntrySerializerKey<?, ?> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
             EncodeConstants.OF_VERSION_1_3, EricExpClass.VALUE, Icmpv6NdReserved.VALUE);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
            EncodeConstants.OF_VERSION_1_3, EricConstants.ERICOXM_OF_EXPERIMENTER_ID,
            EricConstants.ERICOXM_OF_ICMPV6_ND_RESERVED);

    @Override
    public void serialize(final MatchEntry input, final ByteBuf outBuffer) {
        serializeHeader(input, outBuffer);
        Icmpv6NdReservedCaseValue caseValue = (Icmpv6NdReservedCaseValue) input.getMatchEntryValue();
        outBuffer.writeInt(caseValue.getIcmpv6NdReservedValues().getIcmpv6NdReserved().intValue());
    }

    @Override
    public MatchEntry deserialize(final ByteBuf message) {
        return deserializeHeaderToBuilder(message)
                .setMatchEntryValue(new Icmpv6NdReservedCaseValueBuilder()
                    .setIcmpv6NdReservedValues(new Icmpv6NdReservedValuesBuilder()
                        .setIcmpv6NdReserved(readUint32(message))
                        .build())
                    .build())
                .build();
    }

    @Override
    public int getEricFieldCode() {
        return EricConstants.ERICOXM_OF_ICMPV6_ND_RESERVED;
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
        return Icmpv6NdReserved.VALUE;
    }

    @Override
    public OxmClassBase getOxmClass() {
        return EricExpClass.VALUE;
    }
}
