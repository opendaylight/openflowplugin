/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.openflowplugin.common.txchain.TransactionChainManager;

/**
 * Internal interface for submitted operations. Implementations of this
 * interface are enqueued and batched into data store transactions.
 */
interface TopologyOperation {
    /**
     * Execute the operation on top of the transaction.
     *
     * @param manager Datastore transaction manager
     */
    void applyOperation(TransactionChainManager manager);
}