/**
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device.handlers;

/**
 * Interface has to implement all relevant manager to correctly handling
 * device context initialization when device MASTER. Methods are used for order
 * handlers in initialization phase. Ordering is easily changed
 * pragmatically by definition.
 */
public interface ClusterLifecycleSupervisor {

    /**
     * Method sets relevant {@link ClusterInitializationPhaseHandler} for building
     * handler's chain for Device mastership phase.
     * @param handler handler
     */
    void setLifecycleInitializationPhaseHandler(final ClusterInitializationPhaseHandler handler);

    default void setInitialSubmitHandler(final ClusterInitializationPhaseHandler initialSubmitHandler) {
        //Need to be only set in statistics context where after successful initial gather
        //tx need to be submitted
    }

}
