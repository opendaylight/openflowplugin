/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extensions.hpe;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.OfjAugHpeMatchBuilder;

public abstract class HpeAbstractCodec implements OFSerializer<MatchEntry>, OFDeserializer<MatchEntry>,
        HeaderSerializer<MatchEntry>, HeaderDeserializer<MatchEntry> {
    private final Class<? extends MatchField> matchEntryClass;
    private final byte oxmField;
    private final short length;

    protected HpeAbstractCodec(byte oxmField, Class<? extends MatchField> matchEntryClass, short length) {
        this.oxmField = oxmField;
        this.matchEntryClass = matchEntryClass;
        this.length = length;
    }

    protected MatchEntryBuilder deserializeHeaderBuilder(ByteBuf message) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(ExperimenterClass.class);
        // skip oxm_class - provided
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        builder.setOxmMatchField(this.matchEntryClass);
        boolean hasMask = (message.readUnsignedByte() & 1) != 0;
        builder.setHasMask(hasMask);
        // skip match entry length - not needed
        message.skipBytes(EncodeConstants.SIZE_OF_BYTE_IN_BYTES);
        message.skipBytes(EncodeConstants.SIZE_OF_INT_IN_BYTES);
        return builder;
    }

    public MatchEntry deserializeHeader(ByteBuf message) {
        return deserializeHeaderBuilder(message).build();
    }

    public void serializeHeader(ByteBuf outBuffer, boolean hasMask) {
        Long header = ((((long) EncodeConstants.EXPERIMENTER_VALUE) << 16)
                | (this.oxmField << 9) | ((hasMask ? 1 : 0) << 8) | (this.length));
        outBuffer.writeInt(header.intValue());
        outBuffer.writeInt(HpeExtensionProviderImpl.HP_EXP_ID.intValue());
    }

    public void serializeHeader(MatchEntry input, ByteBuf outBuffer) {
        serializeHeader(outBuffer, true);
    }

    public MatchEntrySerializerKey<ExperimenterClass, ? extends MatchField> getMatchEntrySerializerKey() {
        MatchEntrySerializerKey<ExperimenterClass, ? extends MatchField> matchEntrySerializerKey =
                new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID,
                        ExperimenterClass.class, matchEntryClass);
        matchEntrySerializerKey.setExperimenterId(HpeExtensionProviderImpl.HP_EXP_ID);
        return matchEntrySerializerKey;
    }

    public MatchEntryDeserializerKey getMatchEntryDeserializerKey() {
        MatchEntryDeserializerKey matchEntryDeserializerKey = new MatchEntryDeserializerKey(
                EncodeConstants.OF13_VERSION_ID, OxmMatchConstants.EXPERIMENTER_CLASS, oxmField);
        matchEntryDeserializerKey.setExperimenterId(HpeExtensionProviderImpl.HP_EXP_ID);
        return matchEntryDeserializerKey;
    }

    @Override
    public MatchEntry deserialize(ByteBuf message) {
        MatchEntryBuilder matchEntryBuilder = deserializeHeaderBuilder(message);
        ExperimenterIdCaseBuilder experimenterIdCaseBuilder = new ExperimenterIdCaseBuilder();
        ExperimenterBuilder experimenterBuilder = new ExperimenterBuilder();
        OfjAugHpeMatchBuilder ofjAugHpeMatchBuilder = new OfjAugHpeMatchBuilder();
        deserializePayload(message, ofjAugHpeMatchBuilder);
        experimenterBuilder.setExperimenter(new ExperimenterId(HpeExtensionProviderImpl.HP_EXP_ID));
        experimenterBuilder.addAugmentation(OfjAugHpeMatch.class, ofjAugHpeMatchBuilder.build());
        experimenterIdCaseBuilder.setExperimenter(experimenterBuilder.build());
        matchEntryBuilder.setMatchEntryValue(experimenterIdCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    protected abstract void deserializePayload(ByteBuf message, OfjAugHpeMatchBuilder ofjAugHpeMatchBuilder);

    @Override
    public void serialize(MatchEntry input, ByteBuf outBuffer) {
        serializeHeader(outBuffer, input.isHasMask());
        ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) input.getMatchEntryValue();
        Experimenter experimenter = experimenterIdCase.getExperimenter();
        OfjAugHpeMatch ofjAugHpeMatch = experimenter.getAugmentation(OfjAugHpeMatch.class);
        serializePayload(outBuffer, ofjAugHpeMatch);
    }

    protected abstract void serializePayload(ByteBuf outBuffer, OfjAugHpeMatch ofjAugHpeMatch);
}
