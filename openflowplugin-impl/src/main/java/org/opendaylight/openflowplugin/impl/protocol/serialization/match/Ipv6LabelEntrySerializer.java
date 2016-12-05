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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6Label;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;

public class Ipv6LabelEntrySerializer extends AbstractMatchEntrySerializer {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        super.serialize(match, outBuffer);
        final Ipv6Label ipv6Label = Ipv6Match.class.cast(match.getLayer3Match()).getIpv6Label();
        outBuffer.writeInt(ipv6Label.getIpv6Flabel().getValue().intValue());

        if (getHasMask(match)) {
            writeMask(ByteUtil.unsignedIntToBytes(
                    ipv6Label.getFlabelMask().getValue()),
                    outBuffer,
                    getValueLength());
        }
    }

    @Override
    public boolean matchTypeCheck(Match match) {
        return Objects.nonNull(match.getLayer3Match()) &&
                Ipv6Match.class.isInstance(match.getLayer3Match()) &&
                Objects.nonNull(Ipv6Match.class.cast(match.getLayer3Match()).getIpv6Label());
    }

    @Override
    protected boolean getHasMask(Match match) {
        return Objects.nonNull(Ipv6Match.class.cast(match.getLayer3Match()).getIpv6Label().getFlabelMask());
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IPV6_FLABEL;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_INT_IN_BYTES;
    }

}
