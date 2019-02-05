/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm;

import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationListener;
import org.opendaylight.openflowplugin.applications.frm.impl.DevicesGroupRegistry;
import org.opendaylight.openflowplugin.applications.frm.impl.FlowNodeConnectorInventoryTranslatorImpl;
import org.opendaylight.serviceutils.srm.RecoverableListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.ArbitratorReconcileService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * It represent a central point for whole module. Implementation Flow Provider
 * registers the link FlowChangeListener} and it holds all needed services for
 * link FlowChangeListener}.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 */
public interface ForwardingRulesManager extends ConfigurationListener, AutoCloseable {

    void start();

    /**
     * Method returns information : "is Node with send InstanceIdentifier
     * connected"?.
     *
     * @param ident
     *            - the key of the node
     * @return boolean - true if device is connected
     */
    boolean isNodeActive(InstanceIdentifier<FlowCapableNode> ident);

    /**
     * Method returns information : "is Node with send InstanceIdentifier present in
     * operational data store"?.
     *
     * @param ident
     *            - the key of the node
     * @return boolean - true if device is present in operational data store
     */
    boolean checkNodeInOperationalDataStore(InstanceIdentifier<FlowCapableNode> ident);

    /**
     * Method returns generated transaction ID, which is unique for every
     * transaction. ID is composite from prefix ("DOM") and unique number.
     *
     * @return String transactionID for RPC transaction identification
     */
    String getNewTransactionId();

    /**
     * Method returns Read Transaction. It is need for Node reconciliation only.
     *
     * @return ReadOnlyTransaction
     */
    ReadTransaction getReadTransaction();

    /**
     * Flow RPC service.
     *
     */
    SalFlowService getSalFlowService();

    /**
     * Group RPC service.
     *
     */
    SalGroupService getSalGroupService();

    /**
     * Meter RPC service.
     *
     */
    SalMeterService getSalMeterService();

    /**
     * Table RPC service.
     *
     */
    SalTableService getSalTableService();

    /**
     * Return Devices Group Registry which can be used to track the groups present in a device.
     *
     * @return devicesGroupRegistry
     */
    DevicesGroupRegistry getDevicesGroupRegistry();

    /**
     * Bundle RPC service.
     *
     * @return salBundleService
     */
    SalBundleService getSalBundleService();

    /**
     * Content definition method and prevent code duplicity in Reconcil.
     *
     * @return ForwardingRulesCommiter&lt;Flow&gt;.
     */
    ForwardingRulesCommiter<Flow> getFlowCommiter();

    /**
     * Content definition method and prevent code duplicity in Reconcil.
     *
     * @return ForwardingRulesCommiter&lt;Group&gt;
     */
    ForwardingRulesCommiter<Group> getGroupCommiter();

    /**
     * Content definition method and prevent code duplicity.
     *
     * @return ForwardingRulesCommiter&lt;Meter&gt;
     */
    ForwardingRulesCommiter<Meter> getMeterCommiter();

    /**
     * Content definition method and prevent code duplicity.
     *
     * @return ForwardingRulesCommiter&lt;Table&gt;
     */
    ForwardingRulesCommiter<TableFeatures> getTableFeaturesCommiter();

    /**
     * Check if reconciliation is disabled by user.
     *
     * @return true if reconciliation is disabled, else false
     */
    boolean isReconciliationDisabled();

    /**
     * Check if stale marking is enabled for switch reconciliation.
     *
     * @return true if stale marking is enabled, else false
     */
    boolean isStaleMarkingEnabled();

    /**
     * Return number of reconciliation retry are allowed.
     *
     * @return number of retries.
     */
    int getReconciliationRetryCount();

    /**
     * Method checks if *this* instance of openflowplugin is owner of the given
     * openflow node.
     *
     * @return True if owner, else false
     */
    boolean isNodeOwner(InstanceIdentifier<FlowCapableNode> ident);

    /**
     * Content definition method and prevent code duplicity.
     *
     * @return FlowNodeConnectorInventoryTranslatorImpl
     */
    FlowNodeConnectorInventoryTranslatorImpl getFlowNodeConnectorInventoryTranslatorImpl();

    /**
     * holds the value read from the configuration file openflowplugin.cfg file.
     *
     * @return True if user enables bundle-based-reconciliation-enabled field in
     *         config file or False
     */
    boolean isBundleBasedReconciliationEnabled();

    /**
     * Return the NodeConfigurator which could be used to serialize jobs.
     *
     * @return modeConfigurator.
     */
    NodeConfigurator getNodeConfigurator();

    /**
     * Method for register RecoverableListener.
     *
     */
    void addRecoverableListener(RecoverableListener recoverableListener);

    /**
     * Method exposes the ArbitratorReconciliationManager service used for performing Arbitrator Based Reconciliation.
     * @return ArbitratorReconciliationManager
     */
    ArbitratorReconcileService getArbitratorReconciliationManager();
}
