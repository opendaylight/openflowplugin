/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.applications.frsync.impl.FlowForwarder;
import org.opendaylight.openflowplugin.applications.frsync.impl.GroupForwarder;
import org.opendaylight.openflowplugin.applications.frsync.impl.MeterForwarder;
import org.opendaylight.openflowplugin.applications.frsync.impl.TableForwarder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * device synchronization API
 */
public interface SyncReactor {
    /**
     * @param flowcapableNodePath path to openflow augmentation of node
     * @param configTree          configured node
     * @param operationalTree     device reflection
     * @return synchronization outcome
     */
    ListenableFuture<RpcResult<Void>> syncup(InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                             FlowCapableNode configTree, FlowCapableNode operationalTree) throws InterruptedException;

    void setFlowForwarder(FlowForwarder flowForwarder);

    void setTableForwarder(TableForwarder tableForwarder);

    void setMeterForwarder(MeterForwarder meterForwarder);

    void setGroupForwarder(GroupForwarder groupForwarder);

    void setTransactionService(FlowCapableTransactionService transactionService);
}
