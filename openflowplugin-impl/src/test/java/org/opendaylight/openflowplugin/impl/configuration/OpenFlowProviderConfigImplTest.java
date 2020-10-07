/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.configuration;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationProperty;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class OpenFlowProviderConfigImplTest {
    private static final Boolean IS_STATISTICS_POLLING_ON = true;
    private static final Uint16 BARRIER_COUNT_LIMIT = Uint16.valueOf(2000);
    private static final Uint32 BARRIER_INTERVAL_TIMEOUT_LIMIT = Uint32.valueOf(3000);
    private static final Uint32 ECHO_REPLY_TIMEOUT = Uint32.valueOf(4000);
    private static final Boolean ENABLE_FLOW_REMOVED_NOTIFICATION = true;
    private static final Boolean SKIP_TABLE_FEATURES = true;
    private static final Uint32 BASIC_TIMER_DELAY = Uint32.valueOf(2690);
    private static final Uint32 MAXIMUM_TIMER_DELAY = Uint32.valueOf(3679);
    private static final Boolean SWITCH_FEATURES_MANDATORY = false;
    private static final Boolean IS_STATISTICS_RPC_ENABLED = false;
    private static final Boolean USE_SINGLE_LAYER_SERIALIZATION = true;
    private static final Uint16 RPC_REQUESTS_QUOTA = Uint16.valueOf(2500);
    private static final Uint32 GLOBAL_NOTIFICATION_QUOTA = Uint32.valueOf(9000);
    private static final Uint16 THREAD_POOL_MIN_THREADS = Uint16.valueOf(3);
    private static final Uint16 THREAD_POOL_MAX_THREADS = Uint16.valueOf(1000);
    private static final Uint32 THREAD_POOL_TIMEOUT = Uint32.valueOf(60);
    private static final Uint16 DEVICE_CONNECTION_RATE_LIMIT_PER_MIN = Uint16.ZERO;
    private static final Uint16 DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS = Uint16.valueOf(60);
    private static final Uint32 DEVICE_DATASTORE_REMOVAL_DELAY = Uint32.valueOf(500);

    @Mock
    private ConfigurationService configurationService;
    private OpenflowProviderConfig openflowProviderConfig;

    @Before
    public void setUp() {
        when(configurationService.getProperty(eq(ConfigurationProperty.IS_STATISTICS_POLLING_ON.toString()), any()))
                .thenReturn(IS_STATISTICS_POLLING_ON);
        when(configurationService.getProperty(eq(ConfigurationProperty.BARRIER_COUNT_LIMIT.toString()), any()))
                .thenReturn(BARRIER_COUNT_LIMIT);
        when(configurationService.getProperty(eq(ConfigurationProperty.BARRIER_INTERVAL_TIMEOUT_LIMIT.toString()),
                any())).thenReturn(BARRIER_INTERVAL_TIMEOUT_LIMIT);
        when(configurationService.getProperty(eq(ConfigurationProperty.ECHO_REPLY_TIMEOUT.toString()), any()))
                .thenReturn(ECHO_REPLY_TIMEOUT);
        when(configurationService.getProperty(eq(ConfigurationProperty.ENABLE_FLOW_REMOVED_NOTIFICATION.toString()),
                any())).thenReturn(ENABLE_FLOW_REMOVED_NOTIFICATION);
        when(configurationService.getProperty(eq(ConfigurationProperty.SKIP_TABLE_FEATURES.toString()), any()))
                .thenReturn(SKIP_TABLE_FEATURES);
        when(configurationService.getProperty(eq(ConfigurationProperty.BASIC_TIMER_DELAY.toString()), any()))
                .thenReturn(BASIC_TIMER_DELAY);
        when(configurationService.getProperty(eq(ConfigurationProperty.MAXIMUM_TIMER_DELAY.toString()), any()))
                .thenReturn(MAXIMUM_TIMER_DELAY);
        when(configurationService.getProperty(eq(ConfigurationProperty.SWITCH_FEATURES_MANDATORY.toString()), any()))
                .thenReturn(SWITCH_FEATURES_MANDATORY);
        when(configurationService.getProperty(eq(ConfigurationProperty.IS_STATISTICS_RPC_ENABLED.toString()), any()))
                .thenReturn(IS_STATISTICS_RPC_ENABLED);
        when(configurationService.getProperty(eq(ConfigurationProperty.USE_SINGLE_LAYER_SERIALIZATION.toString()),
                any())).thenReturn(USE_SINGLE_LAYER_SERIALIZATION);
        when(configurationService.getProperty(eq(ConfigurationProperty.RPC_REQUESTS_QUOTA.toString()), any()))
                .thenReturn(RPC_REQUESTS_QUOTA);
        when(configurationService.getProperty(eq(ConfigurationProperty.GLOBAL_NOTIFICATION_QUOTA.toString()), any()))
                .thenReturn(GLOBAL_NOTIFICATION_QUOTA);
        when(configurationService.getProperty(eq(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString()), any()))
                .thenReturn(THREAD_POOL_MIN_THREADS);
        when(configurationService.getProperty(eq(ConfigurationProperty.THREAD_POOL_MAX_THREADS.toString()), any()))
                .thenReturn(THREAD_POOL_MAX_THREADS);
        when(configurationService.getProperty(eq(ConfigurationProperty.THREAD_POOL_TIMEOUT.toString()), any()))
                .thenReturn(THREAD_POOL_TIMEOUT);
        when(configurationService.getProperty(eq(ConfigurationProperty.DEVICE_CONNECTION_RATE_LIMIT_PER_MIN.toString()),
                any())).thenReturn(DEVICE_CONNECTION_RATE_LIMIT_PER_MIN);
        when(configurationService.getProperty(
                eq(ConfigurationProperty.DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS.toString()),
                any())).thenReturn(DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS);
        when(configurationService.getProperty(eq(ConfigurationProperty.DEVICE_DATASTORE_REMOVAL_DELAY.toString()),
                any())).thenReturn(DEVICE_DATASTORE_REMOVAL_DELAY);
        openflowProviderConfig = new OpenFlowProviderConfigImpl(configurationService);
    }

    @After
    public void tearDown() throws Exception {
        configurationService.close();
    }

    @Test
    public void getRpcRequestsQuota() {
        assertEquals(RPC_REQUESTS_QUOTA, openflowProviderConfig.getRpcRequestsQuota().getValue());
    }

    @Test
    public void isSwitchFeaturesMandatory() {
        assertEquals(SWITCH_FEATURES_MANDATORY, openflowProviderConfig.isSwitchFeaturesMandatory());
    }

    @Test
    public void getGlobalNotificationQuota() {
        assertEquals(GLOBAL_NOTIFICATION_QUOTA, openflowProviderConfig.getGlobalNotificationQuota());
    }

    @Test
    public void isIsStatisticsPollingOn() {
        assertEquals(IS_STATISTICS_POLLING_ON, openflowProviderConfig.isIsStatisticsPollingOn());
    }

    @Test
    public void isIsStatisticsRpcEnabled() {
        assertEquals(IS_STATISTICS_RPC_ENABLED, openflowProviderConfig.isIsStatisticsRpcEnabled());
    }

    @Test
    public void getBarrierIntervalTimeoutLimit() {
        assertEquals(BARRIER_INTERVAL_TIMEOUT_LIMIT,
            openflowProviderConfig.getBarrierIntervalTimeoutLimit().getValue());
    }

    @Test
    public void getBarrierCountLimit() {
        assertEquals(BARRIER_COUNT_LIMIT, openflowProviderConfig.getBarrierCountLimit().getValue());
    }

    @Test
    public void getEchoReplyTimeout() {
        assertEquals(ECHO_REPLY_TIMEOUT, openflowProviderConfig.getEchoReplyTimeout().getValue());
    }

    @Test
    public void getThreadPoolMinThreads() {
        assertEquals(THREAD_POOL_MIN_THREADS, openflowProviderConfig.getThreadPoolMinThreads());
    }

    @Test
    public void getThreadPoolMaxThreads() {
        assertEquals(THREAD_POOL_MAX_THREADS, openflowProviderConfig.getThreadPoolMaxThreads().getValue());
    }

    @Test
    public void getThreadPoolTimeout() {
        assertEquals(THREAD_POOL_TIMEOUT, openflowProviderConfig.getThreadPoolTimeout());
    }

    @Test
    public void isEnableFlowRemovedNotification() {
        assertEquals(ENABLE_FLOW_REMOVED_NOTIFICATION, openflowProviderConfig.isEnableFlowRemovedNotification());
    }

    @Test
    public void isSkipTableFeatures() {
        assertEquals(SKIP_TABLE_FEATURES, openflowProviderConfig.isSkipTableFeatures());
    }

    @Test
    public void getBasicTimerDelay() {
        assertEquals(BASIC_TIMER_DELAY, openflowProviderConfig.getBasicTimerDelay().getValue());
    }

    @Test
    public void getMaximumTimerDelay() {
        assertEquals(MAXIMUM_TIMER_DELAY, openflowProviderConfig.getMaximumTimerDelay().getValue());
    }

    @Test
    public void isUseSingleLayerSerialization() {
        assertEquals(USE_SINGLE_LAYER_SERIALIZATION, openflowProviderConfig.isUseSingleLayerSerialization());
    }

    @Test
    public void getDeviceConnectionRateLimitPerMin() {
        assertEquals(DEVICE_CONNECTION_RATE_LIMIT_PER_MIN, openflowProviderConfig.getDeviceConnectionRateLimitPerMin());
    }

    @Test
    public void getDeviceConnectionHoldTimeInSeconds() {
        assertEquals(DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS,
                openflowProviderConfig.getDeviceConnectionHoldTimeInSeconds());
    }

    @Test
    public void getDeviceDatastoreRemovalDelay() {
        assertEquals(DEVICE_DATASTORE_REMOVAL_DELAY,
                openflowProviderConfig.getDeviceDatastoreRemovalDelay().getValue());
    }

}