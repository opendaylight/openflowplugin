/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

import org.opendaylight.openflowplugin.applications.frm.impl.ForwardingRulesManagerConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;

import org.opendaylight.openflowplugin.applications.frm.impl.FlowNodeConnectorInventoryTranslatorImpl;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * forwardingrules-manager
 * org.opendaylight.openflowplugin.applications.frm
 *
 * ForwardingRulesManager
 * It represent a central point for whole modul. Implementation
 * Flow Provider registers the link FlowChangeListener} and it holds all needed
 * services for link FlowChangeListener}.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Aug 25, 2014
 */
public interface ForwardingRulesManager extends AutoCloseable {

    public void start();

    /**
     * Method returns information :
     * "is Node with send InstanceIdentifier connected"?
     *
     * @param ident - the key of the node
     * @return boolean - true if device is connected
     */
    public boolean isNodeActive(InstanceIdentifier<FlowCapableNode> ident);

    /**
     * Method returns information :
     * "is Node with send InstanceIdentifier present in operational data store"?
     *
     * @param ident - the key of the node
     * @return boolean - true if device is present in operational data store
     */
    public boolean checkNodeInOperationalDataStore(InstanceIdentifier<FlowCapableNode> ident);

    /**
     * Method add new {@link FlowCapableNode} to active Node Holder.
     * ActiveNodeHolder prevent unnecessary Operational/DS read for identify
     * pre-configure and serious Configure/DS transactions.
     *
     * @param ident - the key of the node
     */
    public void registrateNewNode(InstanceIdentifier<FlowCapableNode> ident);

    /**
     * Method remove disconnected {@link FlowCapableNode} from active Node
     * Holder. And all next flows or groups or meters will stay in Config/DS
     * only.
     *
     * @param ident - the key of the node
     */
    public void unregistrateNode(InstanceIdentifier<FlowCapableNode> ident);

    /**
     * Method returns generated transaction ID, which is unique for
     * every transaction. ID is composite from prefix ("DOM") and unique number.
     *
     * @return String transactionID for RPC transaction identification
     */
    public String getNewTransactionId();

    /**
     * Method returns Read Transacion. It is need for Node reconciliation only.
     *
     * @return ReadOnlyTransaction
     */
    public ReadOnlyTransaction getReadTranaction();

    /**
     * Flow RPC service
     *
     * @return
     */
    public SalFlowService getSalFlowService();

    /**
     * Group RPC service
     *
     * @return
     */
    public SalGroupService getSalGroupService();

    /**
     * Meter RPC service
     *
     * @return
     */
    public SalMeterService getSalMeterService();

    /**
     * Table RPC service
     *
     * @return
     */
    public SalTableService getSalTableService();

    /**
     * Content definition method and prevent code duplicity in Reconcil
     * @return ForwardingRulesCommiter&lt;Flow&gt;
     */
    public ForwardingRulesCommiter<Flow> getFlowCommiter();

    /**
     * Content definition method and prevent code duplicity in Reconcil
     * @return ForwardingRulesCommiter&lt;Group&gt;
     */
    public ForwardingRulesCommiter<Group> getGroupCommiter();

    /**
     * Content definition method and prevent code duplicity
     * @return ForwardingRulesCommiter&lt;Meter&gt;
     */
    public ForwardingRulesCommiter<Meter> getMeterCommiter();

    /**
     * Content definition method and prevent code duplicity
     * @return ForwardingRulesCommiter&lt;Table&gt;
     */
    public ForwardingRulesCommiter<TableFeatures> getTableFeaturesCommiter();

    /**
     * Content definition method
     * @return FlowNodeReconciliation
     */
    public FlowNodeReconciliation getFlowNodeReconciliation();

    /**
     * Returns the config-subsystem/fallback configuration of FRM
     * @return ForwardingRulesManagerConfig
     */
    public ForwardingRulesManagerConfig getConfiguration();

    /**
     * Method checks if *this* instance of openflowplugin is owner of
     * the given openflow node.
     * @return True if owner, else false
     */
    public boolean isNodeOwner(InstanceIdentifier<FlowCapableNode> ident);
     
    /**
     * Content definition method and prevent code duplicity
     * @return FlowNodeConnectorInventoryTranslatorImpl
     */
    public FlowNodeConnectorInventoryTranslatorImpl getFlowNodeConnectorInventoryTranslatorImpl();

}

