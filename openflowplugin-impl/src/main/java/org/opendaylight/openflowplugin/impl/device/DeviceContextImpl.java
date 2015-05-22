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
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.DeviceRequestFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
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
import org.opendaylight.openflowplugin.api.openflow.translator.TranslatorLibrarian;
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
    public static final String DEVICE_DISCONNECTED = "Device disconnected.";

    private final ConnectionContext primaryConnectionContext;
    private final DeviceState deviceState;
    private final DataBroker dataBroker;
    private final HashedWheelTimer hashedWheelTimer;
    private final Map<SwitchConnectionDistinguisher, ConnectionContext> auxiliaryConnectionContexts;
    private final TransactionChainManager txChainManager;
    private final DeviceFlowRegistry deviceFlowRegistry;
    private final DeviceGroupRegistry deviceGroupRegistry;
    private final DeviceMeterRegistry deviceMeterRegistry;
    private Timeout barrierTaskTimeout;
    private NotificationService notificationService;
    private final MessageSpy messageSpy;
    private DeviceDisconnectedHandler deviceDisconnectedHandler;
    private final Collection<DeviceContextClosedHandler> closeHandlers = new HashSet<>();
    private NotificationPublishService notificationPublishService;
    private OutboundQueue outboundQueueProvider;
    private final MultiMsgCollector multiMsgCollector = new MultiMsgCollectorImpl();
    private final MessageTranslator<PortGrouping, FlowCapableNodeConnector> portStatusTranslator;
    private final MessageTranslator<PacketInMessage, PacketReceived> packetInTranslator;

    private final AtomicInteger outstandingNotificationCounter = new AtomicInteger();

    private final Object packetInLock = new Object();
    @GuardedBy("packetInLock")
    private boolean packetInSuspended = false;
    private volatile int resumePacketInWaterMark;
    private OutboundQueueHandlerRegistration outboundQueueHandlerRegistration;

    @Override
    public MultiMsgCollector getMultiMsgCollector() {
        return multiMsgCollector;
    }

    @Override
    public Long getReservedXid() {
        return outboundQueueProvider.reserveEntry();
    }

    @VisibleForTesting
    DeviceContextImpl(@Nonnull final ConnectionContext primaryConnectionContext,
                      @Nonnull final DeviceState deviceState,
                      @Nonnull final DataBroker dataBroker,
                      @Nonnull final HashedWheelTimer hashedWheelTimer,
                      @Nonnull final MessageSpy _messageSpy,
                      @Nonnull final TranslatorLibrarian librarian) {
        this.primaryConnectionContext = Preconditions.checkNotNull(primaryConnectionContext);
        this.deviceState = Preconditions.checkNotNull(deviceState);
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.hashedWheelTimer = Preconditions.checkNotNull(hashedWheelTimer);
        txChainManager = new TransactionChainManager(dataBroker, hashedWheelTimer, 500L, 500L);
        auxiliaryConnectionContexts = new HashMap<>();
        deviceFlowRegistry = new DeviceFlowRegistryImpl();
        deviceGroupRegistry = new DeviceGroupRegistryImpl();
        deviceMeterRegistry = new DeviceMeterRegistryImpl();
        messageSpy = _messageSpy;
        multiMsgCollector.setDeviceReplyProcessor(this);

        final TranslatorLibrary translatorLibrary = librarian.oook();
        portStatusTranslator = translatorLibrary.lookupTranslator(
            new TranslatorKey(deviceState.getVersion(), PortGrouping.class.getName()));
        packetInTranslator = translatorLibrary.lookupTranslator(
            new TranslatorKey(deviceState.getVersion(), PacketIn.class.getName()));
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
    public void processException(final Xid xid, final DeviceRequestFailedException deviceRequestFailedException) {
        messageSpy.spyMessage(deviceRequestFailedException.getClass(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE);
    }

    @Override
    public void processFlowRemovedMessage(final FlowRemoved flowRemoved) {
        //TODO: will be defined later
    }

    @Override
    public void processPortStatusMessage(final PortStatusMessage portStatus) {
        messageSpy.spyMessage(portStatus.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS);
        final FlowCapableNodeConnector flowCapableNodeConnector = portStatusTranslator.translate(portStatus, this, null);

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

    /**
     * Enable packetIn filtering on a connection adapter.
     *
     * @param connectionAdapter
     * @param refCount reference count read before calling in
     * @return True if the method succeeded, false if the notification publish
     *         process should be retried.
     */
    private boolean enablePacketInFiltering(final ConnectionAdapter connectionAdapter, final int refCount) {
        synchronized (packetInLock) {
            final int currentRefcount = outstandingNotificationCounter.get();
            if (currentRefcount != refCount) {
                LOG.trace("Raced with notification completion {} -> {}", refCount, currentRefcount);
                return true;
            }

            if (packetInSuspended) {
                LOG.debug("Filtering on {} already enabled", connectionAdapter.getRemoteAddress());
                return false;
            }

            // We must not filter packetIns if we do not have anything in the queue,
            // because we would have no stimulus which would re-enable it.
            if (currentRefcount != 0) {
                messageSpy.spyMessage(DeviceContext.class, MessageSpy.STATISTIC_GROUP.OFJ_BACKPRESSURE_ON);
                connectionAdapter.setPacketInFiltering(true);

                // TODO: calculate the watermark at which packetIn filtering should be disabled
                final int newWater = 0;
                LOG.debug("Enabled filterin with {} outstanding requests, new resume watermark is {}", currentRefcount, newWater);
                resumePacketInWaterMark = newWater;
                packetInSuspended = true;
            } else {
                LOG.debug("No outstanding notifications from {}, cannot enable PacketIn filtering", connectionAdapter.getRemoteAddress());
            }
        }

        return false;
    }

    private void disablePacketInFiltering(final ConnectionAdapter connectionAdapter) {
        // TODO: while still holding the reference, attempt to clear the emergency slot,
        //       if that succeeds, do not go into the rest of the method

        if (outstandingNotificationCounter.decrementAndGet() == resumePacketInWaterMark) {
            synchronized (packetInLock) {
                if (packetInSuspended) {
                    messageSpy.spyMessage(DeviceContext.class, MessageSpy.STATISTIC_GROUP.OFJ_BACKPRESSURE_OFF);
                    connectionAdapter.setPacketInFiltering(false);
                    packetInSuspended = false;
                } else {
                    LOG.debug("Filtering on {} already disabled", connectionAdapter.getRemoteAddress());
                }
            }
        }
    }

    @Override
    public void processPacketInMessage(final PacketInMessage packetInMessage) {
        messageSpy.spyMessage(packetInMessage.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH);
        final ConnectionAdapter connectionAdapter = getPrimaryConnectionContext().getConnectionAdapter();
        final PacketReceived packetReceived = packetInTranslator.translate(packetInMessage, this, null);

        if (packetReceived == null) {
            LOG.debug("Received a null packet from switch {}", connectionAdapter.getRemoteAddress());
            return;
        }

        final ListenableFuture<? extends Object> offerNotification;
        for (;;) {
            final ListenableFuture<? extends Object> tryOffer = notificationPublishService.offerNotification(packetReceived);
            if (!NotificationPublishService.REJECTED.equals(tryOffer)) {
                offerNotification = tryOffer;
                break;
            }

            LOG.debug("notification offer rejected");
            final int refCount = outstandingNotificationCounter.get();
            if (enablePacketInFiltering(connectionAdapter, refCount)) {
                // TODO: save packet into emergency slot if possible
                messageSpy.spyMessage(packetReceived.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_SRC_FAILURE);
                return;
            }

            LOG.debug("Notification completion ocurred, retry publishing");
        }

        outstandingNotificationCounter.incrementAndGet();
        Futures.addCallback(offerNotification, new FutureCallback<Object>() {
            @Override
            public void onSuccess(final Object result) {
                messageSpy.spyMessage(packetReceived.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS);
                disablePacketInFiltering(connectionAdapter);
            }

            @Override
            public void onFailure(final Throwable t) {
                messageSpy.spyMessage(packetReceived.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_NOTIFICATION_REJECTED);
                LOG.debug("notification offer failed: {}, outstanding: {}", t.getMessage(), outstandingNotificationCounter);
                LOG.trace("notification offer failed..", t);
                disablePacketInFiltering(connectionAdapter);
            }
        });
    }

    @Override
    public HashedWheelTimer getTimer() {
        return hashedWheelTimer;
    }

    @Override
    public void close() {
        deviceState.setValid(false);

        outboundQueueHandlerRegistration.close();

        LOG.trace("Removing node {} from operational DS.", getDeviceState().getNodeId());
        addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, getDeviceState().getNodeInstanceIdentifier());

        deviceGroupRegistry.close();
        deviceFlowRegistry.close();
        deviceMeterRegistry.close();

        if (primaryConnectionContext.getConnectionAdapter().isAlive()) {
            primaryConnectionContext.setConnectionState(ConnectionContext.CONNECTION_STATE.RIP);
            primaryConnectionContext.getConnectionAdapter().disconnect();
        }
        for (final ConnectionContext connectionContext : auxiliaryConnectionContexts.values()) {
            if (connectionContext.getConnectionAdapter().isAlive()) {
                connectionContext.getConnectionAdapter().disconnect();
            }
        }
        for (final DeviceContextClosedHandler deviceContextClosedHandler : closeHandlers) {
            deviceContextClosedHandler.onDeviceContextClosed(this);
        }
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {
        if (this.getPrimaryConnectionContext().equals(connectionContext)) {
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
        this.closeHandlers.add(deviceContextClosedHandler);
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
        for (ConnectionContext switchAuxConnectionContext : auxiliaryConnectionContexts.values()) {
            switchAuxConnectionContext.getConnectionAdapter().setPacketInFiltering(false);
        }
    }

    @Override
    public void registerOutboundQueueProvider(final OutboundQueueProvider outboundQueueProvider, final int maxQueueDepth, final long barrierNanos) {
        final ConnectionAdapter primaryConnectionAdapter = primaryConnectionContext.getConnectionAdapter();
        outboundQueueHandlerRegistration = primaryConnectionAdapter.registerOutboundQueueHandler(outboundQueueProvider, maxQueueDepth, barrierNanos);
        this.outboundQueueProvider = outboundQueueProvider;
        primaryConnectionContext.setOutboundQueueProvider(outboundQueueProvider);
    }

}
