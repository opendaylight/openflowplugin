/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.of.common.HandshakeLogic;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OfmFeaturesReply;
import org.opendaylight.of.lib.msg.OfmHello;
import org.opendaylight.util.TimeUtils;

/**
 * Test fixture for MockOpenflowSwitchTest.
 * <p>
 * Embodies the information about a network connection to an OpenFlow datapath.
 * One or more of these objects are aggregated in a {@code DpInfo} object
 * to provide a complete picture of all connections to a datapath.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
class OfConnection {
    static final TimeUtils TIME = TimeUtils.getInstance();

    private static final String E_ALREADY_NEGOTIATED =
            "Protocol Version has already been negotiated: ";
    private static final String E_HELLO_OUT_ALREADY =
            "Outbound HELLO message already sent.";
    private static final String E_HELLO_IN_DUP =
            "Extra HELLO message from datapath: ";
    private static final String E_FEATURES_IN_DUP =
            "Extra FEATURES_REPLY message from datapath: ";


    final int channelId;
    final String channelStr;

    private OfmHello helloOut;
    private OfmHello helloIn;
    private ProtocolVersion negotiated;
    private OfmFeaturesReply features;
    DataPathId dpid;
    int auxId;
    long readyAt;
    long lastMessageAt;

    /** Constructs a connection.
     *
     * @param channelId the channel id
     * @param channelStr a string representation of the channel
     */
    OfConnection(int channelId, String channelStr) {
        this.channelId = channelId;
        this.channelStr = channelStr;
    }

    /** Called from the {@code MessageHandler} when the outbound HELLO
     * message is sent. If we have both HELLOs, we'll negotiate the version.
     *
     * @param hello the outbound hello
     * @param memo a memento, storing information about the request
     */
    synchronized void outBoundHello(OfmHello hello, ConnMemo memo) {
        if (helloOut != null)
            throw new IllegalStateException(E_HELLO_OUT_ALREADY);

        helloOut = hello;
        if (helloIn != null)
            negotiateVersion(memo);
    }

    /** Called from the {@code MessageHandler} when the inbound HELLO
     * message is received.
     *
     * @param hello the inbound hello
     * @param memo negotiation context memento
     */
    synchronized void inBoundHello(OfmHello hello, ConnMemo memo) {
        if (helloIn != null)
            throw new IllegalStateException(E_HELLO_IN_DUP + hello);

        helloIn = hello;
        lastMessageAt = TIME.currentTimeMillis();
        if (helloOut != null)
            negotiateVersion(memo);
    }

    /** Called from the {@code MessageHandler} when the inbound
     * FEATURES_REPLY message is received.
     *
     * @param frep the inbound features-reply
     * @param memo negotiation context memento
     */
    synchronized void inBoundFeaturesReply(OfmFeaturesReply frep,
                                           ConnMemo memo) {
        if (features != null)
            throw new IllegalStateException(E_FEATURES_IN_DUP + frep);

        features = frep;
        dpid = frep.getDpid();
        auxId = frep.getAuxId();
        lastMessageAt = TIME.currentTimeMillis();
        readyAt = lastMessageAt;

        memo.handshakeComplete();
    }


    /** Determines the protocol version to be used for this connection.
     *  Should only be called once, when both HELLO messages are available.
     *  <p>
     *  The memento is passed in so that we can prod the handler into
     *  sending out the FEATURES_REQUEST.
     *  <p>
     *  Note that this method is only called from synchronized methods.
     *
     *  @param memo the request memento
     */
    private void negotiateVersion(ConnMemo memo) {
        if (negotiated != null)
            throw new IllegalStateException(E_ALREADY_NEGOTIATED + negotiated);
        negotiated = HandshakeLogic.negotiateVersion(helloIn, helloOut);
        memo.versionNegotiatedAs(negotiated);
    }

    /** Returns the protocol version from the header of the HELLO message
     * from the datapath. If the inbound HELLO has not yet been received,
     * {@code null} will be returned.
     *
     * @return the inbound HELLO message's protocol version
     */
    synchronized ProtocolVersion getInBoundHelloVersion() {
        return helloIn == null ? null : helloIn.getVersion();
    }

    /** Returns the FEATURES_REPLY message from the datapath. Will be
     * {@code null} until the handshake is complete.
     *
     * @return the FEATURES_REPLY message from the datapath
     */
    synchronized OfmFeaturesReply getFeaturesReply() {
        return features;
    }

    /** Returns the negotiated protocol version for this connection.
     *
     * @return the negotiated version
     */
    ProtocolVersion getNegotiated() {
        return negotiated;
    }

    /** Marks the lastMessageAt timestamp with "now". */
    void stampTime() {
        lastMessageAt = TIME.currentTimeMillis();
    }

}
