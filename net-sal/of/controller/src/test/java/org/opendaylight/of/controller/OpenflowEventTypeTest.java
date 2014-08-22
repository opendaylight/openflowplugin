/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link OpenflowEventType}.
 *
 * @author Simon Hunt
 */
public class OpenflowEventTypeTest {
    @Test
    public void basic() {
        print(EOL + "basic()");
        for (OpenflowEventType ev : OpenflowEventType.values())
            print("{} => {}", ev, ev.abbrev());
        assertEquals(AM_NEQ, 13, OpenflowEventType.values().length);
    }

}
