/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.CheckForNull;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidate;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets invoked from RpcManagerInitial, registers a candidate with EntityOwnershipService.
 * On receipt of the ownership notification, makes an rpc call to SalRoleSevice.
 *
 * Hands over to StatisticsManager at the end.
 */
public class RoleManagerImpl implements RoleManager {
    private static final Logger LOG = LoggerFactory.getLogger(RoleManagerImpl.class);

    private DeviceInitializationPhaseHandler deviceInitializationPhaseHandler;
    private EntityOwnershipService entityOwnershipService;
    private EntityOwnershipCandidateRegistration entityOwnershipCandidateRegistration;
    private final EntityOwnershipCandidate entityOwnershipCandidate;
    private final RpcProviderRegistry rpcProviderRegistry;
    private DeviceContext deviceContext;


    public RoleManagerImpl(RpcProviderRegistry rpcProviderRegistry, EntityOwnershipService entityOwnershipService) {
        this.entityOwnershipService = entityOwnershipService;
        this.rpcProviderRegistry = rpcProviderRegistry;
        entityOwnershipCandidate = new OpenflowOwnershipCandidate(this);
    }

    @Override
    public void setDeviceInitializationPhaseHandler(DeviceInitializationPhaseHandler handler) {
        deviceInitializationPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(@CheckForNull DeviceContext deviceContext) {
        if (deviceContext.getDeviceState().getFeatures().getVersion() < OFConstants.OFP_VERSION_1_3) {
            // Roles are not supported before OF1.3, so move forward.
            deviceInitializationPhaseHandler.onDeviceContextLevelUp(deviceContext);
        }

        this.deviceContext = deviceContext;
        //make a call to entity ownership service and listen for notifications from the service

        requestOpenflowEntityOwnership();
    }

    private void requestOpenflowEntityOwnership() {
        Entity entity = new Entity("openflow", deviceContext.getPrimaryConnectionContext().getNodeId().getValue());

        try {
            entityOwnershipCandidateRegistration =
                    entityOwnershipService.registerCandidate(entity, entityOwnershipCandidate);
        } catch (CandidateAlreadyRegisteredException e) {
            // we can log and move for this error, as listener is present and role changes will be served.
            LOG.error("Candidate - Entity already registered with Openflow candidate ", entity, e );
        }
    }

    @Override
    public void onRoleChanged(OfpRole oldRole, OfpRole newRole) {
        LOG.debug("Role change received from ownership listener from {} to {} for device:{}", oldRole, newRole,
                deviceContext.getPrimaryConnectionContext().getNodeId());

        SetRoleInput setRoleInput = (new SetRoleInputBuilder())
                .setControllerRole(newRole)
                .setNode(new NodeRef(deviceContext.getDeviceState().getNodeInstanceIdentifier()))
                .build();

        Future<RpcResult<SetRoleOutput>> setRoleOutputFuture = rpcProviderRegistry.getRpcService(SalRoleService.class).setRole(setRoleInput);
        RpcResult<SetRoleOutput> rpcResult = null;
        try {
            rpcResult = setRoleOutputFuture.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Error in getting RoleOutput for device {} ", deviceContext.getPrimaryConnectionContext().getNodeId(), e);
        }

        if (rpcResult != null && rpcResult.isSuccessful()) {
            LOG.debug("Rolechange {} successful made on switch :{}", newRole,
                    deviceContext.getPrimaryConnectionContext().getNodeId());
            deviceContext.getDeviceState().setRole(newRole);
        }

        deviceInitializationPhaseHandler.onDeviceContextLevelUp(deviceContext);
    }

    @Override
    public void close() throws Exception {
        if (entityOwnershipCandidateRegistration != null) {
            entityOwnershipCandidateRegistration.close();
        }
    }
}
