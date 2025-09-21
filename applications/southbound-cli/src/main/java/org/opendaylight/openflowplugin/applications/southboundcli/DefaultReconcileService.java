/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.openflowplugin.api.openflow.ReconciliationState.ReconciliationStatus.COMPLETED;
import static org.opendaylight.openflowplugin.api.openflow.ReconciliationState.ReconciliationStatus.FAILED;
import static org.opendaylight.openflowplugin.api.openflow.ReconciliationState.ReconciliationStatus.STARTED;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.ReconciliationState;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.southboundcli.alarm.NodeReconciliationAlarm;
import org.opendaylight.openflowplugin.applications.southboundcli.util.OFNode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.Reconcile;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounterKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = ReconcileService.class, immediate = true)
// FIXME: this should probably live in FRM, but how does it integrate with its functionality?
public final class DefaultReconcileService implements Reconcile, ReconcileService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultReconcileService.class);
    private static final ObjectName ALARM_NAME;

    static {
        try {
            ALARM_NAME = new ObjectName("SDNC.FM:name=NodeReconciliationOperationOngoingBean");
        } catch (MalformedObjectNameException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final NodeReconciliationAlarm alarm = new NodeReconciliationAlarm();
    private final Map<String, ReconciliationState> reconciliationStates;
    private final FlowNodeReconciliation flowNodeReconciliation;
    private final DpnTracker dpnTracker;
    private final DataBroker broker;

    private ExecutorService executor = Executors.newWorkStealingPool(10);
    private boolean unregister = false;

    @Inject
    @Activate
    public DefaultReconcileService(@Reference final DataBroker broker, @Reference final ForwardingRulesManager frm,
            @Reference final DpnTracker dpnTracker, @Reference final FlowGroupCacheManager flowGroupCacheManager) {
        this.broker = requireNonNull(broker);
        flowNodeReconciliation = frm.getFlowNodeReconciliation();
        this.dpnTracker = requireNonNull(dpnTracker);
        reconciliationStates = flowGroupCacheManager.getReconciliationStates();

        final var mbs = ManagementFactory.getPlatformMBeanServer();
        if (!mbs.isRegistered(ALARM_NAME)) {
            try {
                mbs.registerMBean(alarm, ALARM_NAME);
                unregister = true;
                LOG.info("Registered Mbean {} successfully", ALARM_NAME);
            } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
                LOG.error("Registeration failed for Mbean {}", ALARM_NAME, e);
            }
        }
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        if (unregister) {
            unregister = false;
            try {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(ALARM_NAME);
            } catch (MBeanRegistrationException | InstanceNotFoundException e) {
                LOG.error("Unregisteration failed for Mbean {}", ALARM_NAME, e);
            }
        }

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    @Override
    public ListenableFuture<RpcResult<ReconcileOutput>> reconcile(final Set<Uint64> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return buildErrorResponse("Error executing command reconcile. No Node information was specified.");
        }

        final var allNodes = getAllNodes();
        final var unresolvedNodes = nodes.stream().filter(node -> !allNodes.contains(node.longValue()))
            .collect(Collectors.toList());
        if (!unresolvedNodes.isEmpty()) {
            return buildErrorResponse("Error executing command reconcile. "
                + "Node(s) not found: " + String.join(", ", unresolvedNodes.toString()));
        }
        return doReconcile(nodes.stream().map(Uint64::longValue).collect(Collectors.toList()));
    }

    @Override
    public ListenableFuture<RpcResult<ReconcileOutput>> reconcileAll() {
        return doReconcile(getAllNodes());
    }

    private @NonNull ListenableFuture<RpcResult<ReconcileOutput>> doReconcile(final List<Long> nodes) {
        if (!nodes.isEmpty()) {
            return buildErrorResponse(
                "Error executing command reconcile. No node information is found for reconciliation");
        }
        final var inprogressNodes = ImmutableSet.<Uint64>builder();
        nodes.parallelStream().forEach(nodeId -> {
            ReconciliationState state = getReconciliationState(nodeId);
            if (state != null && state.getState().equals(STARTED)) {
                inprogressNodes.add(Uint64.valueOf(nodeId));
            } else {
                final var alarmText = getAlarmText(nodeId,  " started reconciliation");
                final var source = getSourceText(nodeId);
                LOG.debug("Raising NodeReconciliationOperationOngoing alarm, alarmText {} source {}", alarmText,
                    source);
                alarm.raiseAlarm("NodeReconciliationOperationOngoing", alarmText, source);
                LOG.info("Executing reconciliation for node {} with state ", nodeId);
                NodeKey nodeKey = new NodeKey(new NodeId("openflow:" + nodeId));
                executor.execute(new ReconciliationTask(Uint64.valueOf(nodeId), nodeKey));
            }
        });
        return RpcResultBuilder.success(new ReconcileOutputBuilder()
            .setInprogressNodes(inprogressNodes.build())
            .build())
            .buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReconcileOutput>> invoke(final ReconcileInput input) {
        final var reconcileAllNodes = input.getReconcileAllNodes();
        final var nodes = input.getNodes();
        if (reconcileAllNodes != null && reconcileAllNodes) {
            if (nodes != null && nodes.size() > 0) {
                return buildErrorResponse("Error executing command reconcile. If 'all' option is enabled, no Node "
                    + "must be specified as input parameter.");
            }
            return reconcileAll();
        }
        return reconcile(nodes == null ? Set.of() : nodes);
    }

    private ReconciliationState getReconciliationState(final Long nodeId) {
        return reconciliationStates.get(nodeId.toString());
    }

    private static @NonNull ListenableFuture<RpcResult<ReconcileOutput>> buildErrorResponse(final String msg) {
        LOG.error("Error {}", msg);
        return RpcResultBuilder.<ReconcileOutput>failed()
                .withError(ErrorType.PROTOCOL, new ErrorTag("reconcile"), msg)
                .buildFuture();
    }

    private List<Long> getAllNodes() {
        return dpnTracker.currentNodes().stream().distinct().map(OFNode::getNodeId).collect(Collectors.toList());
    }

    /**
     * Method gets the alarm text for the nodeId.
     *
     * @param nodeId Source of the alarm nodeId
     * @param event reason for alarm invoke/clear
     */
    private static @NonNull String getAlarmText(final Long nodeId, final String event) {
        return "OF Switch " + nodeId + event;
    }

    /**
     * Method gets the source text for the nodeId.
     *
     * @param nodeId Source of the alarm nodeId
     */
    private static String getSourceText(final Long nodeId) {
        return "Dpn=" + nodeId;
    }

    private final class ReconciliationTask implements Runnable {
        private static final String DATE_AND_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
        private final NodeKey nodeKey;
        private final Uint64 nodeId;

        private ReconciliationTask(final Uint64 nodeId, final NodeKey nodeKey) {
            this.nodeId = nodeId;
            this.nodeKey = nodeKey;
        }

        @Override
        public void run() {
            updateReconciliationState(STARTED);
            final var reconOutput = flowNodeReconciliation.reconcileConfiguration(
                DataObjectIdentifier.builder(Nodes.class)
                    .child(Node.class, nodeKey)
                    .augmentation(FlowCapableNode.class)
                    .build());
            try {
                final boolean rpcResult = reconOutput.get();
                increaseReconcileCount(rpcResult);
                if (rpcResult) {
                    updateReconciliationState(COMPLETED);
                    LOG.info("Reconciliation successfully completed for node {}", nodeId);
                } else {
                    updateReconciliationState(FAILED);
                    LOG.error("Reconciliation failed for node {}", nodeId);
                }
            } catch (ExecutionException | InterruptedException e) {
                increaseReconcileCount(false);
                updateReconciliationState(FAILED);
                LOG.error("Error occurred while invoking reconcile RPC for node {}", nodeId, e);
            } finally {
                final var dpnId = nodeId.longValue();
                final var alarmText = getAlarmText(dpnId, " finished reconciliation");
                final var source = getSourceText(dpnId);
                LOG.debug("Clearing NodeReconciliationOperationOngoing alarm of source {}", source);
                alarm.clearAlarm("NodeReconciliationOperationOngoing", alarmText, source);
            }
        }

        private void increaseReconcileCount(final boolean isSuccess) {
            // FIXME: do not use SimpleDateFormat
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
            final var instanceIdentifier = DataObjectIdentifier.builder(ReconciliationCounter.class)
                .child(ReconcileCounter.class, new ReconcileCounterKey(nodeId))
                .build();
            ReadWriteTransaction tx = broker.newReadWriteTransaction();
            Optional<ReconcileCounter> count = getReconciliationCount(tx, instanceIdentifier);
            ReconcileCounterBuilder counterBuilder = new ReconcileCounterBuilder()
                    .withKey(new ReconcileCounterKey(nodeId))
                    .setLastRequestTime(new DateAndTime(simpleDateFormat.format(new Date())));

            if (isSuccess) {
                if (count.isPresent()) {
                    long successCount = count.orElseThrow().getSuccessCount().toJava();
                    counterBuilder.setSuccessCount(Uint32.valueOf(++successCount));
                    LOG.debug("Reconcile success count {} for the node: {} ", successCount, nodeId);
                } else {
                    counterBuilder.setSuccessCount(Uint32.ONE);
                }
            } else if (count.isPresent()) {
                long failureCount = count.orElseThrow().getFailureCount().toJava();
                counterBuilder.setFailureCount(Uint32.valueOf(++failureCount));
                LOG.debug("Reconcile failure count {} for the node: {} ", failureCount, nodeId);
            } else {
                counterBuilder.setFailureCount(Uint32.ONE);
            }
            try {
                tx.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, instanceIdentifier,
                        counterBuilder.build());
                tx.commit().get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Exception while submitting counter for {}", nodeId, e);
            }
        }

        private Optional<ReconcileCounter> getReconciliationCount(final ReadWriteTransaction tx,
                final DataObjectIdentifier<ReconcileCounter> instanceIdentifier) {
            try {
                return tx.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier).get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Exception while reading counter for node: {}", nodeId, e);
                return Optional.empty();
            }
        }

        private void updateReconciliationState(final ReconciliationState.ReconciliationStatus status) {
            reconciliationStates.put(nodeId.toString(), new ReconciliationState(status, LocalDateTime.now()));
        }
    }
}

