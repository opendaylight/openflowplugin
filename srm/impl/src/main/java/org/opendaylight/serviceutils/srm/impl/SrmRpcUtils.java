/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm.impl;

import com.google.common.collect.ImmutableMap;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.ServiceOps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.Services;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.ServicesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.services.Operations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.services.OperationsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RpcFailEntityId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RpcFailEntityName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RpcFailEntityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RpcFailUnknown;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RpcSuccess;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusIfm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusIfmInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusItm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusItmTep;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusItmTz;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtAclInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtAclInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtDhcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtElan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtElanInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtL2gw;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtL2gwConnection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtL2gwNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtQos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtQosPolicyInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtVpn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtVpnInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.Ofplugin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.ServiceOpRecover;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.ServiceOpReinstall;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Utility class for SRM Shell.
 */
public final class SrmRpcUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SrmRpcUtils.class);
    private static final Boolean REINSTALL_FAILED = Boolean.FALSE;
    private static final Boolean REINSTALL_SUCCESS = Boolean.TRUE;

    private SrmRpcUtils() {
        // Hidden on purpose
    }

    private static final ImmutableMap<EntityNameBase, EntityTypeBase> NAME_TO_TYPE_MAP =
        ImmutableMap.<EntityNameBase, EntityTypeBase>builder()
            .put(GeniusItm.VALUE, EntityTypeService.VALUE)
            .put(GeniusIfm.VALUE, EntityTypeService.VALUE)
            .put(NetvirtVpn.VALUE, EntityTypeService.VALUE)
            .put(NetvirtElan.VALUE, EntityTypeService.VALUE)
            .put(NetvirtL2gw.VALUE, EntityTypeService.VALUE)
            .put(NetvirtDhcp.VALUE, EntityTypeService.VALUE)
            .put(NetvirtAcl.VALUE, EntityTypeService.VALUE)
            .put(Ofplugin.VALUE, EntityTypeService.VALUE)
            .put(GeniusItmTep.VALUE, EntityTypeInstance.VALUE)
            .put(GeniusItmTz.VALUE, EntityTypeInstance.VALUE)
            .put(GeniusIfmInterface.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtVpnInstance.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtElanInterface.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtL2gwConnection.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtL2gwNode.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtQos.VALUE, EntityTypeService.VALUE)
            .put(NetvirtQosPolicyInstance.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtAclInterface.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtAclInstance.VALUE, EntityTypeInstance.VALUE)
            .build();

    private static final ImmutableMap<EntityNameBase, EntityNameBase> NAME_TO_SERVICE_MAP =
        ImmutableMap.<EntityNameBase, EntityNameBase>builder()
            .put(GeniusItm.VALUE, GeniusItm.VALUE)
            .put(GeniusIfm.VALUE, GeniusIfm.VALUE)
            .put(GeniusItmTep.VALUE, GeniusItm.VALUE)
            .put(GeniusItmTz.VALUE, GeniusItm.VALUE)
            .put(GeniusIfmInterface.VALUE, GeniusIfm.VALUE)
            .put(NetvirtVpn.VALUE, NetvirtVpn.VALUE)
            .put(NetvirtVpnInstance.VALUE, NetvirtVpn.VALUE)
            .put(NetvirtElan.VALUE, NetvirtElan.VALUE)
            .put(NetvirtElanInterface.VALUE, NetvirtElan.VALUE)
            .put(NetvirtAcl.VALUE, NetvirtAcl.VALUE)
            .put(NetvirtAclInterface.VALUE, NetvirtAcl.VALUE)
            .put(NetvirtAclInstance.VALUE, NetvirtAcl.VALUE)
            .put(NetvirtL2gwConnection.VALUE, NetvirtL2gw.VALUE)
            .put(NetvirtL2gwNode.VALUE, NetvirtL2gw.VALUE)
            .put(NetvirtL2gw.VALUE, NetvirtL2gw.VALUE)
            .put(NetvirtDhcp.VALUE, NetvirtDhcp.VALUE)
            .put(Ofplugin.VALUE, Ofplugin.VALUE)
            .put(NetvirtQos.VALUE, NetvirtQos.VALUE)
            .put(NetvirtQosPolicyInstance.VALUE, NetvirtQos.VALUE)
            .build();


    public static RecoverOutput callSrmOp(DataBroker broker, RecoverInput input) {
        RecoverOutputBuilder outputBuilder = new RecoverOutputBuilder();
        if (input.getEntityName() == null) {
            outputBuilder.setResponse(RpcFailEntityName.VALUE)
                .setMessage("EntityName is null");
            return outputBuilder.build();
        }
        if (input.getEntityType() == null) {
            outputBuilder.setResponse(RpcFailEntityType.VALUE)
                .setMessage(String.format("EntityType for %s can't be null", input.getEntityName()));
            return outputBuilder.build();
        }
        String entityId;
        if (EntityTypeInstance.VALUE.equals(input.getEntityType()) && input.getEntityId() ==  null) {
            outputBuilder.setResponse(RpcFailEntityId.VALUE)
                .setMessage(String.format("EntityId can't be null for %s", input.getEntityName()));
            return outputBuilder.build();
        } else {
            entityId = input.getEntityId();
        }
        EntityNameBase serviceName = NAME_TO_SERVICE_MAP.get(input.getEntityName());
        if (serviceName == null) {
            outputBuilder.setResponse(RpcFailEntityName.VALUE)
                .setMessage(String.format("EntityName %s has no matching service", input.getEntityName()));
            return outputBuilder.build();
        }
        EntityTypeBase entityType = NAME_TO_TYPE_MAP.get(input.getEntityName());
        if (entityType == null || !input.getEntityType().equals(entityType)) {
            outputBuilder.setResponse(RpcFailEntityType.VALUE)
                .setMessage(String.format("EntityName %s doesn't match with EntityType %s",
                    input.getEntityName(), entityType));
            return outputBuilder.build();
        }

        OperationsBuilder opsBuilder = new OperationsBuilder()
            .setEntityName(input.getEntityName())
            .setEntityType(entityType)
            .setTriggerOperation(ServiceOpRecover.VALUE);
        if (entityId != null) {
            opsBuilder.setEntityId(entityId);
        }
        Operations operation = opsBuilder.build();
        InstanceIdentifier<Operations> opsIid = getInstanceIdentifier(operation, serviceName);
        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.mergeParentStructurePut(LogicalDatastoreType.OPERATIONAL, opsIid, operation);
        try {
            tx.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error writing RecoveryOp to datastore. path:{}, data:{}", opsIid, operation);
            outputBuilder.setResponse(RpcFailUnknown.VALUE).setMessage(e.getMessage());
            return outputBuilder.build();
        }
        outputBuilder.setResponse(RpcSuccess.VALUE).setMessage("Recovery operation successfully triggered");
        return outputBuilder.build();
    }

    public static ReinstallOutput callSrmOp(DataBroker broker, ReinstallInput input) {
        ReinstallOutputBuilder outputBuilder = new ReinstallOutputBuilder();
        if (input.getEntityName() == null) {
            outputBuilder.setSuccessful(REINSTALL_FAILED)
                .setMessage("EntityName is null");
            return outputBuilder.build();
        }
        if (input.getEntityType() == null) {
            outputBuilder.setSuccessful(REINSTALL_FAILED)
                .setMessage(String.format("EntityType for %s can't be null", input.getEntityName()));
            return outputBuilder.build();
        }

        if (!EntityTypeService.VALUE.equals(input.getEntityType())) {
            outputBuilder.setSuccessful(REINSTALL_FAILED)
                .setMessage(String.format("EntityType is %s, Reinstall is only for EntityTypeService",
                                          input.getEntityType()));
            return outputBuilder.build();
        }

        EntityNameBase serviceName = NAME_TO_SERVICE_MAP.get(input.getEntityName());
        if (serviceName == null) {
            outputBuilder.setSuccessful(REINSTALL_FAILED)
                .setMessage(String.format("EntityName %s has no matching service", input.getEntityName()));
            return outputBuilder.build();
        }

        EntityTypeBase entityType = NAME_TO_TYPE_MAP.get(input.getEntityName());
        if (entityType == null || !input.getEntityType().equals(entityType)) {
            outputBuilder.setSuccessful(REINSTALL_FAILED)
                .setMessage(String.format("EntityName %s doesn't match with EntityType %s",
                    input.getEntityName(), entityType));
            return outputBuilder.build();
        }

        OperationsBuilder opsBuilder = new OperationsBuilder()
            .setEntityName(input.getEntityName())
            .setEntityType(entityType)
            .setTriggerOperation(ServiceOpReinstall.VALUE);
        Operations operation = opsBuilder.build();
        InstanceIdentifier<Operations> opsIid = getInstanceIdentifier(operation, serviceName);
        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.mergeParentStructurePut(LogicalDatastoreType.OPERATIONAL, opsIid, operation);
        try {
            tx.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error writing RecoveryOp to datastore. path:{}, data:{}", opsIid, operation);
            outputBuilder.setSuccessful(REINSTALL_FAILED).setMessage(e.getMessage());
            return outputBuilder.build();
        }
        outputBuilder.setSuccessful(REINSTALL_SUCCESS).setMessage("Recovery operation successfully triggered");
        return outputBuilder.build();
    }

    private static InstanceIdentifier<Operations> getInstanceIdentifier(
            Operations operation, EntityNameBase serviceName) {
        return InstanceIdentifier.create(ServiceOps.class)
            .child(Services.class, new ServicesKey(serviceName))
            .child(Operations.class, operation.key());
    }
}
