/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static org.opendaylight.openflowplugin.api.openflow.ReconciliationState.ReconciliationStatus.COMPLETED;
import static org.opendaylight.openflowplugin.api.openflow.ReconciliationState.ReconciliationStatus.FAILED;
import static org.opendaylight.openflowplugin.api.openflow.ReconciliationState.ReconciliationStatus.STARTED;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.ReconciliationState;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.frm.util.FrmUtil;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveGroupCaseBuilder;
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
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ForwardingRulesManager}.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 */
public class FlowNodeReconciliationImpl implements FlowNodeReconciliation {

    private static final Logger LOG = LoggerFactory.getLogger(FlowNodeReconciliationImpl.class);
    private static final Logger OF_EVENT_LOG = LoggerFactory.getLogger("OfEventLog");

    // The number of nanoseconds to wait for a single group to be added.
    private static final long ADD_GROUP_TIMEOUT = TimeUnit.SECONDS.toNanos(3);

    // The maximum number of nanoseconds to wait for completion of add-group RPCs.
    private static final long MAX_ADD_GROUP_TIMEOUT = TimeUnit.SECONDS.toNanos(20);
    private static final String SEPARATOR = ":";
    private static final int THREAD_POOL_SIZE = 4;
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat("BundleResync-%d")
            .setDaemon(false)
            .setUncaughtExceptionHandler((thread, ex) -> LOG.error("Uncaught exception {}", thread, ex))
            .build();
    private final DataBroker dataBroker;
    private final ForwardingRulesManager provider;
    private final String serviceName;
    private final int priority;
    private final ResultState resultState;
    private final Map<DeviceInfo, ListenableFuture<Boolean>> futureMap = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    private final SalBundleService salBundleService;

    private static final AtomicLong BUNDLE_ID = new AtomicLong();
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);
    private final Map<String, ReconciliationState> reconciliationStates;

    public FlowNodeReconciliationImpl(final ForwardingRulesManager manager, final DataBroker db,
                                      final String serviceName, final int priority, final ResultState resultState,
                                      final FlowGroupCacheManager flowGroupCacheManager) {
        this.provider = Preconditions.checkNotNull(manager, "ForwardingRulesManager can not be null!");
        dataBroker = Preconditions.checkNotNull(db, "DataBroker can not be null!");
        this.serviceName = serviceName;
        this.priority = priority;
        this.resultState = resultState;
        salBundleService = Preconditions.checkNotNull(manager.getSalBundleService(),
                "salBundleService can not be null!");
        reconciliationStates = flowGroupCacheManager.getReconciliationStates();
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
        // Clearing the group registry cache for the connected node if exists
        String nodeId = FrmUtil.getNodeIdValueFromNodeIdentifier(connectedNode);
        provider.getDevicesGroupRegistry().clearNodeGroups(nodeId);
        if (provider.isStaleMarkingEnabled()) {
            LOG.info("Stale-Marking is ENABLED and proceeding with deletion of " + "stale-marked entities on switch {}",
                    connectedNode);
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

    @Override
    public void flowNodeDisconnected(InstanceIdentifier<FlowCapableNode> disconnectedNode) {
        String node = disconnectedNode.firstKeyOf(Node.class).getId().getValue();
        BigInteger dpnId = getDpnIdFromNodeName(node);
        reconciliationStates.remove(dpnId.toString());
    }

    private class BundleBasedReconciliationTask implements Callable<Boolean> {
        final InstanceIdentifier<FlowCapableNode> nodeIdentity;

        BundleBasedReconciliationTask(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
            nodeIdentity = nodeIdent;
        }

        @Override
        public Boolean call() {
            String node = nodeIdentity.firstKeyOf(Node.class).getId().getValue();
            Optional<FlowCapableNode> flowNode = Optional.empty();
            BundleId bundleIdValue = new BundleId(Uint32.valueOf(BUNDLE_ID.getAndIncrement()));
            BigInteger dpnId = getDpnIdFromNodeName(node);
            ExecutorService service = Executors.newSingleThreadExecutor(THREAD_FACTORY);
            LOG.info("Triggering bundle based reconciliation for device : {}", dpnId);
            try (ReadTransaction trans = provider.getReadTransaction()) {
                flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdentity).get();
            } catch (ExecutionException | InterruptedException e) {
                LOG.error("Error occurred while reading the configuration data store for node {}", nodeIdentity, e);
            }

            if (flowNode.isPresent()) {
                ReconciliationState reconciliationState = new ReconciliationState(
                        STARTED, LocalDateTime.now());
                //put the dpn info into the map
                reconciliationStates.put(dpnId.toString(), reconciliationState);
                LOG.debug("FlowNode present for Datapath ID {}", dpnId);
                OF_EVENT_LOG.debug("Bundle Reconciliation Start, Node: {}", dpnId);
                final NodeRef nodeRef = new NodeRef(nodeIdentity.firstIdentifierOf(Node.class));

                final ControlBundleInput closeBundleInput = new ControlBundleInputBuilder().setNode(nodeRef)
                        .setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTCLOSEREQUEST).build();

                final ControlBundleInput openBundleInput = new ControlBundleInputBuilder().setNode(nodeRef)
                        .setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTOPENREQUEST).build();

                final ControlBundleInput commitBundleInput = new ControlBundleInputBuilder().setNode(nodeRef)
                        .setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTCOMMITREQUEST).build();

                final AddBundleMessagesInput deleteAllFlowGroupsInput = new AddBundleMessagesInputBuilder()
                        .setNode(nodeRef).setBundleId(bundleIdValue).setFlags(BUNDLE_FLAGS)
                        .setMessages(createMessages(nodeRef)).build();

                LOG.debug("Closing openflow bundle for device {}", dpnId);
                /* Close previously opened bundle on the openflow switch if any */
                ListenableFuture<RpcResult<ControlBundleOutput>> closeBundle
                        = salBundleService.controlBundle(closeBundleInput);

                /* Open a new bundle on the switch */
                ListenableFuture<RpcResult<ControlBundleOutput>> openBundle
                        = Futures.transformAsync(closeBundle, rpcResult -> {
                            if (rpcResult.isSuccessful()) {
                                LOG.debug("Existing bundle is successfully closed for device {}", dpnId);
                            }
                            return salBundleService.controlBundle(openBundleInput);
                        }, service);

                    /* Push groups and flows via bundle add messages */
                ListenableFuture<RpcResult<AddBundleMessagesOutput>> deleteAllFlowGroupsFuture
                        = Futures.transformAsync(openBundle, rpcResult -> {
                            if (rpcResult.isSuccessful()) {
                                LOG.debug("Open bundle is successful for device {}", dpnId);
                                return salBundleService.addBundleMessages(deleteAllFlowGroupsInput);
                            }
                            return Futures.immediateFuture(null);
                        }, service);

                /* Push flows and groups via bundle add messages */
                Optional<FlowCapableNode> finalFlowNode = flowNode;
                ListenableFuture<List<RpcResult<AddBundleMessagesOutput>>> addbundlesFuture
                        = Futures.transformAsync(deleteAllFlowGroupsFuture, rpcResult -> {
                            if (rpcResult.isSuccessful()) {
                                LOG.debug("Adding delete all flow/group message is successful for device {}", dpnId);
                                return Futures.allAsList(addBundleMessages(finalFlowNode.get(), bundleIdValue,
                                        nodeIdentity));
                            }
                            return Futures.immediateFuture(null);
                        }, service);

                    /* Commit the bundle on the openflow switch */
                ListenableFuture<RpcResult<ControlBundleOutput>> commitBundleFuture
                        = Futures.transformAsync(addbundlesFuture, rpcResult -> {
                            LOG.debug("Adding bundle messages completed for device {}", dpnId);
                            return salBundleService.controlBundle(commitBundleInput);
                        }, service);

                /* Bundles not supported for meters */
                Collection<Meter> meters = flowNode.get().nonnullMeter().values();
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
                    }, service);
                try {
                    RpcResult<ControlBundleOutput> bundleFuture = commitBundleFuture.get();
                    if (bundleFuture != null && bundleFuture.isSuccessful()) {
                        reconciliationState.setState(COMPLETED, LocalDateTime.now());
                        LOG.debug("Completing bundle based reconciliation for device ID:{}", dpnId);
                        OF_EVENT_LOG.debug("Bundle Reconciliation Finish, Node: {}", dpnId);
                        return true;
                    } else {
                        reconciliationState.setState(FAILED, LocalDateTime.now());
                        LOG.error("commit bundle failed for device {} with error {}", dpnId,
                                commitBundleFuture.get().getErrors());
                        return false;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    reconciliationState.setState(FAILED, LocalDateTime.now());
                    LOG.error("Error while doing bundle based reconciliation for device ID:{}", dpnId, e);
                    return false;
                } finally {
                    service.shutdown();
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
        provider.getDevicesGroupRegistry().clearNodeGroups(node.toString());
        return futureMap.computeIfAbsent(node, future -> reconcileConfiguration(connectedNode));
    }

    @Override
    public ListenableFuture<Boolean> endReconciliation(DeviceInfo node) {
        ListenableFuture<Boolean> listenableFuture = futureMap.computeIfPresent(node, (key, future) -> future);
        if (listenableFuture != null) {
            listenableFuture.cancel(true);
            futureMap.remove(node);
        }
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
            OF_EVENT_LOG.debug("Reconciliation Start, Node: {}", dpnId);

            Optional<FlowCapableNode> flowNode;
            // initialize the counter
            int counter = 0;
            try (ReadTransaction trans = provider.getReadTransaction()) {
                flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdentity).get();
            } catch (ExecutionException | InterruptedException e) {
                LOG.warn("Fail with read Config/DS for Node {} !", nodeIdentity, e);
                return false;
            }

            if (flowNode.isPresent()) {
                /* Tables - have to be pushed before groups */
                // CHECK if while pushing the update, updateTableInput can be null to emulate a
                // table add
                ReconciliationState reconciliationState = new ReconciliationState(
                        STARTED, LocalDateTime.now());
                //put the dpn info into the map
                reconciliationStates.put(dpnId.toString(), reconciliationState);
                LOG.debug("Triggering reconciliation for node {} with state: {}", dpnId, STARTED);
                Collection<TableFeatures> tableList = flowNode.get().nonnullTableFeatures().values();
                for (TableFeatures tableFeaturesItem : tableList) {
                    TableFeaturesKey tableKey = tableFeaturesItem.key();
                    KeyedInstanceIdentifier<TableFeatures, TableFeaturesKey> tableFeaturesII = nodeIdentity
                            .child(TableFeatures.class, new TableFeaturesKey(tableKey.getTableId()));
                    provider.getTableFeaturesCommiter().update(tableFeaturesII, tableFeaturesItem, null, nodeIdentity);
                }

                /* Groups - have to be first */
                Collection<Group> groups = flowNode.get().nonnullGroup().values();
                List<Group> toBeInstalledGroups = new ArrayList<>();
                toBeInstalledGroups.addAll(groups);
                // new list for suspected groups pointing to ports .. when the ports come up
                // late
                List<Group> suspectedGroups = new ArrayList<>();
                Map<Uint32, ListenableFuture<?>> groupFutures = new HashMap<>();

                while ((!toBeInstalledGroups.isEmpty() || !suspectedGroups.isEmpty())
                        && counter <= provider.getReconciliationRetryCount()) { // also check if the counter has not
                                                                                // crossed the threshold

                    if (toBeInstalledGroups.isEmpty() && !suspectedGroups.isEmpty()) {
                        LOG.debug("These Groups are pointing to node-connectors that are not up yet {}",
                                suspectedGroups);
                        toBeInstalledGroups.addAll(suspectedGroups);
                        break;
                    }

                    ListIterator<Group> iterator = toBeInstalledGroups.listIterator();
                    while (iterator.hasNext()) {
                        Group group = iterator.next();
                        boolean okToInstall = true;
                        Buckets buckets = group.getBuckets();
                        Collection<Bucket> bucketList = buckets == null ? null : buckets.nonnullBucket().values();
                        if (bucketList == null) {
                            bucketList = Collections.<Bucket>emptyList();
                        }
                        for (Bucket bucket : bucketList) {
                            Collection<Action> actions = bucket.nonnullAction().values();
                            if (actions == null) {
                                actions = Collections.<Action>emptyList();
                            }
                            for (Action action : actions) {
                                // chained-port
                                if (action.getAction().implementedInterface().getName()
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
                                                nodeConnectorUri, group.getGroupId());
                                        break;
                                    }
                                } else if (action.getAction().implementedInterface().getName()
                                        .equals("org.opendaylight.yang.gen.v1.urn.opendaylight"
                                                + ".action.types.rev131112.action.action.GroupActionCase")) {
                                    // chained groups
                                    Uint32 groupId = ((GroupActionCase) action.getAction()).getGroupAction()
                                            .getGroupId();
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
                                group.getGroupId(), provider.getReconciliationRetryCount());
                        addGroup(groupFutures, group);
                    }
                }
                /* Meters */
                Collection<Meter> meters = flowNode.get().nonnullMeter().values();
                for (Meter meter : meters) {
                    final KeyedInstanceIdentifier<Meter, MeterKey> meterIdent = nodeIdentity.child(Meter.class,
                            meter.key());
                    provider.getMeterCommiter().add(meterIdent, meter, nodeIdentity);
                }

                // Need to wait for all groups to be installed before adding
                // flows.
                awaitGroups(node, groupFutures.values());

                /* Flows */
                Collection<Table> tables = flowNode.get().getTable() != null ? flowNode.get().nonnullTable().values()
                        : Collections.<Table>emptyList();
                int flowCount = 0;
                for (Table table : tables) {
                    final KeyedInstanceIdentifier<Table, TableKey> tableIdent = nodeIdentity.child(Table.class,
                            table.key());
                    Collection<Flow> flows = table.nonnullFlow().values();
                    flowCount += flows.size();
                    for (Flow flow : flows) {
                        final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent = tableIdent.child(Flow.class,
                                flow.key());
                        provider.getFlowCommiter().add(flowIdent, flow, nodeIdentity);
                    }
                }
                reconciliationState.setState(COMPLETED, LocalDateTime.now());
                OF_EVENT_LOG.debug("Reconciliation Finish, Node: {}, flow count: {}", dpnId, flowCount);
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
        private void addGroup(Map<Uint32, ListenableFuture<?>> map, Group group) {
            KeyedInstanceIdentifier<Group, GroupKey> groupIdent = nodeIdentity.child(Group.class, group.key());
            final Uint32 groupId = group.getGroupId().getValue();
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

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private static BigInteger getDpnIdFromNodeName(String nodeName) {
        String dpId = nodeName.substring(nodeName.lastIndexOf(SEPARATOR) + 1);
        return new BigInteger(dpId);
    }

    private void reconciliationPreProcess(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        List<InstanceIdentifier<StaleFlow>> staleFlowsToBeBulkDeleted = Lists.newArrayList();
        List<InstanceIdentifier<StaleGroup>> staleGroupsToBeBulkDeleted = Lists.newArrayList();
        List<InstanceIdentifier<StaleMeter>> staleMetersToBeBulkDeleted = Lists.newArrayList();

        Optional<FlowCapableNode> flowNode = Optional.empty();

        try (ReadTransaction trans = provider.getReadTransaction()) {
            flowNode = trans.read(LogicalDatastoreType.CONFIGURATION, nodeIdent).get();
        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Reconciliation Pre-Processing Fail with read Config/DS for Node {} !", nodeIdent, e);
        }

        if (flowNode.isPresent()) {

            LOG.debug("Proceeding with deletion of stale-marked Flows on switch {} using Openflow interface",
                    nodeIdent);
            /* Stale-Flows - Stale-marked Flows have to be removed first for safety */
            Collection<Table> tables = flowNode.get().nonnullTable().values();
            for (Table table : tables) {
                final KeyedInstanceIdentifier<Table, TableKey> tableIdent = nodeIdent.child(Table.class,
                        table.key());
                Collection<StaleFlow> staleFlows = table.nonnullStaleFlow().values();
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
                    nodeIdent);

            // TODO: Should we collate the futures of RPC-calls to be sure that groups are
            // Flows are fully deleted
            // before attempting to delete groups - just in case there are references

            /* Stale-marked Groups - Can be deleted after flows */
            Collection<StaleGroup> staleGroups = flowNode.get().nonnullStaleGroup().values();
            for (StaleGroup staleGroup : staleGroups) {

                GroupBuilder groupBuilder = new GroupBuilder(staleGroup);
                Group toBeDeletedGroup = groupBuilder.setGroupId(staleGroup.getGroupId()).build();

                final KeyedInstanceIdentifier<Group, GroupKey> groupIdent = nodeIdent.child(Group.class,
                        toBeDeletedGroup.key());

                this.provider.getGroupCommiter().remove(groupIdent, toBeDeletedGroup, nodeIdent);

                staleGroupsToBeBulkDeleted.add(getStaleGroupInstanceIdentifier(staleGroup, nodeIdent));
            }

            LOG.debug("Proceeding with deletion of stale-marked Meters for switch {} using Openflow interface",
                    nodeIdent);
            /* Stale-marked Meters - can be deleted anytime - so least priority */
            Collection<StaleMeter> staleMeters = flowNode.get().getStaleMeter().values();

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
                nodeIdent);
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

        FluentFuture<?> submitFuture = writeTransaction.commit();
        handleStaleEntityDeletionResultFuture(submitFuture);
    }

    private void deleteDSStaleGroups(List<InstanceIdentifier<StaleGroup>> groupsForBulkDelete) {
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        for (InstanceIdentifier<StaleGroup> staleGroupIId : groupsForBulkDelete) {
            writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, staleGroupIId);
        }

        FluentFuture<?> submitFuture = writeTransaction.commit();
        handleStaleEntityDeletionResultFuture(submitFuture);
    }

    private void deleteDSStaleMeters(List<InstanceIdentifier<StaleMeter>> metersForBulkDelete) {
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        for (InstanceIdentifier<StaleMeter> staleMeterIId : metersForBulkDelete) {
            writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, staleMeterIId);
        }

        FluentFuture<?> submitFuture = writeTransaction.commit();
        handleStaleEntityDeletionResultFuture(submitFuture);
    }

    private static InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight
        .flow.inventory.rev130819.tables.table.StaleFlow> getStaleFlowInstanceIdentifier(
            StaleFlow staleFlow, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.child(Table.class, new TableKey(staleFlow.getTableId())).child(
                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlow.class,
                new StaleFlowKey(new FlowId(staleFlow.getId())));
    }

    private static InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight
        .group.types.rev131018.groups.StaleGroup> getStaleGroupInstanceIdentifier(
            StaleGroup staleGroup, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.child(StaleGroup.class, new StaleGroupKey(new GroupId(staleGroup.getGroupId())));
    }

    private static InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight
        .flow.inventory.rev130819.meters.StaleMeter> getStaleMeterInstanceIdentifier(
            StaleMeter staleMeter, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.child(StaleMeter.class, new StaleMeterKey(new MeterId(staleMeter.getMeterId())));
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private List<ListenableFuture<RpcResult<AddBundleMessagesOutput>>> addBundleMessages(final FlowCapableNode flowNode,
                                                         final BundleId bundleIdValue,
                                                         final InstanceIdentifier<FlowCapableNode> nodeIdentity) {
        List<ListenableFuture<RpcResult<AddBundleMessagesOutput>>> futureList = new ArrayList<>();
        for (Group group : flowNode.nonnullGroup().values()) {
            final KeyedInstanceIdentifier<Group, GroupKey> groupIdent = nodeIdentity.child(Group.class, group.key());
            futureList.add(provider.getBundleGroupListener().add(groupIdent, group, nodeIdentity, bundleIdValue));
        }

        for (Table table : flowNode.nonnullTable().values()) {
            final KeyedInstanceIdentifier<Table, TableKey> tableIdent = nodeIdentity.child(Table.class, table.key());
            for (Flow flow : table.nonnullFlow().values()) {
                final KeyedInstanceIdentifier<Flow, FlowKey> flowIdent = tableIdent.child(Flow.class, flow.key());
                futureList.add(provider.getBundleFlowListener().add(flowIdent, flow, nodeIdentity, bundleIdValue));
            }
        }
        OF_EVENT_LOG.debug("Flow/Group count is {}", futureList.size());
        return futureList;
    }

    private static void handleStaleEntityDeletionResultFuture(FluentFuture<?> submitFuture) {
        submitFuture.addCallback(new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LOG.debug("Stale entity removal success");
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.debug("Stale entity removal failed", throwable);
            }
        }, MoreExecutors.directExecutor());
    }

    private static Flow getDeleteAllFlow() {
        return new FlowBuilder().setTableId(OFConstants.OFPTT_ALL).build();
    }

    private static Group getDeleteAllGroup() {
        return new GroupBuilder()
                .setGroupType(GroupTypes.GroupAll)
                .setGroupId(new GroupId(OFConstants.OFPG_ALL))
                .build();
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private static Messages createMessages(final NodeRef nodeRef) {
        final List<Message> messages = new ArrayList<>();
        messages.add(new MessageBuilder().setNode(nodeRef)
                .setBundleInnerMessage(new BundleRemoveFlowCaseBuilder()
                        .setRemoveFlowCaseData(new RemoveFlowCaseDataBuilder(getDeleteAllFlow()).build()).build())
                .build());

        messages.add(new MessageBuilder().setNode(nodeRef)
                .setBundleInnerMessage(new BundleRemoveGroupCaseBuilder()
                        .setRemoveGroupCaseData(new RemoveGroupCaseDataBuilder(getDeleteAllGroup()).build()).build())
                .build());
        return new MessagesBuilder().setMessage(messages).build();
    }
}
