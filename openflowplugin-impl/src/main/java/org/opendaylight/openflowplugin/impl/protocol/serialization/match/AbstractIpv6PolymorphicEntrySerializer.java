/*
 * Copyright (c) 2019 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;

abstract class AbstractIpv6PolymorphicEntrySerializer extends
        AbstractPolymorphicEntrySerializer<Ipv6MatchArbitraryBitMask, Ipv6Match, Ipv6Prefix, Ipv6ArbitraryMask> {

    AbstractIpv6PolymorphicEntrySerializer(final int oxmClassCode, final int oxmFieldCode) {
        super(oxmClassCode, oxmFieldCode, EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES,
            Ipv6MatchArbitraryBitMask.class, Ipv6Match.class, Ipv6Prefix.class, Ipv6ArbitraryMask.class);
    }

    @Override
    final boolean useArbitraryEntry(final Ipv6MatchArbitraryBitMask arbitraryMatch) {
        return extractArbitraryEntryAddress(arbitraryMatch) != null;
    }

    @Override
    final Integer extractNormalEntryMask(final Ipv6Prefix entry) {
        return IpConversionUtil.hasIpv6Prefix(entry);
    }

    @Override
    final void serializeArbitraryEntry(final Ipv6MatchArbitraryBitMask arbitraryMatch, final Ipv6ArbitraryMask mask,
            final ByteBuf outBuffer) {
        writeIpv6Address(extractArbitraryEntryAddress(arbitraryMatch), outBuffer);
        if (mask != null) {
            writeMask(IpConversionUtil.convertIpv6ArbitraryMaskToByteArray(mask), outBuffer,
                EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES);
        }
    }

    @Override
    final void serializeNormalEntry(final Ipv6Prefix entry, final Integer mask, final ByteBuf outBuffer) {
        writeIpv6Prefix(entry, mask, outBuffer);
    }

    abstract Ipv6Address extractArbitraryEntryAddress(@NonNull Ipv6MatchArbitraryBitMask arbitraryMatch);
}
