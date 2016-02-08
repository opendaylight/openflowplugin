/**
 * Copyright (c) 2015 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeConnectorInventoryTranslator;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class FlowNodeConnectorInventoryTranslatorImpl extends AbstractNodeConnectorCommitter<FlowCapableNodeConnector> implements FlowNodeConnectorInventoryTranslator {

    private static final Logger LOG = LoggerFactory.getLogger(FlowNodeConnectorInventoryTranslatorImpl.class);

    private ListenerRegistration<FlowNodeConnectorInventoryTranslatorImpl> dataTreeChangeListenerRegistration;

    public static final String SEPARATOR = ":";

    private static final InstanceIdentifier<FlowCapableNodeConnector> II_TO_FLOW_CAPABLE_NODE_CONNECTOR
            = InstanceIdentifier.builder(Nodes.class)
            .child(Node.class)
            .child(NodeConnector.class)
            .augmentation(FlowCapableNodeConnector.class)
            .build();

    private Multimap<Long,String> dpnToPortMultiMap = Multimaps.synchronizedListMultimap(ArrayListMultimap.<Long,String>create());

    public FlowNodeConnectorInventoryTranslatorImpl(final ForwardingRulesManager manager, final DataBroker dataBroker){
        super(manager, FlowCapableNodeConnector.class);
        Preconditions.checkNotNull(dataBroker, "DataBroker can not be null!");

        final DataTreeIdentifier<FlowCapableNodeConnector> treeId =
                new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, getWildCardPath());
        try {
            SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(ForwardingRulesManagerImpl.STARTUP_LOOP_TICK,
                    ForwardingRulesManagerImpl.STARTUP_LOOP_MAX_RETRIES);
            dataTreeChangeListenerRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<FlowNodeConnectorInventoryTranslatorImpl>>() {
                @Override
                public ListenerRegistration<FlowNodeConnectorInventoryTranslatorImpl> call() throws Exception {
                    return dataBroker.registerDataTreeChangeListener(treeId, FlowNodeConnectorInventoryTranslatorImpl.this);
                }
            });
        } catch (final Exception e) {
            LOG.warn(" FlowNodeConnectorInventoryTranslatorImpl listener registration fail!");
            LOG.debug("FlowNodeConnectorInventoryTranslatorImpl DataChange listener registration fail ..", e);
            throw new IllegalStateException("FlowNodeConnectorInventoryTranslatorImpl startup fail! System needs restart.", e);
        }
    }

    @Override
    protected InstanceIdentifier<FlowCapableNodeConnector> getWildCardPath(){
        return InstanceIdentifier.create(Nodes.class)
                .child(Node.class)
                .child(NodeConnector.class)
                .augmentation(FlowCapableNodeConnector.class);
    }

    @Override
    public void close() {
        if (dataTreeChangeListenerRegistration != null) {
            try {
                dataTreeChangeListenerRegistration.close();
            } catch (final Exception e) {
                LOG.warn("Error by stop FRM FlowNodeConnectorInventoryTranslatorImpl: {}", e.getMessage());
                LOG.debug("Error by stop FRM FlowNodeConnectorInventoryTranslatorImpl..", e);
            }
            dataTreeChangeListenerRegistration = null;
        }
    }
    @Override
    public void remove(InstanceIdentifier<FlowCapableNodeConnector> identifier, FlowCapableNodeConnector del, InstanceIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        if(compareInstanceIdentifierTail(identifier,II_TO_FLOW_CAPABLE_NODE_CONNECTOR)){
            LOG.warn("Node Connector removed");
            String sNodeConnectorIdentifier = nodeConnIdent
                    .firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId().getValue();
            long nDpId = getDpIdFromPortName(sNodeConnectorIdentifier);
            String portName = del.getName();

            dpnToPortMultiMap.remove(nDpId, sNodeConnectorIdentifier);
        }
    }

    @Override
    public void update(InstanceIdentifier<FlowCapableNodeConnector> identifier, FlowCapableNodeConnector original, FlowCapableNodeConnector update, InstanceIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        if(compareInstanceIdentifierTail(identifier,II_TO_FLOW_CAPABLE_NODE_CONNECTOR)){
            LOG.warn("Node Connector updated");
            //donot need to do anything as we are not considering updates here
        }
    }

    @Override
    public void add(InstanceIdentifier<FlowCapableNodeConnector> identifier, FlowCapableNodeConnector add, InstanceIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        if(compareInstanceIdentifierTail(identifier,II_TO_FLOW_CAPABLE_NODE_CONNECTOR)){
            LOG.warn("Node Connector added");
            String sNodeConnectorIdentifier = nodeConnIdent
                    .firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId().getValue();
            long nDpId = getDpIdFromPortName(sNodeConnectorIdentifier);

            String portName = add.getName();
            if(!dpnToPortMultiMap.containsEntry(nDpId,sNodeConnectorIdentifier)) {
                dpnToPortMultiMap.put(nDpId, sNodeConnectorIdentifier);
            }else{
                LOG.error("Duplicate Event.Node Connector already added");
            }
        }
    }

    private boolean compareInstanceIdentifierTail(InstanceIdentifier<?> identifier1,
                                  InstanceIdentifier<?> identifier2) {
        return Iterables.getLast(identifier1.getPathArguments()).equals(Iterables.getLast(identifier2.getPathArguments()));
    }

    @Override
    public boolean isNodeConnectorUpdated(long dpId, String portName){
        return dpnToPortMultiMap.containsEntry(dpId,portName) ;
    }


    private long getDpIdFromPortName(String portName) {
        String dpId = portName.substring(portName.indexOf(SEPARATOR) + 1, portName.lastIndexOf(SEPARATOR));
        return Long.parseLong(dpId);
    }
}

