/**
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
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.role.RoleChangeException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ControllerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kramesha on 8/24/15.
 */
public class RoleService extends AbstractSimpleService<RoleRequestInputBuilder, RoleRequestOutput> {
    private static final Logger LOG = LoggerFactory.getLogger(RoleService.class);

    private final DeviceContext deviceContext;

    protected RoleService(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final Class<RoleRequestOutput> clazz) {
        super(requestContextStack, deviceContext, clazz);
        this.deviceContext = deviceContext;
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final RoleRequestInputBuilder input) {
        input.setXid(xid.getValue());
        return input.build();
    }

    public Future<BigInteger> getGenerationIdFromDevice(final Short version) {
        final NodeId nodeId = deviceContext.getPrimaryConnectionContext().getNodeId();
        LOG.info("getGenerationIdFromDevice called for device:{}", nodeId.getValue());

        // send a dummy no-change role request to get the generation-id of the switch
        final RoleRequestInputBuilder roleRequestInputBuilder = new RoleRequestInputBuilder();
        roleRequestInputBuilder.setRole(toOFJavaRole(OfpRole.NOCHANGE));
        roleRequestInputBuilder.setVersion(version);
        roleRequestInputBuilder.setGenerationId(BigInteger.ZERO);

        final SettableFuture<BigInteger> finalFuture = SettableFuture.create();
        final ListenableFuture<RpcResult<RoleRequestOutput>> genIdListenableFuture = handleServiceCall(roleRequestInputBuilder);
        Futures.addCallback(genIdListenableFuture, new FutureCallback<RpcResult<RoleRequestOutput>>() {
            @Override
            public void onSuccess(final RpcResult<RoleRequestOutput> roleRequestOutputRpcResult) {
                if (roleRequestOutputRpcResult.isSuccessful()) {
                    final RoleRequestOutput roleRequestOutput = roleRequestOutputRpcResult.getResult();
                    if (roleRequestOutput != null) {
                        LOG.debug("roleRequestOutput.getGenerationId()={}", roleRequestOutput.getGenerationId());
                        finalFuture.set(roleRequestOutput.getGenerationId());
                    } else {
                        LOG.info("roleRequestOutput is null in getGenerationIdFromDevice");
                        finalFuture.setException(new RoleChangeException("Exception in getting generationId for device:" + nodeId.getValue()));
                    }

                } else {
                    LOG.error("getGenerationIdFromDevice RPC error " +
                            roleRequestOutputRpcResult.getErrors().iterator().next().getInfo());

                }

            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.info("onFailure - getGenerationIdFromDevice RPC error {}", throwable);
                finalFuture.setException(new ExecutionException(throwable));
            }
        });
        return finalFuture;
    }


    public Future<SetRoleOutput> submitRoleChange(final OfpRole ofpRole, final Short version, final BigInteger generationId) {
        LOG.info("submitRoleChange called for device:{}, role:{}",
                deviceContext.getPrimaryConnectionContext().getNodeId(), ofpRole);
        final RoleRequestInputBuilder roleRequestInputBuilder = new RoleRequestInputBuilder();
        roleRequestInputBuilder.setRole(toOFJavaRole(ofpRole));
        roleRequestInputBuilder.setVersion(version);
        roleRequestInputBuilder.setGenerationId(generationId);

        final ListenableFuture<RpcResult<RoleRequestOutput>> roleListenableFuture = handleServiceCall(roleRequestInputBuilder);

        final SettableFuture<SetRoleOutput> finalFuture = SettableFuture.create();
        Futures.addCallback(roleListenableFuture, new FutureCallback<RpcResult<RoleRequestOutput>>() {
            @Override
            public void onSuccess(final RpcResult<RoleRequestOutput> roleRequestOutputRpcResult) {
                LOG.info("submitRoleChange onSuccess for device:{}, role:{}",
                        deviceContext.getPrimaryConnectionContext().getNodeId(), ofpRole);
                final RoleRequestOutput roleRequestOutput = roleRequestOutputRpcResult.getResult();
                final SetRoleOutputBuilder setRoleOutputBuilder = new SetRoleOutputBuilder();
                setRoleOutputBuilder.setTransactionId(new TransactionId(BigInteger.valueOf(roleRequestOutput.getXid())));
               finalFuture.set(setRoleOutputBuilder.build());
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("submitRoleChange onFailure for device:{}, role:{}",
                        deviceContext.getPrimaryConnectionContext().getNodeId(), ofpRole, throwable);
                finalFuture.set(null);
            }
        });
        return finalFuture;
    }

    private static ControllerRole toOFJavaRole(final OfpRole role) {
        ControllerRole ofJavaRole = null;
        switch (role) {
            case BECOMEMASTER:
                ofJavaRole = ControllerRole.OFPCRROLEMASTER;
                break;
            case BECOMESLAVE:
                ofJavaRole = ControllerRole.OFPCRROLESLAVE;
                break;
            case NOCHANGE:
                ofJavaRole = ControllerRole.OFPCRROLENOCHANGE;
                break;
            default:
                // no intention
                LOG.warn("given role is not supported by protocol roles: {}", role);
                break;
        }
        return ofJavaRole;
    }


}
