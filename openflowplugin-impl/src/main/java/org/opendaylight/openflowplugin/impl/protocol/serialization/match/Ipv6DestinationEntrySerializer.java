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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;
import org.opendaylight.yangtools.yang.common.Empty;

public class Ipv6DestinationEntrySerializer extends
        AbstractPolymorphicEntrySerializer<Ipv6MatchArbitraryBitMask, Ipv6Match, Ipv6Prefix, Ipv6ArbitraryMask> {

    public Ipv6DestinationEntrySerializer() {
        super(OxmMatchConstants.IPV6_DST, OxmMatchConstants.OPENFLOW_BASIC_CLASS,
            EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES, Ipv6MatchArbitraryBitMask.class, Ipv6Match.class,
            Ipv6Prefix.class, Ipv6ArbitraryMask.class);
    }

    @Override
    protected boolean useArbitraryEntry(Ipv6MatchArbitraryBitMask arbitraryMatch) {
        return arbitraryMatch.getIpv6DestinationAddressNoMask() != null;
    }

    @Override
    protected Ipv6Prefix extractNormalEntry(Ipv6Match normalMatch) {
        return normalMatch.getIpv6Destination();
    }

    @Override
    protected Ipv6ArbitraryMask extractArbitraryEntryMask(Ipv6MatchArbitraryBitMask arbitraryMatch) {
        return arbitraryMatch.getIpv6DestinationArbitraryBitmask();
    }

    @Override
    protected Empty extractNormalEntryMask(Ipv6Prefix entry) {
        return IpConversionUtil.hasIpv6Prefix(entry) != null ? Empty.getInstance() : null;
    }

    @Override
    protected void serializeArbitraryEntry(Ipv6MatchArbitraryBitMask arbitraryMatch, Ipv6ArbitraryMask mask,
            ByteBuf outBuffer) {
        writeIpv6Address(arbitraryMatch.getIpv6DestinationAddressNoMask(), outBuffer);
        if (mask != null) {
            writeMask(IpConversionUtil.convertIpv6ArbitraryMaskToByteArray(mask), outBuffer, getValueLength());
        }
    }

    @Override
    protected void serializeNormalEntry(Ipv6Prefix entry, ByteBuf outBuffer) {
        writeIpv6Prefix(entry, outBuffer);
    }
}
