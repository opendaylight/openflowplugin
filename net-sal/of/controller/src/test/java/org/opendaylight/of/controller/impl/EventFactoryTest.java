/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.Test;
import org.opendaylight.of.controller.ErrorEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for EventFactory.
 *
 * @author Simon Hunt
 */
public class EventFactoryTest {

    @Test
    public void errorEvent() {
        print(EOL + "errorEvent()");
        ErrorEvent err = EventFactory.createErrorEvent(null, null, null);
        print(err);
        assertEquals(AM_NEQ, "", err.text());
        assertNull(AM_HUH, err.cause());
        assertNull(AM_HUH, err.context());
    }
}
