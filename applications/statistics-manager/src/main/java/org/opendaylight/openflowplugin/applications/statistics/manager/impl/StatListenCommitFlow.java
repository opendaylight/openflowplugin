/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.statistics.manager.impl;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatNodeRegistration;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatRpcMsgManager.TransactionCacheContainer;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatisticsManager;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatisticsManager.StatDataStoreOperation;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatisticsManager.StatDataStoreOperation.StatsManagerOperationType;
import org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper.FlowComparator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowHashIdMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowHashIdMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.nodes.node.table.FlowHashIdMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.nodes.node.table.FlowHashIdMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.nodes.node.table.FlowHashIdMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.aggregate.flow.statistics.AggregateFlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionAware;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * statistics-manager
 * org.opendaylight.openflowplugin.applications.statistics.manager.impl
 *
 * StatListenCommitFlow
 * Class is a NotifyListener for FlowStatistics and DataTreeChangeListener for Config/DataStore for Flow node.
 * All expected (registered) FlowStatistics will be builded and commit to Operational/DataStore.
 * DataTreeModification should call create/delete Flow in Operational/DS create process needs to pair
 * Device Flow HashCode and FlowId from Config/DS
 */
public class StatListenCommitFlow extends StatAbstractListenCommit<Flow, OpendaylightFlowStatisticsListener>
                                            implements OpendaylightFlowStatisticsListener {

    protected static final Logger LOG = LoggerFactory.getLogger(StatListenCommitFlow.class);

    private static final String ALIEN_SYSTEM_FLOW_ID = "#UF$TABLE*";

    private static final Integer REMOVE_AFTER_MISSING_COLLECTION = 1;
    private static final int TRUNCATED_LOG_MESSAGE_LENGTH = 30;

    private final AtomicInteger unaccountedFlowsCounter = new AtomicInteger(0);

    public StatListenCommitFlow (final StatisticsManager manager, final DataBroker db,
            final NotificationProviderService nps,
                                 final StatNodeRegistration nrm){
        super(manager, db, nps, Flow.class,nrm);
    }

    @Override
    protected OpendaylightFlowStatisticsListener getStatNotificationListener() {
        return this;
    }

    @Override
    protected InstanceIdentifier<Flow> getWildCardedRegistrationPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class)
                .augmentation(FlowCapableNode.class).child(Table.class).child(Flow.class);
    }

    @Override
    public void onAggregateFlowStatisticsUpdate(final AggregateFlowStatisticsUpdate notification) {
        final TransactionId transId = notification.getTransactionId();
        final NodeId nodeId = notification.getId();
        if ( ! isExpectedStatistics(transId, nodeId)) {
            LOG.debug("STAT-MANAGER - AggregateFlowStatisticsUpdate: unregistred notification detect TransactionId {}", transId);
            return;
        }
        manager.getRpcMsgManager().addNotification(notification, nodeId);
        if (notification.isMoreReplies()) {
            return;
        }
        /* check flow Capable Node and write statistics */
        manager.enqueue(new StatDataStoreOperation(StatsManagerOperationType.DATA_COMMIT_OPER_DS,nodeId) {
            @Override
            public void applyOperation(final ReadWriteTransaction tx) {

                final Optional<TransactionCacheContainer<?>> txContainer = getTransactionCacheContainer(transId, nodeId);
                if (( ! txContainer.isPresent()) || txContainer.get().getNotifications() == null) {
                    return;
                }
                final Optional<? extends DataObject> inputObj = txContainer.get().getConfInput();
                if (( ! inputObj.isPresent()) || ( ! (inputObj.get() instanceof Table))) {
                    return;
                }

                if(!nodeRegistrationManager.isFlowCapableNodeOwner(nodeId)) { return; }

                final Table table = (Table) inputObj.get();
                final List<? extends TransactionAware> cacheNotifs = txContainer.get().getNotifications();
                for (final TransactionAware notif : cacheNotifs) {
                    if (notif instanceof AggregateFlowStatisticsUpdate) {
                        final AggregateFlowStatisticsData stats = new AggregateFlowStatisticsDataBuilder()
                            .setAggregateFlowStatistics(new AggregateFlowStatisticsBuilder(notification).build()).build();
                        final InstanceIdentifier<FlowCapableNode> fNodeIdent = InstanceIdentifier.create(Nodes.class)
                                .child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class);
                        final InstanceIdentifier<Table> tableRef = fNodeIdent.child(Table.class, table.getKey());
                        final InstanceIdentifier<AggregateFlowStatisticsData> tableStatRef = tableRef
                                .augmentation(AggregateFlowStatisticsData.class);
                        Optional<FlowCapableNode> fNode = Optional.absent();
                        try {
                            fNode = tx.read(LogicalDatastoreType.OPERATIONAL, fNodeIdent).checkedGet();
                        } catch (final ReadFailedException e) {
                            LOG.debug("Read Operational/DS for FlowCapableNode fail! {}", fNodeIdent, e);
                            return;
                        }
                        if (fNode.isPresent()) {
                            ensureTable(tx, table.getId(), tableRef);
                            tx.put(LogicalDatastoreType.OPERATIONAL, tableStatRef, stats);
                        }
                    }
                }
            }

            @Override
            public UUID generatedUUIDForNode() {
                return manager.getGeneratedUUIDForNode(getNodeIdentifier());
            }
        });
    }

    public void ensureTable(final ReadWriteTransaction tx, final Short tableId, final InstanceIdentifier<Table> tableRef) {
        final Table tableNew = new TableBuilder().setId(tableId).build();
        tx.merge(LogicalDatastoreType.OPERATIONAL, tableRef, tableNew);
    }

    protected void processDataChange(Collection<DataTreeModification<Flow>> changes) {
        if (!changes.isEmpty()) {
            for (DataTreeModification<Flow> dataChange : changes) {
                if (dataChange.getRootNode().getModificationType() == DataObjectModification.ModificationType.DELETE) {
                    final InstanceIdentifier<Node> nodeIdent = dataChange.getRootPath().getRootIdentifier()
                            .firstIdentifierOf(Node.class);
                    if (!removedDataBetweenStatsCycle.containsKey(nodeIdent)) {
                        removedDataBetweenStatsCycle.put(nodeIdent, new ArrayList<>());
                    }
                    Flow data = dataChange.getRootNode().getDataBefore();
                    removedDataBetweenStatsCycle.get(nodeIdent).add(data);
                    LOG.debug("Node: {} :: Flow removed {}",nodeIdent.firstKeyOf(Node.class).getId(), data.toString());
                }
            }
        }
    }

    @Override
    public void onFlowsStatisticsUpdate(final FlowsStatisticsUpdate notification) {
        final TransactionId transId = notification.getTransactionId();
        final NodeId nodeId = notification.getId();
        if ( ! isExpectedStatistics(transId, nodeId)) {
            LOG.debug("STAT-MANAGER - FlowsStatisticsUpdate: unregistered notification detect TransactionId {}",
                    transId);
            return;
        }
        manager.getRpcMsgManager().addNotification(notification, nodeId);
        if (notification.isMoreReplies()) {
            LOG.trace("Next notification for join txId {}", transId);
            return;
        }
        /* add flow's statistics */
        manager.enqueue(new StatDataStoreOperation(StatsManagerOperationType.DATA_COMMIT_OPER_DS,nodeId) {
            @Override
            public void applyOperation(final ReadWriteTransaction tx) {
                final Optional<TransactionCacheContainer<?>> txContainer = getTransactionCacheContainer(transId, nodeId);
                if (( ! txContainer.isPresent()) || txContainer.get().getNotifications() == null) {
                    return;
                }
                if(!nodeRegistrationManager.isFlowCapableNodeOwner(nodeId)) { return; }

                final List<FlowAndStatisticsMapList> flowStats = new ArrayList<FlowAndStatisticsMapList>(10);
                final InstanceIdentifier<Node> nodeIdent = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, new NodeKey(nodeId));
                final List<? extends TransactionAware> cacheNotifs = txContainer.get().getNotifications();
                for (final TransactionAware notif : cacheNotifs) {
                    if (notif instanceof FlowsStatisticsUpdate) {
                        final List<FlowAndStatisticsMapList> notifList =
                                ((FlowsStatisticsUpdate) notif).getFlowAndStatisticsMapList();
                        if (notifList != null) {
                            flowStats.addAll(notifList);
                        }
                    }
                }

                statsFlowCommitAll(flowStats, nodeIdent, tx);
                /* cleaning all not cached hash collisions */
                final Map<InstanceIdentifier<Flow>, Integer> listAliens = mapNodesForDelete.get(nodeIdent);
                if (listAliens != null) {
                    for (final Entry<InstanceIdentifier<Flow>, Integer> nodeForDelete : listAliens.entrySet()) {
                        final Integer lifeIndex = nodeForDelete.getValue();
                        if (nodeForDelete.getValue() > 0) {
                            nodeForDelete.setValue(Integer.valueOf(lifeIndex.intValue() - 1));
                        } else {
                            final InstanceIdentifier<Flow> flowNodeIdent = nodeForDelete.getKey();
                            mapNodesForDelete.get(nodeIdent).remove(flowNodeIdent);
                            tx.delete(LogicalDatastoreType.OPERATIONAL, flowNodeIdent);
                        }
                    }
                }
                /* Notification for continue collecting statistics */
                notifyToCollectNextStatistics(nodeIdent, transId);
            }

            @Override
            public UUID generatedUUIDForNode() {
                return manager.getGeneratedUUIDForNode(getNodeIdentifier());
            }

        });
    }

    private void statsFlowCommitAll(final List<FlowAndStatisticsMapList> list,
            final InstanceIdentifier<Node> nodeIdent, final ReadWriteTransaction tx) {

        final InstanceIdentifier<FlowCapableNode> fNodeIdent = nodeIdent.augmentation(FlowCapableNode.class);

        //cleanup the hashmap ID for the flows deleted between two stats cycle, also cleanup the
        // data change cache as well.
        ArrayList<Flow> deletedFlows = removedDataBetweenStatsCycle.remove(nodeIdent);

        if (deletedFlows != null && !deletedFlows.isEmpty()) {
            LOG.trace("Number of flows deleted from node {} between two stats cycles are {}", nodeIdent, deletedFlows
                    .size());
        }

        final Optional<FlowCapableNode> fNode;
        try {
            fNode = tx.read(LogicalDatastoreType.OPERATIONAL, fNodeIdent).checkedGet();
        }
        catch (final ReadFailedException e) {
            LOG.debug("Read FlowCapableNode {} in Operational/DS fail! Statistic scan not be updated.", nodeIdent, e);
            return;
        }
        if ( ! fNode.isPresent()) {
            LOG.trace("FlowCapableNode {} is not presented in Operational/DS. Statisticscan not be updated.", nodeIdent);
            return;
        }

        final NodeUpdateState nodeState = new NodeUpdateState(fNodeIdent,fNode.get());

        for (final FlowAndStatisticsMapList flowStat : list) {
            final TableKey tableKey = new TableKey(flowStat.getTableId());
            final TableFlowUpdateState tableState = nodeState.getTable(tableKey, tx);
            Flow removedConfigFlow = getFlowIfRemoved(flowStat, deletedFlows);
            if (removedConfigFlow == null) {
                tableState.reportFlow(flowStat,tx, false);
            } else {
                deletedFlows.remove(removedConfigFlow);
                tableState.reportFlow(flowStat,tx, true);
            }
        }

        if (deletedFlows != null ) {
            deletedFlows.clear();
        }
        for (final TableFlowUpdateState table : nodeState.getTables()) {
            table.removeUnreportedFlows(tx);
        }
    }

    private Flow getFlowIfRemoved(FlowAndStatisticsMapList flowStat, ArrayList<Flow> deletedFlows) {
        if (deletedFlows != null && !deletedFlows.isEmpty()) {
            for (Flow flow : deletedFlows) {
                final FlowAndStatisticsMapList configFlowStats = new FlowAndStatisticsMapListBuilder(flow).build();
                if ( flowStat.getMatch().equals(configFlowStats.getMatch()) &&
                        flowStat.getPriority().equals(configFlowStats.getPriority()) &&
                        flowStat.getCookie().equals(configFlowStats.getCookie()!=null?configFlowStats.getCookie():
                                new FlowCookie(new BigInteger("0")))) {
                    LOG.debug("Flow statistics {} are related to flow {}, but it's REMOVED from the config data store" +
                            "store", flowStat, flow);
                    return flow;
                }
            }
        }
        return null;
    }
    /**
     * Method adds statistics to Flow
     *
     * @param flowBuilder
     * @param deviceFlow
     */
    private void addStatistics(final FlowBuilder flowBuilder, final FlowAndStatisticsMapList deviceFlow) {
        final FlowAndStatisticsMapListBuilder stats = new FlowAndStatisticsMapListBuilder(deviceFlow);
        final FlowStatisticsBuilder flowStatisticsBuilder = new FlowStatisticsBuilder(stats.build());
        final FlowStatisticsDataBuilder flowStatisticsData =new FlowStatisticsDataBuilder();
        flowStatisticsData.setFlowStatistics(flowStatisticsBuilder.build());
        flowBuilder.addAugmentation(FlowStatisticsData.class, flowStatisticsData.build());
    }

    /**
     * build pseudoUnique hashCode for flow in table
     * for future easy identification
     *
     * FIXME: we expect same version for YANG models for all clusters and that has to be fix
     * FIXME: CREATE BETTER KEY - for flow (MATCH is the problem)
     */
    static String buildFlowIdOperKey(final FlowAndStatisticsMapList deviceFlow) {
        return new StringBuilder().append(deviceFlow.getMatch())
                .append(deviceFlow.getPriority()).append(deviceFlow.getCookie().getValue()).toString();
    }

    private class NodeUpdateState {
        private final InstanceIdentifier<FlowCapableNode> nodeIdentifier;
        private final Map<TableKey,TableFlowUpdateState> tables = new HashMap<>();

        public NodeUpdateState(final InstanceIdentifier<FlowCapableNode> fNodeIdent, final FlowCapableNode flowCapableNode) {
            nodeIdentifier = fNodeIdent;
            final List<Table> tableList = flowCapableNode.getTable();
            if(tableList != null) {
            for (final Table table : tableList) {
                final TableKey tableKey = table.getKey();
                    tables.put(tableKey, new TableFlowUpdateState(nodeIdentifier.child(Table.class,tableKey),table));
                }
            }
        }

        public Iterable<TableFlowUpdateState> getTables() {
            return tables.values();
        }

        TableFlowUpdateState getTable(final TableKey key,final ReadWriteTransaction tx) {
            TableFlowUpdateState table = tables.get(key);
            if(table == null) {
                table = new TableFlowUpdateState(nodeIdentifier.child(Table.class, key), null);
                tables.put(key, table);
            }
            return table;
        }
    }

    private class TableFlowUpdateState {

        private boolean tableEnsured = false;
        final KeyedInstanceIdentifier<Table, TableKey> tableRef;
        final TableKey tableKey;
        final BiMap<FlowHashIdMapKey, FlowId> flowIdByHash;
        List<Flow> configFlows;

        public TableFlowUpdateState(final KeyedInstanceIdentifier<Table, TableKey> tablePath, final Table table) {
            tableRef = tablePath;
            tableKey = tablePath.getKey();
            flowIdByHash = HashBiMap.create();
            if(table != null) {
                final FlowHashIdMapping flowHashMapping = table.getAugmentation(FlowHashIdMapping.class);
                if (flowHashMapping != null) {
                    final List<FlowHashIdMap>  flowHashMap = flowHashMapping.getFlowHashIdMap() != null
                            ? flowHashMapping.getFlowHashIdMap() : Collections.<FlowHashIdMap> emptyList();
                    for (final FlowHashIdMap flowHashId : flowHashMap) {
                        try {
                            flowIdByHash.put(flowHashId.getKey(), flowHashId.getFlowId());
                        } catch (final Exception e) {
                            //flowHashId.getKey() too verbose for standard log.
                            if(LOG.isDebugEnabled()) {
                                final FlowId currData = flowIdByHash.get(flowHashId.getKey());
                                LOG.debug("Flow hashing hit a duplicate for {} -> {}. Curr value: {} Equals:{}. " +
                                        "Exception was raised:",
                                    flowHashId.getKey(), flowHashId.getFlowId(), currData, flowHashId.getFlowId().equals(currData), e);
                            }
                            else
                            {
                                LOG.warn("Flow hashing hit a duplicate {}. Exception was raised: {}. Enable DEBUG for" +
                                        " more detail.",
                                    flowHashId.getFlowId().toString().substring(0, Math.min(TRUNCATED_LOG_MESSAGE_LENGTH,flowHashId.getFlowId().toString().length())),
                                    e.getMessage().substring(0,Math.min(TRUNCATED_LOG_MESSAGE_LENGTH,e.getMessage().length())));
                            }
                        }
                    }
                }
            }
        }

        private void ensureTableFowHashIdMapping(final ReadWriteTransaction tx) {
            if( ! tableEnsured) {
                ensureTable(tx, tableKey.getId(), tableRef);
                final FlowHashIdMapping emptyMapping = new FlowHashIdMappingBuilder()
                    .setFlowHashIdMap(Collections.<FlowHashIdMap> emptyList()).build();
                tx.merge(LogicalDatastoreType.OPERATIONAL, tableRef.augmentation(FlowHashIdMapping.class), emptyMapping);
                tableEnsured = true;
            }
        }

        private FlowKey searchInConfiguration(final FlowAndStatisticsMapList flowStat, final ReadWriteTransaction trans) {
            initConfigFlows();
            final Iterator<Flow> it = configFlows.iterator();
            while(it.hasNext()) {
                final Flow cfgFlow = it.next();
                final FlowKey cfgKey = cfgFlow.getKey();
                final FlowId cfgFlowId = cfgKey.getId();

                if(! flowIdByHash.inverse().containsKey(cfgFlowId)) {
                    if(FlowComparator.flowEquals(flowStat, cfgFlow)) {
                        return cfgKey;
                    }
                }
            }
            return null;
        }

        private void initConfigFlows() {
            final Optional<Table> table = readLatestConfiguration(tableRef);
            List<Flow> localList = null;
            if(table.isPresent()) {
                localList = table.get().getFlow();
            }
            if(localList == null) {
                configFlows = Collections.emptyList();
            } else {
                configFlows = new ArrayList<>(localList);
            }
        }

        private FlowKey getFlowKeyByHash(final FlowHashIdMapKey key) {
            final FlowId ret = flowIdByHash.get(key);
            if(ret != null) {
                return new FlowKey(ret);
            }
            return null;
        }

        /* Returns FlowKey which doesn't exist in any DataStore for now */
        private FlowKey makeAlienFlowKey() {
            final StringBuilder sBuilder = new StringBuilder(ALIEN_SYSTEM_FLOW_ID)
                .append(tableKey.getId()).append("-").append(unaccountedFlowsCounter.incrementAndGet());
            final FlowId flowId = new FlowId(sBuilder.toString());
            return new FlowKey(flowId);
        }

        private Map<FlowHashIdMapKey, FlowId> getRemovalList() {
            return flowIdByHash;
        }

        void reportFlow(final FlowAndStatisticsMapList flowStat,
                        final ReadWriteTransaction trans,
                        boolean wasRemoved) {
            ensureTableFowHashIdMapping(trans);
            final FlowHashIdMapKey hashingKey = new FlowHashIdMapKey(buildFlowIdOperKey(flowStat));
            FlowKey flowKey = getFlowKeyByHash(hashingKey);
            if (flowKey == null || wasRemoved) {
                flowKey = searchInConfiguration(flowStat, trans);
                if ( flowKey == null) {
                    flowKey = makeAlienFlowKey();
                }
                updateHashCache(trans,flowKey,hashingKey);
            } else {
                flowIdByHash.remove(hashingKey);
            }
            final FlowBuilder flowBuilder = new FlowBuilder(flowStat);
            flowBuilder.setKey(flowKey);
            addStatistics(flowBuilder, flowStat);
            final InstanceIdentifier<Flow> flowIdent = tableRef.child(Flow.class, flowKey);
            trans.put(LogicalDatastoreType.OPERATIONAL, flowIdent, flowBuilder.build());
            /* check life for Alien flows */
            if (flowKey.getId().getValue().startsWith(ALIEN_SYSTEM_FLOW_ID)) {
                removeData(flowIdent, REMOVE_AFTER_MISSING_COLLECTION);
            }
        }

        /* Build and deploy new FlowHashId map */
        private void updateHashCache(final ReadWriteTransaction trans, final FlowKey flowKey, final FlowHashIdMapKey hashingKey) {
            final FlowHashIdMapBuilder flHashIdMap = new FlowHashIdMapBuilder();
            flHashIdMap.setFlowId(flowKey.getId());
            flHashIdMap.setKey(hashingKey);
            final KeyedInstanceIdentifier<FlowHashIdMap, FlowHashIdMapKey> flHashIdent = tableRef
                    .augmentation(FlowHashIdMapping.class).child(FlowHashIdMap.class, hashingKey);
            /* Add new FlowHashIdMap */
            trans.put(LogicalDatastoreType.OPERATIONAL, flHashIdent, flHashIdMap.build());
        }

        void removeUnreportedFlows(final ReadWriteTransaction tx) {
            final InstanceIdentifier<Node> nodeIdent = tableRef.firstIdentifierOf(Node.class);
            final List<InstanceIdentifier<Flow>> listMissingConfigFlows = notStatReportedConfigFlows();
            final Map<InstanceIdentifier<Flow>, Integer> nodeDeleteMap = mapNodesForDelete.get(nodeIdent);
            final Map<FlowHashIdMapKey, FlowId> listForRemove = getRemovalList();
            for (final Entry<FlowHashIdMapKey, FlowId> entryForRemove : listForRemove.entrySet()) {
                final FlowKey flowKey = new FlowKey(entryForRemove.getValue());
                final InstanceIdentifier<Flow> flowRef = tableRef.child(Flow.class, flowKey);
                if (nodeDeleteMap != null && flowKey.getId().getValue().startsWith(ALIEN_SYSTEM_FLOW_ID)) {
                    final Integer lifeIndex = nodeDeleteMap.get(flowRef);
                     if (lifeIndex != null && lifeIndex > 0) {
                        break;
                    } else {
                        nodeDeleteMap.remove(flowRef);
                    }
                } else {
                    if (listMissingConfigFlows.remove(flowRef)) {
                        // it is probable that some multipart message was lost
                        break;
                    }
                }
                final InstanceIdentifier<FlowHashIdMap> flHashIdent =
                        tableRef.augmentation(FlowHashIdMapping.class).child(FlowHashIdMap.class, entryForRemove.getKey());
                tx.delete(LogicalDatastoreType.OPERATIONAL, flowRef);
                tx.delete(LogicalDatastoreType.OPERATIONAL, flHashIdent);
            }
        }

        List<InstanceIdentifier<Flow>> notStatReportedConfigFlows() {
            if (configFlows != null) {
                final List<InstanceIdentifier<Flow>> returnList = new ArrayList<>(configFlows.size());
                for (final Flow confFlow : configFlows) {
                    final InstanceIdentifier<Flow> confFlowIdent = tableRef.child(Flow.class, confFlow.getKey());
                    returnList.add(confFlowIdent);
                }
                return returnList;
            }
            return Collections.emptyList();
        }
    }
}

