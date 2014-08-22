/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.opendaylight.util.Duration;

/**
 * Tests for Duration
 * 
 * @author Ryan Tidwell
 */
public class DurationTest {

    @Test
    public void testValue() {
        Duration d = new Duration(1l, TimeUnit.DAYS);
        assertEquals(d.value(TimeUnit.HOURS), 24l);
        assertEquals(d.value(TimeUnit.MINUTES), 1440l);
        assertEquals(d.value(TimeUnit.SECONDS), 86400l);
        assertEquals(d.value(TimeUnit.MILLISECONDS), 86400000l);
        assertEquals(d.value(TimeUnit.MICROSECONDS), 86400000000l);
    }

    @Test
    public void testEquals() {
        Duration d = new Duration(1l, TimeUnit.DAYS);
        assertTrue(d.equals(new Duration(1l, TimeUnit.DAYS)));
        assertTrue(d.equals(new Duration(24l, TimeUnit.HOURS)));
        assertTrue(d.equals(new Duration(1440l, TimeUnit.MINUTES)));
        assertTrue(d.equals(new Duration(86400l, TimeUnit.SECONDS)));
        assertTrue(d.equals(new Duration(86400000l, TimeUnit.MILLISECONDS)));
        assertTrue(d.equals(new Duration(86400000000l, TimeUnit.MICROSECONDS)));
        assertFalse(d.equals(new Object()));
        assertFalse(d.equals(new Duration(86401l, TimeUnit.SECONDS)));
    }

    public void testHashCode() {
        Duration d1 = new Duration(1l, TimeUnit.DAYS);
        Duration d2 = new Duration(1l, TimeUnit.DAYS);
        Duration d3 = new Duration(1l, TimeUnit.HOURS);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertFalse(d1.hashCode() == d3.hashCode());
    }
}
