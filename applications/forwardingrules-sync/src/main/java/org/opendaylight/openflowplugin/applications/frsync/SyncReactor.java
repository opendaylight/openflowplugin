/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Device synchronization API.
 */
public interface SyncReactor {
    /**
     * @param flowcapableNodePath path to openflow augmentation of node
     * @param configTree configured node
     * @param operationalTree device reflection
     * @param dsType type of DS change
     * @return synchronization outcome
     */
    ListenableFuture<Boolean> syncup(InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                     FlowCapableNode configTree, FlowCapableNode operationalTree,
                                     LogicalDatastoreType dsType) throws InterruptedException;

}
