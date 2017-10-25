/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.src._case.Ipv6Src;

public class OfToSalIpv6SrcCase extends ConvertorCase<Ipv6SrcCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalIpv6SrcCase() {
        super(Ipv6SrcCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull Ipv6SrcCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final Ipv6MatchBuilder ipv6MatchBuilder = data.getIpv6MatchBuilder();
        final Ipv6MatchArbitraryBitMaskBuilder ipv6MatchArbitraryBitMaskBuilder = data.getIpv6MatchArbitraryBitMaskBuilder();

        Ipv6Src ipv6Src = source.getIpv6Src();

        if (ipv6Src != null) {
            byte[] mask = ipv6Src.getMask();
            if (mask != null && IpConversionUtil.isIpv6ArbitraryBitMask(mask)) {
                // case where ipv6dst is of type ipv6MatchBuilder and ipv6src is of type ipv6MatchArbitrary.
                // Need to convert ipv6dst to ipv6MatchArbitrary.

                if (ipv6MatchBuilder.getIpv6Destination() != null) {
                    Ipv6Prefix ipv6PrefixDestinationAddress = ipv6MatchBuilder.getIpv6Destination();
                    Ipv6Address ipv6DstAddress = IpConversionUtil.extractIpv6Address(ipv6PrefixDestinationAddress);
                    Ipv6ArbitraryMask dstIpv6ArbitraryMask = IpConversionUtil.compressedIpv6MaskFormat(
                            IpConversionUtil.extractIpv6AddressMask(ipv6PrefixDestinationAddress));
                    setDstIpv6MatchArbitraryBitMaskBuilderFields(ipv6MatchArbitraryBitMaskBuilder,
                            dstIpv6ArbitraryMask, IpConversionUtil.compressedIpv6AddressFormat(ipv6DstAddress));
                }
                Ipv6ArbitraryMask srcIpv6ArbitraryMask = IpConversionUtil.compressedIpv6MaskFormat(
                        IpConversionUtil.createIpv6ArbitraryBitMask(mask));
                Ipv6Address stringIpv6SrcAddress = IpConversionUtil.compressedIpv6AddressFormat(ipv6Src.getIpv6Address());
                setSrcIpv6MatchArbitraryBitMaskBuilderFields(ipv6MatchArbitraryBitMaskBuilder,
                        srcIpv6ArbitraryMask, stringIpv6SrcAddress);
                matchBuilder.setLayer3Match(ipv6MatchArbitraryBitMaskBuilder.build());
            } else if (ipv6MatchArbitraryBitMaskBuilder.getIpv6DestinationAddressNoMask() != null) {
                         /*
                         TODO Change comments
                        Case where destination is of type ipv6MatchArbitraryBitMask already exists in Layer3Match,
                        source which of type ipv6Match needs to be converted to ipv4MatchArbitraryBitMask.
                        We convert 1::/32 to 1::/FFFF:FFFF::
                        example:-
                        <ipv6-source>1::/32</ipv4-source>
                        <ipv6-destination-address-no-mask>1::1</ipv6-destination-address-no-mask>
                        <ipv6-destination-arbitrary-bitmask>FFFF::0001</ipv6-destination-arbitrary-bitmask>
                        after conversion output example:-
                        <ipv6-source-address-no-mask>1::</ipv6-source-address-no-mask>
                        <ipv6-source-arbitrary-bitmask>FFFF:FFFF::</ipv6-source-arbitrary-bitmask>
                        <ipv6-destination-address-no-mask>1::1</ipv6-destination-address-no-mask>
                        <ipv6-destination-arbitrary-bitmask>FFFF::0001</ipv6-destination-arbitrary-bitmask>
                        */
                Ipv6ArbitraryMask srcIpv6ArbitraryMask = IpConversionUtil.compressedIpv6MaskFormat(
                        IpConversionUtil.createIpv6ArbitraryBitMask(mask));
                Ipv6Address stringIpv6SrcAddress = IpConversionUtil.compressedIpv6AddressFormat(ipv6Src.getIpv6Address());
                setSrcIpv6MatchArbitraryBitMaskBuilderFields(ipv6MatchArbitraryBitMaskBuilder,
                        srcIpv6ArbitraryMask, stringIpv6SrcAddress);
                matchBuilder.setLayer3Match(ipv6MatchArbitraryBitMaskBuilder.build());
            } else {
                Ipv6Address stringIpv6SrcAddress = IpConversionUtil.compressedIpv6AddressFormat(ipv6Src.getIpv6Address());
                setIpv6MatchBuilderFields(ipv6MatchBuilder, mask, stringIpv6SrcAddress);
                matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
            }
        }

        return Optional.of(matchBuilder);
    }

    private static void setIpv6MatchBuilderFields(final Ipv6MatchBuilder builder, final byte[] mask, final Ipv6Address prefix) {
        Ipv6Prefix ipv6Prefix;
        if (mask != null) {
            ipv6Prefix = IpConversionUtil.createPrefix(prefix, mask);
        } else {
            ipv6Prefix = IpConversionUtil.createPrefix(prefix);
        }
        builder.setIpv6Source(ipv6Prefix);
    }

    private static void setSrcIpv6MatchArbitraryBitMaskBuilderFields(final Ipv6MatchArbitraryBitMaskBuilder builder,
                                                                     final Ipv6ArbitraryMask mask,
                                                                     final Ipv6Address ipv6Address) {
        if (mask != null) {
            builder.setIpv6SourceArbitraryBitmask(mask);
        }
        builder.setIpv6SourceAddressNoMask(ipv6Address);
    }

    private static void setDstIpv6MatchArbitraryBitMaskBuilderFields(final Ipv6MatchArbitraryBitMaskBuilder builder,
                                                                     final Ipv6ArbitraryMask mask,
                                                                     final Ipv6Address ipv6Address) {
        if (mask != null) {
            builder.setIpv6DestinationArbitraryBitmask(mask);
        }
        builder.setIpv6DestinationAddressNoMask(ipv6Address);
    }
}
