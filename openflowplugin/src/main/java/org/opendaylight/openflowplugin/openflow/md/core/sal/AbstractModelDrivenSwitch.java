/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collection;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.md.AbstractModelDrivenSwitchRegistration;
import org.opendaylight.openflowplugin.api.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.api.openflow.md.ModelDrivenSwitchRegistration;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.OpendaylightGroupStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.OpendaylightMeterStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.OpendaylightQueueStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * RPC abstract for MD-switch
 */
public abstract class AbstractModelDrivenSwitch implements ModelDrivenSwitch {

    private final InstanceIdentifier<Node> identifier;

    protected final SessionContext sessionContext;

    private boolean isEntityOwner = false;

    protected AbstractModelDrivenSwitch(InstanceIdentifier<Node> identifier,SessionContext conductor) {
        this.identifier = identifier;
        this.sessionContext = conductor;
    }

    @Override
    public final InstanceIdentifier<Node> getIdentifier() {
        return this.identifier;
    }

    @Override
    public ModelDrivenSwitchRegistration register(RpcProviderRegistry rpcProviderRegistry) {
        final Builder<RoutedRpcRegistration<?>> builder = ImmutableList.builder();

        final RoutedRpcRegistration<SalFlowService> flowRegistration = rpcProviderRegistry.addRoutedRpcImplementation(SalFlowService.class, this);
        flowRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(flowRegistration);

        final RoutedRpcRegistration<SalPortService> portRegistration = rpcProviderRegistry.addRoutedRpcImplementation(SalPortService.class, this);
        portRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(portRegistration);

        final RoutedRpcRegistration<SalMeterService> meterRegistration = rpcProviderRegistry.addRoutedRpcImplementation(SalMeterService.class, this);
        meterRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(meterRegistration);

        final RoutedRpcRegistration<SalGroupService> groupRegistration = rpcProviderRegistry.addRoutedRpcImplementation(SalGroupService.class, this);
        groupRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(groupRegistration);

        final RoutedRpcRegistration<SalTableService> tableRegistration = rpcProviderRegistry.addRoutedRpcImplementation(SalTableService.class, this);
        tableRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(tableRegistration);

        final RoutedRpcRegistration<PacketProcessingService> packetRegistration = rpcProviderRegistry.addRoutedRpcImplementation(PacketProcessingService.class, this);
        packetRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(packetRegistration);

        final RoutedRpcRegistration<OpendaylightFlowStatisticsService> flowStatisticsRegistration = rpcProviderRegistry.addRoutedRpcImplementation(OpendaylightFlowStatisticsService.class, this);
        flowStatisticsRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(flowStatisticsRegistration);

        final RoutedRpcRegistration<OpendaylightGroupStatisticsService> groupStatisticsRegistration = rpcProviderRegistry.addRoutedRpcImplementation(OpendaylightGroupStatisticsService.class, this);
        groupStatisticsRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(groupStatisticsRegistration);

        final RoutedRpcRegistration<OpendaylightMeterStatisticsService> meterStatisticsRegistration = rpcProviderRegistry.addRoutedRpcImplementation(OpendaylightMeterStatisticsService.class, this);
        meterStatisticsRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(meterStatisticsRegistration);

        final RoutedRpcRegistration<OpendaylightPortStatisticsService> portStatisticsRegistration = rpcProviderRegistry.addRoutedRpcImplementation(OpendaylightPortStatisticsService.class, this);
        portStatisticsRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(portStatisticsRegistration);

        final RoutedRpcRegistration<NodeConfigService> nodeConfigRegistration = rpcProviderRegistry.addRoutedRpcImplementation(NodeConfigService.class, this);
        nodeConfigRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(nodeConfigRegistration);

        final RoutedRpcRegistration<OpendaylightFlowTableStatisticsService> flowTableStatisticsRegistration = rpcProviderRegistry.addRoutedRpcImplementation(OpendaylightFlowTableStatisticsService.class, this);
        flowTableStatisticsRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(flowTableStatisticsRegistration);

        final RoutedRpcRegistration<OpendaylightQueueStatisticsService> queueStatisticsRegistration = rpcProviderRegistry.addRoutedRpcImplementation(OpendaylightQueueStatisticsService.class, this);
        queueStatisticsRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(queueStatisticsRegistration);

        final Collection<RoutedRpcRegistration<?>> registrations = builder.build();
        return new AbstractModelDrivenSwitchRegistration(this) {
            @Override
            protected void removeRegistration() {
                for (RoutedRpcRegistration<?> r : registrations) {
                    r.close();
                }
            }
        };
    }

    /**
     * @return session context
     */
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    @Override
    public boolean isEntityOwner() {
        return isEntityOwner;
    }

    @Override
    public void setEntityOwnership(boolean isOwner) {
        isEntityOwner = isOwner;
    }
}
