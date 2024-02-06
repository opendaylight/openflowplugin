/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.registry.flow;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupStatus;
import org.opendaylight.openflowplugin.api.openflow.registry.CommonDeviceRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Registry for mapping composite-key of flow ({@link FlowRegistryKey}) from device view
 * to flow descriptor ({@link FlowDescriptor}) as the identifier of the same flow in data store.
 */
public interface DeviceFlowRegistry extends CommonDeviceRegistry<FlowRegistryKey> {

    @NonNull FlowRegistryKey createKey(@NonNull Flow flow);

    ListenableFuture<List<Optional<FlowCapableNode>>> fill();

    void storeDescriptor(@NonNull FlowRegistryKey flowRegistryKey, @NonNull FlowDescriptor flowDescriptor);

    @Nullable FlowDescriptor retrieveDescriptor(@NonNull FlowRegistryKey flowRegistryKey);

    void clearFlowRegistry();

    @Beta
    void appendHistoryFlow(@NonNull FlowId id, Uint8 tableId, @NonNull FlowGroupStatus status);
}
