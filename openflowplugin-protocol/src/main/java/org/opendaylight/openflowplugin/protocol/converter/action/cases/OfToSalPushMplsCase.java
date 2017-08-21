/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter.action.cases;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.protocol.converter.action.data.ActionResponseConverterData;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.mpls._case.PushMplsAction;

public class OfToSalPushMplsCase extends ConvertorCase<PushMplsCase, Action, ActionResponseConverterData> {
    public OfToSalPushMplsCase() {
        super(PushMplsCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<Action> process(@Nonnull final PushMplsCase source, final ActionResponseConverterData data, ConverterExecutor converterExecutor, final ExtensionConverterProvider extensionConverterProvider) {
        PushMplsAction pushMplsActionFromOF = source.getPushMplsAction();
        PushMplsActionBuilder pushMplsAction = new PushMplsActionBuilder();
        pushMplsAction.setEthernetType(pushMplsActionFromOF.getEthertype().getValue());
        return Optional.of(new PushMplsActionCaseBuilder().setPushMplsAction(pushMplsAction.build()).build());
    }
}
