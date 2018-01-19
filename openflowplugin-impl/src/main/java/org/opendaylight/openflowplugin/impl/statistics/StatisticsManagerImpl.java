/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.ChangeStatisticsWorkModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsManagerControlService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsWorkMode;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsManagerImpl implements StatisticsManager, StatisticsManagerControlService {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsManagerImpl.class);

    private final OpenflowProviderConfig config;
    private final ConvertorExecutor converterExecutor;
    private final Executor executor;

    @GuardedBy("this")
    private final Map<DeviceInfo, StatisticsContext> contexts = new HashMap<>();

    @GuardedBy("this")
    // null indicates we are closed
    private RpcRegistration<StatisticsManagerControlService> controlServiceRegistration;

    @GuardedBy("this")
    private StatisticsWorkMode workMode = StatisticsWorkMode.COLLECTALL;

    public StatisticsManagerImpl(@Nonnull final OpenflowProviderConfig config,
                                 @Nonnull final RpcProviderRegistry rpcProviderRegistry,
                                 final ConvertorExecutor convertorExecutor,
                                 @Nonnull final Executor executor) {
        this.config = config;
        this.converterExecutor = convertorExecutor;
        this.controlServiceRegistration = Preconditions.checkNotNull(rpcProviderRegistry
                .addRpcImplementation(StatisticsManagerControlService.class, this));
        this.executor = executor;
    }

    @Override
    public synchronized Future<RpcResult<GetStatisticsWorkModeOutput>> getStatisticsWorkMode() {
        return RpcResultBuilder.success(new GetStatisticsWorkModeOutputBuilder()
                .setMode(workMode)
                .build()).buildFuture();
    }

    @Override
    public synchronized Future<RpcResult<Void>> changeStatisticsWorkMode(final ChangeStatisticsWorkModeInput input) {
        if (controlServiceRegistration == null) {
            return RpcResultBuilder.<Void>failed()
                    .withError(RpcError.ErrorType.APPLICATION, "Statistics manager is shut down")
                    .buildFuture();
        }

        final StatisticsWorkMode targetWorkMode = input.getMode();
        if (workMode.equals(targetWorkMode)) {
            LOG.debug("Statistics work mode already set to {}", targetWorkMode);
            return RpcResultBuilder.<Void>success().buildFuture();
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
                return RpcResultBuilder.<Void>failed()
                        .withError(RpcError.ErrorType.APPLICATION,
                            "Statistics work mode " + targetWorkMode + " not supported")
                        .buildFuture();
        }

        workMode = targetWorkMode;
        contexts.values().forEach(method);
        return RpcResultBuilder.<Void>success().buildFuture();
    }

    @Override
    public synchronized StatisticsContext createContext(@Nonnull final DeviceContext deviceContext,
                                           final boolean useReconciliationFramework) {
        Preconditions.checkState(controlServiceRegistration != null, "Statistics manager already shut down");
        final boolean enabled = !StatisticsWorkMode.FULLYDISABLED.equals(workMode);
        final MultipartWriterProvider statisticsWriterProvider = MultipartWriterProviderFactory
                .createDefaultProvider(deviceContext);

        final StatisticsContext statisticsContext = new StatisticsContextImpl<>(
                deviceContext, converterExecutor,
                statisticsWriterProvider, executor,
                enabled && config.isIsStatisticsPollingOn(),
                useReconciliationFramework,
                config.getBasicTimerDelay().getValue(), config.getMaximumTimerDelay().getValue());
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
