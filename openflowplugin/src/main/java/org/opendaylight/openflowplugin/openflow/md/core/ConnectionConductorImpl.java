/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * @author mirehak
 */
public class ConnectionConductorImpl implements OpenflowProtocolListener,
        SystemNotificationsListener, ConnectionConductor {

    private static final Logger LOG = LoggerFactory
            .getLogger(ConnectionConductorImpl.class);

    private LinkedBlockingQueue<Exception> errorQueue = new LinkedBlockingQueue<>();

    private final ConnectionAdapter connectionAdapter;
    private final List<Short> versionOrder;
    private ConnectionConductor.CONDUCTOR_STATE conductorState;
    private Short version;

    private SwitchConnectionDistinguisher auxiliaryKey;

    private SessionContext sessionContext;

    private Map<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping;

    /**
     * @param connectionAdapter
     */
    public ConnectionConductorImpl(ConnectionAdapter connectionAdapter) {
        this.connectionAdapter = connectionAdapter;
        conductorState = CONDUCTOR_STATE.HANDSHAKING;
        versionOrder = Lists.newArrayList((short) 0x04, (short) 0x01);
        new Thread(new ErrorQueueHandler(errorQueue)).start();
    }

    @Override
    public void init() {
        connectionAdapter.setMessageListener(this);
        connectionAdapter.setSystemListener(this);
    }

    @Override
    public void onEchoRequestMessage(EchoRequestMessage echoRequestMessage) {
        LOG.debug("echo request received: " + echoRequestMessage.getXid());
        EchoReplyInputBuilder builder = new EchoReplyInputBuilder();
        builder.setVersion(echoRequestMessage.getVersion());
        builder.setXid(echoRequestMessage.getXid());
        builder.setData(echoRequestMessage.getData());

        connectionAdapter.echoReply(builder.build());
    }

    @Override
    public void onErrorMessage(ErrorMessage errorMessage) {
        // TODO Auto-generated method stub
        LOG.debug("error received, type: " + errorMessage.getType()
                + "; code: " + errorMessage.getCode());
    }

    @Override
    public void onExperimenterMessage(ExperimenterMessage experimenterMessage) {
        LOG.debug("experimenter received, type: "
                + experimenterMessage.getExpType());
        notifyListeners(ExperimenterMessage.class, experimenterMessage);
    }

    @Override
    public void onFlowRemovedMessage(FlowRemovedMessage message) {
        notifyListeners(FlowRemovedMessage.class, message);
    }

    @Override
    public void onHelloMessage(HelloMessage hello) {
        // do handshake
        LOG.info("handshake STARTED");
        checkState(CONDUCTOR_STATE.HANDSHAKING);

        Short remoteVersion = hello.getVersion();
        long xid = hello.getXid();
        short proposedVersion;
        try {
            proposedVersion = proposeVersion(remoteVersion);
        } catch (Exception e) {
            handleException(e);
            throw e;
        }
        HelloInputBuilder helloBuilder = new HelloInputBuilder();
        xid++;
        helloBuilder.setVersion(proposedVersion).setXid(xid);
        LOG.debug("sending helloReply");
        connectionAdapter.hello(helloBuilder.build());

        if (proposedVersion != remoteVersion) {
            // need to wait for another hello
        } else {
            // sent version is equal to remote --> version is negotiated
            version = proposedVersion;
            LOG.debug("version set: " + proposedVersion);

            // request features
            GetFeaturesInputBuilder featuresBuilder = new GetFeaturesInputBuilder();
            xid++;
            featuresBuilder.setVersion(version).setXid(xid);
            Future<RpcResult<GetFeaturesOutput>> featuresFuture = connectionAdapter
                    .getFeatures(featuresBuilder.build());
            LOG.debug("waiting for features");
            RpcResult<GetFeaturesOutput> rpcFeatures;
            try {
                rpcFeatures = featuresFuture.get(getMaxTimeout(),
                        TimeUnit.MILLISECONDS);
                if (!rpcFeatures.isSuccessful()) {
                    LOG.error("obtained features problem: "
                            + rpcFeatures.getErrors());
                } else {
                    LOG.debug("obtained features: datapathId="
                            + rpcFeatures.getResult().getDatapathId());
                    conductorState = CONDUCTOR_STATE.WORKING;

                    OFSessionUtil.registerSession(this,
                            rpcFeatures.getResult(), version);
                    LOG.info("handshake SETTLED");
                }
            } catch (Exception e) {
                handleException(e);
            }
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
     * @param e
     */
    private void handleException(Exception e) {
        try {
            errorQueue.put(e);
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
                RpcResult<EchoOutput> echoReplyValue = echoReplyFuture.get(5,
                        TimeUnit.SECONDS);
                if (echoReplyValue.isSuccessful()) {
                    conductorState = CONDUCTOR_STATE.WORKING;
                } else {
                    for (RpcError replyError : echoReplyValue.getErrors()) {
                        Throwable cause = replyError.getCause();
                        LOG.error(
                                "while receiving echoReply in TIMEOUTING state: "
                                        + cause.getMessage(), cause);
                    }
                }
            } catch (Exception e) {
                LOG.error("while waiting for echoReply in TIMEOUTING state: "
                        + e.getMessage(), e);
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
    private void checkState(CONDUCTOR_STATE expectedState) {
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

    protected short proposeVersion(short remoteVersion) {
        Short proposal = null;
        for (short offer : versionOrder) {
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

    @Override
    public Short getVersion() {
        return version;
    }

    @Override
    public Future<Boolean> disconnect() {
        return connectionAdapter.disconnect();
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
        //TODO: adjust the listener interface
        this.listenerMapping = listenerMapping;
    }

    /**
     * @param messageType
     * @param message
     */
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
}
