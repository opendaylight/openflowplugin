/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
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

import static org.opendaylight.of.lib.CommonUtils.verMin11;

/**
 * Designates group mod commands; Since 1.1.
 *
 * @author Simon Hunt
 */
public enum GroupModCommand implements OfpCodeBasedEnum {
    /** New group; Since 1.1. */
    ADD(0),
    /** Modify all matching groups; Since 1.1. */
    MODIFY(1),
    /** Delete all matching groups; Since 1.1. */
    DELETE(2),
    ;

    private final int code;

    GroupModCommand(int code) {
        this.code = code;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    /** Decodes the group mod command code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded command
     * @param pv the protocol version
     * @return the group mod command
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the group mod command is not
     *             supported in the given version
     */
    static GroupModCommand decode(int code, ProtocolVersion pv)
            throws DecodeException {
        verMin11(pv);
        GroupModCommand cmd = null;
        for (GroupModCommand c: values())
            if (c.code == code) {
                cmd = c;
                break;
            }
        if (cmd == null)
            throw new DecodeException("GroupModCommand: unknown code: " + code);

        // No further version constraints, currently: same across v1.1 - v1.3
        return cmd;
    }
}
