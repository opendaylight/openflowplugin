/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.dt.MeterId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.CommonUtils.cSize;

/**
 * Represents an OpenFlow METER_MOD message; Since 1.3.
 *
 * @author Simon Hunt
 */
public class OfmMeterMod extends OpenflowMessage {

    MeterModCommand command;
    Set<MeterFlag> flags;
    MeterId meterId;
    List<MeterBand> bands = new ArrayList<MeterBand>();

    /**
     * Constructs an OpenFlow METER_MOD message.
     *
     * @param header the message header
     */
    OfmMeterMod(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len - 1, len, ",cmd=").append(command)
                .append(",flg=").append(flags)
                .append(",id=").append(meterId)
                .append(",#b=").append(cSize(bands))
                .append(",...}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        sb.append(EOLI).append("Command: ").append(command)
                .append(EOLI).append("Flags  : ").append(flags)
                .append(EOLI).append("MeterID: ").append(meterId)
                .append(EOLI).append("Bands:");

        for (MeterBand mb: bands)
            sb.append(EOLI).append("  ").append(mb);

        return sb.toString();
    }

    /** Returns the meter mod command; Since 1.3.
     *
     * @return the command
     */
    public MeterModCommand getCommand() {
        return command;
    }

    /** Returns the meter flags; Since 1.3.
     *
     * @return the flags
     */
    public Set<MeterFlag> getFlags() {
        return flags == null ? null : Collections.unmodifiableSet(flags);
    }

    /** Returns the meter id; Since 1.3.
     *
     * @return the meter id
     */
    public MeterId getMeterId() {
        return meterId;
    }

    /** Returns the bands applied to this meter; Since 1.3.
     *
     * @return the bands
     */
    public List<MeterBand> getBands() {
        return Collections.unmodifiableList(bands);
    }
}
