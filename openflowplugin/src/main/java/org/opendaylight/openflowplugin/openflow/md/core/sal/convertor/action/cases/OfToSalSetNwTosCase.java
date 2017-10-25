/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.tos.action._case.SetNwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTosCase;

public class OfToSalSetNwTosCase extends ConvertorCase<SetNwTosCase, Action, ActionResponseConvertorData> {
    public OfToSalSetNwTosCase() {
        super(SetNwTosCase.class, true, OFConstants.OFP_VERSION_1_0);
    }

    @Override
    public Optional<Action> process(@Nonnull final SetNwTosCase source, final ActionResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        return Optional.of(new SetNwTosActionCaseBuilder()
                .setSetNwTosAction(new SetNwTosActionBuilder()
                        .setTos((int) source.getSetNwTosAction().getNwTos())
                        .build())
                .build());
    }
}
