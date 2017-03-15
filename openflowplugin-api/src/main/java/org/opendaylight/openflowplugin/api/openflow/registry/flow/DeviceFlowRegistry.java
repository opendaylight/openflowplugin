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
import java.util.function.BiConsumer;
import org.opendaylight.openflowplugin.api.openflow.registry.CommonDeviceRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;

/**
 * Registry for mapping composite-key of flow ({@link FlowRegistryKey}) from device view
 * to flow descriptor ({@link FlowDescriptor}) as the identifier of the same flow in data store.
 */
public interface DeviceFlowRegistry extends CommonDeviceRegistry<FlowRegistryKey> {

    ListenableFuture<List<Optional<FlowCapableNode>>> fill();

    void storeDescriptor(FlowRegistryKey flowRegistryKey, FlowDescriptor flowDescriptor);

    FlowDescriptor retrieveDescriptor(FlowRegistryKey flowRegistryKey);

    void forEachEntry(BiConsumer<FlowRegistryKey, FlowDescriptor> consumer);

    void lock(FlowRegistryKey flowRegistryKey);

    void unlock(FlowRegistryKey flowRegistryKey);

    boolean hasLock(FlowRegistryKey flowRegistryKey);

}
