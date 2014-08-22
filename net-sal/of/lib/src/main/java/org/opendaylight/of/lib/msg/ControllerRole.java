/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.OfpCodeBasedEnum;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;

import static org.opendaylight.of.lib.CommonUtils.verMin12;

/**
 * Designates the role of a controller; Since 1.2.
 *
 * @author Simon Hunt
 */
public enum ControllerRole implements OfpCodeBasedEnum {
    /** Don't change the current role; Since 1.2. */
    NO_CHANGE(0),
    /** Default role, full access; Since 1.2. */
    EQUAL(1),
    /** Full access, at most one master; Since 1.2. */
    MASTER(2),
    /** Read-only access; Since 1.2. */
    SLAVE(3),
    ;

    private final int code;

    ControllerRole(int code) {
        this.code = code;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    /** Decodes the controller role code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded role
     * @param pv the protocol version
     * @return the controller role
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the role type is not
     *          supported in the given version
     */
    static ControllerRole decode(int code, ProtocolVersion pv)
            throws DecodeException {
        ControllerRole role = null;
        for (ControllerRole r: values())
            if (r.code == code) {
                role = r;
                break;
            }
        if (role == null)
            throw new DecodeException("ControllerRole: unknown code: " + code);
        verMin12(pv, role.name());
        return role;
    }
}
