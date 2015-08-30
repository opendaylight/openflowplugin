/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidate;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
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
 * Created by kramesha on 9/12/15.
 */
public class RoleContextImpl implements RoleContext {
    private static final Logger LOG = LoggerFactory.getLogger(RoleContextImpl.class);

    private EntityOwnershipService entityOwnershipService;
    private EntityOwnershipCandidateRegistration entityOwnershipCandidateRegistration;
    private final EntityOwnershipCandidate entityOwnershipCandidate;
    private final RpcProviderRegistry rpcProviderRegistry;
    private DeviceContext deviceContext;
    private SettableFuture<Boolean> roleChangeFuture;


    public RoleContextImpl(DeviceContext deviceContext, RpcProviderRegistry rpcProviderRegistry,
                           EntityOwnershipService entityOwnershipService) {
        this.entityOwnershipService = entityOwnershipService;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.deviceContext = deviceContext;
        entityOwnershipCandidate = new OpenflowOwnershipCandidate(this);
    }

    @Override
    public ListenableFuture<Boolean> facilitateRoleChange() {
        roleChangeFuture = SettableFuture.create();

        //make a call to entity ownership service and listen for notifications from the service
        requestOpenflowEntityOwnership();

        return roleChangeFuture;
    }

    private void requestOpenflowEntityOwnership() {
        LOG.info("RoleManager requesting ownership for device:{}", deviceContext.getPrimaryConnectionContext().getNodeId().getValue());
        Entity entity = new Entity("openflow", deviceContext.getPrimaryConnectionContext().getNodeId().getValue());

        try {
            entityOwnershipCandidateRegistration =
                    entityOwnershipService.registerCandidate(entity, entityOwnershipCandidate);
            LOG.info("RoleManager : Candidate registered with ownership sevice for device :{}", deviceContext.getPrimaryConnectionContext().getNodeId().getValue());
        } catch (CandidateAlreadyRegisteredException e) {
            // we can log and move for this error, as listener is present and role changes will be served.
            LOG.error("Candidate - Entity already registered with Openflow candidate ", entity, e );
        }
    }

    @Override
    public void onRoleChanged(final OfpRole oldRole, final OfpRole newRole) {
        LOG.debug("Role change received from ownership listener from {} to {} for device:{}", oldRole, newRole,
                deviceContext.getPrimaryConnectionContext().getNodeId());

        final SetRoleInput setRoleInput = (new SetRoleInputBuilder())
                .setControllerRole(newRole)
                .setNode(new NodeRef(deviceContext.getDeviceState().getNodeInstanceIdentifier()))
                .build();

        Future<RpcResult<SetRoleOutput>> setRoleOutputFuture = rpcProviderRegistry.getRpcService(SalRoleService.class)
                .setRole(setRoleInput);

        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(setRoleOutputFuture), new FutureCallback<RpcResult<SetRoleOutput>>() {
            @Override
            public void onSuccess(RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
                LOG.debug("Rolechange {} successful made on switch :{}", newRole,
                        deviceContext.getPrimaryConnectionContext().getNodeId());
                deviceContext.getDeviceState().setRole(newRole);
                roleChangeFuture.set(true);
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error("Error in setRole {} for device {} ", newRole,
                        deviceContext.getPrimaryConnectionContext().getNodeId(), throwable);
                roleChangeFuture.setException(throwable);
            }
        });
    }

    @Override
    public void close() throws Exception {
        if (entityOwnershipCandidateRegistration != null) {
            entityOwnershipCandidateRegistration.close();
        }
    }
}
