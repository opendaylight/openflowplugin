/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import static org.opendaylight.openflowplugin.applications.topology.manager.FlowCapableNodeMapping.toTopologyLink;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.common.txchain.TransactionChainManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.FlowTopologyDiscoveryListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkOverutilized;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkUtilizationNormal;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowCapableTopologyExporter implements FlowTopologyDiscoveryListener {

    private static final Logger LOG = LoggerFactory.getLogger(FlowCapableTopologyExporter.class);
    private final InstanceIdentifier<Topology> iiToTopology;
    private final OperationProcessor processor;

    FlowCapableTopologyExporter(final OperationProcessor processor,
            final InstanceIdentifier<Topology> topology) {
        this.processor = Preconditions.checkNotNull(processor);
        this.iiToTopology = Preconditions.checkNotNull(topology);
    }

    @Override
    public void onLinkDiscovered(final LinkDiscovered notification) {
        processor.enqueueOperation(new TopologyOperation() {
            @Override
            public void applyOperation(final TransactionChainManager manager) {
                final Link link = toTopologyLink(notification);
                final InstanceIdentifier<Link> path = TopologyManagerUtil.linkPath(link, iiToTopology);
                manager.mergeToTransaction(LogicalDatastoreType.OPERATIONAL, path, link, true);
            }

            @Override
            public String toString() {
                return "onLinkDiscovered";
            }
        });
    }

    @Override
    public void onLinkOverutilized(final LinkOverutilized notification) {
        // NOOP
    }

    @Override
    public void onLinkRemoved(final LinkRemoved notification) {
        processor.enqueueOperation(new TopologyOperation() {
            @Override
            public void applyOperation(final TransactionChainManager manager) {
                Optional<Link> linkOptional = Optional.absent();
                try {
                    // read that checks if link exists (if we do not do this we might get an exception on delete)
                    linkOptional = manager.readFromTransaction(LogicalDatastoreType.OPERATIONAL,
                            TopologyManagerUtil.linkPath(toTopologyLink(notification), iiToTopology)).checkedGet();
                } catch (ReadFailedException e) {
                    LOG.warn("Error occurred when trying to read Link: {}", e.getMessage());
                    LOG.debug("Error occurred when trying to read Link.. ", e);
                }
                if (linkOptional.isPresent()) {
                    manager.addDeleteOperationToTxChain(LogicalDatastoreType.OPERATIONAL,
                            TopologyManagerUtil.linkPath(toTopologyLink(notification), iiToTopology));
                }
            }

            @Override
            public String toString() {
                return "onLinkRemoved";
            }
        });
    }

    @Override
    public void onLinkUtilizationNormal(final LinkUtilizationNormal notification) {
        // NOOP
    }

}
