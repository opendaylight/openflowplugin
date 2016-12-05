/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public class TcpFlagsEntrySerializer extends AbstractExperimenterMatchEntrySerializer {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        super.serialize(match, outBuffer);
        outBuffer.writeShort(match.getTcpFlagsMatch().getTcpFlags());

        if (getHasMask(match)) {
            writeMask(ByteUtil.unsignedShortToBytes(
                    match.getTcpFlagsMatch().getTcpFlagsMask()),
                    outBuffer,
                    getValueLength());
        }
    }

    @Override
    public boolean matchTypeCheck(Match match) {
        return Objects.nonNull(match.getTcpFlagsMatch()) &&
                Objects.nonNull(match.getTcpFlagsMatch().getTcpFlags());
    }

    @Override
    protected boolean getHasMask(Match match) {
        return Objects.nonNull(match.getTcpFlagsMatch().getTcpFlagsMask());
    }

    @Override
    protected long getExperimenterId() {
        return EncodeConstants.ONF_EXPERIMENTER_ID;
    }

    @Override
    protected int getOxmFieldCode() {
        return EncodeConstants.ONFOXM_ET_TCP_FLAGS;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.EXPERIMENTER_CLASS;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_SHORT_IN_BYTES;
    }

}
