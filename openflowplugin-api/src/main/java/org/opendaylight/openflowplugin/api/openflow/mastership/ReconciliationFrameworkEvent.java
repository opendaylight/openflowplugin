/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.mastership;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.OwnershipChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;

/**
 * Service provide one event designed for reconciliation framework.
 * <ul>
 *     <li><i>{@link #onDeviceDisconnected(DeviceInfo)}</i>
 *     is called when device is being mastered by controller but not yet submitted into data store.
 *     This method is being called only if the {@link OwnershipChangeListener#isReconciliationFrameworkRegistered()}
 *     is set to {@link Boolean#TRUE}</li>
 *     <li><i>{@link #onDevicePrepared(DeviceInfo)}</i>
 *     is called when device is disconnected or controller loses control of the switch</li>
 * </ul>
 * <b>This event <i>onDevicePrepared</i> should be used only for reconciliation framework
 * and application can't do anything with node before the device is not stored in to data store.</b>
 * @since 0.5.0 Nitrogen
 */

public interface ReconciliationFrameworkEvent extends AutoCloseable {

    /**
     * Event when device is ready as a master but not yet submitted in data store. This event is evoked by
     * {@link OwnershipChangeListener#becomeMasterBeforeSubmittedDS(DeviceInfo)}
     * @param deviceInfo connected switch identification
     * @return result state if the device can continue with connecting or should be disconnected
     */
    ListenableFuture<ResultState> onDevicePrepared(@NonNull DeviceInfo deviceInfo);

    /**
     * This event occurs after device is disconnected or being slaved.
     * Event is similar to the {@link MastershipChangeService#onLoseOwnership(DeviceInfo)}. This event is used by
     * reconciliation framework that the framework don't need to register {@link MastershipChangeService}
     * @param deviceInfo connected switch identification
     * @return future
     * @see MastershipChangeService
     */
    ListenableFuture<Void> onDeviceDisconnected(@NonNull DeviceInfo deviceInfo);


}
