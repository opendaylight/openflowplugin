/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

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
        Preconditions.checkNotNull(nodeIdent, "FlowCapableNode ident can not be null!");
        return provider.isNodeActive(nodeIdent);
    }
}

