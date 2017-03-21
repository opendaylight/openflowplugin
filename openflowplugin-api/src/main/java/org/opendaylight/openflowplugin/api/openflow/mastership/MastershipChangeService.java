/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.mastership;

import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

public interface MastershipChangeService extends AutoCloseable {

    /**
     * event when device is ready as a master.
     * @param deviceInfo - device
     */
    Future<Void> onBecomeOwner(@Nonnull final DeviceInfo deviceInfo);

    /**
     * event when device disconnected or become slave.
     * @param deviceInfo - device
     */
    Future<Void> onLoseOwnership(@Nonnull final DeviceInfo deviceInfo);

}
