/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.ProtocolVersion;

/**
 * Designates hello message elements; Since 1.3.
 *
 * @author Simon Hunt
 */
public enum HelloElementType implements OfpCodeBasedEnum {
    /** Bitmap of supported versions; Since 1.3. */
    VERSION_BITMAP(1),
    /** Designates any type code that is not defined in the spec. */
    UNKNOWN(0),
    ;

    private final int code;

    HelloElementType(int code) {
        this.code = code;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    /** Decodes an element type code, returning the corresponding
     * enumeration constant. If the code is not one that is defined in
     * the specification, the special value {@code UNKNOWN} is returned.
     *
     * @param code the encoded type
     * @param pv the protocol version
     * @return the corresponding constant
     */
    static HelloElementType decode(int code, ProtocolVersion pv) {
        // Implementation note: Protocol version here is the highest version
        //   supported by the sender; not the "version" of the message
        //   structure. Therefore we should not throw an exception if the
        //   version is < 1.3 (when the element types were added to the
        //   protocol).
        HelloElementType type = UNKNOWN;
        for (HelloElementType t: values())
            if (t.code == code) {
                type = t;
                break;
            }

        // no further version constraints
        return type;
    }
}
