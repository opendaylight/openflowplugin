/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxCtState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.CtStateCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.CtStateCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.ct.state.grouping.CtStateValuesBuilder;

/**
 * Codec for CtSate.
 *
 * @author Aswin Suryanarayanan.
 */
public class CtStateCodec extends AbstractMatchCodec {

    private static final int VALUE_LENGTH = 4;
    private static final int NXM_FIELD_CODE = 105;
    public static final MatchEntrySerializerKey<Nxm1Class, NxmNxCtState> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
            EncodeConstants.OF_VERSION_1_3, Nxm1Class.class, NxmNxCtState.class);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
            EncodeConstants.OF_VERSION_1_3, OxmMatchConstants.NXM_1_CLASS, NXM_FIELD_CODE);

    @Override
    public void serialize(final MatchEntry input, final ByteBuf outBuffer) {
        serializeHeader(input, outBuffer);
        CtStateCaseValue ctStateCase = (CtStateCaseValue) input.getMatchEntryValue();
        outBuffer.writeInt(ctStateCase.getCtStateValues().getCtState().intValue());
        outBuffer.writeInt(ctStateCase.getCtStateValues().getMask().intValue());
    }

    @Override
    public MatchEntry deserialize(final ByteBuf message) {
        return deserializeHeaderToBuilder(message)
                .setHasMask(true)
                .setMatchEntryValue(new CtStateCaseValueBuilder()
                    .setCtStateValues(new CtStateValuesBuilder()
                        .setCtState(readUint32(message))
                        .setMask(readUint32(message))
                        .build())
                    .build())
                .build();
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
        return NxmNxCtState.class;
    }

    @Override
    public Class<? extends OxmClassBase> getOxmClass() {
        return Nxm1Class.class;
    }
}
