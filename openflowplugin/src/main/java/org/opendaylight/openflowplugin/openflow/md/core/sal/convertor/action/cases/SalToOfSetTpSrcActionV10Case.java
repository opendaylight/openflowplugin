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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.tp.src._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalToOfSetTpSrcActionV10Case extends ConvertorCase<SetTpSrcActionCase, Action, ActionConvertorData> {
    private static final Logger LOG = LoggerFactory.getLogger(SalToOfSetTpSrcActionV10Case.class);

    public SalToOfSetTpSrcActionV10Case() {
        super(SetTpSrcActionCase.class, true, OFConstants.OFP_VERSION_1_0);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final SetTpSrcActionCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        SetTpSrcAction settpsrcaction = source.getSetTpSrcAction();
        SetTpSrcCaseBuilder setTpSrcCaseBuilder = new SetTpSrcCaseBuilder();
        SetTpSrcActionBuilder setTpSrcActionBuilder = new SetTpSrcActionBuilder();
        setTpSrcActionBuilder.setPort(new PortNumber(settpsrcaction.getPort()
                .getValue()
                .longValue()));
        setTpSrcCaseBuilder.setSetTpSrcAction(setTpSrcActionBuilder.build());

        return Optional.of(new ActionBuilder()
                .setActionChoice(setTpSrcCaseBuilder.build())
                .build());
    }
}