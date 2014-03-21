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

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
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
    private static final Logger log = LoggerFactory.getLogger(PortConvertor.class);

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


        PortConfig config = null;

        PortModInputBuilder portModInputBuilder = new PortModInputBuilder();
        portModInputBuilder.setAdvertise(getPortFeatures(source.getAdvertisedFeatures()));
        portModInputBuilder.setPortNo(new PortNumber(source.getPortNumber()));
        config = maskPortConfigFields(source.getConfiguration());
        portModInputBuilder.setConfig(config);
        portModInputBuilder.setHwAddress(new MacAddress(source.getHardwareAddress()));
        config = maskPortConfigFields(source.getMask());
        portModInputBuilder.setMask(config);
        portModInputBuilder.setVersion(version);
        return portModInputBuilder.build();

    }

    private static PortConfig maskPortConfigFields(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig configData) {
        Boolean portDown = false;
        Boolean noRecv = false;
        Boolean noFwd = false;
        Boolean noPacketIn = false;
        if (configData.isNOFWD())
            noFwd = true;
        if (configData.isNOPACKETIN())
            noPacketIn = true;
        if (configData.isNORECV())
            noRecv = true;
        if (configData.isPORTDOWN())
            portDown = true;

        return new PortConfig(noFwd, noPacketIn, noRecv, portDown);

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

    /*
     * This method is called as a reply to OFPMP_PORT_DESCRIPTION
     * message(OF1.3.1)
     */
    /**
     * @param source
     *            :SAL FlowCapablePort
     * @return OF:Ports
     */
    public static Ports toPortDesc(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.FlowCapablePort source) {

        PortConfig config = null;
        PortState portState = null;

        PortsBuilder OFPortDescDataBuilder = new PortsBuilder();
        OFPortDescDataBuilder.setPortNo(source.getPortNumber()); // portNO

        OFPortDescDataBuilder.setHwAddr(source.getHardwareAddress());
        OFPortDescDataBuilder.setName(source.getName());

        config = maskPortConfigFields(source.getConfiguration());

        OFPortDescDataBuilder.setConfig(config);

        getPortState(source.getState(), portState);
        OFPortDescDataBuilder.setState(portState);
        OFPortDescDataBuilder.setCurrentFeatures(getPortFeatures(source.getCurrentFeature()));
        OFPortDescDataBuilder.setAdvertisedFeatures(getPortFeatures(source.getAdvertisedFeatures()));
        OFPortDescDataBuilder.setSupportedFeatures(getPortFeatures(source.getSupported()));
        OFPortDescDataBuilder.setPeerFeatures(getPortFeatures(source.getPeerFeatures()));
        OFPortDescDataBuilder.setCurrSpeed(source.getCurrentSpeed());
        OFPortDescDataBuilder.setMaxSpeed(source.getMaximumSpeed());

        return OFPortDescDataBuilder.build();

    }

    private static void getPortState(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState state, PortState portState) {

        boolean isLinkDown = false;// (0),
        boolean isBlocked = false; // (1),
        boolean isLive = false; // (2);

        if (state.isLinkDown()) {
            isLinkDown = true;
        } else if (state.isBlocked()) {
            isBlocked = true;
        } else if (state.isLive()) {
            isLive = true;
        }
        portState = new PortState(isLinkDown, isBlocked, isLive);

    }



}
