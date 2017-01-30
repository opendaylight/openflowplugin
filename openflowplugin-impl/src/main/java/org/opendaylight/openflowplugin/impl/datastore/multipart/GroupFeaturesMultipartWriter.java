/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.datastore.multipart;

import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.features.GroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GroupFeaturesMultipartWriter extends AbstractMultipartWriter<GroupFeatures> {

    public GroupFeaturesMultipartWriter(final TxFacade txFacade, final InstanceIdentifier<Node> instanceIdentifier) {
        super(txFacade, instanceIdentifier);
    }

    @Override
    protected Class<GroupFeatures> getType() {
        return GroupFeatures.class;
    }

    @Override
    public void storeStatistics(final GroupFeatures statistics, final boolean withParents) {
        writeToTransaction(getInstanceIdentifier()
                .augmentation(NodeGroupFeatures.class),
            new NodeGroupFeaturesBuilder()
                .setGroupFeatures(new GroupFeaturesBuilder(statistics)
                    .build())
                .build(),
            withParents);
    }

}
