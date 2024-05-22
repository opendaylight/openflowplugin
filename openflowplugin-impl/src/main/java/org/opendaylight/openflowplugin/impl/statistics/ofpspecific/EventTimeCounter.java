/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.ofpspecific;

final class EventTimeCounter {
    private long delta = 0;
    private long average = 0;
    private long minimum = 0;
    private long maximum = 0;
    private long summary = 0;
    private int counter = 0;

    synchronized void markStart() {
        delta = System.nanoTime();
    }

    synchronized void markEnd() {
        if (0 == delta) {
            return;
        }
        counter++;
        delta = System.nanoTime() - delta;

        if (delta < minimum || minimum == 0) {
            minimum = delta;
        }
        if (delta > maximum) {
            maximum = delta;
        }
        if (average > 0 && delta > average * 1.8) {
            summary += average;
        } else {
            summary += delta;
        }
        average = summary / counter;
    }

    synchronized void resetCounters() {
        delta = 0;
        average = 0;
        minimum = 0;
        maximum = 0;
        summary = 0;
        counter = 0;
    }

    synchronized long getAverage() {
        return average;
    }

    synchronized long getMinimum() {
        return minimum;
    }

    synchronized long getMaximum() {
        return maximum;
    }
}