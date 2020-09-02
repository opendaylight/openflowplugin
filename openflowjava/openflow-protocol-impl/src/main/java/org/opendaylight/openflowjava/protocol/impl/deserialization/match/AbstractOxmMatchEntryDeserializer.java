/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;

/**
 * Base class for an Oxm match entry deserializer.
 *
 * @author michal.polkorab
 */
public abstract class AbstractOxmMatchEntryDeserializer
        implements HeaderDeserializer<MatchEntry>, OFDeserializer<MatchEntry> {
    private final @NonNull Class<? extends OxmClassBase> oxmClass;
    private final @NonNull Class<? extends MatchField> oxmField;

    protected AbstractOxmMatchEntryDeserializer(final Class<? extends OxmClassBase> oxmClass,
            final Class<? extends MatchField> oxmField) {
        this.oxmClass = requireNonNull(oxmClass);
        this.oxmField = requireNonNull(oxmField);
    }

    protected AbstractOxmMatchEntryDeserializer(final Class<? extends MatchField> oxmField) {
        this(OpenflowBasicClass.class, oxmField);
    }

    @Override
    public final MatchEntry deserialize(final ByteBuf input) {
        final MatchEntryBuilder builder = processHeader(input);
        deserialize(input, builder);
        return builder.build();
    }

    protected abstract void deserialize(ByteBuf input, MatchEntryBuilder builder);

    @Override
    public final MatchEntry deserializeHeader(final ByteBuf input) {
        return processHeader(input).build();
    }

    /**
     * Prepares match entry header - sets oxm_class, oxm_field, hasMask
     *  + sets the buffer.readerIndex() to the end of match entry
     *  - where augmentation starts.
     *
     * @param oxmClass oxm class type
     * @param oxmField oxm field type
     * @param input input bytebuf
     * @return MatchEntriesBuilder which can be filled with MatchEntry augmentation
     */
    protected final MatchEntryBuilder processHeader(final ByteBuf input) {
        // skip oxm_class (provided)
        input.skipBytes(Short.BYTES);
        final MatchEntryBuilder builder = new MatchEntryBuilder().setOxmClass(oxmClass).setOxmMatchField(oxmField)
                .setHasMask((input.readUnsignedByte() & 1) != 0);
        // skip match entry length - not needed
        input.skipBytes(Byte.BYTES);
        return builder;
    }
}
