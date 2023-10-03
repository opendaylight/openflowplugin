/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WakeupOnNode implements DataTreeChangeListener<Table> {
    private static final Logger LOG = LoggerFactory.getLogger(WakeupOnNode.class);

    private final LearningSwitchHandler learningSwitchHandler;

    public WakeupOnNode(final LearningSwitchHandler learningSwitchHandler) {
        this.learningSwitchHandler = requireNonNull(learningSwitchHandler);
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<Table>> modifications) {
        Uint8 requiredTableId = Uint8.ZERO;
        // TODO add flow

        for (var modification : modifications) {
            if (modification.getRootNode().getModificationType() == ModificationType.SUBTREE_MODIFIED) {
                var table = modification.getRootNode().getDataAfter();
                if (table != null) {
                    LOG.trace("table: {}", table);
                    if (requiredTableId.equals(table.getId())) {
                        learningSwitchHandler.onSwitchAppeared(modification.getRootPath().getRootIdentifier());
                    }
                }
            }
        }
    }
}
