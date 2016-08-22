/**
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device.handlers;

import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;

/**
 * Interface handles MASTER initialization on ownership change
 */
public interface ClusterInitializationPhaseHandler {

    /**
     * Method for initialization cycle between contexts
     * @param connectionContext to check actual connection state
     */
    boolean onContextInstantiateService(final ConnectionContext connectionContext);

    /**
     * Method for initial submit transaction after successful initial gathering
     */
    default void initialSubmitTransaction(){
        //This method need to be override only in device context to submit initial data
    }
}
