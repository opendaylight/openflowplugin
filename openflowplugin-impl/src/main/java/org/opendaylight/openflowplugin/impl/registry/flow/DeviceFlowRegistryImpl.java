/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowHash;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public class DeviceFlowRegistryImpl implements DeviceFlowRegistry {

    private final Map<FlowHash, FlowDescriptor> flowRegistry = new HashMap<>();
    private final List<FlowHash> marks = new ArrayList();
    private final Logger LOG = LoggerFactory.getLogger(DeviceFlowRegistryImpl.class);

    @Override
    public FlowDescriptor retrieveIdForFlow(final FlowHash flowHash) throws FlowRegistryException {
        if (flowRegistry.containsKey(flowHash)) {
            return flowRegistry.get(flowHash);
        }
        throw new FlowRegistryException("Flow hash not registered.");
    }


    @Override
    public void store(final FlowHash flowHash, final FlowDescriptor flowDescriptor) {
        LOG.trace("Storing flowDescriptor with table ID : {} and flow ID : {} for flow hash : {}", flowDescriptor.getTableKey().getId(), flowDescriptor.getFlowId().getValue(), flowHash.hashCode());
        flowRegistry.put(flowHash, flowDescriptor);
    }

    @Override
    public void markToBeremoved(final FlowHash flowHash) {
        marks.add(flowHash);
        LOG.trace("Flow hash {} was marked for removal.", flowHash.hashCode());

    }

    @Override
    public void removeMarked() {
        for (FlowHash flowHash : marks) {
            LOG.trace("Removing flowDescriptor for flow hash : {}", flowHash.hashCode());
            flowRegistry.remove(flowHash);
        }
        marks.clear();
    }


    @Override
    public Map<FlowHash, FlowDescriptor> getAllFlowDescriptors() {
        return ImmutableMap.copyOf(flowRegistry);
    }

}
