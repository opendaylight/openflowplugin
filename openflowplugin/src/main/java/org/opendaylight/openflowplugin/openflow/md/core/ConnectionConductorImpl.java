/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.openflow.core.IMessageListener;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloElementType;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.ElementsBuilder;
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

    /* variable to make BitMap-based negotiation enabled / disabled.
     * it will help while testing and isolating issues related to processing of
     * BitMaps from switches.
     */
    private static final boolean isBitmapNegotiationEnable = true;
    private LinkedBlockingQueue<Exception> errorQueue = new LinkedBlockingQueue<>();

    private final ConnectionAdapter connectionAdapter;
    private final List<Short> versionOrder;
    private ConnectionConductor.CONDUCTOR_STATE conductorState;
    private Short version;

    private SwitchConnectionDistinguisher auxiliaryKey;

    private SessionContext sessionContext;

    private ImmutableMap<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping;

    private boolean isFirstHelloNegotiation = true;



    /**
     * @param connectionAdapter
     */
    public ConnectionConductorImpl(ConnectionAdapter connectionAdapter) {
        this.connectionAdapter = connectionAdapter;
        conductorState = CONDUCTOR_STATE.HANDSHAKING;
        versionOrder = Lists.newArrayList((short) 0x04, (short) 0x01);
        // TODO: add a thread pool to handle ErrorQueueHandler
        new Thread(new ErrorQueueHandler(errorQueue)).start();
    }

    @Override
    public void init() {
        connectionAdapter.setMessageListener(this);
        connectionAdapter.setSystemListener(this);
        //TODO : Wait for library to provide interface from which we can send first hello message
//        sendFirstHelloMessage();
    }


    /**
     * send first hello message to switch
     */
    private void sendFirstHelloMessage() {
        short highestVersion = versionOrder.get(0);
        Long helloXid = 1L;
        HelloInputBuilder helloInputbuilder = new HelloInputBuilder();
        helloInputbuilder.setVersion(highestVersion);
        helloInputbuilder.setXid(helloXid);
        if (isBitmapNegotiationEnable) {
            int elementsCount = highestVersion / Integer.SIZE;
            ElementsBuilder elementsBuilder = new ElementsBuilder();

            List<Elements> elementList = new ArrayList<Elements>();
            int orderIndex = versionOrder.size();
            int value = versionOrder.get(--orderIndex);
            for (int index = 0; index <= elementsCount; index++) {
                List<Boolean> booleanList = new ArrayList<Boolean>();
                for (int i = 0; i < Integer.SIZE; i++) {
                    if (value == ((index * Integer.SIZE) + i)) {
                        booleanList.add(true);
                        value = (orderIndex == 0) ? highestVersion : versionOrder.get(--orderIndex);
                    } else {
                        booleanList.add(false);
                    }
                }
                elementsBuilder.setType(HelloElementType.forValue(1));
                elementsBuilder.setVersionBitmap(booleanList);
                elementList.add(elementsBuilder.build());
            }
            helloInputbuilder.setElements(elementList);
            LOG.debug("sending first hello message: version header={} , version bitmap={}", highestVersion, elementList);
        } else {
            LOG.debug("sending first hello message: version header={} ", highestVersion);
        }
        connectionAdapter.hello(helloInputbuilder.build());

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
        // TODO Auto-generated method stub
        LOG.debug("experimenter received, type: "
                + experimenterMessage.getExpType());
    }

    @Override
    public void onFlowRemovedMessage(FlowRemovedMessage arg0) {
        // TODO Auto-generated method stub
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
    public void onHelloMessage(HelloMessage hello) {
        // do handshake
        LOG.info("handshake STARTED");
        checkState(CONDUCTOR_STATE.HANDSHAKING);

        Short remoteVersion = hello.getVersion();
        List<Elements> elements = hello.getElements();
        long xid = hello.getXid();
        short proposedVersion;
        LOG.debug("Hello message version={} and bitmap={}", remoteVersion, elements);
        try {
            // find the version from header version field
            proposedVersion = proposeVersion(remoteVersion);

        } catch (IllegalArgumentException e) {
            handleException(e);
            connectionAdapter.disconnect();
            throw e;
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
                throw ex;
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

    /**
     * send hello reply
     * @param proposedVersion
     * @param hello
     */
    private void sendHelloReply(Short proposedVersion, Long xid)
    {
        HelloInputBuilder helloBuilder = new HelloInputBuilder();
        helloBuilder.setVersion(proposedVersion).setXid(xid);
        connectionAdapter.hello(helloBuilder.build());
    }


    /**
     * after handshake set features, register to session
     * @param proposedVersion
     * @param xId
     */
    private void postHandshake(Short proposedVersion, Long xid) {
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
        RpcResult<GetFeaturesOutput> rpcFeatures;
        try {
            rpcFeatures = featuresFuture.get(getMaxTimeout(),
                    TimeUnit.MILLISECONDS);
            if (!rpcFeatures.isSuccessful()) {
                LOG.error("obtained features problem: {}"
                        , rpcFeatures.getErrors());
            } else {
                GetFeaturesOutput featureOutput =  rpcFeatures.getResult();
                LOG.debug("obtained features: datapathId={}"
                        , featureOutput.getDatapathId());
                conductorState = CONDUCTOR_STATE.WORKING;

                OFSessionUtil.registerSession(this,
                        featureOutput, version);
                LOG.info("handshake SETTLED: datapathId={}, auxiliaryId={}", featureOutput.getDatapathId(), featureOutput.getAuxiliaryId());
            }
        } catch (Exception e) {
            handleException(e);
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
    public void onPortStatusMessage(PortStatusMessage arg0) {
        // TODO Auto-generated method stub
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

    /**
     * find supported version based on remoteVersion
     * @param remoteVersion
     * @return
     */
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

    /**
     * find common highest supported bitmap version
     * @param list
     * @return
     */
    protected short proposeBitmapVersion(List<Elements> list)
    {
        Short supportedHighestVersion = null;
        if((null != list) && (0 != list.size()))
        {
           for(Elements element : list)
           {
              List<Boolean> bitmap = element.getVersionBitmap();
              // check for version bitmap
              for(short bitPos : versionOrder)
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
            ImmutableMap<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping) {
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
                //TODO : need to add unit-tests
                //listener.receive(this.getAuxiliaryKey(), this.getSessionContext(), message);
            }
        }
    }

    @Override
    public ConnectionAdapter getConnectionAdapter() {
        return connectionAdapter;
    }
}
