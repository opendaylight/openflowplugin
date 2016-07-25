/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.device
 * <p/>
 * DeviceState is builded from {@link FeaturesReply} and {@link NodeId}. Both values are inside
 * {@link org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext}
 *
 */
class DeviceStateImpl implements DeviceState {

    private boolean meterIsAvailable;
    private boolean groupIsAvailable;
    private boolean flowStatisticsAvailable;
    private boolean tableStatisticsAvailable;
    private boolean portStatisticsAvailable;
    private boolean statPollEnabled;
    private boolean queueStatisticsAvailable;

    public DeviceStateImpl() {
        statPollEnabled = false;
    }

    @Override
    public boolean isMetersAvailable() {
        return meterIsAvailable;
    }

    @Override
    public void setMeterAvailable(final boolean available) {
        meterIsAvailable = available;
    }

    @Override
    public boolean isGroupAvailable() {
        return groupIsAvailable;
    }

    @Override
    public void setGroupAvailable(final boolean available) {
        groupIsAvailable = available;
    }

    @Override
    public boolean isFlowStatisticsAvailable() {
        return flowStatisticsAvailable;
    }

    @Override
    public void setFlowStatisticsAvailable(final boolean available) {
        flowStatisticsAvailable = available;
    }

    @Override
    public boolean isTableStatisticsAvailable() {
        return tableStatisticsAvailable;
    }

    @Override
    public void setTableStatisticsAvailable(final boolean available) {
        tableStatisticsAvailable = available;
    }

    @Override
    public boolean isPortStatisticsAvailable() {
        return portStatisticsAvailable;
    }

    @Override
    public void setPortStatisticsAvailable(final boolean available) {
        portStatisticsAvailable = available;
    }

    @Override
    public boolean isQueueStatisticsAvailable() {
        return queueStatisticsAvailable;
    }

    @Override
    public void setQueueStatisticsAvailable(final boolean available) {
        queueStatisticsAvailable = available;

    }

    @Override
    public boolean isStatisticsPollingEnabled() {
        return statPollEnabled;
    }

}
