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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6ExtHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6ExthdrCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.exthdr._case.Ipv6Exthdr;

public class OfToSalIpv6ExthdrCase extends ConvertorCase<Ipv6ExthdrCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalIpv6ExthdrCase() {
        super(Ipv6ExthdrCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull Ipv6ExthdrCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final Ipv6MatchBuilder ipv6MatchBuilder = data.getIpv6MatchBuilder();

        Ipv6Exthdr ipv6Exthdr = source.getIpv6Exthdr();

        if (ipv6Exthdr != null) {
            Ipv6ExtHeaderBuilder ipv6ExtHeaderBuilder = new Ipv6ExtHeaderBuilder();
            Ipv6ExthdrFlags pField = ipv6Exthdr.getPseudoField();
            Integer bitmap = MatchConvertorUtil.ipv6ExthdrFlagsToInt(pField);
            ipv6ExtHeaderBuilder.setIpv6Exthdr(bitmap);
            byte[] mask = ipv6Exthdr.getMask();

            if (mask != null) {
                ipv6ExtHeaderBuilder.setIpv6ExthdrMask(ByteUtil.bytesToUnsignedShort(mask));
            }

            ipv6MatchBuilder.setIpv6ExtHeader(ipv6ExtHeaderBuilder.build());
            matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
