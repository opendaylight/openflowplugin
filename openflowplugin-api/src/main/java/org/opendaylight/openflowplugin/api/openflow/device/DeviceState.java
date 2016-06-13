/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Holder of device's structure
 */
public interface DeviceState {

    /**
     * @return true if this session is valid
     */
    boolean isValid();

    /**
     * @param valid the valid to set
     */
    void setValid(boolean valid);

    /**
     * Return true if we have relevant meter information
     * from device
     *
     * @return
     */
    boolean isMetersAvailable();

    /**
     * Set information about meter statistics availability.
     */
    void setMeterAvailable(boolean available);

    /**
     * Return true if we have relevant group information
     * from device
     *
     * @return
     */
    boolean isGroupAvailable();

    /**
     * Set information about group statistics availability.
     */
    void setGroupAvailable(boolean available);

    /**
     * Method returns true if initial statistics data were collected and written to DS.
     *
     * @return
     */
    boolean deviceSynchronized();

    /**
     * Method returns true, if device capabilities provides flow statistics.
     *
     * @return
     */
    boolean isFlowStatisticsAvailable();

    void setFlowStatisticsAvailable(boolean available);

    /**
     * Method returns true, if device capabilities provides table statistics.
     *
     * @return
     */
    boolean isTableStatisticsAvailable();

    void setTableStatisticsAvailable(boolean available);

    /**
     * Method returns true, if device capabilities provides flow statistics.
     *
     * @return
     */
    boolean isPortStatisticsAvailable();

    void setPortStatisticsAvailable(boolean available);

    /**
     * Method returns true, if device capabilities provides queue statistics.
     *
     * @return
     */
    boolean isQueueStatisticsAvailable();

    void setQueueStatisticsAvailable(boolean available);

    void setDeviceSynchronized(boolean deviceSynchronized);

    boolean isStatisticsPollingEnabled();

    void setStatisticsPollingEnabledProp(boolean statPollEnabled);

}
