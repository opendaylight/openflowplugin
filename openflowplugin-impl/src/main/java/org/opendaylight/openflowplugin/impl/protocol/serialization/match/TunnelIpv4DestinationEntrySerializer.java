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
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4Match;

public class TunnelIpv4DestinationEntrySerializer extends AbstractMatchEntrySerializer {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        super.serialize(match, outBuffer);
        // TODO: Why is this serialization exactly same as in Ipv4DestinationEntrySerializer?
        writeIpv4Prefix(((TunnelIpv4Match) match.getLayer3Match()).getTunnelIpv4Destination(), outBuffer);
    }

    @Override
    public boolean matchTypeCheck(Match match) {
        return match.getLayer3Match() != null
                && match.getLayer3Match() instanceof TunnelIpv4Match
                && ((TunnelIpv4Match) match.getLayer3Match()).getTunnelIpv4Destination() != null;
    }

    @Override
    protected boolean getHasMask(Match match) {
        return IpConversionUtil.hasIpv4Prefix(((TunnelIpv4Match) match.getLayer3Match()).getTunnelIpv4Destination())
                != null;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IPV4_DST;
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
