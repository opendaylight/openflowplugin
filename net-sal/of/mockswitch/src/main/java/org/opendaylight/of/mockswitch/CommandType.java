/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import static org.opendaylight.util.StringUtils.toCamelCase;

/** Scenario command types.
 *
 * @author Simon Hunt
 */
public enum CommandType {
    /** Activate a switch (connect to the controller). */
    ACTIVATE,
    /** Deactivate a switch (disconnect from the controller). */
    DEACTIVATE,
    /** Wait until HELLO negotiation has completed successfully. */
    HELLO_SUCCESS,
    /** Introduce a delay in the scenario. */
    DELAY,
    /** Suspend the scenario. */
    SUSPEND,

    // === async messages from switch to controller...

    /** Send an EchoRequest message to the controller. */
    ECHO_REQUEST,
    /** Send a PortStatus message to the controller. */
    PORT_STATUS,
    /** Send a FlowRemoved message to the controller. */
    FLOW_REMOVED,
    /** Send a PacketIn message to the controller. */
    PACKET_IN,
    ;

    /** Returns the type constant that matches the given
     * camel-case name. If there is no match, null is returned.
     *
     * @param name the name to match
     * @return the corresponding constant
     */
    public static CommandType get(String name) {
        CommandType type = null;
        for (CommandType t: values())
            if (toCamelCase(t).equals(name)) {
                type = t;
                break;
            }
        return type;
    }
}
