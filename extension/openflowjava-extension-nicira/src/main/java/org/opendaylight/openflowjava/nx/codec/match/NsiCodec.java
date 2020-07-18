/*
 * Copyright (c) 2014 Red Hat, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNsi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsi.grouping.NsiValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsi.grouping.NsiValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NsiCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NsiCaseValueBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

public class NsiCodec extends AbstractExperimenterMatchCodec {

    private static final int VALUE_LENGTH = EncodeConstants.SIZE_OF_BYTE_IN_BYTES;
    private static final int NXM_FIELD_CODE = 5;
    public static final MatchEntrySerializerKey<ExperimenterClass, NxmNxNsi> SERIALIZER_KEY =
            createSerializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    NiciraConstants.NX_NSH_VENDOR_ID,
                    NxmNxNsi.class);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY =
            createDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    NiciraConstants.NX_NSH_VENDOR_ID,
                    NXM_FIELD_CODE);

    @Override
    protected void serializeValue(final NxExpMatchEntryValue value, final boolean hasMask, final ByteBuf outBuffer) {
        NsiCaseValue nsiCaseValue = (NsiCaseValue) value;
        NsiValues nsiValues = nsiCaseValue.getNsiValues();
        outBuffer.writeByte(nsiValues.getNsi().toJava());
        if (hasMask) {
            outBuffer.writeByte(nsiValues.getMask().toJava());
        }
    }

    @Override
    protected NxExpMatchEntryValue deserializeValue(final ByteBuf message, final boolean hasMask) {
        Uint8 value = readUint8(message);
        Uint8 mask = hasMask ? readUint8(message) : null;
        NsiValues nsiValues = new NsiValuesBuilder().setNsi(value).setMask(mask).build();
        return new NsiCaseValueBuilder().setNsiValues(nsiValues).build();
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
    public Class<? extends MatchField> getNxmField() {
        return NxmNxNsi.class;
    }
}
