/**
 * Copyright (c) 2015 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
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

public class FlowNodeConnectorInventoryTranslatorImpl extends AbstractNodeConnectorCommitter<FlowCapableNodeConnector> implements FlowNodeConnectorInventoryTranslator {

    private static final Logger LOG = LoggerFactory.getLogger(FlowNodeConnectorInventoryTranslatorImpl.class);

    private ListenerRegistration<FlowNodeConnectorInventoryTranslatorImpl> dataTreeChangeListenerRegistration;

    private static final String SEPARATOR = ":";

    private static final InstanceIdentifier<FlowCapableNodeConnector> II_TO_FLOW_CAPABLE_NODE_CONNECTOR
            = InstanceIdentifier.builder(Nodes.class)
            .child(Node.class)
            .child(NodeConnector.class)
            .augmentation(FlowCapableNodeConnector.class)
            .build();

    private Multimap<BigInteger,String> dpnToPortMultiMap = Multimaps.synchronizedListMultimap(ArrayListMultimap.<BigInteger,String>create());

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
            LOG.debug("FlowNodeConnectorInventoryTranslatorImpl DataTreeChangeListener registration fail ..", e);
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
            LOG.debug("Node Connector removed");
            String sNodeConnectorIdentifier = nodeConnIdent
                    .firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId().getValue();
            BigInteger nDpId = getDpIdFromPortName(sNodeConnectorIdentifier);

            dpnToPortMultiMap.remove(nDpId, sNodeConnectorIdentifier);
        }
    }

    @Override
    public void update(InstanceIdentifier<FlowCapableNodeConnector> identifier, FlowCapableNodeConnector original, FlowCapableNodeConnector update, InstanceIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        if(compareInstanceIdentifierTail(identifier,II_TO_FLOW_CAPABLE_NODE_CONNECTOR)){
            LOG.debug("Node Connector updated");
            //Don't need to do anything as we are not considering updates here
        }
    }

    @Override
    public void add(InstanceIdentifier<FlowCapableNodeConnector> identifier, FlowCapableNodeConnector add, InstanceIdentifier<FlowCapableNodeConnector> nodeConnIdent) {
        if(compareInstanceIdentifierTail(identifier,II_TO_FLOW_CAPABLE_NODE_CONNECTOR)){
            LOG.debug("Node Connector added");
            String sNodeConnectorIdentifier = nodeConnIdent
                    .firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId().getValue();
            BigInteger nDpId = getDpIdFromPortName(sNodeConnectorIdentifier);

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
    public boolean isNodeConnectorUpdated(BigInteger dpId, String portName){
        return dpnToPortMultiMap.containsEntry(dpId,portName) ;
    }


    private BigInteger getDpIdFromPortName(String portName) {
        String dpId = portName.substring(portName.indexOf(SEPARATOR) + 1, portName.lastIndexOf(SEPARATOR));
        return new BigInteger(dpId);
    }
}

