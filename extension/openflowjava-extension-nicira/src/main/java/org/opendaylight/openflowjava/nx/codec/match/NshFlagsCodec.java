/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.flags.grouping.NshFlagsValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.nsh.flags.grouping.NshFlagsValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshFlagsCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshFlagsCaseValueBuilder;

public class NshFlagsCodec extends AbstractExperimenterMatchCodec {

    private static final int VALUE_LENGTH = EncodeConstants.SIZE_OF_BYTE_IN_BYTES;
    private static final int NXM_FIELD_CODE = 1;
    public static final MatchEntrySerializerKey<ExperimenterClass, NxmNxNshFlags> SERIALIZER_KEY =
            createSerializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    NiciraConstants.NX_NSH_VENDOR_ID,
                    NxmNxNshFlags.class);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY =
            createDeserializerKey(
                    EncodeConstants.OF13_VERSION_ID,
                    NiciraConstants.NX_NSH_VENDOR_ID,
                    NXM_FIELD_CODE);

    @Override
    public int getNxmFieldCode() {
        return NXM_FIELD_CODE;
    }

    @Override
    public Class<? extends MatchField> getNxmField() {
        return NxmNxNshFlags.class;
    }

    @Override
    public int getValueLength() {
        return VALUE_LENGTH;
    }

    @Override
    protected NxExpMatchEntryValue deserializeValue(ByteBuf message, boolean hasMask) {
        Short flagsValue = message.readUnsignedByte();
        Short maskValue = hasMask ? message.readUnsignedByte() : null;
        NshFlagsValues flagsValues = new NshFlagsValuesBuilder().setNshFlags(flagsValue).setMask(maskValue).build();
        return new NshFlagsCaseValueBuilder().setNshFlagsValues(flagsValues).build();
    }

    @Override
    protected long getExperimenterId() {
        return NiciraConstants.NX_NSH_VENDOR_ID;
    }

    @Override
    protected void serializeValue(NxExpMatchEntryValue value, boolean hasMask, ByteBuf outBuffer) {
        NshFlagsCaseValue nshFlagsCaseValue = (NshFlagsCaseValue) value;
        NshFlagsValues nshFlagsValues = nshFlagsCaseValue.getNshFlagsValues();
        outBuffer.writeByte(nshFlagsValues.getNshFlags());
        if (hasMask) {
            outBuffer.writeByte(nshFlagsValues.getMask());
        }
    }
}
