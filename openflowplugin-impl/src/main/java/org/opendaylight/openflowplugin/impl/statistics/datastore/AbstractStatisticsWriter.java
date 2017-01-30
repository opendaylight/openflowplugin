/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.datastore;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractStatisticsWriter<T extends DataObject> {

    private final TxFacade txFacade;

    AbstractStatisticsWriter(final TxFacade txFacade) {
        this.txFacade = txFacade;
    }

    /**
     * Creates put operation using provided data in underlying transaction chain.
     * @param path path
     * @param data data
     * @param <O> data type
     */
    protected <O extends DataObject> void writeToTransaction(final InstanceIdentifier<O> path,
                                                             final O data,
                                                             final boolean withParents) {
        if (withParents) {
            txFacade.writeToTransactionWithParentsSlow(LogicalDatastoreType.OPERATIONAL, path, data);
        } else {
            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, path, data);
        }
    }

    /**
     * Submits transaction
     * @return future with boolean result
     */
    protected ListenableFuture<Boolean> submitTransaction() {
        return Futures.immediateFuture(txFacade.submitTransaction());
    }

    /**
     * Write statistics and return future with true or false as result
     * @param statistics statistics
     * @param instanceIdentifier instance identifier
     * @param withParents write missing parents if needed (slower)
     * @return future
     */
    protected abstract ListenableFuture<Boolean> storeStatistics(final List<T> statistics,
                                                                 final InstanceIdentifier<Node> instanceIdentifier,
                                                                 final boolean withParents);

}
