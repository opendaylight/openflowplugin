/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import java.util.Set;

/**
 * Base class for OfmSetConfig and OfmGetConfigReply.
 *
 * @author Simon Hunt
 */
abstract class SwitchConfig extends OpenflowMessage {
    Set<ConfigFlag> flags;
    int missSendLength;

    /**
     * Base constructor that stores the header.
     *
     * @param header the message header
     */
    SwitchConfig(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",flags=").append(flags)
                .append(",msLen=").append(missSendLength)
                .append("}");
        return sb.toString();
    }


    /** Returns the set of configuration flags.
     *
     * @return the config flags
     */
    public Set<ConfigFlag> getFlags() {
        return flags;
    }

    /** Returns the "miss send" length; Since 1.0.
     * This is the maximum number of bytes of the packet
     * that the switch should send to the controller.
     *
     * @return the "miss send" length
     */
    public int getMissSendLength() {
        return missSendLength;
    }
}
