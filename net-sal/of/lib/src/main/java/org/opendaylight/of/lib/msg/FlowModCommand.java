/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
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

/**
 * Designates flow mod commands; Since 1.0.
 *
 * @author Simon Hunt
 */
public enum FlowModCommand implements OfpCodeBasedEnum {
    /** New flow; Since 1.0. */
    ADD(0),
    /** Modify all matching flows; Since 1.0. */
    MODIFY(1),
    /** Modify entry strictly matching wildcards and priority; Since 1.0. */
    MODIFY_STRICT(2),
    /** Delete all matching flows; Since 1.0. */
    DELETE(3),
    /** Delete entry strictly matching wildcards and priority; Since 1.0. */
    DELETE_STRICT(4),
    ;

    private final int code;

    FlowModCommand(int code) {
        this.code = code;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }

    /** Decodes the flow mod command code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded command
     * @param pv the protocol version
     * @return the flow mod command
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the flow mod command is not
     *             supported in the given version
     */
    static FlowModCommand decode(int code, ProtocolVersion pv)
            throws DecodeException {
        FlowModCommand cmd = null;
        for (FlowModCommand c: values())
            if (c.code == code) {
                cmd = c;
                break;
            }
        if (cmd == null)
            throw new DecodeException("FlowModCommand: unknown code: " + code);

        // No version constraints, currently: same across v1.0 - v1.3
        return cmd;
    }
}
