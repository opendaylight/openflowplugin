/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller;

/**
 * An OpenFlow Controller Checkpoint event.
 * <p>
 * These events subclass {@link MessageEvent}, and may be used by the
 * {@link ControllerMx management API} to inject checkpoints into
 * the recorded TX/RX message stream, for diagnostic purposes.
 * <p>
 * The event types associated with this event are:
 * <ul>
 *     <li> {@link OpenflowEventType#MX_CHECKPOINT} </li>
 * </ul>
 * <p>
 * Each checkpoint also has a {@link Code code} used to designate
 * different categories of checkpoint.
 *
 * @author Simon Hunt
 */
public interface CheckpointEvent extends MessageEvent {

    /** Returns the checkpoint code.
     *
     * @return the checkpoint code
     */
    Code code();

    /** Returns the textual description associated with this event.
     *
     * @return the textual description
     */
    String text();


    /** Designates the checkpoint code. */
    public static enum Code {
        /** The time at which the TX/RX message recording mechanism
         * was started.
         */
        RECORDING_STARTED,

        /** The time at which the TX/RX message recording mechanism
         * was stopped.
         */
        RECORDING_STOPPED,

        /** A generic checkpoint; detail will be in the text message. */
        GENERIC,
        ;
    }
}
