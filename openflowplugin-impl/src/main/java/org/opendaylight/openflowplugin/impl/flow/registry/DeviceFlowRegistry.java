/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.flow.registry;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.openflowplugin.api.openflow.flow.registry.FlowHash;
import org.opendaylight.openflowplugin.api.openflow.flow.registry.FlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.flow.registry.FlowRegistryException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;


/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public class DeviceFlowRegistry implements FlowRegistry {

    private static final Map<FlowHash, FlowId> flowRegistry = new HashMap<>();
    private static final FlowHashFactory FLOW_HASH_FACTORY = new FlowHashFactory();


    @Override
    public FlowId retrieveIdForFlow(final Flow flow) throws FlowRegistryException {
        FlowHash hash = FLOW_HASH_FACTORY.create(flow);
        if (flowRegistry.containsKey(hash)) {
            return flowRegistry.get(hash);
        }
        throw new FlowRegistryException("Flow hash not registered.");
    }

    @Override
    public void store(final Flow flow) {
        FlowHash hash = FLOW_HASH_FACTORY.create(flow);
        flowRegistry.put(hash, flow.getId());
    }

    @Override
    public void remove(final Flow flow) {
        FlowHash flowHash = FLOW_HASH_FACTORY.create(flow);
        flowRegistry.remove(flowHash);

    }

}
