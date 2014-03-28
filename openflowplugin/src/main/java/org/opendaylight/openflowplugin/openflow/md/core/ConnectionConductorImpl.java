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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.PortFeaturesUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionManager;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueKeeper;
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
    private boolean isBitmapNegotiationEnable = true;
    protected ErrorHandler errorHandler;

    private final ConnectionAdapter connectionAdapter;
    private ConnectionConductor.CONDUCTOR_STATE conductorState;
    private Short version;

    protected SwitchConnectionDistinguisher auxiliaryKey;

    protected SessionContext sessionContext;

    private QueueKeeper<OfHeader, DataObject> queueKeeper;
    private ExecutorService hsPool;
    private HandshakeManager handshakeManager;

    private boolean firstHelloProcessed;
    
    private PortFeaturesUtil portFeaturesUtils;

    /**
     * @param connectionAdapter
     */
    public ConnectionConductorImpl(ConnectionAdapter connectionAdapter) {
        this.connectionAdapter = connectionAdapter;
        conductorState = CONDUCTOR_STATE.HANDSHAKING;
        hsPool = Executors.newFixedThreadPool(1);
        firstHelloProcessed = false;
        handshakeManager = new HandshakeManagerImpl(connectionAdapter,
                ConnectionConductor.versionOrder.get(0), ConnectionConductor.versionOrder);
        handshakeManager.setUseVersionBitmap(isBitmapNegotiationEnable);
        handshakeManager.setHandshakeListener(this);
        portFeaturesUtils = PortFeaturesUtil.getInstance();
    }

    @Override
    public void init() {
        connectionAdapter.setMessageListener(this);
        connectionAdapter.setSystemListener(this);
        connectionAdapter.setConnectionReadyListener(this);
    }

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
        queueKeeper.push(errorMessage, this);
    }

    @Override
    public void onExperimenterMessage(ExperimenterMessage experimenterMessage) {
        queueKeeper.push(experimenterMessage, this);
    }

    @Override
    public void onFlowRemovedMessage(FlowRemovedMessage message) {
        queueKeeper.push(message, this);
    }


    /**
     * version negotiation happened as per following steps:
     * 1. If HelloMessage version field has same version, continue connection processing.
     *    If HelloMessage version is lower than supported versions, just disconnect.
     * 2. If HelloMessage contains bitmap and common version found in bitmap
     *    then continue connection processing. if no common version found, just disconnect.
     * 3. If HelloMessage version is not supported, send HelloMessage with lower supported version.
     * 4. If Hello message received again with not supported version, just disconnect.
     */
    @Override
    public synchronized void onHelloMessage(final HelloMessage hello) {
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
        queueKeeper.push(message, this);
    }

    @Override
    public void onPacketInMessage(PacketInMessage message) {
        queueKeeper.push(message, this, QueueKeeper.QueueType.UNORDERED);
    }

    @Override
    public void onPortStatusMessage(PortStatusMessage message) {
        processPortStatusMsg(message);
        queueKeeper.push(message, this);
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
        
        if(portBandwidth == null) {
            LOG.debug("can't get bandwidth info from port: {}, aborting port update", msg.toString());
        } else {
            this.getSessionContext().getPhysicalPorts().put(portNumber, msg);
            this.getSessionContext().getPortsBandwidth().put(portNumber, portBandwidth);                   
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
                    // idle state in any other conductorState than WORKING means real
                    // problem and wont be handled by echoReply, but disconnection
                    disconnect();
                    OFSessionUtil.getSessionManager().invalidateOnDisconnect(ConnectionConductorImpl.this);
                } else {
                    LOG.debug("first idle state occured, sessionCtx={}|auxId={}", sessionContext, auxiliaryKey);
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
                                + e.getMessage());
                        errorHandler.handleException(e, sessionContext);
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
        LOG.trace("disconnecting: sessionCtx={}|auxId={}", sessionContext, auxiliaryKey);

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
    public synchronized void onConnectionReady() {
        LOG.debug("connection is ready-to-use");
        if (! firstHelloProcessed) {
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
        if(version == OFConstants.OFP_VERSION_1_3){
            requestGroupFeatures();
            requestMeterFeatures();
        } else if (version == OFConstants.OFP_VERSION_1_0) {
            //  Because the GetFeaturesOutput contains information about the port
            //  in OF1.0 (that we would otherwise get from the PortDesc) we have to pass
            //  it up for parsing to convert into a NodeConnectorUpdate
            queueKeeper.push(featureOutput, this);
        }
        
        requestDesc();
        requestPorts();
    }

    /**
     * used by tests
     * @param featureOutput
     * @param negotiatedVersion
     */
    protected void postHandshakeBasic(GetFeaturesOutput featureOutput,
            Short negotiatedVersion) {
        version = negotiatedVersion;
        conductorState = CONDUCTOR_STATE.WORKING;
        OFSessionUtil.registerSession(this, featureOutput, negotiatedVersion);
        hsPool.shutdownNow();
    }

    /*
     *  Send an OFPMP_DESC request message to the switch
     */

    private void requestDesc() {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        builder.setType(MultipartType.OFPMPDESC);
        builder.setVersion(getVersion());
        builder.setFlags(new MultipartRequestFlags(false));
        builder.setMultipartRequestBody(new MultipartRequestDescCaseBuilder().build());
        builder.setXid(getSessionContext().getNextXid());
        getConnectionAdapter().multipartRequest(builder.build());
    }

    private void requestPorts() {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        builder.setType(MultipartType.OFPMPPORTDESC);
        builder.setVersion(getVersion());
        builder.setFlags(new MultipartRequestFlags(false));
        builder.setMultipartRequestBody(new MultipartRequestPortDescCaseBuilder().build());
        builder.setXid(getSessionContext().getNextXid());
        getConnectionAdapter().multipartRequest(builder.build());
    }
    private void requestGroupFeatures(){
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPGROUPFEATURES);
        mprInput.setVersion(getVersion());
        mprInput.setFlags(new MultipartRequestFlags(false));
        mprInput.setXid(getSessionContext().getNextXid());

        MultipartRequestGroupFeaturesCaseBuilder mprGroupFeaturesBuild = 
                new MultipartRequestGroupFeaturesCaseBuilder();
        mprInput.setMultipartRequestBody(mprGroupFeaturesBuild.build());

        LOG.debug("Send group features statistics request :{}",mprGroupFeaturesBuild);
        getConnectionAdapter().multipartRequest(mprInput.build());
        
    }
    private void requestMeterFeatures(){
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(MultipartType.OFPMPMETERFEATURES);
        mprInput.setVersion(getVersion());
        mprInput.setFlags(new MultipartRequestFlags(false));
        mprInput.setXid(getSessionContext().getNextXid());

        MultipartRequestMeterFeaturesCaseBuilder mprMeterFeaturesBuild =
                new MultipartRequestMeterFeaturesCaseBuilder();
        mprInput.setMultipartRequestBody(mprMeterFeaturesBuild.build());

        LOG.debug("Send meter features statistics request :{}",mprMeterFeaturesBuild);
        getConnectionAdapter().multipartRequest(mprInput.build());
        
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
