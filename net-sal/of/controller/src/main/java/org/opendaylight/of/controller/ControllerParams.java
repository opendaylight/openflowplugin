/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller;

import org.opendaylight.of.lib.msg.MessageFuture;

/**
 * Encapsulates controller runtime parameters which may be of interest to
 * "external" parties.
 * <p>
 * The values (with defaults shown in square brackets) are:
 * <ul>
 *     <li>
 *         <strong>messageFutureTimeout</strong>: the maximum number of
 *         milliseconds to wait for a {@link MessageFuture} to be satisfied.
 *     </li>
 * </ul>
 * <p>
 * This class uses the builder pattern. For example, to create a parameters
 * instance which sets the message future timeout to half a second:
 * <pre>
 * ControllerParams params = new ControllerParams.Builder()
 *         .messageFutureTimeout(500).build();
 * </pre>
 *
 * @author Simon Hunt
 */
public class ControllerParams {

    /** The default message future timeout (in ms). */
    public static final long DEFAULT_MESSAGE_FUTURE_TIMEOUT_MS = 1000;

    private final long mfTimeout;

    /** Constructs a controller parameters instance using the specified
     * values. This private constructor is called from Builder.build().
     *
     * @param mfTimeout the message future timeout value
     */
    private ControllerParams(long mfTimeout) {
        this.mfTimeout = mfTimeout;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{ControllerParams: mfto=");
        sb.append(mfTimeout).append("ms");
        sb.append("}");
        return sb.toString();
    }

    /** Returns the maximum number of milliseconds to wait for a
     * {@link MessageFuture} to be satisfied.
     *
     * @return message future timeout value
     */
    public long messageFutureTimeout() {
        return mfTimeout;
    }

    //======================================================================

    /** Builds instances of ControllerParams. */
    public static class Builder {
        // explicitly declaring all default values (even nulls and false)...
        private long mfTimeout = DEFAULT_MESSAGE_FUTURE_TIMEOUT_MS;

        /* IMPLEMENTATION NOTE:
        * All boolean values should default to false, and the associated
        * setter should be a no-args method that sets the flag to true.
        */

        /** Returns a controller parameters instances for the current settings
         * on this builder.
         *
         * @return a controller parameters instance
         */
        public ControllerParams build() {
            return new ControllerParams(mfTimeout);
        }

        /** Sets the message future timeout value (in ms).
         *
         * @param mfTimeout the message future timeout value
         * @return self, for chaining
         */
        public Builder messageFutureTimeout(long mfTimeout) {
            this.mfTimeout = mfTimeout;
            return this;
        }
    }
}
