/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.FlowRemovedMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.PacketInMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.PortStatusMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yangtools.yang.binding.DataContainer;


public class AsyncConfigResponseConvertor extends Convertor<org.opendaylight.yang.gen.v1
        .urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput, GetAsyncOutput, VersionConvertorData> {

    private static final List<Class<? extends DataContainer>> TYPES = Arrays.asList(org.opendaylight.yang.gen.v1
            .urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput.class);

    /**
     * Create default empty get async config output
     * Use this method, if result from device is empty.
     *
     * @return default empty get async config output
     */
    public static GetAsyncOutput defaultResult() {
        return new GetAsyncOutputBuilder()
                .build();
    }

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return TYPES;
    }

    @Override
    public GetAsyncOutput convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
                                              .GetAsyncOutput source, VersionConvertorData data) {

        GetAsyncOutputBuilder getAsyncOutputBuilder = new GetAsyncOutputBuilder();
        getAsyncOutputBuilder.setPortStatusMask(getPortStausMask(source.getPortStatusMask()));
        getAsyncOutputBuilder.setPacketInMask(getPacketInMask(source.getPacketInMask()));
        getAsyncOutputBuilder.setFlowRemovedMask(getFlowRemovedMask(source.getFlowRemovedMask()));

        return getAsyncOutputBuilder.build();
    }

    private PortStatusMask
        getPortStausMask(List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
            .async.body.grouping.PortStatusMask> masks) {

        if (Objects.isNull(masks)) {
            return null;
        }

        PortStatusMaskBuilder portStatusMaskBuilder = new PortStatusMaskBuilder();
        portStatusMaskBuilder.setMasterMask(getPortReasons(masks.get(0).getMask()));
        portStatusMaskBuilder.setSlaveMask(getPortReasons(masks.get(1).getMask()));

        return portStatusMaskBuilder.build();
    }

    private PacketInMask
        getPacketInMask(List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
            .async.body.grouping.PacketInMask> masks) {

        if (Objects.isNull(masks)) {
            return null;
        }

        PacketInMaskBuilder packetInMaskBuilder = new PacketInMaskBuilder();
        packetInMaskBuilder.setMasterMask(getPacketInReasons(masks.get(0).getMask()));
        packetInMaskBuilder.setSlaveMask(getPacketInReasons(masks.get(1).getMask()));

        return packetInMaskBuilder.build();
    }

    private FlowRemovedMask
        getFlowRemovedMask(List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
            .async.body.grouping.FlowRemovedMask> masks) {

        if (Objects.isNull(masks)) {
            return null;
        }

        FlowRemovedMaskBuilder flowRemovedMaskBuilder = new FlowRemovedMaskBuilder();
        flowRemovedMaskBuilder.setMasterMask(getFlowRemovedReasons(masks.get(0).getMask()));
        flowRemovedMaskBuilder.setSlaveMask(getFlowRemovedReasons(masks.get(1).getMask()));

        return flowRemovedMaskBuilder.build();
    }

    private org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PortStatusMask
        getPortReasons(List<PortReason> portReasons) {

        boolean isAdd = false;
        boolean isDelete= false;
        boolean isUpdate= false;

        for (PortReason portReason: portReasons) {
            if (PortReason.OFPPRADD.equals(portReason)) {
                isAdd = true;
            } else if (PortReason.OFPPRDELETE.equals(portReason)) {
                isDelete = true;
            } else if (PortReason.OFPPRMODIFY.equals(portReason)) {
                isUpdate = true;
            }
        }
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619
                .PortStatusMask(isAdd, isDelete, isUpdate);
    }

    private org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PacketInMask
        getPacketInReasons(List<PacketInReason> packetInReasons) {

        boolean isAction = false;
        boolean isInvalidTtl = false;
        boolean isNoMatch = false;

        for (PacketInReason packetInReason: packetInReasons) {
            if (PacketInReason.OFPRACTION.equals(packetInReason)) {
                isAction = true;
            } else if (PacketInReason.OFPRINVALIDTTL.equals(packetInReason)) {
                isInvalidTtl = true;
            } else if (PacketInReason.OFPRNOMATCH.equals(packetInReason)) {
                isNoMatch = true;
            }
        }
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619
                .PacketInMask(isAction, isInvalidTtl, isNoMatch);
    }

    private org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.FlowRemovedMask
        getFlowRemovedReasons(List<FlowRemovedReason> flowRemovedReasons) {

        boolean isDelete = false;
        boolean isGroupDelete = false;
        boolean isHardTimeout = false;
        boolean isIdleTimeout = false;

        for (FlowRemovedReason flowRemovedReason: flowRemovedReasons) {
            if (FlowRemovedReason.OFPRRDELETE.equals(flowRemovedReason)) {
                isDelete =  true;
            } else if (FlowRemovedReason.OFPRRGROUPDELETE.equals(flowRemovedReason)) {
                isGroupDelete = true;
            } else if (FlowRemovedReason.OFPRRHARDTIMEOUT.equals(flowRemovedReason)) {
                isHardTimeout = true;
            } else if (FlowRemovedReason.OFPRRIDLETIMEOUT.equals(flowRemovedReason)) {
                isIdleTimeout = true;
            }
        }
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619
                .FlowRemovedMask(isDelete, isGroupDelete, isHardTimeout, isIdleTimeout);
    }
}
