/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.initializer.impl;

import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeException;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.initializer.SwitchInitializerApplication;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.State;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwitchInitializerApplicationImpl implements SwitchInitializerApplication {
    private static final Logger LOG = LoggerFactory.getLogger(SwitchInitializerApplicationImpl.class);

    private final MastershipChangeServiceManager mastershipChangeServiceManager;

    public SwitchInitializerApplicationImpl(MastershipChangeServiceManager mastershipChangeServiceManager) {
        this.mastershipChangeServiceManager = Preconditions
                .checkNotNull(mastershipChangeServiceManager, "MastershipChangeServiceManager can not be null!");
    }

    public void start() throws MastershipChangeException {
        mastershipChangeServiceManager.registerSwitchInitializer(this);
        LOG.info("Switch initializer application has started successfully.");
    }

    @Override
    public void close() {
    }

    @Override
    public Future<Void> onDevicePrepared(@Nonnull DeviceInfo deviceInfo) {
        if (!localPortAdded && lastNodeConnector != null) {
            LOG.info("LOCAL node connector in {} is missing. Creating it.", deviceInfo);

            NodeConnectorBuilder nodeConnectorBuilder = new NodeConnectorBuilder(lastNodeConnector);
            final BigInteger dataPathId = dContext.getPrimaryConnectionContext().getFeatures().getDatapathId();
            final NodeConnectorId nodeConnectorId = NodeStaticReplyTranslatorUtil.nodeConnectorId(dataPathId.toString(),
                    valueLocalPort, ofVersion);
            nodeConnectorBuilder.setId(nodeConnectorId);
            nodeConnectorBuilder.setKey(new NodeConnectorKey(nodeConnectorId));

            // changing the FlowCapableNodeConnector obj
            State state = new StateBuilder().setLinkDown(true).setBlocked(false).setLive(false).build();
            FlowCapableNodeConnector lastFlowCapableNodeConnector = lastNodeConnector
                    .getAugmentation(FlowCapableNodeConnector.class);
            if (lastFlowCapableNodeConnector != null) {
                final FlowCapableNodeConnector fcNodeConnector = new FlowCapableNodeConnectorBuilder(
                        lastFlowCapableNodeConnector).setName("LOCAL").setState(state)
                        .setPortNumber(new PortNumberUni(valueLocalPort))
                        .setHardwareAddress(new MacAddress("00:01:02:03:04:05"))
                        .setConfiguration(new PortConfig(false, false, false, true)).build();
                nodeConnectorBuilder.addAugmentation(FlowCapableNodeConnector.class, fcNodeConnector);
            }
            NodeConnector nodeConnector = nodeConnectorBuilder.build();
            final InstanceIdentifier<NodeConnector> iiNodeConnector = nodeII.child(NodeConnector.class,
                    nodeConnector.getKey());
            dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, iiNodeConnector, nodeConnector);
        }
        LOG.info("Device: {} was connected and new LOCAL port was added.");
        return CompletableFuture.completedFuture(null);
    }
}