/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.arbitratorreconciliation.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.applications.reconciliation.NotificationRegistration;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundle;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundle;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitActiveBundleOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundle;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = { })
public final class ArbitratorReconciliationManagerImpl implements ReconciliationNotificationListener, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ArbitratorReconciliationManagerImpl.class);
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);
    private static final String SERVICE_NAME = "ArbitratorReconciliationManager";

    // FIXME: use CM to control this constant
    private static final int THREAD_POOL_SIZE = 4;
    // FIXME: use CM to control this constant
    private static final int ARBITRATOR_RECONCILIATION_PRIORITY =
        Integer.getInteger("arbitrator.reconciliation.manager.priority", 0 /*default*/);

    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final Map<Uint64, BundleDetails> bundleIdMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Registration> rpcRegistrations = new ConcurrentHashMap<>();

    private final RpcProviderService rpcProviderService;
    private final NotificationRegistration registration;
    private final ControlBundle controlBundle;

    @Inject
    @Activate
    public ArbitratorReconciliationManagerImpl(@Reference final ReconciliationManager reconciliationManager,
            @Reference final RpcProviderService rpcProviderService, @Reference final RpcService rpcService) {
        this.rpcProviderService = requireNonNull(rpcProviderService);
        controlBundle = requireNonNull(rpcService.getRpc(ControlBundle.class));
        registration = reconciliationManager.registerService(this);
        LOG.info("ArbitratorReconciliationManager has started successfully.");
    }

    @Deactivate
    @PreDestroy
    @Override
    public void close() throws Exception {
        executor.shutdown();
        registration.close();
    }

    private ListenableFuture<RpcResult<CommitActiveBundleOutput>> commitActiveBundle(
            final CommitActiveBundleInput input) {
        final var nodeId = input.getNodeId();
        final var details = bundleIdMap.get(nodeId);
        if (details != null) {
            final var rpcResult = controlBundle.invoke(new ControlBundleInputBuilder()
                .setNode(input.getNode())
                .setBundleId(details.bundleId)
                .setFlags(BUNDLE_FLAGS)
                .setType(BundleControlType.ONFBCTCOMMITREQUEST)
                .build());
            bundleIdMap.put(nodeId, new BundleDetails(details.bundleId, rpcResult));

            Futures.addCallback(rpcResult, new CommitActiveBundleCallback(nodeId), MoreExecutors.directExecutor());
            return Futures.transform(rpcResult, createRpcResultCondenser("committed active bundle"),
                MoreExecutors.directExecutor());
        }
        return RpcResultBuilder.success(new CommitActiveBundleOutputBuilder()
                .setResult(null).build())
                .withRpcErrors(List.of(RpcResultBuilder.newError(ErrorType.APPLICATION,
                        null, "No active bundle found for the node" + nodeId))).buildFuture();
    }

    private ListenableFuture<RpcResult<GetActiveBundleOutput>> getActiveBundle(final GetActiveBundleInput input) {
        Uint64 nodeId = input.getNodeId();
        BundleDetails bundleDetails = bundleIdMap.get(nodeId);
        if (bundleDetails != null) {
            try {
                //This blocking call is used to prevent the applications from pushing flows and groups via the default
                // pipeline when the commit bundle is ongoing.
                bundleDetails.result.get();
                return RpcResultBuilder.success(new GetActiveBundleOutputBuilder()
                        .setResult(bundleDetails.bundleId)
                        .build())
                        .buildFuture();
            } catch (InterruptedException | ExecutionException e) {
                return RpcResultBuilder.<GetActiveBundleOutput>failed()
                        .withRpcErrors(List.of(RpcResultBuilder.newError(ErrorType.APPLICATION,
                                null, e.getMessage()))).buildFuture();
            }
        }
        return RpcResultBuilder.success(new GetActiveBundleOutputBuilder().setResult(null).build()).buildFuture();
    }

    @Override
    public ListenableFuture<Boolean> startReconciliation(final DeviceInfo node) {
        registerRpc(node);
        LOG.trace("arbitrator reconciliation is disabled");
        return FluentFutures.immediateTrueFluentFuture();
    }

    @Override
    public ListenableFuture<Boolean> endReconciliation(final DeviceInfo node) {
        Uint64 datapathId = node.getDatapathId();
        LOG.trace("Stopping arbitrator reconciliation for node {}", datapathId);
        bundleIdMap.remove(datapathId);
        deregisterRpc(node);
        return FluentFutures.immediateTrueFluentFuture();
    }

    @Override
    public int getPriority() {
        return ARBITRATOR_RECONCILIATION_PRIORITY;
    }

    @Override
    public String getName() {
        return SERVICE_NAME;
    }

    @Override
    public ResultState getResultState() {
        return ResultState.DONOTHING;
    }

    public final class CommitActiveBundleCallback implements FutureCallback<RpcResult<?>> {
        private final Uint64 nodeId;

        private CommitActiveBundleCallback(final Uint64 nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public void onSuccess(final RpcResult<?> rpcResult) {
            LOG.debug("Completed arbitrator reconciliation for device:{}", nodeId);
            bundleIdMap.remove(nodeId);
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.error("Error while performing arbitrator reconciliation for device {}", nodeId, throwable);
        }
    }

    private static <D> Function<RpcResult<D>, RpcResult<CommitActiveBundleOutput>> createRpcResultCondenser(
            final String action) {
        return input -> {
            final RpcResultBuilder<CommitActiveBundleOutput> resultSink;
            if (input != null) {
                final var errors = new ArrayList<RpcError>();
                if (!input.isSuccessful()) {
                    errors.addAll(input.getErrors());
                    resultSink = RpcResultBuilder.<CommitActiveBundleOutput>failed().withRpcErrors(errors);
                } else {
                    resultSink = RpcResultBuilder.success();
                }
            } else {
                resultSink = RpcResultBuilder.<CommitActiveBundleOutput>failed()
                        .withError(ErrorType.APPLICATION, "action of " + action + " failed");
            }
            return resultSink.build();
        };
    }

    private void registerRpc(final DeviceInfo node) {
        final var path = DataObjectIdentifier.builder(Nodes.class)
            .child(Node.class, new NodeKey(node.getNodeId()))
            .build();
        LOG.debug("The path is registered : {}", path);
        rpcRegistrations.put(node.getNodeId().getValue(), rpcProviderService.registerRpcImplementations(List.of(
            (GetActiveBundle) this::getActiveBundle,
            (CommitActiveBundle) this::commitActiveBundle), Set.of(path)));
    }

    private void deregisterRpc(final DeviceInfo node) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("The path is unregistered : {}", DataObjectIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(node.getNodeId()))
                .build());
        }

        final var reg = rpcRegistrations.remove(node.getNodeId().getValue());
        if (reg != null) {
            reg.close();
        }
    }

    @NonNullByDefault
    record BundleDetails(BundleId bundleId, ListenableFuture<RpcResult<ControlBundleOutput>> result) {
        BundleDetails {
            requireNonNull(bundleId);
            requireNonNull(result);
        }
    }
}
