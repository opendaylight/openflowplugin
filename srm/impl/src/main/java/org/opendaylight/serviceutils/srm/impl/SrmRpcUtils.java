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
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev170711.ServiceOps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev170711.service.ops.Services;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev170711.service.ops.ServicesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev170711.service.ops.services.Operations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev170711.service.ops.services.OperationsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.RecoverInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.RecoverOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.RecoverOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.ReinstallInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.ReinstallOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.ReinstallOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.RpcFailEntityId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.RpcFailEntityName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.RpcFailEntityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.RpcFailUnknown;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpcs.rev170711.RpcSuccess;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.EntityNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.EntityTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.EntityTypeInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.EntityTypeService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.GeniusIfm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.GeniusIfmInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.GeniusItm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.GeniusItmTep;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.GeniusItmTz;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtAclInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtAclInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtDhcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtElan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtElanInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtL2gw;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtL2gwConnection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtL2gwNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtQos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtQosPolicyInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtVpn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.NetvirtVpnInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.Ofplugin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.ServiceOpRecover;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev170711.ServiceOpReinstall;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Utility class for SRM Shell.
 */
public final class SrmRpcUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SrmRpcUtils.class);

    private static final boolean REINSTALL_FAILED = false;
    private static final boolean REINSTALL_SUCCESS = true;
    private static final boolean CREATE_MISSING_PARENT = true;

    private SrmRpcUtils() {
    }

    private static final ImmutableMap<Class<? extends EntityNameBase>,Class<? extends EntityTypeBase>>
        NAME_TO_TYPE_MAP = new ImmutableMap
        .Builder<Class<? extends EntityNameBase>,Class<? extends EntityTypeBase>>()
            .put(GeniusItm.class, EntityTypeService.class)
            .put(GeniusIfm.class, EntityTypeService.class)
            .put(NetvirtVpn.class, EntityTypeService.class)
            .put(NetvirtElan.class, EntityTypeService.class)
            .put(NetvirtL2gw.class, EntityTypeService.class)
            .put(NetvirtDhcp.class, EntityTypeService.class)
            .put(NetvirtAcl.class, EntityTypeService.class)
            .put(Ofplugin.class, EntityTypeService.class)
            .put(GeniusItmTep.class, EntityTypeInstance.class)
            .put(GeniusItmTz.class, EntityTypeInstance.class)
            .put(GeniusIfmInterface.class, EntityTypeInstance.class)
            .put(NetvirtVpnInstance.class, EntityTypeInstance.class)
            .put(NetvirtElanInterface.class, EntityTypeInstance.class)
            .put(NetvirtL2gwConnection.class, EntityTypeInstance.class)
            .put(NetvirtL2gwNode.class, EntityTypeInstance.class)
            .put(NetvirtQos.class, EntityTypeService.class)
            .put(NetvirtQosPolicyInstance.class, EntityTypeInstance.class)
            .put(NetvirtAclInterface.class, EntityTypeInstance.class)
            .put(NetvirtAclInstance.class, EntityTypeInstance.class)
            .build();

    private static final ImmutableMap<Class<? extends EntityNameBase>, Class<? extends EntityNameBase>>
        NAME_TO_SERVICE_MAP = new ImmutableMap
        .Builder<Class<? extends EntityNameBase>, Class<? extends EntityNameBase>>()
            .put(GeniusItm.class, GeniusItm.class)
            .put(GeniusIfm.class, GeniusIfm.class)
            .put(GeniusItmTep.class, GeniusItm.class)
            .put(GeniusItmTz.class, GeniusItm.class)
            .put(GeniusIfmInterface.class, GeniusIfm.class)
            .put(NetvirtVpn.class, NetvirtVpn.class)
            .put(NetvirtVpnInstance.class, NetvirtVpn.class)
            .put(NetvirtElan.class, NetvirtElan.class)
            .put(NetvirtElanInterface.class, NetvirtElan.class)
            .put(NetvirtAcl.class, NetvirtAcl.class)
            .put(NetvirtAclInterface.class, NetvirtAcl.class)
            .put(NetvirtAclInstance.class, NetvirtAcl.class)
            .put(NetvirtL2gwConnection.class, NetvirtL2gw.class)
            .put(NetvirtL2gwNode.class, NetvirtL2gw.class)
            .put(NetvirtL2gw.class, NetvirtL2gw.class)
            .put(NetvirtDhcp.class, NetvirtDhcp.class)
            .put(Ofplugin.class, Ofplugin.class)
            .put(NetvirtQos.class, NetvirtQos.class)
            .put(NetvirtQosPolicyInstance.class, NetvirtQos.class)
            .build();


    public static RecoverOutput callSrmOp(DataBroker broker, RecoverInput input) {
        RecoverOutputBuilder outputBuilder = new RecoverOutputBuilder();
        if (input.getEntityName() == null) {
            outputBuilder.setResponse(RpcFailEntityName.class)
                .setMessage("EntityName is null");
            return outputBuilder.build();
        }
        if (input.getEntityType() == null) {
            outputBuilder.setResponse(RpcFailEntityType.class)
                .setMessage(String.format("EntityType for %s can't be null", input.getEntityName().getSimpleName()));
            return outputBuilder.build();
        }
        String entityId;
        if (EntityTypeInstance.class.equals(input.getEntityType()) && input.getEntityId() ==  null) {
            outputBuilder.setResponse(RpcFailEntityId.class)
                .setMessage(String.format("EntityId can't be null for %s", input.getEntityName().getSimpleName()));
            return outputBuilder.build();
        } else {
            entityId = input.getEntityId();
        }
        Class<? extends EntityNameBase> serviceName = NAME_TO_SERVICE_MAP.get(input.getEntityName());
        if (serviceName == null) {
            outputBuilder.setResponse(RpcFailEntityName.class)
                .setMessage(String.format("EntityName %s has no matching service",
                    input.getEntityName().getSimpleName()));
            return outputBuilder.build();
        }
        Class<? extends EntityTypeBase> entityType = NAME_TO_TYPE_MAP.get(input.getEntityName());
        if (entityType == null || !input.getEntityType().equals(entityType)) {
            outputBuilder.setResponse(RpcFailEntityType.class)
                .setMessage(String.format("EntityName %s doesn't match with EntityType %s",
                    input.getEntityName().getSimpleName(), entityType));
            return outputBuilder.build();
        }

        OperationsBuilder opsBuilder = new OperationsBuilder()
            .setEntityName(input.getEntityName())
            .setEntityType(entityType)
            .setTriggerOperation(ServiceOpRecover.class);
        if (entityId != null) {
            opsBuilder.setEntityId(entityId);
        }
        Operations operation = opsBuilder.build();
        InstanceIdentifier<Operations> opsIid = getInstanceIdentifier(operation, serviceName);
        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, opsIid, operation, CREATE_MISSING_PARENT);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error writing RecoveryOp to datastore. path:{}, data:{}", opsIid, operation);
            outputBuilder.setResponse(RpcFailUnknown.class).setMessage(e.getMessage());
            return outputBuilder.build();
        }
        outputBuilder.setResponse(RpcSuccess.class).setMessage("Recovery operation successfully triggered");
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
                .setMessage(String.format("EntityType for %s can't be null", input.getEntityName().getSimpleName()));
            return outputBuilder.build();
        }

        if (!EntityTypeService.class.equals(input.getEntityType())) {
            outputBuilder.setSuccessful(REINSTALL_FAILED)
                .setMessage(String.format("EntityType is %s, Reinstall is only for EntityTypeService",
                                          input.getEntityType()));
            return outputBuilder.build();
        }

        Class<? extends EntityNameBase> serviceName = NAME_TO_SERVICE_MAP.get(input.getEntityName());
        if (serviceName == null) {
            outputBuilder.setSuccessful(REINSTALL_FAILED)
                .setMessage(String.format("EntityName %s has no matching service",
                    input.getEntityName().getSimpleName()));
            return outputBuilder.build();
        }

        Class<? extends EntityTypeBase> entityType = NAME_TO_TYPE_MAP.get(input.getEntityName());
        if (entityType == null || !input.getEntityType().equals(entityType)) {
            outputBuilder.setSuccessful(REINSTALL_FAILED)
                .setMessage(String.format("EntityName %s doesn't match with EntityType %s",
                    input.getEntityName().getSimpleName(), entityType));
            return outputBuilder.build();
        }

        OperationsBuilder opsBuilder = new OperationsBuilder()
            .setEntityName(input.getEntityName())
            .setEntityType(entityType)
            .setTriggerOperation(ServiceOpReinstall.class);
        Operations operation = opsBuilder.build();
        InstanceIdentifier<Operations> opsIid = getInstanceIdentifier(operation, serviceName);
        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, opsIid, operation, CREATE_MISSING_PARENT);
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error writing RecoveryOp to datastore. path:{}, data:{}", opsIid, operation);
            outputBuilder.setSuccessful(REINSTALL_FAILED).setMessage(e.getMessage());
            return outputBuilder.build();
        }
        outputBuilder.setSuccessful(REINSTALL_SUCCESS).setMessage("Recovery operation successfully triggered");
        return outputBuilder.build();
    }

    private static InstanceIdentifier<Operations> getInstanceIdentifier(
            Operations operation, Class<? extends EntityNameBase> serviceName) {
        return InstanceIdentifier.create(ServiceOps.class)
            .child(Services.class, new ServicesKey(serviceName))
            .child(Operations.class, operation.key());
    }
}
