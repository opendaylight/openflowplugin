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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.src._case.Ipv6Src;

public class OfToSalIpv6SrcCase extends ConvertorCase<Ipv6SrcCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalIpv6SrcCase() {
        super(Ipv6SrcCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull Ipv6SrcCase source, MatchResponseConvertorData data) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final Ipv6MatchBuilder ipv6MatchBuilder = data.getIpv6MatchBuilder();

        Ipv6Src ipv6Src = source.getIpv6Src();

        if (ipv6Src != null) {
            String ipv6PrefixStr = ipv6Src.getIpv6Address().getValue();
            byte[] mask = ipv6Src.getMask();
            Ipv6Prefix ipv6Prefix;

            if (mask != null) {
                ipv6Prefix = IpConversionUtil.createPrefix(new Ipv6Address(ipv6PrefixStr), mask);
            } else {
                ipv6Prefix = IpConversionUtil.createPrefix(new Ipv6Address(ipv6PrefixStr));
            }

            ipv6MatchBuilder.setIpv6Source(ipv6Prefix);
            matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
