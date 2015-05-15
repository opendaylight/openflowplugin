/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public class DeviceFlowRegistryImpl implements DeviceFlowRegistry {

    private final Map<FlowHash, FlowDescriptor> flowRegistry = new HashMap<>();
    private final Collection<FlowHash> marks = new HashSet<>();
    private static final Logger LOG = LoggerFactory.getLogger(DeviceFlowRegistryImpl.class);

    @Override
    public FlowDescriptor retrieveIdForFlow(final FlowHash flowHash) {
        FlowDescriptor flowDescriptor = flowRegistry.get(flowHash);
        return flowDescriptor;
    }


    @Override
    public void store(final FlowHash flowHash, final FlowDescriptor flowDescriptor) {
        LOG.trace("Storing flowDescriptor with table ID : {} and flow ID : {} for flow hash : {}", flowDescriptor.getTableKey().getId(), flowDescriptor.getFlowId().getValue(), flowHash.hashCode());
        synchronized (flowRegistry) {
            flowRegistry.put(flowHash, flowDescriptor);
        }
    }

    @Override
    public FlowId storeIfNecessary(final FlowHash flowHash, final short tableId) {
        FlowId alienFlowId = FlowUtil.createAlienFlowId(tableId);
        FlowDescriptor alienFlowDescriptor = FlowDescriptorFactory.create(tableId, alienFlowId);
        synchronized (flowRegistry) {
            FlowDescriptor flowDescriptorFromRegistry = flowRegistry.get(flowHash);
            if (flowDescriptorFromRegistry != null) {
                return flowDescriptorFromRegistry.getFlowId();
            } else {
                LOG.trace("Flow descriptor for flow hash {} wasn't found.", flowHash.hashCode());
                flowRegistry.put(flowHash, alienFlowDescriptor);
                return alienFlowId;
            }
        }
    }
    
    @Override
    public void markToBeremoved(final FlowHash flowHash) {
        synchronized (marks) {
            marks.add(flowHash);
        }
        LOG.trace("Flow hash {} was marked for removal.", flowHash.hashCode());

    }

    @Override
    public void removeMarked() {
        synchronized (flowRegistry) {
            for (FlowHash flowHash : marks) {
                LOG.trace("Removing flowDescriptor for flow hash : {}", flowHash.hashCode());
                flowRegistry.remove(flowHash);
            }
        }
        synchronized (marks) {
            marks.clear();
        }
    }


    @Override
    public Map<FlowHash, FlowDescriptor> getAllFlowDescriptors() {
        return flowRegistry;
    }

    @Override
    public void close() throws Exception {
        flowRegistry.clear();
        marks.clear();
    }
}
