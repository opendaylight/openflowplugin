/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.core.rpc;

import org.opendaylight.openflowplugin.api.openflow.md.core.device.RequestContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;

/**
 * This context is registered with MD-SAL as a routed RPC provider for the inventory node backed by
 * this switch and tracks the state of any user requests and how they map onto protocol requests. It
 * uses {@link org.opendaylight.openflowplugin.api.openflow.md.core.device.RequestContext} to perform
 * requests.
 * <p/>
 * Created by Martin Bobak <mbobak@cisco.com> on 25.2.2015.
 */
public interface RpcContext extends
        SalGroupService,
        SalFlowService,
        SalMeterService,
        SalTableService,
        SalPortService,
        PacketProcessingService,
        Identifiable<InstanceIdentifier<Node>> {


    <S extends RpcService> void registerRpcServiceImplementation(Class<S> serviceClass, S serviceInstance);

    void setRequestContext(RequestContext requestContext);

    void requestFailed(Long xid, Throwable cause);

    void requestSuccessful(Long xid);

}
