/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.BigPortNumber;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;

/**
 * Represents a flow stats request (multipart body); Since 1.0.
 *
 * @author Simon Hunt
 */
public class MBodyFlowStatsRequest extends OpenflowStructure
        implements MultipartBody {

    private static final int BODY_LEN_10 = 44;
    private static final int BODY_LEN_11 = 120;
    private static final int BODY_FIXED_LEN = 32;

    TableId tableId;
    BigPortNumber outPort;
    GroupId outGroup;
    long cookie;
    long cookieMask;
    Match match;

    /**
     * Constructs a multipart body FLOW type request.
     *
     * @param pv the protocol version
     */
    public MBodyFlowStatsRequest(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{tid:")
                .append(tableId)
                .append(",outP=").append(Port.portNumberToString(outPort));
        if (version.gt(V_1_0))
            sb.append(",outG=").append(outGroup);
        sb.append(",...}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Table ID : ").append(tableId)
                .append(EOLI).append("Out Port : ")
                    .append(Port.portNumberToString(outPort));
        if (version.gt(V_1_0)) {
            sb.append(EOLI).append("Out Group: ").append(outGroup)
                    .append(EOLI).append("Cookie   : ").append(hex(cookie))
                    .append(EOLI).append("Cook.Mask: ").append(hex(cookieMask));
        }
        sb.append(EOLI).append("Match    : ")
                .append(match.toDebugString(INDENT_SIZE));
        return sb.toString();
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(tableId, outPort, outGroup, match);
    }

    @Override
    public int getTotalLength() {
        if (version.ge(V_1_2))
            return BODY_FIXED_LEN + (match == null ? 0 : match.getTotalLength());
        return version == V_1_0 ? BODY_LEN_10 : BODY_LEN_11;
    }

    /** Returns the ID of the table to read; Since 1.0.
     * <p>
     * A value of {@link TableId#ALL} indicates all tables.
     *
     * @return the table id
     */
    public TableId getTableId() {
        return tableId;
    }

    /** Returns the output port that flow entries are required to
     * match; Since 1.0.
     * <p>
     * A value of {@link Port#ANY} indicates no restriction.
     *
     * @return the required out port
     */
    public BigPortNumber getOutPort() {
        return outPort;
    }

    /** Returns the output group that flow entries are required to
     * match; Since 1.1.
     * <p>
     * A value of {@link GroupId#ALL} indicates no restriction.
     * <p>
     * For 1.0, this method will always return null.
     *
     * @return the required out group
     */
    public GroupId getOutGroup() {
        return outGroup;
    }

    /** Returns the cookie value that flow entries are required to
     * match; Since 1.1.
     * <p>
     * For 1.0, this method will always return 0.
     *
     * @see #getCookieMask()
     * @return the required cookie value
     */
    public long getCookie() {
        return cookie;
    }

    /** Returns the mask used to restrict the cookie bits that must match;
     * Since 1.1.
     * <p>
     * A value of 0 indicates no restriction.
     * <p>
     * For 1.0, this method will always return 0.
     *
     * @see #getCookie()
     * @return the cookie mask
     */
    public long getCookieMask() {
        return cookieMask;
    }

    /** Returns the match describing the fields that flow entries should
     * match; Since 1.0.
     *
     * @return the required match
     */
    public Match getMatch() {
        return match;
    }
}
