/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter.match.cases;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorCase;
import org.opendaylight.openflowplugin.protocol.converter.match.data.MatchResponseConverterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.dst._case.UdpDst;

public class OfToSalUdpDstCase extends ConvertorCase<UdpDstCase, MatchBuilder, MatchResponseConverterData> {
    public OfToSalUdpDstCase() {
        super(UdpDstCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull UdpDstCase source, MatchResponseConverterData data, ConverterExecutor converterExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final UdpMatchBuilder udpMatchBuilder = data.getUdpMatchBuilder();

        UdpDst udpDst = source.getUdpDst();

        if (udpDst != null) {
            udpMatchBuilder.setUdpDestinationPort(udpDst.getPort());
            matchBuilder.setLayer4Match(udpMatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
