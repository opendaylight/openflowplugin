/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.match.ext;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.AbstractOxmMatchEntrySerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;

/**
 * Created by Anil Vishnoi (avishnoi@Brocade.com) on 7/25/16.
 */
public abstract class AbstractOxmExperimenterMatchEntrySerializer extends AbstractOxmMatchEntrySerializer {

    @Override
    public void serialize(MatchEntry entry, ByteBuf outBuffer) {
        serializeHeader(entry, outBuffer);
    }

    @Override
    public void serializeHeader(MatchEntry entry, ByteBuf outBuffer) {
        outBuffer.writeShort(getOxmClassCode());
        writeOxmFieldAndLength(outBuffer, getOxmFieldCode(), entry.isHasMask(),
                getValueLength());
    }

    protected static void writeOxmFieldAndLength(ByteBuf out, int fieldValue, boolean hasMask, int lengthArg) {
        int fieldAndMask = fieldValue << 1;
        int length = lengthArg;
        if (hasMask) {
            fieldAndMask |= 1;
            length *= 2;
        }

        //Add experimenter-id lenge
        length = length + EncodeConstants.SIZE_OF_INT_IN_BYTES;
        out.writeByte(fieldAndMask);
        out.writeByte(length);
    }

    protected ExperimenterIdCase serializeExperimenterId(MatchEntry matchEntry, ByteBuf out) {
        ExperimenterIdCase expCase = (ExperimenterIdCase) matchEntry.getMatchEntryValue();
        out.writeInt(expCase.getExperimenter().getExperimenter().getValue().intValue());
        return expCase;
    }

    /**
     * @return Experimenter match entry ID
     */
    protected abstract long getExperimenterId();
}

