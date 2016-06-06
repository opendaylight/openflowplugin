/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;

import java.util.concurrent.Future;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTableFeaturesTestServiceProvider implements
        AutoCloseable, SalTableService {

    private static final Logger LOG = LoggerFactory
            .getLogger(OpenflowpluginTableFeaturesTestServiceProvider.class);
    private RoutedRpcRegistration<SalTableService> tableRegistration;
    private NotificationProviderService notificationService;

    /**
     * get table registration
     *
     * @return {@link #tableRegistration}
     */
    public RoutedRpcRegistration<SalTableService> getTableRegistration() {
        return this.tableRegistration;
    }

    /**
     * set {@link #tableRegistration}
     *
     * @param tableRegistration
     */
    public void setTableRegistration(
            final RoutedRpcRegistration<SalTableService> tableRegistration) {
        this.tableRegistration = tableRegistration;
    }

    /**
     * get notification service
     *
     * @return {@link #notificationService}
     */
    public NotificationProviderService getNotificationService() {
        return this.notificationService;
    }

    /**
     * set {@link #notificationService}
     *
     * @param notificationService
     */
    public void setNotificationService(
            final NotificationProviderService notificationService) {
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
    public void close() throws Exception {
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
    public Future<RpcResult<UpdateTableOutput>> updateTable(
            UpdateTableInput input) {
        String plus = ("updateTable - " + input);
        OpenflowpluginTableFeaturesTestServiceProvider.LOG.info(plus);
        return null;
    }

    /**
     * @param ctx
     * @return {@link ObjectRegistration}
     */
    public ObjectRegistration<OpenflowpluginTableFeaturesTestServiceProvider> register(
            final ProviderContext ctx) {
        RoutedRpcRegistration<SalTableService> addRoutedRpcImplementation = ctx
                .<SalTableService> addRoutedRpcImplementation(
                        SalTableService.class, this);

        setTableRegistration(addRoutedRpcImplementation);

        InstanceIdentifierBuilder<Nodes> builder1 = InstanceIdentifier
                .<Nodes> builder(Nodes.class);

        NodeId nodeId = new NodeId(OpenflowpluginTestActivator.NODE_ID);
        NodeKey nodeKey = new NodeKey(nodeId);

        InstanceIdentifierBuilder<Node> nodeIndentifier = builder1
                .<Node, NodeKey> child(Node.class, nodeKey);

        InstanceIdentifier<Node> instance = nodeIndentifier.build();

        tableRegistration.registerPath(NodeContext.class, instance);

        RoutedRpcRegistration<SalTableService> tableRegistration1 = this
                .getTableRegistration();

        return new AbstractObjectRegistration<OpenflowpluginTableFeaturesTestServiceProvider>(this) {
            @Override
            protected void removeRegistration() {
                tableRegistration1.close();
            }
        };
    }

}
