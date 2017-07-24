package org.opendaylight.openflowplugin.impl.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationListener;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationProperty;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint16Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint32Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.osgi.framework.BundleContext;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceImplTest {
    private static final boolean IS_STATISTICS_POLLING_ON = true;
    private static final int BARRIER_COUNT_LIMIT = 2000;
    private static final long BARRIER_INTERVAL_TIMEOUT_LIMIT = 3000;
    private static final long ECHO_REPLY_TIMEOUT = 4000;
    private static final boolean ENABLE_FLOW_REMOVED_NOTIFICATION = true;
    private static final boolean SKIP_TABLE_FEATURES = true;
    private static final long BASIC_TIMER_DELAY = 2690;
    private static final long MAXIMUM_TIMER_DELAY = 3679;
    private static final boolean SWITCH_FEATURES_MANDATORY = false;
    private static final boolean IS_STATISTICS_RPC_ENABLED = false;
    private static final boolean USE_SINGLE_LAYER_SERIALIZATION = true;
    private static final int RPC_REQUESTS_QUOTA = 2500;
    private static final long GLOBAL_NOTIFICATION_QUOTA = 9000;
    private static final int THREAD_POOL_MIN_THREADS = 3;
    private static final int THREAD_POOL_MAX_THREADS = 1000;
    private static final long THREAD_POOL_TIMEOUT = 60;
    private static final boolean USING_RECONCILIATION_FRAMEWORK = true;

    @Mock
    private OpenflowProviderConfig config;

    @Mock
    private BundleContext bundleContext;

    @Mock
    private ConfigurationListener configurationListener;

    private ConfigurationService configurationService;

    @Before
    public void setUp() throws Exception {
        when(config.isIsStatisticsPollingOn()).thenReturn(IS_STATISTICS_POLLING_ON);
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
        when(config.isUsingReconciliationFramework()).thenReturn(USING_RECONCILIATION_FRAMEWORK);

        configurationService = new ConfigurationServiceFactoryImpl()
                .newInstance(config, bundleContext);
    }

    @Test
    public void update() throws Exception {
        final int tpMinThreads = configurationService
                .getProperty(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString(), Integer::valueOf);

        final int tpMinThreadsUpdate = 4;

        configurationService.update(ImmutableMap
                .<String, String>builder()
                .put(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString(), String.valueOf(4))
                .build());

        final int tpMinThreadsNew = configurationService
                .getProperty(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString(), Integer::valueOf);

        assertNotEquals(tpMinThreadsNew, tpMinThreads);
        assertEquals(tpMinThreadsNew, tpMinThreadsUpdate);
    }

    @Test
    public void getProperty() throws Exception {
        final int tpMaxThreads = configurationService
                .getProperty(ConfigurationProperty.THREAD_POOL_MAX_THREADS.toString(), Integer::valueOf);

        assertEquals(THREAD_POOL_MAX_THREADS, tpMaxThreads);
    }

    @Test
    public void registerListener() throws Exception {
        configurationService.registerListener(configurationListener);
        verify(configurationListener, times(17)).onPropertyChanged(any(), any());
    }

    @Test(expected = NumberFormatException.class)
    public void close() throws Exception {
       configurationService.close();
       configurationService.getProperty(ConfigurationProperty.THREAD_POOL_MAX_THREADS.toString(), Integer::valueOf);
    }

}