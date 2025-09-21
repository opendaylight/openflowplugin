/*
 * Copyright (c) 2015 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.openflowplugin.applications.frm.FlowCapableNodeConnectorCommitter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;

public abstract class AbstractNodeConnectorCommitter<T extends DataObject>
        implements FlowCapableNodeConnectorCommitter<T> {

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<T>> changes) {
        requireNonNull(changes, "Changes may not be null!");

        for (DataTreeModification<T> change : changes) {
            final var key = change.path();
            final var mod = change.getRootNode();
            final var nodeConnIdent = key.trimTo(FlowCapableNodeConnector.class);

            if (preConfigurationCheck(nodeConnIdent)) {
                switch (mod.modificationType()) {
                    case DELETE:
                        remove(key, mod.dataBefore(), nodeConnIdent);
                        break;
                    case SUBTREE_MODIFIED:
                        update(key, mod.dataBefore(), mod.dataAfter(), nodeConnIdent);
                        break;
                    case WRITE:
                        final var dataBefore = mod.dataBefore();
                        if (dataBefore == null) {
                            add(key, mod.dataAfter(), nodeConnIdent);
                        } else {
                            update(key, dataBefore, mod.dataAfter(), nodeConnIdent);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled modification type " + mod.modificationType());
                }
            }
        }
    }

    /**
     * Method return wildCardPath for Listener registration and for identify the
     * correct KeyInstanceIdentifier from data.
     */
    protected abstract DataObjectReference<T> getWildCardPath();

    private static boolean preConfigurationCheck(final DataObjectIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        requireNonNull(nodeConnIdent, "FlowCapableNodeConnector ident can not be null!");
        return true;
    }
}
