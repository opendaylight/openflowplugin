/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

    @GuardedBy("this")
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

        synchronized (this) {
            // compare with last known role and set if different. If they are same, then return.
            if (currentRole == input.getControllerRole()) {
                LOG.info("Role to be set is same as the last known role for the device:{}. Hence ignoring.", input.getControllerRole());
                return RpcResultBuilder.<SetRoleOutput>success().buildFuture();
            }

            int retryCounter = 0;
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
                        return RpcResultBuilder.<SetRoleOutput>failed().buildFuture();
                }

                LOG.info("Requesting state change to {}", input.getControllerRole());

                try {
                    final SetRoleOutput result = tryChangeRole(input.getControllerRole());
                    LOG.info("setRoleOutput received after roleChangeTask execution:{}", result);

                    currentRole = input.getControllerRole();
                    return RpcResultBuilder.<SetRoleOutput>success().withResult(result).buildFuture();
                } catch (RoleChangeException e) {
                    retryCounter++;
                    LOG.info("Exception in setRole(), will retry: {} times.", MAX_RETRIES - retryCounter, e);
                } catch (Exception e) {
                    LOG.warn("Unexpected failure to set role on {}", nodeId, e);
                    return Futures.immediateFailedFuture(e);
                }
            } while (retryCounter < MAX_RETRIES);
        }

        return Futures.immediateFailedFuture(new RoleChangeException(
            "Set Role failed after " + MAX_RETRIES + "tries on device " + input.getNode().getValue()));
    }

    private SetRoleOutput tryChangeRole(final OfpRole role) throws RoleChangeException {
        LOG.info("RoleChangeTask called on device:{} OFPRole:{}", nodeId.getValue(), role);

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
            setRoleOutput = roleService.submitRoleChange(role, version, nextGenerationId).get(10 , TimeUnit.SECONDS);
            LOG.info("setRoleOutput after submitRoleChange:{}", setRoleOutput);
        }  catch (Exception e) {
            LOG.error("Exception in making role change for device", e);
            throw new RoleChangeException("Exception in making role change for device:" + nodeId.getValue());
        }

        return setRoleOutput;
    }

    private static BigInteger getNextGenerationId(final BigInteger generationId) {
        if (generationId.compareTo(MAX_GENERATION_ID) < 0) {
            return generationId.add(BigInteger.ONE);
        } else {
            return BigInteger.ZERO;
        }
    }
}
