/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import java.net.InetSocketAddress;
import java.util.Optional;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.openflow.md.core.sal.SwitchFeaturesUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceInitializationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceInitializationUtil.class);

    private DeviceInitializationUtil() {
        // Hiding implicit constructor
    }

    /**
     * Create specified number of empty tables on device
     * FIXME: remove after ovs table features fix
     * @param txFacade transaction facade
     * @param deviceInfo device info
     * @param nrOfTables number of tables
     */
    public static void makeEmptyTables(final TxFacade txFacade, final DeviceInfo deviceInfo, final Short nrOfTables) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("About to create {} empty tables for node {}.", nrOfTables, deviceInfo.getLOGValue());
        }

        for (int i = 0; i < nrOfTables; i++) {
            try {
                txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL,
                        deviceInfo
                                .getNodeInstanceIdentifier()
                                .augmentation(FlowCapableNode.class)
                                .child(Table.class, new TableKey((short) i)),
                        new TableBuilder()
                                .setId((short) i)
                                .addAugmentation(
                                        FlowTableStatisticsData.class,
                                        new FlowTableStatisticsDataBuilder().build())
                                .build());
            } catch (final Exception e) {
                LOG.debug("makeEmptyTables: Failed to write node {} to DS ", deviceInfo.getLOGValue(), e);
            }
        }
    }

    /**
     * Retrieve ip address from connection
     * @param connectionContext connection context
     * @param instanceIdentifier instance identifier
     * @return ip address
     */
    public static IpAddress getIpAddress(final ConnectionContext connectionContext,
                                         final InstanceIdentifier<Node> instanceIdentifier) {
        final String node = PathUtil.extractNodeId(instanceIdentifier).getValue();

        return getRemoteAddress(connectionContext, instanceIdentifier)
                .map(inetSocketAddress -> {
                    final IpAddress ipAddress = IetfInetUtil.INSTANCE.ipAddressFor(inetSocketAddress.getAddress());
                    LOG.info("IP address of the node {} is: {}", node, ipAddress);
                    return ipAddress;
                })
                .orElse(null);
    }

    /**
     * Retrieve port number from connection
     * @param connectionContext connection context
     * @param instanceIdentifier instance identifier
     * @return port number
     */
    public static PortNumber getPortNumber(final ConnectionContext connectionContext,
                                           final InstanceIdentifier<Node> instanceIdentifier) {
        final String node = PathUtil.extractNodeId(instanceIdentifier).getValue();

        return getRemoteAddress(connectionContext, instanceIdentifier)
                .map(inetSocketAddress -> {
                    final int port = inetSocketAddress.getPort();
                    LOG.info("Port number of the node {} is: {}", node, port);
                    return new PortNumber(port);
                })
                .orElse(null);

    }

    /**
     * Retrieve switch features from connection
     * @param connectionContext connection context
     * @return switch features
     */
    public static SwitchFeatures getSwitchFeatures(final ConnectionContext connectionContext) {
        return SwitchFeaturesUtil
                .getInstance()
                .buildSwitchFeatures(new GetFeaturesOutputBuilder(connectionContext
                        .getFeatures())
                        .build());
    }

    private static Optional<InetSocketAddress> getRemoteAddress(final ConnectionContext connectionContext,
                                                                final InstanceIdentifier<Node> instanceIdentifier) {
        final Optional<InetSocketAddress> inetSocketAddress = Optional
                .ofNullable(connectionContext.getConnectionAdapter())
                .flatMap(connectionAdapter -> Optional.ofNullable(connectionAdapter.getRemoteAddress()));

        if (!inetSocketAddress.isPresent()) {
            LOG.warn("Remote address of the node {} cannot be obtained. No connection with switch.", PathUtil
                    .extractNodeId(instanceIdentifier));
        }

        return inetSocketAddress;
    }

}
