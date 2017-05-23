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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMask;

public class Ipv6SourceEntrySerializer extends AbstractMatchEntrySerializer {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        super.serialize(match, outBuffer);

        if (isPrefix(match)) {
            writeIpv6Prefix(Ipv6Match.class.cast(match.getLayer3Match()).getIpv6Source(), outBuffer);
        } else if (isArbitrary(match)) {
            final Ipv6MatchArbitraryBitMask ipv6 = Ipv6MatchArbitraryBitMask.class.cast(match.getLayer3Match());
            writeIpv6Address(ipv6.getIpv6SourceAddressNoMask(), outBuffer);

            if (getHasMask(match)) {
                writeMask(IpConversionUtil.convertIpv6ArbitraryMaskToByteArray(ipv6.getIpv6SourceArbitraryBitmask()),
                        outBuffer,
                        getValueLength());
            }
        }
    }

    @Override
    public boolean matchTypeCheck(Match match) {
        if (isPrefix(match)) {
            return Objects.nonNull(match.getLayer3Match()) &&
                    Objects.nonNull(Ipv6Match.class.cast(match.getLayer3Match()).getIpv6Source());
        } else if (isArbitrary(match)) {

            return Objects.nonNull(match.getLayer3Match()) &&
                    Objects.nonNull(Ipv6MatchArbitraryBitMask.class.cast(match.getLayer3Match()).getIpv6SourceAddressNoMask());
        }

        return false;
    }

    @Override
    protected boolean getHasMask(Match match) {
        if (isPrefix(match)) {
            if (null != IpConversionUtil.hasIpv6Prefix(Ipv6Match.class.cast(match.getLayer3Match()).getIpv6Source())) {
                return Objects.nonNull(IpConversionUtil
                        .extractIpv6Prefix(Ipv6Match.class.cast(match.getLayer3Match()).getIpv6Source()));
            }
        } else if (isArbitrary(match)) {
            return Objects.nonNull(Ipv6MatchArbitraryBitMask.class.cast(match.getLayer3Match()).getIpv6SourceArbitraryBitmask());
        }

        return false;
    }

    private static boolean isPrefix(Match match) {
        return Ipv6Match.class.isInstance(match.getLayer3Match());
    }

    private static boolean isArbitrary(Match match) {
        return Ipv6MatchArbitraryBitMask.class.isInstance(match.getLayer3Match());
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IPV6_SRC;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES;
    }

}
