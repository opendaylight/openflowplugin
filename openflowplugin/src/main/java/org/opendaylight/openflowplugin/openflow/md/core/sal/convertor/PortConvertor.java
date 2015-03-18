/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor: usha.m.s@ericsson.com
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.PortsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortConfigV13;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortFeaturesV13;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortStateV13;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author esssuuu This convertor class is used for Port Mod,port status and
 *         port description messages,decodes SAL and encodes to OF Data
 */
public final class PortConvertor {
    private static final Logger LOG = LoggerFactory.getLogger(PortConvertor.class);

    private PortConvertor() {

    }

    /**
     * This method is used by PORT_MOD_MESSAGE
     *
     * @param source
     * @return
     */

    public static PortModInput toPortModInput(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port source,
            short version) {


        PortConfig config = maskPortConfigFields(source.getConfiguration(), version);


        PortModInputBuilder portModInputBuilder = new PortModInputBuilder();
        portModInputBuilder.setAdvertise(getPortFeatures(source.getAdvertisedFeatures(), version));
        portModInputBuilder.setPortNo(new PortNumber(
                OpenflowPortsUtil.getProtocolPortNumber(OpenflowVersion.get(version), source.getPortNumber())));


        portModInputBuilder.setConfig(config);
        portModInputBuilder.setMask(config);

        portModInputBuilder.setHwAddress(new MacAddress(source.getHardwareAddress()));

        portModInputBuilder.setVersion(version);

        return portModInputBuilder.build();

    }

    private static PortConfig maskPortConfigFields(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig configData,
            short version) {
        Boolean portDown = configData.isPORTDOWN();
        Boolean noRecv = configData.isNORECV();
        Boolean noFwd = configData.isNOFWD();
        Boolean noPacketIn = configData.isNOPACKETIN();

        PortConfig portConfig = null;
        switch (version) {
            case 10:
                PortConfigV10 portConfigV10 = new PortConfigV10(false, noFwd, noPacketIn, noRecv, false, false, portDown);
                portConfig = new PortConfig(portConfigV10);
                break;
            case 13:
                PortConfigV13 portConfigV13 = new PortConfigV13(noFwd, noPacketIn, noRecv, portDown);
                portConfig = new PortConfig(portConfigV13);
                break;
        }
        return portConfig;

    }

    private static PortFeatures getPortFeatures(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures salPortFeatures,
            short version) {

        PortFeatures portFeatures = null;
        switch (version) {
            case 10:
                PortFeaturesV10 portFeaturesV10 = new PortFeaturesV10(salPortFeatures.isHundredMbFd(), salPortFeatures.isHundredMbHd(),
                        salPortFeatures.isTenGbFd(), salPortFeatures.isTenMbFd(), salPortFeatures.isTenMbHd(),
                        salPortFeatures.isOneGbFd(), salPortFeatures.isOneGbHd(), salPortFeatures.isAutoeng(),
                        salPortFeatures.isCopper(), salPortFeatures.isFiber(),
                        salPortFeatures.isPause(), salPortFeatures.isPauseAsym());
                portFeatures = new PortFeatures(portFeaturesV10);
                break;
            case 13:
                PortFeaturesV13 portFeaturesV13 = new PortFeaturesV13(salPortFeatures.isHundredGbFd(), salPortFeatures.isHundredMbFd(),
                        salPortFeatures.isHundredMbHd(), salPortFeatures.isTenGbFd(), salPortFeatures.isTenMbFd(),
                        salPortFeatures.isTenMbHd(), salPortFeatures.isOneGbFd(), salPortFeatures.isOneGbHd(),
                        salPortFeatures.isOneTbFd(), salPortFeatures.isFortyGbFd(), salPortFeatures.isAutoeng(),
                        salPortFeatures.isCopper(), salPortFeatures.isFiber(), salPortFeatures.isOther(),
                        salPortFeatures.isPause(), salPortFeatures.isPauseAsym());
                portFeatures = new PortFeatures(portFeaturesV13);
                break;
        }
        return portFeatures;
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
     * @param source :SAL FlowCapablePort
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

        config = maskPortConfigFields(source.getConfiguration(), version);

        oFPortDescDataBuilder.setConfig(config);

        portState = getPortState(source.getState(), version);

        oFPortDescDataBuilder.setState(portState);
        oFPortDescDataBuilder.setCurrentFeatures(getPortFeatures(source.getCurrentFeature(), version));
        oFPortDescDataBuilder.setAdvertisedFeatures(getPortFeatures(source.getAdvertisedFeatures(), version));
        oFPortDescDataBuilder.setSupportedFeatures(getPortFeatures(source.getSupported(), version));
        oFPortDescDataBuilder.setPeerFeatures(getPortFeatures(source.getPeerFeatures(), version));
        oFPortDescDataBuilder.setCurrSpeed(source.getCurrentSpeed());
        oFPortDescDataBuilder.setMaxSpeed(source.getMaximumSpeed());

        return oFPortDescDataBuilder.build();

    }

    private static PortState getPortState(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState state,
            short version) {

        boolean isLinkDown = state.isLinkDown();
        boolean isBlocked = state.isBlocked();
        boolean isLive = state.isLive();

        PortState portState = null;
        switch (version) {
            case 10:
                PortStateV10 portStateV10 = new PortStateV10(isBlocked, isLinkDown, isLive, false, false, false, false, false);
                portState = new PortState(portStateV10);
                break;
            case 13:
                PortStateV13 portStateV13 = new PortStateV13(isBlocked, isLinkDown, isLive);
                portState = new PortState(portStateV13);
                break;
        }
        return portState;

    }


}
