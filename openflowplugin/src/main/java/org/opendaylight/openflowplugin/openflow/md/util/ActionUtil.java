/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * OF-action related utilities.
 */
public final class ActionUtil {

    /** http://en.wikipedia.org/wiki/IPv4#Packet_structure (end of octet number 1, bit 14.+15.). */
    public static final int ENC_FIELD_BIT_SIZE = 2;

    private ActionUtil() {
        throw new AssertionError("ActionUtil is not expected to be instantiated.");
    }

    /**
     * Converts TOS to DSCP value.
     *
     * @param tosValue TypeOfService value
     * @return DSCP value
     */
    @SuppressFBWarnings("ICAST_QUESTIONABLE_UNSIGNED_RIGHT_SHIFT")
    public static Short tosToDscp(short tosValue) {
        return (short) (tosValue >>> ActionUtil.ENC_FIELD_BIT_SIZE);
    }

    /**
     * Converts DSCP to TOS value.
     *
     * @param dscpValue TypeOfService value
     * @return TOS value
     */
    public static Short dscpToTos(short dscpValue) {
        return (short) (dscpValue << ActionUtil.ENC_FIELD_BIT_SIZE);
    }
}
