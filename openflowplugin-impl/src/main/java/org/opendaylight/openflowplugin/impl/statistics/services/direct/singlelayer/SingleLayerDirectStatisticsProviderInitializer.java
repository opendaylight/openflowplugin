/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractFlowDirectStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractGroupDirectStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractMeterDirectStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractPortDirectStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractQueueDirectStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.OpendaylightDirectStatisticsServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;

/**
 * Utility class for instantiating #{@link org.opendaylight.openflowplugin.impl.statistics.services.direct.OpendaylightDirectStatisticsServiceProvider}
 * with all multi-layer services already in
 */
public class SingleLayerDirectStatisticsProviderInitializer {

    public static OpendaylightDirectStatisticsServiceProvider createProvider(
        final RequestContextStack requestContextStack,
        final DeviceContext deviceContext,
        final ConverterExecutor converterExecutor,
        final MultipartWriterProvider statisticsWriterProvider) {

        final OpendaylightDirectStatisticsServiceProvider provider = new OpendaylightDirectStatisticsServiceProvider();

        provider.register(AbstractFlowDirectStatisticsService.class, new FlowDirectStatisticsService(
            requestContextStack, deviceContext, converterExecutor, statisticsWriterProvider));
        provider.register(AbstractGroupDirectStatisticsService.class, new GroupDirectStatisticsService(
            requestContextStack, deviceContext, converterExecutor, statisticsWriterProvider));
        provider.register(AbstractMeterDirectStatisticsService.class, new MeterDirectStatisticsService(
            requestContextStack, deviceContext, converterExecutor, statisticsWriterProvider));
        provider.register(AbstractPortDirectStatisticsService.class, new PortDirectStatisticsService(
            requestContextStack, deviceContext, converterExecutor, statisticsWriterProvider));
        provider.register(AbstractQueueDirectStatisticsService.class, new QueueDirectStatisticsService(
            requestContextStack, deviceContext, converterExecutor, statisticsWriterProvider));

        return provider;
    }

}
