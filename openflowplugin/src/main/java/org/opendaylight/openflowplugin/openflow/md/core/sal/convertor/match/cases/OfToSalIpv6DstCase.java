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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.dst._case.Ipv6Dst;

public class OfToSalIpv6DstCase extends ConvertorCase<Ipv6DstCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalIpv6DstCase() {
        super(Ipv6DstCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(ConvertorManager convertorManager, @Nonnull Ipv6DstCase source, MatchResponseConvertorData data) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final Ipv6MatchBuilder ipv6MatchBuilder = data.getIpv6MatchBuilder();

        Ipv6Dst ipv6Dst = source.getIpv6Dst();

        if (ipv6Dst != null) {
            String ipv6PrefixStr = ipv6Dst.getIpv6Address().getValue();
            byte[] mask = ipv6Dst.getMask();
            Ipv6Prefix ipv6Prefix;

            if (mask != null) {
                ipv6Prefix = IpConversionUtil.createPrefix(new Ipv6Address(ipv6PrefixStr), mask);
            } else {
                ipv6Prefix = IpConversionUtil.createPrefix(new Ipv6Address(ipv6PrefixStr));
            }

            ipv6MatchBuilder.setIpv6Destination(ipv6Prefix);
            matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
