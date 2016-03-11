/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.applications.frsync.NodeListener;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Abstract Listener for node changes
 * @author joslezak
 */
public abstract class AbstractFrmSyncListener implements NodeListener {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractFrmSyncListener.class);

    @Override
    public void onDataTreeChanged(@Nonnull final Collection<DataTreeModification<FlowCapableNode>> collection) {
        for (DataTreeModification<FlowCapableNode> modification : collection) {
            final NodeId nodeId = PathUtil.digNodeId(modification.getRootPath().getRootIdentifier());

            try {
                processNodeModification(modification);
            } catch (InterruptedException e) {
                LOG.warn("permit for forwarding rules sync not acquired: {}", nodeId);
            } catch (Exception e) {
                LOG.error("error processing inventory node modification: {}", nodeId, e);
            }
        }
    }

    protected abstract Optional<ListenableFuture<RpcResult<Void>>> processNodeModification(
            DataTreeModification<FlowCapableNode> modification) throws ReadFailedException, InterruptedException;

    public abstract LogicalDatastoreType dsType();
}
