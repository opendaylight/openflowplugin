/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.datastore;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractStatisticsWriter<T extends DataContainer> {

    private final TxFacade txFacade;
    private final InstanceIdentifier<Node> instanceIdentifier;

    AbstractStatisticsWriter(final TxFacade txFacade, final InstanceIdentifier<Node> instanceIdentifier) {
        this.txFacade = txFacade;
        this.instanceIdentifier = instanceIdentifier;
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
     * Get instance identifier
     * @return instance identifier
     */
    protected InstanceIdentifier<Node> getInstanceIdentifier() {
        return instanceIdentifier;
    }

    /**
     * Write statistics
     * @param statistics statistics
     * @param withParents write missing parents if needed (slower)
     */
    public void write(final DataContainer statistics, final boolean withParents) {
        if (getType().isInstance(statistics)) {
            storeStatistics(getType().cast(statistics), withParents);
        }
    }

    /**
     * Get type of writer
     * @return type of writer
     */
    protected abstract Class<T> getType();

    /**
     * Write statistics
     * @param statistics statistics
     * @param withParents write missing parents if needed (slower)
     */
    protected abstract void storeStatistics(final T statistics,
                                            final boolean withParents);


}
