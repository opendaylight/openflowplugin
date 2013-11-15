/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionManager;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueKeeper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.Cookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceivedBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;

/**
 * @author mirehak
 */
public class ConnectionConductorImpl implements OpenflowProtocolListener,
        SystemNotificationsListener, ConnectionConductor, ConnectionReadyListener, HandshakeListener {

    protected static final Logger LOG = LoggerFactory
            .getLogger(ConnectionConductorImpl.class);

    /* variable to make BitMap-based negotiation enabled / disabled.
     * it will help while testing and isolating issues related to processing of
     * BitMaps from switches.
     */
    protected boolean isBitmapNegotiationEnable = true;
    protected ErrorHandler errorHandler;

    private final ConnectionAdapter connectionAdapter;
    private ConnectionConductor.CONDUCTOR_STATE conductorState;
    private Short version;

    private SwitchConnectionDistinguisher auxiliaryKey;

    private SessionContext sessionContext;

    protected boolean isFirstHelloNegotiation = true;
    protected Short lastProposedVersion = null;

    private QueueKeeper<OfHeader, DataObject> queueKeeper;
    private ExecutorService hsPool;
    private HandshakeManager handshakeManager;

    /**
     * @param connectionAdapter
     */
    public ConnectionConductorImpl(ConnectionAdapter connectionAdapter) {
        this.connectionAdapter = connectionAdapter;
        conductorState = CONDUCTOR_STATE.HANDSHAKING;
        hsPool = Executors.newFixedThreadPool(1);
        handshakeManager = new HandshakeManagerImpl(connectionAdapter,
                ConnectionConductor.versionOrder.get(0), ConnectionConductor.versionOrder);
        handshakeManager.setUseVersionBitmap(isBitmapNegotiationEnable);
        handshakeManager.setHandshakeListener(this);
    }

    @Override
    public void init() {
        connectionAdapter.setMessageListener(this);
        connectionAdapter.setSystemListener(this);
        connectionAdapter.setConnectionReadyListener(this);
    }

    /**
     * @param queueKeeper the queueKeeper to set
     */
    @Override
    public void setQueueKeeper(QueueKeeper<OfHeader, DataObject> queueKeeper) {
        this.queueKeeper = queueKeeper;
    }

    /**
     * @param errorHandler the errorHandler to set
     */
    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        handshakeManager.setErrorHandler(errorHandler);
    }

    @Override
    public void onEchoRequestMessage(final EchoRequestMessage echoRequestMessage) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.debug("echo request received: " + echoRequestMessage.getXid());
                EchoReplyInputBuilder builder = new EchoReplyInputBuilder();
                builder.setVersion(echoRequestMessage.getVersion());
                builder.setXid(echoRequestMessage.getXid());
                builder.setData(echoRequestMessage.getData());

                getConnectionAdapter().echoReply(builder.build());
            }
        }).start();
    }

    @Override
    public void onErrorMessage(ErrorMessage errorMessage) {
        queueKeeper.push(ErrorMessage.class, errorMessage, this);
    }

    @Override
    public void onExperimenterMessage(ExperimenterMessage experimenterMessage) {
        queueKeeper.push(ExperimenterMessage.class, experimenterMessage, this);
    }

    @Override
    public void onFlowRemovedMessage(FlowRemovedMessage message) {
        queueKeeper.push(FlowRemovedMessage.class, message, this);
    }


    /**
     * version negotiation happened as per following steps:
     * 1. If HelloMessage version field has same version, continue connection processing.
     *    If HelloMessage version is lower than supported versions, just disconnect.
     * 2. If HelloMessage contains bitmap and common version found in bitmap
     *    then continue connection processing. if no common version found, just disconnect.
     * 3. If HelloMessage version is not supported, send HelloMessage with lower supported version.
     * 4. If Hello message received again with not supported version, just disconnect.
     *
     *   TODO: Better to handle handshake into a maintainable innerclass which uses State-Pattern.
     */
    @Override
    public synchronized void onHelloMessage(final HelloMessage hello) {
        LOG.debug("processing HELLO.xid{}", hello.getXid());
        checkState(CONDUCTOR_STATE.HANDSHAKING);
        handshakeManager.setReceivedHello(hello);
        hsPool.execute(handshakeManager);
    }

    /**
     * @return rpc-response timeout in [ms]
     */
    protected long getMaxTimeout() {
        // TODO:: get from configuration
        return 2000;
    }

    /**
     * @return milliseconds
     */
    protected TimeUnit getMaxTimeoutUnit() {
        // TODO:: get from configuration
        return TimeUnit.MILLISECONDS;
    }

    @Override
    public void onMultipartReplyMessage(MultipartReplyMessage message) {
        queueKeeper.push(MultipartReplyMessage.class, message, this);
    }

    @Override
    public void onMultipartRequestMessage(MultipartRequestMessage message) {
        queueKeeper.push(MultipartRequestMessage.class, message, this);
    }

    @Override
    public void onPacketInMessage(PacketInMessage message) {
        LOG.info("PacketIn: InPort: {} Cookie: {} Match.type: {}",
                 message.getInPort(), message.getCookie(),
                 message.getMatch() != null ? message.getMatch().getType()
                                           : message.getMatch());

        // create a packet received event builder
        PacketReceivedBuilder pktInBuilder = new PacketReceivedBuilder();
        pktInBuilder.setPayload(message.getData());

        // get the DPID
        GetFeaturesOutput features = sessionContext.getFeatures();
        BigInteger dpid = features.getDatapathId();

        // get the Cookie if it exists
        if(message.getCookie() != null) {
            pktInBuilder.setCookie(new Cookie(message.getCookie().longValue()));
        }

        // extract the port number
        Long port = null;

        if (message.getInPort() != null) {
            // this doesn't work--at least for OF1.3
            port = message.getInPort().longValue();
        }

        // this should work for OF1.3
        if (message.getMatch() != null && message.getMatch().getMatchEntries() != null) {
            List<MatchEntries> entries = message.getMatch().getMatchEntries();
            for (MatchEntries entry : entries) {
                PortNumberMatchEntry tmp = entry.getAugmentation(PortNumberMatchEntry.class);
                if (tmp != null) {
                    if (port == null) {
                        port = tmp.getPortNumber().getValue();
                    } else {
                        LOG.warn("Multiple input ports (at least {} and {})",
                                 port, tmp.getPortNumber().getValue());
                    }
                }
            }
        }

        if (port == null) {
            // no incoming port, so drop the event
            LOG.warn("Received packet_in, but couldn't find an input port");
            return;
        }else{
            LOG.info("Receive packet_in from {} on port {}", dpid, port);
        }

        //TODO: need to get the NodeConnectorRef, but NodeConnectors aren't there yet
        InstanceIdentifier<NodeConnector> nci = ncIndentifierFromDPIDandPort(dpid, port);
        NodeConnectorRef ncr = new NodeConnectorRef(nci);
        PacketReceived pktInEvent = pktInBuilder.build();

        // allow others to process this message as well
        queueKeeper.push(PacketInMessage.class, message, this);
    }

    public static InstanceIdentifier<NodeConnector> ncIndentifierFromDPIDandPort(BigInteger dpid, Long port) {
        InstanceIdentifierBuilder<?> builder = InstanceIdentifier.builder().node(Node.class);

        // TODO: this doesn't work yet, needs to actaully get the ref for the real NodeConnector
        //       but that doesn't exist yet
        NodeConnectorKey ncKey = ncKeyFromDPIDandPort(dpid, port);
        return builder.node(NodeConnector.class, ncKey).toInstance();
    }


    public static NodeConnectorKey ncKeyFromDPIDandPort(BigInteger dpid, Long port){
        return new NodeConnectorKey(ncIDfromDPIDandPort(dpid, port));
    }

    public static NodeConnectorId ncIDfromDPIDandPort(BigInteger dpid, Long port){
        return new NodeConnectorId("openflow:"+dpid.toString()+":"+port.toString());
    }

    @Override
    public void onPortStatusMessage(PortStatusMessage message) {
        // update the list of ports
        this.getSessionContext().processPortStatusMsg(message);

        // do any more processing that needs to be done
        queueKeeper.push(PortStatusMessage.class, message, this);
    }

    @Override
    public void onSwitchIdleEvent(SwitchIdleEvent notification) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!CONDUCTOR_STATE.WORKING.equals(getConductorState())) {
                    // idle state in any other conductorState than WORKING means real
                    // problem and wont be handled by echoReply, but disconnection
                    disconnect();
                    OFSessionUtil.getSessionManager().invalidateOnDisconnect(ConnectionConductorImpl.this);
                } else {
                    LOG.debug("first idle state occured");
                    EchoInputBuilder builder = new EchoInputBuilder();
                    builder.setVersion(getVersion());
                    builder.setXid(getSessionContext().getNextXid());

                    Future<RpcResult<EchoOutput>> echoReplyFuture = getConnectionAdapter()
                            .echo(builder.build());

                    try {
                        RpcResult<EchoOutput> echoReplyValue = echoReplyFuture.get(getMaxTimeout(),
                                getMaxTimeoutUnit());
                        if (echoReplyValue.isSuccessful()) {
                            setConductorState(CONDUCTOR_STATE.WORKING);
                        } else {
                            for (RpcError replyError : echoReplyValue.getErrors()) {
                                Throwable cause = replyError.getCause();
                                LOG.error(
                                        "while receiving echoReply in TIMEOUTING state: "
                                                + cause.getMessage(), cause);
                            }
                            //switch issue occurred
                            throw new Exception("switch issue occurred");
                        }
                    } catch (Exception e) {
                        LOG.error("while waiting for echoReply in TIMEOUTING state: "
                                + e.getMessage(), e);
                        //switch is not responding
                        disconnect();
                        OFSessionUtil.getSessionManager().invalidateOnDisconnect(ConnectionConductorImpl.this);
                    }
                }
            }

        }).start();
    }

    /**
     * @param conductorState
     *            the connectionState to set
     */
    @Override
    public void setConductorState(CONDUCTOR_STATE conductorState) {
        this.conductorState = conductorState;
    }

    @Override
    public CONDUCTOR_STATE getConductorState() {
        return conductorState;
    }

    /**
     * @param handshaking
     */
    protected void checkState(CONDUCTOR_STATE expectedState) {
        if (!conductorState.equals(expectedState)) {
            throw new IllegalStateException("Expected state: " + expectedState
                    + ", actual state:" + conductorState);
        }
    }

    @Override
    public void onDisconnectEvent(DisconnectEvent arg0) {
        SessionManager sessionManager = OFSessionUtil.getSessionManager();
        sessionManager.invalidateOnDisconnect(this);
    }

    @Override
    public Short getVersion() {
        return version;
    }

    @Override
    public Future<Boolean> disconnect() {
        LOG.info("disconnecting: sessionCtx="+sessionContext+"|auxId="+auxiliaryKey);

        Future<Boolean> result = null;
        if (connectionAdapter.isAlive()) {
            result = connectionAdapter.disconnect();
        } else {
            LOG.debug("connection already disconnected");
            result = Futures.immediateFuture(true);
        }

        return result;
    }

    @Override
    public void setConnectionCookie(SwitchConnectionDistinguisher auxiliaryKey) {
        this.auxiliaryKey = auxiliaryKey;
    }

    @Override
    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    @Override
    public SwitchConnectionDistinguisher getAuxiliaryKey() {
        return auxiliaryKey;
    }

    @Override
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    @Override
    public ConnectionAdapter getConnectionAdapter() {
        return connectionAdapter;
    }

    @Override
    public void onConnectionReady() {
        LOG.debug("connection is ready-to-use");
        hsPool.execute(handshakeManager);
    }

    @Override
    public void onHandshakeSuccessfull(GetFeaturesOutput featureOutput,
            Short negotiatedVersion) {
        version = negotiatedVersion;
        conductorState = CONDUCTOR_STATE.WORKING;


        OFSessionUtil.registerSession(this, featureOutput, negotiatedVersion);
    }

    /**
     * @param isBitmapNegotiationEnable the isBitmapNegotiationEnable to set
     */
    public void setBitmapNegotiationEnable(
            boolean isBitmapNegotiationEnable) {
        this.isBitmapNegotiationEnable = isBitmapNegotiationEnable;
    }

    protected void shutdownPool() {
        hsPool.shutdownNow();
        LOG.debug("pool is terminated: {}", hsPool.isTerminated());
    }

}
