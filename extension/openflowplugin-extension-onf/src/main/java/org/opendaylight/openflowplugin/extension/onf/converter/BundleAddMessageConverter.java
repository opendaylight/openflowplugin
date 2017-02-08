/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.converter;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.BundleMessageDataInjector;
import org.opendaylight.openflowplugin.extension.api.ConverterMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.extension.onf.OnfConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
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

/**
 * Converter for BundleAddMessage messages (ONF approved extension #230).
 */
public class BundleAddMessageConverter implements ConverterMessageToOFJava<BundleAddMessageSal, BundleAddMessageOnf>, BundleMessageDataInjector {

    private static final ConvertorExecutor converterExecutor = ConvertorManagerFactory.createDefaultManager();
    private long xid;
    private NodeRef node;

    @Override
    public BundleAddMessageOnf convert(BundleAddMessageSal experimenterMessageCase) throws ConversionException {
        final OnfAddMessageGroupingDataBuilder dataBuilder = new OnfAddMessageGroupingDataBuilder();
        dataBuilder.setBundleId(experimenterMessageCase.getSalAddMessageData().getBundleId());
        dataBuilder.setFlags(experimenterMessageCase.getSalAddMessageData().getFlags());
        dataBuilder.setBundleProperty(experimenterMessageCase.getSalAddMessageData().getBundleProperty());
        final BundleInnerMessage innerMessage = experimenterMessageCase.getSalAddMessageData().getBundleInnerMessage();
        final VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_3);
        data.setDatapathId(digDatapathId(node));

        if (innerMessage.getImplementedInterface().equals(BundleAddFlowCase.class)   ||
                innerMessage.getImplementedInterface().equals(BundleUpdateFlowCase.class) ||
                innerMessage.getImplementedInterface().equals(BundleRemoveFlowCase.class)) {
            dataBuilder.setBundleInnerMessage(convertBundleFlowCase(innerMessage, data));
        } else if (innerMessage.getImplementedInterface().equals(BundleAddGroupCase.class) ||
                innerMessage.getImplementedInterface().equals(BundleUpdateGroupCase.class) ||
                innerMessage.getImplementedInterface().equals(BundleRemoveGroupCase.class)) {
            dataBuilder.setBundleInnerMessage(convertBundleGroupCase(innerMessage, data));
        } else if (innerMessage.getImplementedInterface().equals(BundleUpdatePortCase.class)) {
            dataBuilder.setBundleInnerMessage(convertBundlePortCase(innerMessage, data));
        } else {
            throw new ConversionException("Unsupported inner message");
        }

        return new BundleAddMessageOnfBuilder().setOnfAddMessageGroupingData(dataBuilder.build()).build();
    }

    private BundleFlowModCase convertBundleFlowCase(final BundleInnerMessage messageCase, final VersionDatapathIdConvertorData data) throws ConversionException {
        Optional<List<FlowModInputBuilder>> flowModInputs = Optional.empty();
        final Class clazz = messageCase.getImplementedInterface();
        if (clazz.equals(BundleAddFlowCase.class)) {
            flowModInputs = converterExecutor.convert(new AddFlowInputBuilder(((BundleAddFlowCase)messageCase).getAddFlowCaseData()).build(), data);
        } else if (clazz.equals(BundleUpdateFlowCase.class)) {
            flowModInputs = converterExecutor.convert(new UpdatedFlowBuilder(((BundleUpdateFlowCase)messageCase).getUpdateFlowCaseData()).build(), data);
        } else if (clazz.equals(BundleRemoveFlowCase.class)) {
            flowModInputs = converterExecutor.convert(new RemoveFlowInputBuilder(((BundleRemoveFlowCase)messageCase).getRemoveFlowCaseData()).build(), data);
        }

        if (flowModInputs.isPresent()) {
            if (flowModInputs.get().size() == 1) {
                return new BundleFlowModCaseBuilder()
                        .setFlowModCaseData(
                                new FlowModCaseDataBuilder(
                                        (flowModInputs
                                                .get()
                                                .get(0)
                                                .setXid(xid))
                                                .build())
                                        .build())
                        .build();
            } else {
                throw new ConversionException("BundleFlowCase conversion unsuccessful - not able to convert to multiple flows.");
            }
        } else {
            throw new ConversionException("BundleFlowCase conversion unsuccessful.");
        }
    }

    private BundleGroupModCase convertBundleGroupCase(final BundleInnerMessage messageCase, final VersionDatapathIdConvertorData data) throws ConversionException {
        Optional<GroupModInputBuilder> groupModInput = Optional.empty();
        final Class clazz = messageCase.getImplementedInterface();
        if (clazz.equals(BundleAddGroupCase.class)) {
            groupModInput = converterExecutor.convert(new AddGroupInputBuilder(((BundleAddGroupCase)messageCase).getAddGroupCaseData()).build(), data);
        } else if (clazz.equals(BundleUpdateGroupCase.class)) {
            groupModInput = converterExecutor.convert(new UpdatedGroupBuilder(((BundleUpdateGroupCase)messageCase).getUpdateGroupCaseData()).build(), data);
        } else if (clazz.equals(BundleRemoveGroupCase.class)) {
            groupModInput = converterExecutor.convert(new RemoveGroupInputBuilder(((BundleRemoveGroupCase)messageCase).getRemoveGroupCaseData()).build(), data);
        }

        if (groupModInput.isPresent()) {
            return new BundleGroupModCaseBuilder()
                    .setGroupModCaseData(
                            new GroupModCaseDataBuilder(groupModInput.get()
                                    .setXid(xid)
                                    .build())
                            .build()
                    )
                    .build();
        } else {
            throw new ConversionException("BundleGroupCase conversion unsuccessful.");
        }
    }

    private BundlePortModCase convertBundlePortCase(final BundleInnerMessage messageCase, final VersionDatapathIdConvertorData data) throws ConversionException {
        Optional<PortModInput> portModInput = Optional.empty();
        final Class clazz = messageCase.getImplementedInterface();
        if (clazz.equals(BundleUpdatePortCase.class)) {
            portModInput = converterExecutor.convert(new PortBuilder(((BundleUpdatePortCase)messageCase).getUpdatePortCaseData().getPort().getPort().get(0)).build(), data);
        }

        if (portModInput.isPresent()) {
            return new BundlePortModCaseBuilder()
                    .setPortModCaseData(
                            new PortModCaseDataBuilder(portModInput.get())
                                    .setXid(xid)
                                    .build()
                    )
                    .build();
        } else {
            throw new ConversionException("BundlePortCase conversion unsuccessful.");
        }
    }

    private static BigInteger digDatapathId(final NodeRef ref) {
        return InventoryDataServiceUtil.dataPathIdFromNodeId(ref.getValue().firstKeyOf(Node.class).getId());
    }

    @Override
    public ExperimenterId getExperimenterId() {
        return new ExperimenterId(OnfConstants.ONF_EXPERIMENTER_ID);
    }

    @Override
    public long getType() {
        return OnfConstants.ONF_ET_BUNDLE_ADD_MESSAGE;
    }

    @Override
    public void setXid(long xid) {
        this.xid = xid;
    }

    @Override
    public void setNode(NodeRef node) {
        this.node = node;
    }
}
