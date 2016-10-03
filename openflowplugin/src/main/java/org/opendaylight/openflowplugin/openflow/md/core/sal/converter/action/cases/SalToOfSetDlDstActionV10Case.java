/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.converter.action.cases;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.action.data.ActionConverterData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.converter.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.dst.action._case.SetDlDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.dl.dst._case.SetDlDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

public class SalToOfSetDlDstActionV10Case extends ConvertorCase<SetDlDstActionCase, Action, ActionConverterData> {
    public SalToOfSetDlDstActionV10Case() {
        super(SetDlDstActionCase.class, true, OFConstants.OFP_VERSION_1_0);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final SetDlDstActionCase source, final ActionConverterData data, ConverterExecutor converterExecutor) {
        SetDlDstAction setdldstaction = source.getSetDlDstAction();
        SetDlDstCaseBuilder setDlDstCaseBuilder = new SetDlDstCaseBuilder();
        SetDlDstActionBuilder setDlDstActionBuilder = new SetDlDstActionBuilder();
        setDlDstActionBuilder.setDlDstAddress(setdldstaction.getAddress());
        setDlDstCaseBuilder.setSetDlDstAction(setDlDstActionBuilder.build());

        return Optional.of(new ActionBuilder()
                .setActionChoice(setDlDstCaseBuilder.build())
                .build());
    }
}
