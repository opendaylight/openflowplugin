/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
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
import org.opendaylight.openflowplugin.protocol.converter.data.ActionResponseConverterData;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpSrcCase;

public class OfToSalSetTpSrcCase extends ConvertorCase<SetTpSrcCase, Action, ActionResponseConverterData> {
    public OfToSalSetTpSrcCase() {
        super(SetTpSrcCase.class, true, OFConstants.OFP_VERSION_1_0);
    }

    @Override
    public Optional<Action> process(@Nonnull final SetTpSrcCase source, final ActionResponseConverterData data, ConverterExecutor converterExecutor) {
        return Optional.of(new SetTpSrcActionCaseBuilder()
                .setSetTpSrcAction(new SetTpSrcActionBuilder()
                        .setPort(new PortNumber(source.getSetTpSrcAction().getPort().getValue().intValue()))
                        .setIpProtocol(data.getIpProtocol())
                        .build())
                .build());
    }
}
