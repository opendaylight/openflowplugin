/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.flow;

/**
 * Denotes meter event types.
 *
 * @author Simon Hunt
 */
public enum MeterEventType {
    /** A meter was successfully pushed to a datapath. */
    METER_MOD_PUSHED,
    /** A meter push failed. */
    METER_MOD_PUSH_FAILED,
    // required semi-colon
    ;
}
