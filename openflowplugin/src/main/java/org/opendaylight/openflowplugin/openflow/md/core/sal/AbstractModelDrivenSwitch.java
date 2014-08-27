/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.openflowplugin.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.OpendaylightGroupStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.OpendaylightMeterStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.OpendaylightQueueStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration.CompositeObjectRegistrationBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * RPC abstract for MD-switch
 */
public abstract class AbstractModelDrivenSwitch implements ModelDrivenSwitch {

    private final InstanceIdentifier<Node> identifier;

    private RoutedRpcRegistration<SalFlowService> flowRegistration;

    private RoutedRpcRegistration<SalGroupService> groupRegistration;
    
    private RoutedRpcRegistration<SalTableService> tableRegistration;

    private RoutedRpcRegistration<SalMeterService> meterRegistration;
    
    private RoutedRpcRegistration<SalPortService> portRegistration;

    private RoutedRpcRegistration<PacketProcessingService> packetRegistration;

    private RoutedRpcRegistration<OpendaylightFlowStatisticsService> flowStatisticsRegistration;

    private RoutedRpcRegistration<OpendaylightGroupStatisticsService> groupStatisticsRegistration;

    private RoutedRpcRegistration<OpendaylightMeterStatisticsService> meterStatisticsRegistration;

    private RoutedRpcRegistration<OpendaylightPortStatisticsService> portStatisticsRegistration;

    private RoutedRpcRegistration<OpendaylightFlowTableStatisticsService> flowTableStatisticsRegistration;

    private RoutedRpcRegistration<OpendaylightQueueStatisticsService> queueStatisticsRegistration;

    protected final SessionContext sessionContext;

    protected AbstractModelDrivenSwitch(InstanceIdentifier<Node> identifier,SessionContext conductor) {
        this.identifier = identifier;
        this.sessionContext = conductor;
    }

    @Override
    public final InstanceIdentifier<Node> getIdentifier() {
        return this.identifier;
    }

    @Override
    public CompositeObjectRegistration<ModelDrivenSwitch> register(ProviderContext ctx) {
        CompositeObjectRegistrationBuilder<ModelDrivenSwitch> builder = CompositeObjectRegistration
                .<ModelDrivenSwitch> builderFor(this);

        flowRegistration = ctx.addRoutedRpcImplementation(SalFlowService.class, this);
        flowRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(flowRegistration);
        
        portRegistration = ctx.addRoutedRpcImplementation(SalPortService.class, this);
        portRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(portRegistration);

        meterRegistration = ctx.addRoutedRpcImplementation(SalMeterService.class, this);
        meterRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(meterRegistration);

        groupRegistration = ctx.addRoutedRpcImplementation(SalGroupService.class, this);
        groupRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(groupRegistration);
        
        tableRegistration = ctx.addRoutedRpcImplementation(SalTableService.class, this);
        tableRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(tableRegistration);

        packetRegistration = ctx.addRoutedRpcImplementation(PacketProcessingService.class, this);
        packetRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(packetRegistration);

        flowStatisticsRegistration = ctx.addRoutedRpcImplementation(OpendaylightFlowStatisticsService.class, this);
        flowStatisticsRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(flowStatisticsRegistration);

        groupStatisticsRegistration = ctx.addRoutedRpcImplementation(OpendaylightGroupStatisticsService.class, this);
        groupStatisticsRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(groupStatisticsRegistration);

        meterStatisticsRegistration = ctx.addRoutedRpcImplementation(OpendaylightMeterStatisticsService.class, this);
        meterStatisticsRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(meterStatisticsRegistration);

        portStatisticsRegistration = ctx.addRoutedRpcImplementation(OpendaylightPortStatisticsService.class, this);
        portStatisticsRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(portStatisticsRegistration);

        flowTableStatisticsRegistration = ctx.addRoutedRpcImplementation(OpendaylightFlowTableStatisticsService.class, this);
        flowTableStatisticsRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(flowTableStatisticsRegistration);
        
        queueStatisticsRegistration = ctx.addRoutedRpcImplementation(OpendaylightQueueStatisticsService.class, this);
        queueStatisticsRegistration.registerPath(NodeContext.class, getIdentifier());
        builder.add(queueStatisticsRegistration);

        return builder.toInstance();
    }

    /**
     * @return session context 
     */
    public SessionContext getSessionContext() {
        return sessionContext;
    }

}
