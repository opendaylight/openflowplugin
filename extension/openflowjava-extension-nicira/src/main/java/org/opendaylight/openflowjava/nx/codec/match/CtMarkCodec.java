/*
 * Copyright (c) 2017 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtMark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.CtMarkCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.CtMarkCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.ct.mark.grouping.CtMarkValuesBuilder;

/**
 * Codec for CtMark.
 *
 * @author Bertrand Low.
 */
public class CtMarkCodec extends AbstractMatchCodec {
    private static final int VALUE_LENGTH = 4;
    private static final int NXM_FIELD_CODE = 107;

    public static final MatchEntrySerializerKey<Nxm1Class, NxmNxCtMark> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
            EncodeConstants.OF_VERSION_1_3, Nxm1Class.VALUE, NxmNxCtMark.VALUE);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
            EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, NXM_FIELD_CODE);

    @Override
    public void serialize(final MatchEntry input, final ByteBuf outBuffer) {
        serializeHeader(input, outBuffer);
        CtMarkCaseValue ctMarkCase = (CtMarkCaseValue) input.getMatchEntryValue();
        outBuffer.writeInt(ctMarkCase.getCtMarkValues().getCtMark().intValue());
        outBuffer.writeInt(ctMarkCase.getCtMarkValues().getMask().intValue());
    }

    @Override
    public MatchEntry deserialize(final ByteBuf message) {
        final MatchEntryBuilder matchEntryBuilder = deserializeHeaderToBuilder(message);
        CtMarkCaseValueBuilder caseBuilder = new CtMarkCaseValueBuilder();
        CtMarkValuesBuilder ctMarkValuesBuilder = new CtMarkValuesBuilder();
        if (matchEntryBuilder.getHasMask()) {
            ctMarkValuesBuilder.setMask(readUint32(message));
        }
        ctMarkValuesBuilder.setCtMark(readUint32(message));
        caseBuilder.setCtMarkValues(ctMarkValuesBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
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
    public MatchField getNxmField() {
        return NxmNxCtMark.VALUE;
    }

    @Override
    public OxmClassBase getOxmClass() {
        return Nxm1Class.VALUE;
    }
}
