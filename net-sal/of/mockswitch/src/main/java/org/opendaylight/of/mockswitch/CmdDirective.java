/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

/** Concrete class for a scenario directive. These are commands
 * that have no parameters.
 *
 * @author Simon Hunt
 */
public class CmdDirective extends ScenarioCommand {
    /** Constructs the directive.
     *
     * @param type the command type
     */
    public CmdDirective(CommandType type) {
        super(type, null);
    }

    @Override
    public String toString() {
        return "{" + type + "}";
    }

}
