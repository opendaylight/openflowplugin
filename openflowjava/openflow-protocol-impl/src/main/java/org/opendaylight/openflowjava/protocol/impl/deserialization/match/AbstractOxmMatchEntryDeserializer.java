/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
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
    @Override
    public MatchEntry deserializeHeader(final ByteBuf input) {
        MatchEntryBuilder builder = processHeader(getOxmClass(), getOxmField(), input);
        return builder.build();
    }

    /**
     * Returns the oxm_field class.
     */
    protected abstract Class<? extends MatchField> getOxmField();

    /**
     * Returns the oxm_class class.
     */
    protected abstract Class<? extends OxmClassBase> getOxmClass();

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
    protected MatchEntryBuilder processHeader(final Class<? extends OxmClassBase> oxmClass,
            final Class<? extends MatchField> oxmField, final ByteBuf input) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(oxmClass);
        // skip oxm_class (provided)
        input.skipBytes(Short.BYTES);
        builder.setOxmMatchField(oxmField);
        boolean hasMask = (input.readUnsignedByte() & 1) != 0;
        builder.setHasMask(hasMask);
        // skip match entry length - not needed
        input.skipBytes(Byte.BYTES);
        return builder;
    }
}
