/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceLifecycleSupervisor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.translator.TranslatorLibrarian;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

import javax.annotation.CheckForNull;

/**
 * This interface is responsible for instantiating DeviceContext and
 * registering transaction chain for each DeviceContext. Each device
 * has its own device context managed by this manager.
 */
public interface DeviceManager extends DeviceConnectedHandler, DeviceDisconnectedHandler, DeviceLifecycleSupervisor,
        DeviceInitializationPhaseHandler, DeviceTerminationPhaseHandler, TranslatorLibrarian, AutoCloseable, OFPManager {


    /**
     * invoked after all services injected
     */
    void initialize();

    /**
     * Method has to activate (MASTER) or deactivate (SLAVE) TransactionChainManager.
     * TransactionChainManager represents possibility to write or delete Node subtree data
     * for actual Controller Cluster Node. We are able to have an active TxManager only if
     * newRole is {@link OfpRole#BECOMESLAVE}.
     * Parameters are used as marker to be sure it is change to SLAVE from MASTER or from
     * MASTER to SLAVE and the last parameter "cleanDataStore" is used for validation only.
     *
     * @param deviceInfo which device
     * @param role - NewRole expect to be {@link OfpRole#BECOMESLAVE} or {@link OfpRole#BECOMEMASTER}
     * @return RoleChangeTxChainManager future for activation/deactivation
     */
    ListenableFuture<Void> onClusterRoleChange(final DeviceInfo deviceInfo, final OfpRole role);

    /**
     * Register device synchronize listeners
     * @param deviceSynchronizeListener are notified if device is synchronized or not
     */
    void registerDeviceSynchronizeListeners(final DeviceSynchronizeListener deviceSynchronizeListener);

    /**
     * Notify all registered listeners about synchronized status
     * @param deviceInfo which device
     * @param deviceSynchronized true if device is synchronized
     */
    void notifyDeviceSynchronizeListeners(final DeviceInfo deviceInfo, final boolean deviceSynchronized);

    /**
     * Register device valid listeners
     * @param deviceValidListener are notified if device is valid or not
     */
    void registerDeviceValidListeners(final DeviceValidListener deviceValidListener);

    /**
     * Notify all registered listeners about valid status
     * @param deviceInfo which device
     * @param deviceValid true if device is valid
     */
    void notifyDeviceValidListeners(final DeviceInfo deviceInfo, final boolean deviceValid);

}

