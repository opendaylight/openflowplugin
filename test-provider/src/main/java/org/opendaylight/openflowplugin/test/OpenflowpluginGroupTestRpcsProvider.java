/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
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

public class OpenflowpluginGroupTestRpcsProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory
            .getLogger(OpenflowpluginGroupTestRpcsProvider.class);
    private Registration groupRegistration;
    private NotificationPublishService notificationService;

    /**
     * Get group registration.
     *
     * @return {@link #groupRegistration}
     */
    public Registration getGroupRegistration() {
        return groupRegistration;
    }

    /**
     * Set {@link #groupRegistration}.
     */
    public void setGroupRegistration(final Registration groupRegistration) {
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
        OpenflowpluginGroupTestRpcsProvider.LOG
                .info("SalGroupRpcsProvider Started.");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        OpenflowpluginGroupTestRpcsProvider.LOG
                .info("SalGroupRpcsProvide stopped.");
        groupRegistration.close();
    }

    private ListenableFuture<RpcResult<AddGroupOutput>> addGroup(final AddGroupInput input) {
        OpenflowpluginGroupTestRpcsProvider.LOG.info("addGroup - {}", input);
        return null;
    }

    private ListenableFuture<RpcResult<RemoveGroupOutput>> removeGroup(
            final RemoveGroupInput input) {
        OpenflowpluginGroupTestRpcsProvider.LOG.info("removeGroup - {}", input);
        return null;
    }

    private ListenableFuture<RpcResult<UpdateGroupOutput>> updateGroup(
            final UpdateGroupInput input) {
        OpenflowpluginGroupTestRpcsProvider.LOG.info("updateGroup - {}", input);
        return null;
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(AddGroup.class, this::addGroup)
            .put(RemoveGroup.class, this::removeGroup)
            .put(UpdateGroup.class, this::updateGroup)
            .build();
    }

    public Registration register(final RpcProviderService rpcRegistry) {
        setGroupRegistration(rpcRegistry.registerRpcImplementations(this.getRpcClassToInstanceMap(), ImmutableSet.of(
            InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId(OpenflowpluginTestActivator.NODE_ID))))));

        return new AbstractObjectRegistration<>(this) {
            @Override
            protected void removeRegistration() {
                groupRegistration.close();
            }
        };
    }
}
