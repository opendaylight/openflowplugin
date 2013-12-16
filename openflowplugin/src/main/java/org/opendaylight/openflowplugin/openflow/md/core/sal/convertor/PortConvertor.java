package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import org.opendaylight.openflowplugin.openflow.md.util.PortTranslatorUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.PortsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
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
     * @param input
     * @return
     */

    public static PortModInput toPortModInput(
            UpdatePortInput input,
            short version) {


        PortConfig config = null;
        PortConfig mask = null;

        PortModInputBuilder portModInputBuilder = new PortModInputBuilder();
        /* 
         * PortMod advertised features should be zeroed out per
         * OF 1.3 spec section 7.3.4.3 page 70
         */
        portModInputBuilder.setAdvertise(new PortFeatures(false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false, false)); 
        portModInputBuilder.setPortNo(PortTranslatorUtil.nodeConnectorRefToPortNumber(input.getNodeConnectorRef()));
        maskPortConfigFields(input, portModInputBuilder);
        portModInputBuilder.setHwAddress(input.getHardwareAddress());
        portModInputBuilder.setVersion(version);
        return portModInputBuilder.build();

    }

    private static void maskPortConfigFields(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig configData,
            PortModInputBuilder portModInputBuilder) {
        Boolean portDown = false;
        Boolean noRecv = false;
        Boolean noFwd = false;
        Boolean noPacketIn = false;
        Boolean portDownMask = false;
        Boolean noRecvMask = false;
        Boolean noFwdMask = false;
        Boolean noPacketInMask = false;
        if (configData.isPortNoFwd() != null) {
            noFwd = configData.isPortNoFwd();
            noFwdMask = true;
        }
        if (configData.isPortNoPacketIn()!= null) {
            noPacketIn = configData.isPortNoPacketIn();
            noPacketInMask = true;
        }
        if (configData.isPortNoRecv() != null) {
            noRecv = configData.isPortNoRecv();
            noRecvMask = true;
        }
        if (configData.isPortDown() != null) {
            portDown = configData.isPortDown();
            portDown = true;
        }
        portModInputBuilder.setConfig(new PortConfig(noFwd, noPacketIn, noRecv, portDown));
        portModInputBuilder.setMask(new PortConfig(noFwdMask, noPacketInMask, noRecvMask, portDownMask));

    }

    private static PortFeatures getPortFeatures(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures salPortFeatures) {

        return new PortFeatures(salPortFeatures.is_100gbFd(), salPortFeatures.is_100mbFd(),
                salPortFeatures.is_100mbHd(), salPortFeatures.is_10gbFd(), salPortFeatures.is_10mbFd(),
                salPortFeatures.is_10mbHd(), salPortFeatures.is_1gbFd(), salPortFeatures.is_1gbHd(),
                salPortFeatures.is_1tbFd(), salPortFeatures.is_40gbFd(), salPortFeatures.isAutoeng(),
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

        // maskPortConfigFields(source.getConfiguration(), config);

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

        if (state.getIntValue() == 0) {
            isLinkDown = true;
        } else if (state.getIntValue() == 1) {
            isBlocked = true;
        } else if (state.getIntValue() == 2) {
            isLive = true;
        }
        portState = new PortState(isLinkDown, isBlocked, isLive);

    }



}
