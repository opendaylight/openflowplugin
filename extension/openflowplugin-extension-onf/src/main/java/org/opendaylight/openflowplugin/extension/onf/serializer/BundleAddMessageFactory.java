/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.onf.serializer;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.BundleInnerMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleFlowModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundleGroupModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundlePortModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessageOnf;

/**
 * Translates BundleAddMessage messages (OpenFlow v1.3 extension #230).
 */
public class BundleAddMessageFactory extends AbstractBundleMessageFactory<BundleAddMessageOnf> {

    @Override
    public void serialize(BundleAddMessageOnf input, ByteBuf outBuffer) {
        outBuffer.writeInt(input.getOnfAddMessageGroupingData().getBundleId().getValue().intValue());
        outBuffer.writeZero(2);
        writeBundleFlags(input.getOnfAddMessageGroupingData().getFlags(), outBuffer);

        int msgStart = outBuffer.writerIndex();
        final BundleInnerMessage message = input.getOnfAddMessageGroupingData().getBundleInnerMessage();
        serializeInnerMessage(message, outBuffer);
        int msgLength = outBuffer.writerIndex() - msgStart;

        List<BundleProperty> bundleProperties = input.getOnfAddMessageGroupingData().getBundleProperty();
        if (bundleProperties != null && !bundleProperties.isEmpty()) {
            outBuffer.writeZero(paddingNeeded(msgLength));
            writeBundleProperties(input.getOnfAddMessageGroupingData().getBundleProperty(), outBuffer);
        }
    }

    private void serializeInnerMessage(final BundleInnerMessage message, final ByteBuf outBuffer) {
        final Class<?> clazz = message.implementedInterface();
        if (clazz.equals(BundleFlowModCase.class)) {
            OFSerializer<FlowMod> serializer = serializerRegistry.getSerializer(
                    new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, FlowModInput.class));
            serializer.serialize(((BundleFlowModCase)message)
                    .getFlowModCaseData(), outBuffer);
        } else if (clazz.equals(BundleGroupModCase.class)) {
            OFSerializer<GroupMod> serializer = serializerRegistry.getSerializer(
                    new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, GroupModInput.class));
            serializer.serialize(((BundleGroupModCase)message)
                    .getGroupModCaseData(), outBuffer);
        } else if (clazz.equals(BundlePortModCase.class)) {
            OFSerializer<PortMod> serializer = serializerRegistry.getSerializer(
                    new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, PortModInput.class));
            serializer.serialize(((BundlePortModCase)message)
                    .getPortModCaseData(), outBuffer);
        }
    }

    private static int paddingNeeded(final int length) {
        int paddingRemainder = length % EncodeConstants.PADDING;
        return (paddingRemainder != 0) ? (EncodeConstants.PADDING - paddingRemainder) : 0;
    }

}
