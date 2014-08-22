/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.util.NotYetImplementedException;
import org.opendaylight.util.ResourceUtils;

import java.util.ResourceBundle;

/**
 * An embodiment of a command in a mock-switch-bank scenario.
 *
 * @author Simon Hunt
 */
public class ScenarioCommand {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            ScenarioCommand.class, "scenarioCommand");

    protected static final String E_INV_PARAMS = RES.getString("e_inv_params");

    protected final CommandType type;
    protected final String params;
    protected String error;

    /** Constructs a scenario command.
     *
     * @param type the command type
     * @param params the command parameters
     */
    public ScenarioCommand(CommandType type, String params) {
        this.type = type;
        this.params = params;
    }

    /** Returns the command type.
     *
     * @return the command type
     */
    public CommandType getType() {
        return type;
    }

    /** Validates the command; returns null if all is well, or a short
     * string describing the problem.
     *
     * @return a validation error string; or null
     */
    public String validate() {
        return error;
    }

    @Override
    public String toString() {
        return "{" + type + " " + params + "}";
    }

    // ======================================================================

    /** Returns a command encapsulating the given parameters.
     *
     * @param type the command type
     * @param params the parameters
     * @return the command
     */
    public static ScenarioCommand create(CommandType type, String params) {
        switch (type) {
            case SUSPEND:
                return new CmdDirective(type);

            case ACTIVATE:
            case DEACTIVATE:
            case HELLO_SUCCESS:
                return new CmdSwitchVerb(type, params);

            case DELAY:
                return new CmdDelay(type, params);

            case ECHO_REQUEST:
                return new CmdEchoRequest(type, params);

            case PORT_STATUS:
                return new CmdPortStatus(type, params);

//            case FLOW_REMOVED:
//                return new CmdFlowRemoved(type, params);

//            case PACKET_IN:
//                return new CmdPacketIn(type, params);
        }
        throw new NotYetImplementedException(type.toString());
    }
}