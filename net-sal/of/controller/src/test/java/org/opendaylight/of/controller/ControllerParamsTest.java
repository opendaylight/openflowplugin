/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.opendaylight.of.controller.ControllerParams.DEFAULT_MESSAGE_FUTURE_TIMEOUT_MS;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for ControllerParams.
 *
 * @author Simon Hunt
 */
public class ControllerParamsTest {

    private ControllerParams cp;

    @Test
    public void basic() {
        print(EOL + "basic()");
        cp = new ControllerParams.Builder().build();
        print(cp);
        assertEquals(AM_NEQ, DEFAULT_MESSAGE_FUTURE_TIMEOUT_MS,
                cp.messageFutureTimeout());
    }

    @Test
    public void setMfTimeout() {
        print(EOL + "setMfTimeout()");
        cp = new ControllerParams.Builder().messageFutureTimeout(200).build();
        print(cp);
        assertEquals(AM_NEQ, 200, cp.messageFutureTimeout());
    }
}
