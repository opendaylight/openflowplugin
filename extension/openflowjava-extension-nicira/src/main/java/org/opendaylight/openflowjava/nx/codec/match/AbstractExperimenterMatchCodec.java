/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.match.entry.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.match.entry.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.match.entry.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxExpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.OfjAugNxExpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.NxExpMatchEntryValue;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

public abstract class AbstractExperimenterMatchCodec extends AbstractMatchCodec {

    protected static <F extends MatchField> MatchEntrySerializerKey<ExperimenterClass, F> createSerializerKey(
            final Uint8 version, final Uint32 expId, final Class<F> oxmField) {
        MatchEntrySerializerKey<ExperimenterClass, F> key = new MatchEntrySerializerKey<>(
                version, ExperimenterClass.class, oxmField);
        key.setExperimenterId(expId);
        return key;
    }

    protected static MatchEntryDeserializerKey createDeserializerKey(
            final Uint8 version, final Uint32 expId, final int fieldCode) {
        MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(
                version, OxmMatchConstants.EXPERIMENTER_CLASS, fieldCode);
        key.setExperimenterId(expId);
        return key;
    }

    @Override
    public void serialize(final MatchEntry input, final ByteBuf outBuffer) {
        // serializes standard header + experimenterId
        serializeHeader(input, outBuffer);

        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) input.getMatchEntryValue();
        OfjAugNxExpMatch ofjAugNxExpMatch = experimenterIdCase.augmentation(OfjAugNxExpMatch.class);
        NxExpMatchEntryValue nxExpMatchEntryValue = ofjAugNxExpMatch.getNxExpMatchEntryValue();

        serializeValue(nxExpMatchEntryValue, input.getHasMask(), outBuffer);
    }

    protected abstract void serializeValue(NxExpMatchEntryValue value, boolean hasMask, ByteBuf outBuffer);

    @Override
    public MatchEntry deserialize(final ByteBuf message) {
        final MatchEntryBuilder matchEntryBuilder = deserializeHeaderToBuilder(message);

        // skip experimenter Id
        message.skipBytes(Integer.BYTES);

        ExperimenterIdCaseBuilder expCaseBuilder = new ExperimenterIdCaseBuilder();
        ExperimenterBuilder expBuilder = new ExperimenterBuilder();
        expBuilder.setExperimenter(new ExperimenterId(getExperimenterId()));
        expCaseBuilder.setExperimenter(expBuilder.build());

        final NxExpMatchEntryValue value = deserializeValue(message, matchEntryBuilder.getHasMask());

        return buildMatchEntry(matchEntryBuilder, expCaseBuilder, value);
    }

    protected abstract NxExpMatchEntryValue deserializeValue(ByteBuf message, boolean hasMask);

    private static MatchEntry buildMatchEntry(final MatchEntryBuilder matchEntryBuilder,
                                              final ExperimenterIdCaseBuilder experimenterIdCaseBuilder,
                                              final NxExpMatchEntryValue nxExpMatchEntryValue) {
        return matchEntryBuilder
                .setMatchEntryValue(experimenterIdCaseBuilder.addAugmentation(new OfjAugNxExpMatchBuilder()
                    .setNxExpMatchEntryValue(nxExpMatchEntryValue)
                    .build())
                    .build())
                .build();
    }

    @Override
    public void serializeHeader(final NxmHeader input, final ByteBuf outBuffer) {
        outBuffer.writeLong(input.toLong());
    }

    @Override
    protected NxmHeader buildHeader(final boolean hasMask) {
        return new NxmHeader(
                getNxmFieldCode(),
                hasMask,
                Integer.BYTES + (hasMask ? getValueLength() * 2 : getValueLength()),
                getExperimenterId().longValue());
    }

    @Override
    public Class<? extends OxmClassBase> getOxmClass() {
        return ExperimenterClass.class;
    }

    @Override
    public int getOxmClassCode() {
        return OxmMatchConstants.EXPERIMENTER_CLASS;
    }

    protected abstract @NonNull Uint32 getExperimenterId();
}
