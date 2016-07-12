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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.src.action._case.SetDlSrcAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.dl.src._case.SetDlSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

public class SalToOfSetDlSrcActionV10Case extends ConvertorCase<SetDlSrcActionCase, Action, ActionConvertorData> {
    public SalToOfSetDlSrcActionV10Case() {
        super(SetDlSrcActionCase.class, true, OFConstants.OFP_VERSION_1_0);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final SetDlSrcActionCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        SetDlSrcAction setdlsrcaction = source.getSetDlSrcAction();
        SetDlSrcCaseBuilder setDlSrcCaseBuilder = new SetDlSrcCaseBuilder();
        SetDlSrcActionBuilder setDlSrcActionBuilder = new SetDlSrcActionBuilder();
        setDlSrcActionBuilder.setDlSrcAddress(setdlsrcaction.getAddress());
        setDlSrcCaseBuilder.setSetDlSrcAction(setDlSrcActionBuilder.build());

        return Optional.of(new ActionBuilder()
                .setActionChoice(setDlSrcCaseBuilder.build())
                .build());
    }
}
