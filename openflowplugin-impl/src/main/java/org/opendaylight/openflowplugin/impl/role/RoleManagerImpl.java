/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.util.concurrent.FutureCallback;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.CheckForNull;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
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

    public RoleManagerImpl(final RpcProviderRegistry rpcProviderRegistry, final EntityOwnershipService entityOwnershipService) {
        this.entityOwnershipService = entityOwnershipService;
        this.rpcProviderRegistry = rpcProviderRegistry;
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
        LOG.debug("Created role context");

        // if the device context gets closed (mostly on connection close), we would need to cleanup
        deviceContext.addDeviceContextClosedHandler(roleContext);

        roleContext.facilitateRoleChange(new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean aBoolean) {
                LOG.debug("roleChangeFuture success for device:{}. Moving to StatisticsManager", deviceContext.getDeviceState().getNodeId());
                deviceInitializationPhaseHandler.onDeviceContextLevelUp(deviceContext);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("RoleChange on device {} was not successful after several attempts. " +
                        "Closing the device Context, reconnect the device and start over",
                        deviceContext.getPrimaryConnectionContext().getNodeId().getValue(), throwable);
                try {
                    deviceContext.close();
                } catch (final Exception e) {
                    LOG.warn("Error closing device context for device:{}",
                            deviceContext.getPrimaryConnectionContext().getNodeId().getValue(),  e);
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        for (final Map.Entry<DeviceContext, RoleContext> roleContextEntry : contexts.entrySet()) {
            roleContextEntry.getValue().close();
        }
        this.openflowOwnershipListener.close();
    }
}
