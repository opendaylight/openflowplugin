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
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.extension.onf.OnfConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContextRef;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleFlowModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleFlowModCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleGroupModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleGroupModCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundlePortModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundlePortModCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInputBuilder;

/**
 * Converter for BundleAddMessage messages (ONF approved extension #230).
 */
public class BundleAddMessageConverter implements
        ConvertorMessageToOFJava<BundleAddMessage,
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessage> {

    private static final ConvertorExecutor converterExecutor = ConvertorManagerFactory.createDefaultManager();
    private static final VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_3);

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessage
            convert(BundleAddMessage experimenterMessageCase) throws ConversionException {
        final BundleAddMessageBuilder builder = new BundleAddMessageBuilder();
        builder.setBundleId(experimenterMessageCase.getBundleId());
        builder.setFlags(experimenterMessageCase.getFlags());
        builder.setBundleProperty(experimenterMessageCase.getBundleProperty());
        final BundleInnerMessage innerMessage = experimenterMessageCase.getBundleInnerMessage();
        final Class clazz = innerMessage.getImplementedInterface();
        data.setDatapathId(digDatapathId((NodeContextRef)innerMessage));

        if (innerMessage instanceof Flow) {
            builder.setBundleInnerMessage(convertBundleFlowCase(innerMessage));
        } else if (innerMessage instanceof Group) {
            builder.setBundleInnerMessage(convertBundleGroupCase(innerMessage));
        } else if (innerMessage instanceof PortMod) {
            builder.setBundleInnerMessage(convertBundlePortCase(innerMessage));
        } else {
            throw new ConversionException("Unsupported inner message");
        }

        return builder.build();
    }

    private BundleFlowModCase convertBundleFlowCase(final BundleInnerMessage messageCase) throws ConversionException {
        Optional<List<FlowModInputBuilder>> flowModInputs = Optional.empty();
        final Class clazz = messageCase.getImplementedInterface();
        if (clazz.equals(BundleAddFlowCase.class)) {
            flowModInputs = converterExecutor.convert(new AddFlowInputBuilder((Flow)messageCase).build(), data);
        } else if (clazz.equals(BundleUpdateFlowCase.class)) {
            flowModInputs = converterExecutor.convert(new UpdateFlowInputBuilder((BundleUpdateFlowCase)messageCase).build(), data);
        } else if (clazz.equals(BundleRemoveFlowCase.class)) {
            flowModInputs = converterExecutor.convert(new RemoveFlowInputBuilder((Flow)messageCase).build(), data);
        }

        if (flowModInputs.isPresent()) {
            return new BundleFlowModCaseBuilder(flowModInputs.get().get(0).build()).build();
        } else {
            throw new ConversionException("BundleFlowCase conversion unsuccessful.");
        }
    }

    private BundleGroupModCase convertBundleGroupCase(final BundleInnerMessage messageCase) throws ConversionException {
        Optional<GroupModInputBuilder> groupModInput = Optional.empty();
        final Class clazz = messageCase.getImplementedInterface();
        if (clazz.equals(BundleAddGroupCase.class)) {
            groupModInput = converterExecutor.convert(new AddGroupInputBuilder((Group)messageCase).build(), data);
        } else if (clazz.equals(BundleUpdateGroupCase.class)) {
            groupModInput = converterExecutor.convert(new UpdateGroupInputBuilder((BundleUpdateGroupCase)messageCase).build(), data);
        } else if (clazz.equals(BundleRemoveGroupCase.class)) {
            groupModInput = converterExecutor.convert(new RemoveGroupInputBuilder((Group)messageCase).build(), data);
        }

        if (groupModInput.isPresent()) {
            return new BundleGroupModCaseBuilder(groupModInput.get().build()).build();
        } else {
            throw new ConversionException("BundleGroupCase conversion unsuccessful.");
        }
    }

    private BundlePortModCase convertBundlePortCase(final BundleInnerMessage messageCase) throws ConversionException {
        Optional<PortModInput> portModInput = Optional.empty();
        final Class clazz = messageCase.getImplementedInterface();
        if (clazz.equals(BundleUpdatePortCase.class)) {
            portModInput = converterExecutor.convert(new UpdatePortInputBuilder((BundleUpdatePortCase)messageCase).build(), data);
        }

        if (portModInput.isPresent()) {
            return new BundlePortModCaseBuilder(portModInput.get()).build();
        } else {
            throw new ConversionException("BundlePortCase conversion unsuccessful.");
        }
    }

    private static BigInteger digDatapathId(final NodeContextRef ref) {
        return InventoryDataServiceUtil.dataPathIdFromNodeId(ref.getNode().getValue().firstKeyOf(Node.class).getId());
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
