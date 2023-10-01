/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTestRpcProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginTestRpcProvider.class);

    private final DataBroker dataService;
    private Registration flowRegistration = null;
    private final NotificationPublishService notificationProviderService;

    public OpenflowpluginTestRpcProvider(final DataBroker dataService,
            final NotificationPublishService notificationProviderService) {
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
    public Registration getFlowRegistration() {
        return flowRegistration;
    }

    /**
     * Set {@link #flowRegistration}.
     */
    public void setFlowRegistration(final Registration flowRegistration) {
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
        OpenflowpluginTestRpcProvider.LOG
                .info("SalFlowRpcProvide stopped.");
        flowRegistration.close();
    }

    private ListenableFuture<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {
        OpenflowpluginTestRpcProvider.LOG.info("addFlow - {}", input);
        return null;
    }

    private ListenableFuture<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {
        OpenflowpluginTestRpcProvider.LOG.info("removeFlow - {}", input);
        return null;
    }

    private ListenableFuture<RpcResult<UpdateFlowOutput>> updateFlow(final UpdateFlowInput input) {
        OpenflowpluginTestRpcProvider.LOG.info("updateFlow - {}", input);
        return null;
    }

    public Registration register(final RpcProviderService rpcRegistry) {
        setFlowRegistration(rpcRegistry.registerRpcImplementations(ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(AddFlow.class, this::addFlow)
            .put(RemoveFlow.class, this::removeFlow)
            .put(UpdateFlow.class, this::updateFlow)
            .build(), ImmutableSet.of(
            InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID))))));

        return new AbstractObjectRegistration<>(this) {
            @Override
            protected void removeRegistration() {
                flowRegistration.close();
            }
        };
    }
}
