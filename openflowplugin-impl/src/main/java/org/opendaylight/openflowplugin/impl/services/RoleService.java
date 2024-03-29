/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.ExecutionException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.role.RoleChangeException;
import org.opendaylight.openflowplugin.impl.util.ErrorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ControllerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleService extends AbstractSimpleService<RoleRequestInputBuilder, RoleRequestOutput> {
    private static final Logger LOG = LoggerFactory.getLogger(RoleService.class);

    private final DeviceContext deviceContext;

    public RoleService(final RequestContextStack requestContextStack,
                       final DeviceContext deviceContext,
                       final Class<RoleRequestOutput> clazz) {
        super(requestContextStack, deviceContext, clazz);
        this.deviceContext = deviceContext;
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final RoleRequestInputBuilder input) {
        return input.setXid(xid.getValue()).build();
    }

    public ListenableFuture<Uint64> getGenerationIdFromDevice(final Uint8 version) {
        LOG.info("getGenerationIdFromDevice called for device: {}", getDeviceInfo().getNodeId().getValue());

        // send a dummy no-change role request to get the generation-id of the switch
        final var finalFuture = SettableFuture.<Uint64>create();
        Futures.addCallback(handleServiceCall(new RoleRequestInputBuilder()
            .setRole(ControllerRole.OFPCRROLENOCHANGE)
            .setVersion(version)
            .setGenerationId(Uint64.ZERO)),
            new FutureCallback<>() {
                @Override
                public void onSuccess(final RpcResult<RoleRequestOutput> roleRequestOutputRpcResult) {
                    if (roleRequestOutputRpcResult.isSuccessful()) {
                        final var roleRequestOutput = roleRequestOutputRpcResult.getResult();
                        if (roleRequestOutput != null) {
                            LOG.debug("roleRequestOutput.getGenerationId()={}", roleRequestOutput.getGenerationId());
                            finalFuture.set(roleRequestOutput.getGenerationId());
                        } else {
                            LOG.info("roleRequestOutput is null in getGenerationIdFromDevice");
                            finalFuture.setException(
                                new RoleChangeException("Exception in getting generationId for device: "
                                    + getDeviceInfo().getNodeId().getValue()));
                        }
                    } else {
                        final var errors = roleRequestOutputRpcResult.getErrors();
                        LOG.error("getGenerationIdFromDevice RPC error {}", errors.iterator().next().getInfo());
                        finalFuture.setException(new RoleChangeException(ErrorUtil.errorsToString(errors)));
                    }
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    LOG.info("onFailure - getGenerationIdFromDevice RPC error", throwable);
                    finalFuture.setException(new ExecutionException(throwable));
                }
            }, MoreExecutors.directExecutor());
        return finalFuture;
    }


    public ListenableFuture<RpcResult<SetRoleOutput>> submitRoleChange(final OfpRole ofpRole, final Uint8 version,
                                                                       final Uint64 generationId) {
        LOG.info("submitRoleChange called for device:{}, role:{}", getDeviceInfo().getNodeId(), ofpRole);
        final var finalFuture = SettableFuture.<RpcResult<SetRoleOutput>>create();
        Futures.addCallback(handleServiceCall(new RoleRequestInputBuilder()
            .setRole(toOFJavaRole(ofpRole))
            .setVersion(version)
            .setGenerationId(generationId)),
            new FutureCallback<>() {
                @Override
                public void onSuccess(final RpcResult<RoleRequestOutput> roleRequestOutputRpcResult) {
                    LOG.info("submitRoleChange onSuccess for device:{}, role:{}",
                        getDeviceInfo().getNodeId(), ofpRole);
                    final var roleRequestOutput = roleRequestOutputRpcResult.getResult();
                    final var rpcErrors = roleRequestOutputRpcResult.getErrors();
                    if (roleRequestOutput != null) {
                        finalFuture.set(RpcResultBuilder.<SetRoleOutput>success()
                            .withResult(new SetRoleOutputBuilder()
                                .setTransactionId(new TransactionId(Uint64.valueOf(roleRequestOutput.getXid())))
                                .build())
                            .build());

                    } else if (rpcErrors != null) {
                        LOG.trace("roleRequestOutput is null , rpcErrors={}", rpcErrors);
                        for (RpcError rpcError : rpcErrors) {
                            LOG.warn("RpcError on submitRoleChange for {}: {}",
                                deviceContext.getPrimaryConnectionContext().getNodeId(), rpcError.toString());
                        }
                        finalFuture.set(RpcResultBuilder.<SetRoleOutput>failed().withRpcErrors(rpcErrors).build());
                    }
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    LOG.error("submitRoleChange onFailure for device:{}, role:{}", getDeviceInfo().getNodeId(), ofpRole,
                        throwable);
                    finalFuture.setException(throwable);
                }
            }, MoreExecutors.directExecutor());
        return finalFuture;
    }

    private static ControllerRole toOFJavaRole(final OfpRole role) {
        return switch (role) {
            case BECOMEMASTER -> ControllerRole.OFPCRROLEMASTER;
            case BECOMESLAVE -> ControllerRole.OFPCRROLESLAVE;
            case NOCHANGE -> ControllerRole.OFPCRROLENOCHANGE;
        };
    }
}
