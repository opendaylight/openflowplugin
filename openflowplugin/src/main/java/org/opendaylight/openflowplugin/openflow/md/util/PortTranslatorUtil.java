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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.State;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;

public abstract class PortTranslatorUtil {
    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures translatePortFeatures(PortFeatures apf) {
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
            nstate.setBlocked(state.isBlocked());
            nstate.setLinkDown(state.isLinkDown());
            nstate.setLive(state.isLive());
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
            npc = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig(pc.isNoFwd(),
                    pc.isNoPacketIn(), pc.isNoRecv(), pc.isPortDown());
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
            fcncub.setAdvertisedFeatures(PortTranslatorUtil.translatePortFeatures(port.getAdvertisedFeaturesV10()));
            fcncub.setConfiguration(PortTranslatorUtil.translatePortConfig(port.getConfigV10()));
            fcncub.setCurrentFeature(PortTranslatorUtil.translatePortFeatures(port.getCurrentFeaturesV10()));
            fcncub.setPeerFeatures(PortTranslatorUtil.translatePortFeatures(port.getPeerFeaturesV10()));
            fcncub.setState(PortTranslatorUtil.translatePortState(port.getStateV10()));
            fcncub.setSupported(PortTranslatorUtil.translatePortFeatures(port.getSupportedFeaturesV10()));
        }
        fcncub.setCurrentSpeed(port.getCurrSpeed());
        fcncub.setHardwareAddress(port.getHwAddr());
        fcncub.setMaximumSpeed(port.getMaxSpeed());
        fcncub.setName(port.getName());
        fcncub.setPortNumber(OpenflowPortsUtil.getProtocolAgnosticPort(ofVersion, portNumber));
        builder.addAugmentation(FlowCapableNodeConnectorUpdated.class, fcncub.build());
        return builder.build();
    }
}
