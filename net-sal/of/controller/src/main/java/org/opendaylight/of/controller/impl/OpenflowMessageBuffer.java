/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.util.nbio.IOLoop;
import org.opendaylight.util.nbio.MessageBuffer;
import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.err.ECodeHelloFailed;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.*;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageFactory.supportedVersions;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.Log.stackTraceSnippet;

/**
 * Message buffer capable of transferring {@link OpenflowMessage OpenFlow
 * messages}. It also manages the initial device hand-shake protocol.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
// TODO: inspect for unnecessary synchronization, now that we are NBIO bound
class OpenflowMessageBuffer extends MessageBuffer<OpenflowMessage> {

    private static final String E_INCOMPAT_VER =
            "Controller supports 1.0 and 1.3 only";
    private static final String E_CLOSING_INCOMPAT_CHAN =
            "Closing incompatible channel remoteAddr={}, pv={}";
    private static final String E_SEND =
            "Unable to send message";
    private static final String E_SEND_MSG =
            "Unable to send message {} to datapath {}: {}";
    private static final String E_SWITCH_SENT_ERROR =
            "Switch sent error; id={}, err={}";

    // the time (ms) we'll wait for a datapath's HELLO message after connection
    //  before sending our own HELLO.
    private static final long GRACE_MS = 200;

    // scheduler for HELLO grace period time-outs
    private static final ScheduledExecutorService graceExec =
            Executors.newSingleThreadScheduledExecutor();

    private final OpenflowController controller;

    private OpenflowConnection connection;
    private ScheduledFuture graceFuture;

    /**
     * Creates a new buffer for transfer of OpenFlow messages.
     *
     * @param ch backing socket channel
     * @param loop driver loop
     * @param controller OpenFlow controller
     * @param sslContext optional TLS context
     */
    OpenflowMessageBuffer(ByteChannel ch, 
                          IOLoop<OpenflowMessage, ?> loop, 
                          OpenflowController controller, 
                          SSLContext sslContext) {
        super(ch, loop, sslContext);
        this.log = LoggerFactory.getLogger(OpenflowController.class);
        this.controller = controller;
    }

    @Override
    public String toString() {
        return "{OfMsgBuff:" +
                (connection != null ? connection : "unconnected") + "}";
    }

    @Override
    protected OpenflowMessage get(ByteBuffer rb) {
        try {
            return MessageFactory.parseMessage(rb);
        } catch (MessageParseException e) {
            // Message factory has already logged the issue
            return null;
        }
    }

    @Override
    protected void put(OpenflowMessage message, ByteBuffer wb) {
        try {
            MessageFactory.encodeMessage(message, wb);
        } catch (IncompleteMessageException | IncompleteStructureException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation also delegates to the controller that the
     * connection has been closed.
     */
    @Override
    public void discard() {
        synchronized (this) {
            if (alreadyDiscarded())
                return;
        }
        super.discard();

        log.debug("Disconnected {}", connection);
        controller.connectionClosed(connection);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation kicks off the initial version negotiation and
     * device interrogation hand-shake process.
     */
    @Override
    public void setKey(SelectionKey key) {
        super.setKey(key);
        kickOffHandshake((SocketChannel) key().channel());
    }

    /**
     * Returns the associated OpenFlow connection descriptor.
     *
     * @return OpenFlow connection descriptor
     */
    OpenflowConnection connection() {
        return connection;
    }

    /**
     * Initiates version negotiation hand-shake and subsequent device
     * interrogation.
     *
     * @param channel the backing socket channel
     */
    private void kickOffHandshake(SocketChannel channel) {
        connection = new OpenflowConnection(channel, this);
        log.debug("Received connection from {}", connection);

        // Inform the controller of the connection event
        controller.receivedDuringHandshake(connection, null);
        // prepare for no HELLO forthcoming from the datapath
        scheduleOutboundHello();
    }

    synchronized void scheduleOutboundHello() {
        OfmHello in = connection.getInBoundHello();
        if (in != null) {
            // if the datapath already sent its HELLO, reply appropriately
            replyWithHello(in);

        } else {
            // otherwise, schedule a controller-initiated hello
            graceFuture = graceExec.schedule(new Runnable() {
                @Override
                public void run() {
                    sendControllerInitiatedHello();
                }
            }, GRACE_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void replyWithHello(OfmHello in) {
        OfmHello out = createHello(in);
        sendHandshake(out);
        connection.outBoundHello(out);
    }

    private OfmHello createHello(OfmHello in) {
        // if the datapath HELLO version is 1.0, we'll reply with a
        //  simple 1.0 HELLO (with no payload)...
        if (in.getVersion() == ProtocolVersion.V_1_0)
            return (OfmHello) MessageFactory.create(in, HELLO).toImmutable();

        // otherwise we'll reply with a 1.3 HELLO with version bitmap
        return createStandardHello(in);
    }

    private OfmHello createStandardHello(OfmHello in) {
        Set<ProtocolVersion> supported = supportedVersions();
        HelloElement versionBitmap =
                HelloElementFactory.createVersionBitmapElement(supported);
        OfmMutableHello mh = (OfmMutableHello)
                MessageFactory.create(versionBitmap.getVersion(), HELLO);
        mh.addElement(versionBitmap);
        if (in != null)
            MessageFactory.copyXid(in, mh);
        else
            mh.clearXid();
        return (OfmHello) mh.toImmutable();
    }

    private synchronized void sendControllerInitiatedHello() {
        graceFuture = null;
        OfmHello out = createStandardHello(null);
        sendHandshake(out);
        connection.outBoundHello(out);
    }

    /**
     * Invoked by the {@link OpenflowConnection} once the features-reply has
     * been received and processed.
     */
    synchronized void handshakeComplete() {
        controller.handshakeComplete(connection);
        if (connection.isMain())
            requestAdditionalDatapathInfo();
    }

    /**
     * Send off requests to the device for additional data.
     */
    private void requestAdditionalDatapathInfo() {
        requestDeviceDescription();
        if (connection.negotiated.ge(V_1_3)) {
            requestPorts();
            requestTableFeatures();
        }
    }

    /**
     * Invoked when we finally have all the data we requested from the device,
     * and so we are now ready to publish to the outside world.
     */
    synchronized void fullHandshakeComplete() {
        DataPathId dpid = connection.dpid;

        // pass the initial port state data up to the controller
        if (connection.negotiated.ge(V_1_3)) { 
            controller.portDataReadyMp(dpid, connection.portDescs);
            connection.portDescs.clear();
        } else { 
            controller.portDataReady(dpid, connection.features.getPorts());
        }
        // signal to the outside world that a new datapath has joined the fray
        controller.newMainConnectionComplete(connection);
    }

    /**
     * Invoked by the {@link OpenflowConnection} once the negotiated protocol
     * version has been determined. If no common version can be established
     * between the controller and the datapath, the version parameter will be
     * {@code null}.
     * <p>
     * If an agreed upon protocol version is specified, this method will send
     * a FEATURES_REQUEST to the datapath.
     *
     * @param pv the negotiated protocol version
     */
    void versionNegotiatedAs(ProtocolVersion pv) {
        if (pv != null) {
            // send features request using the negotiated version
            sendHandshake(create(pv, FEATURES_REQUEST).toImmutable());
        } else {
            // send error, using switch's protocol version...
            ProtocolVersion swPv = connection.getInBoundHelloVersion();

            OfmMutableError err = (OfmMutableError) create(swPv, ERROR);
            err.errorType(ErrorType.HELLO_FAILED);
            err.errorCode(ECodeHelloFailed.INCOMPATIBLE);
            err.errorMessage(E_INCOMPAT_VER);
            sendHandshake(err.toImmutable());
            log.warn(E_CLOSING_INCOMPAT_CHAN, connection.remoteAddress, swPv);
            connection.revokeConnection();
            discard();
        }
    }

    private void requestDeviceDescription() {
        requestMp(MultipartType.DESC);
    }

    private void requestPorts() {
        requestMp(MultipartType.PORT_DESC);
    }

    private void requestTableFeatures() {
        requestMp(MultipartType.TABLE_FEATURES);
    }

    private void requestMp(MultipartType mt) {
        OpenflowMessage req = create(connection.negotiated,
                MULTIPART_REQUEST, mt).toImmutable();
        connection.extHsResponse.put(req.getXid(), req);
        guardedSend(req);
    }

    private void guardedSend(OpenflowMessage msg) {
        try {
            controller.send(msg, connection);
        } catch (OpenflowException e) {
            log.error(E_SEND_MSG, msg, connection.dpid, stackTraceSnippet(e));
        }
    }

    /**
     * Emits echo request.
     */
    void sendEchoRequest() {
        guardedSend(create(connection.negotiated, ECHO_REQUEST).toImmutable());
    }

    /**
     * Notifies the controller that connection has gone idle.
     */
    void signalDisconnect() {
        controller.closingIdleConnection(connection);
    }

    /**
     * Processes the specified OpenFlow message.
     *
     * @param msg in-bound OpenFlow message
     */
    void processMessage(OpenflowMessage msg) {
        MessageType type = msg.getType();
        connection.stampTime();

        // Give priority to the packet-in messages first if done handshaking
        if (type == PACKET_IN && connection.fullHandshakeDone) {
            handleMsg(msg);

        } else if (type == HELLO) {
            handleHello((OfmHello) msg);
        } else if (type == FEATURES_REPLY) {
            handleFeaturesReply((OfmFeaturesReply) msg);

        } else if (type == MULTIPART_REPLY && !connection.fullHandshakeDone) {
            handleHandshakeMpReply((OfmMultipartReply) msg);

        } else if (type == ECHO_REQUEST) {
            // Intercept any heart-beat messages from the datapath
            handleEchoRequest((OfmEchoRequest) msg);
        } else if (type == ECHO_REPLY) {
            handleEchoReply((OfmEchoReply) msg);

        } else if (type == ERROR && !connection.fullHandshakeDone) {
            handleHandshakeError((OfmError) msg);
            
        } else if (connection.fullHandshakeDone) {
            // All other message types are passed up to the controller
            // This includes any ECHO_REPLY messages solicited by
            // controller-initiated heart-beat requests.
            handleMsg(msg);
        }
    }

    private synchronized void handleHello(OfmHello in) {
        cancelHelloFuture();
        controller.receivedDuringHandshake(connection, in);
        if (connection.inBoundHello(in))
            replyWithHello(in);
    }

    private void cancelHelloFuture() {
        if (graceFuture != null) {
            graceFuture.cancel(false);
            graceFuture = null;
        }
    }

    private void handleFeaturesReply(OfmFeaturesReply reply) {
        controller.receivedDuringHandshake(connection, reply);
        connection.inBoundFeaturesReply(reply);
    }

    private void handleHandshakeMpReply(OfmMultipartReply msg) {
        controller.tallyAndRecordMsg(connection, msg);
        MultipartType mpt = msg.getMultipartType();
        if (mpt == MultipartType.DESC) {
            connection.addDescription(msg);
        } else if (mpt == MultipartType.PORT_DESC) {
            connection.addPortDesc(msg);
        } else if (mpt == MultipartType.TABLE_FEATURES) {
            connection.addTableFeatures(msg);
        }
    }

    private void handleHandshakeError(OfmError error) {
        if (!connection.basicHandshakeDone) {
            // we have an error during the basic handshake
            controller.receivedDuringHandshake(connection, error);
            logErrorAndRevoke(error);
        } else {
            // we have an error during the extended handshake
            controller.tallyAndRecordMsg(connection, error);
            OpenflowMessage req = connection.extHsResponse.get(error.getXid());
            if (req == null) {
                // received an error for a message we never sent
                // for now we'll log the error and continue
                logError(error);
            } else {
                OfmMultipartRequest r = (OfmMultipartRequest) req;
                MultipartType mt = r.getMultipartType();
                if (mt == MultipartType.TABLE_FEATURES) {
                    log.warn(E_SWITCH_SENT_ERROR, connection.dpid, error);
                    connection.addTableFeatures(null);
                } else {
                    // if the switch cannot respond to either DESC or PORT_DESC
                    // then we won't accept the connection.
                    logErrorAndRevoke(error);
                }
            }
        }
    }

    private void logError(OfmError error) {
        log.warn(E_SWITCH_SENT_ERROR, connection.remoteAddress, error);
    }

    private void logErrorAndRevoke(OfmError error) {
        connection.revokeConnection();
        logError(error);
        discard();
    }

    private void handleEchoRequest(OfmEchoRequest request) {
        controller.tallyAndRecordMsg(connection, request);
        guardedSend(createEchoReply(request));
    }

    private OpenflowMessage createEchoReply(OfmEchoRequest request) {
        OfmMutableEchoReply reply = 
                (OfmMutableEchoReply) create(request, ECHO_REPLY);
        byte[] data = request.getData();
        if (data != null)
            reply.data(data);
        return reply.toImmutable();
    }

    // Informs the controller and updates connection time-out state
    private void handleEchoReply(OfmEchoReply reply) {
        // Pass up the reply to the controller as a regular message
        // TODO: Consider filtering out the replies for requests we sent
        if (!connection.fullHandshakeDone)
            controller.tallyAndRecordMsg(connection, reply);
        else
            handleMsg(reply);
    }

    // Informs the controller and tracks connection with xid
    private void handleMsg(OpenflowMessage msg) {
        controller.incomingMsg(connection, msg);
    }

    // Send the specified hand-shake message after informing the controller
    private void sendHandshake(OpenflowMessage msg) {
        controller.sentDuringHandshake(connection, msg);
        try {
            queue(msg);
        } catch (IOException e) {
            log.warn(E_SEND, e);
            discard();
        }
    }
}