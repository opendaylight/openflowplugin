/*
 * Copyright (c) 2017 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.match;

import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.ct.label.grouping.CtLabelValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.CtLabelCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.CtLabelCaseValueBuilder;

public class CtLabelCodec extends AbstractMatchCodec {

    private static final int VALUE_LENGTH = 32;
    private static final int NXM_FIELD_CODE = 108;
    public static final MatchEntrySerializerKey<Nxm1Class, NxmNxCtLabel> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
            EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxCtLabel.class);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, NXM_FIELD_CODE);

    @Override
    public void serialize(MatchEntry input, ByteBuf outBuffer) {
        serializeHeader(input, outBuffer);
        CtLabelCaseValue ctLabelCase = ((CtLabelCaseValue) input.getMatchEntryValue());

        BigInteger value = ctLabelCase.getCtLabelValues().getCtLabel();
        BigInteger mask = ctLabelCase.getCtLabelValues().getMask();

        outBuffer.writeZero(8); // pad to 128 bits
        outBuffer.writeLong(value.longValue());
        outBuffer.writeZero(8); // pad to 128 bits
        outBuffer.writeLong(mask.longValue());
    }

    @Override
    public MatchEntry deserialize(ByteBuf message) {
        MatchEntryBuilder matchEntryBuilder = deserializeHeaderToBuilder(message);
        CtLabelCaseValueBuilder caseBuilder = new CtLabelCaseValueBuilder();
        CtLabelValuesBuilder ctLabelValuesBuilder = new CtLabelValuesBuilder();
        message.readLong(); // remove padding leading 0s
        ctLabelValuesBuilder.setCtLabel(BigInteger.valueOf(message.readLong()));
        message.readLong(); // remove padding leading 0s
        ctLabelValuesBuilder.setMask(BigInteger.valueOf(message.readLong()));
        caseBuilder.setCtLabelValues(ctLabelValuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        matchEntryBuilder.setHasMask(true);
        return matchEntryBuilder.build();
    }

    @Override
    public int getNxmFieldCode() {
        return NXM_FIELD_CODE;
    }

    @Override
    public int getOxmClassCode() {
        return OxmMatchConstants.NXM_1_CLASS;
    }

    @Override
    public int getValueLength() {
        return VALUE_LENGTH;
    }

    @Override
    public Class<? extends MatchField> getNxmField() {
        return NxmNxCtLabel.class;
    }

    @Override
    public Class<? extends OxmClassBase> getOxmClass() {
        return Nxm1Class.class;
    }

}
