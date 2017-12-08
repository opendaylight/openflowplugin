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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ForwardingRulesManager}
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 */
public class FlowNodeReconciliationImpl implements FlowNodeReconciliation {
     private static final Logger LOG = LoggerFactory.getLogger(FlowNodeReconciliationImpl.class);

    //The number of nanoseconds to wait for a single group to be added.
    private static final long  ADD_GROUP_TIMEOUT = TimeUnit.SECONDS.toNanos(3);

     //The maximum number of nanoseconds to wait for completion of add-group RPCs.
    private static final long  MAX_ADD_GROUP_TIMEOUT = TimeUnit.SECONDS.toNanos(20);
    private static final String SEPARATOR = ":";
    private static final int THREAD_POOL_SIZE = 4;

    private final DataBroker dataBroker;
    private final ForwardingRulesManager provider;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public FlowNodeReconciliationImpl (final ForwardingRulesManager manager, final DataBroker db) {
        this.provider = Preconditions.checkNotNull(manager, "ForwardingRulesManager can not be null!");
        dataBroker = Preconditions.checkNotNull(db, "DataBroker can not be null!");
    }

    @Override
    public void close() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Override
    public void reconcileConfiguration(InstanceIdentifier<FlowCapableNode> connectedNode) {
        if (provider.isReconciliationDisabled()) {
            LOG.debug("Reconciliation is disabled by user. Skipping reconciliation of node : {}", connectedNode
                    .firstKeyOf(Node.class));
            return;
        }
        if (provider.isNodeOwner(connectedNode)) {
            LOG.info("Triggering reconciliation for device {}", connectedNode.firstKeyOf(Node.class));
            if (provider.isStaleMarkingEnabled()) {
                LOG.info("Stale-Marking is ENABLED and proceeding with deletion of stale-marked entities on switch {}",
                        connectedNode.toString());
                reconciliationPreProcess(connectedNode);
            }
            ReconciliationTask reconciliationTask = new ReconciliationTask(connectedNode);
            executor.execute(reconciliationTask);
        }
    }

    private class ReconciliationTask implements Runnable {

        InstanceIdentifier<FlowCapableNode> nodeIdentity;

        public ReconciliationTask(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
           nodeIdentity = nodeIdent;
        }

        @Override
        public void run() {

            String sNode = nodeIdentity.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
            BigInteger nDpId = getDpnIdFromNodeName(sNode);

            ReadOnlyTransaction trans = provider.getReadTranaction();
            Optional<FlowCapableNode> flowNode = Optional.absent();

            //initialize the counter
            int counter = 0;
            try {
                flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdentity).get();
            } catch (Exception e) {
                LOG.error("Fail with read Config/DS for Node {} !", nodeIdentity, e);
            }

            if (flowNode.isPresent()) {
            /* Tables - have to be pushed before groups */
                // CHECK if while pusing the update, updateTableInput can be null to emulate a table add
                List<TableFeatures> tableList = flowNode.get().getTableFeatures() != null
                        ? flowNode.get().getTableFeatures() : Collections.<TableFeatures>emptyList();
                for (TableFeatures tableFeaturesItem : tableList) {
                    TableFeaturesKey tableKey = tableFeaturesItem.getKey();
                    KeyedInstanceIdentifier<TableFeatures, TableFeaturesKey> tableFeaturesII
                            = nodeIdentity.child(TableFeatures.class, new TableFeaturesKey(tableKey.getTableId()));
                    provider.getTableFeaturesCommiter().update(tableFeaturesII, tableFeaturesItem, null, nodeIdentity);
                }

            /* Groups - have to be first */
                List<Group> groups = flowNode.get().getGroup() != null
                        ? flowNode.get().getGroup() : Collections.<Group>emptyList();
                List<Group> toBeInstalledGroups = new ArrayList<>();
                toBeInstalledGroups.addAll(groups);
                //new list for suspected groups pointing to ports .. when the ports come up late
                List<Group> suspectedGroups = new ArrayList<>();
                Map<Long, ListenableFuture<?>> groupFutures = new HashMap<>();

                while ((!(toBeInstalledGroups.isEmpty()) || !(suspectedGroups.isEmpty())) &&
                        (counter <= provider.getReconciliationRetryCount())) { //also check if the counter has not crossed the threshold

                    if (toBeInstalledGroups.isEmpty() && !suspectedGroups.isEmpty()) {
                        LOG.debug("These Groups are pointing to node-connectors that are not up yet {}", suspectedGroups.toString());
                        toBeInstalledGroups.addAll(suspectedGroups);
                        break;
                    }

                    ListIterator<Group> iterator = toBeInstalledGroups.listIterator();
                    while (iterator.hasNext()) {
                        Group group = iterator.next();
                        boolean okToInstall = true;
                        Buckets buckets = group.getBuckets();
                        List<Bucket> bucketList = (buckets == null)
                            ? null : buckets.getBucket();
                        if (bucketList == null) {
                            bucketList = Collections.<Bucket>emptyList();
                        }
                        for (Bucket bucket : bucketList) {
                            List<Action> actions = bucket.getAction();
                            if (actions == null) {
                                actions = Collections.<Action>emptyList();
                            }
                            for (Action action : actions) {
                                //chained-port
                                if (action.getAction().getImplementedInterface().getName()
                                        .equals("org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase")) {
                                    String nodeConnectorUri = ((OutputActionCase) (action.getAction()))
                                            .getOutputAction().getOutputNodeConnector().getValue();

                                    LOG.debug("Installing the group for node connector {}", nodeConnectorUri);

                                    //check if the nodeconnector is there in the multimap
                                    boolean isPresent = provider.getFlowNodeConnectorInventoryTranslatorImpl()
                                            .isNodeConnectorUpdated(nDpId, nodeConnectorUri);
                                    //if yes set okToInstall = true

                                    if (isPresent) {
                                        break;
                                    }//else put it in a different list and still set okToInstall = true
                                    else {
                                        suspectedGroups.add(group);
                                        LOG.debug("Not yet received the node-connector updated for {} " +
                                                "for the group with id {}", nodeConnectorUri, group.getGroupId().toString());
                                        break;
                                    }


                                }
                                //chained groups
                                else if (action.getAction().getImplementedInterface().getName()
                                        .equals("org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase")) {
                                    Long groupId = ((GroupActionCase) (action.getAction())).getGroupAction().getGroupId();
                                    ListenableFuture<?> future =
                                        groupFutures.get(groupId);
                                    if (future == null) {
                                        okToInstall = false;
                                        break;
                                    }

                                    // Need to ensure that the group specified
                                    // by group-action is already installed.
                                    awaitGroup(sNode, future);
                                }
                            }
                            if (!okToInstall) {
                                //increment retry counter value
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

            /* installation of suspected groups*/
                if (!toBeInstalledGroups.isEmpty()) {
                    for (Group group : toBeInstalledGroups) {
                        LOG.debug("Installing the group {} finally although the port is not up after checking for {} times "
                                , group.getGroupId().toString(), provider.getReconciliationRetryCount());
                        addGroup(groupFutures, group);
                    }
                }
            /* Meters */
                List<Meter> meters = flowNode.get().getMeter() != null
                        ? flowNode.get().getMeter() : Collections.<Meter>emptyList();
                for (Meter meter : meters) {
                    final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent =
                            nodeIdentity.child(Meter.class, meter.getKey());
                    provider.getMeterCommiter().add(meterIdent, meter, nodeIdentity);
                }

                // Need to wait for all groups to be installed before adding
                // flows.
                awaitGroups(sNode, groupFutures.values());

            /* Flows */
                List<Table> tables = flowNode.get().getTable() != null
                        ? flowNode.get().getTable() : Collections.<Table>emptyList();
                for (Table table : tables) {
                    final KeyedInstanceIdentifier<Table, TableKey> tableIdent =
                            nodeIdentity.child(Table.class, table.getKey());
                    List<Flow> flows = table.getFlow() != null ? table.getFlow() : Collections.<Flow>emptyList();
                    for (Flow flow : flows) {
                        final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent =
                                tableIdent.child(Flow.class, flow.getKey());
                        provider.getFlowCommiter().add(flowIdent, flow, nodeIdentity);
                    }
                }
            }
        /* clean transaction */
            trans.close();
        }

        /**
         * Invoke add-group RPC, and put listenable future associated with the
         * RPC into the given map.
         *
         * @param map        The map to store listenable futures associated with
         *                   add-group RPC.
         * @param group      The group to add.
         */
        private void addGroup(Map<Long, ListenableFuture<?>> map, Group group) {
            KeyedInstanceIdentifier<Group, GroupKey> groupIdent =
                nodeIdentity.child(Group.class, group.getKey());
            final Long groupId = group.getGroupId().getValue();
            ListenableFuture<?> future = JdkFutureAdapters.listenInPoolThread(
                provider.getGroupCommiter().add(
                    groupIdent, group, nodeIdentity));

            Futures.addCallback(future, new FutureCallback<Object>() {
                @Override
                public void onSuccess(Object result) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("add-group RPC completed: node={}, id={}",
                                  nodeIdentity.firstKeyOf(Node.class).getId().
                                  getValue(), groupId);
                    }
                }

                @Override
                public void onFailure(Throwable cause) {
                    String msg = "add-group RPC failed: node=" +
                        nodeIdentity.firstKeyOf(Node.class).getId().getValue() +
                        ", id=" + groupId;
                    LOG.error(msg, cause);
                }
            });

            map.put(groupId, future);
        }

        /**
         * Wait for completion of add-group RPC.
         *
         * @param nodeId  The identifier for the target node.
         * @param future  Future associated with add-group RPC that installs
         *                the target group.
         */
        private void awaitGroup(String nodeId, ListenableFuture<?> future) {
            awaitGroups(nodeId, Collections.singleton(future));
        }

        /**
         * Wait for completion of add-group RPCs.
         *
         * @param nodeId   The identifier for the target node.
         * @param futures  A collection of futures associated with add-group
         *                 RPCs.
         */
        private void awaitGroups(String nodeId,
                                 Collection<ListenableFuture<?>> futures) {
            if (!futures.isEmpty()) {
                long timeout = Math.min(
                    ADD_GROUP_TIMEOUT * futures.size(), MAX_ADD_GROUP_TIMEOUT);
                try {
                    Futures.successfulAsList(futures).
                        get(timeout, TimeUnit.NANOSECONDS);
                    LOG.trace("awaitGroups() completed: node={}", nodeId);
                } catch (TimeoutException e) {
                    LOG.warn("add-group RPCs did not complete: node={}",
                             nodeId);
                } catch (Exception e) {
                    LOG.error("Unhandled exception while waiting for group installation on node {}",
                              nodeId, e);
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


        ReadOnlyTransaction trans = provider.getReadTranaction();
        Optional<FlowCapableNode> flowNode = Optional.absent();

        try {
            flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdent).get();
        }
        catch (Exception e) {
            LOG.error("Reconciliation Pre-Processing Fail with read Config/DS for Node {} !", nodeIdent, e);
        }

        if (flowNode.isPresent()) {

            LOG.debug("Proceeding with deletion of stale-marked Flows on switch {} using Openflow interface",
                    nodeIdent.toString());
            /* Stale-Flows - Stale-marked Flows have to be removed first for safety */
            List<Table> tables = flowNode.get().getTable() != null
                    ? flowNode.get().getTable() : Collections.<Table> emptyList();
            for (Table table : tables) {
                final KeyedInstanceIdentifier<Table, TableKey> tableIdent =
                        nodeIdent.child(Table.class, table.getKey());
                List<StaleFlow> staleFlows = table.getStaleFlow() != null ? table.getStaleFlow() : Collections.<StaleFlow> emptyList();
                for (StaleFlow staleFlow : staleFlows) {

                    FlowBuilder flowBuilder = new FlowBuilder(staleFlow);
                    Flow toBeDeletedFlow = flowBuilder.setId(staleFlow.getId()).build();

                    final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent =
                            tableIdent.child(Flow.class, toBeDeletedFlow.getKey());


                    this.provider.getFlowCommiter().remove(flowIdent, toBeDeletedFlow, nodeIdent);

                    staleFlowsToBeBulkDeleted.add(getStaleFlowInstanceIdentifier(staleFlow, nodeIdent));
                }
            }


            LOG.debug("Proceeding with deletion of stale-marked Groups for switch {} using Openflow interface",
                    nodeIdent.toString());

            // TODO: Should we collate the futures of RPC-calls to be sure that groups are Flows are fully deleted
            // before attempting to delete groups - just in case there are references

            /* Stale-marked Groups - Can be deleted after flows */
            List<StaleGroup> staleGroups = flowNode.get().getStaleGroup() != null
                    ? flowNode.get().getStaleGroup() : Collections.<StaleGroup> emptyList();
            for (StaleGroup staleGroup : staleGroups) {

                GroupBuilder groupBuilder = new GroupBuilder(staleGroup);
                Group toBeDeletedGroup = groupBuilder.setGroupId(staleGroup.getGroupId()).build();

                final KeyedInstanceIdentifier<Group, GroupKey> groupIdent =
                        nodeIdent.child(Group.class, toBeDeletedGroup.getKey());

                this.provider.getGroupCommiter().remove(groupIdent, toBeDeletedGroup, nodeIdent);

                staleGroupsToBeBulkDeleted.add(getStaleGroupInstanceIdentifier(staleGroup, nodeIdent));
            }

            LOG.debug("Proceeding with deletion of stale-marked Meters for switch {} using Openflow interface",
                    nodeIdent.toString());
            /* Stale-marked Meters - can be deleted anytime - so least priority */
            List<StaleMeter> staleMeters = flowNode.get().getStaleMeter() != null
                    ? flowNode.get().getStaleMeter() : Collections.<StaleMeter> emptyList();

            for (StaleMeter staleMeter : staleMeters) {

                MeterBuilder meterBuilder = new MeterBuilder(staleMeter);
                Meter toBeDeletedMeter = meterBuilder.setMeterId(staleMeter.getMeterId()).build();

                final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent =
                        nodeIdent.child(Meter.class, toBeDeletedMeter.getKey());


                this.provider.getMeterCommiter().remove(meterIdent, toBeDeletedMeter, nodeIdent);

                staleMetersToBeBulkDeleted.add(getStaleMeterInstanceIdentifier(staleMeter, nodeIdent));
            }

        }
        /* clean transaction */
        trans.close();

        LOG.debug("Deleting all stale-marked flows/groups/meters of for switch {} in Configuration DS",
                nodeIdent.toString());
                // Now, do the bulk deletions
                deleteDSStaleFlows(staleFlowsToBeBulkDeleted);
        deleteDSStaleGroups(staleGroupsToBeBulkDeleted);
        deleteDSStaleMeters(staleMetersToBeBulkDeleted);

    }


    private void deleteDSStaleFlows(List<InstanceIdentifier<StaleFlow>> flowsForBulkDelete){
        ImmutableList.Builder<InstanceIdentifier<StaleFlow>> builder = ImmutableList.builder();

        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        for (InstanceIdentifier<StaleFlow> staleFlowIId : flowsForBulkDelete){
            writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, staleFlowIId);
        }

        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTransaction.submit();
        handleStaleEntityDeletionResultFuture(submitFuture);
    }

    private void deleteDSStaleGroups(List<InstanceIdentifier<StaleGroup>> groupsForBulkDelete){
        ImmutableList.Builder<InstanceIdentifier<StaleGroup>> builder = ImmutableList.builder();

        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        for (InstanceIdentifier<StaleGroup> staleGroupIId : groupsForBulkDelete){
            writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, staleGroupIId);
        }

        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTransaction.submit();
        handleStaleEntityDeletionResultFuture(submitFuture);

    }

    private void deleteDSStaleMeters(List<InstanceIdentifier<StaleMeter>> metersForBulkDelete){
        ImmutableList.Builder<InstanceIdentifier<StaleMeter>> builder = ImmutableList.builder();

        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        for (InstanceIdentifier<StaleMeter> staleMeterIId : metersForBulkDelete){
            writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, staleMeterIId);
        }

        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTransaction.submit();
        handleStaleEntityDeletionResultFuture(submitFuture);


    }


    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlow> getStaleFlowInstanceIdentifier(StaleFlow staleFlow, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent
                .child(Table.class, new TableKey(staleFlow.getTableId()))
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlow.class,
                        new StaleFlowKey(new FlowId(staleFlow.getId())));
    }

    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroup> getStaleGroupInstanceIdentifier(StaleGroup staleGroup, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent
                .child(StaleGroup.class, new StaleGroupKey(new GroupId(staleGroup.getGroupId())));
    }


    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeter> getStaleMeterInstanceIdentifier(StaleMeter staleMeter, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent
                .child(StaleMeter.class, new StaleMeterKey(new MeterId(staleMeter.getMeterId())));
    }


    private void handleStaleEntityDeletionResultFuture(CheckedFuture<Void, TransactionCommitFailedException> submitFuture) {
        Futures.addCallback(submitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                LOG.debug("Stale entity removal success");
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("Stale entity removal failed {}", t);
            }
        });
    }
}

