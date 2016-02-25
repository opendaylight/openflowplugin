/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import javax.annotation.CheckForNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.impl.services.FlowCapableTransactionServiceImpl;
import org.opendaylight.openflowplugin.impl.services.NodeConfigServiceImpl;
import org.opendaylight.openflowplugin.impl.services.PacketProcessingServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalEchoServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalFlowServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalGroupServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalMeterServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalPortServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalTableServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowStatisticsServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SalEchoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;

public class MdSalRegistratorUtils {

    private MdSalRegistratorUtils() {
        throw new IllegalStateException();
    }

    /**
     * Method registers all OF services for role {@link OfpRole#BECOMEMASTER}
     * @param rpcContext - registration processing is implemented in {@link RpcContext}
     * @param deviceContext - every service needs {@link DeviceContext} as input parameter
     * @param newRole - role validation for {@link OfpRole#BECOMEMASTER}
     */
    public static void registerMasterServices(@CheckForNull final RpcContext rpcContext,
            @CheckForNull final DeviceContext deviceContext, @CheckForNull final OfpRole newRole) {
        Preconditions.checkArgument(rpcContext != null);
        Preconditions.checkArgument(deviceContext != null);
        Preconditions.checkArgument(newRole != null);
        Verify.verify(OfpRole.BECOMEMASTER.equals(newRole), "Service call with bad Role {} we expect role BECOMEMASTER", newRole);

        rpcContext.registerRpcServiceImplementation(SalEchoService.class, new SalEchoServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(SalFlowService.class, new SalFlowServiceImpl(rpcContext, deviceContext));
        //TODO: add constructors with rcpContext and deviceContext to meter, group, table constructors
        rpcContext.registerRpcServiceImplementation(FlowCapableTransactionService.class, new FlowCapableTransactionServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(SalMeterService.class, new SalMeterServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(SalGroupService.class, new SalGroupServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(SalTableService.class, new SalTableServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(SalPortService.class, new SalPortServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(PacketProcessingService.class, new PacketProcessingServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(NodeConfigService.class, new NodeConfigServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(OpendaylightFlowStatisticsService.class, new OpendaylightFlowStatisticsServiceImpl(rpcContext, deviceContext));
    }

    /**
     * Method unregisters all services in first step. So we don't need to call {@link MdSalRegistratorUtils#unregisterServices(RpcContext)}
     * directly before by change role from {@link OfpRole#BECOMEMASTER} to {@link OfpRole#BECOMESLAVE}.
     * Method registers {@link SalEchoService} in next step only because we would like to have SalEchoService as local service for all apps
     * to be able actively check connection status for slave connection too.
     * @param rpcContext - registration/unregistration processing is implemented in {@link RpcContext}
     * @param newRole - role validation for {@link OfpRole#BECOMESLAVE}
     */
    public static void registerSlaveServices(@CheckForNull final RpcContext rpcContext, @CheckForNull final OfpRole newRole) {
        Preconditions.checkArgument(rpcContext != null);
        Preconditions.checkArgument(newRole != null);
        Verify.verify(OfpRole.BECOMESLAVE.equals(newRole), "Service call with bad Role {} we expect role BECOMESLAVE", newRole);
        
        unregisterServices(rpcContext);
    }

    /**
     * Method unregisters all OF services.
     * @param rpcContext - unregistration processing is implemented in {@link RpcContext}
     */
    public static void unregisterServices(@CheckForNull final RpcContext rpcContext) {
        Preconditions.checkArgument(rpcContext != null);

        rpcContext.unregisterRpcServiceImplementation(SalEchoService.class);
        rpcContext.unregisterRpcServiceImplementation(SalFlowService.class);
        //TODO: add constructors with rcpContext and deviceContext to meter, group, table constructors
        rpcContext.unregisterRpcServiceImplementation(FlowCapableTransactionService.class);
        rpcContext.unregisterRpcServiceImplementation(SalMeterService.class);
        rpcContext.unregisterRpcServiceImplementation(SalGroupService.class);
        rpcContext.unregisterRpcServiceImplementation(SalTableService.class);
        rpcContext.unregisterRpcServiceImplementation(SalPortService.class);
        rpcContext.unregisterRpcServiceImplementation(PacketProcessingService.class);
        rpcContext.unregisterRpcServiceImplementation(NodeConfigService.class);
        rpcContext.unregisterRpcServiceImplementation(OpendaylightFlowStatisticsService.class);
    }
}
