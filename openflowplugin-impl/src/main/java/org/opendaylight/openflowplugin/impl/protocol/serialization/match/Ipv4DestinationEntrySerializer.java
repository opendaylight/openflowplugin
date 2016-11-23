/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;

public class Ipv4DestinationEntrySerializer extends AbstractMatchEntrySerializer {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        super.serialize(match, outBuffer);
        writeIpv4Prefix(Ipv4Match.class.cast(match.getLayer3Match()).getIpv4Destination(), outBuffer);
    }

    @Override
    boolean matchTypeCheck(Match match) {
        return Objects.nonNull(match.getLayer3Match()) &&
                Ipv4Match.class.isInstance(match.getLayer3Match()) &&
                Objects.nonNull(Ipv4Match.class.cast(match.getLayer3Match()).getIpv4Destination());
    }

    @Override
    protected boolean getHasMask(Match match) {
        // Split address to IP and mask
        final Iterator<String> addressParts = IpConversionUtil.splitToParts(
                Ipv4Match.class.cast(match.getLayer3Match()).getIpv4Destination());
        addressParts.next();

        // Check if we have mask
        return addressParts.hasNext() && Integer.parseInt(addressParts.next()) < 32;
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
