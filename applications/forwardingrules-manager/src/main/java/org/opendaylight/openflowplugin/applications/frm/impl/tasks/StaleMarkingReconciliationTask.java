/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl.tasks;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaleMarkingReconciliationTask implements Callable<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(StaleMarkingReconciliationTask.class);
    private final ForwardingRulesManager provider;
    private final InstanceIdentifier<FlowCapableNode> nodeIdent;

    public StaleMarkingReconciliationTask(final ForwardingRulesManager provider,
                              final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        this.provider = provider;
        this.nodeIdent = nodeIdent;
    }

    @Override
    public Boolean call() throws Exception {
        List<InstanceIdentifier<StaleFlow>> staleFlowsToBeBulkDeleted = Lists.newArrayList();
        List<InstanceIdentifier<StaleGroup>> staleGroupsToBeBulkDeleted = Lists.newArrayList();
        List<InstanceIdentifier<StaleMeter>> staleMetersToBeBulkDeleted = Lists.newArrayList();

        ReadOnlyTransaction trans = provider.getReadTransaction();
        Optional<FlowCapableNode> flowNode = Optional.absent();

        try {
            flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdent).get();
        }
        catch (Exception e) {
            LOG.warn("Reconciliation Pre-Processing Fail with read Config/DS for Node {} !", nodeIdent, e);
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
        return true;
    }

    private void deleteDSStaleFlows(List<InstanceIdentifier<StaleFlow>> flowsForBulkDelete){
        ImmutableList.Builder<InstanceIdentifier<StaleFlow>> builder = ImmutableList.builder();

        WriteTransaction writeTransaction = provider.getWriteTransaction();

        for (InstanceIdentifier<StaleFlow> staleFlowIId : flowsForBulkDelete){
            writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, staleFlowIId);
        }

        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTransaction.submit();
        handleStaleEntityDeletionResultFuture(submitFuture);
    }

    private void deleteDSStaleGroups(List<InstanceIdentifier<StaleGroup>> groupsForBulkDelete){
        ImmutableList.Builder<InstanceIdentifier<StaleGroup>> builder = ImmutableList.builder();

        WriteTransaction writeTransaction = provider.getWriteTransaction();

        for (InstanceIdentifier<StaleGroup> staleGroupIId : groupsForBulkDelete){
            writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, staleGroupIId);
        }

        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTransaction.submit();
        handleStaleEntityDeletionResultFuture(submitFuture);
    }

    private void deleteDSStaleMeters(List<InstanceIdentifier<StaleMeter>> metersForBulkDelete){
        ImmutableList.Builder<InstanceIdentifier<StaleMeter>> builder = ImmutableList.builder();

        WriteTransaction writeTransaction = provider.getWriteTransaction();

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
                LOG.debug("Stale entity removal failed {}", t);
            }
        });
    }
}