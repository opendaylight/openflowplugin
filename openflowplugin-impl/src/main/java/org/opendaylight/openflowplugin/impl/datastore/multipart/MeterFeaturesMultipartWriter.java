/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.datastore.multipart;

import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.node.meter.features.MeterFeatures;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

public class MeterFeaturesMultipartWriter extends AbstractMultipartWriter<MeterFeatures> {
    public MeterFeaturesMultipartWriter(final TxFacade txFacade, final WithKey<Node, NodeKey> instanceIdentifier) {
        super(txFacade, instanceIdentifier);
    }

    @Override
    protected Class<MeterFeatures> getType() {
        return MeterFeatures.class;
    }

    @Override
    public void storeStatistics(final MeterFeatures statistics, final boolean withParents) {
        writeToTransaction(getInstanceIdentifier().toBuilder().augmentation(NodeMeterFeatures.class).build(),
            new NodeMeterFeaturesBuilder()
                .setMeterFeatures(statistics)
                .build(),
            withParents);
    }
}
