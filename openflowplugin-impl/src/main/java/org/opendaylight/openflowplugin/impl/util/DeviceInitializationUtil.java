/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.impl.device.SwitchFeaturesUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeviceInitializationUtil {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceInitializationUtil.class);

    private DeviceInitializationUtil() {
        // Hiding implicit constructor
    }

    /**
     * Merge empty nodes to operational DS to predict any problems with missing parent for node.
     *
     * @param dataBroker the data broker
     */
    public static void makeEmptyNodes(final DataBroker dataBroker) {
        final var tx = dataBroker.newWriteOnlyTransaction();
        tx.merge(LogicalDatastoreType.OPERATIONAL, DataObjectIdentifier.builder(Nodes.class).build(),
            new NodesBuilder().build());
        try {
            tx.commit().get();
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Creation of node failed.", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Create specified number of empty tables on device.
     * FIXME: remove after ovs table features fix
     *
     * @param txFacade   transaction facade
     * @param deviceInfo device info
     * @param nrOfTables number of tables
     */
    public static void makeEmptyTables(final TxFacade txFacade, final DeviceInfo deviceInfo, final short nrOfTables) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("About to create {} empty tables for node {}.", nrOfTables, deviceInfo);
        }

        for (int i = 0; i < nrOfTables; i++) {
            final Uint8 tableId = Uint8.valueOf(i);
            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL,
                    deviceInfo.getNodeInstanceIdentifier().toBuilder()
                            .augmentation(FlowCapableNode.class)
                            .child(Table.class, new TableKey(tableId))
                            .build(),
                    new TableBuilder()
                            .setId(tableId)
                            .addAugmentation(new FlowTableStatisticsDataBuilder().build())
                            .build());
        }
    }

    /**
     * Retrieve ip address from connection.
     *
     * @param connectionContext  connection context
     * @param instanceIdentifier instance identifier
     * @return ip address
     */
    public static IpAddress getIpAddress(final ConnectionContext connectionContext,
                                         final WithKey<Node, NodeKey> instanceIdentifier) {
        final String node = instanceIdentifier.key().getId().getValue();
        final InetSocketAddress address = getRemoteAddress(connectionContext, instanceIdentifier);
        if (address == null) {
            return null;
        }
        final IpAddress ipAddress = IetfInetUtil.ipAddressFor(address.getAddress());
        LOG.info("IP address of the node {} is: {}", node, ipAddress);
        return ipAddress;
    }

    /**
     * Retrieve port number from connection.
     *
     * @param connectionContext  connection context
     * @param instanceIdentifier instance identifier
     * @return port number
     */
    public static PortNumber getPortNumber(final ConnectionContext connectionContext,
                                           final WithKey<Node, NodeKey> instanceIdentifier) {
        final String node = instanceIdentifier.key().getId().getValue();
        final InetSocketAddress address = getRemoteAddress(connectionContext, instanceIdentifier);
        if (address == null) {
            return null;
        }
        final int port = address.getPort();
        LOG.info("Port number of the node {} is: {}", node, port);
        return new PortNumber(Uint16.valueOf(port));
    }

    /**
     * Retrieve switch features from connection.
     *
     * @param connectionContext connection context
     * @return switch features
     */
    public static SwitchFeatures getSwitchFeatures(final ConnectionContext connectionContext) {
        return SwitchFeaturesUtil.buildSwitchFeatures(new GetFeaturesOutputBuilder(connectionContext.getFeatures())
            .build());
    }

    private static @Nullable InetSocketAddress getRemoteAddress(final ConnectionContext connectionContext,
                                                                final WithKey<Node, NodeKey> instanceIdentifier) {
        final ConnectionAdapter adapter = connectionContext.getConnectionAdapter();
        final InetSocketAddress remoteAddress = adapter == null ? null : adapter.getRemoteAddress();
        if (remoteAddress == null) {
            LOG.warn("Remote address of the node {} cannot be obtained. No connection with switch.",
                instanceIdentifier.key().getId());
        }
        return remoteAddress;
    }
}
