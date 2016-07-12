/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases;

import java.nio.ByteBuffer;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.src._case.Ipv4Src;

public class OfToSalTunnelIpv4SrcCase extends ConvertorCase<Ipv4SrcCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalTunnelIpv4SrcCase() {
        super(Ipv4SrcCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull Ipv4SrcCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final Ipv4MatchBuilder ipv4MatchBuilder = data.getIpv4MatchBuilder();
        final TunnelIpv4MatchBuilder tunnelIpv4MatchBuilder = data.getTunnelIpv4MatchBuilder();

        Ipv4Src tunnelIpv4Dst = source.getIpv4Src();

        if (tunnelIpv4Dst != null) {
            String ipv4PrefixStr = tunnelIpv4Dst.getIpv4Address().getValue();
            byte[] mask = tunnelIpv4Dst.getMask();
            ipv4PrefixStr += IpConversionUtil.PREFIX_SEPARATOR + ByteBuffer.wrap(tunnelIpv4Dst.getMask()).getInt();

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

            ipv4MatchBuilder.setIpv4Source(ipv4Prefix);
            matchBuilder.setLayer3Match(tunnelIpv4MatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
