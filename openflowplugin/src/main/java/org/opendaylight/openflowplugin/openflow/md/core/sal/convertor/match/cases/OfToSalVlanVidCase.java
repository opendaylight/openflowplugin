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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.vid._case.VlanVid;

public class OfToSalVlanVidCase extends ConvertorCase<VlanVidCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalVlanVidCase() {
        super(VlanVidCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull VlanVidCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final VlanMatchBuilder vlanMatchBuilder = data.getVlanMatchBuilder();

        final VlanVid vlanVid = source.getVlanVid();

        if (vlanVid != null) {
            VlanIdBuilder vlanBuilder = new VlanIdBuilder();
            vlanBuilder.setVlanId(new VlanId(vlanVid.getVlanVid()));
            vlanBuilder.setVlanIdPresent(vlanVid.isCfiBit());
            vlanMatchBuilder.setVlanId(vlanBuilder.build());
            matchBuilder.setVlanMatch(vlanMatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
