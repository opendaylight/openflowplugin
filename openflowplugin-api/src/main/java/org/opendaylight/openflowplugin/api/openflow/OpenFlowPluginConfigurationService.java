/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow;

import java.util.Map;

/**
 * Manages OpenFlowPlugin configuration
 */
public interface OpenFlowPluginConfigurationService {

    /**
     * Parses key-value pairs of properties and updates them
     *
     * @param properties properties
     */
    void update(Map<String,Object> properties);

    /**
     * This parameter indicates whether it is mandatory for switch to support OF1.3 features : table, flow, meter,group.
     * If this is set to true and switch doesn't support these features its connection will be denied.
     *
     * @param switchFeaturesMandatory the switch features mandatory
     */
    void updateSwitchFeaturesMandatory(boolean switchFeaturesMandatory);

    /**
     * Update is statistics polling on.
     *
     * @param isStatisticsPollingOn the is statistics polling on
     */
    void updateIsStatisticsPollingOn(boolean isStatisticsPollingOn);

    /**
     * Backward compatibility feature - exposing rpc for statistics polling (result is provided in form of async notification)
     *
     * @param isStatisticsRpcEnabled the is statistics rpc enabled
     */
    void updateIsStatisticsRpcEnabled(boolean isStatisticsRpcEnabled);

    /**
     * Update barrier count limit.
     *
     * @param barrierCountLimit the barrier count limit
     */
    void updateBarrierCountLimit(int barrierCountLimit);

    /**
     * Update barrier interval timeout limit.
     *
     * @param barrierTimeoutLimit the barrier timeout limit
     */
    void updateBarrierIntervalTimeoutLimit(long barrierTimeoutLimit);

    /**
     * Update echo reply timeout.
     *
     * @param echoReplyTimeout the echo reply timeout
     */
    void updateEchoReplyTimeout(long echoReplyTimeout);

    /**
     * Update enable flow removed notification.
     *
     * @param isFlowRemovedNotificationOn the is flow removed notification on
     */
    void updateEnableFlowRemovedNotification(boolean isFlowRemovedNotificationOn);

    /**
     * Update skip table features.
     *
     * @param skipTableFeatures the skip table features
     */
    void updateSkipTableFeatures(boolean skipTableFeatures);

    /**
     * Update basic timer delay.
     *
     * @param basicTimerDelay the basic timer delay
     */
    void updateBasicTimerDelay(long basicTimerDelay);

    /**
     * Update maximum timer delay.
     *
     * @param maximumTimerDelay the maximum timer delay
     */
    void updateMaximumTimerDelay(long maximumTimerDelay);

    /**
     * Update use single layer serialization.
     *
     * @param useSingleLayerSerialization the use single layer serialization
     */
    void updateUseSingleLayerSerialization(boolean useSingleLayerSerialization);

    /**
     * Update rpc requests quota.
     *
     * @param rpcRequestsQuota the rpc requests quota
     */
    void updateRpcRequestsQuota(int rpcRequestsQuota);

    /**
     * Update global notification quota.
     *
     * @param globalNotificationQuota the global notification quota
     */
    void updateGlobalNotificationQuota(long globalNotificationQuota);

    /**
     * Update thread pool min threads.
     *
     * @param threadPoolMinThreads the thread pool min threads
     */
    void updateThreadPoolMinThreads(int threadPoolMinThreads);

    /**
     * Update thread pool max threads.
     *
     * @param threadPoolMaxThreads the thread pool max threads
     */
    void updateThreadPoolMaxThreads(int threadPoolMaxThreads);

    /**
     * Update thread pool timeout.
     *
     * @param threadPoolTimeout the thread pool timeout
     */
    void updateThreadPoolTimeout(long threadPoolTimeout);

}
