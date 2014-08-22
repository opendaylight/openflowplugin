/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.flow;

/**
 * Denotes group event types.
 *
 * @author Simon Hunt
 */
public enum GroupEventType {
    /** A group was successfully pushed to a datapath. */
    GROUP_MOD_PUSHED,
    /** A group push failed. */
    GROUP_MOD_PUSH_FAILED,
    // required semi-colon
    ;
}
