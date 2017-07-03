/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.plan;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.opendaylight.controller.sal.common.util.RpcErrors;
import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandler;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowjava.protocol.api.extensibility.AlienMessageListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEventBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
public class ConnectionAdapterStackImpl implements ConnectionAdapter, Runnable {

    /** notify and rpc-response thread start default delay in [ms] */
    public static final int JOB_DELAY = 50;

    protected static final Logger LOG = LoggerFactory
            .getLogger(ConnectionAdapterStackImpl.class);

    protected Stack<? extends SwitchTestEvent> eventPlan;
    protected OpenflowProtocolListener ofListener;
    protected SystemNotificationsListener systemListener;

    protected Map<Long, SettableFuture<?>> rpcResults = new HashMap<>();
    protected boolean planTouched = false;

    private long proceedTimeout;

    protected List<Exception> occuredExceptions = new ArrayList<>();

    private ConnectionReadyListener connectionReadyListener;

    private int planItemCounter;

    private boolean autoRead = true;
    private final ExecutorService pool;
    private boolean packetInFiltering;


    /**
     * default ctor
     */
    public ConnectionAdapterStackImpl() {
        pool = Executors.newSingleThreadExecutor();
    }

    @Override
    public synchronized Future<RpcResult<BarrierOutput>> barrier(
            final BarrierInput arg0) {
        checkRpcAndNext(arg0, "barrier");
        SettableFuture<RpcResult<BarrierOutput>> result = createAndRegisterRpcResult(arg0);
        return result;
    }

    @Override
    public synchronized Future<RpcResult<EchoOutput>> echo(final EchoInput arg0) {
        checkRpcAndNext(arg0, "echo");
        Future<RpcResult<EchoOutput>> result = createAndRegisterRpcResult(arg0);
        return result;
    }

    @Override
    public Future<RpcResult<Void>> echoReply(final EchoReplyInput arg0) {
        checkRpcAndNext(arg0, "echoReply");
        ListenableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> experimenter(final ExperimenterInput arg0) {
        checkRpcAndNext(arg0, "experimenter");
        ListenableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> flowMod(final FlowModInput arg0) {
        checkRpcAndNext(arg0, "flowMod");
        ListenableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        return result;
    }

    @Override
    public synchronized Future<RpcResult<GetAsyncOutput>> getAsync(
            final GetAsyncInput arg0) {
        checkRpcAndNext(arg0, "echo");
        Future<RpcResult<GetAsyncOutput>> result = createAndRegisterRpcResult(arg0);
        return result;
    }

    @Override
    public synchronized Future<RpcResult<GetConfigOutput>> getConfig(
            final GetConfigInput arg0) {
        checkRpcAndNext(arg0, "echo");
        Future<RpcResult<GetConfigOutput>> result = createAndRegisterRpcResult(arg0);
        return result;
    }

    @Override
    public synchronized Future<RpcResult<GetFeaturesOutput>> getFeatures(
            final GetFeaturesInput arg0) {
        checkRpcAndNext(arg0, "getFeatures");
        Future<RpcResult<GetFeaturesOutput>> result = createAndRegisterRpcResult(arg0);
        return result;
    }

    @Override
    public synchronized Future<RpcResult<GetQueueConfigOutput>> getQueueConfig(
            final GetQueueConfigInput arg0) {
        checkRpcAndNext(arg0, "echo");
        Future<RpcResult<GetQueueConfigOutput>> result = createAndRegisterRpcResult(arg0);
        return result;
    }

    @Override
    public Future<RpcResult<Void>> groupMod(final GroupModInput arg0) {
        checkRpcAndNext(arg0, "groupMod");
        ListenableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> hello(final HelloInput arg0) {
        checkRpcAndNext(arg0, "helloReply");
        ListenableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> meterMod(final MeterModInput arg0) {
        checkRpcAndNext(arg0, "meterMod");
        ListenableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> packetOut(final PacketOutInput arg0) {
        checkRpcAndNext(arg0, "packetOut");
        ListenableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> portMod(final PortModInput arg0) {
        checkRpcAndNext(arg0, "portMod");
        ListenableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        return result;
    }

    @Override
    public synchronized Future<RpcResult<RoleRequestOutput>> roleRequest(
            final RoleRequestInput arg0) {
        checkRpcAndNext(arg0, "echo");
        Future<RpcResult<RoleRequestOutput>> result = createAndRegisterRpcResult(arg0);
        return result;
    }

    @Override
    public Future<RpcResult<Void>> setAsync(final SetAsyncInput arg0) {
        checkRpcAndNext(arg0, "setAsync");
        ListenableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> setConfig(final SetConfigInput arg0) {
        checkRpcAndNext(arg0, "setConfig");
        ListenableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> tableMod(final TableModInput arg0) {
        checkRpcAndNext(arg0, "tableMod");
        ListenableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        return result;
    }

    @Override
    public Future<Boolean> disconnect() {
        LOG.debug("adapter is told to disconnect");
        DisconnectEventBuilder disconnectEventBuilder = new DisconnectEventBuilder();
        disconnectEventBuilder.setInfo("disconnected by plugin");
        systemListener.onDisconnectEvent(disconnectEventBuilder.build());
        return null;
    }

    @Override
    public boolean isAlive() {
        // TODO make dynamic
        return true;
    }

    @Override
    public void setMessageListener(final OpenflowProtocolListener ofListener) {
        this.ofListener = ofListener;
    }

    @Override
    public void checkListeners() {
        if (ofListener == null || systemListener == null
                || connectionReadyListener == null) {
            occuredExceptions
                    .add(new IllegalStateException("missing listeners"));
        }
    }

    @Override
    public void setSystemListener(final SystemNotificationsListener systemListener) {
        this.systemListener = systemListener;
    }

    @Override
    public void setAlienMessageListener(final AlienMessageListener alienMessageListener) {

    }

    /**
     * @param rpcInput
     *            rpc call parameter
     * @param rpcName
     *            rpc yang name
     */
    private boolean checkRpc(final OfHeader rpcInput, final String rpcName) {
        String msg = null;
        boolean finished = true;

        if (eventPlan.isEmpty()) {
            throw new IllegalStateException("eventPlan already depleted");
        }

        LOG.debug("checking rpc: name={}, ver={}, xid={}", rpcName,
                rpcInput.getVersion(), rpcInput.getXid());
        if (!(eventPlan.peek() instanceof SwitchTestWaitForRpcEvent)
                && !(eventPlan.peek() instanceof SwitchTestWaitForAllEvent)) {
            if (eventPlan.peek() instanceof SwitchTestNotificationEvent) {
                SwitchTestNotificationEvent notifEvent = (SwitchTestNotificationEvent) (eventPlan
                        .peek());
                msg = "expected [notification: "
                        + notifEvent.getPlannedNotification() + "], got ["
                        + rpcInput.getClass().getSimpleName() + "]";
            } else if (eventPlan.peek() instanceof SwitchTestRcpResponseEvent) {
                SwitchTestRcpResponseEvent rpcEvent = (SwitchTestRcpResponseEvent) (eventPlan
                        .peek());
                msg = "expected [rpc: " + rpcEvent.getPlannedRpcResponse()
                        + "], got [" + rpcInput.getClass().getSimpleName()
                        + "]";
            }
        } else {
            if (eventPlan.peek() instanceof SwitchTestWaitForAllEvent) {
                SwitchTestWaitForAllEvent switchTestWaitForAll = (SwitchTestWaitForAllEvent) eventPlan
                        .peek();
                Set<SwitchTestWaitForRpcEvent> eventBag = switchTestWaitForAll
                        .getWaitEventBag();
                List<String> msgLot = new ArrayList<>();

                if (eventBag == null || eventBag.isEmpty()) {
                    msg = "no wait events in bag";
                } else {
                    finished = false;
                    for (SwitchTestWaitForRpcEvent switchTestWaitForRpc : eventBag) {
                        String msgPart = checkSingleRpcContent(rpcInput,
                                rpcName, switchTestWaitForRpc);

                        if (msgPart != null) {
                            msgLot.add(msgPart);
                        } else {
                            LOG.debug("wait event matched: {}", rpcName);
                            eventBag.remove(switchTestWaitForRpc);
                            if (eventBag.isEmpty()) {
                                finished = true;
                            }
                            msgLot.clear();
                            break;
                        }
                    }
                }

                if (!msgLot.isEmpty()) {
                    msg = Joiner.on(" | ").join(msgLot);
                }
            } else if (eventPlan.peek() instanceof SwitchTestWaitForRpcEvent) {
                SwitchTestWaitForRpcEvent switchTestRpcEvent = (SwitchTestWaitForRpcEvent) eventPlan
                        .peek();
                msg = checkSingleRpcContent(rpcInput, rpcName,
                        switchTestRpcEvent);
            }
        }

        if (msg != null) {
            LOG.debug("rpc check .. FAILED: " + msg);
            occuredExceptions.add(new IllegalArgumentException("step:"
                    + planItemCounter + " | " + msg));
        } else {
            LOG.debug("rpc check .. OK");
        }
        return finished;
    }

    /**
     * @param rpcInput
     * @param rpcName
     * @param switchTestWaitForRpc
     * @return
     */
    private static String checkSingleRpcContent(final OfHeader rpcInput,
            final String rpcName, final SwitchTestWaitForRpcEvent switchTestWaitForRpc) {
        String failureMsg = null;
        if (!rpcName.equals(switchTestWaitForRpc.getRpcName())) {
            failureMsg = "expected rpc name ["
                    + switchTestWaitForRpc.getRpcName() + "], got [" + rpcName
                    + "]";
        } else if (!rpcInput.getXid().equals(switchTestWaitForRpc.getXid())) {
            failureMsg = "expected " + rpcName + ".xid ["
                    + switchTestWaitForRpc.getXid() + "], got ["
                    + rpcInput.getXid() + "]";
        }

        return failureMsg;
    }

    /**
     * @param rpcInput
     *            rpc call parameter
     * @param rpcName
     *            rpc yang name
     */
    private synchronized void checkRpcAndNext(final OfHeader rpcInput, final String rpcName) {
        boolean finished = checkRpc(rpcInput, rpcName);
        if (finished) {
            next();
        }
    }

    /**
     * discard current event, execute next, if possible
     */
    private void next() {
        LOG.debug("<---> STEPPING TO NEXT event in plan (leaving [{}] {})",
                planItemCounter, eventPlan.peek());
        eventPlan.pop();
        planItemCounter++;
        planTouched = true;
        try {
            Thread.sleep(JOB_DELAY);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        notify();
    }

    /**
     * start or continue processing plan
     */
    private synchronized void proceed() {
        boolean processed = false;
        LOG.debug("proceeding plan item[{}]: {}", planItemCounter,
                eventPlan.peek());
        if (eventPlan.peek() instanceof SwitchTestNotificationEvent) {
            SwitchTestNotificationEvent notification = (SwitchTestNotificationEvent) eventPlan
                    .peek();
            processNotification(notification);
            processed = true;
        } else if (eventPlan.peek() instanceof SwitchTestRcpResponseEvent) {
            SwitchTestRcpResponseEvent rpcResponse = (SwitchTestRcpResponseEvent) eventPlan
                    .peek();
            processRpcResponse(rpcResponse);
            processed = true;
        } else if (eventPlan.peek() instanceof SwitchTestCallbackEvent) {
            SwitchTestCallbackEvent callbackEvent = (SwitchTestCallbackEvent) eventPlan
                    .peek();
            try {
                callbackEvent.getCallback().call();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                occuredExceptions.add(e);
            }
            processed = true;
        }

        if (processed) {
            next();
        } else {
            try {
                LOG.debug("now WAITING for OF_LISTENER to act ..");
                wait(proceedTimeout);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void run() {
        LOG.debug("|---> evenPlan STARTING ..");
        planItemCounter = 0;
        while (!eventPlan.isEmpty()) {
            planTouched = false;
            proceed();
            if (!planTouched) {
                occuredExceptions
                        .add(new IllegalStateException(
                                "eventPlan STALLED, planItemCounter="
                                        + planItemCounter));
                break;
            }
        }

        try {
            Thread.sleep(JOB_DELAY);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.debug("<---| eventPlan DONE");
    }

    /**
     * @param notificationEvent
     */
    private synchronized void processNotification(
            final SwitchTestNotificationEvent notificationEvent) {

        Notification notification = notificationEvent.getPlannedNotification();
        LOG.debug("notificating OF_LISTENER: "
                + notification.getClass().getSimpleName());

        // system events
        if (notification instanceof DisconnectEvent) {
            systemListener.onDisconnectEvent((DisconnectEvent) notification);
        }
        // of notifications
        else if (notification instanceof EchoRequestMessage) {
            ofListener.onEchoRequestMessage((EchoRequestMessage) notification);
        } else if (notification instanceof ErrorMessage) {
            ofListener.onErrorMessage((ErrorMessage) notification);
        } else if (notification instanceof ExperimenterMessage) {
            ofListener
                    .onExperimenterMessage((ExperimenterMessage) notification);
        } else if (notification instanceof FlowRemovedMessage) {
            ofListener.onFlowRemovedMessage((FlowRemovedMessage) notification);
        } else if (notification instanceof HelloMessage) {
            ofListener.onHelloMessage((HelloMessage) notification);
        } else if (notification instanceof MultipartReplyMessage) {
            ofListener
                    .onMultipartReplyMessage((MultipartReplyMessage) notification);
        } else if (notification instanceof PacketInMessage) {
            ofListener.onPacketInMessage((PacketInMessage) notification);
        } else if (notification instanceof PortStatusMessage) {
            ofListener.onPortStatusMessage((PortStatusMessage) notification);
        }
        // default
        else {
            occuredExceptions.add(new IllegalStateException("step:"
                    + planItemCounter + " | "
                    + "message listening not supported for type: "
                    + notification.getClass()));
        }

        LOG.debug("notification [" + notification.getClass().getSimpleName()
                + "] .. done");
    }

    /**
     * @param rpcResponse
     */
    private synchronized void processRpcResponse(
            final SwitchTestRcpResponseEvent rpcResponse) {
        OfHeader plannedRpcResponseValue = rpcResponse.getPlannedRpcResponse();
        LOG.debug("rpc-responding to OF_LISTENER: {}", rpcResponse.getXid());

        @SuppressWarnings("unchecked")
        final SettableFuture<RpcResult<?>> response = (SettableFuture<RpcResult<?>>) rpcResults
                .get(rpcResponse.getXid());

        if (response != null) {
            boolean successful = plannedRpcResponseValue != null;
            Collection<RpcError> errors;
            if (successful) {
                errors = Collections.emptyList();
            } else {
                errors = Lists.newArrayList(RpcErrors.getRpcError("unit",
                        "unit", "not requested", ErrorSeverity.ERROR,
                        "planned response to RPC.id = " + rpcResponse.getXid(),
                        ErrorType.RPC, new Exception(
                                "rpc response failed (planned behavior)")));
            }

            final RpcResult<?> result = Rpcs.getRpcResult(successful,
                    plannedRpcResponseValue, errors);
            setFutureViaPool(response, result);
        } else {
            String msg = "RpcResponse not expected: xid="
                    + rpcResponse.getXid() + ", "
                    + plannedRpcResponseValue.getClass().getSimpleName();
            LOG.error(msg);
            occuredExceptions.add(new IllegalStateException("step:"
                    + planItemCounter + " | " + msg));
        }

        LOG.debug("rpc [" + rpcResponse.getXid() + "] .. done");
    }

    private void setFutureViaPool(final SettableFuture<RpcResult<?>> response, final RpcResult<?> result) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                response.set(result);
            }
        });
    }

    /**
     * @param arg0
     *            rpc call content
     * @return rpc future result
     */
    private <IN extends OfHeader, OUT extends OfHeader> SettableFuture<RpcResult<OUT>> createAndRegisterRpcResult(
            final IN arg0) {
        SettableFuture<RpcResult<OUT>> result = SettableFuture.create();
        rpcResults.put(arg0.getXid(), result);
        return result;
    }

    /**
     * @return rpc future result
     */
    private static ListenableFuture<RpcResult<Void>> createOneWayRpcResult() {
        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

    /**
     * @param eventPlan
     *            the eventPlan to set
     */
    public void setEventPlan(final Stack<? extends SwitchTestEvent> eventPlan) {
        this.eventPlan = eventPlan;
    }

    /**
     * @param proceedTimeout
     *            max timeout for processing one planned event (in [ms])
     */
    public void setProceedTimeout(final long proceedTimeout) {
        this.proceedTimeout = proceedTimeout;
    }

    /**
     * @return the occuredExceptions
     */
    public List<Exception> getOccuredExceptions() {
        return occuredExceptions;
    }

    @Override
    public void fireConnectionReadyNotification() {
        connectionReadyListener.onConnectionReady();
    }

    @Override
    public void setConnectionReadyListener(
            final ConnectionReadyListener connectionReadyListener) {
        this.connectionReadyListener = connectionReadyListener;
    }

    @Override
    public Future<RpcResult<Void>> multipartRequest(final MultipartRequestInput arg0) {
        checkRpcAndNext(arg0, "multipartRequestInput");
        ListenableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        return result;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return InetSocketAddress.createUnresolved("unittest-odl.example.org", 4242);
    }

    @Override
    public boolean isAutoRead() {
        return autoRead;
    }

    @Override
    public <T extends OutboundQueueHandler> OutboundQueueHandlerRegistration<T> registerOutboundQueueHandler(final T t, final int i, final long l) {
        return null;
    }

    @Override
    public void setAutoRead(final boolean autoRead) {
        this.autoRead = autoRead;
    }

    @Override
    public void setPacketInFiltering(final boolean packetInFiltering) {
        this.packetInFiltering = packetInFiltering;
    }
}
