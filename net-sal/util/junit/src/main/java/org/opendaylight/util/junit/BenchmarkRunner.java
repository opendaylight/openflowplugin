/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;


import static org.opendaylight.util.junit.TestTools.AM_UNEX;
import static org.opendaylight.util.junit.TestTools.EOL;

import java.text.DecimalFormat;

import org.junit.Assert;

/**
 * Runs benchmarks on {@link BenchmarkSubject}s.
 *
 * @author Simon Hunt
 */
public class BenchmarkRunner {

    private static final int DEFAULT_NUM_LOOPS = 100000;

    /** Runs the benchmark on the subject.
     * <p>
     * This is done by first timing how long it takes to run just
     * the {@link BenchmarkSubject#prepare()} method on its own N times,
     * where N is given by {@link #numLoops()}. Secondly, the time taken
     * to run both the {@link BenchmarkSubject#prepare()} and
     * {@link BenchmarkSubject#run()} methods one after the other N times is
     * measured. This information is stored and returned in a
     * {@link Stats} object, which offers interesting statistics about
     * the benchmark run.
     *
     * @param subject the subject to be bench-marked
     * @return a stats object containing the results of the benchmark run
     */
    public final Stats benchmark(BenchmarkSubject subject) {
        // first, time the prepare() method alone to get "tare" duration
        final int n = numLoops();
        long start = System.currentTimeMillis();
        for (int i=0; i<n; i++)
            subject.prepare();
        long end = System.currentTimeMillis();
        long tareDuration = end - start;

        // now, run the same sequence, doing the work as well
        start = System.currentTimeMillis();
        for (int i=0; i<n; i++) {
            subject.prepare();
            try {
                subject.run();
            } catch (Exception e) {
                Assert.fail(AM_UNEX);
            }
        }
        end = System.currentTimeMillis();
        long ladenDuration = end - start;

        // time to post the results
        return new Stats(subject.getClass(), n, tareDuration, ladenDuration);
    }

    /** Returns the number of loops to use in the benchmark test.
     * <p>
     * This default implementation returns 1 million.
     * Concrete subclasses may override this method to define a
     * different number of loops.
     * <p>
     *
     * @return the number of loops
     */
    protected int numLoops() {
        return DEFAULT_NUM_LOOPS;
    }

    /** Formats the value as a string to 6 decimal places.
     *
     * @param value the value to format
     * @return the formatted value
     */
    public static String numMicro(double value) {
        return new DecimalFormat("##,##0.000000").format(value);
    }

    /** Formats the value as a string to 1 decimal place.
     *
     * @param value the value to format
     * @return the formatted value
     */
    public static String num(double value) {
        return new DecimalFormat("##,##0.00").format(value);
    }

    /** Formats the value as a string to 1 decimal place, and
     * includes an " ms" suffix.
     *
     * @param value the value to format
     * @return the formatted value
     */
    public static String numMs(double value) {
        return num(value) + " ms";
    }



    /** Stats returned from the benchmark. */
    public static class Stats {
        private static final double MS_PER_S = 1000.0;

        private final Class<? extends BenchmarkSubject> clazz;
        private final int numLoops;
        private final long tareDuration;
        private final long ladenDuration;

        /** Constructor.
         *
         * @param clazz class of benchmark subject
         * @param n number of loops in the test
         * @param tare total time for just the prepare() method (ms)
         * @param laden total time for prepare() and run() methods (ms)
         */
        private Stats(Class<? extends BenchmarkSubject> clazz,
                      int n, long tare, long laden) {
            this.clazz = clazz;
            numLoops = n;
            tareDuration = tare;
            ladenDuration = laden;
        }

        /** Returns the number of loops used in the test.
         *
         * @return the number of loops
         */
        public int getNumLoops() {
            return numLoops;
        }

        /** Returns the time taken (ms) to run the
         * {@link BenchmarkSubject#prepare() prepare()} method N times,
         * where N is the {@link #getNumLoops() number of loops}.
         *
         * @return the total time (ms) for the prepare() method N times
         */
        public long getTareDuration() {
            return tareDuration;
        }

        /** Returns the time taken (ms) to run the
         * {@link BenchmarkSubject#prepare() prepare()} method, followed by the
         * {@link BenchmarkSubject#run() run()} method, N times,
         * where N is the {@link #getNumLoops() number of loops}.
         *
         * @return the total time (ms) for the prepare() and run() methods
         *          N times
         */
        public long getLadenDuration() {
            return ladenDuration;
        }

        /** Returns the calculated time taken (ms) to run the
         * {@link BenchmarkSubject#run() run()} method, N times,
         * where N is the {@link #getNumLoops() number of loops}.
         * This is the difference between the Laden and Tare durations.

         * @return the total time (ms) for the run() method N times
         */
        public double totalRunDurationMs() {
            return ladenDuration - tareDuration;
        }

        /** Returns the calculated average duration of a single invocation
         * of the {@link BenchmarkSubject#run() run()} method.
         *
         * @return the average duration (ms) for a single run() invocation
         */
        public double avRunDurationMs() {
            return totalRunDurationMs() / numLoops;
        }

        /** Returns the calculated average number of invocations
         * of the {@link BenchmarkSubject#run() run()} method per second.
         *
         * @return the average number of run() invocations per second
         */
        public double avRunsPerSecond() {
            double secs = totalRunDurationMs() / MS_PER_S;
            return numLoops / secs;
        }

        @Override
        public String toString() {
            return "Benchmark: " + clazz.getSimpleName() +
                    " Rate: " + num(avRunsPerSecond()) + " runs/sec";
        }

        /** Returns a multi-line string representation of this stats object.
         * For example:
         * <pre>
         * Benchmark:  Rate: 151423.4 runs/sec
         * Loop Count: 1000000
         * Tare Duration: 7894.0 ms
         * Laden Duration: 14498.0 ms
         * Total Run Duration: 6604.0 ms
         * Av. Run Duration: 0.006604 ms
         * Rate: 151423.4 runs/second
         * </pre>
         *
         * @return a multi-line string representation
         */
        public String toDebugString() {
            return toString() +
                    EOL + "Loop Count: " + numLoops +
                    EOL + "Tare Duration: " + numMs(tareDuration) +
                    EOL + "Laden Duration: " + numMs(ladenDuration) +
                    EOL + "Total Run Duration: " + numMs(totalRunDurationMs()) +
                    EOL + "Av. Run Duration: " + numMicro(avRunDurationMs()) + " ms" +
                    EOL + "Rate: " + num(avRunsPerSecond()) + " runs/second";
        }
    }
}
