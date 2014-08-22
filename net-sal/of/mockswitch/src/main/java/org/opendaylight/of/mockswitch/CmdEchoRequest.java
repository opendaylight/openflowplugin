/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.util.ByteUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Concrete class for Echo-Request command.
 *
 * @author Simon Hunt
 */
public class CmdEchoRequest extends ScenarioCommand {

    // {cmd} {switch-variable} {hex-string-payload}
    // echoRequest SW10 0affbc
    private static final Pattern RE_ECHO_REQUEST =
            Pattern.compile("(SW\\w*)\\s+(x|([0-9a-fA-F]{2})+)");
    private static final String X = "x";

    private final String sw;
    private final byte[] payload;

    /**
     * Constructs an echo-request scenario command.
     *
     * @param type   the command type
     * @param params the command parameters
     */
    public CmdEchoRequest(CommandType type, String params) {
        super(type, params);
        Matcher m = RE_ECHO_REQUEST.matcher(params);
        if (m.matches()) {
            sw = m.group(1); // "SW10"
            byte[] hex = null;
            try {
                String h = m.group(2);
                if (!h.equals(X))
                    hex = ByteUtils.parseHex(h);
            } catch (IllegalArgumentException e) {
                error = E_INV_PARAMS + params;
            }
            payload = hex;
        } else {
            error = E_INV_PARAMS + params;
            sw = null;
            payload = null;
        }
    }

    /** Returns the switch ID.
     *
     * @return the switch ID
     */
    public String getSwid() {
        return sw;
    }

    /** Returns the payload of the echo request.
     *
     * @return the payload
     */
    public byte[] getPayload() {
        return payload == null ? null : payload.clone();
    }
}
