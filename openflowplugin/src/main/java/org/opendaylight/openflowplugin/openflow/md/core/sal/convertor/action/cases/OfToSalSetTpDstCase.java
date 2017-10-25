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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpDstCase;

public class OfToSalSetTpDstCase extends ConvertorCase<SetTpDstCase, Action, ActionResponseConvertorData> {
    public OfToSalSetTpDstCase() {
        super(SetTpDstCase.class, true, OFConstants.OFP_VERSION_1_0);
    }

    @Override
    public Optional<Action> process(@Nonnull final SetTpDstCase source, final ActionResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        return Optional.of(new SetTpDstActionCaseBuilder()
                .setSetTpDstAction(new SetTpDstActionBuilder()
                        .setPort(new PortNumber(source.getSetTpDstAction().getPort().getValue().intValue()))
                        .setIpProtocol(data.getIpProtocol())
                        .build())
                .build());
    }
}
