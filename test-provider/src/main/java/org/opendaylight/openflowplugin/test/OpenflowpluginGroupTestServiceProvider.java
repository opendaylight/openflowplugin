/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
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

public class OpenflowpluginGroupTestServiceProvider implements AutoCloseable,
        SalGroupService {

    private static final Logger LOG = LoggerFactory
            .getLogger(OpenflowpluginGroupTestServiceProvider.class);
    private RoutedRpcRegistration<SalGroupService> groupRegistration;
    private NotificationProviderService notificationService;

    /**
     * Get group registration.
     *
     * @return {@link #groupRegistration}
     */
    public RoutedRpcRegistration<SalGroupService> getGroupRegistration() {
        return groupRegistration;
    }

    /**
     * Set {@link #groupRegistration}.
     */
    public void setGroupRegistration(
            final RoutedRpcRegistration<SalGroupService> groupRegistration) {
        this.groupRegistration = groupRegistration;
    }

    /**
     * Get notification service.
     *
     * @return {@link #notificationService}
     */
    public NotificationProviderService getNotificationService() {
        return notificationService;
    }

    /**
     * Set {@link #notificationService}.
     */
    public void setNotificationService(
            final NotificationProviderService notificationService) {
        this.notificationService = notificationService;
    }

    public void start() {
        OpenflowpluginGroupTestServiceProvider.LOG
                .info("SalGroupServiceProvider Started.");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        OpenflowpluginGroupTestServiceProvider.LOG
                .info("SalGroupServiceProvide stopped.");
        groupRegistration.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918
     * .SalGroupService
     * #addGroup(org.opendaylight.yang.gen.v1.urn.opendaylight.group
     * .service.rev130918.AddGroupInput)
     */
    @Override
    public ListenableFuture<RpcResult<AddGroupOutput>> addGroup(AddGroupInput input) {
        OpenflowpluginGroupTestServiceProvider.LOG.info("addGroup - {}", input);
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918
     * .SalGroupService
     * #removeGroup(org.opendaylight.yang.gen.v1.urn.opendaylight
     * .group.service.rev130918.RemoveGroupInput)
     */
    @Override
    public ListenableFuture<RpcResult<RemoveGroupOutput>> removeGroup(
            RemoveGroupInput input) {
        OpenflowpluginGroupTestServiceProvider.LOG.info("removeGroup - {}", input);
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918
     * .SalGroupService
     * #updateGroup(org.opendaylight.yang.gen.v1.urn.opendaylight
     * .group.service.rev130918.UpdateGroupInput)
     */
    @Override
    public ListenableFuture<RpcResult<UpdateGroupOutput>> updateGroup(
            UpdateGroupInput input) {
        OpenflowpluginGroupTestServiceProvider.LOG.info("updateGroup - {}", input);
        return null;
    }

    public ObjectRegistration<OpenflowpluginGroupTestServiceProvider> register(
            final RpcProviderRegistry rpcRegistry) {
        RoutedRpcRegistration<SalGroupService> addRoutedRpcImplementation = rpcRegistry.addRoutedRpcImplementation(
                        SalGroupService.class, this);
        setGroupRegistration(addRoutedRpcImplementation);

        InstanceIdentifierBuilder<Nodes> builder1 = InstanceIdentifier
                .<Nodes>builder(Nodes.class);

        NodeId nodeId = new NodeId(OpenflowpluginTestActivator.NODE_ID);
        NodeKey nodeKey = new NodeKey(nodeId);

        InstanceIdentifierBuilder<Node> nodeIndentifier = builder1
                .<Node, NodeKey>child(Node.class, nodeKey);
        InstanceIdentifier<Node> instance = nodeIndentifier.build();
        groupRegistration.registerPath(NodeContext.class, instance);
        RoutedRpcRegistration<SalGroupService> groupRegistration1 = this
                .getGroupRegistration();
        return new AbstractObjectRegistration<OpenflowpluginGroupTestServiceProvider>(this) {
            @Override
            protected void removeRegistration() {
                groupRegistration1.close();
            }
        };
    }
}
