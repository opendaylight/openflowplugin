/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.registry.flow;


import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;

import java.util.Map;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public interface DeviceFlowRegistry extends AutoCloseable {

    FlowDescriptor retrieveIdForFlow(FlowHash flowHash);

    void store(FlowHash flowHash, FlowDescriptor flowDescriptor);

    FlowId storeIfNecessary(FlowHash flowHash, short tableId);

    void markToBeremoved(FlowHash flowHash);

    void removeMarked();

    Map<FlowHash, FlowDescriptor> getAllFlowDescriptors();
}
