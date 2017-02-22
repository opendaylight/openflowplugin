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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;

import io.netty.buffer.ByteBuf;

public class Ipv6DestinationEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        final boolean hasMask = processHeader(message);
        final Ipv6Address address = ByteBufUtils.readIetfIpv6Address(message);

        if (hasMask) {
            final byte[] mask = OxmDeserializerHelper.convertMask(message, EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES);

            if (IpConversionUtil.isIpv6ArbitraryBitMask(mask)) {
                setArbitraryMatch(builder, address, mask);
            } else {
                setPrefixMatch(builder, address, mask);
            }
        } else {
            setPrefixMatch(builder, address, null);
        }
    }

    private static void setPrefixMatch(final MatchBuilder builder, final Ipv6Address address, final byte[] mask) {
        if (Objects.isNull(builder.getLayer3Match())) {
            builder.setLayer3Match(new Ipv6MatchBuilder()
                .setIpv6Destination(IpConversionUtil.createPrefix(address, mask))
                .build());
        } else if (Ipv6Match.class.isInstance(builder.getLayer3Match())
            && Objects.isNull(Ipv6Match.class.cast(builder.getLayer3Match()).getIpv6Destination())) {
            builder.setLayer3Match(new Ipv6MatchBuilder(Ipv6Match.class.cast(builder.getLayer3Match()))
                .setIpv6Destination(IpConversionUtil.createPrefix(address, mask))
                .build());
        } else {
            throwErrorOnMalformed(builder, "layer3Match", "ipv6Destination");
        }
    }

    private static void setArbitraryMatch(final MatchBuilder builder, final Ipv6Address address,
            final byte[] mask) {
        if (Objects.isNull(builder.getLayer3Match())) {
            builder.setLayer3Match(new Ipv6MatchArbitraryBitMaskBuilder()
                    .setIpv6DestinationAddressNoMask(address)
                    .setIpv6DestinationArbitraryBitmask(IpConversionUtil.createIpv6ArbitraryBitMask(mask))
                    .build());
        } else if (Ipv6MatchArbitraryBitMask.class.isInstance(builder.getLayer3Match())
            && Objects.isNull(Ipv6MatchArbitraryBitMask.class.cast(builder.getLayer3Match()).getIpv6DestinationAddressNoMask())) {
            final Ipv6MatchArbitraryBitMask match = Ipv6MatchArbitraryBitMask.class.cast(builder.getLayer3Match());
            builder.setLayer3Match(new Ipv6MatchArbitraryBitMaskBuilder(match)
                    .setIpv6DestinationAddressNoMask(address)
                    .setIpv6DestinationArbitraryBitmask(IpConversionUtil.createIpv6ArbitraryBitMask(mask))
                    .build());
        } else {
            throwErrorOnMalformed(builder, "layer3Match", "ipv6DestinationAddressNoMask");
        }
    }

}
