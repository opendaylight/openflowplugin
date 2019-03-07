/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.ChangeStatisticsWorkModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.ChangeStatisticsWorkModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsManagerControlService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsWorkMode;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsManagerImpl implements StatisticsManager, StatisticsManagerControlService {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsManagerImpl.class);

    private final OpenflowProviderConfig config;
    private final ConvertorExecutor converterExecutor;
    private final ListeningExecutorService executorService;

    @GuardedBy("this")
    private final Map<DeviceInfo, StatisticsContext> contexts = new HashMap<>();

    @GuardedBy("this")
    // null indicates we are closed
    private ObjectRegistration<StatisticsManagerControlService> controlServiceRegistration;

    @GuardedBy("this")
    private StatisticsWorkMode workMode = StatisticsWorkMode.COLLECTALL;

    public StatisticsManagerImpl(@Nonnull final OpenflowProviderConfig config,
                                 @Nonnull final RpcProviderService rpcProviderRegistry,
                                 final ConvertorExecutor convertorExecutor,
                                 @Nonnull final ListeningExecutorService executorService) {
        this.config = config;
        this.converterExecutor = convertorExecutor;
        this.controlServiceRegistration = Preconditions.checkNotNull(rpcProviderRegistry
                .registerRpcImplementation(StatisticsManagerControlService.class, this));
        this.executorService = executorService;
    }

    @Override
    public synchronized ListenableFuture<RpcResult<GetStatisticsWorkModeOutput>> getStatisticsWorkMode(
            GetStatisticsWorkModeInput input) {
        return RpcResultBuilder.success(new GetStatisticsWorkModeOutputBuilder()
                .setMode(workMode)
                .build()).buildFuture();
    }

    @Override
    public synchronized ListenableFuture<RpcResult<ChangeStatisticsWorkModeOutput>> changeStatisticsWorkMode(
            ChangeStatisticsWorkModeInput input) {
        if (controlServiceRegistration == null) {
            return RpcResultBuilder.<ChangeStatisticsWorkModeOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION, "Statistics manager is shut down")
                    .buildFuture();
        }

        final StatisticsWorkMode targetWorkMode = input.getMode();
        if (workMode.equals(targetWorkMode)) {
            LOG.debug("Statistics work mode already set to {}", targetWorkMode);
            return RpcResultBuilder.<ChangeStatisticsWorkModeOutput>success().buildFuture();
        }

        final Consumer<StatisticsContext> method;
        switch (targetWorkMode) {
            case COLLECTALL:
                method = StatisticsContext::enableGathering;
                break;
            case FULLYDISABLED:
                method = StatisticsContext::disableGathering;
                break;
            default:
                return RpcResultBuilder.<ChangeStatisticsWorkModeOutput>failed()
                        .withError(RpcError.ErrorType.APPLICATION,
                            "Statistics work mode " + targetWorkMode + " not supported")
                        .buildFuture();
        }

        workMode = targetWorkMode;
        contexts.values().forEach(method);
        return RpcResultBuilder.<ChangeStatisticsWorkModeOutput>success().buildFuture();
    }

    @Override
    public synchronized StatisticsContext createContext(@Nonnull final DeviceContext deviceContext,
                                           final boolean useReconciliationFramework) {
        Preconditions.checkState(controlServiceRegistration != null, "Statistics manager already shut down");
        final MultipartWriterProvider statisticsWriterProvider = MultipartWriterProviderFactory
                .createDefaultProvider(deviceContext);

        final StatisticsContext statisticsContext = new StatisticsContextImpl<>(
                deviceContext,
                converterExecutor,
                statisticsWriterProvider,
                executorService,
                config,
                !StatisticsWorkMode.FULLYDISABLED.equals(workMode) && config.isIsStatisticsPollingOn(),
                useReconciliationFramework);

        contexts.put(deviceContext.getDeviceInfo(), statisticsContext);
        return statisticsContext;
    }

    @Override
    public synchronized void onDeviceRemoved(final DeviceInfo deviceInfo) {
        contexts.remove(deviceInfo);
        LOG.debug("Statistics context removed for node {}", deviceInfo);
    }

    @Override
    public synchronized void close() {
        if (controlServiceRegistration != null) {
            controlServiceRegistration.close();
            controlServiceRegistration = null;

            for (StatisticsContext context : contexts.values()) {
                context.close();
            }
            contexts.clear();
        }
    }
}
