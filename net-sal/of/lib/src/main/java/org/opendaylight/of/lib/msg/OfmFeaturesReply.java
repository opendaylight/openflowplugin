/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;


import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;

/**
 * Represents an OpenFlow FEATURES_REPLY message; Since 1.0.
 *
 * @author Simon Hunt
 */
public class OfmFeaturesReply extends OpenflowMessage
        implements Comparable<OfmFeaturesReply> {

    /** Datapath unique id. Encapsulates a VId (implementor-defined value)
     * and MacAddress. */
    DataPathId dpid;

    /** Maximum number of packets buffered (by the switch) at once (u32). */
    long numBuffers;

    /** Number of tables supported by the datapath (u8). */
    int numTables;

    /** Identifies the type of connection from the switch to the controller;
     * Since 1.3.
     * <p>
     * The main connection has this field set to zero, an auxiliary connection
     * has this field set to a non-zero value (u8).
     */
    int auxId;

    /** The capabilities supported by the datapath. */
    Set<Capability> capabilities;

    /** Set of supported action flags; Since 1.0; Removed at 1.1.
     * <p>
     * Designates the actions that the switch supports.
     */
    Set<SupportedAction> suppActions;

    /** List of OpenFlow-enabled ports; Since 1.0; Removed at 1.3.
     * <p>
     * For 1.3 and later, the port list has to be explicitly requested
     * via a {@link OfmMultipartRequest} of
     * type {@link MultipartType#PORT_DESC}.
     */
    List<Port> ports;

    /**
     * Constructs an OpenFlow FEATURES_REPLY message.
     *
     * @param header the message header
     */
    OfmFeaturesReply(Header header) {
        super(header);
    }

    @Override
    public int compareTo(OfmFeaturesReply o) {
        int result = dpid.compareTo(o.dpid);
        return result == 0 ? auxId - o.auxId : result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OfmFeaturesReply that = (OfmFeaturesReply) o;
        return auxId == that.auxId && dpid.equals(that.dpid);
    }

    @Override
    public int hashCode() {
        return 31 * dpid.hashCode() + auxId;
    }

    @Override
    public String toString() {
        final ProtocolVersion pv = getVersion();
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",dpid=").append(dpid)
                .append(",#buf=").append(numBuffers)
                .append(",#tab=").append(numTables);
        if (pv.ge(V_1_3))
              sb.append(",aux=").append(auxId);
        sb.append(",cap=").append(capabilities);
        if (pv == V_1_0)
            sb.append(",supA=").append(suppActions);
        if (pv.lt(V_1_3))
            sb.append(",#Ports=").append(cSize(ports));
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /** Returns a multi-line string representation of this features reply.
     *
     * @param indent the additional indent (in spaces)
     * @return a multi-line string representation
     */
    public String toDebugString(int indent) {
        String eoliSpc = EOLI + StringUtils.spaces(indent);
        final ProtocolVersion pv = getVersion();
        StringBuilder sb = new StringBuilder("Features-Reply(")
                .append(pv).append("):")
                .append(eoliSpc).append("dpid: ").append(dpid)
                .append(eoliSpc).append("#buf: ").append(numBuffers)
                .append(eoliSpc).append("#tab: ").append(numTables);
        if (pv.ge(V_1_3))
            sb.append(eoliSpc).append("aux: ").append(auxId);
        sb.append(eoliSpc).append("cap: ").append(capabilities);
        if (pv == V_1_0)
            sb.append(eoliSpc).append("supA: ").append(suppActions);
        if (pv.lt(V_1_3))
            sb.append(eoliSpc).append("Ports: (")
                    .append(cSize(ports)).append(")...")
                    .append(PortFactory.toDebugString(indent + 4, ports));
        return sb.toString();
    }

    /** Returns the datapath id uniquely identifying the datapath;
     * Since 1.0.
     *
     * @return the datapath id
     */
    public DataPathId getDpid() {
        return dpid;
    }

    /** Returns the maximum number of packets the switch can buffer at once;
     * Since 1.0.
     *
     * @return the max number of packet buffers
     */
    public long getNumBuffers() {
        return numBuffers;
    }

    /** Returns the number of tables supported by the switch; Since 1.0.
     *
     * @return the number of tables supported
     */
    public int getNumTables() {
        return numTables;
    }

    /** Returns the auxiliary id, representing the type of connection from
     * the switch to the controller; Since 1.3.
     * The main connection will have a value
     * of zero; auxiliary connections will have a non-zero value.
     *
     * @return the auxiliary id
     */
    public int getAuxId() {
        return auxId;
    }

    /** Returns the set of capabilities supported by the switch; Since 1.0.
     *
     * @return the set of capabilities.
     */
    public Set<Capability> getCapabilities() {
        return capabilities == null ? null :
                Collections.unmodifiableSet(capabilities);
    }


    /** Set of supported action flags; Since 1.0; Removed at 1.1.
     * <p>
     * Designates the actions that the switch supports.
     *
     * @return the set of supported actions
     * @throws VersionMismatchException if message version is not 1.0
     */
    public Set<SupportedAction> getSupportedActions() {
        if (header.version != V_1_0)
            throw new VersionMismatchException(E_DEPRECATED + V_1_1);
        return suppActions == null ? null :
                Collections.unmodifiableSet(suppActions);
    }

    /** List of OpenFlow-enabled ports; Since 1.0; Removed at 1.3.
     * <p>
     * For 1.3 and later, the port list has to be explicitly requested
     * via a {@link OfmMultipartRequest} of
     * type {@link MultipartType#PORT_DESC}.
     *
     * @return the list of OpenFlow capable ports
     * @throws VersionMismatchException if message version is 1.3 or newer
     */
    public List<Port> getPorts() {
        if (header.version.ge(V_1_3))
            throw new VersionMismatchException(E_DEPRECATED + V_1_3);
        return ports == null ? null : Collections.unmodifiableList(ports);
    }

}
