/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.southboundcli.util.OFNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = DpnTracker.class, immediate = true)
public final class DefaultDpnTracker implements DpnTracker, DataTreeChangeListener<FlowCapableNode>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDpnTracker.class);
    public static final String DEFAULT_DPN_NAME = "UNKNOWN";
    public static final String SEPARATOR = ":";

    private final Map<Long, String> dpnIdToNameCache = new HashMap<>();
    private final Registration listenerReg;

    @Inject
    @Activate
    public DefaultDpnTracker(@Reference final DataBroker dataBroker) {
        listenerReg = dataBroker.registerTreeChangeListener(LogicalDatastoreType.OPERATIONAL,
            DataObjectReference.builder(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class).build(),
            this);
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        listenerReg.close();
    }

    @Override
    public synchronized List<OFNode> currentNodes() {
        final var dpnList = new ArrayList<OFNode>();
        for (var entry : dpnIdToNameCache.entrySet()) {
            final var dpn = new OFNode(entry.getKey(), entry.getValue());
            dpnList.add(dpn);
            LOG.trace("Added OFNode: {} to the list", dpn.getNodeId());
        }
        dpnList.sort(null);
        return dpnList;
    }

    @Override
    public synchronized void onDataTreeChanged(final List<DataTreeModification<FlowCapableNode>> changes) {
        for (var change : changes) {
            final var mod = change.getRootNode();
            switch (mod.modificationType()) {
                case DELETE:
                    remove(change.path(), mod.dataBefore());
                    break;
                case SUBTREE_MODIFIED:
                    update(change.path(), mod.dataBefore(), mod.dataAfter());
                    break;
                case WRITE:
                    final var dataBefore = mod.dataBefore();
                    if (dataBefore == null) {
                        add(change.path(), mod.dataAfter());
                    } else {
                        update(change.path(), dataBefore, mod.dataAfter());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type " + mod.modificationType());
            }
        }
    }

    private void remove(final DataObjectIdentifier<FlowCapableNode> instId, final FlowCapableNode delNode) {
        LOG.trace("Received remove notification for {}", delNode);
        final var nodeId = instId.getFirstKeyOf(Node.class).getId().getValue();
        final var node = nodeId.split(SEPARATOR);
        if (node.length < 2) {
            LOG.error("Failed to remove Unexpected nodeId {}", nodeId);
        } else {
            dpnIdToNameCache.remove(Long.parseLong(node[1]));
        }
    }

    private void update(final DataObjectIdentifier<FlowCapableNode> instId,
            final FlowCapableNode dataObjectModificationBefore, final FlowCapableNode dataObjectModificationAfter) {
        LOG.trace("Received update notification {}", instId);
        final var nodeId = instId.getFirstKeyOf(Node.class).getId().getValue();
        final var node = nodeId.split(SEPARATOR);
        if (node.length < 2) {
            LOG.error("Failed to add Unexpected nodeId {}", nodeId);
            return;
        }
        long dpnId = Long.parseLong(node[1]);
        String nodeName = dataObjectModificationAfter == null ? null : dataObjectModificationAfter.getDescription();
        if (nodeName != null) {
            dpnIdToNameCache.put(dpnId, nodeName);
        } else {
            dpnIdToNameCache.put(dpnId, DEFAULT_DPN_NAME);
        }
    }

    private void add(final DataObjectIdentifier<FlowCapableNode> instId, final FlowCapableNode addNode) {
        LOG.trace("Received ADD notification for {}", instId);
        final var nodeId = instId.getFirstKeyOf(Node.class).getId().getValue();
        final var node = nodeId.split(SEPARATOR);
        if (node.length < 2) {
            LOG.error("Failed to add Unexpected nodeId {}", nodeId);
            return;
        }
        long dpnId = Long.parseLong(node[1]);
        String dpnName = addNode == null ? null : addNode.getDescription();
        LOG.trace("Adding DPNID {} to cache", dpnId);
        if (dpnName == null) {
            dpnName = DEFAULT_DPN_NAME;
        }
        dpnIdToNameCache.put(dpnId, dpnName);
    }
}