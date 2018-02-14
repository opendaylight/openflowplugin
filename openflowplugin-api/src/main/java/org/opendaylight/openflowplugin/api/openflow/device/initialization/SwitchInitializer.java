/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device.initialization;

import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * Service provide one event designed for additional switch initialization.
 * <ul>
 *     <li><i>{@link #onDevicePrepared(DeviceInfo)}</i>
 *      is called when device is being mastered by controller</li>
 * </ul>
 * <b>This event <i>onDevicePrepared</i> should be used only for additional initialization for given
 * switch if needed.</b>
 * @since 0.5.0 Nitrogen
 */
public interface SwitchInitializer extends AutoCloseable {

    /**
     * Event is invoked when device is mastered and initialized.
     * @param deviceInfo connected switch identification
     * @return future as this event call should not stop or delay
     */
    Future<Void> onDevicePrepared(@Nonnull DeviceInfo deviceInfo);

}
