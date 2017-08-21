/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter.action.cases;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.protocol.converter.data.ActionConverterData;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.vlan.vid._case.SetVlanVidActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

public class SalToOfSetFieldV10Case extends ConvertorCase<SetFieldCase, Action, ActionConverterData> {
    public SalToOfSetFieldV10Case() {
        super(SetFieldCase.class, true, OFConstants.OFP_VERSION_1_0);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final SetFieldCase source, final ActionConverterData data, ConverterExecutor converterExecutor) {
        SetVlanVidCaseBuilder setVlanVidCaseBuilder = new SetVlanVidCaseBuilder();

        if (source.getSetField().getVlanMatch() != null) {
            setVlanVidCaseBuilder.setSetVlanVidAction(new SetVlanVidActionBuilder()
                    .setVlanVid(source.getSetField().getVlanMatch().getVlanId().getVlanId().getValue())
                    .build());
        }

        return Optional.of(new ActionBuilder()
                .setActionChoice(setVlanVidCaseBuilder.build())
                .build());
    }
}
