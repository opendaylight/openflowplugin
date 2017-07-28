/* Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.base.Preconditions;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import javax.annotation.Nonnull;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ReconciliationFrameworkRegistrar;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
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
    private final ConcurrentMap<DeviceInfo, StatisticsContext> contexts = new ConcurrentHashMap<>();
    private final Semaphore workModeGuard = new Semaphore(1, true);
    private final BindingAwareBroker.RpcRegistration<StatisticsManagerControlService> controlServiceRegistration;
    private StatisticsWorkMode workMode = StatisticsWorkMode.COLLECTALL;
    private ReconciliationFrameworkRegistrar reconciliationFrameworkRegistrar;
    private boolean isStatisticsFullyDisabled;

    public StatisticsManagerImpl(@Nonnull final OpenflowProviderConfig config,
                                 @Nonnull final RpcProviderRegistry rpcProviderRegistry,
                                 final ConvertorExecutor convertorExecutor) {
        this.config = config;
        this.converterExecutor = convertorExecutor;
        this.controlServiceRegistration = Preconditions.checkNotNull(rpcProviderRegistry
                .addRpcImplementation(StatisticsManagerControlService.class, this));
    }

    @Override
    public Future<RpcResult<GetStatisticsWorkModeOutput>> getStatisticsWorkMode() {
        return RpcResultBuilder.success(new GetStatisticsWorkModeOutputBuilder()
                .setMode(workMode)
                .build()).buildFuture();
    }

    @Override
    public Future<RpcResult<Void>> changeStatisticsWorkMode(ChangeStatisticsWorkModeInput input) {
        if (workModeGuard.tryAcquire()) {
            final StatisticsWorkMode targetWorkMode = input.getMode();
            isStatisticsFullyDisabled = StatisticsWorkMode.FULLYDISABLED.equals(targetWorkMode);

            contexts.values().forEach(context -> {
                switch (targetWorkMode) {
                    case COLLECTALL:
                        context.setSchedulingEnabled(true);
                    case FULLYDISABLED:
                        context.setSchedulingEnabled(false);
                        break;
                    default:
                        LOG.warn("Statistics work mode not supported: {}", targetWorkMode);
                }
            });

            workModeGuard.release();
            return RpcResultBuilder.<Void>success().buildFuture();
        }

        return RpcResultBuilder.<Void>failed()
                .withError(RpcError.ErrorType.APPLICATION, "mode change already in progress")
                .buildFuture();
    }

    @Override
    public void setReconciliationFrameworkRegistrar(@Nonnull ReconciliationFrameworkRegistrar rfRegistrar) {
        this.reconciliationFrameworkRegistrar = rfRegistrar;
    }

    @Override
    public StatisticsContext createContext(@Nonnull final DeviceContext deviceContext) {
        final MultipartWriterProvider statisticsWriterProvider = MultipartWriterProviderFactory
                .createDefaultProvider(deviceContext);

        final StatisticsContext statisticsContext =
                deviceContext.canUseSingleLayerSerialization() ?
                        new StatisticsContextImpl<MultipartReply>(
                                deviceContext,
                                converterExecutor,
                                statisticsWriterProvider,
                                !isStatisticsFullyDisabled && config.isIsStatisticsPollingOn(),
                                reconciliationFrameworkRegistrar.isReconciliationFrameworkRegistered(),
                                config.getBasicTimerDelay().getValue(),
                                config.getMaximumTimerDelay().getValue()) :
                        new StatisticsContextImpl<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
                                .MultipartReply>(
                                deviceContext,
                                converterExecutor,
                                statisticsWriterProvider,
                                !isStatisticsFullyDisabled && config.isIsStatisticsPollingOn(),
                                reconciliationFrameworkRegistrar.isReconciliationFrameworkRegistered(),
                                config.getBasicTimerDelay().getValue(),
                                config.getMaximumTimerDelay().getValue());

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