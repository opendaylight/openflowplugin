/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;

public abstract class AbstractMatchEntrySerializer implements OFSerializer<Match>, HeaderSerializer<Match> {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        outBuffer.writeShort(getOxmClassCode());
        writeOxmFieldAndLength(outBuffer, getOxmFieldCode(), getHasMask(match), getValueLength());
    }

    @Override
    public void serializeHeader(Match match, ByteBuf outBuffer) {
        serialize(match, outBuffer);
    }

    protected static void writeMask(byte[] mask, ByteBuf out, int length) {
        if (mask != null && mask.length != length) {
            throw new IllegalArgumentException("incorrect length of mask: "+
                    mask.length + ", expected: " + length);
        }

        out.writeBytes(mask);
    }

    private void writeOxmFieldAndLength(ByteBuf out, int fieldValue, boolean hasMask, int lengthArg) {
        int fieldAndMask = fieldValue << 1;
        int length = lengthArg;

        if (hasMask) {
            fieldAndMask |= 1;
            length *= 2;
        }

        out.writeShort(OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        out.writeByte(fieldAndMask);
        out.writeByte(length);
    }

    /**
     * Checks if current match is this match type
     * @param match Openflow match
     * @return true if matched
     */
    abstract boolean matchTypeCheck(final Match match);

    /**
     * @param match Openflow match
     * @return if field has or has not mask
     */
    protected abstract boolean getHasMask(final Match match);

    /**
     * @return numeric representation of oxm_field
     */
    protected abstract int getOxmFieldCode();

    /**
     * @return numeric representation of oxm_class
     */
    protected abstract int getOxmClassCode();

    /**
     * @return match entry value length (without mask length)
     */
    protected abstract int getValueLength();
}
