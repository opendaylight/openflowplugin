/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.mastership;

import com.google.common.util.concurrent.FutureCallback;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationProperty;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.OwnershipChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;

/**
 * Service provide one event designed for reconciliation framework.
 * <ul>
 *     <li><i>{@link #onDevicePrepared(DeviceInfo, FutureCallback)}</i>
 *     is called when device is being mastered by controller but not yet submitted into data store.
 *     This method is being called only if the {@link ConfigurationProperty#USING_RECONCILIATION_FRAMEWORK}
 *     is set to {@link Boolean#TRUE}</li>
 * </ul>
 * Other event are defined in {@link MastershipChangeService}
 * <p>
 * <b>This event <i>onDevicePrepared</i> should be used only for reconciliation framework and application can't do anything with
 * node before the device is not stored in to data store.</b>
 * @since 0.5.0 Nitrogen
 */

public interface ReconciliationFrameworkEvent extends AutoCloseable {

    /**
     * Event when device is ready as a master but not yet submitted in data store. This event is evoked by
     * {@link OwnershipChangeListener#becomeMasterBeforeSubmittedDS(DeviceInfo, FutureCallback)}
     * @param deviceInfo connected switch identification
     * @param callback callback need to be attached to reconciliation result future
     */
    default void onDevicePrepared(@Nonnull DeviceInfo deviceInfo, @Nonnull FutureCallback<ResultState> callback) {
        callback.onSuccess(ResultState.DONOTHING);
    }
}
