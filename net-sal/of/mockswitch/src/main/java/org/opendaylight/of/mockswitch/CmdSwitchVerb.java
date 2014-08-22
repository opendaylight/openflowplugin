/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

/** Concrete class for a switch specific command.
 *
 * @author Simon Hunt
 */
public class CmdSwitchVerb extends ScenarioCommand {
    /** Constructs the switch-verb command.
     *
     * @param type the command type
     * @param params the parameters
     */
    public CmdSwitchVerb(CommandType type, String params) {
        super(type, params);
    }

    /** Returns the variable name assigned to this switch.
     *
     * @return the variable name
     */
    public String getVar() {
        return params;
    }
}
