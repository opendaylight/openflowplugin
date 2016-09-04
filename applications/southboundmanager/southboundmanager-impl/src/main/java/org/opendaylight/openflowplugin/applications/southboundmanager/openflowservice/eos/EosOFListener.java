/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.eos;

import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.IPriorityTaskManager;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.task.EntityLifecycleState;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.task.EntityState;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.transactions.api.TransactionTracker;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.transactions.api.TransactionTrackerFactory;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.util.SouthboundManagerUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EosOFListener implements EntityOwnershipListener {

    private TransactionTrackerFactory txnTrackerFactory;
    private final IPriorityTaskManager resyncTaskManager;
    private final Logger LOG = LoggerFactory.getLogger(EosOFListener.class);


    public EosOFListener(TransactionTrackerFactory txnTrackerFactory, IPriorityTaskManager resyncTaskManager) {
        this.txnTrackerFactory = txnTrackerFactory;
        this.resyncTaskManager = resyncTaskManager;
    }

    @Override
    public void ownershipChanged(EntityOwnershipChange entityOwnershipChange) {
        NodeId nodeId = SouthboundManagerUtil.getNodeId(entityOwnershipChange.
                getEntity().getId());
        TransactionTracker txTracker = txnTrackerFactory.getCacheEntry(nodeId);
        boolean isOwner = entityOwnershipChange.isOwner();
        boolean hasOwner = entityOwnershipChange.hasOwner();

        if (hasOwner) {
            if (isOwner) {
                if (txTracker != null) {
                    LOG.debug("Cache entry already exists for nodeId={}. " +
                            "Updating flags: isOwner={}, hasOwner={}", nodeId, isOwner, hasOwner);
                    txTracker.setIsOwner(isOwner);
                    txTracker.setHasOwner(hasOwner);
                } else {
                    LOG.debug("Adding cache entry for nodeId={}. " +
                            "Updating flags: isOwner={}, hasOwner={}", nodeId, isOwner, hasOwner);
                    txnTrackerFactory.addCacheEntry(nodeId, isOwner, hasOwner);
                }
                // For case where current-node becomes owner of given switch
                /*
                  #### IMPORTANT NOTE #####
                  Below triggering will work only when OF-HA is NOT used.
                  If OF-HA is used, we cannot depend upon ONLY OF-HA ownership changes
                  since master-change for a switch does NOT imply switch-connected/disconnected
                  events
                */
                triggerNodeAssociation(SouthboundManagerUtil
                        .getNodeIdAsString(entityOwnershipChange.getEntity().getId()));
            } else {
                if (txTracker != null) {
                    LOG.debug("Cache entry already exists for nodeId={}. Cleaning up ... " +
                            "Updating flags: isOwner={}, hasOwner={}", nodeId, isOwner, hasOwner);
                    txTracker.invalidateAll();
                    txTracker.setIsOwner(isOwner);
                    txTracker.setHasOwner(hasOwner);
                } else {
                    LOG.debug("Adding cache entry for nodeId={}. " +
                            "Updating flags: isOwner={}, hasOwner={}", nodeId, isOwner, hasOwner);
                    txnTrackerFactory.addCacheEntry(nodeId, isOwner, hasOwner);
                }
                // For case where current-node lost ownership for given switch
                /*
                  #### IMPORTANT NOTE #####
                  Below triggering will work only when OF-HA is NOT used.
                  If OF-HA is used, we cannot depend upon ONLY OF-HA ownership changes
                  since master-change for a switch does NOT imply switch-connected/disconnected
                  events
                */
                triggerNodeDissociation(SouthboundManagerUtil
                        .getNodeIdAsString(entityOwnershipChange.getEntity().getId()));
            }
        } else {
            txnTrackerFactory.removeCacheEntry(nodeId);
            LOG.error("EOS raised unexpected event: NodeId={}, isOwner={}, hasOwner={}", isOwner, hasOwner, nodeId);
            // For weird case where no node is owner of given switch
                /*
                  #### IMPORTANT NOTE #####
                  Below triggering will work only when OF-HA is NOT used.
                  If OF-HA is used, we cannot depend upon ONLY OF-HA ownership changes
                  since master-change for a switch does NOT imply switch-connected/disconnected
                  events
                */
            triggerNodeDissociation(SouthboundManagerUtil
                    .getNodeIdAsString(entityOwnershipChange.getEntity().getId()));
        }
    }

    private void triggerNodeAssociation(String nodeId){
        if (Boolean.getBoolean(SouthboundManagerUtil.TRIGGER_RESYNC_VIA_EOS)) {
            LOG.info("Triggering NodeAssociation event for nodeId {} to start Resync tasks", nodeId);
            EntityState entityState = new EntityState(nodeId, EntityLifecycleState.ASSOCIATED);
            resyncTaskManager.sendEntityState(entityState, SouthboundManagerUtil.PRIORITY_TASK_ACTION_TYPE);
        }
    }

    private void triggerNodeDissociation(String nodeId){
        if (Boolean.getBoolean(SouthboundManagerUtil.TRIGGER_RESYNC_VIA_EOS)) {
            LOG.info("Triggering NodeDissociation event for nodeId {} to cancel any ongoing Resync tasks", nodeId);
            EntityState entityState = new EntityState(nodeId, EntityLifecycleState.DISSOCIATED);
            resyncTaskManager.sendEntityState(entityState, SouthboundManagerUtil.PRIORITY_TASK_ACTION_TYPE);
        }
    }
}
