/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import static java.util.Objects.requireNonNull;

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

    private final Class<? extends MatchField> nxmField;
    private final Class<? extends OxmClassBase> oxmClass;
    private final NxmHeader headerWithMask;
    private final NxmHeader headerWithoutMask;

    protected AbstractMatchCodec(Class<? extends OxmClassBase> oxmClass, Class<? extends MatchField> nxmField,
            NxmHeader headerWithMask, NxmHeader headerWithoutMask) {
        this.oxmClass = requireNonNull(oxmClass);
        this.nxmField = requireNonNull(nxmField);
        this.headerWithMask = requireNonNull(headerWithMask);
        this.headerWithoutMask = requireNonNull(headerWithoutMask);
    }

    protected MatchEntryBuilder deserializeHeaderToBuilder(ByteBuf message) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(oxmClass);
        // skip oxm_class - provided
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        builder.setOxmMatchField(nxmField);
        boolean hasMask = (message.readUnsignedByte() & 1) != 0;
        builder.setHasMask(hasMask);
        // skip match entry length - not needed
        message.skipBytes(EncodeConstants.SIZE_OF_BYTE_IN_BYTES);
        return builder;
    }

    @Override
    public MatchEntry deserializeHeader(ByteBuf message) {
        return deserializeHeaderToBuilder(message).build();
    }

    @Override
    public void serializeHeader(MatchEntry input, ByteBuf outBuffer) {
        serializeHeader(input.isHasMask() ? headerWithMask : headerWithoutMask, outBuffer);
    }

    public void serializeHeader(NxmHeader input, ByteBuf outBuffer) {
        outBuffer.writeInt((int) input.toLong());
    }

    protected NxmHeader buildHeader(boolean hasMask) {
        return new NxmHeader(
                getOxmClassCode(),
                getNxmFieldCode(),
                hasMask,
                hasMask ? getValueLength() * 2 : getValueLength()
        );
    }

    public final NxmHeader getHeaderWithoutHasMask() {
        return headerWithoutMask;
    }

    public final NxmHeader getHeaderWithHasMask() {
        return headerWithMask;
    }

    /**
     * Returns the numeric representation of nxm_field.
     */
    public abstract int getNxmFieldCode();

    /**
     * Returns the numeric representation of oxm_class.
     */
    public abstract int getOxmClassCode();

    /**
     * Returns the match entry value length.
     */
    public abstract int getValueLength();

    /**
     * Returns the nxm_field class.
     */
    public final Class<? extends MatchField> getNxmField() {
        return nxmField;
    }

    /**
     * Returns the oxm_class class.
     */
    public final Class<? extends OxmClassBase> getOxmClass() {
        return oxmClass;
    }
}
