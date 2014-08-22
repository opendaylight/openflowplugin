/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.flow;

/**
 * Denotes flow event types.
 *
 * @author Simon Hunt
 */
public enum FlowEventType {
    /** A flow was successfully pushed to a datapath. */
    FLOW_MOD_PUSHED,
    /** A flow push failed. */
    FLOW_MOD_PUSH_FAILED,
    /** A flow was removed from a datapath. */
    FLOW_REMOVED,
    // required semi-colon
    ;
}
