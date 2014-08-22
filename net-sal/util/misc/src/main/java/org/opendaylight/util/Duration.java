/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.concurrent.TimeUnit;

/**
 * Represents a duration of time and provide a means of representing that
 * duration in different denominations
 * 
 * @author Ryan Tidwell
 */
public class Duration {

    private static final TimeUnit BASE_UNIT;

    public static final Duration ZERO;

    static {
        BASE_UNIT = TimeUnit.MILLISECONDS;
        ZERO = new Duration(0, TimeUnit.MILLISECONDS);
    }

    private final long duration;

    /**
     * @param duration The value of the Duration
     * @param tu The unit of time the given value is to be expressed in
     */
    public Duration(long duration, TimeUnit tu) {
        this.duration = BASE_UNIT.convert(duration, tu);
    }

    /**
     * Return the value of this Duration in the given units of time
     * 
     * @param tu The unit of time return the value in
     * @return The value of this Duration
     */
    public long value(TimeUnit tu) {
        return tu.convert(duration, BASE_UNIT);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Duration) {
            Duration other = (Duration) o;
            return other.value(BASE_UNIT) == value(BASE_UNIT);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) value(BASE_UNIT);
    }
}
