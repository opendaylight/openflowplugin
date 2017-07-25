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
import org.opendaylight.yangtools.yang.binding.DataContainer;

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
    public Long getGlobalNotificationQuota() {
        return service.getProperty(ConfigurationProperty.GLOBAL_NOTIFICATION_QUOTA.toString(), Long::valueOf);
    }

    @Override
    public Boolean isIsStatisticsPollingOn() {
        return service.getProperty(ConfigurationProperty.IS_STATISTICS_POLLING_ON.toString(), Boolean::valueOf);
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
    public Integer getThreadPoolMinThreads() {
        return service.getProperty(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString(), Integer::valueOf);
    }

    @Override
    public NonZeroUint16Type getThreadPoolMaxThreads() {
        final Integer property = service.getProperty(
                ConfigurationProperty.THREAD_POOL_MAX_THREADS.toString(),
                Integer::valueOf);

        return new NonZeroUint16Type(property);
    }

    @Override
    public Long getThreadPoolTimeout() {
        return service.getProperty(ConfigurationProperty.THREAD_POOL_TIMEOUT.toString(), Long::valueOf);
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
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig>> E getAugmentation(java.lang.Class<E> augmentationType) {
        return null;
    }

    @Override
    public Class<? extends DataContainer> getImplementedInterface() {
        return OpenflowProviderConfig.class;
    }

}
