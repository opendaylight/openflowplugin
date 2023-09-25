/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.connection;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.extensibility.AlienMessageListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Barrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Echo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Hello;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SslConnectionError;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Manages a switch connection.
 *
 * @author mirehak
 * @author michal.polkorab
 */
public interface ConnectionAdapter {
    @NonNullByDefault
    interface SystemListener {
        void onDisconnect(DisconnectEvent disconnect);

        void onSwitchIdle(SwitchIdleEvent switchIdle);

        void onSslConnectionError(SslConnectionError sslConnectionError);
    }

    /**
     * Disconnect corresponding switch.
     *
     * @return future set to true, when disconnect completed
     */
    Future<Boolean> disconnect();

    /**
     * Determines if the connection to the switch is alive.
     *
     * @return true, if connection to switch is alive
     */
    boolean isAlive();

    /**
     * Returns the address of the connected switch.
     *
     * @return address of the remote end - address of a switch if connected
     */
    InetSocketAddress getRemoteAddress();

    /**
     * Sets the protocol message listener.
     *
     * @param messageListener here will be pushed all messages from switch
     */
    void setMessageListener(OpenflowProtocolListener messageListener);

    /**
     * Sets the system message listener.
     *
     * @param systemListener here will be pushed all system messages from library
     */
    void setSystemListener(SystemListener systemListener);

    /**
     * Set handler for alien messages received from device.
     *
     * @param alienMessageListener here will be pushed all alien messages from switch
     */
    void setAlienMessageListener(AlienMessageListener alienMessageListener);

    /**
     * Throws exception if any of required listeners is missing.
     */
    void checkListeners();

    /**
     * Notify listener about connection ready-to-use event.
     */
    void fireConnectionReadyNotification();

    /**
     * Notify listener about switch certificate information.
     *
     * @param certificateChain X509 certificate chain presented by the switch
     */
    void onSwitchCertificateIdentified(@Nullable List<X509Certificate> certificateChain);

    /**
     * Set listener for connection became ready-to-use event.
     *
     * @param connectionReadyListener listens to connection ready event
     */
    void setConnectionReadyListener(ConnectionReadyListener connectionReadyListener);

    /**
     * Sets option for automatic channel reading - if set to false, incoming messages won't be read.
     *
     * @param autoRead target value to be switched to
     */
    void setAutoRead(boolean autoRead);

    /**
     * Determines if the channel is configured to auto-read.
     *
     * @return true, if channel is configured to auto-read
     */
    boolean isAutoRead();

    /**
     * Registers a new bypass outbound queue.
     *
     * @param <T> handler type
     * @param handler queue handler
     * @param maxQueueDepth max amount of not confirmed messaged in queue (i.e. edge for barrier message)
     * @param maxBarrierNanos regular base for barrier message
     * @return An {@link OutboundQueueHandlerRegistration}
     */
    @Beta
    <T extends OutboundQueueHandler> OutboundQueueHandlerRegistration<T> registerOutboundQueueHandler(T handler,
        int maxQueueDepth, long maxBarrierNanos);

    /**
     * Set filtering of PacketIn messages. By default these messages are not filtered.
     * @param enabled True if PacketIn messages should be filtered, false if they should be reported.
     */
    @Beta
    void setPacketInFiltering(boolean enabled);

    /**
     * Set datapathId for the dpn.
     * @param datapathId of the dpn
     */
    void setDatapathId(BigInteger datapathId);

    /**
     * Sets executorService.
     * @param executorService for all dpns
     */
    void setExecutorService(ExecutorService executorService);

    // Access to individual requests one can make on a connection.

    /**
     * Invoke {@code barrier} RPC. See also {@link Barrier}.
     *
     * @param input of {@code barrier}
     * @return output of {@code barrier}
     */
    @NonNull ListenableFuture<RpcResult<BarrierOutput>> barrier(BarrierInput input);

    /**
     * Invoke {@code echo} RPC. See also {@link Echo}.
     *
     * @param input of {@code echo}
     * @return output of {@code echo}
     */
    @NonNull ListenableFuture<RpcResult<EchoOutput>> echo(EchoInput input);

    /**
     * Invoke {@code echo-reply} RPC. See also {@link EchoReply}.
     *
     * @param input of {@code echo-reply}
     * @return output of {@code echo-reply}
     */
    @NonNull ListenableFuture<RpcResult<EchoReplyOutput>> echoReply(EchoReplyInput input);

    /**
     * Invoke {@code experimenter} RPC. See also {@link Experimenter}.
     *
     * @param input of {@code experimenter}
     * @return output of {@code experimenter}
     */
    @NonNull ListenableFuture<RpcResult<ExperimenterOutput>> experimenter(ExperimenterInput input);

    /**
     * Invoke {@code flow-mod} RPC. See also {@link FlowMod}.
     *
     * @param input of {@code flow-mod}
     * @return output of {@code flow-mod}
     */
    @NonNull ListenableFuture<RpcResult<FlowModOutput>> flowMod(FlowModInput input);

    /**
     * Invoke {@code get-async} RPC. See also {@link GetAsync}.
     *
     * @param input of {@code get-async}
     * @return output of {@code get-async}
     */
    @NonNull ListenableFuture<RpcResult<GetAsyncOutput>> getAsync(GetAsyncInput input);

    /**
     * Invoke {@code get-config} RPC. See also {@link GetConfig}.
     *
     * @param input of {@code get-config}
     * @return output of {@code get-config}
     */
    @NonNull ListenableFuture<RpcResult<GetConfigOutput>> getConfig(GetConfigInput input);

    /**
     * Invoke {@code get-features} RPC. See also {@link GetFeatures}.
     *
     * @param input of {@code get-features}
     * @return output of {@code get-features}
     */
    @NonNull ListenableFuture<RpcResult<GetFeaturesOutput>> getFeatures(GetFeaturesInput input);

    /**
     * Invoke {@code get-queue-config} RPC. See also {@link GetQueueConfig}.
     *
     * @param input of {@code get-queue-config}
     * @return output of {@code get-queue-config}
     */
    @NonNull ListenableFuture<RpcResult<GetQueueConfigOutput>> getQueueConfig(GetQueueConfigInput input);

    /**
     * Invoke {@code group-mod} RPC. See also {@link GroupMod}.
     *
     * @param input of {@code group-mod}
     * @return output of {@code group-mod}
     */
    @NonNull ListenableFuture<RpcResult<GroupModOutput>> groupMod(GroupModInput input);

    /**
     * Invoke {@code hello} RPC. See also {@link Hello}.
     *
     * @param input of {@code hello}
     * @return output of {@code hello}
     */
    @NonNull ListenableFuture<RpcResult<HelloOutput>> hello(HelloInput input);

    /**
     * Invoke {@code meter-mod} RPC. See also {@link MeterMod}.
     *
     * @param input of {@code meter-mod}
     * @return output of {@code meter-mod}
     */
    @NonNull ListenableFuture<RpcResult<MeterModOutput>> meterMod(MeterModInput input);

    /**
     * Invoke {@code multipart-request} RPC. See also {@link MultipartRequest}.
     *
     * @param input of {@code multipart-request}
     * @return output of {@code multipart-request}
     */
    @NonNull ListenableFuture<RpcResult<MultipartRequestOutput>> multipartRequest(MultipartRequestInput input);

    /**
     * Invoke {@code packet-out} RPC. See also {@link PacketOut}.
     *
     * @param input of {@code packet-out}
     * @return output of {@code packet-out}
     */
    @NonNull ListenableFuture<RpcResult<PacketOutOutput>> packetOut(PacketOutInput input);

    /**
     * Invoke {@code port-mod} RPC. See also {@link PortMod}.
     *
     * @param input of {@code port-mod}
     * @return output of {@code port-mod}
     */
    @NonNull ListenableFuture<RpcResult<PortModOutput>> portMod(PortModInput input);

    /**
     * Invoke {@code role-request} RPC. See also {@link RoleRequest}.
     *
     * @param input of {@code role-request}
     * @return output of {@code role-request}
     */
    @NonNull ListenableFuture<RpcResult<RoleRequestOutput>> roleRequest(RoleRequestInput input);

    /**
     * Invoke {@code set-async} RPC. See also {@link SetAsync}.
     *
     * @param input of {@code set-async}
     * @return output of {@code set-async}
     */
    @NonNull ListenableFuture<RpcResult<SetAsyncOutput>> setAsync(SetAsyncInput input);

    /**
     * Invoke {@code set-config} RPC. See also {@link SetConfig}.
     *
     * @param input of {@code set-config}
     * @return output of {@code set-config}
     */
    @NonNull ListenableFuture<RpcResult<SetConfigOutput>> setConfig(SetConfigInput input);

    /**
     * Invoke {@code table-mod} RPC. See also {@link TableMod}.
     *
     * @param input of {@code table-mod}
     * @return output of {@code table-mod}
     */
    @NonNull ListenableFuture<RpcResult<TableModOutput>> tableMod(TableModInput input);
}
