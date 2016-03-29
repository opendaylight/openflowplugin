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
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.services.SalRoleServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kramesha on 9/12/15.
 */
public class RoleContextImpl implements RoleContext {
    private static final Logger LOG = LoggerFactory.getLogger(RoleContextImpl.class);

    private final EntityOwnershipService entityOwnershipService;
    private EntityOwnershipCandidateRegistration entityOwnershipCandidateRegistration;
    private EntityOwnershipCandidateRegistration txEntityOwnershipCandidateRegistration;

    private final DeviceContext deviceContext;

    @GuardedBy("mainCandidateGuard")
    private final Entity entity;
    @GuardedBy("txCandidateGuard")
    private final Entity txEntity;

    private SalRoleService salRoleService;

    private final Semaphore roleChangeGuard = new Semaphore(1, true);

    @GuardedBy("roleChangeGuard")
    private OfpRole clusterRole;

    public RoleContextImpl(final DeviceContext deviceContext, final EntityOwnershipService entityOwnershipService,
                           final Entity entity, final Entity txEntity) {
        this.entityOwnershipService = Preconditions.checkNotNull(entityOwnershipService);
        this.deviceContext = Preconditions.checkNotNull(deviceContext);
        this.entity = Preconditions.checkNotNull(entity);
        this.txEntity = Preconditions.checkNotNull(txEntity);
        salRoleService = new SalRoleServiceImpl(this, deviceContext);
        clusterRole = OfpRole.BECOMESLAVE;
    }

    @Override
    public void initializationRoleContext() {
        LOG.trace("Initialization MainCandidate for Node {}", deviceContext.getDeviceState().getNodeId());
        final AsyncFunction<RpcResult<SetRoleOutput>, Void> initFunction = new AsyncFunction<RpcResult<SetRoleOutput>, Void>() {
            @Override
            public ListenableFuture<Void> apply(final RpcResult<SetRoleOutput> input) throws Exception {
                LOG.debug("Initialization request OpenflowEntityOwnership for entity {}", entity);
                getDeviceState().setRole(OfpRole.BECOMESLAVE);
                entityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(entity);
                LOG.debug("RoleContextImpl : Candidate registered with ownership service for device :{}", deviceContext
                        .getPrimaryConnectionContext().getNodeId().getValue());
                return Futures.immediateFuture(null);
            }
        };

        try {
            roleChangeGuard.acquire();
            final ListenableFuture<Void> roleChange = sendRoleChangeToDevice(OfpRole.BECOMESLAVE, initFunction);
            Futures.addCallback(roleChange, new FutureCallback<Void>() {

                @Override
                public void onSuccess(final Void result) {
                    LOG.debug("Initial RoleContext for Node {} is successful", deviceContext.getDeviceState().getNodeId());
                    roleChangeGuard.release();
                }

                @Override
                public void onFailure(final Throwable t) {
                    LOG.warn("Initial RoleContext for Node {} fail", deviceContext.getDeviceState().getNodeId(), t);
                    roleChangeGuard.release();
                    deviceContext.shutdownConnection();
                }
            });
        } catch (final Exception e) {
            LOG.warn("Unexpected exception bu Initialization RoleContext for Node {}", deviceContext.getDeviceState().getNodeId(), e);
            roleChangeGuard.release();
            deviceContext.shutdownConnection();
        }
    }

    @Override
    public void terminationRoleContext() {
        LOG.trace("Termination MainCandidate for Node {}", deviceContext.getDeviceState().getNodeId());
        if (null != entityOwnershipCandidateRegistration) {
            LOG.debug("Closing EntityOwnershipCandidateRegistration for {}", entity);
            try {
                roleChangeGuard.acquire();
            } catch (final InterruptedException e) {
                LOG.warn("Unexpected exception in closing EntityOwnershipCandidateRegistration process for entity {}", entity);
            } finally {
                entityOwnershipCandidateRegistration.close();
                entityOwnershipCandidateRegistration = null;
                // FIXME: call suspendTxCandidate here means lost protection for possible Delete Node before take ownership
                // by another ClusterNode, but it stabilized cluster behavior in general - So try to find another solution
                suspendTxCandidate();
                roleChangeGuard.release();
            }
        }
    }

    @Override
    public void onDeviceTryToTakeClusterLeadership() {
        LOG.trace("onDeviceTryToTakeClusterLeadership method call for Entity {}", entity);
        boolean callShutdown = false;
        try {
            roleChangeGuard.acquire();
            Verify.verify(null != entityOwnershipCandidateRegistration);
            Verify.verify(OfpRole.BECOMESLAVE.equals(clusterRole));

            clusterRole = OfpRole.BECOMEMASTER;
            /* register TxCandidate and wait for mainCandidateGuard release from onDeviceTakeLeadership method */
            setupTxCandidate();

        } catch (final Exception e) {
            LOG.warn("Unexpected exception in roleChange process for entity {}", entity);
            callShutdown = true;
        } finally {
            roleChangeGuard.release();
        }
        if (callShutdown) {
            deviceContext.shutdownConnection();
        }
    }

    @Override
    public void onDeviceTakeClusterLeadership() {
        LOG.trace("onDeviceTakeClusterLeadership for entity {}", txEntity);
        try {
            roleChangeGuard.acquire();
            Verify.verify(null != txEntityOwnershipCandidateRegistration);
            Verify.verify(OfpRole.BECOMEMASTER.equals(clusterRole));

            if (null == entityOwnershipCandidateRegistration) {
                LOG.debug("EntityOwnership candidate for entity {} is closed.", txEntity);
                suspendTxCandidate();
                roleChangeGuard.release();
                return;
            }

            final ListenableFuture<Void> future = onRoleChanged(OfpRole.BECOMESLAVE, OfpRole.BECOMEMASTER);
            Futures.addCallback(future, new FutureCallback<Void>() {

                @Override
                public void onSuccess(final Void result) {
                    LOG.debug("Take Leadership for node {} was successful", getDeviceState().getNodeId());
                    roleChangeGuard.release();
                }

                @Override
                public void onFailure(final Throwable t) {
                    LOG.warn("Take Leadership for node {} failed", getDeviceState().getNodeId(), t);
                    roleChangeGuard.release();
                    deviceContext.shutdownConnection();
                }
            });

        } catch (final Exception e) {
            LOG.warn("Unexpected exception in roleChange process for entity {}", txEntity);
            roleChangeGuard.release();
            deviceContext.shutdownConnection();
        }
    };

    @Override
    public void onDeviceLostClusterLeadership() {
        LOG.trace("onDeviceLostClusterLeadership method call for Entity {}", entity);
        try {
            roleChangeGuard.acquire();
            Verify.verify(null != entityOwnershipCandidateRegistration);
            Verify.verify(null != txEntityOwnershipCandidateRegistration);
            Verify.verify(OfpRole.BECOMEMASTER.equals(clusterRole));

            clusterRole = OfpRole.BECOMESLAVE;

            final ListenableFuture<Void> future = onRoleChanged(OfpRole.BECOMEMASTER, OfpRole.BECOMESLAVE);
            Futures.addCallback(future, new FutureCallback<Void>() {

                @Override
                public void onSuccess(final Void result) {
                    LOG.debug("Lost Leadership for node {} was successful", getDeviceState().getNodeId());
                    suspendTxCandidate();
                    roleChangeGuard.release();
                }

                @Override
                public void onFailure(final Throwable t) {
                    LOG.debug("Lost Leadership for node {} faild", getDeviceState().getNodeId(), t);
                    roleChangeGuard.release();
                    deviceContext.shutdownConnection();
                }

            });

        } catch (final Exception e) {
            LOG.warn("Unexpected exception in roleChange process for entity {}", entity);
            roleChangeGuard.release();
            deviceContext.shutdownConnection();
        }
    }

    @Override
    public boolean isMainCandidateRegistered() {
        final boolean result;
        try {
            roleChangeGuard.acquire();
        } catch (final InterruptedException e) {
            LOG.warn("Unexpected exception in check EntityOwnershipCandidateRegistration process for entity {}", entity);
        } finally {
            result = entityOwnershipCandidateRegistration != null;
            roleChangeGuard.release();
        }
        return result;
    }

    @Override
    public boolean isTxCandidateRegistered() {
        final boolean result;
        try {
            roleChangeGuard.acquire();
        } catch (final InterruptedException e) {
            LOG.warn("Unexpected exception in check TxEntityOwnershipCandidateRegistration process for txEntity {}", txEntity);
        } finally {
            result = txEntityOwnershipCandidateRegistration != null;
            roleChangeGuard.release();
        }
        return result;
    }

    @VisibleForTesting
    ListenableFuture<Void> onRoleChanged(final OfpRole oldRole, final OfpRole newRole) {
        LOG.trace("onRoleChanged method call for Entity {}", entity);

        if (!isDeviceConnected()) {
            // this can happen as after the disconnect, we still get a last message from EntityOwnershipService.
            LOG.debug("Device {} is disconnected from this node. Hence not attempting a role change.", deviceContext
                    .getPrimaryConnectionContext().getNodeId());
            // we don't need to do anything
            return Futures.immediateFuture(null);
        }

        final AsyncFunction<RpcResult<SetRoleOutput>, Void> roleChangeFunction = new AsyncFunction<RpcResult<SetRoleOutput>, Void>() {
            @Override
            public ListenableFuture<Void> apply(final RpcResult<SetRoleOutput> setRoleOutputRpcResult) throws Exception {
                LOG.debug("Role change {} successful made on switch :{}", newRole, deviceContext.getDeviceState().getNodeId());
                getDeviceState().setRole(newRole);
                return deviceContext.onClusterRoleChange(oldRole, newRole);
            }
        };
        return sendRoleChangeToDevice(newRole, roleChangeFunction);
    }

    @GuardedBy("roleChangeGuard")
    private void setupTxCandidate() throws Exception {
        LOG.debug("setupTxCandidate for entity {} and Transaction entity {}", entity, txEntity);
        Verify.verify(txEntity != null);
        Verify.verify(entityOwnershipCandidateRegistration != null);
        Verify.verify(txEntityOwnershipCandidateRegistration == null);
        txEntityOwnershipCandidateRegistration = entityOwnershipService.registerCandidate(txEntity);
    }

    @GuardedBy("roleChangeGuard")
    private void suspendTxCandidate() {
        LOG.trace("Suspend TxCandidate for Node {}", deviceContext.getDeviceState().getNodeId());
        if (null != txEntityOwnershipCandidateRegistration) {
            LOG.debug("Closing TxEntityOwnershipCandidateRegistration for {}", txEntity);
            txEntityOwnershipCandidateRegistration.close();
            txEntityOwnershipCandidateRegistration = null;
        }
    }

    @Override
    public void close() {
        LOG.trace("Close RoleCtx for Node {}", deviceContext.getDeviceState().getNodeId());
        if (null != entityOwnershipCandidateRegistration) {
            LOG.info("Close Node Entity {} registration", entity);
            entityOwnershipCandidateRegistration.close();
            entityOwnershipCandidateRegistration = null;
        }
        if (null != txEntityOwnershipCandidateRegistration) {
            LOG.info("Close Tx Entity {} registration", txEntity);
            txEntityOwnershipCandidateRegistration.close();
            txEntityOwnershipCandidateRegistration = null;
        }
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public Entity getTxEntity() {
        return txEntity;
    }

    private boolean isDeviceConnected() {
        return ConnectionContext.CONNECTION_STATE.WORKING.equals(
                deviceContext.getPrimaryConnectionContext().getConnectionState());
    }

    @Nullable
    @Override
    public <T> RequestContext<T> createRequestContext() {
        final AbstractRequestContext<T> ret = new AbstractRequestContext<T>(deviceContext.reservedXidForDeviceMessage()) {
            @Override
            public void close() {
            }
        };
        return ret;
    }

    @VisibleForTesting
    void setSalRoleService(final SalRoleService salRoleService) {
        this.salRoleService = salRoleService;
    }

    @Override
    public DeviceState getDeviceState() {
        return deviceContext.getDeviceState();
    }

    @Override
    public DeviceContext getDeviceContext() {
        return deviceContext;
    }

    private ListenableFuture<Void> sendRoleChangeToDevice(final OfpRole newRole, final AsyncFunction<RpcResult<SetRoleOutput>, Void> function) {
        LOG.debug("Send new role {} to device {}", newRole, deviceContext.getDeviceState().getNodeId());
        final Future<RpcResult<SetRoleOutput>> setRoleOutputFuture;
        if (deviceContext.getDeviceState().getFeatures().getVersion() < OFConstants.OFP_VERSION_1_3) {
            LOG.debug("Device OF version {} not support ROLE", deviceContext.getDeviceState().getFeatures().getVersion());
            setRoleOutputFuture = Futures.immediateFuture(RpcResultBuilder.<SetRoleOutput> success().build());
        } else {
            final SetRoleInput setRoleInput = (new SetRoleInputBuilder()).setControllerRole(newRole)
                    .setNode(new NodeRef(deviceContext.getDeviceState().getNodeInstanceIdentifier())).build();
            setRoleOutputFuture = salRoleService.setRole(setRoleInput);
            final TimerTask timerTask = new TimerTask() {

                @Override
                public void run(final Timeout timeout) throws Exception {
                    if (!setRoleOutputFuture.isDone()) {
                        LOG.info("New role {} was not propagated to device {} during 10 sec. Close connection immediately.",
                                newRole, deviceContext.getDeviceState().getNodeId());
                        setRoleOutputFuture.cancel(true);
                    }
                }
            };
            deviceContext.getTimer().newTimeout(timerTask, 10, TimeUnit.SECONDS);
        }
        return Futures.transform(JdkFutureAdapters.listenInPoolThread(setRoleOutputFuture), function);
    }


    @Override
    public OfpRole getClusterRole() {
        final OfpRole role;
        try {
            roleChangeGuard.acquire();
        } catch (final InterruptedException e) {
            LOG.warn("Unexpected exception in get ClusterRole process for entity {}", entity);
        } finally {
            role = OfpRole.forValue(clusterRole.getIntValue());
            roleChangeGuard.release();
        }
        return role;
    }

}
