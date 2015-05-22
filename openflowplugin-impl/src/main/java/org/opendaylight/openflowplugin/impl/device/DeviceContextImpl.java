/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextClosedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.common.NodeStaticReplyTranslatorUtil;
import org.opendaylight.openflowplugin.impl.device.listener.MultiMsgCollectorImpl;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.openflowplugin.impl.registry.group.DeviceGroupRegistryImpl;
import org.opendaylight.openflowplugin.impl.registry.meter.DeviceMeterRegistryImpl;
import org.opendaylight.openflowplugin.openflow.md.core.session.SwitchConnectionCookieOFImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsDataBuilder;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DeviceContextImpl implements DeviceContext {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceContextImpl.class);

    // TODO: watermarks should be derived from effective rpc limit (75%|95%)
    private static final int PACKETIN_LOW_WATERMARK = 15000;
    private static final int PACKETIN_HIGH_WATERMARK = 19000;
    // TODO: drain factor should be parametrized
    public static final float REJECTED_DRAIN_FACTOR = 0.25f;

    private final ConnectionContext primaryConnectionContext;
    private final DeviceState deviceState;
    private final DataBroker dataBroker;
    private final HashedWheelTimer hashedWheelTimer;
    private final Map<SwitchConnectionDistinguisher, ConnectionContext> auxiliaryConnectionContexts;
    private final TransactionChainManager txChainManager;
    private final DeviceFlowRegistry deviceFlowRegistry;
    private final DeviceGroupRegistry deviceGroupRegistry;
    private final DeviceMeterRegistry deviceMeterRegistry;
    private final Collection<DeviceContextClosedHandler> closeHandlers = new HashSet<>();
    private final PacketInRateLimiter packetInLimiter;
    private final MessageSpy messageSpy;
    private NotificationPublishService notificationPublishService;
    private DeviceDisconnectedHandler deviceDisconnectedHandler;
    private NotificationService notificationService;
    private TranslatorLibrary translatorLibrary;
    private OutboundQueue outboundQueueProvider;
    private Timeout barrierTaskTimeout;

    @VisibleForTesting
    DeviceContextImpl(@Nonnull final ConnectionContext primaryConnectionContext,
                      @Nonnull final DeviceState deviceState,
                      @Nonnull final DataBroker dataBroker,
                      @Nonnull final HashedWheelTimer hashedWheelTimer,
                      @Nonnull final MessageSpy _messageSpy, OutboundQueueProvider outboundQueueProvider) {
        this.primaryConnectionContext = Preconditions.checkNotNull(primaryConnectionContext);
        this.deviceState = Preconditions.checkNotNull(deviceState);
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.hashedWheelTimer = Preconditions.checkNotNull(hashedWheelTimer);
        this.outboundQueueProvider = Preconditions.checkNotNull(outboundQueueProvider);
        txChainManager = new TransactionChainManager(dataBroker, hashedWheelTimer, 500L, 500L);
        auxiliaryConnectionContexts = new HashMap<>();
        deviceFlowRegistry = new DeviceFlowRegistryImpl();
        deviceGroupRegistry = new DeviceGroupRegistryImpl();
        deviceMeterRegistry = new DeviceMeterRegistryImpl();
        messageSpy = _messageSpy;

        this.packetInLimiter = new PacketInRateLimiter(primaryConnectionContext.getConnectionAdapter(),
                PACKETIN_LOW_WATERMARK, PACKETIN_HIGH_WATERMARK, messageSpy, REJECTED_DRAIN_FACTOR);
    }

    /**
     * This method is called from {@link DeviceManagerImpl} only. So we could say "posthandshake process finish"
     * and we are able to set a scheduler for an automatic transaction submitting by time (0,5sec).
     */
    void submitTransaction() {
        txChainManager.enableSubmit();
        txChainManager.submitTransaction();
    }

    @Override
    public Long getReservedXid() {
        return outboundQueueProvider.reserveEntry();
    }

    @Override
    public <M extends ChildOf<DataObject>> void onMessage(final M message, final RequestContext<?> requestContext) {
        // TODO Auto-generated method stub
    }

    @Override
    public void addAuxiliaryConenctionContext(final ConnectionContext connectionContext) {
        final SwitchConnectionDistinguisher connectionDistinguisher = createConnectionDistinguisher(connectionContext);
        auxiliaryConnectionContexts.put(connectionDistinguisher, connectionContext);
    }

    private static SwitchConnectionDistinguisher createConnectionDistinguisher(final ConnectionContext connectionContext) {
        return new SwitchConnectionCookieOFImpl(connectionContext.getFeatures().getAuxiliaryId());
    }

    @Override
    public void removeAuxiliaryConenctionContext(final ConnectionContext connectionContext) {
        // TODO Auto-generated method stub
    }

    @Override
    public DeviceState getDeviceState() {
        return deviceState;
    }

    @Override
    public ReadTransaction getReadTransaction() {
        return dataBroker.newReadOnlyTransaction();
    }

    @Override
    public <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                          final InstanceIdentifier<T> path, final T data) {
        txChainManager.writeToTransaction(store, path, data);
    }

    @Override
    public <T extends DataObject> void addDeleteToTxChain(final LogicalDatastoreType store, final InstanceIdentifier<T> path) {
        txChainManager.addDeleteOperationTotTxChain(store, path);
    }

    @Override
    public ConnectionContext getPrimaryConnectionContext() {
        return primaryConnectionContext;
    }

    @Override
    public ConnectionContext getAuxiliaryConnectiobContexts(final BigInteger cookie) {
        return auxiliaryConnectionContexts.get(new SwitchConnectionCookieOFImpl(cookie.longValue()));
    }

    @Override
    public DeviceFlowRegistry getDeviceFlowRegistry() {
        return deviceFlowRegistry;
    }

    @Override
    public DeviceGroupRegistry getDeviceGroupRegistry() {
        return deviceGroupRegistry;
    }

    @Override
    public DeviceMeterRegistry getDeviceMeterRegistry() {
        return deviceMeterRegistry;
    }

    @Override
    public void processReply(final OfHeader ofHeader) {
        if (ofHeader instanceof Error) {
            messageSpy.spyMessage(ofHeader.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE);
        } else {
            messageSpy.spyMessage(ofHeader.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS);
        }
    }

    @Override
    public void processReply(final Xid xid, final List<MultipartReply> ofHeaderList) {
        for (final MultipartReply multipartReply : ofHeaderList) {
            messageSpy.spyMessage(multipartReply.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE);
        }
    }

    @Override
    public void processFlowRemovedMessage(final FlowRemoved flowRemoved) {
        //TODO: will be defined later
    }

    @Override
    public void processPortStatusMessage(final PortStatusMessage portStatus) {
        messageSpy.spyMessage(portStatus.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS);
        final TranslatorKey translatorKey = new TranslatorKey(portStatus.getVersion(), PortGrouping.class.getName());
        final MessageTranslator<PortGrouping, FlowCapableNodeConnector> messageTranslator = translatorLibrary.lookupTranslator(translatorKey);
        final FlowCapableNodeConnector flowCapableNodeConnector = messageTranslator.translate(portStatus, this, null);

        final KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> iiToNodeConnector = provideIIToNodeConnector(portStatus.getPortNo(), portStatus.getVersion());
        if (portStatus.getReason().equals(PortReason.OFPPRADD) || portStatus.getReason().equals(PortReason.OFPPRMODIFY)) {
            // because of ADD status node connector has to be created
            final NodeConnectorBuilder nConnectorBuilder = new NodeConnectorBuilder().setKey(iiToNodeConnector.getKey());
            nConnectorBuilder.addAugmentation(FlowCapableNodeConnectorStatisticsData.class, new FlowCapableNodeConnectorStatisticsDataBuilder().build());
            nConnectorBuilder.addAugmentation(FlowCapableNodeConnector.class, flowCapableNodeConnector);
            writeToTransaction(LogicalDatastoreType.OPERATIONAL, iiToNodeConnector, nConnectorBuilder.build());
        } else if (portStatus.getReason().equals(PortReason.OFPPRDELETE)) {
            addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, iiToNodeConnector);
        }
    }

    private KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> provideIIToNodeConnector(final long portNo, final short version) {
        final InstanceIdentifier<Node> iiToNodes = deviceState.getNodeInstanceIdentifier();
        final BigInteger dataPathId = deviceState.getFeatures().getDatapathId();
        final NodeConnectorId nodeConnectorId = NodeStaticReplyTranslatorUtil.nodeConnectorId(dataPathId.toString(), portNo, version);
        return iiToNodes.child(NodeConnector.class, new NodeConnectorKey(nodeConnectorId));
    }

    @Override
    public void processPacketInMessage(final PacketInMessage packetInMessage) {
        messageSpy.spyMessage(packetInMessage.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH);
        final ConnectionAdapter connectionAdapter = getPrimaryConnectionContext().getConnectionAdapter();

        final TranslatorKey translatorKey = new TranslatorKey(packetInMessage.getVersion(), PacketIn.class.getName());
        final MessageTranslator<PacketInMessage, PacketReceived> messageTranslator = translatorLibrary.lookupTranslator(translatorKey);
        final PacketReceived packetReceived = messageTranslator.translate(packetInMessage, this, null);

        if (packetReceived == null) {
            LOG.debug("Received a null packet from switch {}", connectionAdapter.getRemoteAddress());
            return;
        }

        if (!packetInLimiter.acquirePermit()) {
            LOG.debug("Packet limited");
            // TODO: save packet into emergency slot if possible
            // FIXME: some other counter
            messageSpy.spyMessage(packetReceived.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_SRC_FAILURE);
            return;
        }

        final ListenableFuture<? extends Object> offerNotification = notificationPublishService.offerNotification(packetReceived);
        if (NotificationPublishService.REJECTED.equals(offerNotification)) {
            LOG.debug("notification offer rejected");
            messageSpy.spyMessage(packetReceived.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_SRC_FAILURE);
            packetInLimiter.drainLowWaterMark();
            packetInLimiter.releasePermit();
            return;
        }

        Futures.addCallback(offerNotification, new FutureCallback<Object>() {
            @Override
            public void onSuccess(final Object result) {
                messageSpy.spyMessage(packetReceived.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS);
                packetInLimiter.releasePermit();
            }

            @Override
            public void onFailure(final Throwable t) {
                messageSpy.spyMessage(packetReceived.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_NOTIFICATION_REJECTED);
                LOG.debug("notification offer failed: {}", t.getMessage());
                LOG.trace("notification offer failed..", t);
                packetInLimiter.releasePermit();
            }
        });
    }

    @Override
    public TranslatorLibrary oook() {
        return translatorLibrary;
    }

    @Override
    public void setTranslatorLibrary(final TranslatorLibrary translatorLibrary) {
        this.translatorLibrary = translatorLibrary;
    }

    @Override
    public HashedWheelTimer getTimer() {
        return hashedWheelTimer;
    }

    @Override
    public void close() {
        deviceState.setValid(false);

        LOG.trace("Removing node {} from operational DS.", getDeviceState().getNodeId());
        addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, getDeviceState().getNodeInstanceIdentifier());

        deviceGroupRegistry.close();
        deviceFlowRegistry.close();
        deviceMeterRegistry.close();

        primaryConnectionContext.close();
        for (final ConnectionContext connectionContext : auxiliaryConnectionContexts.values()) {
            connectionContext.close();
        }

        for (final DeviceContextClosedHandler deviceContextClosedHandler : closeHandlers) {
            deviceContextClosedHandler.onDeviceContextClosed(this);
        }
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {
        if (getPrimaryConnectionContext().equals(connectionContext)) {
            try {
                close();
            } catch (final Exception e) {
                LOG.trace("Error closing device context.");
            }
            if (null != deviceDisconnectedHandler) {
                deviceDisconnectedHandler.onDeviceDisconnected(connectionContext);
            }
        } else {
            final SwitchConnectionDistinguisher connectionDistinguisher = createConnectionDistinguisher(connectionContext);
            auxiliaryConnectionContexts.remove(connectionDistinguisher);
        }
    }

    @Override
    public void setCurrentBarrierTimeout(final Timeout timeout) {
        barrierTaskTimeout = timeout;
    }

    @Override
    public Timeout getBarrierTaskTimeout() {
        return barrierTaskTimeout;
    }

    @Override
    public void setNotificationService(final NotificationService notificationServiceParam) {
        notificationService = notificationServiceParam;
    }

    @Override
    public void setNotificationPublishService(final NotificationPublishService notificationPublishService) {
        this.notificationPublishService = notificationPublishService;
    }

    @Override
    public MessageSpy getMessageSpy() {
        return messageSpy;
    }

    @Override
    public void setDeviceDisconnectedHandler(final DeviceDisconnectedHandler deviceDisconnectedHandler) {
        this.deviceDisconnectedHandler = deviceDisconnectedHandler;
    }

    @Override
    public void addDeviceContextClosedHandler(final DeviceContextClosedHandler deviceContextClosedHandler) {
        closeHandlers.add(deviceContextClosedHandler);
    }

    @Override
    public void startGatheringOperationsToOneTransaction() {
        txChainManager.startGatheringOperationsToOneTransaction();
    }

    @Override
    public void commitOperationsGatheredInOneTransaction() {
        txChainManager.commitOperationsGatheredInOneTransaction();
    }

    @Override
    public void onPublished() {
        primaryConnectionContext.getConnectionAdapter().setPacketInFiltering(false);
        for (final ConnectionContext switchAuxConnectionContext : auxiliaryConnectionContexts.values()) {
            switchAuxConnectionContext.getConnectionAdapter().setPacketInFiltering(false);
        }
    }

    @Override
    public MultiMsgCollector getMultiMsgCollector(final RequestContext<List<MultipartReply>> requestContext) {
        return new MultiMsgCollectorImpl(this, requestContext);
    }
}
