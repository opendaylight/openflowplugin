/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.State;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.shared.port.rev141119.PortStateV10;
import java.math.BigInteger;

public abstract class PortTranslatorUtil {
    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures translatePortFeatures(PortFeatures apf) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures napf = null;
        if (apf != null) {
            napf = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures(
                    apf.getPortFeaturesV13().isAutoneg(), //_autoeng
                    apf.getPortFeaturesV13().isCopper(), //_copper
                    apf.getPortFeaturesV13().isFiber(), //_fiber
                    apf.getPortFeaturesV13().is_40gbFd(), //_fortyGbFd
                    apf.getPortFeaturesV13().is_100gbFd(), //_hundredGbFd
                    apf.getPortFeaturesV13().is_100mbFd(), //_hundredMbFd
                    apf.getPortFeaturesV13().is_100mbHd(), //_hundredMbHd
                    apf.getPortFeaturesV13().is_1gbFd(), //_oneGbFd
                    apf.getPortFeaturesV13().is_1gbHd(), //_oneGbHd
                    apf.getPortFeaturesV13().is_1tbFd(), //_oneTbFd
                    apf.getPortFeaturesV13().isOther(), //_other
                    apf.getPortFeaturesV13().isPause(), //_pause
                    apf.getPortFeaturesV13().isPauseAsym(), //_pauseAsym
                    apf.getPortFeaturesV13().is_10gbFd(), //_tenGbFd
                    apf.getPortFeaturesV13().is_10mbFd(), //_tenMbFd
                    apf.getPortFeaturesV13().is_10mbHd()//_tenMbHd
            );

        }
        return napf;
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures translatePortFeatures(
            PortFeaturesV10 apf) {
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

    public static State translatePortState(PortState state) {
        StateBuilder nstate = new StateBuilder();
        if (state != null) {
            nstate.setBlocked(state.getPortStateV13().isBlocked());
            nstate.setLinkDown(state.getPortStateV13().isLinkDown());
            nstate.setLive(state.getPortStateV13().isLive());
        }
        return nstate.build();
    }

    public static State translatePortState(PortStateV10 state) {
        StateBuilder nstate = new StateBuilder();
        if (state != null) {
            nstate.setBlocked(state.isBlocked());
            nstate.setLinkDown(state.isLinkDown());
            nstate.setLive(state.isLive());
        }
        return nstate.build();
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig translatePortConfig(PortConfig pc) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig npc = null;
        if (pc != null) {
            npc = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig(pc.getPortConfigV13().isNoFwd(),
                    pc.getPortConfigV13().isNoPacketIn(), pc.getPortConfigV13().isNoRecv(), pc.getPortConfigV13().isPortDown());
        }
        return npc;
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig translatePortConfig(
            PortConfigV10 pc) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig npc = null;
        if (pc != null) {
            npc = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig(pc.isNoFwd(),
                    pc.isNoPacketIn(), pc.isNoRecv(), pc.isPortDown());
        }
        return npc;
    }

    public static NodeConnectorUpdated translatePort(Short version, BigInteger datapathId, Long portNumber, PortGrouping port) {
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
            fcncub.setAdvertisedFeatures(PortTranslatorUtil.translatePortFeatures(port.getAdvertisedFeatures().getPortFeaturesV10()));
            fcncub.setConfiguration(PortTranslatorUtil.translatePortConfig(port.getConfig().getPortConfigV10()));
            fcncub.setCurrentFeature(PortTranslatorUtil.translatePortFeatures(port.getCurrentFeatures().getPortFeaturesV10()));
            fcncub.setPeerFeatures(PortTranslatorUtil.translatePortFeatures(port.getPeerFeatures().getPortFeaturesV10()));
            fcncub.setState(PortTranslatorUtil.translatePortState(port.getState().getPortStateV10()));
            fcncub.setSupported(PortTranslatorUtil.translatePortFeatures(port.getSupportedFeatures().getPortFeaturesV10()));
        }
        fcncub.setCurrentSpeed(port.getCurrSpeed());
        fcncub.setHardwareAddress(port.getHwAddr());
        fcncub.setMaximumSpeed(port.getMaxSpeed());
        fcncub.setName(port.getName());
        fcncub.setPortNumber(OpenflowPortsUtil.getProtocolAgnosticPort(ofVersion, portNumber));
        builder.addAugmentation(FlowCapableNodeConnectorUpdated.class, fcncub.build());
        return builder.build();
    }

    public static NodeConnectorRemoved translatePortRemoved(Short version, BigInteger datapathId, Long portNumber, PortGrouping port) {
        OpenflowVersion ofVersion = OpenflowVersion.get(version);
        NodeConnectorRemovedBuilder builder = new NodeConnectorRemovedBuilder();
        builder.setNodeConnectorRef(InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(datapathId, portNumber, ofVersion));
        return builder.build();
    }
}
