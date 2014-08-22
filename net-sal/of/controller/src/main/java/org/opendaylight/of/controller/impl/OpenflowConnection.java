/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.common.HandshakeLogic;
import org.opendaylight.of.controller.ConnectionDetails;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MBodyPortDesc;
import org.opendaylight.of.lib.mp.MBodyTableFeatures;
import org.opendaylight.of.lib.msg.OfmFeaturesReply;
import org.opendaylight.of.lib.msg.OfmHello;
import org.opendaylight.of.lib.msg.OfmMultipartReply;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.TimeUtils;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.PortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;

/**
 * Embodies the information about a network connection to an OpenFlow datapath.
 * One or more of these objects are aggregated in a {@code DpInfo} object
 * to provide a complete picture of all connections to a datapath.
 *
 * @author Scott Simes
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
class OpenflowConnection {

    private Logger log = LoggerFactory.getLogger(OpenflowController.class);

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            OpenflowConnection.class, "openflowConnection");

    private static final String E_NO_SOCK = RES.getString("e_no_sock");
    private static final String E_SEND = RES.getString("e_send");

    static final TimeUtils TIME = TimeUtils.getInstance();

    private static final String E_ALREADY_NEGOTIATED = RES
            .getString("e_already_negotiated");
    private static final String E_HELLO_OUT_ALREADY = RES
            .getString("e_hello_out_already");
    private static final String E_HELLO_IN_DUP = RES
            .getString("e_hello_in_dup");
    private static final String E_FEATURES_IN_DUP = RES
            .getString("e_features_in_dup");
    private static final String E_NULL_DPID = RES.getString("e_null_dpid");

    // Main connection aux id is zero.
    private static final int MAIN_ID = 0;

    // the number of additional messages we expect from the device, based on 
    // protocol version

    // MP/DESC
    private static final int EXTRA_DATA_10 = 1;
    // MP/DESC, MP/PORT_DESC, MP/TABLE_FEATURES
    private static final int EXTRA_DATA_13 = 3;


    // Container for a few run-time tunable parameters
    static class Config {
        int idleCheckMs = 500;
        int maxIdleMs = 5000;
        int maxEchoMs = 5000;
        int maxEchoAttempts = 5;
    }

    private static final Config CONFIG = new Config();


    // Associated message transfer buffer
    final OpenflowMessageBuffer buffer;

    // Remote end-point address and port
    final IpAddress remoteAddress;
    final PortNumber remotePort;

    // Hand-shake negotiation state
    private OfmHello helloOut;
    private OfmHello helloIn;
    OfmFeaturesReply features;
    private boolean connectionRevoked = false;

    // State information about the datapath and readiness
    ProtocolVersion negotiated;
    DataPathId dpid;
    int auxId;
    long readyAt;
    long lastMessageAt;
    boolean basicHandshakeDone;
    boolean fullHandshakeDone;
    private int pendingDataCount;

    MBodyDesc deviceDesc;
    final List<MBodyPortDesc.Array> portDescs = new ArrayList<>();
    final List<MBodyTableFeatures.Array> tableFeats = new ArrayList<>();
    // Track extended handshake request message XIDs...
    final Map<Long, OpenflowMessage> extHsResponse = new ConcurrentHashMap<>(3);
    boolean noTF = false;

    // Connection liveness state
    private byte echoAttempts;
    private long nextEchoRequest;

    /**
     * Creates a connection associated with the specified channel and message
     * buffer.
     *
     * @param channel socket channel
     * @param buffer message buffer
     */
    OpenflowConnection(SocketChannel channel, OpenflowMessageBuffer buffer) {
        this.buffer = buffer;

        InetSocketAddress sock = null;
        try {
            sock = (InetSocketAddress) channel.getRemoteAddress();
        } catch (IOException e) {
            log.warn(E_NO_SOCK, e);
        }

        // Absorb issues with resolving address into undetermined address/port
        remoteAddress = sock != null ? IpAddress.valueOf(sock.getAddress()) :
                                IpAddress.UNDETERMINED_IPv4;
        remotePort = sock != null ? PortNumber.valueOf(sock.getPort()) :
                                PortNumber.valueOf(PortNumber.MIN_VALUE);
        // stamp time, so idle check doesn't kick in immediately
        stampTime();
    }

    @Override
    public String toString() {
        return "{OfConn:" + remoteAddress + "}";
    }

    /**
     * Closes the specified connection and discards the associated buffer.
     */
    void close() {
        buffer.discard();
    }

    /**
     * Queues the specified message to the associated message buffer.
     *
     * @param message message to send
     * @throws OpenflowException if unable to queue message
     */
    void send(OpenflowMessage message) throws OpenflowException {
        try {
            buffer.queue(message);
        } catch (IOException e) {
            throw new OpenflowException(E_SEND, e);
        }
    }

    /**
     * Queues the specified messages to the associated message buffer.
     *
     * @param messages list of messages to send
     * @throws OpenflowException if unable to queue any of the messages
     */
    void send(List<OpenflowMessage> messages) throws OpenflowException {
        try {
            buffer.queue(messages);
        } catch (IOException e) {
            throw new OpenflowException(E_SEND, e);
        }
    }

    /**
     * Called when the outbound HELLO message is sent. If we have both HELLOs,
     * we'll negotiate the version.
     *
     * @param hello the outbound hello
     */
    synchronized void outBoundHello(OfmHello hello) {
        if (helloOut != null)
            throw new IllegalStateException(E_HELLO_OUT_ALREADY);

        helloOut = hello;
        if (helloIn != null)
            negotiateVersion();
    }

    /**
     * Called when the in-bound HELLO message is received. If the controller
     * has already sent the out-bound HELLO message, the protocol version will
     * be negotiated from the two messages. Otherwise, we indicated that the
     * outbound reply is still required by returning true.
     *
     * @param hello the in-bound hello
     * @return true, if the out-bound hello needs to be sent still
     */
    synchronized boolean inBoundHello(OfmHello hello) {
        if (helloIn != null)
            throw new IllegalStateException(E_HELLO_IN_DUP + hello);

        helloIn = hello;
        if (helloOut == null)
            return true;

        negotiateVersion();
        return false;
    }

    /**
     * Called when the in-bound FEATURES_REPLY message is received.
     *
     * @param frep the in-bound features-reply
     */
    synchronized void inBoundFeaturesReply(OfmFeaturesReply frep) {
        if (features != null)
            throw new IllegalStateException(E_FEATURES_IN_DUP + frep);
        if (frep.getDpid() == null)
            throw new NullPointerException(E_NULL_DPID + frep);

        features = frep;
        dpid = frep.getDpid();
        auxId = frep.getAuxId();
        readyAt = lastMessageAt;
        pendingDataCount = negotiated.ge(V_1_3) ? EXTRA_DATA_13 : EXTRA_DATA_10;

        // mark basic handshake as done, for both main and aux.
        basicHandshakeDone = true;
        // mark full handshake as complete for auxiliary connections
        fullHandshakeDone = !isMain(); 
        buffer.handshakeComplete();
    }

    void addDescription(OfmMultipartReply msg) {
        // NOTE: we assume there will be only one part to this message
        // so we simply overwrite the reference.
        deviceDesc = (MBodyDesc) msg.getBody();
        if (!msg.hasMore())
            checkForAllDataReceived();
    }

    void addPortDesc(OfmMultipartReply msg) {
        portDescs.add((MBodyPortDesc.Array) msg.getBody());
        if (!msg.hasMore())
            checkForAllDataReceived();
    }

    void addTableFeatures(OfmMultipartReply msg) {
        if (msg == null) {
            // the switch sent an error in response to TF request
            noTF = true;
            checkForAllDataReceived();
        } else {
            tableFeats.add((MBodyTableFeatures.Array) msg.getBody());
            if (!msg.hasMore()) {
                // just for our own sanity...
                int tfCount = 0;
                for (MBodyTableFeatures.Array a : tableFeats)
                    tfCount += a.getList().size();
                if (tfCount == 0) {
                    noTF = true;
                    log.warn(MSG_NO_TF_PAYLOAD, dpid);
                }
                // finish up...
                checkForAllDataReceived();
            }
        }
    }

    private static final String MSG_NO_TF_PAYLOAD =
            "No table feature payload in MP/Reply from dpid {}";

    private void checkForAllDataReceived() {
        if (--pendingDataCount == 0) {
            fullHandshakeDone = true;
            buffer.fullHandshakeComplete();
        }
    }

    /**
     * Determines the protocol version to be used for this connection. Should
     * only be called once, when both HELLO messages are available.
     * <p>
     * The associated buffer is notified that the version is negotiated.
     * <p>
     * Note that this method is only called from synchronized methods.
     */
    private void negotiateVersion() {
        if (negotiated != null)
            throw new IllegalStateException(E_ALREADY_NEGOTIATED + negotiated);
        negotiated = HandshakeLogic.negotiateVersion(helloIn, helloOut);
        buffer.versionNegotiatedAs(negotiated);
    }

    /**
     * Steps through the connection liveness protocol.
     *
     * @param now now in milliseconds
     * @return true, if still lively; false otherwise
     */
    synchronized boolean checkLiveness(long now) {
        if (now - lastMessageAt < CONFIG.maxIdleMs || now < nextEchoRequest)
            return true;

        if (echoAttempts++ < CONFIG.maxEchoAttempts) {
            buffer.sendEchoRequest();
            nextEchoRequest = now + CONFIG.maxEchoMs;
            return true;
        } else {
            buffer.signalDisconnect();
            return false;
        }
    }

    /**
     * Returns the in-bound HELLO from the datapath, or null if it hasn't
     * arrived yet.
     *
     * @return the in-bound HELLO message
     */
    synchronized OfmHello getInBoundHello() {
        return helloIn;
    }

    /**
     * Returns the protocol version from the header of the HELLO message from
     * the datapath. If the in-bound HELLO has not yet been received,
     * {@code null} will be returned.
     *
     * @return the in-bound HELLO message's protocol version
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

    /**
     * Returns the negotiated protocol version for this connection.
     *
     * @return the negotiated version
     */
    ProtocolVersion getNegotiated() {
        return negotiated;
    }

    /** Marks the lastMessageAt timestamp with "now". */
    void stampTime() {
        lastMessageAt = TIME.currentTimeMillis();
        // we got a message from the datapath ... reset echo timer
        nextEchoRequest = 0;
        echoAttempts = 0;
    }

    /**
     * Returns the configuration parameters descriptor.
     *
     * @return configuration singleton
     */
    static Config getConfig() {
        return CONFIG;
    }

    /**
     * Invoked by the controller to revoke the connection in the case of an
     * unsupported protocol version, or to invalidate a duplicate DPID.
     */
    void revokeConnection() {
        connectionRevoked = true;
    }

    /**
     * Returns true if this connection has been revoked, (because a duplicate
     * dpid was detected).
     *
     * @return true if revoked
     */
    boolean revoked() {
        return connectionRevoked;
    }

    /**
     * Returns true if this is a main connection. In other words, the value
     * of {@code auxId} is zero.
     * 
     * @return true if this is a main connection
     */
    boolean isMain() {
        return auxId == MAIN_ID;
    }
    
    // =====================================================================
    // SNAPSHOT

    /**
     * Creates and returns a snapshot of this connection.
     *
     * @return a snapshot of this connection
     */
    ConnectionDetails snapshot() {
        return new ConnectionSnapshot(this);
    }


    private static class ConnectionSnapshot implements ConnectionDetails {

        private final IpAddress remoteAddress;
        private final PortNumber remotePort;
        private final OfmHello helloOut;
        private final OfmHello helloIn;
        private final ProtocolVersion negotiated;
        private final OfmFeaturesReply features;
        private final DataPathId dpid;
        private final int auxId;
        private final long readyAt;
        private final long lastMessageAt;

        /** Constructs a snapshot of a connection.
         *
         * @param conn the connection to snapshot
         */
        private ConnectionSnapshot(OpenflowConnection conn) {
            remoteAddress = conn.remoteAddress;
            remotePort = conn.remotePort;
            helloOut = conn.helloOut;
            helloIn = conn.helloIn;
            negotiated = conn.negotiated;
            features = conn.features;
            dpid = conn.dpid;
            auxId = conn.auxId;
            readyAt = conn.readyAt;
            lastMessageAt = conn.lastMessageAt;
        }

        @Override
        public String toString() {
            return "{" + remoteAddress + ":" + remotePort + ",hello[" +
                    (helloOut == null ? "---" : "OUT") +
                    (helloIn == null ? "--" : "IN") +
                    "],neg=" + (negotiated == null ? "??" : negotiated) +
                    ",dpid=" + dpid + ",auxId=" + auxId + "}";
        }

        @Override
        public DataPathId dpid() {
            return dpid;
        }

        @Override
        public OfmHello helloOut() {
            return helloOut;
        }

        @Override
        public OfmHello helloIn() {
            return helloIn;
        }

        @Override
        public ProtocolVersion negotiated() {
            return negotiated;
        }

        @Override
        public OfmFeaturesReply features() {
            return features;
        }

        @Override
        public int auxId() {
            return auxId;
        }

        @Override
        public long readyAt() {
            return readyAt;
        }

        @Override
        public long lastMessageAt() {
            return lastMessageAt;
        }

        @Override
        public IpAddress remoteAddress() {
            return remoteAddress;
        }

        @Override
        public PortNumber remotePort() {
            return remotePort;
        }
    }
}