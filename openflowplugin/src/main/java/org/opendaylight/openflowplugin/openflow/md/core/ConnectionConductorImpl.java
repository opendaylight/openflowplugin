/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionManager;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueKeeper;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueKeeperLightImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
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
        SystemNotificationsListener, ConnectionConductor, ConnectionReadyListener {

    protected static final Logger LOG = LoggerFactory
            .getLogger(ConnectionConductorImpl.class);

    /* variable to make BitMap-based negotiation enabled / disabled.
     * it will help while testing and isolating issues related to processing of
     * BitMaps from switches.
     */
    private static final boolean isBitmapNegotiationEnable = true;
    private LinkedBlockingQueue<Exception> errorQueue = new LinkedBlockingQueue<>();

    protected final ConnectionAdapter connectionAdapter;
    private ConnectionConductor.CONDUCTOR_STATE conductorState;
    private Short version;

    private SwitchConnectionDistinguisher auxiliaryKey;

    private SessionContext sessionContext;

    private Map<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping;

    protected boolean isFirstHelloNegotiation = true;

    // TODO: use appropriate interface instead of Object
    private QueueKeeper<Object> queueKeeper;



    /**
     * @param connectionAdapter
     */
    public ConnectionConductorImpl(ConnectionAdapter connectionAdapter) {
        this.connectionAdapter = connectionAdapter;
        conductorState = CONDUCTOR_STATE.HANDSHAKING;
        new Thread(new ErrorQueueHandler(errorQueue)).start();
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
    public void setQueueKeeper(QueueKeeper<Object> queueKeeper) {
        this.queueKeeper = queueKeeper;
    }


    /**
     * send first hello message to switch
     */
    protected void sendFirstHelloMessage() {
        Short highestVersion = ConnectionConductor.versionOrder.get(0);
        Long helloXid = 21L;
        HelloInput helloInput = null;
        
        if (isBitmapNegotiationEnable) {
            helloInput = MessageFactory.createHelloInput(highestVersion, helloXid, ConnectionConductor.versionOrder);
            LOG.debug("sending first hello message: vertsion header={} , version bitmap={}", 
                    highestVersion, helloInput.getElements());
        } else {
            helloInput = MessageFactory.createHelloInput(highestVersion, helloXid);
            LOG.debug("sending first hello message: version header={} ", highestVersion);
        }
        
        try {
            RpcResult<Void> helloResult = connectionAdapter.hello(helloInput).get(getMaxTimeout(), getMaxTimeoutUnit());
            smokeRpc(helloResult);
            LOG.debug("FIRST HELLO sent.");
        } catch (Throwable e) {
            LOG.debug("FIRST HELLO sending failed.");
            handleException(e);
        }
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
                
                connectionAdapter.echoReply(builder.build());
            }
        }).start();            
    }

    @Override
    public void onErrorMessage(ErrorMessage errorMessage) {
        // TODO Auto-generated method stub
        LOG.debug("error received, type: " + errorMessage.getType()
                + "; code: " + errorMessage.getCode());
    }

    @Override
    public void onExperimenterMessage(ExperimenterMessage experimenterMessage) {
        queueKeeper.push(ExperimenterMessage.class, experimenterMessage, this);
//        notifyListeners(ExperimenterMessage.class, experimenterMessage);
    }

    @Override
    public void onFlowRemovedMessage(FlowRemovedMessage message) {
        notifyListeners(FlowRemovedMessage.class, message);
    }


    /**
     * version negotiation happened as per following steps:
     * 1. If HelloMessage version field has same version, continue connection processing.
     *    If HelloMessage version is lower than supported versions, just disconnect.
     * 2. If HelloMessage contains bitmap and common version found in bitmap
     *    then continue connection processing. if no common version found, just disconnect.
     * 3. If HelloMessage version is not supported, send HelloMessage with lower supported version.
     *    If Hello message received again with not supported version, just disconnect.
     *
     *   TODO: Better to handle handshake into a maintainable innerclass which uses State-Pattern.
     */
    @Override
    public void onHelloMessage(final HelloMessage hello) {
        // do handshake

        new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("handshake STARTED");
                checkState(CONDUCTOR_STATE.HANDSHAKING);
                
                Short remoteVersion = hello.getVersion();
                List<Elements> elements = hello.getElements();
                Long xid = hello.getXid();
                Short proposedVersion;
                LOG.debug("Hello message version={} and bitmap={}", remoteVersion, elements);
                try {
                    // find the version from header version field
                    proposedVersion = proposeVersion(remoteVersion);
                    
                } catch (IllegalArgumentException e) {
                    handleException(e);
                    connectionAdapter.disconnect();
                    return;
                }
                
                // sent version is equal to remote --> version is negotiated
                if (proposedVersion == remoteVersion) {
                    LOG.debug("sending helloReply as version in header is supported: {}", proposedVersion);
                    sendHelloReply(proposedVersion, ++xid);
                    postHandshake(proposedVersion, ++xid);
                    
                } else if (isBitmapNegotiationEnable && null != elements && 0 != elements.size()) {
                    try {
                        // hello contains version bitmap, checking highest common
                        // version in bitmap
                        proposedVersion = proposeBitmapVersion(elements);
                    } catch (IllegalArgumentException ex) {
                        handleException(ex);
                        connectionAdapter.disconnect();
                        return;
                    }
                    LOG.debug("sending helloReply for common bitmap version : {}", proposedVersion);
                    sendHelloReply(proposedVersion, ++xid);
                    postHandshake(proposedVersion, ++xid);
                } else {
                    if (isFirstHelloNegotiation) {
                        isFirstHelloNegotiation = false;
                        LOG.debug("sending helloReply for lowest supported version : {}", proposedVersion);
                        // send hello reply with lower version number supported
                        sendHelloReply(proposedVersion, ++xid);
                    } else {
                        // terminate the connection.
                        LOG.debug("Version negotiation failed. unsupported version : {}", remoteVersion);
                        connectionAdapter.disconnect();
                    }
                }
            }
        }).start();
    }

    /**
     * send hello reply
     * @param proposedVersion
     * @param hello
     */
    protected void sendHelloReply(Short proposedVersion, Long xid)
    {
        HelloInput helloMsg = MessageFactory.createHelloInput(proposedVersion, xid);
        RpcResult<Void> result;
        try {
            result = connectionAdapter.hello(helloMsg).get(getMaxTimeout(), getMaxTimeoutUnit());
            smokeRpc(result);
        } catch (Throwable e) {
            handleException(e);
        }
    }


    /**
     * @param futureResult
     * @throws Throwable 
     */
    private static void smokeRpc(RpcResult<?> result) throws Throwable {
        if (!result.isSuccessful()) {
            Throwable firstCause = null;
            StringBuffer sb = new StringBuffer();
            for (RpcError error : result.getErrors()) {
                if (firstCause != null) {
                    firstCause = error.getCause();
                }
                
                sb.append("rpcError:").append(error.getCause().getMessage()).append(";");
            }
            throw new Exception(sb.toString(), firstCause);
        }
    }

    /**
     * after handshake set features, register to session
     * @param proposedVersion
     * @param xId
     */
    protected void postHandshake(Short proposedVersion, Long xid) {
        // set version
        version = proposedVersion;
        LOG.debug("version set: " + proposedVersion);
        // request features
        GetFeaturesInputBuilder featuresBuilder = new GetFeaturesInputBuilder();
        featuresBuilder.setVersion(version).setXid(xid);
        LOG.debug("sending feature request for version={} and xid={}", version, xid);
        Future<RpcResult<GetFeaturesOutput>> featuresFuture = connectionAdapter
                .getFeatures(featuresBuilder.build());
        LOG.debug("waiting for features");
        try {
            RpcResult<GetFeaturesOutput> rpcFeatures = 
                    featuresFuture.get(getMaxTimeout(), getMaxTimeoutUnit());
            smokeRpc(rpcFeatures);
            
            GetFeaturesOutput featureOutput =  rpcFeatures.getResult();
            LOG.debug("obtained features: datapathId={}",
                    featureOutput.getDatapathId());
            LOG.debug("obtained features: auxiliaryId={}",
                    featureOutput.getAuxiliaryId());
            conductorState = CONDUCTOR_STATE.WORKING;

            OFSessionUtil.registerSession(this,
                    featureOutput, version);
            this.setListenerMapping(OFSessionUtil.getListenersMap());
            LOG.info("handshake SETTLED: datapathId={}, auxiliaryId={}", featureOutput.getDatapathId(), featureOutput.getAuxiliaryId());
        } catch (Throwable e) {
            //handshake failed
            LOG.error("issuing disconnect during handshake, reason: "+e.getMessage());
            handleException(e);
            disconnect();
        }
    }

    /**
     * @return rpc-response timeout in [ms]
     */
    private long getMaxTimeout() {
        // TODO:: get from configuration
        return 2000;
    }
    
    /**
     * @return milliseconds
     */
    private TimeUnit getMaxTimeoutUnit() {
        // TODO:: get from configuration
        return TimeUnit.MILLISECONDS;
    }


    /**
     * @param e
     */
    protected void handleException(Throwable e) {
        String sessionKeyId = null;
        if (getSessionContext() != null) {
            sessionKeyId = Arrays.toString(getSessionContext().getSessionKey().getId());
        }
        
        Exception causeAndThread = new Exception(
                "IN THREAD: "+Thread.currentThread().getName() +
                "; session:"+sessionKeyId, e);
        try {
            errorQueue.put(causeAndThread);
        } catch (InterruptedException e1) {
            LOG.error(e1.getMessage(), e1);
        }
    }

    @Override
    public void onMultipartReplyMessage(MultipartReplyMessage arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onMultipartRequestMessage(MultipartRequestMessage arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPacketInMessage(PacketInMessage message) {
        notifyListeners(PacketInMessage.class, message);
    }

    @Override
    public void onPortStatusMessage(PortStatusMessage message) {
        this.getSessionContext().processPortStatusMsg(message);
        notifyListeners(PortStatusMessage.class, message);
    }

    @Override
    public void onSwitchIdleEvent(SwitchIdleEvent notification) {
        if (!CONDUCTOR_STATE.WORKING.equals(conductorState)) {
            // idle state in any other conductorState than WORKING means real
            // problem and wont be handled by echoReply, but disconnection
            disconnect();
            OFSessionUtil.getSessionManager().invalidateOnDisconnect(this);
        } else {
            LOG.debug("first idle state occured");
            EchoInputBuilder builder = new EchoInputBuilder();
            builder.setVersion(version);
            // TODO: get xid from sessionContext
            builder.setXid(42L);

            Future<RpcResult<EchoOutput>> echoReplyFuture = connectionAdapter
                    .echo(builder.build());

            try {
                // TODO: read timeout from config
                RpcResult<EchoOutput> echoReplyValue = echoReplyFuture.get(getMaxTimeout(),
                        getMaxTimeoutUnit());
                if (echoReplyValue.isSuccessful()) {
                    conductorState = CONDUCTOR_STATE.WORKING;
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
                OFSessionUtil.getSessionManager().invalidateOnDisconnect(this);
            }
        }
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

    /**
     * find supported version based on remoteVersion
     * @param remoteVersion
     * @return
     */
    protected short proposeVersion(short remoteVersion) {
        Short proposal = null;
        for (short offer : ConnectionConductor.versionOrder) {
            if (offer <= remoteVersion) {
                proposal = offer;
                break;
            }
        }
        if (proposal == null) {
            throw new IllegalArgumentException("unsupported version: "
                    + remoteVersion);
        }
        return proposal;
    }

    /**
     * find common highest supported bitmap version
     * @param list
     * @return
     */
    protected Short proposeBitmapVersion(List<Elements> list)
    {
        Short supportedHighestVersion = null;
        if((null != list) && (0 != list.size()))
        {
           for(Elements element : list)
           {
              List<Boolean> bitmap = element.getVersionBitmap();
              // check for version bitmap
              for(short bitPos : ConnectionConductor.versionOrder)
              {
                  // with all the version it should work.
                  if(bitmap.get(bitPos % Integer.SIZE))
                  {
                      supportedHighestVersion = bitPos;
                      break;
                  }
              }
           }
           if(null == supportedHighestVersion)
            {
                throw new IllegalArgumentException("unsupported bitmap version.");
            }

        }

        return supportedHighestVersion;
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
    
    /**
     * @param listenerMapping the listenerMapping to set
     */
    public void setListenerMapping(
            Map<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping) {
        this.listenerMapping = listenerMapping;
    }

    /**
     * @param messageType
     * @param message
     * @deprecated use {@link QueueKeeper} strategy
     */
    @Deprecated
    private void notifyListeners(Class<? extends DataObject> messageType, DataObject message) {
        Collection<IMDMessageListener> listeners = listenerMapping.get(messageType);
        if (listeners != null) {
                for (IMDMessageListener listener : listeners) {
                    // Pass cookie only for PACKT_IN
                    if ( messageType.equals("PacketInMessage.class")){
                        listener.receive(this.getAuxiliaryKey(), this.getSessionContext(), message);
                    } else {
                        listener.receive(null, this.getSessionContext(), message);
                    }
                }
        } else {
            LOG.warn("No listeners for this message Type {}", messageType);
        }
    }

    @Override
    public ConnectionAdapter getConnectionAdapter() {
        return connectionAdapter;
    }

    @Override
    public void onConnectionReady() {
        LOG.debug("connection is ready-to-use");
        //TODO: fire first helloMessage
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendFirstHelloMessage();
            }
        }).start();
    }
    
}
