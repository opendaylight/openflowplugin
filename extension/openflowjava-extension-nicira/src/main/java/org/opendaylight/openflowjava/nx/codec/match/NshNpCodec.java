/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshNp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.np.grouping.NshNpValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.NshNpCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.NshNpCaseValueBuilder;

public class NshNpCodec extends AbstractMatchCodec {

    private static final int VALUE_LENGTH = 1;
    private static final int NXM_FIELD_CODE = 114;
    public static final MatchEntrySerializerKey<Nxm1Class, NxmNxNshNp> SERIALIZER_KEY = new MatchEntrySerializerKey<>(
            EncodeConstants.OF13_VERSION_ID, Nxm1Class.class, NxmNxNshNp.class);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY = new MatchEntryDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.NXM_1_CLASS, NXM_FIELD_CODE);

    @Override
    public void serialize(MatchEntry input, ByteBuf outBuffer) {
        serializeHeader(input, outBuffer);
        NshNpCaseValue nshNpCaseValue = ((NshNpCaseValue) input.getMatchEntryValue());
        outBuffer.writeByte(nshNpCaseValue.getNshNpValues().getValue());
    }

    @Override
    public MatchEntry deserialize(ByteBuf message) {
        MatchEntryBuilder matchEntriesBuilder = deserializeHeader(message);
        NshNpCaseValueBuilder nshNpCaseValueBuilder = new NshNpCaseValueBuilder();
        nshNpCaseValueBuilder.setNshNpValues(new NshNpValuesBuilder().setValue(message.readUnsignedByte()).build());
        matchEntriesBuilder.setMatchEntryValue(nshNpCaseValueBuilder.build());
        matchEntriesBuilder.setHasMask(false);
        return matchEntriesBuilder.build();
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
        return NxmNxNshNp.class;
    }

    @Override
    public Class<? extends OxmClassBase> getOxmClass() {
        return Nxm1Class.class;
    }
}
