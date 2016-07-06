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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.ttl._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

public class SalToOfSetNwTtlActionCase extends ConvertorCase<SetNwTtlActionCase, Action, ActionConvertorData> {
    public SalToOfSetNwTtlActionCase() {
        super(SetNwTtlActionCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final SetNwTtlActionCase source, final ActionConvertorData data) {
        SetNwTtlCaseBuilder nwTtlCaseBuilder = new SetNwTtlCaseBuilder();
        SetNwTtlActionBuilder nwTtlBuilder = new SetNwTtlActionBuilder();
        nwTtlBuilder.setNwTtl(source.getSetNwTtlAction().getNwTtl());
        nwTtlCaseBuilder.setSetNwTtlAction(nwTtlBuilder.build());

        return Optional.of(new ActionBuilder()
                .setActionChoice(nwTtlCaseBuilder.build())
                .build());
    }
}