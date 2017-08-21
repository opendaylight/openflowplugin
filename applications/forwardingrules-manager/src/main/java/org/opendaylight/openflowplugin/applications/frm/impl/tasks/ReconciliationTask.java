/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl.tasks;

import com.google.common.base.Optional;
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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconciliationTask implements Callable<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationTask.class);

    //The number of nanoseconds to wait for a single group to be added.
    private static final long  ADD_GROUP_TIMEOUT = TimeUnit.SECONDS.toNanos(3);

    //The maximum number of nanoseconds to wait for completion of add-group RPCs.
    private static final long  MAX_ADD_GROUP_TIMEOUT = TimeUnit.SECONDS.toNanos(20);

    private static final String SEPARATOR = ":";
    private final ForwardingRulesManager provider;
    private final InstanceIdentifier<FlowCapableNode> nodeIdent;

    public ReconciliationTask(final ForwardingRulesManager provider,
                              final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        this.provider = provider;
        this.nodeIdent = nodeIdent;
    }

    public Boolean call() {
        String sNode = nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue();
        BigInteger nDpId = getDpnIdFromNodeName(sNode);

        ReadOnlyTransaction trans = provider.getReadTransaction();
        Optional<FlowCapableNode> flowNode;
        //initialize the counter
        int counter = 0;
        try {
            flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdent).get();
        } catch (Exception e) {
            LOG.warn("Fail with read Config/DS for Node {} !", nodeIdent, e);
            return false;
        }

        if (flowNode.isPresent()) {
            /* Tables - have to be pushed before groups */
            // CHECK if while pusing the update, updateTableInput can be null to emulate a table add
            List<TableFeatures> tableList = flowNode.get().getTableFeatures() != null
                    ? flowNode.get().getTableFeatures() : Collections.<TableFeatures>emptyList();
            for (TableFeatures tableFeaturesItem : tableList) {
                TableFeaturesKey tableKey = tableFeaturesItem.getKey();
                KeyedInstanceIdentifier<TableFeatures, TableFeaturesKey> tableFeaturesII
                        = nodeIdent.child(TableFeatures.class, new TableFeaturesKey(tableKey.getTableId()));
                provider.getTableFeaturesCommiter().update(tableFeaturesII, tableFeaturesItem, null, nodeIdent);
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
                        nodeIdent.child(Meter.class, meter.getKey());
                provider.getMeterCommiter().add(meterIdent, meter, nodeIdent);
            }

            // Need to wait for all groups to be installed before adding
            // flows.
            awaitGroups(sNode, groupFutures.values());

            /* Flows */
            List<Table> tables = flowNode.get().getTable() != null
                    ? flowNode.get().getTable() : Collections.<Table>emptyList();
            for (Table table : tables) {
                final KeyedInstanceIdentifier<Table, TableKey> tableIdent =
                        nodeIdent.child(Table.class, table.getKey());
                List<Flow> flows = table.getFlow() != null ? table.getFlow() : Collections.<Flow>emptyList();
                for (Flow flow : flows) {
                    final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent =
                            tableIdent.child(Flow.class, flow.getKey());
                    provider.getFlowCommiter().add(flowIdent, flow, nodeIdent);
                }
            }
        }
        /* clean transaction */
        trans.close();
        return true;
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
                nodeIdent.child(Group.class, group.getKey());
        final Long groupId = group.getGroupId().getValue();
        ListenableFuture<?> future = JdkFutureAdapters.listenInPoolThread(
                provider.getGroupCommiter().add(
                        groupIdent, group, nodeIdent));

        Futures.addCallback(future, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("add-group RPC completed: node={}, id={}",
                            nodeIdent.firstKeyOf(Node.class).getId().
                                    getValue(), groupId);
                }
            }

            @Override
            public void onFailure(Throwable cause) {
                String msg = "add-group RPC failed: node=" +
                        nodeIdent.firstKeyOf(Node.class).getId().getValue() +
                        ", id=" + groupId;
                LOG.debug(msg, cause);
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
                LOG.debug("add-group RPCs did not complete: node={}",
                        nodeId);
            } catch (Exception e) {
                LOG.debug("Unhandled exception while waiting for group installation on node {}",
                        nodeId, e);
            }
        }
    }

    private BigInteger getDpnIdFromNodeName(String nodeName) {

        String dpId = nodeName.substring(nodeName.lastIndexOf(SEPARATOR) + 1);
        return new BigInteger(dpId);
    }
}