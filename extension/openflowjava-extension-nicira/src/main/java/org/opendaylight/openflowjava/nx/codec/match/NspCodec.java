/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsp.grouping.NspValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsp.grouping.NspValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NspCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NspCaseValueBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class NspCodec extends AbstractExperimenterMatchCodec {

    private static final int VALUE_LENGTH = Integer.BYTES;
    private static final int NXM_FIELD_CODE = 4;
    public static final MatchEntrySerializerKey<ExperimenterClass, NxmNxNsp> SERIALIZER_KEY =
            createSerializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    NiciraConstants.NX_NSH_VENDOR_ID,
                    NxmNxNsp.class);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY =
            createDeserializerKey(
                EncodeConstants.OF13_VERSION_ID,
                NiciraConstants.NX_NSH_VENDOR_ID,
                NXM_FIELD_CODE);

    @Override
    protected void serializeValue(NxExpMatchEntryValue value, boolean hasMask, ByteBuf outBuffer) {
        NspCaseValue nspCaseValue = (NspCaseValue) value;
        NspValues nspValues = nspCaseValue.getNspValues();
        outBuffer.writeInt(nspValues.getNsp().intValue());
        if (hasMask) {
            outBuffer.writeInt(nspValues.getMask().intValue());
        }
    }

    @Override
    protected NxExpMatchEntryValue deserializeValue(ByteBuf message, boolean hasMask) {
        Uint32 nspValue = readUint32(message);
        Uint32 maskValue = hasMask ? readUint32(message) : null;
        return new NspCaseValueBuilder()
                .setNspValues(new NspValuesBuilder().setNsp(nspValue).setMask(maskValue).build())
                .build();
    }

    @Override
    protected Uint32 getExperimenterId() {
        return NiciraConstants.NX_NSH_VENDOR_ID;
    }

    @Override
    public int getNxmFieldCode() {
        return NXM_FIELD_CODE;
    }

    @Override
    public int getValueLength() {
        return VALUE_LENGTH;
    }

    @Override
    public Class<? extends MatchField> getNxmField() {
        return NxmNxNsp.class;
    }
}
