/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
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

    protected MatchEntryBuilder deserializeHeaderToBuilder(ByteBuf message) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(getOxmClass());
        // skip oxm_class - provided
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        builder.setOxmMatchField(getEricField());
        boolean hasMask = (message.readUnsignedByte() & 1) != 0;
        builder.setHasMask(hasMask);
        // skip experimenter class, match length and experimenter id - not needed
        message.skipBytes(EncodeConstants.SIZE_OF_BYTE_IN_BYTES);
        return builder;
    }

    @Override
    public MatchEntry deserializeHeader(ByteBuf message) {
        return deserializeHeaderToBuilder(message).build();
    }

    @Override
    public void serializeHeader(MatchEntry input, ByteBuf outBuffer) {
        outBuffer.writeInt(serializeHeaderToLong(input.isHasMask()).intValue());
    }

    private Long serializeHeaderToLong(boolean hasMask) {
        if (hasMask) {
            return getHeaderWithHasMask().toLong();
        }
        return getHeaderWithoutHasMask().toLong();
    }

    public EricHeader getHeaderWithoutHasMask() {
        if (headerWithoutMask == null) {
            headerWithoutMask = new EricHeader(getOxmClassCode(), getEricFieldCode(), false, getValueLength());
        }
        return headerWithoutMask;
    }

    public EricHeader getHeaderWithHasMask() {
        if (headerWithMask == null) {
            headerWithMask = new EricHeader(getOxmClassCode(), getEricFieldCode(), true, getValueLength());
        }
        return headerWithMask;
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