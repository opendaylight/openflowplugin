/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import java.math.BigInteger;

import org.opendaylight.openflowplugin.openflow.md.OFConstants;
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

public class PortTranslatorUtil {
    public static  org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures translatePortFeatures(PortFeatures apf) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures napf = null;
        if(apf != null){
                napf = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures(
                        apf.is_100gbFd(),apf.is_100mbFd(),apf.is_100mbHd(),apf.is_10gbFd(),apf.is_10mbFd(),apf.is_10mbHd(),
                        apf.is_1gbFd(),apf.is_1gbHd(),apf.is_1tbFd(),apf.is_40gbFd(),apf.isAutoneg(),apf.isCopper(),apf.isFiber(),apf.isOther(),
                        apf.isPause(), apf.isPauseAsym());
        }
        return napf;
    }
    
    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures translatePortFeatures(
            PortFeaturesV10 apf) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures napf = null;
        if(apf != null){
                napf = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures(
                        false,apf.is_100mbFd(),apf.is_100mbHd(),apf.is_10gbFd(),apf.is_10mbFd(),apf.is_10mbHd(),
                        apf.is_1gbFd(),apf.is_1gbHd(),false,false,apf.isAutoneg(),apf.isCopper(),apf.isFiber(),false,
                        apf.isPause(), apf.isPauseAsym());
        }
        return napf;
    }

    public static  State translatePortState(PortState state) {
        StateBuilder nstate = new StateBuilder();
        if(state !=null) {
            nstate.setBlocked(state.isBlocked());
            nstate.setLinkDown(state.isLinkDown());
            nstate.setLive(state.isLive());
        }
        return nstate.build();
    }
    
    public static State translatePortState(PortStateV10 state) {
        StateBuilder nstate = new StateBuilder();
        if(state !=null) {
            nstate.setBlocked(state.isBlocked());
            nstate.setLinkDown(state.isLinkDown());
            nstate.setLive(state.isLive());
        }
        return nstate.build();
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig translatePortConfig(PortConfig pc) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig npc = null;
        if(pc != null) {
                npc = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig(pc.isNoFwd(),
                        pc.isNoPacketIn(), pc.isNoRecv(), pc.isPortDown());
        }
        return npc;
    }
    
    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig translatePortConfig(
            PortConfigV10 pc) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig npc = null;
        if(pc != null) {
                npc = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig(pc.isNoFwd(),
                        pc.isNoPacketIn(), pc.isNoRecv(), pc.isPortDown());
        }
        return npc;
    }
    
    public static NodeConnectorUpdated translatePort(Short version,BigInteger datapathId,Long portNo, PortGrouping port) {
        NodeConnectorUpdatedBuilder builder = InventoryDataServiceUtil
                .nodeConnectorUpdatedBuilderFromDatapathIdPortNo(datapathId,port.getPortNo());
        FlowCapableNodeConnectorUpdatedBuilder fcncub = new FlowCapableNodeConnectorUpdatedBuilder();
        if(version == OFConstants.OFP_VERSION_1_3) {
            fcncub.setAdvertisedFeatures(PortTranslatorUtil.translatePortFeatures(port.getAdvertisedFeatures()));
            fcncub.setConfiguration(PortTranslatorUtil.translatePortConfig(port.getConfig()));
            fcncub.setCurrentFeature(PortTranslatorUtil.translatePortFeatures(port.getCurrentFeatures()));
            fcncub.setPeerFeatures(PortTranslatorUtil.translatePortFeatures(port.getPeerFeatures()));
            fcncub.setState(PortTranslatorUtil.translatePortState(port.getState()));
            fcncub.setSupported(PortTranslatorUtil.translatePortFeatures(port.getSupportedFeatures()));
        } else if (version == OFConstants.OFP_VERSION_1_0) {
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
        fcncub.setPortNumber(port.getPortNo());
        builder.addAugmentation(FlowCapableNodeConnectorUpdated.class, fcncub.build());
        return builder.build();
    }
}
