/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author esssuuu This convertor class is used for Port Mod,port status and
 *         port description messages,decodes SAL and encodes to OF Data
 *
 */
public final class PortConvertor {
    private static final Logger LOG = LoggerFactory.getLogger(PortConvertor.class);

    private PortConvertor() {

    }

    /**
     * This method is used by PORT_MOD_MESSAGE
     *
     * @param source source port
     * @param version openflow version
     * @return port mod input
     */

    public static PortModInput toPortModInput(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port source,
            short version) {


        PortConfig config = maskPortConfigFields(source.getConfiguration());
        PortConfigV10 configV10 = maskPortConfigV10Fields(source.getConfiguration());

        PortModInputBuilder portModInputBuilder = new PortModInputBuilder();
        portModInputBuilder.setAdvertise(getPortFeatures(source.getAdvertisedFeatures()));
        portModInputBuilder.setPortNo(new PortNumber(
                OpenflowPortsUtil.getProtocolPortNumber(OpenflowVersion.get(version), source.getPortNumber())));

        portModInputBuilder.setConfig(config);
        portModInputBuilder.setMask(config);

        portModInputBuilder.setHwAddress(new MacAddress(source.getHardwareAddress()));

        portModInputBuilder.setVersion(version);

        portModInputBuilder.setConfigV10(configV10);
        portModInputBuilder.setMaskV10(configV10);
        portModInputBuilder.setAdvertiseV10(getPortFeaturesV10(source.getAdvertisedFeatures()));
        return portModInputBuilder.build();

    }

    private static PortConfig maskPortConfigFields(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig configData) {
        Boolean portDown = configData.isPORTDOWN();
        Boolean noRecv = configData.isNORECV();
        Boolean noFwd = configData.isNOFWD();
        Boolean noPacketIn = configData.isNOPACKETIN();

        return new PortConfig(noFwd, noPacketIn, noRecv, portDown);

    }

    private static PortConfigV10 maskPortConfigV10Fields(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig configData) {
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


    /*
     * This method is called as a reply to OFPMP_PORT_DESCRIPTION
     * message(OF1.3.1)
     */
    /**
     * @param source FlowCapablePort
     * @param version openflow version
     * @return OF:Ports
     */
    public static Ports toPortDesc(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.FlowCapablePort source,
            short version) {

        PortConfig config = null;
        PortState portState = null;

        PortsBuilder oFPortDescDataBuilder = new PortsBuilder();

        oFPortDescDataBuilder.setPortNo(
                OpenflowPortsUtil.getProtocolPortNumber(OpenflowVersion.get(version), source.getPortNumber())); // portNO

        oFPortDescDataBuilder.setHwAddr(source.getHardwareAddress());
        oFPortDescDataBuilder.setName(source.getName());

        config = maskPortConfigFields(source.getConfiguration());

        oFPortDescDataBuilder.setConfig(config);

        portState = getPortState(source.getState());

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



}
