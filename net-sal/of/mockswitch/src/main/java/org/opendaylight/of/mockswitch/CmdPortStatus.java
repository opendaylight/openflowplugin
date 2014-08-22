/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Concrete class for Port-Status command.
 *
 * @author Simon Hunt
 */
public class CmdPortStatus extends ScenarioCommand {

    // {cmd} {switch-variable} {port} {state}
    // portStatus SW10 p3 up
    private static final Pattern RE_PORT_STATUS =
            Pattern.compile("(SW\\w*)\\s+(p(\\d+))\\s+(up|down)");

    /** Designates change in port state. */
    public static enum State {UP, DOWN}

    private final String sw;
    private final int pIndex;
    private final State state;

    /**
     * Constructs a port-status scenario command.
     *
     * @param type   the command type
     * @param params the command parameters
     */
    public CmdPortStatus(CommandType type, String params) {
        super(type, params);
        Matcher m = RE_PORT_STATUS.matcher(params);
        if (m.matches()) {
            sw = m.group(1); // "SW10"
            // TODO: try catch around port number:
            //   we should know the valid range from the mockswitch.def.file
            pIndex = Integer.parseInt(m.group(3));
            state = State.valueOf(m.group(4).toUpperCase());
        } else {
            error = E_INV_PARAMS + params;
            sw = null;
            pIndex = 0;
            state = null;
        }
    }

    /** Returns the switch ID.
     *
     * @return the switch ID
     */
    public String getSwid() {
        return sw;
    }

    /** Returns the port index number.
     *
     * @return the port index
     */
    public int getPortNum() {
        return pIndex;
    }

    /** Returns the state the port is changing to.
     *
     * @return the port state
     */
    public State getState() {
        return state;
    }
}
