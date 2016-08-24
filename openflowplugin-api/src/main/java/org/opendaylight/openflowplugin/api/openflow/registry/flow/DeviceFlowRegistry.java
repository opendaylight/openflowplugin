/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.registry.flow;

import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;

/**
 * Registry for mapping composite-key of flow ({@link FlowRegistryKey}) from device view
 * to flow descriptor ({@link FlowDescriptor}) as the identifier of the same flow in data store.
 */
public interface DeviceFlowRegistry extends AutoCloseable {

    FlowDescriptor retrieveIdForFlow(FlowRegistryKey flowRegistryKey);

    void store(FlowRegistryKey flowRegistryKey, FlowDescriptor flowDescriptor);

    FlowId storeIfNecessary(FlowRegistryKey flowRegistryKey, short tableId);

    void removeDescriptor(FlowRegistryKey flowRegistryKey);

    Map<FlowRegistryKey, FlowDescriptor> getAllFlowDescriptors();

    @Override
    void close();
}
