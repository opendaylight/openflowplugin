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
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalToOfOutputActionCase extends ConvertorCase<OutputActionCase, Action, ActionConvertorData> {
    private static final Logger LOG = LoggerFactory.getLogger(SalToOfOutputActionCase.class);

    public SalToOfOutputActionCase() {
        super(OutputActionCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final OutputActionCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        final OutputAction outputAction = source.getOutputAction();
        final OutputActionBuilder outputBuilder = new OutputActionBuilder();

        if (outputAction.getMaxLength() != null) {
            outputBuilder.setMaxLength(outputAction.getMaxLength());
        } else {
            outputBuilder.setMaxLength(0);
        }

        final OpenflowVersion version = OpenflowVersion.get(data.getVersion());
        final String nodeConnectorId = outputAction.getOutputNodeConnector().getValue();
        final Long portNumber = InventoryDataServiceUtil.portNumberfromNodeConnectorId(version, nodeConnectorId);

        if (OpenflowPortsUtil.checkPortValidity(version, portNumber)) {
            outputBuilder.setPort(new PortNumber(portNumber));
        } else {
            LOG.error("Invalid Port specified {} for Output Action for OF version: {}", portNumber, version);
        }

        return Optional.of(new ActionBuilder()
                .setActionChoice(new OutputActionCaseBuilder()
                        .setOutputAction(outputBuilder.build())
                        .build())
                .build());
    }
}
