/*
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.PortsBuilder;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Converts port mod, port status and port description MD-SAL messages to OF library data
 *
 * Example usage:
 * <pre>
 * {@code
 * VersionConvertorData data = new VersionConvertorData(version);
 * Optional<PortModInput> ofPort = convertorManager.convert(salPort, data);
 * }
 * </pre>
 */
public class PortConvertor extends Convertor<Port, PortModInput, VersionConvertorData> {

    private static final Set<Class<? extends DataContainer>> TYPES = Collections.singleton(Port.class);

    /**
     * Create default empty port mod input
     * Use this method, if result from convertor is empty.
     *
     * @param version Openflow version
     * @return default empty port mod input
     */
    public static PortModInput defaultResult(short version) {
        return new PortModInputBuilder()
                .setVersion(version)
                .build();
    }

    private static PortConfig maskPortConfigFields(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig configData) {

        if (Objects.isNull(configData)) {
            return null;
        }

        Boolean portDown = configData.isPORTDOWN();
        Boolean noRecv = configData.isNORECV();
        Boolean noFwd = configData.isNOFWD();
        Boolean noPacketIn = configData.isNOPACKETIN();

        return new PortConfig(noFwd, noPacketIn, noRecv, portDown);

    }

    private static PortConfigV10 maskPortConfigV10Fields(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig configData) {

        if (Objects.isNull(configData)) {
            return null;
        }

        Boolean portDown = configData.isPORTDOWN();
        Boolean noRecv = configData.isNORECV();
        Boolean noFwd = configData.isNOFWD();
        Boolean noPacketIn = configData.isNOPACKETIN();

        return new PortConfigV10(false, noFwd, noPacketIn, noRecv, true, true, portDown);

    }

    private static PortFeatures getPortFeatures(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures salPortFeatures) {

        return new PortFeatures(salPortFeatures.isHundredGbFd(), salPortFeatures.isHundredMbFd(),
                salPortFeatures.isHundredMbHd(), salPortFeatures.isTenGbFd(), salPortFeatures.isTenMbFd(),
                salPortFeatures.isTenMbHd(), salPortFeatures.isOneGbFd(), salPortFeatures.isOneGbHd(),
                salPortFeatures.isOneTbFd(), salPortFeatures.isFortyGbFd(), salPortFeatures.isAutoeng(),
                salPortFeatures.isCopper(), salPortFeatures.isFiber(), salPortFeatures.isOther(),
                salPortFeatures.isPause(), salPortFeatures.isPauseAsym());
    }

    private static PortFeaturesV10 getPortFeaturesV10(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures salPortFeatures) {

        return new PortFeaturesV10(salPortFeatures.isHundredMbFd(), salPortFeatures.isHundredMbHd(), salPortFeatures.isTenGbFd(), salPortFeatures.isTenMbFd(), salPortFeatures.isTenMbHd(),
                salPortFeatures.isOneGbFd(), salPortFeatures.isOneGbHd(), salPortFeatures.isAutoeng(), salPortFeatures.isCopper(), salPortFeatures.isFiber(),
                salPortFeatures.isPause(), salPortFeatures.isPauseAsym());
    }

    /**
     * This method is called as a reply to OFPMP_PORT_DESCRIPTION
     * message(OF1.3.1)
     *
     * @param source  FlowCapablePort
     * @param version openflow version
     * @return OF:Ports
     */
    @VisibleForTesting
    static Ports toPortDesc(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.FlowCapablePort source,
            short version) {

        PortsBuilder oFPortDescDataBuilder = new PortsBuilder();

        oFPortDescDataBuilder.setPortNo(
                OpenflowPortsUtil.getProtocolPortNumber(OpenflowVersion.get(version), source.getPortNumber())); // portNO

        oFPortDescDataBuilder.setHwAddr(source.getHardwareAddress());
        oFPortDescDataBuilder.setName(source.getName());

        PortConfig config = maskPortConfigFields(source.getConfiguration());

        oFPortDescDataBuilder.setConfig(config);

        PortState portState = getPortState(source.getState());

        oFPortDescDataBuilder.setState(portState);
        oFPortDescDataBuilder.setCurrentFeatures(getPortFeatures(source.getCurrentFeature()));
        oFPortDescDataBuilder.setAdvertisedFeatures(getPortFeatures(source.getAdvertisedFeatures()));
        oFPortDescDataBuilder.setSupportedFeatures(getPortFeatures(source.getSupported()));
        oFPortDescDataBuilder.setPeerFeatures(getPortFeatures(source.getPeerFeatures()));
        oFPortDescDataBuilder.setCurrSpeed(source.getCurrentSpeed());
        oFPortDescDataBuilder.setMaxSpeed(source.getMaximumSpeed());

        return oFPortDescDataBuilder.build();

    }

    private static PortState getPortState(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState state) {

        boolean isLinkDown = state.isLinkDown();
        boolean isBlocked = state.isBlocked();
        boolean isLive = state.isLive();

        return new PortState(isLinkDown, isBlocked, isLive);

    }

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return TYPES;
    }

    @Override
    public PortModInput convert(Port source, VersionConvertorData data) {
        PortConfig config = maskPortConfigFields(source.getConfiguration());
        PortConfigV10 configV10 = maskPortConfigV10Fields(source.getConfiguration());

        PortModInputBuilder portModInputBuilder = new PortModInputBuilder();
        portModInputBuilder.setAdvertise(getPortFeatures(source.getAdvertisedFeatures()));
        portModInputBuilder.setPortNo(new PortNumber(
                OpenflowPortsUtil.getProtocolPortNumber(OpenflowVersion.get(data.getVersion()), source.getPortNumber())));

        portModInputBuilder.setConfig(config);
        portModInputBuilder.setMask(MoreObjects.firstNonNull(maskPortConfigFields(source.getMask()),
                new PortConfig(true, true, true, true)));

        portModInputBuilder.setHwAddress(new MacAddress(source.getHardwareAddress()));

        portModInputBuilder.setVersion(data.getVersion());

        portModInputBuilder.setConfigV10(configV10);
        portModInputBuilder.setMaskV10(MoreObjects.firstNonNull(maskPortConfigV10Fields(source.getMask()),
                new PortConfigV10(true, true, true, true, true, true, true)));
        portModInputBuilder.setAdvertiseV10(getPortFeaturesV10(source.getAdvertisedFeatures()));
        return portModInputBuilder.build();
    }
}