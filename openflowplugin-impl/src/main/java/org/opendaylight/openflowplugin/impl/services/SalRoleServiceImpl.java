/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
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
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SalRoleServiceImpl extends AbstractSimpleService<SetRoleInput, SetRoleOutput> implements SalRoleService  {

    private static final Logger LOG = LoggerFactory.getLogger(SalRoleServiceImpl.class);

    private static final BigInteger MAX_GENERATION_ID = new BigInteger("ffffffffffffffff", 16);

    private static final int MAX_RETRIES = 42;

    private final DeviceContext deviceContext;
    private final RoleService roleService;
    private final AtomicReference<OfpRole> lastKnownRoleRef = new AtomicReference<>(OfpRole.NOCHANGE);
    private final ExecutorService executorService;
    private final NodeId nodeId;
    private final Short version;

    public SalRoleServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, SetRoleOutput.class);
        this.deviceContext = deviceContext;
        this.roleService =  new RoleService(requestContextStack, deviceContext, RoleRequestOutput.class);
        nodeId = deviceContext.getPrimaryConnectionContext().getNodeId();
        version = deviceContext.getPrimaryConnectionContext().getFeatures().getVersion();
        executorService = Executors.newSingleThreadExecutor();

    }

    @Override
    protected OfHeader buildRequest(Xid xid, SetRoleInput input) {
        return null;
    }

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
        OfpRole lastKnownRole = lastKnownRoleRef.get();

        // compare with last known role and set if different. If they are same, then return.
        if (lastKnownRoleRef.compareAndSet(input.getControllerRole(), input.getControllerRole())) {
            LOG.info("Role to be set is same as the last known role for the device:{}. Hence ignoring.", input.getControllerRole());
            SettableFuture<RpcResult<SetRoleOutput>> resultFuture = SettableFuture.create();
            resultFuture.set(RpcResultBuilder.<SetRoleOutput>success().build());
            return resultFuture;
        } else {
            // current role and new role are different
            lastKnownRoleRef.set(input.getControllerRole());
        }

        SettableFuture<RpcResult<SetRoleOutput>> resultFuture = SettableFuture.create();

        Future<SetRoleOutput> taskFuture = executorService.submit(new RoleChangeTask(nodeId, input.getControllerRole(), version, roleService));

        try {
            SetRoleOutput setRoleOutput = taskFuture.get(5, TimeUnit.SECONDS);
            if (setRoleOutput != null) {
                resultFuture.set(RpcResultBuilder.<SetRoleOutput>success().withResult(setRoleOutput).build());

            } else {
                resultFuture.set(RpcResultBuilder.<SetRoleOutput>failed().withError(RpcError.ErrorType.RPC,
                        "Set Role failed after " + MAX_RETRIES + "tries on device " + input.getNode().getValue()).build());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Exception in setRole {} {} ", nodeId.getValue(), input.getControllerRole(), e);
            resultFuture.set(RpcResultBuilder.<SetRoleOutput>failed().withError(RpcError.ErrorType.RPC,
                    "Exception in setRole " +input.getControllerRole()+ " for device:" + nodeId.getValue())
                    .build());
        } catch (TimeoutException e) {
            resultFuture.set(RpcResultBuilder.<SetRoleOutput>failed().withError(RpcError.ErrorType.RPC,
                    "Set Role RPC Future timeout, " + input.getNode().getValue()).build());
        }

        return resultFuture;
    }


    class RoleChangeTask implements Callable<SetRoleOutput> {

        private final NodeId nodeId;
        private final OfpRole ofpRole;
        private final Short version;
        private final RoleService roleService;

        public RoleChangeTask(NodeId nodeId, OfpRole ofpRole, Short version, RoleService roleService) {
            this.nodeId = nodeId;
            this.ofpRole = ofpRole;
            this.version = version;
            this.roleService = roleService;
        }

        @Override
        public SetRoleOutput call() throws Exception {
            LOG.debug("setRole called on device:{} OFPRole:{}", this.nodeId.getValue(), ofpRole);

            int tries = 1;

            SetRoleOutput roleChangeResult = null;
            do {
                tries++;
                try {
                    final BigInteger generationId = this.roleService.getGenerationIdFromDevice(version).get(5, TimeUnit.SECONDS);
                    if (generationId.intValue() == -1) {
                        throw new RoleChangeException("Exception in getting generationId for device:" + nodeId.getValue());
                    }

                    LOG.info("GenerationId received from device:{} is {}", nodeId.getValue(), generationId);

                    final BigInteger nextGenerationId = getNextGenerationId(generationId);

                    LOG.info("nextGenerationId received from device:{} is {}", nodeId.getValue(), generationId);

                    roleChangeResult = roleService.submitRoleChange(ofpRole, version, nextGenerationId).get(5, TimeUnit.SECONDS);
                    if (roleChangeResult == null ) {
                        throw new RoleChangeException("Exception in role change for device:" + nodeId + " OFRole:" + ofpRole);
                    }

                } catch (RoleChangeException | InterruptedException | ExecutionException | TimeoutException e) {
                    LOG.debug("Exception in setRole(), will retry:" + (MAX_RETRIES - tries) + " times.", e);
                }
            } while(roleChangeResult == null && tries < MAX_RETRIES);

            return roleChangeResult;
        }
    }


}
