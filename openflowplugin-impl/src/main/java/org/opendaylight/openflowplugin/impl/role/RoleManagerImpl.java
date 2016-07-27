/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.impl.services.SalRoleServiceImpl;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets invoked from RpcManagerInitial, registers a candidate with EntityOwnershipService.
 * On receipt of the ownership notification, makes an rpc call to SalRoleService.
 *
 * Hands over to StatisticsManager at the end.
 */
public class RoleManagerImpl implements RoleManager {
    private static final Logger LOG = LoggerFactory.getLogger(RoleManagerImpl.class);

    // Maximum limit of timeout retries when cleaning DS, to prevent infinite recursive loops
    private static final int MAX_CLEAN_DS_RETRIES = 3;

    private DeviceInitializationPhaseHandler deviceInitializationPhaseHandler;
    private DeviceTerminationPhaseHandler deviceTerminationPhaseHandler;
    private final DataBroker dataBroker;
    private final ConcurrentMap<DeviceInfo, RoleContext> contexts = new ConcurrentHashMap<>();
    private final HashedWheelTimer hashedWheelTimer;

    public RoleManagerImpl(final DataBroker dataBroker, final HashedWheelTimer hashedWheelTimer) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.hashedWheelTimer = hashedWheelTimer;
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitializationPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(@CheckForNull final DeviceInfo deviceInfo, final LifecycleService lifecycleService) throws Exception {
        final DeviceContext deviceContext = Preconditions.checkNotNull(lifecycleService.getDeviceContext());
        final RoleContext roleContext = new RoleContextImpl(deviceInfo, hashedWheelTimer, this);
        roleContext.setSalRoleService(new SalRoleServiceImpl(roleContext, deviceContext));
        Verify.verify(contexts.putIfAbsent(deviceInfo, roleContext) == null, "Role context for master Node %s is still not closed.", deviceInfo.getNodeId());
        roleContext.makeDeviceSlave().get();
        lifecycleService.setRoleContext(roleContext);
        deviceInitializationPhaseHandler.onDeviceContextLevelUp(deviceInfo, lifecycleService);
    }

    @Override
    public void close() {
        LOG.debug("Close method on role manager was called.");
        for (final Iterator<RoleContext> iterator = Iterators.consumingIterator(contexts.values().iterator()); iterator.hasNext();) {
            // got here because last known role is LEADER and DS might need clearing up
            final RoleContext roleContext = iterator.next();
            contexts.remove(roleContext.getDeviceInfo());
            removeDeviceFromOperationalDS(roleContext.getDeviceInfo(), MAX_CLEAN_DS_RETRIES);
        }
    }

    @Override
    public void onDeviceContextLevelDown(final DeviceInfo deviceInfo) {
        contexts.remove(deviceInfo);
        deviceTerminationPhaseHandler.onDeviceContextLevelDown(deviceInfo);
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> removeDeviceFromOperationalDS(final DeviceInfo deviceInfo, final int numRetries) {
        final WriteTransaction delWtx = dataBroker.newWriteOnlyTransaction();
        delWtx.delete(LogicalDatastoreType.OPERATIONAL, DeviceStateUtil.createNodeInstanceIdentifier(deviceInfo.getNodeId()));
        final CheckedFuture<Void, TransactionCommitFailedException> delFuture = delWtx.submit();

        Futures.addCallback(delFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                LOG.debug("Delete Node {} was successful", deviceInfo);
                contexts.remove(deviceInfo);
            }

            @Override
            public void onFailure(@Nonnull final Throwable t) {
                // If we have any retries left, we will try to clean the datastore again
                if (numRetries > 0) {
                    // We "used" one retry here, so decrement it
                    final int curRetries = numRetries - 1;
                    LOG.debug("Delete node {} failed with exception {}. Trying again (retries left: {})", deviceInfo.getNodeId(), t, curRetries);
                    // Recursive call to this method with "one less" retry
                    removeDeviceFromOperationalDS(deviceInfo, curRetries);
                    return;
                }

                // No retries left, so we will just close the role context, and ignore datastore cleanup
                LOG.warn("Delete node {} failed with exception {}. No retries left, aborting", deviceInfo.getNodeId(), t);
                contexts.remove(deviceInfo);
            }
        });

        return delFuture;
    }

    @Override
    public void setDeviceTerminationPhaseHandler(final DeviceTerminationPhaseHandler handler) {
        deviceTerminationPhaseHandler = handler;
    }

    @VisibleForTesting
    RoleContext getRoleContext(final DeviceInfo deviceInfo){
        return contexts.get(deviceInfo);
    }

    @Override
    public <T extends OFPContext> T gainContext(final DeviceInfo deviceInfo) {
        return (T) contexts.get(deviceInfo);
    }
}
