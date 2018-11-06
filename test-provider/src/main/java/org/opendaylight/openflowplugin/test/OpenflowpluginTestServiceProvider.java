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
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTestServiceProvider implements AutoCloseable,
        SalFlowService {

    private static final Logger LOG = LoggerFactory
            .getLogger(OpenflowpluginTestServiceProvider.class);

    private final DataBroker dataService;
    private ObjectRegistration<SalFlowService> flowRegistration;
    private final NotificationPublishService notificationProviderService;

    public OpenflowpluginTestServiceProvider(DataBroker dataService,
            NotificationPublishService notificationProviderService) {
        this.dataService = dataService;
        this.notificationProviderService = notificationProviderService;
    }

    /**
     * Get data service.
     *
     * @return {@link #dataService}
     */
    public DataBroker getDataService() {
        return dataService;
    }

    /**
     * Get flow registration.
     *
     * @return {@link #flowRegistration}
     */
    public ObjectRegistration<SalFlowService> getFlowRegistration() {
        return flowRegistration;
    }

    /**
     * Set {@link #flowRegistration}.
     */
    public void setFlowRegistration(final ObjectRegistration<SalFlowService> flowRegistration) {
        this.flowRegistration = flowRegistration;
    }

    /**
     * Get notification service.
     *
     * @return {@link #notificationProviderService}
     */
    public NotificationPublishService getNotificationService() {
        return notificationProviderService;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
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
    public ListenableFuture<RpcResult<AddFlowOutput>> addFlow(AddFlowInput input) {
        OpenflowpluginTestServiceProvider.LOG.info("addFlow - {}", input);
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
    public ListenableFuture<RpcResult<RemoveFlowOutput>> removeFlow(RemoveFlowInput input) {
        OpenflowpluginTestServiceProvider.LOG.info("removeFlow - {}", input);
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
    public ListenableFuture<RpcResult<UpdateFlowOutput>> updateFlow(UpdateFlowInput input) {
        OpenflowpluginTestServiceProvider.LOG.info("updateFlow - {}", input);
        return null;
    }

    public ObjectRegistration<OpenflowpluginTestServiceProvider> register(RpcProviderService rpcRegistry) {
        setFlowRegistration(rpcRegistry.registerRpcImplementation(SalFlowService.class, this, ImmutableSet.of(
            InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID))))));

        return new AbstractObjectRegistration<OpenflowpluginTestServiceProvider>(this) {
            @Override
            protected void removeRegistration() {
                flowRegistration.close();
            }
        };
    }
}
