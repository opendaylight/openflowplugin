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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
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

public class OpenflowpluginGroupTestServiceProvider implements AutoCloseable,
        SalGroupService {

    private static final Logger LOG = LoggerFactory
            .getLogger(OpenflowpluginGroupTestServiceProvider.class);
    private ObjectRegistration<SalGroupService> groupRegistration;
    private NotificationPublishService notificationService;

    /**
     * Get group registration.
     *
     * @return {@link #groupRegistration}
     */
    public ObjectRegistration<SalGroupService> getGroupRegistration() {
        return groupRegistration;
    }

    /**
     * Set {@link #groupRegistration}.
     */
    public void setGroupRegistration(final ObjectRegistration<SalGroupService> groupRegistration) {
        this.groupRegistration = groupRegistration;
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
        OpenflowpluginGroupTestServiceProvider.LOG
                .info("SalGroupServiceProvider Started.");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
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

    public ObjectRegistration<OpenflowpluginGroupTestServiceProvider> register(final RpcProviderService rpcRegistry) {
        setGroupRegistration(rpcRegistry.registerRpcImplementation(SalGroupService.class, this, ImmutableSet.of(
            InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID))))));

        return new AbstractObjectRegistration<OpenflowpluginGroupTestServiceProvider>(this) {
            @Override
            protected void removeRegistration() {
                groupRegistration.close();
            }
        };
    }
}
