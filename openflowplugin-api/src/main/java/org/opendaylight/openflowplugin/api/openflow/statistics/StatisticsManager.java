/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.statistics;

import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextReadyHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceSynchronizedHandler;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 26.2.2015.
 */
public interface StatisticsManager extends DeviceContextReadyHandler {


    /**
     * Method registers handler responsible for handling operations needed to be done when
     * device state is synchronized. Synchronized state means, that all dynamic information
     * (groups, meters, etc ...) are stored in operational datastore.
     *
     * @param deviceSynchronizedHandler
     */
    public void addRequestDeviceSynchronizedHandler(DeviceSynchronizedHandler deviceSynchronizedHandler);



}
