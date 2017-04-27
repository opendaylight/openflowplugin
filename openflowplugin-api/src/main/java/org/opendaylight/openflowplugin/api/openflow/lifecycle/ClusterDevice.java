/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * Helper interface to store all devices created in operational DS. This is for all instances in cluster to be aware
 * about all devices in cluster even if there is no connection for particular cluster node.
 * If there is being granted mastership by controllers election without connection it should be deleted from DS.
 */
public interface ClusterDevice {

    DeviceInfo changeDeviceInfo(@Nonnull final DeviceInfo deviceInfo);
    @Nullable
    DeviceInfo getDeviceInfo();
    void connect();
    void disconnect();
    boolean isConnected();

}
