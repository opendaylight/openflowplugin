/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.configuration;

import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationProperty;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint16Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint32Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yangtools.yang.binding.AbstractAugmentable;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

// FIXME: Rename this to "ConfigurationOpenflowProviderConfig" or some such. OpenFlowProviderConfigImpl is taken as
//        Builder-based (guaranteed to work).
public class OpenFlowProviderConfigImpl extends AbstractAugmentable<OpenflowProviderConfig>
        implements OpenflowProviderConfig {
    private final ConfigurationService service;

    public OpenFlowProviderConfigImpl(final ConfigurationService service) {
        this.service = service;
    }

    @Override
    public NonZeroUint16Type getRpcRequestsQuota() {
        return new NonZeroUint16Type(service.<Uint16>getProperty(
            ConfigurationProperty.RPC_REQUESTS_QUOTA.toString(), Uint16::valueOf));
    }

    @Override
    public Boolean getSwitchFeaturesMandatory() {
        return service.getProperty(ConfigurationProperty.SWITCH_FEATURES_MANDATORY.toString(), Boolean::valueOf);
    }

    @Override
    public Uint32 getGlobalNotificationQuota() {
        return service.getProperty(ConfigurationProperty.GLOBAL_NOTIFICATION_QUOTA.toString(), Uint32::valueOf);
    }

    @Override
    public Boolean getIsStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean getIsTableStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_TABLE_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean getIsFlowStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_FLOW_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean getIsGroupStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_GROUP_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean getIsMeterStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_METER_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean getIsQueueStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_QUEUE_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean getIsPortStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_PORT_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean getIsStatisticsRpcEnabled() {
        return service.getProperty(ConfigurationProperty.IS_STATISTICS_RPC_ENABLED.toString(), Boolean::valueOf);
    }

    @Override
    public NonZeroUint32Type getBarrierIntervalTimeoutLimit() {
        return new NonZeroUint32Type(service.<Uint32>getProperty(
            ConfigurationProperty.BARRIER_INTERVAL_TIMEOUT_LIMIT.toString(), Uint32::valueOf));
    }

    @Override
    public NonZeroUint16Type getBarrierCountLimit() {
        return new NonZeroUint16Type(service.<Uint16>getProperty(
            ConfigurationProperty.BARRIER_COUNT_LIMIT.toString(), Uint16::valueOf));
    }

    @Override
    public NonZeroUint32Type getEchoReplyTimeout() {
        return new NonZeroUint32Type(service.<Uint32>getProperty(
            ConfigurationProperty.ECHO_REPLY_TIMEOUT.toString(), Uint32::valueOf));
    }

    @Override
    public Uint16 getThreadPoolMinThreads() {
        return service.<Uint16>getProperty(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString(), Uint16::valueOf);
    }

    @Override
    public NonZeroUint16Type getThreadPoolMaxThreads() {
        return new NonZeroUint16Type(service.<Uint16>getProperty(
            ConfigurationProperty.THREAD_POOL_MAX_THREADS.toString(), Uint16::valueOf));
    }

    @Override
    public Uint32 getThreadPoolTimeout() {
        return service.getProperty(ConfigurationProperty.THREAD_POOL_TIMEOUT.toString(), Uint32::valueOf);
    }

    @Override
    public Boolean getEnableFlowRemovedNotification() {
        return service.getProperty(ConfigurationProperty.ENABLE_FLOW_REMOVED_NOTIFICATION.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean getSkipTableFeatures() {
        return service.getProperty(ConfigurationProperty.SKIP_TABLE_FEATURES.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean getEnableEqualRole() {
        return service.getProperty(ConfigurationProperty.ENABLE_EQUAL_ROLE.toString(), Boolean::valueOf);
    }

    @Override
    public NonZeroUint32Type getBasicTimerDelay() {
        return new NonZeroUint32Type(service.<Uint32>getProperty(
            ConfigurationProperty.BASIC_TIMER_DELAY.toString(), Uint32::valueOf));
    }

    @Override
    public NonZeroUint32Type getMaximumTimerDelay() {
        return new NonZeroUint32Type(service.<Uint32>getProperty(
            ConfigurationProperty.MAXIMUM_TIMER_DELAY.toString(), Uint32::valueOf));
    }

    @Override
    public Boolean getUseSingleLayerSerialization() {
        return service.getProperty(ConfigurationProperty.USE_SINGLE_LAYER_SERIALIZATION.toString(), Boolean::valueOf);
    }

    @Override
    public Uint16 getDeviceConnectionRateLimitPerMin() {
        return service.getProperty(
            ConfigurationProperty.DEVICE_CONNECTION_RATE_LIMIT_PER_MIN.toString(), Uint16::valueOf);
    }

    @Override
    public Uint16 getDeviceConnectionHoldTimeInSeconds() {
        return service.getProperty(
            ConfigurationProperty.DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS.toString(), Uint16::valueOf);
    }

    @Override
    public NonZeroUint32Type getDeviceDatastoreRemovalDelay() {
        return new NonZeroUint32Type(service.<Uint32>getProperty(
            ConfigurationProperty.DEVICE_DATASTORE_REMOVAL_DELAY.toString(), Uint32::valueOf));
    }

    @Override
    public Boolean getEnableCustomTrustManager() {
        return service.getProperty(ConfigurationProperty.ENABLE_CUSTOM_TRUST_MANAGER.toString(),
                Boolean::valueOf);
    }
}
