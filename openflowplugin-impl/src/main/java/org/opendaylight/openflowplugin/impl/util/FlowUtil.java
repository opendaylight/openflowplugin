/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public final class FlowUtil {

    private static final String ALIEN_SYSTEM_FLOW_ID = "#UF$TABLE*";
    private static final AtomicInteger unaccountedFlowsCounter = new AtomicInteger(0);
    private static final Logger LOG = LoggerFactory.getLogger(FlowUtil.class);


    private FlowUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    public static FlowId createAlienFlowId(final short tableId) {
        final StringBuilder sBuilder = new StringBuilder(ALIEN_SYSTEM_FLOW_ID)
                .append(tableId).append('-').append(unaccountedFlowsCounter.incrementAndGet());
        String alienId =  sBuilder.toString();
        return new FlowId(alienId);

    }

    public static void preloadConfiguredFlows(final DeviceFlowRegistry deviceFlowRegistry, final DataBroker dataBrokerArg,
                                              final DeviceState deviceState, final long DS_READ_TIMEOUT) {
        final ReadOnlyTransaction rTx = dataBrokerArg.newReadOnlyTransaction();
        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> nodeReading = rTx.read(LogicalDatastoreType.CONFIGURATION,
                deviceState.getNodeInstanceIdentifier().augmentation(FlowCapableNode.class));

        try {
            final Optional<FlowCapableNode> flowCapableNodeOptional = nodeReading.get(DS_READ_TIMEOUT, TimeUnit.SECONDS);
            if (flowCapableNodeOptional.isPresent()) {
                final FlowCapableNode flowCapableNode = flowCapableNodeOptional.get();
                final List<Table> tableList = flowCapableNode.getTable();
                if (!tableList.isEmpty()) {
                    for (Table table : tableList) {
                        final List<Flow> flowList = table.getFlow();
                        if (flowList != null) {
                            for (Flow flow : flowList) {
                                final FlowId flowId = flow.getId();
                                final FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(flow);
                                final FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(table.getId(), flowId);
                                deviceFlowRegistry.store(flowRegistryKey, flowDescriptor);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("failed to read configured flows for device: {}", deviceState.getNodeId());
        } finally {
            rTx.close();
        }
    }
}
