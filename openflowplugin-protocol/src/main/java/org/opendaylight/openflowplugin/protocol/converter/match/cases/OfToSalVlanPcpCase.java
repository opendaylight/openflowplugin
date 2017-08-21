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
import org.opendaylight.openflowplugin.protocol.converter.data.MatchResponseConverterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanPcpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.pcp._case.VlanPcp;

public class OfToSalVlanPcpCase extends ConvertorCase<VlanPcpCase, MatchBuilder, MatchResponseConverterData> {
    public OfToSalVlanPcpCase() {
        super(VlanPcpCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull VlanPcpCase source, MatchResponseConverterData data, ConverterExecutor converterExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final VlanMatchBuilder vlanMatchBuilder = data.getVlanMatchBuilder();

        final VlanPcp vlanPcp = source.getVlanPcp();

        if (vlanPcp != null) {
            vlanMatchBuilder
                    .setVlanPcp(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp(
                            vlanPcp.getVlanPcp()));
            matchBuilder.setVlanMatch(vlanMatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
