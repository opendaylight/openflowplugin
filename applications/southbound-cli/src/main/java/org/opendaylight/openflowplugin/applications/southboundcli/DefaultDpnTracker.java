/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.southboundcli.util.OFNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = DpnTracker.class)
public final class DefaultDpnTracker
        implements DpnTracker, ClusteredDataTreeChangeListener<FlowCapableNode>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDpnTracker.class);
    public static final String DEFAULT_DPN_NAME = "UNKNOWN";
    public static final String SEPARATOR = ":";

    private final Map<Long, String> dpnIdToNameCache = new HashMap<>();
    private final ListenerRegistration<?> listenerReg;

    @Inject
    @Activate
    public DefaultDpnTracker(@Reference final DataBroker dataBroker) {
        listenerReg = dataBroker.registerDataTreeChangeListener(
            DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class)), this);
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
    public synchronized void onDataTreeChanged(final Collection<DataTreeModification<FlowCapableNode>> changes) {
        for (var change : changes) {
            final var key = change.getRootPath().getRootIdentifier();
            final var mod = change.getRootNode();
            final var nodeIdent = key.firstIdentifierOf(FlowCapableNode.class);
            switch (mod.getModificationType()) {
                case DELETE:
                    remove(nodeIdent, mod.getDataBefore());
                    break;
                case SUBTREE_MODIFIED:
                    update(nodeIdent, mod.getDataBefore(), mod.getDataAfter());
                    break;
                case WRITE:
                    if (mod.getDataBefore() == null) {
                        add(nodeIdent, mod.getDataAfter());
                    } else {
                        update(nodeIdent, mod.getDataBefore(), mod.getDataAfter());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
            }
        }
    }

    private void remove(final InstanceIdentifier<FlowCapableNode> instId, final FlowCapableNode delNode) {
        LOG.trace("Received remove notification for {}", delNode);
        String[] node = instId.firstKeyOf(Node.class).getId().getValue().split(SEPARATOR);
        if (node.length < 2) {
            LOG.error("Failed to remove Unexpected nodeId {}", instId.firstKeyOf(Node.class).getId()
                    .getValue());
            return;
        }
        dpnIdToNameCache.remove(Long.parseLong(node[1]));
    }

    private void update(final InstanceIdentifier<FlowCapableNode> instId,
            final FlowCapableNode dataObjectModificationBefore, final FlowCapableNode dataObjectModificationAfter) {

        LOG.trace("Received update notification {}", instId);
        String[] node = instId.firstKeyOf(Node.class).getId().getValue().split(SEPARATOR);
        if (node.length < 2) {
            LOG.error("Failed to add Unexpected nodeId {}", instId.firstKeyOf(Node.class).getId().getValue());
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

    private void add(final InstanceIdentifier<FlowCapableNode> instId, final FlowCapableNode addNode) {
        LOG.trace("Received ADD notification for {}", instId);
        String[] node = instId.firstKeyOf(Node.class).getId().getValue().split(SEPARATOR);
        if (node.length < 2) {
            LOG.error("Failed to add Unexpected nodeId {}", instId.firstKeyOf(Node.class).getId().getValue());
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