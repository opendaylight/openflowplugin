/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Optional;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * AbstractChangeListner implemented basic {@link AsyncDataChangeEvent} processing for
 * flow node subDataObject (flows, groups and meters).
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 */
public abstract class AbstractListeningCommiter <T extends DataObject> implements ForwardingRulesCommiter<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractListeningCommiter.class);

    protected ForwardingRulesManager provider;

    protected final Class<T> clazz;

    public AbstractListeningCommiter (ForwardingRulesManager provider, Class<T> clazz) {
        this.provider = Preconditions.checkNotNull(provider, "ForwardingRulesManager can not be null!");
        this.clazz = Preconditions.checkNotNull(clazz, "Class can not be null!");
    }

    /*
    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changeEvent) {
        Preconditions.checkNotNull(changeEvent,"Async ChangeEvent can not be null!");
        Map<InstanceIdentifier<?>, DataObject> createdData = changeEvent.getCreatedData();
        Map<InstanceIdentifier<?>, DataObject> updatedData = changeEvent.getUpdatedData();
        Set<InstanceIdentifier<?>> removedData = changeEvent.getRemovedPaths();
        Map<InstanceIdentifier<?>, DataObject> originalData = changeEvent.getOriginalData();


        if(createdData != null && !createdData.isEmpty()) {
            Set<InstanceIdentifier<?>> createdDataKeys = createdData.keySet();
            for (InstanceIdentifier instanceId : createdDataKeys) {
                InstanceIdentifier<FlowCapableNode> nodeInstanceId = 
                    instanceId.firstIdentifierOf(FlowCapableNode.class);
                if(nodeInstanceId != null &&
                		getWildCardPath().getTargetType().isAssignableFrom(createdData.get(instanceId).getClass()) &&
                				preConfigurationCheck(nodeInstanceId)) {
                    add((InstanceIdentifier<T>)instanceId,(T)createdData.get(instanceId), nodeInstanceId);
                }
            }
            return;
        }

        if(updatedData != null && !updatedData.isEmpty()) {
            Set<InstanceIdentifier<?>> updatedDataKeys = updatedData.keySet();
            for (InstanceIdentifier instanceId : updatedDataKeys) {
                InstanceIdentifier<FlowCapableNode> nodeInstanceId = 
                    instanceId.firstIdentifierOf(FlowCapableNode.class);
                if(nodeInstanceId != null &&
                		getWildCardPath().getTargetType().isAssignableFrom(createdData.get(instanceId).getClass()) &&
                		getWildCardPath().getTargetType().isAssignableFrom(originalData.get(instanceId).getClass()) &&
                				preConfigurationCheck(nodeInstanceId)) {
                    update((InstanceIdentifier<T>)instanceId, (T)originalData.get(instanceId),
                        (T)createdData.get(instanceId), nodeInstanceId);
                }
            }
            return;
        }

        if(removedData != null && !removedData.isEmpty()) {
            for (InstanceIdentifier instanceId : removedData) {
                InstanceIdentifier<FlowCapableNode> nodeInstanceId = 
                    instanceId.firstIdentifierOf(FlowCapableNode.class);
                if(nodeInstanceId != null &&
                		getWildCardPath().getTargetType().isAssignableFrom(originalData.get(instanceId).getClass()) &&
                				preConfigurationCheck(nodeInstanceId)) {
                    remove ((InstanceIdentifier<T>)instanceId, (T)originalData.get(instanceId), nodeInstanceId);
                }
            }
            return;
        }
    }
*/
    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<T>> changes) {
        Preconditions.checkNotNull(changes, "Changes may not be null!");

        for (DataTreeModification<T> change : changes) {
            final InstanceIdentifier<T> key = change.getRootPath().getRootIdentifier();
            final DataObjectModification<T> mod = change.getRootNode();
            final InstanceIdentifier<FlowCapableNode> nodeIdent =
                    key.firstIdentifierOf(FlowCapableNode.class);

            if (preConfigurationCheck(nodeIdent)) {
                switch (mod.getModificationType()) {
                case DELETE:
                    remove(key, mod.getDataBefore(), nodeIdent);
                    break;
                case SUBTREE_MODIFIED:
                    update(key, mod.getDataBefore(), mod.getDataAfter(), nodeIdent);
                    break;
                case WRITE:
                    if (mod.getDataBefore() == null) {
                        add(key, mod.getDataAfter(), nodeIdent);
                    } else {
                        update(key, mod.getDataBefore(), mod.getDataAfter(), nodeIdent);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
                }
            }
            else{
                if (provider.getConfiguration().isStaleMarkingEnabled()) {
                    LOG.info("Stale-Marking ENABLED and switch {} is NOT connected, storing stale entities",
                            nodeIdent.toString());
                    // Switch is NOT connected
                    switch (mod.getModificationType()) {
                        case DELETE:
                            createStaleMarkEntity(key, mod.getDataBefore(), nodeIdent);
                            break;
                        case SUBTREE_MODIFIED:
                            break;
                        case WRITE:
                            break;
                        default:
                            throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
                    }
                }
            }
        }
    }

    /**
     * Method return wildCardPath for Listener registration
     * and for identify the correct KeyInstanceIdentifier from data;
     */
    protected abstract InstanceIdentifier<T> getWildCardPath();

    private boolean preConfigurationCheck(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        Preconditions.checkNotNull(nodeIdent, "nodeIdent can not be null!");
        EntityOwnershipService ownershipService = provider.getOwnershipService();
        if(ownershipService == null) {
            LOG.debug("preConfigCheck: entityOwnershipService is null - assuming ownership");
            return true;
        }

        InstanceIdentifier<Node> nodeInstanceId = nodeIdent.firstIdentifierOf(Node.class);
        NodeId nodeId = InstanceIdentifier.keyOf(nodeInstanceId).getId();
        final Entity entity = new Entity("openflow", nodeId.getValue());
        Optional<EntityOwnershipState> entityOwnershipStateOptional = ownershipService.getOwnershipState(entity);
        if(!entityOwnershipStateOptional.isPresent()) { //abset - assume this ofp is owning entity
            LOG.debug("preConfigCheck: entity state of " + nodeId.getValue() + " is absent - assuming ownership");
            return provider.isNodeActive(nodeIdent);
        }
        final EntityOwnershipState entityOwnershipState = entityOwnershipStateOptional.get();
        if(!(entityOwnershipState.hasOwner() && entityOwnershipState.isOwner())) {
            LOG.debug("preConfigCheck: not owner of " + nodeId.getValue() + " - skipping configuration");
            return false;
        }
        return provider.isNodeActive(nodeIdent);
    }
}

