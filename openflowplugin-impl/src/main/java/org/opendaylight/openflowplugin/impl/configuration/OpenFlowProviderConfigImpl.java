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
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

public class OpenFlowProviderConfigImpl implements OpenflowProviderConfig {
    private final ConfigurationService service;

    public OpenFlowProviderConfigImpl(final ConfigurationService service) {
        this.service = service;
    }

    @Override
    public NonZeroUint16Type getRpcRequestsQuota() {
        final Integer property = service.getProperty(
                ConfigurationProperty.RPC_REQUESTS_QUOTA.toString(),
                Integer::valueOf);

        return new NonZeroUint16Type(property);
    }

    @Override
    public Boolean isSwitchFeaturesMandatory() {
        return service.getProperty(ConfigurationProperty.SWITCH_FEATURES_MANDATORY.toString(), Boolean::valueOf);
    }

    @Override
    public Uint32 getGlobalNotificationQuota() {
        return service.getProperty(ConfigurationProperty.GLOBAL_NOTIFICATION_QUOTA.toString(), Uint32::valueOf);
    }

    @Override
    public Boolean isIsStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean isIsTableStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_TABLE_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean isIsFlowStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_FLOW_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean isIsGroupStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_GROUP_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean isIsMeterStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_METER_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean isIsQueueStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_QUEUE_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean isIsPortStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_PORT_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
    }


    @Override
    public Boolean isIsStatisticsRpcEnabled() {
        return service.getProperty(ConfigurationProperty.IS_STATISTICS_RPC_ENABLED.toString(), Boolean::valueOf);
    }

    @Override
    public NonZeroUint32Type getBarrierIntervalTimeoutLimit() {
        final Long property = service.getProperty(
                ConfigurationProperty.BARRIER_INTERVAL_TIMEOUT_LIMIT.toString(),
                Long::valueOf);

        return new NonZeroUint32Type(property);
    }

    @Override
    public NonZeroUint16Type getBarrierCountLimit() {
        final Integer property = service.getProperty(
                ConfigurationProperty.BARRIER_COUNT_LIMIT.toString(),
                Integer::valueOf);

        return new NonZeroUint16Type(property);
    }

    @Override
    public NonZeroUint32Type getEchoReplyTimeout() {
        final Long property = service.getProperty(
                ConfigurationProperty.ECHO_REPLY_TIMEOUT.toString(),
                Long::valueOf);

        return new NonZeroUint32Type(property);
    }

    @Override
    public Uint16 getThreadPoolMinThreads() {
        return service.getProperty(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString(), Uint16::valueOf);
    }

    @Override
    public NonZeroUint16Type getThreadPoolMaxThreads() {
        final Integer property = service.getProperty(
                ConfigurationProperty.THREAD_POOL_MAX_THREADS.toString(),
                Integer::valueOf);

        return new NonZeroUint16Type(property);
    }

    @Override
    public Uint32 getThreadPoolTimeout() {
        return service.getProperty(ConfigurationProperty.THREAD_POOL_TIMEOUT.toString(), Uint32::valueOf);
    }

    @Override
    public Boolean isEnableFlowRemovedNotification() {
        return service.getProperty(ConfigurationProperty.ENABLE_FLOW_REMOVED_NOTIFICATION.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean isSkipTableFeatures() {
        return service.getProperty(ConfigurationProperty.SKIP_TABLE_FEATURES.toString(), Boolean::valueOf);
    }

    @Override
    public Boolean isEnableEqualRole() {
        return service.getProperty(ConfigurationProperty.ENABLE_EQUAL_ROLE.toString(), Boolean::valueOf);
    }

    @Override
    public NonZeroUint32Type getBasicTimerDelay() {
        final Long property = service.getProperty(
                ConfigurationProperty.BASIC_TIMER_DELAY.toString(),
                Long::valueOf);

        return new NonZeroUint32Type(property);
    }

    @Override
    public NonZeroUint32Type getMaximumTimerDelay() {
        final Long property = service.getProperty(
                ConfigurationProperty.MAXIMUM_TIMER_DELAY.toString(),
                Long::valueOf);

        return new NonZeroUint32Type(property);
    }

    @Override
    public Boolean isUseSingleLayerSerialization() {
        return service.getProperty(ConfigurationProperty.USE_SINGLE_LAYER_SERIALIZATION.toString(), Boolean::valueOf);
    }

    @Override
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang
            .openflow.provider.config.rev160510.OpenflowProviderConfig>> E augmentation(
                    final java.lang.Class<E> augmentationType) {
        return null;
    }

    @Override
    public Uint16 getDeviceConnectionRateLimitPerMin() {
        return service.getProperty(ConfigurationProperty.DEVICE_CONNECTION_RATE_LIMIT_PER_MIN.toString(),
                Uint16::valueOf);
    }

    @Override
    public Uint16 getDeviceConnectionHoldTimeInSeconds() {
        return service.getProperty(ConfigurationProperty.DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS.toString(),
                Uint16::valueOf);
    }

    @Override
    public NonZeroUint32Type getDeviceDatastoreRemovalDelay() {
        final Long property = service.getProperty(
                ConfigurationProperty.DEVICE_DATASTORE_REMOVAL_DELAY.toString(),
                Long::valueOf);

        return new NonZeroUint32Type(property);
    }

    @Override
    public Boolean isEnableCustomTrustManager() {
        return service.getProperty(ConfigurationProperty.ENABLE_CUSTOM_TRUST_MANAGER.toString(),
                Boolean::valueOf);
    }
}
