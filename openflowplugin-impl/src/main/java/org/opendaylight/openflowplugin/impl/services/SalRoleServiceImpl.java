/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
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

    private static final Function<Exception, RoleChangeException> EXCEPTION_FUNCTION = new Function<Exception, RoleChangeException>() {
        @Override
        public RoleChangeException apply(final Exception input) {
            if (input instanceof ExecutionException) {
                final Throwable cause = input.getCause();
                if (cause instanceof RoleChangeException) {
                    return (RoleChangeException) cause;
                }
            } else if (input instanceof RoleChangeException) {
                return (RoleChangeException) input;
            }

            return new RoleChangeException(input.getMessage(), input);
        }
    };

    private final DeviceContext deviceContext;
    private final RoleService roleService;
    private final AtomicReference<OfpRole> lastKnownRoleRef = new AtomicReference<>(OfpRole.NOCHANGE);
    private final ListeningExecutorService listeningExecutorService;
    private final NodeId nodeId;
    private final Short version;

    public SalRoleServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, SetRoleOutput.class);
        this.deviceContext = Preconditions.checkNotNull(deviceContext);
        this.roleService =  new RoleService(requestContextStack, deviceContext, RoleRequestOutput.class);
        nodeId = deviceContext.getPrimaryConnectionContext().getNodeId();
        version = deviceContext.getPrimaryConnectionContext().getFeatures().getVersion();
        listeningExecutorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final SetRoleInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<SetRoleOutput>> setRole(final SetRoleInput input) {
        LOG.info("SetRole called with input:{}", input);

        // compare with last known role and set if different. If they are same, then return.
        if (lastKnownRoleRef.compareAndSet(input.getControllerRole(), input.getControllerRole())) {
            LOG.info("Role to be set is same as the last known role for the device:{}. Hence ignoring.", input.getControllerRole());
            return Futures.immediateFuture(RpcResultBuilder.<SetRoleOutput>success().build());
        }

        RoleChangeTask roleChangeTask = new RoleChangeTask(input.getControllerRole());

        do {
            // Check current connection state
            final CONNECTION_STATE state = deviceContext.getPrimaryConnectionContext().getConnectionState();
            switch (state) {
                case RIP:
                    LOG.info("Device {} has been disconnected", input.getNode());
                    return Futures.immediateFailedFuture(new Exception(String.format(
                        "Device connection doesn't exist anymore. Primary connection status : %s", state)));
                case WORKING:
                    // We can proceed
                    break;
                default:
                    LOG.info("Device {} is in state {}, role change is not allowed", input.getNode(), state);
                    return Futures.immediateCheckedFuture(RpcResultBuilder.<SetRoleOutput>failed().build());
            }

            ListenableFuture<SetRoleOutput> taskFuture = listeningExecutorService.submit(roleChangeTask);
            LOG.info("RoleChangeTask submitted for execution");
            CheckedFuture<SetRoleOutput, RoleChangeException> taskFutureChecked = Futures.makeChecked(taskFuture, EXCEPTION_FUNCTION);
            try {
                SetRoleOutput setRoleOutput = taskFutureChecked.checkedGet(10, TimeUnit.SECONDS);
                LOG.info("setRoleOutput received after roleChangeTask execution:{}", setRoleOutput);
                lastKnownRoleRef.set(input.getControllerRole());
                return Futures.immediateFuture(RpcResultBuilder.<SetRoleOutput>success().withResult(setRoleOutput).build());

            } catch (TimeoutException | RoleChangeException e) {
                roleChangeTask.incrementRetryCounter();
                LOG.info("Exception in setRole(), will retry: {} times.",
                    MAX_RETRIES - roleChangeTask.getRetryCounter(), e);
            }

        } while (roleChangeTask.getRetryCounter() < MAX_RETRIES);

        return Futures.immediateFailedFuture(new RoleChangeException(
            "Set Role failed after " + MAX_RETRIES + "tries on device " + input.getNode().getValue()));
    }

    private static BigInteger getNextGenerationId(final BigInteger generationId) {
        if (generationId.compareTo(MAX_GENERATION_ID) < 0) {
            return generationId.add(BigInteger.ONE);
        } else {
            return BigInteger.ZERO;
        }
    }

    private final class RoleChangeTask implements Callable<SetRoleOutput> {

        private final OfpRole ofpRole;
        private int retryCounter = 0;

        RoleChangeTask(final OfpRole ofpRole) {
            this.ofpRole = Preconditions.checkNotNull(ofpRole);
        }

        @Override
        public SetRoleOutput call() throws RoleChangeException {
            LOG.info("RoleChangeTask called on device:{} OFPRole:{}", nodeId.getValue(), ofpRole);

            // we cannot move ahead without having the generation id, so block the thread till we get it.
            final BigInteger generationId;
            try {
                generationId = roleService.getGenerationIdFromDevice(version).get(10, TimeUnit.SECONDS);
                LOG.info("RoleChangeTask, GenerationIdFromDevice from device is {}", generationId);
            } catch (Exception e ) {
                LOG.info("Exception in getting generationId for device:{}", nodeId.getValue(), e);
                throw new RoleChangeException("Exception in getting generationId for device:"+ nodeId.getValue(), e);
            }

            LOG.info("GenerationId received from device:{} is {}", nodeId.getValue(), generationId);
            final BigInteger nextGenerationId = getNextGenerationId(generationId);
            LOG.info("nextGenerationId received from device:{} is {}", nodeId.getValue(), nextGenerationId);

            final SetRoleOutput setRoleOutput;
            try {
                setRoleOutput = roleService.submitRoleChange(ofpRole, version, nextGenerationId).get(10 , TimeUnit.SECONDS);
                LOG.info("setRoleOutput after submitRoleChange:{}", setRoleOutput);

            }  catch (InterruptedException | ExecutionException |  TimeoutException e) {
                LOG.error("Exception in making role change for device", e);
                throw new RoleChangeException("Exception in making role change for device:" + nodeId.getValue());
            }

            return setRoleOutput;
        }

        public void incrementRetryCounter() {
            this.retryCounter++;
        }

        public int getRetryCounter() {
            return retryCounter;
        }
    }
}
