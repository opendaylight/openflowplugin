/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.ChangeStatisticsWorkMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.ChangeStatisticsWorkModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.ChangeStatisticsWorkModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsWorkMode;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StatisticsManagerImpl implements StatisticsManager {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsManagerImpl.class);

    @VisibleForTesting
    final ConcurrentMap<DeviceInfo, StatisticsContext> contexts = new ConcurrentHashMap<>();

    private final OpenflowProviderConfig config;
    private final ConvertorExecutor converterExecutor;
    private final Executor executor;
    private final Semaphore workModeGuard = new Semaphore(1, true);
    private final Registration controlServiceRegistration;
    private final StatisticsWorkMode workMode = StatisticsWorkMode.COLLECTALL;
    private boolean isStatisticsFullyDisabled;

    public StatisticsManagerImpl(@NonNull final OpenflowProviderConfig config,
                                 @NonNull final RpcProviderService rpcProviderRegistry,
                                 final ConvertorExecutor convertorExecutor,
                                 @NonNull final Executor executor) {
        this.config = config;
        this.executor = executor;
        converterExecutor = convertorExecutor;
        controlServiceRegistration = rpcProviderRegistry.registerRpcImplementations(
            (GetStatisticsWorkMode) this::getStatisticsWorkMode,
            (ChangeStatisticsWorkMode) this::changeStatisticsWorkMode);
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<GetStatisticsWorkModeOutput>> getStatisticsWorkMode(
            final GetStatisticsWorkModeInput input) {
        return RpcResultBuilder.success(new GetStatisticsWorkModeOutputBuilder()
                .setMode(workMode)
                .build()).buildFuture();
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<ChangeStatisticsWorkModeOutput>> changeStatisticsWorkMode(
            final ChangeStatisticsWorkModeInput input) {
        if (workModeGuard.tryAcquire()) {
            final StatisticsWorkMode targetWorkMode = input.getMode();
            isStatisticsFullyDisabled = StatisticsWorkMode.FULLYDISABLED.equals(targetWorkMode);

            contexts.values().forEach(context -> {
                switch (targetWorkMode) {
                    case COLLECTALL:
                        context.enableGathering();
                        break;
                    case FULLYDISABLED:
                        context.disableGathering();
                        break;
                    default:
                        LOG.warn("Statistics work mode not supported: {}", targetWorkMode);
                }
            });

            workModeGuard.release();
            return RpcResultBuilder.<ChangeStatisticsWorkModeOutput>success().buildFuture();
        }

        return RpcResultBuilder.<ChangeStatisticsWorkModeOutput>failed()
                .withError(ErrorType.APPLICATION, "Statistics work mode change is already in progress")
                .buildFuture();
    }

    @Override
    public StatisticsContext createContext(@NonNull final DeviceContext deviceContext,
                                           final boolean useReconciliationFramework) {
        final MultipartWriterProvider statisticsWriterProvider = MultipartWriterProviderFactory
                .createDefaultProvider(deviceContext);

        final StatisticsContext statisticsContext = new StatisticsContextImpl<>(
                deviceContext,
                converterExecutor,
                statisticsWriterProvider,
                executor,
                config,
                !isStatisticsFullyDisabled && config.getIsStatisticsPollingOn(),
                useReconciliationFramework);

        contexts.put(deviceContext.getDeviceInfo(), statisticsContext);
        return statisticsContext;
    }

    @Override
    public void onDeviceRemoved(final DeviceInfo deviceInfo) {
        contexts.remove(deviceInfo);
        LOG.debug("Statistics context removed for node {}", deviceInfo);
    }

    @Override
    public void close() {
        isStatisticsFullyDisabled = true;
        controlServiceRegistration.close();

        for (StatisticsContext context : contexts.values()) {
            context.close();
        }

        contexts.clear();
    }
}
