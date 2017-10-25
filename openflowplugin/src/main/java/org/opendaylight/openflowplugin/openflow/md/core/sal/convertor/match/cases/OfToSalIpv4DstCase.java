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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.dst._case.Ipv4Dst;

public class OfToSalIpv4DstCase extends ConvertorCase<Ipv4DstCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalIpv4DstCase() {
        super(Ipv4DstCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    private static void setIpv4MatchBuilderFields(final Ipv4MatchBuilder ipv4MatchBuilder, final byte[] mask, final String ipv4PrefixStr) {
        final Ipv4Prefix ipv4Prefix;
        if (mask != null) {
            ipv4Prefix = IpConversionUtil.createPrefix(new Ipv4Address(ipv4PrefixStr), mask);
        } else {
            //Openflow Spec : 1.3.2
            //An all-one-bits oxm_mask is equivalent to specifying 0 for oxm_hasmask and omitting oxm_mask.
            // So when user specify 32 as a mast, switch omit that mast and we get null as a mask in flow
            // statistics response.
            ipv4Prefix = IpConversionUtil.createPrefix(new Ipv4Address(ipv4PrefixStr));
        }

        ipv4MatchBuilder.setIpv4Destination(ipv4Prefix);
    }

    private static void setSrcIpv4MatchArbitraryBitMaskBuilderFields(
            final Ipv4MatchArbitraryBitMaskBuilder ipv4MatchArbitraryBitMaskBuilder,
            final DottedQuad mask, final String ipv4AddressStr) {
        if (mask != null) {
            ipv4MatchArbitraryBitMaskBuilder.setIpv4SourceArbitraryBitmask(mask);
        }

        ipv4MatchArbitraryBitMaskBuilder.setIpv4SourceAddressNoMask(new Ipv4Address(ipv4AddressStr));
    }

    private static void setDstIpv4MatchArbitraryBitMaskBuilderFields(
            final Ipv4MatchArbitraryBitMaskBuilder ipv4MatchArbitraryBitMaskBuilder,
            final DottedQuad mask, final String ipv4AddressStr) {
        if (mask != null) {
            ipv4MatchArbitraryBitMaskBuilder.setIpv4DestinationArbitraryBitmask(mask);
        }

        ipv4MatchArbitraryBitMaskBuilder.setIpv4DestinationAddressNoMask(new Ipv4Address(ipv4AddressStr));
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull Ipv4DstCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final Ipv4MatchBuilder ipv4MatchBuilder = data.getIpv4MatchBuilder();
        final Ipv4MatchArbitraryBitMaskBuilder ipv4MatchArbitraryBitMaskBuilder = data.getIpv4MatchArbitraryBitMaskBuilder();

        Ipv4Dst ipv4Address = source.getIpv4Dst();

        if (ipv4Address != null) {
            byte[] mask = ipv4Address.getMask();

            if (mask != null && IpConversionUtil.isArbitraryBitMask(mask)) {
                // case where ipv4src is of type ipv4MatchBuilder and ipv4dst is of type ipv4MatchArbitrary.
                // Needs to convert ipv4src to ipv4MatchArbitrary.
                if (ipv4MatchBuilder.getIpv4Source() != null) {
                    Ipv4Prefix ipv4PrefixSourceAddress = ipv4MatchBuilder.getIpv4Source();
                    Ipv4Address ipv4SourceAddress = IpConversionUtil.extractIpv4Address(ipv4PrefixSourceAddress);
                    DottedQuad srcDottedQuad = IpConversionUtil.extractIpv4AddressMask(ipv4PrefixSourceAddress);
                    setSrcIpv4MatchArbitraryBitMaskBuilderFields(ipv4MatchArbitraryBitMaskBuilder,
                            srcDottedQuad, ipv4SourceAddress.getValue());
                }

                DottedQuad dstDottedQuadMask = IpConversionUtil.createArbitraryBitMask(mask);
                String stringIpv4DstAddress = ipv4Address.getIpv4Address().getValue();
                setDstIpv4MatchArbitraryBitMaskBuilderFields(ipv4MatchArbitraryBitMaskBuilder,
                        dstDottedQuadMask, stringIpv4DstAddress);
                matchBuilder.setLayer3Match(ipv4MatchArbitraryBitMaskBuilder.build());
            } else if (ipv4MatchArbitraryBitMaskBuilder.getIpv4SourceAddressNoMask() != null) {
                        /*
                        Case where source is of type ipv4MatchArbitraryBitMask already exists in Layer3Match,
                        destination which of type ipv4Match needs to be converted to ipv4MatchArbitraryBitMask.
                        We convert 36.36.36.0/24 to 36.36.0/255.255.255.0
                        expected output example:-
                        <ipv4-source>36.36.36.0/24</ipv4-source>
                        <ipv4-destination-address-no-mask>36.36.36.0</ipv4-destination-address-no-mask>
                        <ipv4-destination-arbitrary-bitmask>255.0.255.0</ipv4-destination-arbitrary-bitmask>
                        after conversion output example:-
                        <ipv4-source-address-no-mask>36.36.36.0</ipv4-source-address-no-mask>
                        <ipv4-source-arbitrary-bitmask>255.255.255.0</ipv4-source-arbitrary-bitmask>
                        <ipv4-destination-address-no-mask>36.36.36.0</ipv4-destination-address-no-mask>
                        <ipv4-destination-arbitrary-bitmask>255.0.255.0</ipv4-destination-arbitrary-bitmask>
                        */
                DottedQuad dstDottedQuadMask = IpConversionUtil.createArbitraryBitMask(mask);
                String stringIpv4DstAddress = ipv4Address.getIpv4Address().getValue();
                setDstIpv4MatchArbitraryBitMaskBuilderFields(ipv4MatchArbitraryBitMaskBuilder,
                        dstDottedQuadMask, stringIpv4DstAddress);
                matchBuilder.setLayer3Match(ipv4MatchArbitraryBitMaskBuilder.build());
            } else {
                String ipv4PrefixStr = ipv4Address.getIpv4Address().getValue();
                setIpv4MatchBuilderFields(ipv4MatchBuilder, mask, ipv4PrefixStr);
                matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
            }
        }

        return Optional.of(matchBuilder);
    }
}
