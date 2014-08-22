/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.mp.MBodyFlowStats;
import org.opendaylight.of.lib.msg.OfmFlowMod;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;

/**
 * Provides useful utilities regarding flow messages.
 *
 * @author Simon Hunt
 */
public class FlowUtils {

    /** Returns true if the specified <em>FlowStats</em> structures represent
     * the same flow. This is determined by examining the protocol version,
     * table ID, priority, and match; if each of these are equal, it is the
     * same flow.
     *
     * @param flowA the first flow
     * @param flowB the second flow
     * @return true if the structures represent the same flow; false otherwise
     * @throws NullPointerException if either parameter is null
     */
    public static boolean sameFlow(MBodyFlowStats flowA, MBodyFlowStats flowB) {
        ProtocolVersion va = flowA.getVersion();
        ProtocolVersion vb = flowB.getVersion();
        if (va != vb)
            return false;

        // ignore table id for 1.0 stats...
        int tableA = va == V_1_0 ? 0 : flowA.getTableId().toInt();
        int tableB = vb == V_1_0 ? 0 : flowB.getTableId().toInt();

        return tableA == tableB &&
                flowA.getPriority() == flowB.getPriority() &&
                flowA.getMatch().equals(flowB.getMatch());
    }

    // private helper method to compute a flow ID
    private static int computeKey(ProtocolVersion pv, int table, int priority,
                                  Match match) {
        int result = pv.hashCode();
        result = 31 * result + table;
        result = 31 * result + priority;
        result = 31 * result + match.hashCode();
        return result;
    }

    /** Computes and returns a unique key for a flow, based on
     * the table ID (ignored for 1.0), priority, and match fields.
     *
     * @param f the flow
     * @return the key
     * @throws NullPointerException if f is null
     */
    public static int flowKey(MBodyFlowStats f) {
        // ignore table id for 1.0 flows...
        ProtocolVersion pv = f.getVersion();
        int table = pv == V_1_0 ? 0 : f.getTableId().toInt();
        return computeKey(pv, table, f.getPriority(), f.getMatch());
    }

    /** Computes and returns a unique key for a flow, based on
     * the table ID (ignored for 1.0), priority, and match fields.
     *
     * @param f the flow
     * @return the key
     * @throws NullPointerException if f is null
     */
    public static int flowKey(OfmFlowMod f) {
        // ignore table id for 1.0 flows...
        ProtocolVersion pv = f.getVersion();
        int table = pv == V_1_0 ? 0 : f.getTableId().toInt();
        return computeKey(pv, table, f.getPriority(), f.getMatch());
    }
}
