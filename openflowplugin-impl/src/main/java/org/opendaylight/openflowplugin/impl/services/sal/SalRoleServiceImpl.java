/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext.CONNECTION_STATE;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.RoleService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class SalRoleServiceImpl extends AbstractSimpleService<SetRoleInput, SetRoleOutput> implements SalRoleService  {
    private static final Logger LOG = LoggerFactory.getLogger(SalRoleServiceImpl.class);
    private static final BigInteger MAX_GENERATION_ID = new BigInteger("ffffffffffffffff", 16);

    private final DeviceContext deviceContext;
    private final RoleService roleService;

    public SalRoleServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, SetRoleOutput.class);
        this.deviceContext = Preconditions.checkNotNull(deviceContext);
        this.roleService =  new RoleService(requestContextStack, deviceContext, RoleRequestOutput.class);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final SetRoleInput input) throws ServiceException {
        return null;
    }

    @Override
    public Future<RpcResult<SetRoleOutput>> setRole(final SetRoleInput input) {
        LOG.info("SetRole called with input:{}", input);

        // Check current connection state
        final CONNECTION_STATE state = deviceContext.getPrimaryConnectionContext().getConnectionState();
        switch (state) {
            case RIP:
                LOG.info("Device {} has been disconnected", input.getNode());
                return Futures.immediateFailedFuture(new Exception(String
                        .format("Device connection doesn't exist anymore. Primary connection status : %s",
                                state)));
            case WORKING:
                // We can proceed
                LOG.trace("Device {} has been working", input.getNode());
                break;
            default:
                LOG.warn("Device {} is in state {}, role change is not allowed", input.getNode(), state);
                return Futures.immediateFailedFuture(new Exception(String
                        .format("Unexpected device connection status : %s", state)));
        }

        LOG.info("Requesting state change to {}", input.getControllerRole());
        return tryToChangeRole(input.getControllerRole());
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