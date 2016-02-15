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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
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
    private final NodeId nodeId;
    private final Short version;

    private final Semaphore currentRoleGuard = new Semaphore(1, true);

    @GuardedBy("currentRoleGuard")
    private OfpRole currentRole = OfpRole.NOCHANGE;

    public SalRoleServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, SetRoleOutput.class);
        this.deviceContext = Preconditions.checkNotNull(deviceContext);
        this.roleService =  new RoleService(requestContextStack, deviceContext, RoleRequestOutput.class);
        nodeId = deviceContext.getPrimaryConnectionContext().getNodeId();
        version = deviceContext.getPrimaryConnectionContext().getFeatures().getVersion();
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
            LOG.trace("currentRole lock queue: " + currentRoleGuard.getQueueLength());
        } catch (final InterruptedException e) {
            LOG.warn("Unexpected exception for acquire semaphor for input {}", input);
            return RpcResultBuilder.<SetRoleOutput> success().buildFuture();
        }
        // compare with last known role and set if different. If they are same, then return.
        if (currentRole == input.getControllerRole()) {
            LOG.info("Role to be set is same as the last known role for the device:{}. Hence ignoring.",
                    input.getControllerRole());
            currentRoleGuard.release();
            return RpcResultBuilder.<SetRoleOutput> success().buildFuture();
        }

        final SettableFuture<RpcResult<SetRoleOutput>> resultFuture = SettableFuture
                .<RpcResult<SetRoleOutput>> create();
        repeaterForChangeRole(resultFuture, input, 0);
        return resultFuture;
    }

    private void repeaterForChangeRole(final SettableFuture<RpcResult<SetRoleOutput>> future, final SetRoleInput input,
            final int retryCounter) {
        if (retryCounter >= MAX_RETRIES) {
            currentRoleGuard.release();
            future.setException(new RoleChangeException(String.format("Set Role failed after %s tries on device %s",
                    MAX_RETRIES, input.getNode().getValue())));
            return;
        }
        // Check current connection state
        final CONNECTION_STATE state = deviceContext.getPrimaryConnectionContext().getConnectionState();
        switch (state) {
        case RIP:
            LOG.info("Device {} has been disconnected", input.getNode());
            currentRoleGuard.release();
            future.setException(new Exception(String.format(
                    "Device connection doesn't exist anymore. Primary connection status : %s", state)));
            return;
        case WORKING:
            // We can proceed
            break;
        default:
            LOG.warn("Device {} is in state {}, role change is not allowed", input.getNode(), state);
            currentRoleGuard.release();
            future.setException(new Exception(String.format("Unexcpected device connection status : %s", state)));
            return;
        }

        LOG.info("Requesting state change to {}", input.getControllerRole());
        final ListenableFuture<SetRoleOutput> changeRoleFuture = tryToChangeRole(input.getControllerRole());
        Futures.addCallback(changeRoleFuture, new FutureCallback<SetRoleOutput>() {

            @Override
            public void onSuccess(final SetRoleOutput result) {
                LOG.info("setRoleOutput received after roleChangeTask execution:{}", result);
                currentRole = input.getControllerRole();
                currentRoleGuard.release();
                future.set(RpcResultBuilder.<SetRoleOutput> success().withResult(result).build());
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.info("Exception in setRole(), will retry: {} times.", MAX_RETRIES - retryCounter, t);
                repeaterForChangeRole(future, input, (retryCounter + 1));
            }
        });
    }

    private ListenableFuture<SetRoleOutput> tryToChangeRole(final OfpRole role) {
        LOG.info("RoleChangeTask called on device:{} OFPRole:{}", nodeId.getValue(), role);

        final Future<BigInteger> generationFuture = roleService.getGenerationIdFromDevice(version);

        return Futures.transform(JdkFutureAdapters.listenInPoolThread(generationFuture), new AsyncFunction<BigInteger, SetRoleOutput>() {

            @Override
            public ListenableFuture<SetRoleOutput> apply(final BigInteger generationId) throws Exception {
                LOG.debug("RoleChangeTask, GenerationIdFromDevice from device {} is {}", nodeId.getValue(), generationId);
                final BigInteger nextGenerationId = getNextGenerationId(generationId);
                LOG.debug("nextGenerationId received from device:{} is {}", nodeId.getValue(), nextGenerationId);
                final Future<SetRoleOutput> submitRoleFuture = roleService.submitRoleChange(role, version, nextGenerationId);
                return JdkFutureAdapters.listenInPoolThread(submitRoleFuture);
            }
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
