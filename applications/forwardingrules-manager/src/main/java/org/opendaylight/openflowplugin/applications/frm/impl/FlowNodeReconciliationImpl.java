/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.frm.impl.tasks.BundleBasedReconciliationTask;
import org.opendaylight.openflowplugin.applications.frm.impl.tasks.ReconciliationTask;
import org.opendaylight.openflowplugin.applications.frm.impl.tasks.StaleMarkingReconciliationTask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ForwardingRulesManager}
 */
public class FlowNodeReconciliationImpl implements FlowNodeReconciliation {
    private static final Logger LOG = LoggerFactory.getLogger(FlowNodeReconciliationImpl.class);
    private static final int THREAD_POOL_SIZE = 4;
    private static final int FRM_RECONCILIATION_PRIORITY = Integer.getInteger("frm.tasks.priority", 0);
    private final ForwardingRulesManager provider;
    private final Map<DeviceInfo, ListenableFuture<Boolean>> futureMap = new HashMap<>();
    private final ListeningExecutorService executor = MoreExecutors
            .listeningDecorator(Executors.newFixedThreadPool(THREAD_POOL_SIZE));

    FlowNodeReconciliationImpl(final ForwardingRulesManager manager) {
        this.provider = manager;
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    @Override
    public ListenableFuture<Boolean> startReconciliation(final DeviceInfo node) {
        final InstanceIdentifier<FlowCapableNode> connectedNode = node
                .getNodeInstanceIdentifier()
                .augmentation(FlowCapableNode.class);

        return futureMap.computeIfAbsent(node, future -> {
            LOG.info("Triggering reconciliation for device {}", connectedNode.firstKeyOf(Node.class));

            ListenableFuture<Boolean> resultFuture = Futures.immediateFuture(true);

            if (provider.isStaleMarkingEnabled()) {
                LOG.info("Stale-Marking is ENABLED and proceeding with deletion of "
                                + "stale-marked entities on switch {}",
                        connectedNode.toString());
                resultFuture = executor.submit(new StaleMarkingReconciliationTask(provider, connectedNode));
            }

            return Futures.transformAsync(resultFuture, (result) -> executor
                    .submit(provider.isBundleBasedReconciliationEnabled()
                            ? new BundleBasedReconciliationTask(provider, connectedNode)
                            : new ReconciliationTask(provider, connectedNode)), executor);
        });
    }

    @Override
    public ListenableFuture<Boolean> endReconciliation(final DeviceInfo node) {
        futureMap.computeIfPresent(node, (key, future) -> {
            future.cancel(true);
            return future;
        });

        futureMap.remove(node);
        return Futures.immediateFuture(true);
    }

    @Override
    public int getPriority() {
        return FRM_RECONCILIATION_PRIORITY;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public ResultState getResultState() {
        return ResultState.DONOTHING;
    }
}
