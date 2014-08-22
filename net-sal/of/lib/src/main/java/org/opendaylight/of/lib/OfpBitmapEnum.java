/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib;

/**
 * Implemented by enums that represent OpenFlow Protocol bitmap fields.
 *
 * @author Simon Hunt
 */
public interface OfpBitmapEnum extends OfpEnum {
    /** Returns the bit value for the constant, under the given protocol
     * version.
     *
     * @param pv the protocol version
     * @return the bit value for the constant
     */
    public int getBit(ProtocolVersion pv);

    /** Returns a string representation of the constant, suitable for
     * presentation in a display.
     *
     * @return a display string for the constant
     */
    public String toDisplayString();
}
