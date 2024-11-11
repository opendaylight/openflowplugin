/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.api.NotificationService.CompositeListener;
import org.opendaylight.mdsal.binding.api.NotificationService.CompositeListener.Component;
import org.opendaylight.mdsal.binding.api.NotificationService.Listener;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.common.txchain.TransactionChainManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemoved;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowCapableTopologyExporter {
    private static final Logger LOG = LoggerFactory.getLogger(FlowCapableTopologyExporter.class);

    private final WithKey<Topology, TopologyKey> iiToTopology;
    private final OperationProcessor processor;

    FlowCapableTopologyExporter(final OperationProcessor processor, final WithKey<Topology, TopologyKey> topology) {
        this.processor = requireNonNull(processor);
        iiToTopology = requireNonNull(topology);
    }

    CompositeListener toListener() {
        return new CompositeListener(Set.of(
            new Component(LinkDiscovered.class, (Listener<LinkDiscovered>) this::onLinkDiscovered),
            new Component(LinkRemoved.class, (Listener<LinkRemoved>) this::onLinkRemoved)));
    }

    @VisibleForTesting
    void onLinkDiscovered(final LinkDiscovered notification) {
        processor.enqueueOperation(new OnLinkDiscovered(notification));
    }

    @VisibleForTesting
    void onLinkRemoved(final LinkRemoved notification) {
        processor.enqueueOperation(new OnLinkRemoved(notification));
    }

    private abstract static class AbstractLinkOperation implements TopologyOperation {
        private final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.Link link;

        AbstractLinkOperation(
                final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.Link link) {
            this.link = requireNonNull(link);
        }

        @Override
        public final void applyOperation(final TransactionChainManager manager) {
            applyOperation(manager, FlowCapableNodeMapping.toTopologyLink(link));
        }

        abstract void applyOperation(TransactionChainManager manager, Link link);
    }

    private final class OnLinkDiscovered extends AbstractLinkOperation {
        OnLinkDiscovered(final LinkDiscovered notification) {
            super(notification);
        }

        @Override
        public void applyOperation(final TransactionChainManager manager, final Link link) {
            manager.mergeToTransaction(LogicalDatastoreType.OPERATIONAL,
                TopologyManagerUtil.linkPath(link, iiToTopology), link, true);
        }

        @Override
        public String toString() {
            return "onLinkDiscovered";
        }
    }

    private final class OnLinkRemoved extends AbstractLinkOperation {
        OnLinkRemoved(final LinkRemoved notification) {
            super(notification);
        }

        @Override
        public void applyOperation(final TransactionChainManager manager, final Link link) {
            final var linkPath = TopologyManagerUtil.linkPath(link, iiToTopology);

            Optional<Link> linkOptional = Optional.empty();
            try {
                // read that checks if link exists (if we do not do this we might get an exception on delete)
                linkOptional = manager.readFromTransaction(LogicalDatastoreType.OPERATIONAL, linkPath).get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Error occurred when trying to read Link: {}", e.getMessage());
                LOG.debug("Error occurred when trying to read Link.. ", e);
            }
            if (linkOptional.isPresent()) {
                manager.addDeleteOperationToTxChain(LogicalDatastoreType.OPERATIONAL, linkPath);
            }
        }

        @Override
        public String toString() {
            return "onLinkRemoved";
        }
    }
}
