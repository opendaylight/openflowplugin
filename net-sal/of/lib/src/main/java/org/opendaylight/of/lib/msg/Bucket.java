/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.net.BigPortNumber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.util.StringUtils.EOL;

/**
 * Represents a bucket used in {@link OfmGroupMod groups}; Since 1.1.
 * <p>
 * The weight of the bucket is only defined for select groups, and support
 * is optional. The bucket's share of the traffic processed by the group is
 * defined by the individual bucket's weight divided by the sum of the bucket
 * weights in the group.
 * <p>
 * The {@link #getWatchPort() watch port} and
 * {@link #getWatchGroup() watch group} are only required for fast failover
 * groups, and may be optionally implemented for other group types. These
 * fields indicate the port and/or group whose liveness controls whether
 * this bucket is a candidate for forwarding. For fast failover groups, the
 * first bucket defined is the highest-priority bucket, and only the
 * highest-priority live bucket is used.
 *
 * @author Simon Hunt
 */
public class Bucket extends OpenflowStructure {

    int length;
    int weight;
    BigPortNumber watchPort;
    GroupId watchGroup;
    List<Action> actions = new ArrayList<Action>();

    /**
     * Constructs a bucket.
     *
     * @param pv the protocol version
     */
    public Bucket(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        // NOTE: we do not expose length, which is an internal detail
        return "{bucket:wgt=" + weight +
                ",wPort=" + Port.portNumberToString(watchPort) +
                ",wGrp=" + watchGroup +
                ",#a:" + cSize(actions) +
                "}";
    }


    /** Returns a multi-line representation of this bucket.
     * Useful for debug output.
     *
     * @return a multi-line representation
     */
    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /** Returns a multi-line representation of this bucket.
     * Useful for debug output.
     *
     * @param indent number of spaces to prefix each line with
     * @return a multi-line representation
     */
    public String toDebugString(int indent) {
        String istr = StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder(toString());
        if (actions == null || actions.isEmpty())
            sb.append(EOL).append(istr).append(INDENT).append(NONE);
        else {
            for (Action a: actions)
                sb.append(EOL).append(istr).append(INDENT).append(a);
        }
        return sb.toString();
    }

    // Implementation Note:
    // The length field is an implementation detail, and is not exposed
    //  outside the package, since the consumer should neither know nor care.

    /** Returns the weight of this bucket; Since 1.1.
     *
     * @return the weight
     */
    public int getWeight() {
        return weight;
    }

    /** Returns the watch port id; Since 1.1.
     *
     * @return the watch port id
     */
    public BigPortNumber getWatchPort() {
        return watchPort;
    }

    /** Returns the ID of the watch group.
     *
     * @return the watch group id
     */
    public GroupId getWatchGroup() {
        return watchGroup;
    }

    /** Returns the actions associated with this bucket; Since 1.1.
     *
     * @return the actions
     */
    public List<Action> getActions() {
        return actions == null ? null : Collections.unmodifiableList(actions);
    }
}
