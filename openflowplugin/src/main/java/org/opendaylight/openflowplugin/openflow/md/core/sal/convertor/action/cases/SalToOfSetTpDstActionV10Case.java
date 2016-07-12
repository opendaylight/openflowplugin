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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.tp.dst._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;

public class SalToOfSetTpDstActionV10Case extends ConvertorCase<SetTpDstActionCase, Action, ActionConvertorData> {
    public SalToOfSetTpDstActionV10Case() {
        super(SetTpDstActionCase.class, true, OFConstants.OFP_VERSION_1_0);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final SetTpDstActionCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        SetTpDstAction settpdstaction = source.getSetTpDstAction();
        SetTpDstCaseBuilder setTpDstCaseBuilder = new SetTpDstCaseBuilder();
        SetTpDstActionBuilder setTpDstActionBuilder = new SetTpDstActionBuilder();
        setTpDstActionBuilder.setPort(new PortNumber(settpdstaction.getPort().getValue().longValue()));
        setTpDstCaseBuilder.setSetTpDstAction(setTpDstActionBuilder.build());

        return Optional.of(new ActionBuilder()
                .setActionChoice(setTpDstCaseBuilder.build())
                .build());
    }
}