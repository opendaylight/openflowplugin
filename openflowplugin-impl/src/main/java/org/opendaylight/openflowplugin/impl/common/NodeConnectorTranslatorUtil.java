/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.common;

import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.State;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.common
 *
 * Translator helper for Translating OF java models to MD-SAL inventory models.
 * Translator focus for {@link NodeConnector} object and relevant OF augmentation
 * {@link FlowCapableNodeConnector}.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Mar 31, 2015
 */
public final class NodeConnectorTranslatorUtil {

    private NodeConnectorTranslatorUtil () {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Method translates {@link PhyPort} object directly to {@link NodeConnector} which is augmented
     * by {@link FlowCapableNodeConnector} and contains all relevant content translated by actual OF version.
     *
     * @param featuresReply
     * @return
     */
    public static List<NodeConnector> translateNodeConnectorFromFeaturesReply(@CheckForNull final FeaturesReply featuresReply) {
        Preconditions.checkArgument(featuresReply != null);
        Preconditions.checkArgument(featuresReply.getPhyPort() != null);
        final Short version = featuresReply.getVersion();
        final BigInteger dataPathId = featuresReply.getDatapathId();
        final List<NodeConnector> resultList = new ArrayList<>(featuresReply.getPhyPort().size());
        for (final PhyPort port : featuresReply.getPhyPort()) {
            final NodeConnectorBuilder ncBuilder = new NodeConnectorBuilder();
            ncBuilder.setId(makeNodeConnectorId(dataPathId, port.getName(), port.getPortNo()));
            ncBuilder.addAugmentation(FlowCapableNodeConnector.class, translateFlowCapableNodeFromPhyPort(port, version));
            resultList.add(ncBuilder.build());
        }
        return resultList;
    }

    /**
     * Method translates {@link PhyPort} object directly to {@link FlowCapableNodeConnector} which is augmented
     * by {@link NodeConnector} and contains all relevant content translated by actual OF version.
     *
     * @param port
     * @param version
     * @return
     */
    public static FlowCapableNodeConnector translateFlowCapableNodeFromPhyPort(@CheckForNull final PhyPort port, final short version) {
        Preconditions.checkArgument(port != null);
        final FlowCapableNodeConnectorBuilder fcncBuilder = new FlowCapableNodeConnectorBuilder();
        fcncBuilder.setHardwareAddress(port.getHwAddr());
        fcncBuilder.setCurrentSpeed(port.getCurrSpeed());
        fcncBuilder.setMaximumSpeed(port.getMaxSpeed());
        fcncBuilder.setName(port.getName());
        fcncBuilder.setPortNumber(new PortNumberUni(port.getPortNo()));
        if (OFConstants.OFP_VERSION_1_3 == version) {
            fcncBuilder.setAdvertisedFeatures(translatePortFeatures(port.getAdvertisedFeatures()));
            fcncBuilder.setConfiguration(translatePortConfig(port.getConfig()));
            fcncBuilder.setCurrentFeature(translatePortFeatures(port.getCurrentFeatures()));
            fcncBuilder.setPeerFeatures(translatePortFeatures(port.getPeerFeatures()));
            fcncBuilder.setSupported(translatePortFeatures(port.getSupportedFeatures()));
            fcncBuilder.setState(translatePortState(port.getState()));
        } else if (OFConstants.OFP_VERSION_1_0 == version) {
            fcncBuilder.setAdvertisedFeatures(translatePortFeatures(port.getAdvertisedFeaturesV10()));
            fcncBuilder.setConfiguration(translatePortConfig(port.getConfigV10()));
            fcncBuilder.setCurrentFeature(translatePortFeatures(port.getCurrentFeaturesV10()));
            fcncBuilder.setPeerFeatures(translatePortFeatures(port.getPeerFeaturesV10()));
            fcncBuilder.setSupported(translatePortFeatures(port.getSupportedFeaturesV10()));
            fcncBuilder.setState(translatePortState(port.getStateV10()));
        } else {
            throw new IllegalArgumentException("Unknown OF version " + version);
        }
        return fcncBuilder.build();
    }

    private static PortConfig translatePortConfig(@CheckForNull final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig pc) {
        Preconditions.checkArgument(pc != null);
        return new PortConfig(pc.isNoFwd(), pc.isNoPacketIn(), pc.isNoRecv(), pc.isPortDown());
    }

    private static PortConfig translatePortConfig(@CheckForNull final PortConfigV10 pc) {
        Preconditions.checkArgument(pc != null);
        return new PortConfig(pc.isNoFwd(), pc.isNoPacketIn(), pc.isNoRecv(), pc.isPortDown());
    }

    private static PortFeatures translatePortFeatures(@CheckForNull final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures pf) {
        Preconditions.checkArgument(pf != null);
        return new PortFeatures(pf.isAutoneg(), pf.isCopper(), pf.isFiber(), pf.is_40gbFd(), pf.is_100gbFd(), pf.is_100mbFd(), pf.is_100mbHd(),
                pf.is_1gbFd(), pf.is_1gbHd(), pf.is_1tbFd(), pf.isOther(), pf.isPause(), pf.isPauseAsym(), pf.is_10gbFd(), pf.is_10mbFd(), pf.is_10mbHd());
    }

    private static PortFeatures translatePortFeatures(@CheckForNull final PortFeaturesV10 pf) {
        Preconditions.checkArgument(pf != null);
        return new PortFeatures(pf.isAutoneg(), pf.isCopper(), pf.isFiber(), Boolean.FALSE, Boolean.FALSE, pf.is_100mbFd(), pf.is_100mbHd(),
                pf.is_1gbFd(), pf.is_1gbHd(), Boolean.FALSE, Boolean.FALSE, pf.isPause(), pf.isPauseAsym(), pf.is_10gbFd(), pf.is_10mbFd(), pf.is_10mbHd());
    }

    private static State translatePortState(@CheckForNull final PortState state) {
        Preconditions.checkArgument(state != null);
        return new StateBuilder().setBlocked(state.isBlocked()).setLinkDown(state.isLinkDown()).setLive(state.isLive()).build();
    }

    private static State translatePortState(@CheckForNull final PortStateV10 state) {
        Preconditions.checkArgument(state != null);
        return new StateBuilder().setBlocked(state.isBlocked()).setLinkDown(state.isLinkDown()).setLive(state.isLive()).build();
    }

    /**
     * Method makes NodeConnectorId with prefix  "openflow:" from dataPathId and logical name or port number
     *
     * @param dataPathId
     * @param logicalName
     * @param portNo
     * @return
     */
    public static NodeConnectorId makeNodeConnectorId(@CheckForNull final BigInteger dataPathId,
            @Nullable final String logicalName, final long portNo) {
        Preconditions.checkArgument(dataPathId != null);
        return new NodeConnectorId(OFConstants.OF_URI_PREFIX + dataPathId + ":" + (logicalName == null ? portNo : logicalName));
    }
}
