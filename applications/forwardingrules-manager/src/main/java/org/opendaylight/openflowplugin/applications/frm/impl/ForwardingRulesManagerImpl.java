/**
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * forwardingrules-manager
 * org.opendaylight.openflowplugin.applications.frm.impl
 *
 * Manager and middle point for whole module.
 * It contains ActiveNodeHolder and provide all RPC services.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Aug 25, 2014
 */
public class ForwardingRulesManagerImpl implements ForwardingRulesManager {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardingRulesManagerImpl.class);
    public static final int STARTUP_LOOP_TICK = 500;
    public static final int STARTUP_LOOP_MAX_RETRIES = 8;

    private final AtomicLong txNum = new AtomicLong();
    private final Object lockObj = new Object();
    private Set<InstanceIdentifier<FlowCapableNode>> activeNodes = Collections.emptySet();

    private final DataBroker dataService;
    private final SalFlowService salFlowService;
    private final SalGroupService salGroupService;
    private final SalMeterService salMeterService;
    private final SalTableService salTableService;

    private ForwardingRulesCommiter<Flow> flowListener;
    private ForwardingRulesCommiter<Group> groupListener;
    private ForwardingRulesCommiter<Meter> meterListener;
    private ForwardingRulesCommiter<TableFeatures> tableListener;
    private FlowNodeReconciliation nodeListener;

    private final ForwardingRulesManagerConfig forwardingRulesManagerConfig;

    public ForwardingRulesManagerImpl(final DataBroker dataBroker,
                                      final RpcConsumerRegistry rpcRegistry,
                                      final ForwardingRulesManagerConfig config) {
        this.dataService = Preconditions.checkNotNull(dataBroker, "DataBroker can not be null!");
        this.forwardingRulesManagerConfig = Preconditions.checkNotNull(config, "Configuration for FRM cannot be null");

        Preconditions.checkArgument(rpcRegistry != null, "RpcConsumerRegistry can not be null !");

        this.salFlowService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalFlowService.class),
                "RPC SalFlowService not found.");
        this.salGroupService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalGroupService.class),
                "RPC SalGroupService not found.");
        this.salMeterService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalMeterService.class),
                "RPC SalMeterService not found.");
        this.salTableService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalTableService.class),
                "RPC SalTableService not found.");
    }

    @Override
    public void start() {

        this.flowListener = new FlowForwarder(this, dataService);

        this.groupListener = new GroupForwarder(this, dataService);
        this.meterListener = new MeterForwarder(this, dataService);

        this.tableListener = new TableForwarder(this, dataService);
        this.nodeListener = new FlowNodeReconciliationImpl(this, dataService);
        LOG.info("ForwardingRulesManager has started successfully.");

    }

    @Override
    public void close() throws Exception {
        if (this.flowListener != null) {
            this.flowListener.close();
            this.flowListener = null;
        }
        if (this.groupListener != null) {
            this.groupListener.close();
            this.groupListener = null;
        }
        if (this.meterListener != null) {
            this.meterListener.close();
            this.meterListener = null;
        }
        if (this.tableListener != null) {
            this.tableListener.close();
            this.tableListener = null;
        }
        if (this.nodeListener != null) {
            this.nodeListener.close();
            this.nodeListener = null;
        }
    }

    @Override
    public ReadOnlyTransaction getReadTranaction() {
        return dataService.newReadOnlyTransaction();
    }

    @Override
    public String getNewTransactionId() {
        return "DOM-" + txNum.getAndIncrement();
    }

    @Override
    public boolean isNodeActive(InstanceIdentifier<FlowCapableNode> ident) {
        return activeNodes.contains(ident);
    }

    @Override
    public boolean checkNodeInOperationalDataStore(InstanceIdentifier<FlowCapableNode> ident) {
        boolean result = false;
        InstanceIdentifier<Node> nodeIid = ident.firstIdentifierOf(Node.class);
        final ReadOnlyTransaction transaction = dataService.newReadOnlyTransaction();
        Optional<Node> optionalDataObject;
        CheckedFuture<Optional<Node>, ReadFailedException> future = transaction.read(LogicalDatastoreType.OPERATIONAL, nodeIid);
        try {
            optionalDataObject = future.checkedGet();
            if (optionalDataObject.isPresent()) {
                result = true;
            } else {
                LOG.debug("{}: Failed to read {}",
                        Thread.currentThread().getStackTrace()[1], nodeIid);
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", nodeIid, e);
        }
        transaction.close();

        return result;
    }

    @Override
    public void registrateNewNode(InstanceIdentifier<FlowCapableNode> ident) {
        if (!activeNodes.contains(ident)) {
            synchronized (lockObj) {
                if (!activeNodes.contains(ident)) {
                    Set<InstanceIdentifier<FlowCapableNode>> set =
                            Sets.newHashSet(activeNodes);
                    set.add(ident);
                    activeNodes = Collections.unmodifiableSet(set);
                }
            }
        }
    }

    @Override
    public void unregistrateNode(InstanceIdentifier<FlowCapableNode> ident) {
        if (activeNodes.contains(ident)) {
            synchronized (lockObj) {
                if (activeNodes.contains(ident)) {
                    Set<InstanceIdentifier<FlowCapableNode>> set =
                            Sets.newHashSet(activeNodes);
                    set.remove(ident);
                    activeNodes = Collections.unmodifiableSet(set);
                }
            }
        }
    }

    @Override
    public SalFlowService getSalFlowService() {
        return salFlowService;
    }

    @Override
    public SalGroupService getSalGroupService() {
        return salGroupService;
    }

    @Override
    public SalMeterService getSalMeterService() {
        return salMeterService;
    }

    @Override
    public SalTableService getSalTableService() {
        return salTableService;
    }

    @Override
    public ForwardingRulesCommiter<Flow> getFlowCommiter() {
        return flowListener;
    }

    @Override
    public ForwardingRulesCommiter<Group> getGroupCommiter() {
        return groupListener;
    }

    @Override
    public ForwardingRulesCommiter<Meter> getMeterCommiter() {
        return meterListener;
    }

    @Override
    public ForwardingRulesCommiter<TableFeatures> getTableFeaturesCommiter() {
        return tableListener;
    }

    @Override
    public FlowNodeReconciliation getFlowNodeReconciliation() {
        return nodeListener;
    }

    @Override
    public ForwardingRulesManagerConfig getConfiguration() {
        return forwardingRulesManagerConfig;
    }
}

