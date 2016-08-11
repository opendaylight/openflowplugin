/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext.CONNECTION_STATE;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.role.RoleChangeException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class SalRoleServiceImpl extends AbstractSimpleService<SetRoleInput, SetRoleOutput> implements SalRoleService  {

    private static final Logger LOG = LoggerFactory.getLogger(SalRoleServiceImpl.class);

    private static final BigInteger MAX_GENERATION_ID = new BigInteger("ffffffffffffffff", 16);

    private static final int MAX_RETRIES = 42;

    private final DeviceContext deviceContext;
    private final RoleService roleService;

    private final Semaphore currentRoleGuard = new Semaphore(1, true);

    @GuardedBy("currentRoleGuard")
    private OfpRole currentRole = OfpRole.NOCHANGE;

    public SalRoleServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, SetRoleOutput.class);
        this.deviceContext = Preconditions.checkNotNull(deviceContext);
        this.roleService =  new RoleService(requestContextStack, deviceContext, RoleRequestOutput.class);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final SetRoleInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<SetRoleOutput>> setRole(final SetRoleInput input) {
        LOG.info("SetRole called with input:{}", input);
        try {
            currentRoleGuard.acquire();
            LOG.trace("currentRole lock queue length: {} " + currentRoleGuard.getQueueLength());
        } catch (final InterruptedException e) {
            LOG.error("Unexpected exception {} for acquire semaphore for input {}", e, input);
            return RpcResultBuilder.<SetRoleOutput> failed().buildFuture();
        }
        // compare with last known role and set if different. If they are same, then return.
        if (currentRole.equals(input.getControllerRole())) {
            LOG.info("Role to be set is same as the last known role for the device:{}. Hence ignoring.",
                    input.getControllerRole());
            currentRoleGuard.release();
            return RpcResultBuilder.<SetRoleOutput> success().buildFuture();
        }

        final SettableFuture<RpcResult<SetRoleOutput>> resultFuture = SettableFuture.<RpcResult<SetRoleOutput>> create();
        repeaterForChangeRole(resultFuture, input, 0);
        /* Add Callback for release Guard */
        Futures.addCallback(resultFuture, new FutureCallback<RpcResult<SetRoleOutput>>() {

            @Override
            public void onSuccess(final RpcResult<SetRoleOutput> result) {
                LOG.debug("SetRoleService for Node: {} is ok Role: {}", input.getNode().getValue(),
                        input.getControllerRole());
                currentRoleGuard.release();
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.error("SetRoleService set Role {} for Node: {} fail . Reason {}", input.getControllerRole(),
                        input.getNode().getValue(), t);
                currentRoleGuard.release();
            }
        });
        return resultFuture;
    }

    private void repeaterForChangeRole(final SettableFuture<RpcResult<SetRoleOutput>> future, final SetRoleInput input,
            final int retryCounter) {
        if (future.isCancelled()) {
            future.setException(new RoleChangeException(String.format(
                    "Set Role for device %s stop because Future was canceled", input.getNode().getValue())));
            return;
        }
        if (retryCounter >= MAX_RETRIES) {
            future.setException(new RoleChangeException(String.format("Set Role failed after %s tries on device %s",
                    MAX_RETRIES, input.getNode().getValue())));
            return;
        }
        // Check current connection state
        final CONNECTION_STATE state = deviceContext.getPrimaryConnectionContext().getConnectionState();
        switch (state) {
        case RIP:
            LOG.info("Device {} has been disconnected", input.getNode());
            future.setException(new Exception(String.format(
                    "Device connection doesn't exist anymore. Primary connection status : %s", state)));
            return;
        case WORKING:
            // We can proceed
            LOG.trace("Device {} has been working", input.getNode());
            break;
        default:
            LOG.warn("Device {} is in state {}, role change is not allowed", input.getNode(), state);
            future.setException(new Exception(String.format("Unexcpected device connection status : %s", state)));
            return;
        }

        LOG.info("Requesting state change to {}", input.getControllerRole());
        final ListenableFuture<RpcResult<SetRoleOutput>> changeRoleFuture = tryToChangeRole(input.getControllerRole());
        Futures.addCallback(changeRoleFuture, new FutureCallback<RpcResult<SetRoleOutput>>() {

            @Override
            public void onSuccess(final RpcResult<SetRoleOutput> result) {
                if (result.isSuccessful()) {
                    LOG.debug("setRoleOutput received after roleChangeTask execution:{}", result);
                    currentRole = input.getControllerRole();
                    future.set(RpcResultBuilder.<SetRoleOutput> success().withResult(result.getResult()).build());
                } else {
                    LOG.error("setRole() failed with errors, will retry: {} times.", MAX_RETRIES - retryCounter);
                    repeaterForChangeRole(future, input, (retryCounter + 1));
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.error("Exception in setRole(), will retry: {} times.", t, MAX_RETRIES - retryCounter);
                repeaterForChangeRole(future, input, (retryCounter + 1));
            }
        });
    }

    private ListenableFuture<RpcResult<SetRoleOutput>> tryToChangeRole(final OfpRole role) {
        LOG.info("RoleChangeTask called on device:{} OFPRole:{}", getDeviceInfo().getNodeId().getValue(), role);

        final Future<BigInteger> generationFuture = roleService.getGenerationIdFromDevice(getVersion());

        return Futures.transform(JdkFutureAdapters.listenInPoolThread(generationFuture), (AsyncFunction<BigInteger, RpcResult<SetRoleOutput>>) generationId -> {
            LOG.debug("RoleChangeTask, GenerationIdFromDevice from device {} is {}", getDeviceInfo().getNodeId().getValue(), generationId);
            final BigInteger nextGenerationId = getNextGenerationId(generationId);
            LOG.debug("nextGenerationId received from device:{} is {}", getDeviceInfo().getNodeId().getValue(), nextGenerationId);
            final Future<RpcResult<SetRoleOutput>> submitRoleFuture = roleService.submitRoleChange(role, getVersion(), nextGenerationId);
            return JdkFutureAdapters.listenInPoolThread(submitRoleFuture);
        });
    }

    private static BigInteger getNextGenerationId(final BigInteger generationId) {
        if (generationId.compareTo(MAX_GENERATION_ID) < 0) {
            return generationId.add(BigInteger.ONE);
        } else {
            return BigInteger.ZERO;
        }
    }
}
