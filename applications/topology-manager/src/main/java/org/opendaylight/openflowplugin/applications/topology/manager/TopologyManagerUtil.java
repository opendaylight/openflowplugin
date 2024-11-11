/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.common.txchain.TransactionChainManager;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TopologyManagerUtil {
    private static final Logger LOG = LoggerFactory.getLogger(TopologyManagerUtil.class);

    private TopologyManagerUtil() {
        // Hidden on purpose
    }

    static void removeAffectedLinks(final NodeId id, final TransactionChainManager manager,
                                    final WithKey<Topology, TopologyKey> topology) {
        final Optional<Topology> topologyOptional;
        try {
            topologyOptional = manager.readFromTransaction(LogicalDatastoreType.OPERATIONAL, topology).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Error reading topology data for topology {}: {}", topology, e.getMessage());
            LOG.debug("Error reading topology data for topology.. ", e);
            return;
        }
        if (topologyOptional.isPresent()) {
            for (Link link : topologyOptional.orElseThrow().nonnullLink().values()) {
                if (id.equals(link.getSource().getSourceNode()) || id.equals(link.getDestination().getDestNode())) {
                    manager.addDeleteOperationToTxChain(LogicalDatastoreType.OPERATIONAL, linkPath(link, topology));
                }
            }
        }
    }

    static void removeAffectedLinks(final TpId id, final TransactionChainManager manager,
                                    final WithKey<Topology, TopologyKey> topology) {
        final Optional<Topology> topologyOptional;
        try {
            topologyOptional = manager.readFromTransaction(LogicalDatastoreType.OPERATIONAL, topology).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Error reading topology data for topology {}: {}", topology, e.getMessage());
            LOG.debug("Error reading topology data for topology..", e);
            return;
        }
        if (topologyOptional.isPresent()) {
            for (Link link : topologyOptional.orElseThrow().nonnullLink().values()) {
                if (id.equals(link.getSource().getSourceTp()) || id.equals(link.getDestination().getDestTp())) {
                    manager.addDeleteOperationToTxChain(LogicalDatastoreType.OPERATIONAL, linkPath(link, topology));
                }
            }
        }
    }

    static WithKey<Link, LinkKey> linkPath(final Link link, final WithKey<Topology, TopologyKey> topology) {
        return topology.toBuilder().child(Link.class, link.key()).build();
    }
}
