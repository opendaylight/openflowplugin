/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMask;
import org.opendaylight.yangtools.yang.common.Empty;

public class Ipv4SourceEntrySerializer
        extends AbstractPolymorphicEntrySerializer<Ipv4MatchArbitraryBitMask, Ipv4Match, Ipv4Prefix, DottedQuad> {
    public Ipv4SourceEntrySerializer() {
        super(OxmMatchConstants.IPV4_SRC, OxmMatchConstants.OPENFLOW_BASIC_CLASS, EncodeConstants.SIZE_OF_INT_IN_BYTES,
            Ipv4MatchArbitraryBitMask.class, Ipv4Match.class, Ipv4Prefix.class, DottedQuad.class);
    }

    @Override
    protected boolean useArbitraryEntry(Ipv4MatchArbitraryBitMask arbitraryMatch) {
        return arbitraryMatch.getIpv4SourceAddressNoMask() != null;
    }

    @Override
    protected Ipv4Prefix extractNormalEntry(Ipv4Match normalMatch) {
        return normalMatch.getIpv4Source();
    }

    @Override
    protected DottedQuad extractArbitraryEntryMask(Ipv4MatchArbitraryBitMask arbitraryMatch) {
        return arbitraryMatch.getIpv4SourceArbitraryBitmask();
    }

    @Override
    protected Empty extractNormalEntryMask(Ipv4Prefix entry) {
        return AbstractIpv4PrefixEntrySerializer.extractMask(entry);
    }

    @Override
    protected void serializeArbitraryEntry(Ipv4MatchArbitraryBitMask arbitraryMatch, DottedQuad mask,
            ByteBuf outBuffer) {
        writeIpv4Address(arbitraryMatch.getIpv4SourceAddressNoMask(), outBuffer);
        if (mask != null) {
            writeMask(IpConversionUtil.convertArbitraryMaskToByteArray(mask), outBuffer, getValueLength());
        }
    }

    @Override
    protected void serializeNormalEntry(@NonNull Ipv4Prefix entry, @NonNull ByteBuf outBuffer) {
        writeIpv4Prefix(entry, outBuffer);
    }
}
