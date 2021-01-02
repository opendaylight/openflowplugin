/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.converter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowplugin.extension.api.ConverterMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionConvertorData;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.extension.api.path.MessagePath;
import org.opendaylight.openflowplugin.extension.onf.OnfConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.XidConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.BundleInnerMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleUpdateFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleUpdateGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleUpdatePortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageSal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageSalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.bundle.add.message.sal.SalAddMessageDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleFlowModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleFlowModCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleGroupModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleGroupModCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundlePortModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundlePortModCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.bundle.flow.mod._case.FlowModCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.bundle.group.mod._case.GroupModCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.bundle.port.mod._case.PortModCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessageOnf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessageOnfBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.bundle.add.message.onf.OnfAddMessageGroupingDataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter for BundleAddMessage messages (ONF approved extension #230).
 */
public class BundleAddMessageConverter implements
        ConverterMessageToOFJava<BundleAddMessageSal, BundleAddMessageOnf, ExtensionConvertorData>,
        ConvertorMessageFromOFJava<BundleAddMessageOnf, MessagePath> {

    private static final Logger LOG = LoggerFactory.getLogger(BundleAddMessageConverter.class);
    private static final ConvertorExecutor CONVERTER_EXECUTOR = ConvertorManagerFactory.createDefaultManager();

    @Override
    public BundleAddMessageOnf convert(final BundleAddMessageSal experimenterMessageCase,
            final ExtensionConvertorData extensionData) throws ConversionException {
        final OnfAddMessageGroupingDataBuilder dataBuilder = new OnfAddMessageGroupingDataBuilder();
        dataBuilder.setBundleId(experimenterMessageCase.getSalAddMessageData().getBundleId());
        dataBuilder.setFlags(experimenterMessageCase.getSalAddMessageData().getFlags());
        dataBuilder.setBundleProperty(experimenterMessageCase.getSalAddMessageData().getBundleProperty());
        final BundleInnerMessage innerMessage = experimenterMessageCase.getSalAddMessageData().getBundleInnerMessage();
        final XidConvertorData data = new XidConvertorData(extensionData.getVersion());
        data.setDatapathId(extensionData.getDatapathId());
        data.setXid(extensionData.getXid());
        LOG.trace("Flow or group pushed to the node: {} with transaction id : {} is {}",
                data.getDatapathId(), data.getXid(),
                experimenterMessageCase.getSalAddMessageData().getBundleInnerMessage());
        if (innerMessage.implementedInterface().equals(BundleAddFlowCase.class)
                || innerMessage.implementedInterface().equals(BundleUpdateFlowCase.class)
                || innerMessage.implementedInterface().equals(BundleRemoveFlowCase.class)) {
            dataBuilder.setBundleInnerMessage(convertBundleFlowCase(innerMessage, data));
        } else if (innerMessage.implementedInterface().equals(BundleAddGroupCase.class)
                || innerMessage.implementedInterface().equals(BundleUpdateGroupCase.class)
                || innerMessage.implementedInterface().equals(BundleRemoveGroupCase.class)) {
            dataBuilder.setBundleInnerMessage(convertBundleGroupCase(innerMessage, data));
        } else if (innerMessage.implementedInterface().equals(BundleUpdatePortCase.class)) {
            dataBuilder.setBundleInnerMessage(convertBundlePortCase(innerMessage, data));
        } else {
            throw new ConversionException("Unsupported inner message");
        }

        return new BundleAddMessageOnfBuilder().setOnfAddMessageGroupingData(dataBuilder.build()).build();
    }

    @Override
    public BundleAddMessageSal convert(final BundleAddMessageOnf input, final MessagePath path) {
        return new BundleAddMessageSalBuilder()
                .setSalAddMessageData(new SalAddMessageDataBuilder(input.getOnfAddMessageGroupingData())
                        .build())
                .build();
    }

    private static BundleFlowModCase convertBundleFlowCase(final BundleInnerMessage messageCase,
            final XidConvertorData data) throws ConversionException {
        final Optional<List<FlowModInputBuilder>> flowModInputs;
        final Class<?> clazz = messageCase.implementedInterface();
        if (clazz.equals(BundleAddFlowCase.class)) {
            flowModInputs = CONVERTER_EXECUTOR.convert(
                    new AddFlowInputBuilder(((BundleAddFlowCase) messageCase).getAddFlowCaseData()).build(), data);
        } else if (clazz.equals(BundleUpdateFlowCase.class)) {
            flowModInputs = CONVERTER_EXECUTOR.convert(
                    new UpdatedFlowBuilder(((BundleUpdateFlowCase) messageCase).getUpdateFlowCaseData()).build(), data);
        } else if (clazz.equals(BundleRemoveFlowCase.class)) {
            flowModInputs = CONVERTER_EXECUTOR.convert(
                    new RemoveFlowInputBuilder(((BundleRemoveFlowCase) messageCase).getRemoveFlowCaseData()).build(),
                    data);
        } else {
            flowModInputs = Optional.empty();
        }

        if (!flowModInputs.isPresent()) {
            throw new ConversionException("BundleFlowCase conversion unsuccessful.");
        }
        final List<FlowModInputBuilder> inputs = flowModInputs.get();
        if (inputs.size() != 1) {
            throw new ConversionException(
                "BundleFlowCase conversion unsuccessful - not able to convert to multiple flows.");
        }

        return new BundleFlowModCaseBuilder()
                .setFlowModCaseData(new FlowModCaseDataBuilder(inputs.get(0).setXid(data.getXid()).build()).build())
                .build();
    }

    private static BundleGroupModCase convertBundleGroupCase(final BundleInnerMessage messageCase,
            final XidConvertorData data) throws ConversionException {
        final Optional<GroupModInputBuilder> groupModInput;
        final Class<?> clazz = messageCase.implementedInterface();
        if (clazz.equals(BundleAddGroupCase.class)) {
            groupModInput = CONVERTER_EXECUTOR.convert(
                    new AddGroupInputBuilder(((BundleAddGroupCase) messageCase).getAddGroupCaseData()).build(), data);
        } else if (clazz.equals(BundleUpdateGroupCase.class)) {
            groupModInput = CONVERTER_EXECUTOR.convert(
                    new UpdatedGroupBuilder(((BundleUpdateGroupCase) messageCase).getUpdateGroupCaseData()).build(),
                    data);
        } else if (clazz.equals(BundleRemoveGroupCase.class)) {
            groupModInput = CONVERTER_EXECUTOR.convert(
                    new RemoveGroupInputBuilder(((BundleRemoveGroupCase) messageCase).getRemoveGroupCaseData()).build(),
                    data);
        } else {
            groupModInput = Optional.empty();
        }

        if (!groupModInput.isPresent()) {
            throw new ConversionException("BundleGroupCase conversion unsuccessful.");
        }

        return new BundleGroupModCaseBuilder()
                .setGroupModCaseData(new GroupModCaseDataBuilder(groupModInput.get().setXid(data.getXid()).build())
                    .build())
                .build();
    }

    private static BundlePortModCase convertBundlePortCase(final BundleInnerMessage messageCase,
            final XidConvertorData data) throws ConversionException {
        final Optional<PortModInput> portModInput;
        final Class<?> clazz = messageCase.implementedInterface();
        if (clazz.equals(BundleUpdatePortCase.class)) {
            Collection<Port> ports
                    = ((BundleUpdatePortCase) messageCase).getUpdatePortCaseData().getPort().nonnullPort().values();
            Port port = ports.iterator().next();
            portModInput = CONVERTER_EXECUTOR.convert(port, data);
        } else {
            portModInput = Optional.empty();
        }

        if (!portModInput.isPresent()) {
            throw new ConversionException("BundlePortCase conversion unsuccessful.");
        }
        return new BundlePortModCaseBuilder()
                .setPortModCaseData(new PortModCaseDataBuilder(portModInput.get()).setXid(data.getXid()).build())
                .build();
    }

    @Override
    public ExperimenterId getExperimenterId() {
        return OnfConstants.ONF_EXPERIMENTER_ID;
    }

    @Override
    public long getType() {
        return OnfConstants.ONF_ET_BUNDLE_ADD_MESSAGE;
    }
}
