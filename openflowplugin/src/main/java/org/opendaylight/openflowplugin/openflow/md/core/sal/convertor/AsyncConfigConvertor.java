/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.AsyncConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.RemovedFlowReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.InvalidTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.NoMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.SendToController;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Converts async config MD-SAL messages to OF library data
 *
 * Example usage:
 * <pre>
 * {@code
 * VersionConvertorData data = new VersionConvertorData(version);
 * Optional<SetAsyncInput> ofSetAsync = convertorManager.convert(salAsyncConfig, data);
 * }
 * </pre>
 */
public class AsyncConfigConvertor extends Convertor<AsyncConfig, SetAsyncInput, VersionConvertorData> {
    private static final List<Class<? extends DataContainer>> TYPES = Arrays.asList(org.opendaylight.yang.gen.v1
            .urn.opendaylight.async.config.service.rev170619
            .SetAsyncInput.class, AsyncConfig.class);

    /**
     * Create default empty async config input
     * Use this method, if result from convertor is empty.
     *
     * @param version Openflow version
     * @return default empty async config input
     */
    public static SetAsyncInput defaultResult(short version) {
        return new SetAsyncInputBuilder()
                .setVersion(version)
                .build();
    }

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return TYPES;
    }

    @Override
    public SetAsyncInput convert(AsyncConfig source, VersionConvertorData data) {

        SetAsyncInputBuilder asyncInputBuilder = new SetAsyncInputBuilder();
        asyncInputBuilder.setFlowRemovedMask(getFlowRemovedMask(source.getFlowRemovedMask()));
        asyncInputBuilder.setPacketInMask(getPacketInMask(source.getPacketInMask()));
        asyncInputBuilder.setPortStatusMask(getPortStatusMask(source.getPortStatusMask()));
        asyncInputBuilder.setVersion(data.getVersion());

        return asyncInputBuilder.build();
    }

    private List<PortStatusMask> getPortStatusMask(List<org.opendaylight.yang.gen.v1.urn.opendaylight
            .async.config.service.rev170619.async.config.PortStatusMask> masks) {

        if (Objects.isNull(masks)) {
            return null;
        }

        List<PortStatusMask> portStatusMasks = new ArrayList<>();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619
                .async.config.PortStatusMask portStatusMask : masks) {

            List<PortReason> portReasons = new ArrayList<>();

            for (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortReason
                    portReason: portStatusMask.getMask()) {
                if (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortReason
                        .Add.equals(portReason)) {
                    portReasons.add(PortReason.OFPPRADD);
                } else if (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortReason
                        .Delete.equals(portReason)) {
                    portReasons.add(PortReason.OFPPRDELETE);
                } else if (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortReason
                        .Update.equals(portReason)) {
                    portReasons.add(PortReason.OFPPRMODIFY);
                }
            }
            portStatusMasks.add(new PortStatusMaskBuilder()
                    .setMask(portReasons)
                    .build());
        }

        return portStatusMasks;
    }

    private List<PacketInMask> getPacketInMask(List<org.opendaylight.yang.gen.v1.urn.opendaylight
            .async.config.service.rev170619.async.config.PacketInMask> masks) {

        if (Objects.isNull(masks)) {
            return null;
        }

        List<PacketInMask> packetInMasks = new ArrayList<>();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619
                .async.config.PacketInMask packetInMask: masks) {

            List<PacketInReason> packetInReasons = new ArrayList<>();

            for (Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketInReason>
                    packetInReason: packetInMask.getMask()) {

                if (NoMatch.class.equals(packetInReason)) {
                    packetInReasons.add(PacketInReason.OFPRNOMATCH);
                } else if (SendToController.class.equals(packetInReason)) {
                    packetInReasons.add(PacketInReason.OFPRACTION);
                } else if (InvalidTtl.class.equals(packetInReason)) {
                    packetInReasons.add(PacketInReason.OFPRINVALIDTTL);
                }
            }
            packetInMasks.add(new PacketInMaskBuilder()
                    .setMask(packetInReasons)
                    .build());
        }

        return packetInMasks;
    }

    private List<FlowRemovedMask> getFlowRemovedMask(List<org.opendaylight.yang.gen.v1.urn.opendaylight
            .async.config.service.rev170619.async.config.FlowRemovedMask> masks) {

        if (Objects.isNull(masks)) {
            return null;
        }

        List<FlowRemovedMask> flowRemovedMasks = new ArrayList<>();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619
                .async.config.FlowRemovedMask flowRemovedMask: masks) {

            List<FlowRemovedReason> flowRemovedReasons = new ArrayList<>();

            for (RemovedFlowReason removedFlowReason: flowRemovedMask.getMask()) {
                if (RemovedFlowReason.OFPRRIDLETIMEOUT.equals(removedFlowReason)) {
                    flowRemovedReasons.add(FlowRemovedReason.OFPRRIDLETIMEOUT);
                } else if (RemovedFlowReason.OFPRRHARDTIMEOUT.equals(removedFlowReason)) {
                    flowRemovedReasons.add(FlowRemovedReason.OFPRRHARDTIMEOUT);
                } else if (RemovedFlowReason.OFPRRDELETE.equals(removedFlowReason)) {
                    flowRemovedReasons.add(FlowRemovedReason.OFPRRDELETE);
                } else if (RemovedFlowReason.OFPRRGROUPDELETE.equals(removedFlowReason)) {
                    flowRemovedReasons.add(FlowRemovedReason.OFPRRGROUPDELETE);
                }
            }
            flowRemovedMasks.add(new FlowRemovedMaskBuilder()
                    .setMask(flowRemovedReasons)
                    .build());
        }

        return flowRemovedMasks;
    }

}
