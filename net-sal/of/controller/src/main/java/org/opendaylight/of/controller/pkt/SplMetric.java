/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.pkt;

/**
 * Encapsulates metric data for a {@link SequencedPacketListener sequenced
 * packet listener}.
 *
 * @author Simon Hunt
 * @author Frank Wood
 */
public interface SplMetric {

    /**
     * Returns the implementing class of the sequenced packet listener,
     * for which these samples were taken.
     *
     * @return the class of listener
     */
    Class<? extends SequencedPacketListener> splClass();

    /**
     * Returns the packet listener's registered role.
     *
     * @return the role
     */
    SequencedPacketListenerRole role();

    /**
     * Returns the packet listener's registered altitude.
     *
     * @return the altitude
     */
    int altitude();

    /**
     * Returns the sum of all the sampled durations (nanoseconds).
     *
     * @return the total duration
     */
    long totalDuration();

    /**
     * Returns the number of samples.
     *
     * @return the sample count
     */
    long sampleCount();

    /**
     * Returns the average duration of the sampled event() callback,
     * measured in nanoseconds. If no samples were taken, -1.0 is returned.
     *
     * @return average duration
     */
    double averageDurationNanos();

    /**
     * Returns the average duration of the sampled event() callback,
     * measured in microseconds. If no samples were taken, -1.0 is returned.
     *
     * @return average duration
     */
    double averageDurationMs();

    /**
     * Returns the computed average duration of the sampled event()
     * callback, measured in milliseconds and formatted to 6 decimal
     * places.
     *
     * @return the formatted duration in ms
     */
    String averageMs();
}
