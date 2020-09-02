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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMask;

abstract class AbstractIpv4PolymorphicEntrySerializer
        extends AbstractPolymorphicEntrySerializer<Ipv4MatchArbitraryBitMask, Ipv4Match, Ipv4Prefix, DottedQuad> {
    AbstractIpv4PolymorphicEntrySerializer(final int oxmClassCode, final int oxmFieldCode) {
        super(oxmClassCode, oxmFieldCode, Integer.BYTES, Ipv4MatchArbitraryBitMask.class,
            Ipv4Match.class, Ipv4Prefix.class, DottedQuad.class);
    }

    @Override
    final boolean useArbitraryEntry(final Ipv4MatchArbitraryBitMask arbitraryMatch) {
        return extractArbitraryEntryAddress(arbitraryMatch) != null;
    }

    @Override
    final Integer extractNormalEntryMask(final Ipv4Prefix entry) {
        return IpConversionUtil.hasIpv4Prefix(entry);
    }

    @Override
    final void serializeArbitraryEntry(final Ipv4MatchArbitraryBitMask arbitraryMatch, final DottedQuad mask,
            final ByteBuf outBuffer) {
        writeIpv4Address(extractArbitraryEntryAddress(arbitraryMatch), outBuffer);
        if (mask != null) {
            writeMask(IpConversionUtil.convertArbitraryMaskToByteArray(mask), outBuffer,
                Integer.BYTES);
        }
    }

    @Override
    final void serializeNormalEntry(final Ipv4Prefix entry, final Integer mask, final ByteBuf outBuffer) {
        writeIpv4Prefix(entry, mask, outBuffer);
    }

    abstract Ipv4Address extractArbitraryEntryAddress(@NonNull Ipv4MatchArbitraryBitMask arbitraryMatch);
}
