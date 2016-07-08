/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.registry.flow;


import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public interface DeviceFlowRegistry extends AutoCloseable {

    ListenableFuture<List<Optional<FlowCapableNode>>> fill(KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier);

    FlowDescriptor retrieveIdForFlow(FlowRegistryKey flowRegistryKey);

    void store(FlowRegistryKey flowRegistryKey, FlowDescriptor flowDescriptor);

    FlowId storeIfNecessary(FlowRegistryKey flowRegistryKey);

    void markToBeremoved(FlowRegistryKey flowRegistryKey);

    void removeMarked();

    Map<FlowRegistryKey, FlowDescriptor> getAllFlowDescriptors();

    @Override
    void close();
}
