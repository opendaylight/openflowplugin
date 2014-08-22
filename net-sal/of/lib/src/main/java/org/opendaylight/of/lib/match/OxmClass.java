/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;

import static org.opendaylight.of.lib.CommonUtils.verMin12;

/**
 * Denotes the OXM match classes.
 * <p>
 * See also the <a href="https://www.opennetworking.org/wiki/display/PUBLIC/ONF+Registry">ONF Registry</a>.
 *
 * @author Simon Hunt
 */
public enum OxmClass {
    /** Nicira Networks (NXM compatibility); Since 1.2. */
    NXM_0(0x0000),
    /** Nicira Networks (NXM compatibility); Since 1.2. */
    NXM_1(0x0001),

    /** Big Switch Networks; Since 1.3. */
    BIG_SWITCH(0x0003),
    /** Hewlett-Packard; Since 1.3. */
    HP(0x0004),

    /** Basic class for OpenFlow; Since 1.2. */
    OPENFLOW_BASIC(0x8000),
    /** Experimenter class; Since 1.2. */
    EXPERIMENTER(0xffff),

    /** Special value to represent an unknown OXM class. */
    UNKNOWN(0xabc0000),
    ;

    private static final int MSB = 0x8000;

    private int code;

    OxmClass(int code) {
        this.code = code;
    }

    /** Returns the code value for this OXM Class.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /** Returns true if this class is an ONF reserved class.
     *
     * @return true if an ONF reserved class
     */
    public boolean isReservedClass() {
        return (code & MSB) != 0;
    }

    /** Decodes the OXM class value and returns the corresponding
     * constant. If the code is not recognized, the special value
     * {@link OxmClass#UNKNOWN} is returned.
     *
     * @param code the encoded OXM class
     * @param pv the protocol version
     * @return the OXM class
     * @throws VersionMismatchException if the OXM class is not supported
     *          in the given version
     */
    static OxmClass decode(int code, ProtocolVersion pv) {
        OxmClass oc = null;
        for (OxmClass c: values())
            if (c.code == code) {
                oc = c;
                break;
            }
        // exit if not found
        if (oc == null)
            return UNKNOWN;

        // validate version constraints
        // TODO: Confirm lessThan 1.2 is correct.
        verMin12(pv, oc.toString());
        return oc;
    }
}
