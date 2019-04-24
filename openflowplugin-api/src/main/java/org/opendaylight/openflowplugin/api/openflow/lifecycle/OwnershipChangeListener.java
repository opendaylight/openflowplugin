/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;

/**
 * Internal OFP interface used by mastership service.
 * @see ContextChainHolder
 * @since 0.5.0 Nitrogen
 */
public interface OwnershipChangeListener extends ReconciliationFrameworkRegistrar {

    /**
     * This event is called when device is fully mastered. All condition have to been done successful.
     * <ul>
     *     <li>Transaction chain created</li>
     *     <li> RPC services started </li>
     *     <li> Initial statistics gathering. </li>
     *     <li> Initial DS submit. </li>
     *     <li> Flow registry filled. - <b>this step is not mandatory</b></li>
     * </ul>
     * @param deviceInfo connected switch identification
     */
    void becomeMaster(@NonNull DeviceInfo deviceInfo);

    /**
     * Should be called when device is disconnected or going to be slaved.
     * @param deviceInfo connected switch identification
     */
    void becomeSlaveOrDisconnect(@NonNull DeviceInfo deviceInfo);

    /**
     * Should be called when device is being mastered as in {@link #becomeMaster(DeviceInfo)}.
     * <p> But before: Initial DS submit</p>
     * <b>This is special call designed only for reconciliation framework.</b>
     * @see #becomeMaster(DeviceInfo)
     * @see #isReconciliationFrameworkRegistered()
     * @param deviceInfo connected switch identification
     * @return future to be able handle device after reconciliation
     */
    ListenableFuture<ResultState> becomeMasterBeforeSubmittedDS(@NonNull DeviceInfo deviceInfo);

    /**
     * Set the device mastership checker.
     * @param masterChecker {@link MasterChecker}
     */
    void setMasterChecker(@NonNull MasterChecker masterChecker);

}
