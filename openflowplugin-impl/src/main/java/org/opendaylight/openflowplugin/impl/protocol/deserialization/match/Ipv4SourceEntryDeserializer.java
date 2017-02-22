/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import java.util.Objects;

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmDeserializerHelper;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;

import io.netty.buffer.ByteBuf;

public class Ipv4SourceEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        final boolean hasMask = processHeader(message);
        final Ipv4Address address = ByteBufUtils.readIetfIpv4Address(message);

        if (hasMask) {
            final byte[] mask = OxmDeserializerHelper.convertMask(message, EncodeConstants.GROUPS_IN_IPV4_ADDRESS);

            if (IpConversionUtil.isArbitraryBitMask(mask)) {
                setArbitraryMatch(builder, address, mask);
            } else {
                setPrefixMatch(builder, address, mask);
            }
        } else {
            setPrefixMatch(builder, address, null);
        }
    }

    private static void setPrefixMatch(final MatchBuilder builder, final Ipv4Address address, final byte[] mask) {
        if (Objects.isNull(builder.getLayer3Match())) {
            builder.setLayer3Match(new Ipv4MatchBuilder()
                .setIpv4Source(IpConversionUtil.createPrefix(address, mask))
                .build());
        } else if (Ipv4Match.class.isInstance(builder.getLayer3Match())
            && Objects.isNull(Ipv4Match.class.cast(builder.getLayer3Match()).getIpv4Source())) {
            builder.setLayer3Match(new Ipv4MatchBuilder(Ipv4Match.class.cast(builder.getLayer3Match()))
                .setIpv4Source(IpConversionUtil.createPrefix(address, mask))
                .build());
        } else {
            throwErrorOnMalformed(builder, "layer3Match", "ipv4Source");
        }
    }

    private static void setArbitraryMatch(final MatchBuilder builder, final Ipv4Address address,
            final byte[] mask) {
        if (Objects.isNull(builder.getLayer3Match())) {
            builder.setLayer3Match(new Ipv4MatchArbitraryBitMaskBuilder()
                    .setIpv4SourceAddressNoMask(address)
                    .setIpv4SourceArbitraryBitmask(IpConversionUtil.createArbitraryBitMask(mask))
                    .build());
        } else if (Ipv4MatchArbitraryBitMask.class.isInstance(builder.getLayer3Match())
            && Objects.isNull(Ipv4MatchArbitraryBitMask.class.cast(builder.getLayer3Match()).getIpv4SourceAddressNoMask())) {
            builder.setLayer3Match(new Ipv4MatchArbitraryBitMaskBuilder(Ipv4MatchArbitraryBitMask.class.cast(builder.getLayer3Match()))
                    .setIpv4SourceAddressNoMask(address)
                    .setIpv4SourceArbitraryBitmask(IpConversionUtil.createArbitraryBitMask(mask))
                    .build());
        } else {
            throwErrorOnMalformed(builder, "layer3Match", "ipv4SourceAddressNoMask");
        }
    }

}
