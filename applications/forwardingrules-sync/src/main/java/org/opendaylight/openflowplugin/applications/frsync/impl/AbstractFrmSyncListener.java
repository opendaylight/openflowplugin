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

            try {
                final Optional<ListenableFuture<Boolean>> optFuture = processNodeModification(modification);
                if (optFuture.isPresent()) {
                    final ListenableFuture<Boolean> future = optFuture.get();
                    final Boolean ret = future.get(15000, TimeUnit.MILLISECONDS);
                    LOG.debug("syncup ret {} {} {} thread:{}", dsType(), ret, nodeId.getValue(), threadName());
                }
            } catch (InterruptedException e) {
                LOG.warn("permit for forwarding rules sync not acquired: {}", nodeId.getValue());
            } catch (Exception e) {
                LOG.error("error processing inventory node modification: {}", nodeId.getValue(), e);
            }
        }
    }

    protected abstract Optional<ListenableFuture<Boolean>> processNodeModification(
            DataTreeModification<T> modification) throws InterruptedException;

    protected abstract LogicalDatastoreType dsType();

    private static String threadName() {
        final Thread currentThread = Thread.currentThread();
        return currentThread.getName();
    }
}
