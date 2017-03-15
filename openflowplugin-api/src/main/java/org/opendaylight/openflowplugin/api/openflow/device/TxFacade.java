/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Handles operations with transactions.
 */
public interface TxFacade {

    /**
     * Method creates put operation using provided data in underlying transaction chain.
     */
    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                   final InstanceIdentifier<T> path,
                                                   final T data);

    /**
     * Method creates put operation using provided data in underlying transaction
     * chain and flag to create missing parents.
     * WARNING: This method is slow because of additional reading cost.
     * Use it only if you really need to create parents.
     */
    <T extends DataObject> void writeToTransactionWithParentsSlow(final LogicalDatastoreType store,
                                                                  final InstanceIdentifier<T> path,
                                                                  final T data);

    /**
     * Method creates delete operation for provided path in underlying transaction chain.
     */
    <T extends DataObject> void addDeleteToTxChain(final LogicalDatastoreType store,
                                                   final InstanceIdentifier<T> path);

    /**
     * Method submits Transaction to DataStore.
     * @return transaction is submitted successfully
     */
    boolean submitTransaction();

    /**
     * Method exposes transaction created for device
     * represented by this context. This read only transaction has a fresh dataStore snapshot.
     * There is a possibility to get different data set from  DataStore
     * as write transaction in this context.
     * @return readOnlyTransaction - Don't forget to close it after finish reading
     */
    ReadOnlyTransaction getReadTransaction();

    /**
     * Method returns true if transaction chain manager is enabled
     * @return is transaction chain manager enabled
     */
    boolean isTransactionsEnabled();
}
