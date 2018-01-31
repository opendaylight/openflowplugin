/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.clients;

import com.google.common.util.concurrent.SettableFuture;

/**
 * Unifying interface for simple clients / switch simulators.
 *
 * @author michal.polkorab
 */
public interface OFClient extends Runnable {

    /**
     * Returns the isOnlineFuture which is set when client is started.
     */
    SettableFuture<Boolean> getIsOnlineFuture();

    /**
     * Returns the scenarioDone when scenario is successfully finished.
     */
    SettableFuture<Boolean> getScenarioDone();

    /**
     * Sets the ScenarioHandler.
     *
     * @param scenario list of desired actions
     */
    void setScenarioHandler(ScenarioHandler scenario);

    /**
     * Sets wether client should use encrypted communication.
     *
     * @param securedClient true is client should use encrypted communication, false otherwise
     */
    void setSecuredClient(boolean securedClient);
}
