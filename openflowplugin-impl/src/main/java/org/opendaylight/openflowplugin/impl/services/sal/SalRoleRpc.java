/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext.CONNECTION_STATE;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.RoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class SalRoleRpc extends AbstractSimpleService<SetRoleInput, SetRoleOutput> implements SetRole {
    private static final Logger LOG = LoggerFactory.getLogger(SalRoleRpc.class);
    private static final Uint64 MAX_GENERATION_ID = Uint64.valueOf("ffffffffffffffff", 16);

    private final DeviceContext deviceContext;
    private final RoleService roleService;

    public SalRoleRpc(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, SetRoleOutput.class);
        this.deviceContext = requireNonNull(deviceContext);
        roleService =  new RoleService(requestContextStack, deviceContext, RoleRequestOutput.class);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final SetRoleInput input) {
        return null;
    }

    @Override
    public ListenableFuture<RpcResult<SetRoleOutput>> invoke(final SetRoleInput input) {
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

        return Futures.transformAsync(roleService.getGenerationIdFromDevice(getVersion()), generationId -> {
            LOG.debug("RoleChangeTask, GenerationIdFromDevice from device {} is {}",
                    getDeviceInfo().getNodeId().getValue(), generationId);
            final Uint64 nextGenerationId = getNextGenerationId(generationId);
            LOG.debug("nextGenerationId received from device:{} is {}",
                    getDeviceInfo().getNodeId().getValue(), nextGenerationId);
            return roleService.submitRoleChange(role, getVersion(), nextGenerationId);
        }, MoreExecutors.directExecutor());
    }

    private static Uint64 getNextGenerationId(final Uint64 generationId) {
        if (generationId.compareTo(MAX_GENERATION_ID) < 0) {
            return Uint64.valueOf(generationId.longValue() + 1);
        } else {
            return Uint64.ZERO;
        }
    }
}
