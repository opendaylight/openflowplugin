/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing sleep (wait) event
 *
 * @author michal.polkorab
 */
public class SleepEvent implements ClientEvent {

    private static final Logger LOG = LoggerFactory.getLogger(SleepEvent.class);
    private long sleepTime;

    /**
     *
     * @param sleepTime time of {@link Thread#sleep(long)} in milliseconds
     */
    public SleepEvent(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public boolean eventExecuted() {
        try {
            Thread.sleep(sleepTime);
            LOG.debug("Sleeping");
            return true;
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }
}
