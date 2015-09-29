/**
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.ErrorHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeListener;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeManager;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationEnqueuer;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationQueueWrapper;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionManager;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueKeeper.QueueType;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueProcessor;
import org.opendaylight.openflowplugin.api.openflow.md.queue.WaterMarkListener;
import org.opendaylight.openflowplugin.api.openflow.md.queue.WaterMarkListenerImpl;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.PortFeaturesUtil;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueKeeperFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
public class ConnectionConductorImpl implements OpenflowProtocolListener,
        SystemNotificationsListener, ConnectionConductor,
        ConnectionReadyListener, HandshakeListener, NotificationEnqueuer,
        AutoCloseable {

    /**
     * ingress queue limit
     */
    private static final int INGRESS_QUEUE_MAX_SIZE = 200;

    protected static final Logger LOG = LoggerFactory
            .getLogger(ConnectionConductorImpl.class);

    /*
     * variable to make BitMap-based negotiation enabled / disabled. it will
     * help while testing and isolating issues related to processing of BitMaps
     * from switches.
     */
    private boolean isBitmapNegotiationEnable = true;
    protected ErrorHandler errorHandler;

    private final ConnectionAdapter connectionAdapter;
    private ConnectionConductor.CONDUCTOR_STATE conductorState;
    private Short version;

    protected SwitchConnectionDistinguisher auxiliaryKey;

    protected SessionContext sessionContext;

    private QueueProcessor<OfHeader, DataObject> queueProcessor;
    private QueueKeeper<OfHeader> queue;
    private ThreadPoolExecutor hsPool;
    private HandshakeManager handshakeManager;

    private boolean firstHelloProcessed;

    private PortFeaturesUtil portFeaturesUtils;

    private int conductorId;

    private int ingressMaxQueueSize;
    private HandshakeContext handshakeContext;

    /**
     * @param connectionAdapter connection adaptor for switch
     */
    public ConnectionConductorImpl(ConnectionAdapter connectionAdapter) {
        this(connectionAdapter, INGRESS_QUEUE_MAX_SIZE);
    }

    /**
     * @param connectionAdapter connection adaptor for switch
     * @param ingressMaxQueueSize ingress queue limit (blocking)
     */
    public ConnectionConductorImpl(ConnectionAdapter connectionAdapter,
                                   int ingressMaxQueueSize) {
        this.connectionAdapter = connectionAdapter;
        this.ingressMaxQueueSize = ingressMaxQueueSize;
        conductorState = CONDUCTOR_STATE.HANDSHAKING;
        firstHelloProcessed = false;
        handshakeManager = new HandshakeManagerImpl(connectionAdapter,
                ConnectionConductor.versionOrder.get(0),
                ConnectionConductor.versionOrder);
        handshakeManager.setUseVersionBitmap(isBitmapNegotiationEnable);
        handshakeManager.setHandshakeListener(this);
        portFeaturesUtils = PortFeaturesUtil.getInstance();
    }

    @Override
    public void init() {
        int handshakeThreadLimit = 1;
        hsPool = new ThreadPoolLoggingExecutor(handshakeThreadLimit,
                handshakeThreadLimit, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), "OFHandshake-"
                + conductorId);

        connectionAdapter.setMessageListener(this);
        connectionAdapter.setSystemListener(this);
        connectionAdapter.setConnectionReadyListener(this);
        WaterMarkListener waterMarkListener = new WaterMarkListenerImpl(
                connectionAdapter);
        queue = QueueKeeperFactory.createFairQueueKeeper(queueProcessor,
                ingressMaxQueueSize, waterMarkListener);
    }

    @Override
    public void setQueueProcessor(
            QueueProcessor<OfHeader, DataObject> queueProcessor) {
        this.queueProcessor = queueProcessor;
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
                LOG.debug("echo request received: "
                        + echoRequestMessage.getXid());
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
        enqueueMessage(errorMessage);
    }

    /**
     * @param message
     */
    private void enqueueMessage(OfHeader message) {
        enqueueMessage(message, QueueType.DEFAULT);
    }

    @Override
    public void enqueueNotification(NotificationQueueWrapper notification) {
        enqueueMessage(notification);
    }

    /**
     * @param message
     * @param queueType enqueue type
     */
    private void enqueueMessage(OfHeader message, QueueType queueType) {
        queue.push(message, this, queueType);
    }

    @Override
    public void onExperimenterMessage(ExperimenterMessage experimenterMessage) {
        enqueueMessage(experimenterMessage);
    }

    @Override
    public void onFlowRemovedMessage(FlowRemovedMessage message) {
        enqueueMessage(message);
    }

    /**
     * version negotiation happened as per following steps: 1. If HelloMessage
     * version field has same version, continue connection processing. If
     * HelloMessage version is lower than supported versions, just disconnect.
     * 2. If HelloMessage contains bitmap and common version found in bitmap
     * then continue connection processing. if no common version found, just
     * disconnect. 3. If HelloMessage version is not supported, send
     * HelloMessage with lower supported version. 4. If Hello message received
     * again with not supported version, just disconnect.
     */
    @Override
    public void onHelloMessage(final HelloMessage hello) {
        LOG.debug("processing HELLO.xid: {}", hello.getXid());
        firstHelloProcessed = true;
        checkState(CONDUCTOR_STATE.HANDSHAKING);
        HandshakeStepWrapper handshakeStepWrapper = new HandshakeStepWrapper(
                hello, handshakeManager, connectionAdapter);
        hsPool.submit(handshakeStepWrapper);
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
        enqueueMessage(message);
    }

    @Override
    public void onPacketInMessage(PacketInMessage message) {
        enqueueMessage(message, QueueKeeper.QueueType.UNORDERED);
    }

    @Override
    public void onPortStatusMessage(PortStatusMessage message) {
        try {
            processPortStatusMsg(message);
        } finally {
            enqueueMessage(message);
        }
    }

    protected void processPortStatusMsg(PortStatus msg) {
        if (msg.getReason().getIntValue() == 2) {
            updatePort(msg);
        } else if (msg.getReason().getIntValue() == 0) {
            updatePort(msg);
        } else if (msg.getReason().getIntValue() == 1) {
            deletePort(msg);
        }
    }

    protected void updatePort(PortStatus msg) {
        Long portNumber = msg.getPortNo();
        Boolean portBandwidth = portFeaturesUtils.getPortBandwidth(msg);

        if (portBandwidth == null) {
            LOG.debug(
                    "can't get bandwidth info from port: {}, aborting port update",
                    msg.toString());
        } else {
            if (null != this.sessionContext) {
                //FIXME these two properties are never used in code
                this.getSessionContext().getPhysicalPorts().put(portNumber, msg);
                this.getSessionContext().getPortsBandwidth()
                        .put(portNumber, portBandwidth);
            } else {
                LOG.warn("Trying to process update port message before session context was created.");
            }
        }
    }

    protected void deletePort(PortGrouping port) {
        Long portNumber = port.getPortNo();

        this.getSessionContext().getPhysicalPorts().remove(portNumber);
        this.getSessionContext().getPortsBandwidth().remove(portNumber);
    }

    @Override
    public void onSwitchIdleEvent(SwitchIdleEvent notification) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!CONDUCTOR_STATE.WORKING.equals(getConductorState())) {
                    // idle state in any other conductorState than WORKING means
                    // real
                    // problem and wont be handled by echoReply, but
                    // disconnection
                    disconnect();
                    OFSessionUtil.getSessionManager().invalidateOnDisconnect(
                            ConnectionConductorImpl.this);
                } else {
                    LOG.debug(
                            "first idle state occured, sessionCtx={}|auxId={}",
                            sessionContext, auxiliaryKey);
                    EchoInputBuilder builder = new EchoInputBuilder();
                    builder.setVersion(getVersion());
                    builder.setXid(getSessionContext().getNextXid());

                    Future<RpcResult<EchoOutput>> echoReplyFuture = getConnectionAdapter()
                            .echo(builder.build());

                    try {
                        RpcResult<EchoOutput> echoReplyValue = echoReplyFuture
                                .get(getMaxTimeout(), getMaxTimeoutUnit());
                        if (echoReplyValue.isSuccessful()) {
                            setConductorState(CONDUCTOR_STATE.WORKING);
                        } else {
                            for (RpcError replyError : echoReplyValue
                                    .getErrors()) {
                                Throwable cause = replyError.getCause();
                                LOG.error(
                                        "while receiving echoReply in TIMEOUTING state: "
                                                + cause.getMessage(), cause);
                            }
                            // switch issue occurred
                            throw new Exception("switch issue occurred");
                        }
                    } catch (Exception e) {
                        LOG.error("while waiting for echoReply in TIMEOUTING state: "
                                + e.getMessage());
                        errorHandler.handleException(e, sessionContext);
                        // switch is not responding
                        disconnect();
                        OFSessionUtil.getSessionManager()
                                .invalidateOnDisconnect(
                                        ConnectionConductorImpl.this);
                    }
                }
            }

        }).start();
    }

    /**
     * @param conductorState the connectionState to set
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
     * @param expectedState connection conductor state
     */
    protected void checkState(CONDUCTOR_STATE expectedState) {
        if (!conductorState.equals(expectedState)) {
            LOG.warn("State of connection to switch {} is not correct, "
                    + "terminating the connection", connectionAdapter.getRemoteAddress());
            throw new IllegalStateException("Expected state: " + expectedState
                    + ", actual state:" + conductorState);
        }
    }

    @Override
    public void onDisconnectEvent(DisconnectEvent arg0) {
        SessionManager sessionManager = OFSessionUtil.getSessionManager();
        sessionManager.invalidateOnDisconnect(this);
        close();
    }

    @Override
    public Short getVersion() {
        return version;
    }

    @Override
    public Future<Boolean> disconnect() {
        LOG.trace("disconnecting: sessionCtx={}|auxId={}", sessionContext,
                auxiliaryKey);

        Future<Boolean> result = null;
        if (connectionAdapter.isAlive()) {
            result = connectionAdapter.disconnect();
        } else {
            LOG.debug("connection already disconnected");
            result = Futures.immediateFuture(true);
        }
        close();
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
        if (!firstHelloProcessed) {
            checkState(CONDUCTOR_STATE.HANDSHAKING);
            HandshakeStepWrapper handshakeStepWrapper = new HandshakeStepWrapper(
                    null, handshakeManager, connectionAdapter);
            hsPool.execute(handshakeStepWrapper);
            firstHelloProcessed = true;
        } else {
            LOG.debug("already touched by hello message");
        }
    }

    @Override
    public void onHandshakeSuccessfull(GetFeaturesOutput featureOutput,
                                       Short negotiatedVersion) {
        postHandshakeBasic(featureOutput, negotiatedVersion);

        // post-handshake actions
        if (version == OFConstants.OFP_VERSION_1_3) {
            requestPorts();
        }

        requestDesc();
    }

    @Override
    public void onHandshakeFailure() {
        LOG.info("OF handshake failed, doing cleanup.");
        close();
    }

    /**
     * used by tests
     *
     * @param featureOutput feature request output
     * @param negotiatedVersion negotiated openflow connection version
     */
    protected void postHandshakeBasic(GetFeaturesOutput featureOutput,
                                      Short negotiatedVersion) {
        version = negotiatedVersion;
        if (version == OFConstants.OFP_VERSION_1_0) {
            // Because the GetFeaturesOutput contains information about the port
            // in OF1.0 (that we would otherwise get from the PortDesc) we have
            // to pass
            // it up for parsing to convert into a NodeConnectorUpdate
            //
            // BUG-1988 - this must be the first item in queue in order not to
            // get behind link-up message
            enqueueMessage(featureOutput);
        }

        SessionContext sessionContext =  OFSessionUtil.registerSession(this, featureOutput, negotiatedVersion);
        hsPool.shutdown();
        hsPool.purge();
        conductorState = CONDUCTOR_STATE.WORKING;
        QueueKeeperFactory.plugQueue(queueProcessor, queue);
	OFSessionUtil.setRole(sessionContext);
    }

    /*
     * Send an OFPMP_DESC request message to the switch
     */
    private void requestDesc() {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        builder.setType(MultipartType.OFPMPDESC);
        builder.setVersion(getVersion());
        builder.setFlags(new MultipartRequestFlags(false));
        builder.setMultipartRequestBody(new MultipartRequestDescCaseBuilder()
                .build());
        builder.setXid(getSessionContext().getNextXid());
        getConnectionAdapter().multipartRequest(builder.build());
    }

    private void requestPorts() {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        builder.setType(MultipartType.OFPMPPORTDESC);
        builder.setVersion(getVersion());
        builder.setFlags(new MultipartRequestFlags(false));
        builder.setMultipartRequestBody(new MultipartRequestPortDescCaseBuilder()
                .build());
        builder.setXid(getSessionContext().getNextXid());
        getConnectionAdapter().multipartRequest(builder.build());
    }

    private void requestGroupFeatures() {
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPGROUPFEATURES);
        mprInput.setVersion(getVersion());
        mprInput.setFlags(new MultipartRequestFlags(false));
        mprInput.setXid(getSessionContext().getNextXid());

        MultipartRequestGroupFeaturesCaseBuilder mprGroupFeaturesBuild = new MultipartRequestGroupFeaturesCaseBuilder();
        mprInput.setMultipartRequestBody(mprGroupFeaturesBuild.build());

        LOG.debug("Send group features statistics request :{}",
                mprGroupFeaturesBuild);
        getConnectionAdapter().multipartRequest(mprInput.build());

    }

    private void requestMeterFeatures() {
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPMETERFEATURES);
        mprInput.setVersion(getVersion());
        mprInput.setFlags(new MultipartRequestFlags(false));
        mprInput.setXid(getSessionContext().getNextXid());

        MultipartRequestMeterFeaturesCaseBuilder mprMeterFeaturesBuild = new MultipartRequestMeterFeaturesCaseBuilder();
        mprInput.setMultipartRequestBody(mprMeterFeaturesBuild.build());

        LOG.debug("Send meter features statistics request :{}",
                mprMeterFeaturesBuild);
        getConnectionAdapter().multipartRequest(mprInput.build());

    }

    /**
     * @param isBitmapNegotiationEnable the isBitmapNegotiationEnable to set
     */
    public void setBitmapNegotiationEnable(boolean isBitmapNegotiationEnable) {
        this.isBitmapNegotiationEnable = isBitmapNegotiationEnable;
    }

    @Override
    public void setId(int conductorId) {
        this.conductorId = conductorId;
    }

    @Override
    public void close() {
        conductorState = CONDUCTOR_STATE.RIP;
        if (handshakeContext != null) {
            try {
                handshakeContext.close();
            } catch (Exception e) {
                LOG.warn("Closing handshake context failed: {}", e.getMessage());
                LOG.debug("Detail in hanshake context close:", e);
            }
        } else {
            //This condition will occure when Old Helium openflowplugin implementation will be used.
            shutdownPoolPolitely();
        }
    }

    private void shutdownPoolPolitely() {
        LOG.debug("Terminating handshake pool for node {}", connectionAdapter.getRemoteAddress());
        hsPool.shutdown();
        try {
            hsPool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.debug("Error while awaiting termination of pool. Will force shutdown now.");
        } finally {
            hsPool.purge();
            if (!hsPool.isTerminated()) {
                hsPool.shutdownNow();
            }
            LOG.debug("is handshake pool for node {} is terminated : {}",
                    connectionAdapter.getRemoteAddress(), hsPool.isTerminated());
        }
    }

    @Override
    public void setHandshakeContext(HandshakeContext handshakeContext) {
        this.handshakeContext = handshakeContext;
    }

    @VisibleForTesting
    ThreadPoolExecutor getHsPool() {
        return hsPool;
    }
}
