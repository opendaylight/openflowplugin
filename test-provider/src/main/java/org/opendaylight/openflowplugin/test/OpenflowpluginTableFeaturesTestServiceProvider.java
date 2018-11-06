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
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTableFeaturesTestServiceProvider implements
        AutoCloseable, SalTableService {

    private static final Logger LOG = LoggerFactory
            .getLogger(OpenflowpluginTableFeaturesTestServiceProvider.class);
    private ObjectRegistration<SalTableService> tableRegistration;
    private NotificationPublishService notificationService;

    /**
     * Get table registration.
     *
     * @return {@link #tableRegistration}
     */
    public ObjectRegistration<SalTableService> getTableRegistration() {
        return this.tableRegistration;
    }

    /**
     * Set {@link #tableRegistration}.
     */
    public void setTableRegistration(final ObjectRegistration<SalTableService> tableRegistration) {
        this.tableRegistration = tableRegistration;
    }

    /**
     * Get notification service.
     *
     * @return {@link #notificationService}
     */
    public NotificationPublishService getNotificationService() {
        return this.notificationService;
    }

    /**
     * Set {@link #notificationService}.
     */
    public void setNotificationService(final NotificationPublishService notificationService) {
        this.notificationService = notificationService;
    }

    public void start() {
        OpenflowpluginTableFeaturesTestServiceProvider.LOG
                .info("SalTableServiceProvider Started.");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        OpenflowpluginTableFeaturesTestServiceProvider.LOG
                .info("SalTableServiceProvider stopped.");
        tableRegistration.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026
     * .SalTableService
     * #updateTable(org.opendaylight.yang.gen.v1.urn.opendaylight
     * .table.service.rev131026.UpdateTableInput)
     */
    @Override
    public ListenableFuture<RpcResult<UpdateTableOutput>> updateTable(
            UpdateTableInput input) {
        OpenflowpluginTableFeaturesTestServiceProvider.LOG.info("updateTable - {}", input);
        return null;
    }

    public ObjectRegistration<OpenflowpluginTableFeaturesTestServiceProvider> register(
            final RpcProviderService rpcRegistry) {
        setTableRegistration(rpcRegistry.registerRpcImplementation(SalTableService.class, this, ImmutableSet.of(
            InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID))))));

        return new AbstractObjectRegistration<OpenflowpluginTableFeaturesTestServiceProvider>(this) {
            @Override
            protected void removeRegistration() {
                tableRegistration.close();
            }
        };
    }
}
