/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.datastore.multipart;

import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMultipartWriter<T extends DataContainer> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMultipartWriter.class);

    private final TxFacade txFacade;
    private final WithKey<Node, NodeKey> instanceIdentifier;

    AbstractMultipartWriter(final TxFacade txFacade, final WithKey<Node, NodeKey> instanceIdentifier) {
        this.txFacade = txFacade;
        this.instanceIdentifier = instanceIdentifier;
    }

    /**
     * Creates put operation using provided data in underlying transaction chain.
     *
     * @param path path
     * @param data data
     * @param <O> data type
     */
    protected <O extends DataObject> void writeToTransaction(final DataObjectIdentifier<O> path,
                                                             final O data,
                                                             final boolean withParents) {
        if (withParents) {
            txFacade.writeToTransactionWithParentsSlow(LogicalDatastoreType.OPERATIONAL, path, data);
        } else {
            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, path, data);
        }
    }

    /**
     * Get instance identifier.
     *
     * @return instance identifier
     */
    protected WithKey<Node, NodeKey> getInstanceIdentifier() {
        return instanceIdentifier;
    }

    /**
     * Write dataContainer.
     *
     * @param dataContainer dataContainer
     * @param withParents write missing parents if needed (slower)
     * @return true if we have correct dataContainer type
     */
    public boolean write(final DataContainer dataContainer, final boolean withParents) {
        if (getType().isInstance(dataContainer)) {
            LOG.debug("Writing multipart data of type {} for node {}", getType(), getInstanceIdentifier());
            storeStatistics(getType().cast(dataContainer), withParents);
            return true;
        }

        LOG.debug("Failed to write multipart data of type {} for node {}", getType(), getInstanceIdentifier());
        return false;
    }

    /**
     * Get type of writer.
     *
     * @return type of writer
     */
    protected abstract Class<T> getType();

    /**
     * Write statistics.
     *
     * @param statistics statistics
     * @param withParents write missing parents if needed (slower)
     */
    protected abstract void storeStatistics(T statistics, boolean withParents);
}
