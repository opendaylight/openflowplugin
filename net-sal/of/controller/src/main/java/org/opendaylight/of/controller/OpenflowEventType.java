/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

/**
 * Designates the different OpenFlow Controller events that can occur.
 *
 * @author Simon Hunt
 */
public enum OpenflowEventType {

    /**
     * An OpenFlow-capable device connected to the controller. Note that
     * the device might not yet have the default flows installed by the
     * controller at this time.
     *
     * @see #DATAPATH_READY
     */
    DATAPATH_CONNECTED("Conn"),

    /**
     * An OpenFlow-capable device has its default flows installed, and is
     * ready.
     *
     * @see #DATAPATH_CONNECTED
     */
    DATAPATH_READY("Ready"),

    /** An OpenFlow-capable device disconnected from the controller. */
    DATAPATH_DISCONNECTED("Disc"),

    /**
     * An OpenFlow-capable device was revoked from the controller,
     * usually because a duplicate datapath ID was detected.
     */
    DATAPATH_REVOKED("Revk"),

    /** An OpenFlow message was received from a datapath. */
    MESSAGE_RX("Rx"),

    /** An OpenFlow message was transmitted to a datapath. */
    MESSAGE_TX("Tx"),

    /** An {@link OpenflowListener} has registered with the controller. */
    LISTENER_ADDED("LAdd"),

    /** An {@link OpenflowListener} has unregistered from the controller. */
    LISTENER_REMOVED("LRem"),

    /** An unexpected error occurred. */
    ERROR("Err"),

    /** A listener's event queue has exceeded capacity, and has been flagged
     * as "on probation". This is not a good thing. OpenFlow events
     * will <em>not</em> be posted to the listener's queue again until the
     * queue has been drained below the queue-full-reset level, (which will
     * clear the "on probation" state).
     */
    QUEUE_FULL("QFull"),

    /** A listener's event queue has been drained below the queue-full-reset
     * level (a percentage of the queue capacity), and the "on probation" state
     * has been cleared.
     */
    QUEUE_FULL_RESET("QFR"),

    /** Signifies an unspecified number of dropped events, and marks the
     * position in the queue where the posting of events was resumed.
     */
    DROPPED_EVENTS_CHECKPOINT("DECk"),

    /** A synthetic event, used as a checkpoint by the management API. */
    MX_CHECKPOINT("CkPt"),
    ;

    private final String abbrev;

    OpenflowEventType(String abb) {
        abbrev = abb;
    }

    /**
     * Returns an abbreviated name for the constant, suitable for displaying
     * in the UI while conserving space.
     */
    public String abbrev() {
        return abbrev;
    }
}
