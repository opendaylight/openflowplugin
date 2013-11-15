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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
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

    private RoutedRpcRegistration<SalMeterService> meterRegistration;

    private RoutedRpcRegistration<PacketProcessingService> packetRegistration;

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

        //meterRegistration = ctx.addRoutedRpcImplementation(SalMeterService.class, this);
        //meterRegistration.registerPath(NodeContext.class, getIdentifier());
        //builder.add(meterRegistration);

        //groupRegistration = ctx.addRoutedRpcImplementation(SalGroupService.class, this);
        //groupRegistration.registerPath(NodeContext.class, getIdentifier());
        //builder.add(groupRegistration);

        //packetRegistration = ctx.addRoutedRpcImplementation(PacketProcessingService.class, this);
        //packetRegistration.registerPath(NodeContext.class, getIdentifier());
        //builder.add(packetRegistration);

        return builder.toInstance();
    }

    /**
     * @return session context 
     */
    public SessionContext getSessionContext() {
        return sessionContext;
    }

}
