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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.sctp.dst._case.SctpDst;

public class OfToSalSctpDstCase extends ConvertorCase<SctpDstCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalSctpDstCase() {
        super(SctpDstCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull SctpDstCase source, MatchResponseConvertorData data) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final SctpMatchBuilder sctpMatchBuilder = data.getSctpMatchBuilder();

        SctpDst sctpDst = source.getSctpDst();
        PortNumber portNumber = sctpDst.getPort();

        if (portNumber != null) {
            sctpMatchBuilder.setSctpDestinationPort(portNumber);
            matchBuilder.setLayer4Match(sctpMatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
