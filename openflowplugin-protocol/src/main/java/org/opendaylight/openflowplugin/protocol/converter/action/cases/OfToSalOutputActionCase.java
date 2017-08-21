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
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.protocol.converter.action.data.ActionResponseConverterData;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorCase;
import org.opendaylight.openflowplugin.common.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OfToSalOutputActionCase extends ConvertorCase<OutputActionCase, Action, ActionResponseConverterData> {
    private static final Logger LOG = LoggerFactory.getLogger(OfToSalOutputActionCase.class);

    public OfToSalOutputActionCase() {
        super(OutputActionCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<Action> process(@Nonnull final OutputActionCase source, final ActionResponseConverterData data, ConverterExecutor converterExecutor, final ExtensionConverterProvider extensionConverterProvider) {
        final OpenflowVersion ofVersion = OpenflowVersion.get(data.getVersion());

        OutputActionBuilder outputAction = new OutputActionBuilder();
        OutputAction outputActionFromOF = source.getOutputAction();

        if (outputActionFromOF.getPort() != null) {
            PortNumberUni protocolAgnosticPort = OpenflowPortsUtil.getProtocolAgnosticPort(ofVersion, outputActionFromOF.getPort().getValue());
            String portNumberAsString = OpenflowPortsUtil.portNumberToString(protocolAgnosticPort);
            outputAction.setOutputNodeConnector(new Uri(portNumberAsString));
        } else {
            LOG.error("Provided action is not OF Output action, no associated port found!");
        }

        Integer maxLength = outputActionFromOF.getMaxLength();

        if (maxLength != null) {
            outputAction.setMaxLength(maxLength);
        } else {
            LOG.error("Provided action is not OF Output action, no associated length found!");
        }

        OutputActionCaseBuilder outputActionCaseBuilder = new OutputActionCaseBuilder();
        outputActionCaseBuilder.setOutputAction(outputAction.build());

        return Optional.of(outputActionCaseBuilder.build());
    }
}
