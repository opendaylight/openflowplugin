/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * This class is a binder between all managers
 * Should be defined in OpenFlowPluginProviderImpl
 */
public interface LifecycleConductor {

    /**
     * Returns device context from device manager device contexts maps
     *
     * @param deviceInfo@return null if context doesn't exists
     */
    DeviceContext getDeviceContext(DeviceInfo deviceInfo);


    /**
     * Setter for device manager once set it cant be unset or overwritten
     * @param manager
     */
    void setSafelyManager(OFPManager manager);

    /**
     * Registers ont time listener for notify when services rpc, statistics are done stop or start
     * @param manager service change listener
     * @param deviceInfo node identification
     */
    void addOneTimeListenerWhenServicesChangesDone(final ServiceChangeListener manager, final DeviceInfo deviceInfo);

    /**
     * Set new timeout for {@link io.netty.util.HashedWheelTimer}
     * @param task timer task
     * @param delay delay
     * @param unit time unit
     * @return new timeout
     */
    Timeout newTimeout(@Nonnull TimerTask task, long delay, @Nonnull TimeUnit unit);

    /**
     * Returns message intelligence agency
     * @return MessageIntelligenceAgency set by constructor
     */
    MessageIntelligenceAgency getMessageIntelligenceAgency();

    /**
     * Interrupt connection for the node
     * @param deviceInfo node identification
     */
    void closeConnection(final DeviceInfo deviceInfo);

    /**
     * Xid from outboundqueue
     * @param deviceInfo
     * @return
     */
    Long reserveXidForDeviceMessage(final DeviceInfo deviceInfo);

    NotificationPublishService getNotificationPublishService();

    void setNotificationPublishService(NotificationPublishService notificationPublishService);
}
