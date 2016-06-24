/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.statistics.manager.impl;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.opendaylight.controller.md.sal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatRpcMsgManager;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatisticsManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionAware;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.OpendaylightGroupStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.OpendaylightMeterStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.OpendaylightQueueStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.SettableFuture;


/**
 * statistics-manager
 * org.opendaylight.openflowplugin.applications.statistics.manager.impl
 *
 * StatRpcMsgManagerImpl
 * Class register and provide all RPC Statistics Device Services and implement pre-defined
 * wrapped methods for prepare easy access to RPC Statistics Device Services like getAllStatisticsFor...
 *
 * In next Class implement process for joining multipart messages.
 * Class internally use two WeakHashMap and GuavaCache for holding values for joining multipart msg.
 * One Weak map is used for holding all Multipart Messages and second is used for possible input
 * Config/DS light-weight DataObject (DataObject contains only necessary identification fields as
 * TableId, GroupId, MeterId or for flow Match, Priority, FlowCookie, TableId and FlowId ...
 *
 * @author avishnoi@in.ibm.com <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 */
public class StatRpcMsgManagerImpl implements StatRpcMsgManager {


    private static final Logger LOG = LoggerFactory.getLogger(StatRpcMsgManagerImpl.class);

    private final Cache<String, TransactionCacheContainer<? super TransactionAware>> txCache;

    /**
     * Cache for futures to be returned by
     * {@link #isExpectedStatistics(TransactionId, NodeId)}.
     */
    private final Cache<String, SettableFuture<Boolean>>  txFutureCache;

    /**
     * The number of seconds to wait for transaction container to be put into
     * {@link #txCache}.
     */
    private static final long TXCACHE_WAIT_TIMEOUT = 10L;

    private static final int MAX_CACHE_SIZE = 10000;

    private static final String MSG_TRANS_ID_NOT_NULL = "TransactionId can not be null!";
    private static final String MSG_NODE_ID_NOT_NULL = "NodeId can not be null!";
    private static final String MSG_NODE_REF_NOT_NULL = "NodeRef can not be null!";
    /**
     *  Number of possible statistic which are waiting for notification
     *      - check it in StatPermCollectorImpl method collectStatCrossNetwork()
     */
    private static final long POSSIBLE_STAT_WAIT_FOR_NOTIFICATION = 7;

    private final OpendaylightGroupStatisticsService groupStatsService;
    private final OpendaylightMeterStatisticsService meterStatsService;
    private final OpendaylightFlowStatisticsService flowStatsService;
    private final OpendaylightPortStatisticsService portStatsService;
    private final OpendaylightFlowTableStatisticsService flowTableStatsService;
    private final OpendaylightQueueStatisticsService queueStatsService;

    public StatRpcMsgManagerImpl (final StatisticsManager manager,
            final RpcConsumerRegistry rpcRegistry, final long maxNodeForCollector) {
        Preconditions.checkArgument(manager != null, "StatisticManager can not be null!");
        Preconditions.checkArgument(rpcRegistry != null, "RpcConsumerRegistry can not be null !");
        groupStatsService = Preconditions.checkNotNull(
                rpcRegistry.getRpcService(OpendaylightGroupStatisticsService.class),
                "OpendaylightGroupStatisticsService can not be null!");
        meterStatsService = Preconditions.checkNotNull(
                rpcRegistry.getRpcService(OpendaylightMeterStatisticsService.class),
                "OpendaylightMeterStatisticsService can not be null!");
        flowStatsService = Preconditions.checkNotNull(
                rpcRegistry.getRpcService(OpendaylightFlowStatisticsService.class),
                "OpendaylightFlowStatisticsService can not be null!");
        portStatsService = Preconditions.checkNotNull(
                rpcRegistry.getRpcService(OpendaylightPortStatisticsService.class),
                "OpendaylightPortStatisticsService can not be null!");
        flowTableStatsService = Preconditions.checkNotNull(
                rpcRegistry.getRpcService(OpendaylightFlowTableStatisticsService.class),
                "OpendaylightFlowTableStatisticsService can not be null!");
        queueStatsService = Preconditions.checkNotNull(
                rpcRegistry.getRpcService(OpendaylightQueueStatisticsService.class),
                "OpendaylightQueueStatisticsService can not be null!");

        txCache = CacheBuilder.newBuilder().expireAfterWrite((maxNodeForCollector * POSSIBLE_STAT_WAIT_FOR_NOTIFICATION), TimeUnit.SECONDS)
                .maximumSize(MAX_CACHE_SIZE).build();
        txFutureCache = CacheBuilder.newBuilder().
            expireAfterWrite(TXCACHE_WAIT_TIMEOUT, TimeUnit.SECONDS).
            maximumSize(MAX_CACHE_SIZE).build();
    }

    @Override
    public <T extends TransactionAware, D extends DataObject> void registrationRpcFutureCallBack(
            final Future<RpcResult<T>> future, final D inputObj, final NodeRef nodeRef,
            final SettableFuture<TransactionId> resultTransId) {

        class FutureCallbackImpl implements FutureCallback<RpcResult<? extends TransactionAware>> {
            @Override
            public void onSuccess(final RpcResult<? extends TransactionAware> result) {
                final TransactionId id = result.getResult().getTransactionId();
                final NodeKey nodeKey = nodeRef.getValue().firstKeyOf(Node.class, NodeKey.class);
                if (id == null) {
                    String[] multipartRequestName = result.getResult().getClass().getSimpleName().split("(?=\\p{Upper})");
                    LOG.warn("Node [{}] does not support statistics request type : {}",
                            nodeKey.getId(),Joiner.on(" ").join(Arrays.copyOfRange(multipartRequestName, 2, multipartRequestName.length-2)));
                    if (resultTransId != null) {
                        resultTransId.setException(
                            new UnsupportedOperationException());
                    }
                } else {
                    if (resultTransId != null) {
                        resultTransId.set(id);
                    }
                    final String cacheKey = buildCacheKey(id, nodeKey.getId());
                    final TransactionCacheContainer<? super TransactionAware> container =
                            new TransactionCacheContainerImpl<>(id, inputObj, nodeKey.getId());
                    putTransaction(cacheKey, container);
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("Response Registration for Statistics RPC call fail!", t);
                if (resultTransId != null) {
                    if (t instanceof DOMRpcImplementationNotAvailableException) {
                        //If encountered with RPC not availabe exception, retry till
                        // stats manager remove the node from the stats collector pool
                        resultTransId.set(StatPermCollectorImpl.getFakeTxId());
                    } else {
                        resultTransId.setException(t);
                    }
                }
            }
        }

        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(future),new FutureCallbackImpl());
    }

    private String buildCacheKey(final TransactionId id, final NodeId nodeId) {
        return String.valueOf(id.getValue()) + "-" + nodeId.getValue();
    }

    /**
     * Put the given statistics transaction container into the cache.
     *
     * @param key        Key that specifies the given transaction container.
     * @param container  Transaction container.
     */
    private synchronized void putTransaction(
        String key, TransactionCacheContainer<? super TransactionAware> container) {
        txCache.put(key, container);

        SettableFuture<Boolean> future = txFutureCache.asMap().remove(key);
        if (future != null) {
            // Wake up a thread waiting for this transaction container.
            future.set(true);
        }
    }

    /**
     * Check to see if the specified transaction container is cached in
     * {@link #txCache}.
     *
     * @param key  Key that specifies the transaction container.
     * @return  A future that will contain the result.
     */
    private synchronized Future<Boolean> isExpectedStatistics(String key) {
        Future<Boolean> future;
        TransactionCacheContainer<?> container = txCache.getIfPresent(key);
        if (container == null) {
            // Wait for the transaction container to be put into the cache.
            SettableFuture<Boolean> f = SettableFuture.<Boolean>create();
            SettableFuture<Boolean> current =
                txFutureCache.asMap().putIfAbsent(key, f);
            future = (current == null) ? f : current;
        } else {
            future = Futures.immediateFuture(Boolean.TRUE);
        }

        return future;
    }

    @Override
    public Future<Optional<TransactionCacheContainer<?>>> getTransactionCacheContainer(
            final TransactionId id, final NodeId nodeId) {
        Preconditions.checkArgument(id != null, MSG_TRANS_ID_NOT_NULL);
        Preconditions.checkArgument(nodeId != null, MSG_NODE_ID_NOT_NULL);

        String key = buildCacheKey(id, nodeId);
        Optional<TransactionCacheContainer<?>> resultContainer =
            Optional.<TransactionCacheContainer<?>> fromNullable(
                txCache.asMap().remove(key));
        if (!resultContainer.isPresent()) {
            LOG.warn("Transaction cache not found: {}", key);
        }

        return Futures.immediateFuture(resultContainer);
    }

    @Override
    public Future<Boolean> isExpectedStatistics(final TransactionId id, final NodeId nodeId) {
        Preconditions.checkArgument(id != null, MSG_TRANS_ID_NOT_NULL);
        Preconditions.checkArgument(nodeId != null, MSG_NODE_ID_NOT_NULL);

        String key = buildCacheKey(id, nodeId);
        return isExpectedStatistics(key);
    }

    @Override
    public void addNotification(final TransactionAware notification, final NodeId nodeId) {
        Preconditions.checkArgument(notification != null, "TransactionAware can not be null!");
        Preconditions.checkArgument(nodeId != null, MSG_NODE_ID_NOT_NULL);

        TransactionId txId = notification.getTransactionId();
        String key = buildCacheKey(txId, nodeId);
        TransactionCacheContainer<? super TransactionAware> container =
            txCache.getIfPresent(key);
        if (container != null) {
            container.addNotif(notification);
        } else {
            LOG.warn("Unable to add notification: {}, {}", key,
                     notification.getImplementedInterface());
        }
    }

    @Override
    public Future<TransactionId> getAllGroupsStat(final NodeRef nodeRef) {
        Preconditions.checkArgument(nodeRef != null, MSG_NODE_REF_NOT_NULL);
        SettableFuture<TransactionId> result = SettableFuture.create();
        GetAllGroupStatisticsInputBuilder builder =
            new GetAllGroupStatisticsInputBuilder();
        builder.setNode(nodeRef);
        registrationRpcFutureCallBack(
            groupStatsService.getAllGroupStatistics(builder.build()), null,
            nodeRef, result);
        return result;
    }

    @Override
    public Future<TransactionId> getAllMetersStat(final NodeRef nodeRef) {
        Preconditions.checkArgument(nodeRef != null, MSG_NODE_REF_NOT_NULL);
        SettableFuture<TransactionId> result = SettableFuture.create();
        GetAllMeterStatisticsInputBuilder builder =
            new GetAllMeterStatisticsInputBuilder();
        builder.setNode(nodeRef);
        registrationRpcFutureCallBack(
            meterStatsService.getAllMeterStatistics(builder.build()), null,
            nodeRef, result);
        return result;
    }

    @Override
    public Future<TransactionId> getAllFlowsStat(final NodeRef nodeRef) {
        Preconditions.checkArgument(nodeRef != null, MSG_NODE_REF_NOT_NULL);
        SettableFuture<TransactionId> result = SettableFuture.create();
        GetAllFlowsStatisticsFromAllFlowTablesInputBuilder builder =
            new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder();
        builder.setNode(nodeRef);
        registrationRpcFutureCallBack(
            flowStatsService.getAllFlowsStatisticsFromAllFlowTables(builder.build()),
            null, nodeRef, result);
        return result;
    }

    @Override
    public void getAggregateFlowStat(final NodeRef nodeRef, final TableId tableId) {
        Preconditions.checkArgument(nodeRef != null, MSG_NODE_REF_NOT_NULL);
        Preconditions.checkArgument(tableId != null, "TableId can not be null!");
        GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder builder =
            new GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder();
        builder.setNode(nodeRef).setTableId(tableId);

        TableBuilder tbuilder = new TableBuilder().
            setId(tableId.getValue()).
            setKey(new TableKey(tableId.getValue()));
        registrationRpcFutureCallBack(
            flowStatsService.getAggregateFlowStatisticsFromFlowTableForAllFlows(builder.build()),
            tbuilder.build(), nodeRef, null);
    }

    @Override
    public Future<TransactionId> getAllPortsStat(final NodeRef nodeRef) {
        Preconditions.checkArgument(nodeRef != null, MSG_NODE_REF_NOT_NULL);
        SettableFuture<TransactionId> result = SettableFuture.create();
        GetAllNodeConnectorsStatisticsInputBuilder builder =
            new GetAllNodeConnectorsStatisticsInputBuilder();
        builder.setNode(nodeRef);
        Future<RpcResult<GetAllNodeConnectorsStatisticsOutput>> rpc =
            portStatsService.getAllNodeConnectorsStatistics(builder.build());
        registrationRpcFutureCallBack(rpc, null, nodeRef, result);
        return result;
    }

    @Override
    public Future<TransactionId> getAllTablesStat(final NodeRef nodeRef) {
        Preconditions.checkArgument(nodeRef != null, MSG_NODE_REF_NOT_NULL);
        SettableFuture<TransactionId> result = SettableFuture.create();
        GetFlowTablesStatisticsInputBuilder builder =
            new GetFlowTablesStatisticsInputBuilder();
        builder.setNode(nodeRef);
        registrationRpcFutureCallBack(
            flowTableStatsService.getFlowTablesStatistics(builder.build()),
            null, nodeRef, result);
        return result;
    }

    @Override
    public Future<TransactionId>  getAllQueueStat(final NodeRef nodeRef) {
        Preconditions.checkArgument(nodeRef != null, MSG_NODE_REF_NOT_NULL);
        SettableFuture<TransactionId> result = SettableFuture.create();
        GetAllQueuesStatisticsFromAllPortsInputBuilder builder =
            new GetAllQueuesStatisticsFromAllPortsInputBuilder();
        builder.setNode(nodeRef);
        registrationRpcFutureCallBack(
            queueStatsService.getAllQueuesStatisticsFromAllPorts(builder.build()),
            null, nodeRef, result);
        return result;
    }

    @Override
    public Future<TransactionId> getAllMeterConfigStat(final NodeRef nodeRef) {
        Preconditions.checkArgument(nodeRef != null, MSG_NODE_REF_NOT_NULL);
        SettableFuture<TransactionId> result = SettableFuture.create();
        GetAllMeterConfigStatisticsInputBuilder builder =
            new GetAllMeterConfigStatisticsInputBuilder();
        builder.setNode(nodeRef);
        registrationRpcFutureCallBack(
            meterStatsService.getAllMeterConfigStatistics(builder.build()),
            null, nodeRef, result);
        return result;
    }

    @Override
    public void getGroupFeaturesStat(final NodeRef nodeRef) {
        Preconditions.checkArgument(nodeRef != null, MSG_NODE_REF_NOT_NULL);
        GetGroupFeaturesInputBuilder input = new GetGroupFeaturesInputBuilder().
            setNode(nodeRef);
        registrationRpcFutureCallBack(
            groupStatsService.getGroupFeatures(input.build()), null, nodeRef,
            null);
    }

    @Override
    public void getMeterFeaturesStat(final NodeRef nodeRef) {
        Preconditions.checkArgument(nodeRef != null, MSG_NODE_REF_NOT_NULL);
        GetMeterFeaturesInputBuilder input = new GetMeterFeaturesInputBuilder().
            setNode(nodeRef);
        registrationRpcFutureCallBack(
            meterStatsService.getMeterFeatures(input.build()), null, nodeRef,
            null);
    }

    @Override
    public Future<TransactionId> getAllGroupsConfStats(final NodeRef nodeRef) {
        Preconditions.checkArgument(nodeRef != null, MSG_NODE_REF_NOT_NULL);
        SettableFuture<TransactionId> result = SettableFuture.create();
        GetGroupDescriptionInputBuilder builder =
            new GetGroupDescriptionInputBuilder();
        builder.setNode(nodeRef);
        registrationRpcFutureCallBack(
            groupStatsService.getGroupDescription(builder.build()), null,
            nodeRef, result);
        return result;
    }

    public class TransactionCacheContainerImpl<T extends TransactionAware> implements TransactionCacheContainer<T> {

        private final TransactionId id;
        private final NodeId nId;
        private final List<T> notifications;
        private final Optional<? extends DataObject> confInput;

        public <D extends DataObject> TransactionCacheContainerImpl (final TransactionId id, final D input, final NodeId nodeId) {
            this.id = Preconditions.checkNotNull(id, MSG_TRANS_ID_NOT_NULL);
            notifications = new CopyOnWriteArrayList<T>();
            confInput = Optional.fromNullable(input);
            nId = nodeId;
        }

        @Override
        public void addNotif(final T notif) {
            notifications.add(notif);
        }

        @Override
        public TransactionId getId() {
            return id;
        }

        @Override
        public NodeId getNodeId() {
            return nId;
        }

        @Override
        public List<T> getNotifications() {
            return notifications;
        }

        @Override
        public Optional<? extends DataObject> getConfInput() {
            return confInput;
        }
    }
}
