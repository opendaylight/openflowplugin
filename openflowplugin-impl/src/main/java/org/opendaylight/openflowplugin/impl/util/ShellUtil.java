/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.impl.karaf.Dpn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ShellUtil.class);

    public static List<Dpn> getAllDpns(DataBroker broker) {
        List<Dpn> dpnList = new ArrayList<>();
        List<Node> nodes = null;
        ReadOnlyTransaction tx = broker.newReadOnlyTransaction();
        InstanceIdentifier<Nodes> path = InstanceIdentifier.builder(Nodes.class).build();
        try {
            CheckedFuture<Optional<Nodes>, ReadFailedException> checkedFuture =
                    tx.read(LogicalDatastoreType.OPERATIONAL, path);
            Optional<Nodes> result = checkedFuture.get();
            if (result.isPresent()) {
                nodes = result.get().getNode();
            }
        } catch (Exception e) {
            LOG.error("getAllDpns Error reading nodes from Inventory DS", e);
        }
        if (nodes != null) {
            for (Node node : nodes) {
                try {
                    String[] nodeId = node.getId().getValue().split(":");
                    String name = null;
                    try {
                        name = node.<FlowCapableNode>getAugmentation(FlowCapableNode.class).getDescription();
                    } catch (Exception e) {
                        LOG.error("getAllDpns Error while converting Node: {} to FlowCapableNode",node.getId(), e);
                    }
                    Dpn dpn = new Dpn(Long.parseLong(nodeId[1]), name);
                    if (dpn != null) {
                        LOG.trace("getAllDpn. Added Dpn: {} to the list", dpn.getDpnId());
                        dpnList.add(dpn);
                    }
                } catch (Exception e) {
                    LOG.error("getAllDpns Error while getting Dpn: {}", node.getId(), e);
                }
            }
            Collections.sort(dpnList);
        }
        return dpnList;
    }

}
