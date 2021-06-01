/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NshTtlCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NshTtlCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.ttl.grouping.NshTtlValuesBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class NshTtlCodec extends AbstractExperimenterMatchCodec {

    private static final int VALUE_LENGTH = Byte.BYTES;
    private static final int NXM_FIELD_CODE = 10;
    public static final MatchEntrySerializerKey<ExperimenterClass, NxmNxNshTtl> SERIALIZER_KEY =
            createSerializerKey(EncodeConstants.OF_VERSION_1_3, NiciraConstants.NX_NSH_VENDOR_ID, NxmNxNshTtl.class);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY =
            createDeserializerKey(EncodeConstants.OF_VERSION_1_3, NiciraConstants.NX_NSH_VENDOR_ID, NXM_FIELD_CODE);

    @Override
    protected void serializeValue(final NxExpMatchEntryValue value, final boolean hasMask, final ByteBuf outBuffer) {
        final var nshTtlValues = ((NshTtlCaseValue) value).getNshTtlValues();
        outBuffer.writeByte(nshTtlValues.getNshTtl().toJava());
        if (hasMask) {
            outBuffer.writeByte(nshTtlValues.getMask().toJava());
        }
    }

    @Override
    protected NxExpMatchEntryValue deserializeValue(final ByteBuf message, final boolean hasMask) {
        return new NshTtlCaseValueBuilder()
            .setNshTtlValues(new NshTtlValuesBuilder()
                .setNshTtl(readUint8(message))
                .setMask(hasMask ? readUint8(message) : null)
                .build())
            .build();
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
    public int getNxmFieldCode() {
        return NXM_FIELD_CODE;
    }

    @Override
    public Class<? extends MatchField> getNxmField() {
        return NxmNxNshTtl.class;
    }
}
