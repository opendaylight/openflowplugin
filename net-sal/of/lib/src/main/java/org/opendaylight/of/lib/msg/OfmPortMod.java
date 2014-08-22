/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;

import java.util.Collections;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.EOLI;

/**
 * Represents an OpenFlow PORT_MOD message; Since 1.0.
 *
 * @author Simon Hunt
 */
public class OfmPortMod extends OpenflowMessage {

    BigPortNumber port;
    MacAddress hwAddress;
    Set<PortConfig> config;
    Set<PortConfig> mask;
    Set<PortFeature> advertise;

    /**
     * Constructs an OpenFlow PORT_MOD message.
     *
     * @param header the message header
     */
    OfmPortMod(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len - 1, len, ",port=").append(port)
                .append(",cfg=").append(config)
                .append(",...}");
        return sb.toString();
    }

    // TODO - combine the config and mask values to show settings...
    //  e.g. NO_RECV=1, NO_FWD=1, NO_PACKET_IN=0

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        sb.append(EOLI).append("Port   : ").append(port)
          .append(EOLI).append("HW Addr: ").append(hwAddress)
          .append(EOLI).append("Config : ").append(config)
          .append(EOLI).append("Mask   : ").append(mask)
          .append(EOLI).append("Advert.: ").append(advertise);
        return sb.toString();
    }


    /** Returns the id of the port to be modified; Since 1.0.
     *
     * @return the port number
     */
    public BigPortNumber getPort() {
        return port;
    }

    /** Returns the hardware address; Since 1.0.
     * <p>
     * The hardware address is not configurable. This is used to sanity-check
     * the request, so it must be the same as returned in a {@link Port}
     * structure.
     *
     * @return the hardware address of the port
     */
    public MacAddress getHwAddress() {
        return hwAddress;
    }

    /** The configuration flags; Since 1.0.
     *
     * @return the configuration flags
     */
    public Set<PortConfig> getConfig() {
        return config == null ? null : Collections.unmodifiableSet(config);
    }

    /** The flags in the mask select which bits in the config field to change;
     * Since 1.0.
     *
     * @return the selected config bits to change
     */
    public Set<PortConfig> getConfigMask() {
        return mask == null ? null : Collections.unmodifiableSet(mask);
    }

    /** The features of this port to advertise; Since 1.0.
     *
     * @return the features to advertise
     */
    public Set<PortFeature> getAdvertise() {
        return advertise == null ? null : Collections.unmodifiableSet(advertise);
    }
}
