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

import static org.opendaylight.of.lib.CommonUtils.verMin13;

/**
 * Designates meter mod commands; Since 1.3.
 *
 * @author Simon Hunt
 */
public enum MeterModCommand implements OfpCodeBasedEnum {
    /** New meter; Since 1.3. */
    ADD(0),
    /** Modify specified meter; Since 1.3. */
    MODIFY(1),
    /** Delete specified meter; Since 1.3. */
    DELETE(2),
    ;

    private final int code;

    MeterModCommand(int code) {
        this.code = code;
    }

    @Override
    public int getCode(ProtocolVersion pv) {
        return code;
    }


    /** Decodes the meter mod command code and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded command
     * @param pv the protocol version
     * @return the command
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the meter mod command is not
     *             supported in the given version
     */
    static MeterModCommand decode(int code, ProtocolVersion pv)
            throws DecodeException {
        verMin13(pv);
        MeterModCommand cmd = null;
        for (MeterModCommand c: values())
            if (c.code == code) {
                cmd = c;
                break;
            }
        if (cmd == null)
            throw new DecodeException("MeterModCommand: unknown code: " + code);

        // No further version constraints.
        return cmd;
    }

}
