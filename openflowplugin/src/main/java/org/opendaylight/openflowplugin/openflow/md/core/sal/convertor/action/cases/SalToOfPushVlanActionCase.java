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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.vlan._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;

public class SalToOfPushVlanActionCase extends ConvertorCase<PushVlanActionCase, Action, ActionConvertorData> {
    public SalToOfPushVlanActionCase() {
        super(PushVlanActionCase.class, true, OFConstants.OFP_VERSION_1_3);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final PushVlanActionCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        PushVlanAction pushVlanAction = source.getPushVlanAction();

        PushVlanCaseBuilder pushVlanCaseBuilder = new PushVlanCaseBuilder();
        PushVlanActionBuilder pushVlanBuilder = new PushVlanActionBuilder();

        if (pushVlanAction.getEthernetType() != null) {
            pushVlanBuilder.setEthertype(new EtherType(pushVlanAction.getEthernetType()));
        }

        pushVlanCaseBuilder.setPushVlanAction(pushVlanBuilder.build());

        return Optional.of(new ActionBuilder()
                .setActionChoice(pushVlanCaseBuilder.build())
                .build());
    }
}