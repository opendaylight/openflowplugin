/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshNp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NshNpCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NshNpCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.np.grouping.NshNpValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.np.grouping.NshNpValuesBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class NshNpCodec extends AbstractExperimenterMatchCodec {

    private static final int VALUE_LENGTH = Byte.BYTES;
    private static final int NXM_FIELD_CODE = 3;
    public static final MatchEntrySerializerKey<ExperimenterClass, NxmNxNshNp> SERIALIZER_KEY =
            createSerializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    NiciraConstants.NX_NSH_VENDOR_ID,
                    NxmNxNshNp.VALUE);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY =
            createDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    NiciraConstants.NX_NSH_VENDOR_ID,
                    NXM_FIELD_CODE);

    @Override
    protected void serializeValue(final NxExpMatchEntryValue value, final boolean hasMask, final ByteBuf outBuffer) {
        NshNpCaseValue nshNpCaseValue = (NshNpCaseValue) value;
        NshNpValues nshNpValues = nshNpCaseValue.getNshNpValues();
        outBuffer.writeByte(nshNpValues.getValue().toJava());
    }

    @Override
    protected NxExpMatchEntryValue deserializeValue(final ByteBuf message, final boolean hasMask) {
        return new NshNpCaseValueBuilder()
                .setNshNpValues(new NshNpValuesBuilder().setValue(readUint8(message)).build())
                .build();
    }

    @Override
    public int getNxmFieldCode() {
        return NXM_FIELD_CODE;
    }

    @Override
    protected Uint32 getExperimenterId() {
        return NiciraConstants.NX_NSH_VENDOR_ID;
    }

    @Override
    public int getValueLength() {
        return VALUE_LENGTH;
    }

    @Override
    public MatchField getNxmField() {
        return NxmNxNshNp.VALUE;
    }
}
