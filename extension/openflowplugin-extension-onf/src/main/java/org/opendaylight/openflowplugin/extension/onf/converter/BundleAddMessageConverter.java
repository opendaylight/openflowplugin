/**
 Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.

 This program and the accompanying materials are made available under the
 terms of the Eclipse Public License v1.0 which accompanies this distribution,
 and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.converter;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageToOFJava;
import org.opendaylight.openflowplugin.extension.onf.OnfConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev161201.bundle.inner.message.grouping.BundleInnerMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev161201.bundle.inner.message.grouping.bundle.inner.message.AddFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev161201.send.experimenter.input.experimenter.message.of.choice.BundleAddMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.BundleAddMessageBuilder;

/**
 * Converter for BundleAddMessage messages (ONF approved extension #230).
 */
public class BundleAddMessageConverter implements
        ConvertorMessageToOFJava<BundleAddMessage,
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.BundleAddMessage> {

    private static final ConvertorExecutor converterExecutor = ConvertorManagerFactory.createDefaultManager();

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.BundleAddMessage
            convert(BundleAddMessage experimenterMessageCase) {
        final BundleAddMessageBuilder builder = new BundleAddMessageBuilder();
        builder.setBundleId(experimenterMessageCase.getBundleId());
        builder.setFlags(experimenterMessageCase.getFlags());
        builder.setBundleProperty(experimenterMessageCase.getBundleProperty());

        final BundleInnerMessage innerMessage = experimenterMessageCase.getBundleInnerMessage();
        final Class clazz = innerMessage.getImplementedInterface();
        final VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_3);
        if (clazz.equals(AddFlowCase.class)) {
//            Optional<FlowMod> flowMod = converterExecutor.convert(innerMessage, data);
//            builder.setBundleInnerMessage(flowMod);
        }

        return builder.build();
    }

    @Override
    public ExperimenterId getExperimenterId() {
        return new ExperimenterId(OnfConstants.ONF_EXPERIMENTER_ID);
    }

    @Override
    public long getType() {
        return OnfConstants.ONF_ET_BUNDLE_ADD_MESSAGE;
    }
}
