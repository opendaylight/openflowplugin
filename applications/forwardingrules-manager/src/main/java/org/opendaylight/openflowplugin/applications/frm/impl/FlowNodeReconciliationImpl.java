/**
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.Messages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.MessagesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.MessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.add.flow._case.AddFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.add.group._case.AddGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.remove.flow._case.RemoveFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.remove.group._case.RemoveGroupCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ForwardingRulesManager}.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 */
public class FlowNodeReconciliationImpl implements FlowNodeReconciliation {

    private static final Logger LOG = LoggerFactory.getLogger(FlowNodeReconciliationImpl.class);

    // The number of nanoseconds to wait for a single group to be added.
    private static final long ADD_GROUP_TIMEOUT = TimeUnit.SECONDS.toNanos(3);

    // The maximum number of nanoseconds to wait for completion of add-group RPCs.
    private static final long MAX_ADD_GROUP_TIMEOUT = TimeUnit.SECONDS.toNanos(20);
    private static final String SEPARATOR = ":";
    private static final int THREAD_POOL_SIZE = 4;

    private final DataBroker dataBroker;
    private final ForwardingRulesManager provider;
    private final String serviceName;
    private final int priority;
    private final ResultState resultState;
    private final Map<DeviceInfo, ListenableFuture<Boolean>> futureMap = new HashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    private final SalBundleService salBundleService;

    private static final AtomicLong BUNDLE_ID = new AtomicLong();
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);

    public FlowNodeReconciliationImpl(final ForwardingRulesManager manager, final DataBroker db,
            final String serviceName, final int priority, final ResultState resultState) {
        this.provider = Preconditions.checkNotNull(manager, "ForwardingRulesManager can not be null!");
        dataBroker = Preconditions.checkNotNull(db, "DataBroker can not be null!");
        this.serviceName = serviceName;
        this.priority = priority;
        this.resultState = resultState;
        salBundleService = Preconditions.checkNotNull(manager.getSalBundleService(),
                "salBundleService can not be null!");
    }

    @Override
    public void close() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Override
    public ListenableFuture<Boolean> reconcileConfiguration(InstanceIdentifier<FlowCapableNode> connectedNode) {
        LOG.info("Triggering reconciliation for device {}", connectedNode.firstKeyOf(Node.class));
        if (provider.isStaleMarkingEnabled()) {
            LOG.info("Stale-Marking is ENABLED and proceeding with deletion of " + "stale-marked entities on switch {}",
                    connectedNode.toString());
            reconciliationPreProcess(connectedNode);
        }
        if (provider.isBundleBasedReconciliationEnabled()) {
            BundleBasedReconciliationTask bundleBasedReconTask = new BundleBasedReconciliationTask(connectedNode);
            return JdkFutureAdapters.listenInPoolThread(executor.submit(bundleBasedReconTask));
        } else {
            ReconciliationTask reconciliationTask = new ReconciliationTask(connectedNode);
            return JdkFutureAdapters.listenInPoolThread(executor.submit(reconciliationTask));
        }
    }

    private class BundleBasedReconciliationTask implements Callable<Boolean> {
        final InstanceIdentifier<FlowCapableNode> nodeIdentity;

        BundleBasedReconciliationTask(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
            nodeIdentity = nodeIdent;
        }

        @Override
        public Boolean call() {
            String node = nodeIdentity.firstKeyOf(Node.class).getId().getValue();
            Optional<FlowCapableNode> flowNode = Optional.absent();
            BundleId bundleIdValue = new BundleId(BUNDLE_ID.getAndIncrement());
            BigInteger dpnId = getDpnIdFromNodeName(node);
            LOG.info("Triggering bundle based reconciliation for device : {}", dpnId);
            try (ReadOnlyTransaction trans = provider.getReadTransaction()) {
                flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdentity).get();
            } catch (ExecutionException | InterruptedException e) {
                LOG.error("Error occurred while reading the configuration data store for node {}", nodeIdentity, e);
            }

            if (flowNode.isPresent()) {
                LOG.debug("FlowNode present for Datapath ID {}", dpnId);
                final NodeRef nodeRef = new NodeRef(nodeIdentity.firstIdentifierOf(Node.class));

                final ControlBundleInput closeBundleInput = new ControlBundleInputBuilder().setNode(nodeRef)
                        .setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTCLOSEREQUEST).build();

                final ControlBundleInput openBundleInput = new ControlBundleInputBuilder().setNode(nodeRef)
                        .setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS).setType(BundleControlType.ONFBCTOPENREQUEST)
                        .build();

                final ControlBundleInput commitBundleInput = new ControlBundleInputBuilder().setNode(nodeRef)
                        .setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTCOMMITREQUEST).build();

                final AddBundleMessagesInput addBundleMessagesInput = new AddBundleMessagesInputBuilder()
                        .setNode(nodeRef).setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                        .setMessages(createMessages(nodeRef, flowNode)).build();

                /* Close previously opened bundle on the openflow switch if any */
                ListenableFuture<RpcResult<ControlBundleOutput>> closeBundle
                        = salBundleService.controlBundle(closeBundleInput);

                /* Open a new bundle on the switch */
                ListenableFuture<RpcResult<ControlBundleOutput>> openBundle =
                        Futures.transformAsync(closeBundle,
                            rpcResult -> salBundleService.controlBundle(openBundleInput),
                            MoreExecutors.directExecutor());

                /* Push groups and flows via bundle add messages */
                ListenableFuture<RpcResult<AddBundleMessagesOutput>> addBundleMessagesFuture
                        = Futures.transformAsync(openBundle, rpcResult -> {
                            if (rpcResult.isSuccessful()) {
                                return salBundleService.addBundleMessages(addBundleMessagesInput);
                            }
                            return Futures.immediateFuture(null);
                        }, MoreExecutors.directExecutor());

                /* Commit the bundle on the openflow switch */
                ListenableFuture<RpcResult<ControlBundleOutput>> commitBundleFuture
                        = Futures.transformAsync(addBundleMessagesFuture, rpcResult -> {
                            if (rpcResult.isSuccessful()) {
                                return salBundleService.controlBundle(commitBundleInput);
                            }
                            return Futures.immediateFuture(null);
                        }, MoreExecutors.directExecutor());

                /* Bundles not supported for meters */
                List<Meter> meters = flowNode.get().getMeter() != null ? flowNode.get().getMeter()
                        : Collections.emptyList();
                Futures.transformAsync(commitBundleFuture,
                    rpcResult -> {
                        if (rpcResult.isSuccessful()) {
                            for (Meter meter : meters) {
                                final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent = nodeIdentity
                                        .child(Meter.class, meter.key());
                                provider.getMeterCommiter().add(meterIdent, meter, nodeIdentity);
                            }
                        }
                        return Futures.immediateFuture(null);
                    }, MoreExecutors.directExecutor());

                try {
                    if (commitBundleFuture.get().isSuccessful()) {
                        LOG.debug("Completing bundle based reconciliation for device ID:{}", dpnId);
                        return true;
                    } else {
                        return false;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("Error while doing bundle based reconciliation for device ID:{}", nodeIdentity);
                    return false;
                }
            }
            LOG.error("FlowNode not present for Datapath ID {}", dpnId);
            return false;
        }
    }

    @Override
    public ListenableFuture<Boolean> startReconciliation(DeviceInfo node) {
        InstanceIdentifier<FlowCapableNode> connectedNode = node.getNodeInstanceIdentifier()
                .augmentation(FlowCapableNode.class);
        // Clearing the group registry cache for the connected node if exists
        provider.getDevicesGroupRegistry().clearNodeGroups(node.getNodeId());
        return futureMap.computeIfAbsent(node, future -> reconcileConfiguration(connectedNode));
    }

    @Override
    public ListenableFuture<Boolean> endReconciliation(DeviceInfo node) {
        futureMap.computeIfPresent(node, (key, future) -> future).cancel(true);
        futureMap.remove(node);
        return Futures.immediateFuture(true);
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public String getName() {
        return serviceName;
    }

    @Override
    public ResultState getResultState() {
        return resultState;
    }

    private class ReconciliationTask implements Callable<Boolean> {

        InstanceIdentifier<FlowCapableNode> nodeIdentity;

        ReconciliationTask(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
            nodeIdentity = nodeIdent;
        }

        @Override
        public Boolean call() {
            String node = nodeIdentity.firstKeyOf(Node.class).getId().getValue();
            BigInteger dpnId = getDpnIdFromNodeName(node);

            Optional<FlowCapableNode> flowNode;
            // initialize the counter
            int counter = 0;
            try (ReadOnlyTransaction trans = provider.getReadTransaction()) {
                flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdentity).get();
            } catch (ExecutionException | InterruptedException e) {
                LOG.warn("Fail with read Config/DS for Node {} !", nodeIdentity, e);
                return false;
            }

            if (flowNode.isPresent()) {
                /* Tables - have to be pushed before groups */
                // CHECK if while pushing the update, updateTableInput can be null to emulate a
                // table add
                List<TableFeatures> tableList = flowNode.get().getTableFeatures() != null
                        ? flowNode.get().getTableFeatures()
                        : Collections.<TableFeatures>emptyList();
                for (TableFeatures tableFeaturesItem : tableList) {
                    TableFeaturesKey tableKey = tableFeaturesItem.key();
                    KeyedInstanceIdentifier<TableFeatures, TableFeaturesKey> tableFeaturesII = nodeIdentity
                            .child(TableFeatures.class, new TableFeaturesKey(tableKey.getTableId()));
                    provider.getTableFeaturesCommiter().update(tableFeaturesII, tableFeaturesItem, null, nodeIdentity);
                }

                /* Groups - have to be first */
                List<Group> groups = flowNode.get().getGroup() != null ? flowNode.get().getGroup()
                        : Collections.<Group>emptyList();
                List<Group> toBeInstalledGroups = new ArrayList<>();
                toBeInstalledGroups.addAll(groups);
                // new list for suspected groups pointing to ports .. when the ports come up
                // late
                List<Group> suspectedGroups = new ArrayList<>();
                Map<Long, ListenableFuture<?>> groupFutures = new HashMap<>();

                while ((!toBeInstalledGroups.isEmpty() || !suspectedGroups.isEmpty())
                        && counter <= provider.getReconciliationRetryCount()) { // also check if the counter has not
                                                                                // crossed the threshold

                    if (toBeInstalledGroups.isEmpty() && !suspectedGroups.isEmpty()) {
                        LOG.debug("These Groups are pointing to node-connectors that are not up yet {}",
                                suspectedGroups.toString());
                        toBeInstalledGroups.addAll(suspectedGroups);
                        break;
                    }

                    ListIterator<Group> iterator = toBeInstalledGroups.listIterator();
                    while (iterator.hasNext()) {
                        Group group = iterator.next();
                        boolean okToInstall = true;
                        Buckets buckets = group.getBuckets();
                        List<Bucket> bucketList = buckets == null ? null : buckets.getBucket();
                        if (bucketList == null) {
                            bucketList = Collections.<Bucket>emptyList();
                        }
                        for (Bucket bucket : bucketList) {
                            List<Action> actions = bucket.getAction();
                            if (actions == null) {
                                actions = Collections.<Action>emptyList();
                            }
                            for (Action action : actions) {
                                // chained-port
                                if (action.getAction().getImplementedInterface().getName()
                                        .equals("org.opendaylight.yang.gen.v1.urn.opendaylight"
                                                + ".action.types.rev131112.action.action.OutputActionCase")) {
                                    String nodeConnectorUri = ((OutputActionCase) action.getAction()).getOutputAction()
                                            .getOutputNodeConnector().getValue();

                                    LOG.debug("Installing the group for node connector {}", nodeConnectorUri);

                                    // check if the nodeconnector is there in the multimap
                                    boolean isPresent = provider.getFlowNodeConnectorInventoryTranslatorImpl()
                                            .isNodeConnectorUpdated(dpnId, nodeConnectorUri);
                                    // if yes set okToInstall = true

                                    if (isPresent) {
                                        break;
                                    } else {
                                        // else put it in a different list and still set okToInstall = true
                                        suspectedGroups.add(group);
                                        LOG.debug(
                                                "Not yet received the node-connector updated for {} "
                                                        + "for the group with id {}",
                                                nodeConnectorUri, group.getGroupId().toString());
                                        break;
                                    }
                                } else if (action.getAction().getImplementedInterface().getName()
                                        .equals("org.opendaylight.yang.gen.v1.urn.opendaylight"
                                                + ".action.types.rev131112.action.action.GroupActionCase")) {
                                    // chained groups
                                    Long groupId = ((GroupActionCase) action.getAction()).getGroupAction().getGroupId();
                                    ListenableFuture<?> future = groupFutures.get(groupId);
                                    if (future == null) {
                                        okToInstall = false;
                                        break;
                                    }
                                    // Need to ensure that the group specified
                                    // by group-action is already installed.
                                    awaitGroup(node, future);
                                }
                            }
                            if (!okToInstall) {
                                // increment retry counter value
                                counter++;
                                break;
                            }
                        }
                        if (okToInstall) {
                            addGroup(groupFutures, group);
                            iterator.remove();
                            // resetting the counter to zero
                            counter = 0;
                        }
                    }
                }

                /* installation of suspected groups */
                if (!toBeInstalledGroups.isEmpty()) {
                    for (Group group : toBeInstalledGroups) {
                        LOG.debug(
                                "Installing the group {} finally although "
                                        + "the port is not up after checking for {} times ",
                                group.getGroupId().toString(), provider.getReconciliationRetryCount());
                        addGroup(groupFutures, group);
                    }
                }
                /* Meters */
                List<Meter> meters = flowNode.get().getMeter() != null ? flowNode.get().getMeter()
                        : Collections.<Meter>emptyList();
                for (Meter meter : meters) {
                    final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent = nodeIdentity.child(Meter.class,
                            meter.key());
                    provider.getMeterCommiter().add(meterIdent, meter, nodeIdentity);
                }

                // Need to wait for all groups to be installed before adding
                // flows.
                awaitGroups(node, groupFutures.values());

                /* Flows */
                List<Table> tables = flowNode.get().getTable() != null ? flowNode.get().getTable()
                        : Collections.<Table>emptyList();
                for (Table table : tables) {
                    final KeyedInstanceIdentifier<Table, TableKey> tableIdent = nodeIdentity.child(Table.class,
                            table.key());
                    List<Flow> flows = table.getFlow() != null ? table.getFlow() : Collections.<Flow>emptyList();
                    for (Flow flow : flows) {
                        final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent = tableIdent.child(Flow.class,
                                flow.key());
                        provider.getFlowCommiter().add(flowIdent, flow, nodeIdentity);
                    }
                }
            }
            return true;
        }

        /**
         * Invoke add-group RPC, and put listenable future associated with the RPC into
         * the given map.
         *
         * @param map
         *            The map to store listenable futures associated with add-group RPC.
         * @param group
         *            The group to add.
         */
        private void addGroup(Map<Long, ListenableFuture<?>> map, Group group) {
            KeyedInstanceIdentifier<Group, GroupKey> groupIdent = nodeIdentity.child(Group.class, group.key());
            final Long groupId = group.getGroupId().getValue();
            ListenableFuture<?> future = JdkFutureAdapters
                    .listenInPoolThread(provider.getGroupCommiter().add(groupIdent, group, nodeIdentity));

            Futures.addCallback(future, new FutureCallback<Object>() {
                @Override
                public void onSuccess(Object result) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("add-group RPC completed: node={}, id={}",
                                nodeIdentity.firstKeyOf(Node.class).getId().getValue(), groupId);
                    }
                }

                @Override
                public void onFailure(Throwable cause) {
                    LOG.debug("add-group RPC failed: node={}, id={}",
                            nodeIdentity.firstKeyOf(Node.class).getId().getValue(), groupId, cause);
                }
            }, MoreExecutors.directExecutor());

            map.put(groupId, future);
        }

        /**
         * Wait for completion of add-group RPC.
         *
         * @param nodeId
         *            The identifier for the target node.
         * @param future
         *            Future associated with add-group RPC that installs the target
         *            group.
         */
        private void awaitGroup(String nodeId, ListenableFuture<?> future) {
            awaitGroups(nodeId, Collections.singleton(future));
        }

        /**
         * Wait for completion of add-group RPCs.
         *
         * @param nodeId
         *            The identifier for the target node.
         * @param futures
         *            A collection of futures associated with add-group RPCs.
         */
        private void awaitGroups(String nodeId, Collection<ListenableFuture<?>> futures) {
            if (!futures.isEmpty()) {
                long timeout = Math.min(ADD_GROUP_TIMEOUT * futures.size(), MAX_ADD_GROUP_TIMEOUT);
                try {
                    Futures.successfulAsList(futures).get(timeout, TimeUnit.NANOSECONDS);
                    LOG.trace("awaitGroups() completed: node={}", nodeId);
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    LOG.debug("add-group RPCs did not complete: node={}", nodeId);
                }
            }
        }
    }

    private BigInteger getDpnIdFromNodeName(String nodeName) {

        String dpId = nodeName.substring(nodeName.lastIndexOf(SEPARATOR) + 1);
        return new BigInteger(dpId);
    }

    private void reconciliationPreProcess(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        List<InstanceIdentifier<StaleFlow>> staleFlowsToBeBulkDeleted = Lists.newArrayList();
        List<InstanceIdentifier<StaleGroup>> staleGroupsToBeBulkDeleted = Lists.newArrayList();
        List<InstanceIdentifier<StaleMeter>> staleMetersToBeBulkDeleted = Lists.newArrayList();

        Optional<FlowCapableNode> flowNode = Optional.absent();

        try (ReadOnlyTransaction trans = provider.getReadTransaction()) {
            flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdent).get();
        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Reconciliation Pre-Processing Fail with read Config/DS for Node {} !", nodeIdent, e);
        }

        if (flowNode.isPresent()) {

            LOG.debug("Proceeding with deletion of stale-marked Flows on switch {} using Openflow interface",
                    nodeIdent.toString());
            /* Stale-Flows - Stale-marked Flows have to be removed first for safety */
            List<Table> tables = flowNode.get().getTable() != null ? flowNode.get().getTable()
                    : Collections.<Table>emptyList();
            for (Table table : tables) {
                final KeyedInstanceIdentifier<Table, TableKey> tableIdent = nodeIdent.child(Table.class,
                        table.key());
                List<StaleFlow> staleFlows = table.getStaleFlow() != null ? table.getStaleFlow()
                        : Collections.<StaleFlow>emptyList();
                for (StaleFlow staleFlow : staleFlows) {

                    FlowBuilder flowBuilder = new FlowBuilder(staleFlow);
                    Flow toBeDeletedFlow = flowBuilder.setId(staleFlow.getId()).build();

                    final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent = tableIdent.child(Flow.class,
                            toBeDeletedFlow.key());

                    this.provider.getFlowCommiter().remove(flowIdent, toBeDeletedFlow, nodeIdent);

                    staleFlowsToBeBulkDeleted.add(getStaleFlowInstanceIdentifier(staleFlow, nodeIdent));
                }
            }

            LOG.debug("Proceeding with deletion of stale-marked Groups for switch {} using Openflow interface",
                    nodeIdent.toString());

            // TODO: Should we collate the futures of RPC-calls to be sure that groups are
            // Flows are fully deleted
            // before attempting to delete groups - just in case there are references

            /* Stale-marked Groups - Can be deleted after flows */
            List<StaleGroup> staleGroups = flowNode.get().getStaleGroup() != null ? flowNode.get().getStaleGroup()
                    : Collections.<StaleGroup>emptyList();
            for (StaleGroup staleGroup : staleGroups) {

                GroupBuilder groupBuilder = new GroupBuilder(staleGroup);
                Group toBeDeletedGroup = groupBuilder.setGroupId(staleGroup.getGroupId()).build();

                final KeyedInstanceIdentifier<Group, GroupKey> groupIdent = nodeIdent.child(Group.class,
                        toBeDeletedGroup.key());

                this.provider.getGroupCommiter().remove(groupIdent, toBeDeletedGroup, nodeIdent);

                staleGroupsToBeBulkDeleted.add(getStaleGroupInstanceIdentifier(staleGroup, nodeIdent));
            }

            LOG.debug("Proceeding with deletion of stale-marked Meters for switch {} using Openflow interface",
                    nodeIdent.toString());
            /* Stale-marked Meters - can be deleted anytime - so least priority */
            List<StaleMeter> staleMeters = flowNode.get().getStaleMeter() != null ? flowNode.get().getStaleMeter()
                    : Collections.<StaleMeter>emptyList();

            for (StaleMeter staleMeter : staleMeters) {

                MeterBuilder meterBuilder = new MeterBuilder(staleMeter);
                Meter toBeDeletedMeter = meterBuilder.setMeterId(staleMeter.getMeterId()).build();

                final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent = nodeIdent.child(Meter.class,
                        toBeDeletedMeter.key());

                this.provider.getMeterCommiter().remove(meterIdent, toBeDeletedMeter, nodeIdent);

                staleMetersToBeBulkDeleted.add(getStaleMeterInstanceIdentifier(staleMeter, nodeIdent));
            }

        }

        LOG.debug("Deleting all stale-marked flows/groups/meters of for switch {} in Configuration DS",
                nodeIdent.toString());
        // Now, do the bulk deletions
        deleteDSStaleFlows(staleFlowsToBeBulkDeleted);
        deleteDSStaleGroups(staleGroupsToBeBulkDeleted);
        deleteDSStaleMeters(staleMetersToBeBulkDeleted);
    }

    private void deleteDSStaleFlows(List<InstanceIdentifier<StaleFlow>> flowsForBulkDelete) {
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        for (InstanceIdentifier<StaleFlow> staleFlowIId : flowsForBulkDelete) {
            writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, staleFlowIId);
        }

        ListenableFuture<Void> submitFuture = writeTransaction.submit();
        handleStaleEntityDeletionResultFuture(submitFuture);
    }

    private void deleteDSStaleGroups(List<InstanceIdentifier<StaleGroup>> groupsForBulkDelete) {
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        for (InstanceIdentifier<StaleGroup> staleGroupIId : groupsForBulkDelete) {
            writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, staleGroupIId);
        }

        ListenableFuture<Void> submitFuture = writeTransaction.submit();
        handleStaleEntityDeletionResultFuture(submitFuture);
    }

    private void deleteDSStaleMeters(List<InstanceIdentifier<StaleMeter>> metersForBulkDelete) {
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        for (InstanceIdentifier<StaleMeter> staleMeterIId : metersForBulkDelete) {
            writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, staleMeterIId);
        }

        ListenableFuture<Void> submitFuture = writeTransaction.submit();
        handleStaleEntityDeletionResultFuture(submitFuture);
    }

    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight
        .flow.inventory.rev130819.tables.table.StaleFlow> getStaleFlowInstanceIdentifier(
            StaleFlow staleFlow, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.child(Table.class, new TableKey(staleFlow.getTableId())).child(
                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlow.class,
                new StaleFlowKey(new FlowId(staleFlow.getId())));
    }

    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight
        .group.types.rev131018.groups.StaleGroup> getStaleGroupInstanceIdentifier(
            StaleGroup staleGroup, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.child(StaleGroup.class, new StaleGroupKey(new GroupId(staleGroup.getGroupId())));
    }

    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight
        .flow.inventory.rev130819.meters.StaleMeter> getStaleMeterInstanceIdentifier(
            StaleMeter staleMeter, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.child(StaleMeter.class, new StaleMeterKey(new MeterId(staleMeter.getMeterId())));
    }

    private void handleStaleEntityDeletionResultFuture(ListenableFuture<Void> submitFuture) {
        Futures.addCallback(submitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                LOG.debug("Stale entity removal success");
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.debug("Stale entity removal failed {}", throwable);
            }
        }, MoreExecutors.directExecutor());
    }

    private Flow getDeleteAllFlow() {
        final FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setTableId(OFConstants.OFPTT_ALL);
        return flowBuilder.build();
    }

    private Group getDeleteAllGroup() {
        final GroupBuilder groupBuilder = new GroupBuilder();
        groupBuilder.setGroupType(GroupTypes.GroupAll);
        groupBuilder.setGroupId(new GroupId(OFConstants.OFPG_ALL));
        return groupBuilder.build();
    }

    private Messages createMessages(final NodeRef nodeRef, final Optional<FlowCapableNode> flowNode) {
        final List<Message> messages = new ArrayList<>();
        messages.add(new MessageBuilder().setNode(nodeRef)
                .setBundleInnerMessage(new BundleRemoveFlowCaseBuilder()
                        .setRemoveFlowCaseData(new RemoveFlowCaseDataBuilder(getDeleteAllFlow()).build()).build())
                .build());

        messages.add(new MessageBuilder().setNode(nodeRef)
                .setBundleInnerMessage(new BundleRemoveGroupCaseBuilder()
                        .setRemoveGroupCaseData(new RemoveGroupCaseDataBuilder(getDeleteAllGroup()).build()).build())
                .build());

        if (flowNode.get().getGroup() != null) {
            for (Group gr : flowNode.get().getGroup()) {
                NodeId nodeId = nodeRef.getValue().firstKeyOf(Node.class).getId();
                provider.getDevicesGroupRegistry().storeGroup(nodeId,gr.getGroupId().getValue());
                messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(new BundleAddGroupCaseBuilder()
                        .setAddGroupCaseData(new AddGroupCaseDataBuilder(gr).build()).build()).build());
            }
        }

        if (flowNode.get().getTable() != null) {
            for (Table table : flowNode.get().getTable()) {
                for (Flow flow : table.getFlow()) {
                    messages.add(
                            new MessageBuilder().setNode(nodeRef)
                                    .setBundleInnerMessage(new BundleAddFlowCaseBuilder()
                                            .setAddFlowCaseData(new AddFlowCaseDataBuilder(flow).build()).build())
                                    .build());
                }
            }
        }

        LOG.debug("The size of the flows and group messages created in createMessage() {}", messages.size());
        return new MessagesBuilder().setMessage(messages).build();
    }
}
