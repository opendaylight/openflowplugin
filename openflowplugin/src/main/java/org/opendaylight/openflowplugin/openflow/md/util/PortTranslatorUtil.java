/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import java.math.BigInteger;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.State;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PortTranslatorUtil {
    private static final Logger LOG = LoggerFactory.getLogger(PortTranslatorUtil.class);
    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures translatePortFeatures(final PortFeatures apf) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures napf = null;
        if (apf != null) {
            napf = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures(
                    apf.isAutoneg(), //_autoeng
                    apf.isCopper(), //_copper
                    apf.isFiber(), //_fiber
                    apf.is_40gbFd(), //_fortyGbFd
                    apf.is_100gbFd(), //_hundredGbFd
                    apf.is_100mbFd(), //_hundredMbFd
                    apf.is_100mbHd(), //_hundredMbHd
                    apf.is_1gbFd(), //_oneGbFd
                    apf.is_1gbHd(), //_oneGbHd
                    apf.is_1tbFd(), //_oneTbFd
                    apf.isOther(), //_other
                    apf.isPause(), //_pause
                    apf.isPauseAsym(), //_pauseAsym
                    apf.is_10gbFd(), //_tenGbFd
                    apf.is_10mbFd(), //_tenMbFd
                    apf.is_10mbHd()//_tenMbHd
            );

        }
        return napf;
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures translatePortFeatures(
            final PortFeaturesV10 apf) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures napf = null;
        if (apf != null) {
            napf = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures(
                    apf.isAutoneg(), //_autoeng
                    apf.isCopper(), //_copper
                    apf.isFiber(), //_fiber
                    false, //_fortyGbFd
                    false, //_hundredGbFd
                    apf.is_100mbFd(), //_hundredMbFd
                    apf.is_100mbHd(), //_hundredMbHd
                    apf.is_1gbFd(), //_oneGbFd
                    apf.is_1gbHd(), //_oneGbHd
                    false, //_oneTbFd
                    false, //_other
                    apf.isPause(), //_pause
                    apf.isPauseAsym(), //_pauseAsym
                    apf.is_10gbFd(), //_tenGbFd
                    apf.is_10mbFd(), //_tenMbFd
                    apf.is_10mbHd()//_tenMbHd
                    );
        }
        return napf;
    }

    public static State translatePortState(final PortState state) {
        StateBuilder nstate = new StateBuilder();
        if (state != null) {
            nstate.setBlocked(state.isBlocked());
            nstate.setLinkDown(state.isLinkDown());
            nstate.setLive(state.isLive());
        }
        return nstate.build();
    }

    public static State translatePortState(final PortStateV10 state) {
        StateBuilder nstate = new StateBuilder();
        if (state != null) {
            nstate.setBlocked(state.isBlocked());
            nstate.setLinkDown(state.isLinkDown());
            nstate.setLive(state.isLive());
        }
        return nstate.build();
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig translatePortConfig(final PortConfig pc) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig npc = null;
        if (pc != null) {
            npc = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig(pc.isNoFwd(),
                    pc.isNoPacketIn(), pc.isNoRecv(), pc.isPortDown());
        }
        return npc;
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig translatePortConfig(
            final PortConfigV10 pc) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig npc = null;
        if (pc != null) {
            npc = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig(pc.isNoFwd(),
                    pc.isNoPacketIn(), pc.isNoRecv(), pc.isPortDown());
        }
        return npc;
    }

    public static NodeConnectorUpdated translatePort(final Short version, final BigInteger datapathId, final Long portNumber, final PortGrouping port) {
        OpenflowVersion ofVersion = OpenflowVersion.get(version);
        NodeConnectorUpdatedBuilder builder = InventoryDataServiceUtil
                .nodeConnectorUpdatedBuilderFromDatapathIdPortNo(datapathId, port.getPortNo(), ofVersion);
        FlowCapableNodeConnectorUpdatedBuilder fcncub = new FlowCapableNodeConnectorUpdatedBuilder();
        if (ofVersion == OpenflowVersion.OF13) {
            fcncub.setAdvertisedFeatures(PortTranslatorUtil.translatePortFeatures(port.getAdvertisedFeatures()));
            fcncub.setConfiguration(PortTranslatorUtil.translatePortConfig(port.getConfig()));
            fcncub.setCurrentFeature(PortTranslatorUtil.translatePortFeatures(port.getCurrentFeatures()));
            fcncub.setPeerFeatures(PortTranslatorUtil.translatePortFeatures(port.getPeerFeatures()));
            fcncub.setState(PortTranslatorUtil.translatePortState(port.getState()));
            fcncub.setSupported(PortTranslatorUtil.translatePortFeatures(port.getSupportedFeatures()));

        } else if (ofVersion == OpenflowVersion.OF10) {
            fcncub.setAdvertisedFeatures(PortTranslatorUtil.translatePortFeatures(port.getAdvertisedFeaturesV10()));
            fcncub.setConfiguration(PortTranslatorUtil.translatePortConfig(port.getConfigV10()));
            fcncub.setCurrentFeature(PortTranslatorUtil.translatePortFeatures(port.getCurrentFeaturesV10()));
            fcncub.setPeerFeatures(PortTranslatorUtil.translatePortFeatures(port.getPeerFeaturesV10()));
            fcncub.setState(PortTranslatorUtil.translatePortState(port.getStateV10()));
            fcncub.setSupported(PortTranslatorUtil.translatePortFeatures(port.getSupportedFeaturesV10()));
        }
        if (port instanceof PortStatusMessage) {
            if (((PortStatusMessage) port).getReason() != null) {
                fcncub.setReason(PortReason.forValue(((PortStatusMessage) port).getReason().getIntValue()));
            }else {
                LOG.debug("PortStatus Message has reason as null");
            }
        }
        fcncub.setCurrentSpeed(port.getCurrSpeed());
        fcncub.setHardwareAddress(port.getHwAddr());
        fcncub.setMaximumSpeed(port.getMaxSpeed());
        fcncub.setName(port.getName());
        fcncub.setPortNumber(OpenflowPortsUtil.getProtocolAgnosticPort(ofVersion, portNumber));
        builder.addAugmentation(FlowCapableNodeConnectorUpdated.class, fcncub.build());
        return builder.build();
    }

    public static NodeConnectorRemoved translatePortRemoved(final Short version, final BigInteger datapathId, final Long portNumber, final PortGrouping port) {
        OpenflowVersion ofVersion = OpenflowVersion.get(version);
        NodeConnectorRemovedBuilder builder = new NodeConnectorRemovedBuilder();
        builder.setNodeConnectorRef(InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(datapathId, portNumber, ofVersion));
        return builder.build();
    }
}
