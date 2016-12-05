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

public class TunnelIdEntrySerializer extends AbstractMatchEntrySerializer {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        super.serialize(match, outBuffer);
        outBuffer.writeBytes(ByteUtil.convertBigIntegerToNBytes(match.getTunnel().getTunnelId(), getValueLength()));

        if (getHasMask(match)) {
            writeMask(ByteUtil.convertBigIntegerToNBytes(
                    match.getTunnel().getTunnelMask(), getValueLength()),
                    outBuffer,
                    getValueLength());
        }
    }

    @Override
    public boolean matchTypeCheck(Match match) {
        return Objects.nonNull(match.getTunnel()) &&
                Objects.nonNull(match.getTunnel().getTunnelId());
    }

    @Override
    protected boolean getHasMask(Match match) {
        return Objects.nonNull(match.getTunnel().getTunnelMask());
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.TUNNEL_ID;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_LONG_IN_BYTES;
    }

}
