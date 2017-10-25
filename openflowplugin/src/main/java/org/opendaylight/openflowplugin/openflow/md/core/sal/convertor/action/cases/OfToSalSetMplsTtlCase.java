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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.mpls.ttl._case.SetMplsTtlAction;

public class OfToSalSetMplsTtlCase extends ConvertorCase<SetMplsTtlCase, Action, ActionResponseConvertorData> {
    public OfToSalSetMplsTtlCase() {
        super(SetMplsTtlCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<Action> process(@Nonnull final SetMplsTtlCase source, final ActionResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        SetMplsTtlAction setMplsTtlActionFromOF = source.getSetMplsTtlAction();

        SetMplsTtlActionBuilder mplsTtlAction = new SetMplsTtlActionBuilder();
        mplsTtlAction.setMplsTtl(setMplsTtlActionFromOF.getMplsTtl());
        return Optional.of(new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(mplsTtlAction.build()).build());
    }
}
