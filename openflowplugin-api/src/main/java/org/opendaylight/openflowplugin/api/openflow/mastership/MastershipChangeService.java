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
 * This service is for registering any application intent to be informed about device mastership changes.
 * Service provides three events.
 * <ul>
 *     <li><i>{@link #onBecomeOwner(DeviceInfo)}</i> is called when device is fully mastered by controller</li>
 *     <li><i>{@link #onDevicePrepared(DeviceInfo, FutureCallback)}</i>
 *     is called when device is being mastered by controller but not yet submitted into data store.
 *     This method is being called only if the {@link ConfigurationProperty#USING_RECONCILIATION_FRAMEWORK}
 *     is set to {@link Boolean#TRUE}</li>
 *     <li><i>{@link #onLoseOwnership(DeviceInfo)}</i>
 *     is called when device is disconnected or is being to be slave</li>
 * </ul>
 * There is no need to have two different method for slave or disconnect because application just have to stop working
 * with the device in both cases.
 * <p>
 * <b>The event <i>onDevicePrepared</i> should be used only for reconciliation framework and application can't do anything with
 * node before the device is not stored in to data store.</b>
 * @since 0.5.0 Nitrogen
 */
public interface MastershipChangeService extends AutoCloseable {

    /**
     * Event when device is ready as a master. This event is evoked by
     * {@link OwnershipChangeListener#becomeMaster(DeviceInfo)}
     * @param deviceInfo connected switch identification
     */
    void onBecomeOwner(@Nonnull DeviceInfo deviceInfo);

    /**
     * Event when device is ready as a master but not yet submitted in data store. This event is evoked by
     * {@link OwnershipChangeListener#becomeMasterBeforeSubmittedDS(DeviceInfo, FutureCallback)}
     * @param deviceInfo connected switch identification
     * @param callback callback need to be attached to reconciliation result future
     */
    void onDevicePrepared(@Nonnull DeviceInfo deviceInfo, @Nonnull FutureCallback<ResultState> callback);

    /**
     * Event when device disconnected or become slave. This event is evoked by
     * {@link OwnershipChangeListener#becomeSlaveOrDisconnect(DeviceInfo)}
     * @param deviceInfo connected switch identification
     */
    void onLoseOwnership(@Nonnull DeviceInfo deviceInfo);

}