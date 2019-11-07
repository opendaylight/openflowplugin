/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.it.integration;

import com.google.common.util.concurrent.SettableFuture;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.impl.core.SwitchConnectionProviderImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SslConnectionError;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock plugin.
 *
 * @author michal.polkorab
 */
public class MockPlugin implements OpenflowProtocolListener, SwitchConnectionHandler,
        SystemNotificationsListener, ConnectionReadyListener {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MockPlugin.class);
    protected volatile ConnectionAdapter adapter;
    private final SettableFuture<Void> finishedFuture;
    private int idleCounter = 0;

    public MockPlugin() {
        LOGGER.trace("Creating MockPlugin");
        finishedFuture = SettableFuture.create();
        LOGGER.debug("mockPlugin: {}", System.identityHashCode(this));
    }

    @Override
    public void onSwitchConnected(ConnectionAdapter connection) {
        LOGGER.debug("onSwitchConnected: {}", connection);
        this.adapter = connection;
        connection.setMessageListener(this);
        connection.setSystemListener(this);
        connection.setConnectionReadyListener(this);
    }

    @Override
    public boolean accept(InetAddress switchAddress) {
        LOGGER.debug("MockPlugin.accept(): {}", switchAddress.toString());

        return true;
    }

    @Override
    public void onEchoRequestMessage(final EchoRequestMessage notification) {
        LOGGER.debug("MockPlugin.onEchoRequestMessage() adapter: {}", adapter);
        new Thread(() -> {
            LOGGER.debug("MockPlugin.onEchoRequestMessage().run() started adapter: {}", adapter);
            EchoReplyInputBuilder replyBuilder = new EchoReplyInputBuilder();
            replyBuilder.setVersion((short) 4);
            replyBuilder.setXid(notification.getXid());
            EchoReplyInput echoReplyInput = replyBuilder.build();
            adapter.echoReply(echoReplyInput);
            LOGGER.debug("adapter.EchoReply(Input) sent : ", echoReplyInput.toString());
            LOGGER.debug("MockPlugin.onEchoRequestMessage().run() finished adapter: {}", adapter);
        }).start();
    }

    @Override
    public void onErrorMessage(ErrorMessage notification) {
        LOGGER.debug("Error message received");

    }

    @Override
    public void onExperimenterMessage(ExperimenterMessage notification) {
        LOGGER.debug("Experimenter message received");

    }

    @Override
    public void onFlowRemovedMessage(FlowRemovedMessage notification) {
        LOGGER.debug("FlowRemoved message received");

    }

    @Override
    public void onHelloMessage(HelloMessage notification) {
        new Thread(() -> {
            LOGGER.debug("MockPlugin.onHelloMessage().run() Hello message received");
            HelloInputBuilder hib = new HelloInputBuilder();
            hib.setVersion((short) 4);
            hib.setXid(2L);
            HelloInput hi = hib.build();
            adapter.hello(hi);
            LOGGER.debug("hello msg sent");
            new Thread(this::getSwitchFeatures).start();
        }).start();

    }

    protected void getSwitchFeatures() {
        GetFeaturesInputBuilder featuresBuilder = new GetFeaturesInputBuilder();
        featuresBuilder.setVersion((short) 4);
        featuresBuilder.setXid(3L);
        GetFeaturesInput featuresInput = featuresBuilder.build();
        try {
            LOGGER.debug("Requesting features ");
            RpcResult<GetFeaturesOutput> rpcResult = adapter.getFeatures(
                    featuresInput).get(2500, TimeUnit.MILLISECONDS);
            if (rpcResult.isSuccessful()) {
                LOGGER.debug("DatapathId: {}", rpcResult.getResult().getDatapathId());
            } else {
                RpcError rpcError = rpcResult.getErrors().iterator().next();
                LOGGER.warn("rpcResult failed", rpcError.getCause());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("getSwitchFeatures() exception caught: ", e.getMessage(), e);
        }
    }

    protected void shutdown() throws InterruptedException, ExecutionException {
        LOGGER.debug("MockPlugin.shutdown() sleeping 5... : {}", System.identityHashCode(this));
        Thread.sleep(500);
        if (adapter != null) {
            Future<Boolean> disconnect = adapter.disconnect();
            disconnect.get();
            LOGGER.debug("MockPlugin.shutdown() Disconnected");
        }

        finishedFuture.set(null);
    }

    @Override
    public void onMultipartReplyMessage(MultipartReplyMessage notification) {
        LOGGER.debug("MultipartReply message received");

    }

    @Override
    public void onPacketInMessage(PacketInMessage notification) {
        LOGGER.debug("PacketIn message received");
        LOGGER.debug("BufferId: {}", notification.getBufferId());
        LOGGER.debug("TotalLength: {}", notification.getTotalLen());
        LOGGER.debug("Reason: {}", notification.getReason());
        LOGGER.debug("TableId: {}", notification.getTableId());
        LOGGER.debug("Cookie: {}", notification.getCookie());
        LOGGER.debug("Class: {}", notification.getMatch().getMatchEntry().get(0).getOxmClass());
        LOGGER.debug("Field: {}", notification.getMatch().getMatchEntry().get(0).getOxmMatchField());
        LOGGER.debug("Datasize: {}", notification.getData().length);
    }

    @Override
    public void onPortStatusMessage(PortStatusMessage notification) {
        LOGGER.debug("MockPlugin.onPortStatusMessage() message received");

    }

    @Override
    public void onDisconnectEvent(DisconnectEvent notification) {
        LOGGER.debug("disconnection occured: {}", notification.getInfo());
    }

    public SettableFuture<Void> getFinishedFuture() {
        return finishedFuture;
    }

    @Override
    public void onSwitchIdleEvent(SwitchIdleEvent notification) {
        LOGGER.debug("MockPlugin.onSwitchIdleEvent() switch status: {}", notification.getInfo());
        idleCounter ++;
    }

    /**
     * Returns number of occurred idleEvents.
     */
    public int getIdleCounter() {
        return idleCounter;
    }

    @Override
    public void onConnectionReady() {
        LOGGER.trace("MockPlugin().onConnectionReady()");
    }

    /**
     * Initiates connection to device.
     *
     * @param switchConnectionProvider the SwitchConnectionProviderImpl
     * @param host - host IP
     * @param port - port number
     */
    public void initiateConnection(SwitchConnectionProviderImpl switchConnectionProvider, String host, int port) {
        LOGGER.trace("MockPlugin().initiateConnection()");
        switchConnectionProvider.initiateConnection(host, port);
    }

    @Override
    public void onSslConnectionError(SslConnectionError notification) {
        LOGGER.debug("Ssl error occured: {}", notification.getInfo());

    }
}
