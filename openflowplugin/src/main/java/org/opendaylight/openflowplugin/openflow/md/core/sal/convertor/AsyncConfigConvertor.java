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

    private List<PortStatusMask> getPortStatusMask(
            org.opendaylight.yang.gen.v1.urn.opendaylight
                    .async.config.service.rev170619.async.config.PortStatusMask mask) {

        if (Objects.isNull(mask)) {
            return null;
        }

        List<PortStatusMask> portStatusMasks = new ArrayList<>();

        portStatusMasks.add(new PortStatusMaskBuilder()
                .setMask(getPortReasons(mask.getMasterMask()))
                .build());
        portStatusMasks.add(new PortStatusMaskBuilder()
                .setMask(getPortReasons(mask.getSlaveMask()))
                .build());

        return portStatusMasks;
    }

    private List<PortReason> getPortReasons(
            org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PortStatusMask mask) {
        List<PortReason> portReasons = new ArrayList<>();

        if (mask.isADD()) {
            portReasons.add(PortReason.OFPPRADD);
        }
        if (mask.isDELETE()) {
            portReasons.add(PortReason.OFPPRDELETE);
        }
        if (mask.isUPDATE()) {
            portReasons.add(PortReason.OFPPRMODIFY);
        }
        return portReasons;
    }

    private List<PacketInMask> getPacketInMask(
            org.opendaylight.yang.gen.v1.urn.opendaylight
                    .async.config.service.rev170619.async.config.PacketInMask mask) {

        if (Objects.isNull(mask)) {
            return null;
        }

        List<PacketInMask> packetInMasks = new ArrayList<>();

        packetInMasks.add(new PacketInMaskBuilder()
                .setMask(getPacketInReasons(mask.getMasterMask()))
                .build());
        packetInMasks.add(new PacketInMaskBuilder()
                .setMask(getPacketInReasons(mask.getSlaveMask()))
                .build());

        return packetInMasks;
    }

    private List<PacketInReason> getPacketInReasons(
            org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PacketInMask mask) {
        List<PacketInReason> packetInReasons = new ArrayList<>();

        if (mask.isNOMATCH()) {
            packetInReasons.add(PacketInReason.OFPRNOMATCH);
        }
        if (mask.isACTION()) {
            packetInReasons.add(PacketInReason.OFPRACTION);
        }
        if (mask.isINVALIDTTL()) {
            packetInReasons.add(PacketInReason.OFPRINVALIDTTL);
        }

        return packetInReasons;
    }

    private List<FlowRemovedMask> getFlowRemovedMask(
            org.opendaylight.yang.gen.v1.urn.opendaylight
                    .async.config.service.rev170619.async.config.FlowRemovedMask mask) {

        if (Objects.isNull(mask)) {
            return null;
        }

        List<FlowRemovedMask> flowRemovedMasks = new ArrayList<>();

        flowRemovedMasks.add(new FlowRemovedMaskBuilder()
                .setMask(getFlowRemovedReasons(mask.getMasterMask()))
                .build());
        flowRemovedMasks.add(new FlowRemovedMaskBuilder()
                .setMask(getFlowRemovedReasons(mask.getSlaveMask()))
                .build());

        return flowRemovedMasks;
    }

    private List<FlowRemovedReason> getFlowRemovedReasons(
            org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.FlowRemovedMask mask) {
        List<FlowRemovedReason> flowRemovedReasons = new ArrayList<>();

        if (mask.isIDLETIMEOUT()) {
            flowRemovedReasons.add(FlowRemovedReason.OFPRRIDLETIMEOUT);
        }
        if (mask.isHARDTIMEOUT()) {
            flowRemovedReasons.add(FlowRemovedReason.OFPRRHARDTIMEOUT);
        }
        if (mask.isDELETE()) {
            flowRemovedReasons.add(FlowRemovedReason.OFPRRDELETE);
        }
        if (mask.isGROUPDELETE()) {
            flowRemovedReasons.add(FlowRemovedReason.OFPRRGROUPDELETE);
        }

        return flowRemovedReasons;
    }

}
