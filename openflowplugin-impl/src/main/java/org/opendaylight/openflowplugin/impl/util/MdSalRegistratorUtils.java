/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.impl.services.NodeConfigServiceImpl;
import org.opendaylight.openflowplugin.impl.services.PacketProcessingServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalFlowServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalGroupServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalMeterServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalTableServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;

/**
 * @author joe
 */
public class MdSalRegistratorUtils {

    private MdSalRegistratorUtils() {
        throw new IllegalStateException();
    }

    public static void registerServices(final RpcContext rpcContext, final DeviceContext deviceContext) {
        rpcContext.registerRpcServiceImplementation(SalFlowService.class, new SalFlowServiceImpl(rpcContext, deviceContext));
        //TODO: add constructors with rcpContext and deviceContext to meter, group, table constructors
        rpcContext.registerRpcServiceImplementation(SalMeterService.class, new SalMeterServiceImpl());
        rpcContext.registerRpcServiceImplementation(SalGroupService.class, new SalGroupServiceImpl());
        rpcContext.registerRpcServiceImplementation(SalTableService.class, new SalTableServiceImpl());
        rpcContext.registerRpcServiceImplementation(PacketProcessingService.class, new PacketProcessingServiceImpl(rpcContext, deviceContext));
        rpcContext.registerRpcServiceImplementation(NodeConfigService.class, new NodeConfigServiceImpl());
    }
}
