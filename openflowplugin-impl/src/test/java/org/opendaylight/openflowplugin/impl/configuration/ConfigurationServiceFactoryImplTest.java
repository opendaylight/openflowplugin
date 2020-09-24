/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Hashtable;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationListener;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationProperty;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint16Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint32Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceFactoryImplTest {
    private static final int CONFIG_PROP_COUNT = 25;
    private static final boolean IS_STATISTICS_POLLING_ON = true;
    private static final Uint16 BARRIER_COUNT_LIMIT = Uint16.valueOf(2000);
    private static final Uint32 BARRIER_INTERVAL_TIMEOUT_LIMIT = Uint32.valueOf(3000);
    private static final Uint32 ECHO_REPLY_TIMEOUT = Uint32.valueOf(4000);
    private static final boolean ENABLE_FLOW_REMOVED_NOTIFICATION = true;
    private static final boolean SKIP_TABLE_FEATURES = true;
    private static final Uint32 BASIC_TIMER_DELAY = Uint32.valueOf(2690);
    private static final Uint32 MAXIMUM_TIMER_DELAY = Uint32.valueOf(3679);
    private static final boolean SWITCH_FEATURES_MANDATORY = false;
    private static final boolean IS_STATISTICS_RPC_ENABLED = false;
    private static final boolean USE_SINGLE_LAYER_SERIALIZATION = true;
    private static final Uint16 RPC_REQUESTS_QUOTA = Uint16.valueOf(2500);
    private static final Uint32 GLOBAL_NOTIFICATION_QUOTA = Uint32.valueOf(9000);
    private static final Uint16 THREAD_POOL_MIN_THREADS = Uint16.valueOf(3);
    private static final int THREAD_POOL_MIN_THREADS_UPDATE = 4;
    private static final Uint16 THREAD_POOL_MAX_THREADS = Uint16.valueOf(1000);
    private static final Uint32 THREAD_POOL_TIMEOUT = Uint32.valueOf(60);
    private static final Uint16 DEVICE_CONNECTION_RATE_LIMIT_PER_MIN = Uint16.ZERO;
    private static final Uint16 DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS = Uint16.valueOf(60);
    private static final Uint32 DEVICE_DATASTORE_REMOVAL_DELAY = Uint32.valueOf(500);

    @Mock
    private OpenflowProviderConfig config;

    @Mock
    private ConfigurationListener configurationListener;

    private ConfigurationService configurationService;

    @Before
    public void setUp() {
        when(config.isIsStatisticsPollingOn()).thenReturn(IS_STATISTICS_POLLING_ON);
        when(config.isIsFlowStatisticsPollingOn()).thenReturn(IS_STATISTICS_POLLING_ON);
        when(config.isIsTableStatisticsPollingOn()).thenReturn(IS_STATISTICS_POLLING_ON);
        when(config.isIsFlowStatisticsPollingOn()).thenReturn(IS_STATISTICS_POLLING_ON);
        when(config.isIsGroupStatisticsPollingOn()).thenReturn(IS_STATISTICS_POLLING_ON);
        when(config.isIsMeterStatisticsPollingOn()).thenReturn(IS_STATISTICS_POLLING_ON);
        when(config.isIsQueueStatisticsPollingOn()).thenReturn(IS_STATISTICS_POLLING_ON);
        when(config.isIsPortStatisticsPollingOn()).thenReturn(IS_STATISTICS_POLLING_ON);
        when(config.getBarrierCountLimit()).thenReturn(new NonZeroUint16Type(BARRIER_COUNT_LIMIT));
        when(config.getBarrierIntervalTimeoutLimit()).thenReturn(new NonZeroUint32Type(BARRIER_INTERVAL_TIMEOUT_LIMIT));
        when(config.getEchoReplyTimeout()).thenReturn(new NonZeroUint32Type(ECHO_REPLY_TIMEOUT));
        when(config.isEnableFlowRemovedNotification()).thenReturn(ENABLE_FLOW_REMOVED_NOTIFICATION);
        when(config.isSkipTableFeatures()).thenReturn(SKIP_TABLE_FEATURES);
        when(config.getBasicTimerDelay()).thenReturn(new NonZeroUint32Type(BASIC_TIMER_DELAY));
        when(config.getMaximumTimerDelay()).thenReturn(new NonZeroUint32Type(MAXIMUM_TIMER_DELAY));
        when(config.isSwitchFeaturesMandatory()).thenReturn(SWITCH_FEATURES_MANDATORY);
        when(config.isIsStatisticsRpcEnabled()).thenReturn(IS_STATISTICS_RPC_ENABLED);
        when(config.isUseSingleLayerSerialization()).thenReturn(USE_SINGLE_LAYER_SERIALIZATION);
        when(config.getRpcRequestsQuota()).thenReturn(new NonZeroUint16Type(RPC_REQUESTS_QUOTA));
        when(config.getGlobalNotificationQuota()).thenReturn(GLOBAL_NOTIFICATION_QUOTA);
        when(config.getThreadPoolMinThreads()).thenReturn(THREAD_POOL_MIN_THREADS);
        when(config.getThreadPoolMaxThreads()).thenReturn(new NonZeroUint16Type(THREAD_POOL_MAX_THREADS));
        when(config.getThreadPoolTimeout()).thenReturn(THREAD_POOL_TIMEOUT);
        when(config.getDeviceConnectionRateLimitPerMin()).thenReturn(DEVICE_CONNECTION_RATE_LIMIT_PER_MIN);
        when(config.getDeviceConnectionHoldTimeInSeconds()).thenReturn(DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS);
        when(config.getDeviceDatastoreRemovalDelay()).thenReturn(new NonZeroUint32Type(DEVICE_DATASTORE_REMOVAL_DELAY));

        final Map<String, String> properties = new Hashtable<>();
        properties.put(ConfigurationProperty.IS_STATISTICS_POLLING_ON.toString(),
                Boolean.toString(IS_STATISTICS_POLLING_ON));

        configurationService = new ConfigurationServiceFactoryImpl().newInstance(config);
        configurationService.update(properties);
    }

    @Test
    public void update() {
        final int tpMinThreads = configurationService
                .getProperty(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString(), Integer::valueOf);

        configurationService.update(ImmutableMap
                .<String, String>builder()
                .put(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString(), String
                        .valueOf(THREAD_POOL_MIN_THREADS_UPDATE))
                .build());

        final int tpMinThreadsNew = configurationService
                .getProperty(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString(), Integer::valueOf);

        assertNotEquals(tpMinThreadsNew, tpMinThreads);
        assertEquals(tpMinThreadsNew, THREAD_POOL_MIN_THREADS_UPDATE);
    }

    @Test
    public void getProperty() {
        final int tpMaxThreads = configurationService
                .getProperty(ConfigurationProperty.THREAD_POOL_MAX_THREADS.toString(), Integer::valueOf);

        assertEquals(THREAD_POOL_MAX_THREADS.intValue(), tpMaxThreads);
    }

    @Test
    public void registerListener() {
        configurationService.registerListener(configurationListener);
        verify(configurationListener, times(CONFIG_PROP_COUNT)).onPropertyChanged(any(), any());
    }

    @Test(expected = NumberFormatException.class)
    public void close() throws Exception {
        configurationService.close();
        configurationService.getProperty(ConfigurationProperty.THREAD_POOL_MAX_THREADS.toString(), Integer::valueOf);
    }
}
