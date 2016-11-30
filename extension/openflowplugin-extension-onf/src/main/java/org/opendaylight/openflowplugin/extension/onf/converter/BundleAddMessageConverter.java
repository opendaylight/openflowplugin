/**
 Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.

 This program and the accompanying materials are made available under the
 terms of the Eclipse Public License v1.0 which accompanies this distribution,
 and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.converter;

import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.BundleAddMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.bundle.add.message.Message;

/**
 * Converter for BundleAddMessage messages (ONF approved extension #230).
 */
public class BundleAddMessageConverter implements ConvertorMessageToOFJava<BundleAddMessage, BundleAddMessage> {

    private static final ExperimenterId ONF_EXP_ID = new ExperimenterId(0x4F4E4600L);
    private static final long BUNDLE_ADD_MESSAGE_TYPE = 2301;
    private static final ConvertorExecutor converterExecutor = ConvertorManagerFactory.createDefaultManager();

    @Override
    public BundleAddMessage convert(BundleAddMessage input) throws ConversionException {
        return input;
    }

    private Message convertInnerMessage(Message original) throws ConversionException {
        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_3);

        if (original instanceof FlowAddCase) {
            NodeFlow flow = (NodeFlow) original;
            data.setDatapathId(InventoryDataServiceUtil.dataPathIdFromNodeId(flow.getNode().getValue().firstKeyOf(Node.class).getId()));

            final Optional<List<FlowModInput>> flowModInputs = converterExecutor.convert(new AddFlowInputBuilder(flow).build(), data);
            if (flowModInputs.isPresent()) {
                return new FlowModCaseBuilder(flowModInputs.get().get(0)).build();
            } else {
                throw new ConversionException("Conversion of bundle-add-flow failed.");
            }
        }

        return null;
    }

    @Override
    public ExperimenterId getExperimenterId() {
        return ONF_EXP_ID;
    }

    @Override
    public long getType() {
        return BUNDLE_ADD_MESSAGE_TYPE;
    }
}
