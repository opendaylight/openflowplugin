/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.role.RoleChangeException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SalRoleServiceImpl implements SalRoleService {

    private static final Logger LOG = LoggerFactory.getLogger(SalRoleServiceImpl.class);

    private static final BigInteger MAX_GENERATION_ID = new BigInteger("ffffffffffffffff", 16);

    private static final int MAX_RETRIES = 42;

    private final DeviceContext deviceContext;
    private final RoleService roleService;
    private final AtomicReference<OfpRole> lastKnownRoleRef = new AtomicReference<>(OfpRole.NOCHANGE);
    private final ListeningExecutorService listeningExecutorService;
    private final NodeId nodeId;
    private final Short version;

    public SalRoleServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        //super(requestContextStack, deviceContext, SetRoleOutput.class);
        this.deviceContext = deviceContext;
        this.roleService =  new RoleService(requestContextStack, deviceContext, RoleRequestOutput.class);
        nodeId = deviceContext.getPrimaryConnectionContext().getNodeId();
        version = deviceContext.getPrimaryConnectionContext().getFeatures().getVersion();
        listeningExecutorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    }

//    @Override
//    protected OfHeader buildRequest(Xid xid, SetRoleInput input) {
//        return null;
//    }

    public static BigInteger getNextGenerationId(BigInteger generationId) {
        BigInteger nextGenerationId = null;
        if (generationId.compareTo(MAX_GENERATION_ID) < 0) {
            nextGenerationId = generationId.add(BigInteger.ONE);
        } else {
            nextGenerationId = BigInteger.ZERO;
        }

        return nextGenerationId;
    }


    @Override
    public Future<RpcResult<SetRoleOutput>> setRole(final SetRoleInput input) {
        LOG.info("SetRole called with input:{}", input);
        OfpRole lastKnownRole = lastKnownRoleRef.get();

        // compare with last known role and set if different. If they are same, then return.
        if (lastKnownRoleRef.compareAndSet(input.getControllerRole(), input.getControllerRole())) {
            LOG.info("Role to be set is same as the last known role for the device:{}. Hence ignoring.", input.getControllerRole());
            SettableFuture<RpcResult<SetRoleOutput>> resultFuture = SettableFuture.create();
            resultFuture.set(RpcResultBuilder.<SetRoleOutput>success().build());
            return resultFuture;
        }

        final SettableFuture<RpcResult<SetRoleOutput>> resultFuture = SettableFuture.create();

        RoleChangeTask roleChangeTask = new RoleChangeTask(nodeId, input.getControllerRole(), version, roleService);

        do {
            ListenableFuture<RpcResult<SetRoleOutput>> deviceCheck = deviceConnectionCheck();
            if (deviceCheck != null) {
                LOG.info("Device {} is disconnected or state is not valid. Giving up on role change", input.getNode());
                return deviceCheck;
            }

            ListenableFuture<SetRoleOutput> taskFuture = listeningExecutorService.submit(roleChangeTask);
            LOG.info("RoleChangeTask submitted for execution");
            CheckedFuture<SetRoleOutput, RoleChangeException> taskFutureChecked = makeCheckedFuture(taskFuture);
            try {
                SetRoleOutput setRoleOutput = taskFutureChecked.checkedGet(10, TimeUnit.SECONDS);
                LOG.info("setRoleOutput received after roleChangeTask execution:{}", setRoleOutput);
                resultFuture.set(RpcResultBuilder.<SetRoleOutput>success().withResult(setRoleOutput).build());
                lastKnownRoleRef.set(input.getControllerRole());
                return resultFuture;

            } catch (TimeoutException | RoleChangeException e) {
                roleChangeTask.incrementRetryCounter();
                LOG.info("Exception in setRole(), will retry:" + (MAX_RETRIES - roleChangeTask.getRetryCounter()) + " times.", e);
            }

        } while (roleChangeTask.getRetryCounter() < MAX_RETRIES);

        resultFuture.setException(new RoleChangeException("Set Role failed after " + MAX_RETRIES + "tries on device " + input.getNode().getValue()));

        return resultFuture;
    }

    private ListenableFuture<RpcResult<SetRoleOutput>> deviceConnectionCheck() {
        if (!ConnectionContext.CONNECTION_STATE.WORKING.equals(deviceContext.getPrimaryConnectionContext().getConnectionState())) {
            ListenableFuture<RpcResult<SetRoleOutput>> resultingFuture = SettableFuture.create();
            switch (deviceContext.getPrimaryConnectionContext().getConnectionState()) {
                case RIP:
                    final String errMsg = String.format("Device connection doesn't exist anymore. Primary connection status : %s",
                            deviceContext.getPrimaryConnectionContext().getConnectionState());
                    resultingFuture = Futures.immediateFailedFuture(new Throwable(errMsg));
                    break;
                default:
                    resultingFuture = Futures.immediateCheckedFuture(RpcResultBuilder.<SetRoleOutput>failed().build());
                    break;
            }
            return resultingFuture;
        }
        return null;
    }

    class RoleChangeTask implements Callable<SetRoleOutput> {

        private final NodeId nodeId;
        private final OfpRole ofpRole;
        private final Short version;
        private final RoleService roleService;
        private int retryCounter = 0;

        public RoleChangeTask(NodeId nodeId, OfpRole ofpRole, Short version, RoleService roleService) {
            this.nodeId = nodeId;
            this.ofpRole = ofpRole;
            this.version = version;
            this.roleService = roleService;
        }

        @Override
        public SetRoleOutput call() throws RoleChangeException {
            LOG.info("RoleChangeTask called on device:{} OFPRole:{}", this.nodeId.getValue(), ofpRole);

            // we cannot move ahead without having the generation id, so block the thread till we get it.
            BigInteger generationId = null;
            SetRoleOutput setRoleOutput = null;

            try {
                generationId = this.roleService.getGenerationIdFromDevice(version).get(10, TimeUnit.SECONDS);
                LOG.info("RoleChangeTask, GenerationIdFromDevice from device is {}", generationId);

            } catch (Exception e ) {
                LOG.info("Exception in getting generationId for device:{}. Ex:{}" + this.nodeId.getValue(), e);
                throw new RoleChangeException("Exception in getting generationId for device:"+ this.nodeId.getValue(), e);
            }


            LOG.info("GenerationId received from device:{} is {}", nodeId.getValue(), generationId);

            final BigInteger nextGenerationId = getNextGenerationId(generationId);

            LOG.info("nextGenerationId received from device:{} is {}", nodeId.getValue(), nextGenerationId);

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
            this.retryCounter = retryCounter + 1;
        }

        public int getRetryCounter() {
            return retryCounter;
        }
    }

    public static CheckedFuture<SetRoleOutput, RoleChangeException> makeCheckedFuture(ListenableFuture<SetRoleOutput> rolePushResult) {
        return Futures.makeChecked(rolePushResult,
                new Function<Exception, RoleChangeException>() {
                    @Override
                    public RoleChangeException apply(Exception input) {
                        RoleChangeException output = null;
                        if (input instanceof ExecutionException) {
                            if (input.getCause() instanceof RoleChangeException) {
                                output = (RoleChangeException) input.getCause();
                            }
                        }

                        if (output == null) {
                            output = new RoleChangeException(input.getMessage(), input);
                        }

                        return output;
                    }
                });
    }
}
