/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTableFeaturesTestServiceProvider implements AutoCloseable, UpdateTable {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginTableFeaturesTestServiceProvider.class);

    private Registration tableRegistration = null;
    private NotificationPublishService notificationService;

    /**
     * Get table registration.
     *
     * @return {@link #tableRegistration}
     */
    public Registration getTableRegistration() {
        return tableRegistration;
    }

    /**
     * Set {@link #tableRegistration}.
     */
    public void setTableRegistration(final Registration tableRegistration) {
        this.tableRegistration = tableRegistration;
    }

    /**
     * Get notification service.
     *
     * @return {@link #notificationService}
     */
    public NotificationPublishService getNotificationService() {
        return notificationService;
    }

    /**
     * Set {@link #notificationService}.
     */
    public void setNotificationService(final NotificationPublishService notificationService) {
        this.notificationService = notificationService;
    }

    public void start() {
        LOG.info("SalTableServiceProvider Started.");
    }

    @Override
    public void close() {
        LOG.info("SalTableServiceProvider stopped.");
        tableRegistration.close();
    }

    @Override
    public ListenableFuture<RpcResult<UpdateTableOutput>> invoke(final UpdateTableInput input) {
        LOG.info("updateTable - {}", input);
        return RpcResultBuilder.success(new UpdateTableOutputBuilder().build()).buildFuture();
    }

    public ObjectRegistration<OpenflowpluginTableFeaturesTestServiceProvider> register(
            final RpcProviderService rpcRegistry) {
        setTableRegistration(rpcRegistry.registerRpcImplementation(this, ImmutableSet.of(
            InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID))))));

        return new AbstractObjectRegistration<>(this) {
            @Override
            protected void removeRegistration() {
                tableRegistration.close();
            }
        };
    }
}
