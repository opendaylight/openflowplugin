/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.eric.codec.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;

public abstract class AbstractMatchCodec implements
        OFSerializer<MatchEntry>,
        OFDeserializer<MatchEntry>,
        HeaderSerializer<MatchEntry>,
        HeaderDeserializer<MatchEntry> {

    protected EricHeader headerWithMask;
    protected EricHeader headerWithoutMask;

    protected MatchEntryBuilder deserializeHeaderToBuilder(final ByteBuf message) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(getOxmClass());
        // skip oxm_class - provided
        message.skipBytes(Short.BYTES);
        builder.setOxmMatchField(getEricField());
        boolean hasMask = (message.readUnsignedByte() & 1) != 0;
        builder.setHasMask(hasMask);
        // skip experimenter class, match length and experimenter id - not needed
        message.skipBytes(Byte.BYTES);
        return builder;
    }

    @Override
    public MatchEntry deserializeHeader(final ByteBuf message) {
        return deserializeHeaderToBuilder(message).build();
    }

    @Override
    public void serializeHeader(final MatchEntry input, final ByteBuf outBuffer) {
        serializeHeader(getHeader(input.getHasMask()), outBuffer);
    }

    public void serializeHeader(final EricHeader input, final ByteBuf outBuffer) {
        outBuffer.writeInt((int) input.toLong());
    }

    protected EricHeader getHeader(final boolean hasMask) {
        if (hasMask) {
            if (headerWithMask == null) {
                headerWithMask = buildHeader(hasMask);
            }
            return headerWithMask;
        } else {
            if (headerWithoutMask == null) {
                headerWithoutMask = buildHeader(hasMask);
            }
            return headerWithoutMask;
        }
    }

    protected EricHeader buildHeader(final boolean hasMask) {
        return new EricHeader(
                getOxmClassCode(),
                getEricFieldCode(),
                hasMask,
                hasMask ? getValueLength() * 2 : getValueLength()
        );
    }

    public EricHeader getHeaderWithoutHasMask() {
        return getHeader(false);
    }

    public EricHeader getHeaderWithHasMask() {
        return getHeader(true);
    }

    /**
     * Returns the numeric representation of eric_field.
     */
    public abstract int getEricFieldCode();

    /**
     * Returns the numeric representation of oxm_class.
     */
    public abstract int getOxmClassCode();

    /**
     * Returns the match entry value length.
     */
    public abstract int getValueLength();

    /**
     * Returns the eric_field class.
     */
    public abstract Class<? extends MatchField> getEricField();

    /**
     * Returns the oxm_class class.
     */
    public abstract Class<? extends OxmClassBase> getOxmClass();
}
