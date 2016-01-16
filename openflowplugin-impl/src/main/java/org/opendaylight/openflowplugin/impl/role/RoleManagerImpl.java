/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.CheckForNull;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.impl.util.DeviceInitializationUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets invoked from RpcManagerInitial, registers a candidate with EntityOwnershipService.
 * On receipt of the ownership notification, makes an rpc call to SalRoleSevice.
 *
 * Hands over to StatisticsManager at the end.
 */
public class RoleManagerImpl implements RoleManager {
    private static final Logger LOG = LoggerFactory.getLogger(RoleManagerImpl.class);

    private DeviceInitializationPhaseHandler deviceInitializationPhaseHandler;
    private final EntityOwnershipService entityOwnershipService;
    private final RpcProviderRegistry rpcProviderRegistry;
    private final ConcurrentHashMap<DeviceContext, RoleContext> contexts = new ConcurrentHashMap<>();
    private final OpenflowOwnershipListener openflowOwnershipListener;
    private final boolean switchFeaturesMandatory;

    public RoleManagerImpl(final RpcProviderRegistry rpcProviderRegistry,
            final EntityOwnershipService entityOwnershipService, final boolean switchFeaturesMandatory) {
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.rpcProviderRegistry = Preconditions.checkNotNull(rpcProviderRegistry);
        this.switchFeaturesMandatory = switchFeaturesMandatory;
        this.openflowOwnershipListener = new OpenflowOwnershipListener(entityOwnershipService);
        LOG.debug("Registering OpenflowOwnershipListener listening to all entity ownership changes");
        openflowOwnershipListener.init();
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitializationPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(@CheckForNull final DeviceContext deviceContext) {
        LOG.debug("RoleManager called for device:{}", deviceContext.getPrimaryConnectionContext().getNodeId());
        if (deviceContext.getDeviceState().getFeatures().getVersion() < OFConstants.OFP_VERSION_1_3) {
            // Roles are not supported before OF1.3, so move forward.
            deviceInitializationPhaseHandler.onDeviceContextLevelUp(deviceContext);
            return;
        }

        final RoleContext roleContext = new RoleContextImpl(deviceContext, rpcProviderRegistry, entityOwnershipService, openflowOwnershipListener);
        contexts.put(deviceContext, roleContext);
        // if the device context gets closed (mostly on connection close), we would need to cleanup
        deviceContext.addDeviceContextClosedHandler(roleContext);
        OfpRole role = null;
        try {
            role = roleContext.initialization().get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException | CandidateAlreadyRegisteredException e) {
            LOG.warn("Unexpected exception by DeviceConection {}. Connection has to close.", deviceContext.getDeviceState().getNodeId(), e);
            final Optional<EntityOwnershipState> entityOwnershipStateOptional = entityOwnershipService.getOwnershipState(roleContext.getEntity());
            if (entityOwnershipStateOptional.isPresent()) {
                role = entityOwnershipStateOptional.get().isOwner() ? OfpRole.BECOMEMASTER : OfpRole.BECOMESLAVE;
            } else {
                deviceContext.close();
                return;
            }
        }
        if (OfpRole.BECOMEMASTER.equals(role)) {
            final ListenableFuture<Void> initNodeFuture = DeviceInitializationUtils.initializeNodeInformation(deviceContext, switchFeaturesMandatory);
            Futures.addCallback(initNodeFuture, new FutureCallback<Void>() {
                @Override
                public void onSuccess(final Void result) {
                    LOG.trace("Node {} was initialized", deviceContext.getDeviceState().getNodeId());
                    getRoleContextLevelUp(deviceContext);
                }

                @Override
                public void onFailure(final Throwable t) {
                    LOG.warn("Node {} Initialization fail", deviceContext.getDeviceState().getNodeId(), t);
                    deviceContext.close();
                }
            });
        } else {
            getRoleContextLevelUp(deviceContext);
        }

    }

    void getRoleContextLevelUp(final DeviceContext deviceContext) {
        LOG.debug("Created role context for node {}", deviceContext.getDeviceState().getNodeId());
        LOG.debug("roleChangeFuture success for device:{}. Moving to StatisticsManager", deviceContext.getDeviceState().getNodeId());
        deviceInitializationPhaseHandler.onDeviceContextLevelUp(deviceContext);
    }

    @Override
    public void close() throws Exception {
        for (final Map.Entry<DeviceContext, RoleContext> roleContextEntry : contexts.entrySet()) {
            if (roleContextEntry.getValue() != null) {
                roleContextEntry.getValue().close();
            }
        }
        this.openflowOwnershipListener.close();
    }
}
