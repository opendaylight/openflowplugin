/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanPcpActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.pcp.action._case.SetVlanPcpAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanPcpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.vlan.pcp._case.SetVlanPcpActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

public class SalToOfSetVlanPcpActionV10Case extends ConvertorCase<SetVlanPcpActionCase, Action, ActionConvertorData> {
    public SalToOfSetVlanPcpActionV10Case() {
        super(SetVlanPcpActionCase.class, true, OFConstants.OFP_VERSION_1_0);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final SetVlanPcpActionCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        SetVlanPcpAction setvlanpcpaction = source.getSetVlanPcpAction();
        SetVlanPcpActionBuilder setVlanPcpActionBuilder = new SetVlanPcpActionBuilder();
        SetVlanPcpCaseBuilder setVlanPcpCaseBuilder = new SetVlanPcpCaseBuilder();
        setVlanPcpActionBuilder.setVlanPcp(setvlanpcpaction.getVlanPcp().getValue());
        setVlanPcpCaseBuilder.setSetVlanPcpAction(setVlanPcpActionBuilder.build());

        return Optional.of(new ActionBuilder()
                .setActionChoice(setVlanPcpCaseBuilder.build())
                .build());
    }
}
