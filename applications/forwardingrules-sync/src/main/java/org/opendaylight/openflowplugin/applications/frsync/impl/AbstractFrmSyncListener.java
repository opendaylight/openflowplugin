/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.NodeListener;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Listener for node changes.
 */
public abstract class AbstractFrmSyncListener<T extends DataObject> implements NodeListener<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractFrmSyncListener.class);

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<T>> modifications) {
        for (DataTreeModification<T> modification : modifications) {
            final NodeId nodeId = PathUtil.digNodeId(modification.getRootPath().path());
            if (LOG.isTraceEnabled()) {
                LOG.trace("DataTreeModification of {} in {} datastore", nodeId.getValue(), dsType());
            }
            processNodeModification(modification).ifPresent(future -> {
                try {
                    future.get(15000, TimeUnit.MILLISECONDS);
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Syncup for {} return from {} listener", nodeId.getValue(), dsType());
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    LOG.error("Error processing inventory node modification: {}", nodeId.getValue(), e);
                }
            });
        }
    }

    protected abstract Optional<ListenableFuture<Boolean>> processNodeModification(
            DataTreeModification<T> modification);

    protected abstract LogicalDatastoreType dsType();
}
