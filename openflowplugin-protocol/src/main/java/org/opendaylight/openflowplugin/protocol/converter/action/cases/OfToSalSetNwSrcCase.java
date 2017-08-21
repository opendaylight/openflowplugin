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
import org.opendaylight.openflowplugin.protocol.converter.data.ActionResponseConverterData;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorCase;
import org.opendaylight.openflowplugin.protocol.converter.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCase;

public class OfToSalSetNwSrcCase extends ConvertorCase<SetNwSrcCase, Action, ActionResponseConverterData> {
    public OfToSalSetNwSrcCase() {
        super(SetNwSrcCase.class, true, OFConstants.OFP_VERSION_1_0);
    }

    @Override
    public Optional<Action> process(@Nonnull final SetNwSrcCase source, final ActionResponseConverterData data, ConverterExecutor converterExecutor) {
        return Optional.of(new SetNwSrcActionCaseBuilder()
                .setSetNwSrcAction(new SetNwSrcActionBuilder()
                        .setAddress(new Ipv4Builder()
                                .setIpv4Address(IpConversionUtil.createPrefix(source.getSetNwSrcAction().getIpAddress()))
                                .build())
                        .build())
                .build());
    }
}
