/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import java.util.concurrent.TimeUnit;

/**
 * Moving average - measure and compute
 */
public class TimeCounter {
    private long beginningOfLap;
    private long delta;
    private int marksCount = 0;

    public void markStart() {
        beginningOfLap = System.nanoTime();
        delta = 0;
        marksCount = 0;
    }

    public void addTimeMark() {
        final long now = System.nanoTime();
        delta += now - beginningOfLap;
        marksCount++;
        beginningOfLap = now;
    }

    public long getAverageTimeBetweenMarks() {
        long average = 0;
        if (marksCount > 0) {
            average = delta / marksCount;
        }
        return TimeUnit.NANOSECONDS.toMillis(average);
    }
}
