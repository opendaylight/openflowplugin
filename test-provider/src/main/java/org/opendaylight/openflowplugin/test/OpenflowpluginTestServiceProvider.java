/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;

import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTestServiceProvider implements AutoCloseable,
        SalFlowService {

    private static final Logger LOG = LoggerFactory
            .getLogger(OpenflowpluginTestServiceProvider.class);

    private DataBroker dataService;
    private RoutedRpcRegistration<SalFlowService> flowRegistration;
    private NotificationProviderService notificationProviderService;

    /**
     * get data service
     *
     * @return {@link #dataService}
     */
    public DataBroker getDataService() {
        return dataService;
    }

    /**
     * set {@link #dataService}
     *
     * @param dataService
     */
    public void setDataService(final DataBroker dataService) {
        this.dataService = dataService;
    }

    /**
     * get flow registration
     *
     * @return {@link #flowRegistration}
     */
    public RoutedRpcRegistration<SalFlowService> getFlowRegistration() {
        return flowRegistration;
    }

    /**
     * set {@link #flowRegistration}
     *
     * @param flowRegistration
     */
    public void setFlowRegistration(
            final RoutedRpcRegistration<SalFlowService> flowRegistration) {
        this.flowRegistration = flowRegistration;
    }

    /**
     * get notification service
     *
     * @return {@link #notificationProviderService}
     */
    public NotificationProviderService getNotificationService() {
        return notificationProviderService;
    }

    /**
     * set {@link #notificationProviderService}
     *
     * @param notificationProviderService
     */
    public void setNotificationService(
            final NotificationProviderService notificationProviderService) {
        this.notificationProviderService = notificationProviderService;
    }

    public void start() {
        OpenflowpluginTestServiceProvider.LOG
                .info("SalFlowServiceProvider Started.");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        OpenflowpluginTestServiceProvider.LOG
                .info("SalFlowServiceProvide stopped.");
        flowRegistration.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.
     * SalFlowService
     * #addFlow(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.
     * service.rev130819.AddFlowInput)
     */
    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(AddFlowInput input) {
        String plus = ("addFlow - " + input);
        OpenflowpluginTestServiceProvider.LOG.info(plus);
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.
     * SalFlowService
     * #removeFlow(org.opendaylight.yang.gen.v1.urn.opendaylight.flow
     * .service.rev130819.RemoveFlowInput)
     */
    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(RemoveFlowInput input) {
        String plus = ("removeFlow - " + input);
        OpenflowpluginTestServiceProvider.LOG.info(plus);
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.
     * SalFlowService
     * #updateFlow(org.opendaylight.yang.gen.v1.urn.opendaylight.flow
     * .service.rev130819.UpdateFlowInput)
     */
    @Override
    public Future<RpcResult<UpdateFlowOutput>> updateFlow(UpdateFlowInput input) {
        String plus = ("updateFlow - " + input);
        OpenflowpluginTestServiceProvider.LOG.info(plus);
        return null;
    }

    /**
     * @param ctx
     * @return {@link ObjectRegistration}
     */
    public ObjectRegistration<OpenflowpluginTestServiceProvider> register(
            final ProviderContext ctx) {
        RoutedRpcRegistration<SalFlowService> addRoutedRpcImplementation = ctx
                .<SalFlowService> addRoutedRpcImplementation(
                        SalFlowService.class, this);

        setFlowRegistration(addRoutedRpcImplementation);

        InstanceIdentifierBuilder<Nodes> builderII = InstanceIdentifier
                .<Nodes> builder(Nodes.class);

        NodeId nodeId = new NodeId(OpenflowpluginTestActivator.NODE_ID);
        NodeKey nodeKey = new NodeKey(nodeId);

        InstanceIdentifierBuilder<Node> nodeIdentifier = builderII
                .<Node, NodeKey> child(Node.class, nodeKey);

        InstanceIdentifier<Node> instance = nodeIdentifier.build();

        flowRegistration.registerPath(NodeContext.class, instance);

        RoutedRpcRegistration<SalFlowService> flowRegistration2 = getFlowRegistration();

        return new AbstractObjectRegistration<OpenflowpluginTestServiceProvider>(this) {
            @Override
            protected void removeRegistration() {
                flowRegistration2.close();
            }
        };
    }
}
