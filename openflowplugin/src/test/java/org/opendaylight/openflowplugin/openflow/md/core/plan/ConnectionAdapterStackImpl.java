/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.plan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Future;

import org.opendaylight.controller.sal.common.util.RpcErrors;
import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestMessage;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;

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

    /**
     * default ctor
     */
    public ConnectionAdapterStackImpl() {
        // do nothing
    }

    @Override
    public Future<RpcResult<BarrierOutput>> barrier(BarrierInput arg0) {
        checkRpc(arg0, "barrier");
        SettableFuture<RpcResult<BarrierOutput>> result = createAndRegisterRpcResult(arg0);
        next();
        return result;
    }

    @Override
    public Future<RpcResult<EchoOutput>> echo(EchoInput arg0) {
        checkRpc(arg0, "echo");
        Future<RpcResult<EchoOutput>> result = createAndRegisterRpcResult(arg0);
        next();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> echoReply(EchoReplyInput arg0) {
        checkRpc(arg0, "echoReply");
        SettableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        next();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> experimenter(ExperimenterInput arg0) {
        checkRpc(arg0, "experimenter");
        SettableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        next();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> flowMod(FlowModInput arg0) {
        checkRpc(arg0, "flowMod");
        SettableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        next();
        return result;
    }

    @Override
    public Future<RpcResult<GetAsyncOutput>> getAsync(GetAsyncInput arg0) {
        checkRpc(arg0, "echo");
        Future<RpcResult<GetAsyncOutput>> result = createAndRegisterRpcResult(arg0);
        next();
        return result;
    }

    @Override
    public Future<RpcResult<GetConfigOutput>> getConfig(GetConfigInput arg0) {
        checkRpc(arg0, "echo");
        Future<RpcResult<GetConfigOutput>> result = createAndRegisterRpcResult(arg0);
        next();
        return result;
    }

    @Override
    public Future<RpcResult<GetFeaturesOutput>> getFeatures(
            GetFeaturesInput arg0) {
        checkRpc(arg0, "getFeatures");
        Future<RpcResult<GetFeaturesOutput>> result = createAndRegisterRpcResult(arg0);
        next();
        return result;
    }

    @Override
    public Future<RpcResult<GetQueueConfigOutput>> getQueueConfig(
            GetQueueConfigInput arg0) {
        checkRpc(arg0, "echo");
        Future<RpcResult<GetQueueConfigOutput>> result = createAndRegisterRpcResult(arg0);
        next();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> groupMod(GroupModInput arg0) {
        checkRpc(arg0, "groupMod");
        SettableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        next();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> hello(HelloInput arg0) {
        checkRpc(arg0, "helloReply");
        SettableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        next();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> meterMod(MeterModInput arg0) {
        checkRpc(arg0, "meterMod");
        SettableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        next();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> packetOut(PacketOutInput arg0) {
        checkRpc(arg0, "packetOut");
        SettableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        next();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> portMod(PortModInput arg0) {
        checkRpc(arg0, "portMod");
        SettableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        next();
        return result;
    }

    @Override
    public Future<RpcResult<RoleRequestOutput>> roleRequest(
            RoleRequestInput arg0) {
        checkRpc(arg0, "echo");
        Future<RpcResult<RoleRequestOutput>> result = createAndRegisterRpcResult(arg0);
        next();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> setAsync(SetAsyncInput arg0) {
        checkRpc(arg0, "setAsync");
        SettableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        next();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> setConfig(SetConfigInput arg0) {
        checkRpc(arg0, "setConfig");
        SettableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        next();
        return result;
    }

    @Override
    public Future<RpcResult<Void>> tableMod(TableModInput arg0) {
        checkRpc(arg0, "tableMod");
        SettableFuture<RpcResult<Void>> result = createOneWayRpcResult();
        next();
        return result;
    }

    @Override
    public Future<Boolean> disconnect() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAlive() {
        // TODO make dynamic
        return true;
    }

    @Override
    public void setMessageListener(OpenflowProtocolListener ofListener) {
        this.ofListener = ofListener;
    }

    @Override
    public void checkListeners() {
        if (ofListener == null || systemListener == null || connectionReadyListener == null) {
            occuredExceptions
                    .add(new IllegalStateException("missing listeners"));
        }
    }

    @Override
    public void setSystemListener(SystemNotificationsListener systemListener) {
        this.systemListener = systemListener;
    }

    /**
     * @param rpcInput
     *            rpc call parameter
     * @param rpcName
     *            rpc yang name
     */
    private synchronized void checkRpc(OfHeader rpcInput, String rpcName) {
        String msg = null;
        LOG.debug("checking rpc: " + rpcName);
        if (!(eventPlan.peek() instanceof SwitchTestWaitForRpcEvent)) {
            if (eventPlan.peek() instanceof SwitchTestNotificationEvent) {
                SwitchTestNotificationEvent notifEvent = (SwitchTestNotificationEvent) (eventPlan.peek());
                msg = "expected [notification: " +notifEvent.getPlannedNotification()+ "], got [" + rpcInput.getClass().getSimpleName()
                        + "]";
            } else if (eventPlan.peek() instanceof SwitchTestRcpResponseEvent) {
                SwitchTestRcpResponseEvent rpcEvent = (SwitchTestRcpResponseEvent) (eventPlan.peek());
                msg = "expected [rpc: " +rpcEvent.getPlannedRpcResponse()+ "], got [" + rpcInput.getClass().getSimpleName()
                        + "]";
            }
        } else {
            SwitchTestWaitForRpcEvent switchTestRpcEvent = (SwitchTestWaitForRpcEvent) eventPlan
                    .peek();
            if (!rpcName.equals(switchTestRpcEvent.getRpcName())) {
                msg = "expected rpc name [" + switchTestRpcEvent.getRpcName()
                        + "], got [" + rpcName + "]";
            } else if (!rpcInput.getXid().equals(switchTestRpcEvent.getXid())) {
                msg = "expected xid [" + switchTestRpcEvent.getXid()
                        + "], got [" + rpcInput.getXid() + "]";
            }
        }

        if (msg != null) {
            LOG.debug("rpc check .. FAILED: " + msg);
            occuredExceptions.add(new IllegalArgumentException(msg));
        }
        LOG.debug("rpc check .. OK");
    }

    /**
     * discard current event, execute next, if possible
     */
    private synchronized void next() {
        LOG.debug("<---> STEPPING TO NEXT event in plan (leaving [{}] {})", planItemCounter, eventPlan.peek());
        eventPlan.pop();
        planItemCounter ++;
        planTouched = true;
        notify();
    }

    /**
     * start or continue processing plan
     */
    private synchronized void proceed() {
        boolean processed = false;
        LOG.debug("proceeding plan item[{}]: {}", planItemCounter, eventPlan.peek());
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
                occuredExceptions.add(new IllegalStateException(
                        "eventPlan STALLED"));
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
    private void processNotification(
            final SwitchTestNotificationEvent notificationEvent) {

        Notification notification = notificationEvent
                .getPlannedNotification();
        LOG.debug("notificating OF_LISTENER: "
                + notification.getClass().getSimpleName());

        // system events
        if (notification instanceof DisconnectEvent) {
            systemListener
            .onDisconnectEvent((DisconnectEvent) notification);
        }
        // of notifications
        else if (notification instanceof EchoRequestMessage) {
            ofListener
            .onEchoRequestMessage((EchoRequestMessage) notification);
        } else if (notification instanceof ErrorMessage) {
            ofListener.onErrorMessage((ErrorMessage) notification);
        } else if (notification instanceof ExperimenterMessage) {
            ofListener
            .onExperimenterMessage((ExperimenterMessage) notification);
        } else if (notification instanceof FlowRemovedMessage) {
            ofListener
            .onFlowRemovedMessage((FlowRemovedMessage) notification);
        } else if (notification instanceof HelloMessage) {
            ofListener.onHelloMessage((HelloMessage) notification);
        } else if (notification instanceof MultipartReplyMessage) {
            ofListener
            .onMultipartReplyMessage((MultipartReplyMessage) notification);
        } else if (notification instanceof MultipartRequestMessage) {
            ofListener
            .onMultipartRequestMessage((MultipartRequestMessage) notification);
        } else if (notification instanceof PacketInMessage) {
            ofListener
            .onPacketInMessage((PacketInMessage) notification);
        } else if (notification instanceof PortStatusMessage) {
            ofListener
            .onPortStatusMessage((PortStatusMessage) notification);
        }
        // default
        else {
            occuredExceptions.add(new IllegalStateException(
                    "message listening not supported for type: "
                            + notification.getClass()));
        }

        LOG.debug("notification ["+notification.getClass().getSimpleName()+"] .. done");
    }

    /**
     * @param rpcResponse
     */
    private void processRpcResponse(final SwitchTestRcpResponseEvent rpcResponse) {
        OfHeader plannedRpcResponseValue = rpcResponse
                .getPlannedRpcResponse();
        LOG.debug("rpc-responding to OF_LISTENER: " + rpcResponse.getXid());

        @SuppressWarnings("unchecked")
        SettableFuture<RpcResult<?>> response = (SettableFuture<RpcResult<?>>) rpcResults
        .get(rpcResponse.getXid());

        if (response != null) {
            boolean successful = plannedRpcResponseValue != null;
            Collection<RpcError> errors;
            if (successful) {
                errors = Collections.emptyList();
            } else {
                errors = Lists
                        .newArrayList(RpcErrors
                                .getRpcError(
                                        "unit",
                                        "unit",
                                        "not requested",
                                        ErrorSeverity.ERROR,
                                        "planned response to RPC.id = "
                                                + rpcResponse.getXid(),
                                                ErrorType.RPC,
                                                new Exception(
                                                        "rpc response failed (planned behavior)")));
            }
            RpcResult<?> result = Rpcs.getRpcResult(successful,
                    plannedRpcResponseValue, errors);
            response.set(result);
        } else {
            String msg = "RpcResponse not expected: xid="
                    + rpcResponse.getXid()
                    + ", "
                    + plannedRpcResponseValue.getClass()
                    .getSimpleName();
            LOG.error(msg);
            occuredExceptions.add(new IllegalStateException(msg));
        }

        LOG.debug("rpc ["+rpcResponse.getXid()+"] .. done");
    }

    /**
     * @param arg0
     *            rpc call content
     * @return rpc future result
     */
    private <IN extends OfHeader, OUT extends OfHeader> SettableFuture<RpcResult<OUT>> createAndRegisterRpcResult(
            IN arg0) {
        SettableFuture<RpcResult<OUT>> result = SettableFuture.create();
        rpcResults.put(arg0.getXid(), result);
        return result;
    }

    /**
     * @return rpc future result
     */
    private static SettableFuture<RpcResult<Void>> createOneWayRpcResult() {
        SettableFuture<RpcResult<Void>> result = SettableFuture.create();
        List<RpcError> errors = Collections.emptyList();
        result.set(Rpcs.getRpcResult(true, (Void) null, errors));
        return result;
    }

    /**
     * @param eventPlan
     *            the eventPlan to set
     */
    public void setEventPlan(Stack<? extends SwitchTestEvent> eventPlan) {
        this.eventPlan = eventPlan;
    }

    /**
     * @param proceedTimeout
     *            max timeout for processing one planned event (in [ms])
     */
    public void setProceedTimeout(long proceedTimeout) {
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
            ConnectionReadyListener connectionReadyListener) {
        this.connectionReadyListener = connectionReadyListener;
    }
}
