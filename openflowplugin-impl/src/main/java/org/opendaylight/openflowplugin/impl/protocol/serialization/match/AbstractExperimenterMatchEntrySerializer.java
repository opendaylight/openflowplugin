/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public abstract class AbstractExperimenterMatchEntrySerializer extends AbstractMatchEntrySerializer {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        super.serialize(match, outBuffer);
        outBuffer.writeInt(Long.valueOf(getExperimenterId()).intValue());
    }

    @Override
    public void serializeHeader(Match match, ByteBuf outBuffer) {
        outBuffer.writeShort(getOxmClassCode());

        int fieldAndMask = getOxmFieldCode() << 1;
        int length = getValueLength();

        if (getHasMask(match)) {
            fieldAndMask |= 1;
            length *= 2;
        }

        outBuffer.writeByte(fieldAndMask);

        // Add length of experimenter ID to total length
        outBuffer.writeByte(length + EncodeConstants.SIZE_OF_INT_IN_BYTES);
    }

    /**
     * @return experimenter match entry id
     */
    protected abstract long getExperimenterId();
}
