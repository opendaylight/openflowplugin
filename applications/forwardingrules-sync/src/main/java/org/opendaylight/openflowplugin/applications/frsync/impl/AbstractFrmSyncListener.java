/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.NodeListener;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Listener for node changes.
 */
public abstract class AbstractFrmSyncListener<T extends DataObject> implements NodeListener<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFrmSyncListener.class);

    @Override
    public void onDataTreeChanged(@Nonnull final Collection<DataTreeModification<T>> modifications) {
        for (DataTreeModification<T> modification : modifications) {
            final NodeId nodeId = PathUtil.digNodeId(modification.getRootPath().getRootIdentifier());
            if (LOG.isTraceEnabled()) {
                LOG.trace("DataTreeModification of {} in {} datastore", nodeId.getValue(), dsType());
            }
            try {
                final Optional<ListenableFuture<Boolean>> optFuture = processNodeModification(modification);
                if (optFuture.isPresent()) {
                    final ListenableFuture<Boolean> future = optFuture.get();
                    future.get(15000, TimeUnit.MILLISECONDS);
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Syncup for {} return from {} listener", nodeId.getValue(), dsType());
                    }
                }
            } catch (Exception e) {
                LOG.error("Error processing inventory node modification: {}, {}", nodeId.getValue(), e);
            }
        }
    }

    protected abstract Optional<ListenableFuture<Boolean>> processNodeModification(
            final DataTreeModification<T> modification);

    protected abstract LogicalDatastoreType dsType();

}
