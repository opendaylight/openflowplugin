/**
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractChangeListner implemented basic {@link org.opendaylight.controller.md.sal.binding.api.DataTreeModification}
 * processing for flow node subDataObject (flows, groups and meters).
 */
public abstract class AbstractListeningCommiter<T extends DataObject> implements ForwardingRulesCommiter<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractListeningCommiter.class);
    private final AutoCloseable registration;
    private final ForwardingRulesManager provider;

    public AbstractListeningCommiter(ForwardingRulesManager provider, DataBroker dataBroker) {
        final DataTreeIdentifier<T> treeId = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION,
                getWildCardPath());

        this.provider = provider;
        this.registration = dataBroker.registerDataTreeChangeListener(treeId, this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull final Collection<DataTreeModification<T>> changes) {
        LOG.trace("Received data changes :{}", changes);

        for (DataTreeModification<T> change : changes) {
            final InstanceIdentifier<T> key = change.getRootPath().getRootIdentifier();
            final DataObjectModification<T> mod = change.getRootNode();
            final InstanceIdentifier<FlowCapableNode> nodeIdent = key.firstIdentifierOf(FlowCapableNode.class);

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
                        throwInvalidException(change);
                }
            } else if (provider.isStaleMarkingEnabled()) {
                LOG.info("Stale-Marking ENABLED and switch {} is NOT connected, storing stale entities", nodeIdent);

                switch (mod.getModificationType()) {
                    case DELETE:
                        createStaleMarkEntity(key, mod.getDataBefore(), nodeIdent);
                        break;
                    case SUBTREE_MODIFIED:
                        break;
                    case WRITE:
                        break;
                    default:
                        throwInvalidException(change);
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        registration.close();
    }

    /**
     * Method return wildCardPath for Listener registration
     * and for identify the correct KeyInstanceIdentifier from data.
     */
    protected abstract InstanceIdentifier<T> getWildCardPath();

    /**
     * Method returns forwarding rules manager for child classes.
     * @return forwarding rules manager
     */
    protected ForwardingRulesManager getProvider() {
        return provider;
    }

    protected boolean preConfigurationCheck(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return Objects.nonNull(nodeIdent) &&
                provider.isNodeOwner(nodeIdent) &&
                provider.checkNodeInOperationalDataStore(nodeIdent);
    }

    private void throwInvalidException(final DataTreeModification<T> modification) {
        throw new IllegalArgumentException("Unhandled modification type " + modification
                .getRootNode()
                .getModificationType());
    }
}