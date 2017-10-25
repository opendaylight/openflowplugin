/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.applications.frsync.impl.strategy.SynchronizationDiffInput;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncCrudCounters;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Prescribes common synchronization plan execution strategy.
 * Implementations should be stateless.
 */
public interface SyncPlanPushStrategy {

    /**
     * @param resultVehicle bootstrap future - execution will chain it's async calls to this one
     * @param diffInput     wraps all diff data required for any strategy ({add,remove,update} x {flow,group,meter})
     * @param counters      reference to internal one-shot statistics - summary off successfully pushed items
     *                      shall be recorded here
     * @return last future of the chain
     */
    ListenableFuture<RpcResult<Void>> executeSyncStrategy(ListenableFuture<RpcResult<Void>> resultVehicle,
                                                          SynchronizationDiffInput diffInput,
                                                          SyncCrudCounters counters);
}
