/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

public interface MastershipChangeListener {

    void becomeMaster(@Nonnull final DeviceInfo deviceInfo);
    void becomeSlaveOrDisconnect(@Nonnull final DeviceInfo deviceInfo);
    void setMasterChecker(@Nonnull final MasterChecker masterChecker);

}
