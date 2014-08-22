/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

import org.opendaylight.util.TimeUtils;

/**
 * Represents an alert raised by the controller.
 *
 * @author Simon Hunt
 */
public interface Alert {

    /** Returns the alert timestamp.
     * The timestamp is set to {@link TimeUtils#currentTimeMillis()} at the
     * creation of the alert.
     *
     * @return the timestamp
     */
    long ts();

    /** Returns the severity of the alert.
     *
     * @return the alert severity
     */
    Severity severity();

    /** Returns a description of the alert condition.
     *
     * @return a description of the alert condition
     */
    String description();


    /** Designates the severity of the alert. */
    public static enum Severity {
        CRITICAL, WARNING, INFO
    }
}
