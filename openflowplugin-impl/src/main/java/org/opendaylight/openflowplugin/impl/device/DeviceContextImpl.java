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
import java.util.concurrent.ConcurrentHashMap;
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
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.registry.ItemLifeCycleRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleKeeper;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.common.ItemLifeCycleSourceImpl;
import org.opendaylight.openflowplugin.impl.common.NodeStaticReplyTranslatorUtil;
import org.opendaylight.openflowplugin.impl.device.listener.MultiMsgCollectorImpl;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.registry.group.DeviceGroupRegistryImpl;
import org.opendaylight.openflowplugin.impl.registry.meter.DeviceMeterRegistryImpl;
import org.opendaylight.openflowplugin.openflow.md.core.session.SwitchConnectionCookieOFImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
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

    // TODO: drain factor should be parametrized
    public static final float REJECTED_DRAIN_FACTOR = 0.25f;
    // TODO: low water mark factor should be parametrized
    private static final float LOW_WATERMARK_FACTOR = 0.75f;
    // TODO: high water mark factor should be parametrized
    private static final float HIGH_WATERMARK_FACTOR = 0.95f;

    private final ConnectionContext primaryConnectionContext;
    private final DeviceState deviceState;
    private final DataBroker dataBroker;
    private final HashedWheelTimer hashedWheelTimer;
    private final Map<SwitchConnectionDistinguisher, ConnectionContext> auxiliaryConnectionContexts;
    private final TransactionChainManager transactionChainManager;
    private final DeviceFlowRegistry deviceFlowRegistry;
    private final DeviceGroupRegistry deviceGroupRegistry;
    private final DeviceMeterRegistry deviceMeterRegistry;
    private final Collection<DeviceContextClosedHandler> closeHandlers = new HashSet<>();
    private final PacketInRateLimiter packetInLimiter;
    private final MessageSpy messageSpy;
    private final ItemLifeCycleKeeper flowLifeCycleKeeper;
    private NotificationPublishService notificationPublishService;
    private NotificationService notificationService;
    private final OutboundQueue outboundQueueProvider;
    private Timeout barrierTaskTimeout;
    private final MessageTranslator<PortGrouping, FlowCapableNodeConnector> portStatusTranslator;
    private final MessageTranslator<PacketInMessage, PacketReceived> packetInTranslator;
    private final MessageTranslator<FlowRemoved, org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved> flowRemovedTranslator;
    private final TranslatorLibrary translatorLibrary;
    private Map<Long, NodeConnectorRef> nodeConnectorCache;
    private ItemLifeCycleRegistry itemLifeCycleSourceRegistry;
    private RpcContext rpcContext;

    @VisibleForTesting
    DeviceContextImpl(@Nonnull final ConnectionContext primaryConnectionContext,
                      @Nonnull final DeviceState deviceState,
                      @Nonnull final DataBroker dataBroker,
                      @Nonnull final HashedWheelTimer hashedWheelTimer,
                      @Nonnull final MessageSpy _messageSpy,
                      @Nonnull final OutboundQueueProvider outboundQueueProvider,
                      @Nonnull final TranslatorLibrary translatorLibrary,
                      @Nonnull final TransactionChainManager transactionChainManager) {
        this.primaryConnectionContext = Preconditions.checkNotNull(primaryConnectionContext);
        this.deviceState = Preconditions.checkNotNull(deviceState);
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.hashedWheelTimer = Preconditions.checkNotNull(hashedWheelTimer);
        this.outboundQueueProvider = Preconditions.checkNotNull(outboundQueueProvider);
        this.transactionChainManager = Preconditions.checkNotNull(transactionChainManager);
        auxiliaryConnectionContexts = new HashMap<>();
        deviceFlowRegistry = new DeviceFlowRegistryImpl();
        deviceGroupRegistry = new DeviceGroupRegistryImpl();
        deviceMeterRegistry = new DeviceMeterRegistryImpl();
        messageSpy = _messageSpy;

        packetInLimiter = new PacketInRateLimiter(primaryConnectionContext.getConnectionAdapter(),
                /*initial*/ 1000, /*initial*/2000, messageSpy, REJECTED_DRAIN_FACTOR);

        this.translatorLibrary = translatorLibrary;
        portStatusTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceState.getVersion(), PortGrouping.class.getName()));
        packetInTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceState.getVersion(), PacketIn.class.getName()));
        flowRemovedTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceState.getVersion(), FlowRemoved.class.getName()));


        nodeConnectorCache = new ConcurrentHashMap<>();

        itemLifeCycleSourceRegistry = new ItemLifeCycleRegistryImpl();
        flowLifeCycleKeeper = new ItemLifeCycleSourceImpl();
        itemLifeCycleSourceRegistry.registerLifeCycleSource(flowLifeCycleKeeper);
    }

    /**
     * This method is called from {@link DeviceManagerImpl} only. So we could say "posthandshake process finish"
     * and we are able to set a scheduler for an automatic transaction submitting by time (0,5sec).
     */
    void initialSubmitTransaction() {
        transactionChainManager.initialSubmitWriteTransaction();
    }

    /**
     * This method is called fron
     */
    void cancelTransaction() {
        transactionChainManager.cancelWriteTransaction();
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
        transactionChainManager.writeToTransaction(store, path, data);
    }

    @Override
    public <T extends DataObject> void addDeleteToTxChain(final LogicalDatastoreType store, final InstanceIdentifier<T> path) {
        transactionChainManager.addDeleteOperationTotTxChain(store, path);
    }

    @Override
    public boolean submitTransaction() {
        return transactionChainManager.submitWriteTransaction();
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
        final ItemLifecycleListener itemLifecycleListener = flowLifeCycleKeeper.getItemLifecycleListener();
        if (itemLifecycleListener != null) {
            //1. translate to general flow (table, priority, match, cookie)
            final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved flowRemovedNotification =
                    flowRemovedTranslator.translate(flowRemoved, this, null);
            //2. create registry key
            FlowRegistryKey flowRegKey = FlowRegistryKeyFactory.create(flowRemovedNotification);
            //3. lookup flowId
            final FlowDescriptor flowDescriptor = deviceFlowRegistry.retrieveIdForFlow(flowRegKey);
            //4. if flowId present:
            if (flowDescriptor != null) {
                // a) construct flow path
                KeyedInstanceIdentifier<Flow, FlowKey> flowPath = getDeviceState().getNodeInstanceIdentifier()
                        .augmentation(FlowCapableNode.class)
                        .child(Table.class, flowDescriptor.getTableKey())
                        .child(Flow.class, new FlowKey(flowDescriptor.getFlowId()));
                // b) notify listener
                itemLifecycleListener.onRemoved(flowPath);
            } else {
                LOG.debug("flow id not found: nodeId={} tableId={}, priority={}",
                        getDeviceState().getNodeId(), flowRegKey.getTableId(), flowRemovedNotification.getPriority());
            }
        }
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
        submitTransaction();
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
        final PacketReceived packetReceived = packetInTranslator.translate(packetInMessage, this, null);

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
    public HashedWheelTimer getTimer() {
        return hashedWheelTimer;
    }

    @Override
    public void close() {
        LOG.debug("closing deviceContext: {}, nodeId:{}",
                getPrimaryConnectionContext().getConnectionAdapter().getRemoteAddress(),
                getDeviceState().getNodeId());

        tearDown();

        primaryConnectionContext.closeConnection(false);
    }

    private void tearDown() {
        deviceState.setValid(false);

        for (final ConnectionContext connectionContext : auxiliaryConnectionContexts.values()) {
            connectionContext.closeConnection(false);
        }

        deviceGroupRegistry.close();
        deviceFlowRegistry.close();
        deviceMeterRegistry.close();

        itemLifeCycleSourceRegistry.clear();


        for (final DeviceContextClosedHandler deviceContextClosedHandler : closeHandlers) {
            deviceContextClosedHandler.onDeviceContextClosed(this);
        }

        LOG.info("Closing transaction chain manager without cleaning inventory operational");
        transactionChainManager.closeWithoutCleanup();
    }

    @Override
    public void onDeviceDisconnectedFromCluster() {
        LOG.info("Removing device from operational and closing transaction Manager for device:{}", getDeviceState().getNodeId());
        transactionChainManager.close();
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {
        if (getPrimaryConnectionContext().equals(connectionContext)) {
            try {
                tearDown();
            } catch (final Exception e) {
                LOG.trace("Error closing device context.");
            }
        } else {
            LOG.debug("auxiliary connection dropped: {}, nodeId:{}",
                    connectionContext.getConnectionAdapter().getRemoteAddress(),
                    getDeviceState().getNodeId());
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
    public void addDeviceContextClosedHandler(final DeviceContextClosedHandler deviceContextClosedHandler) {
        closeHandlers.add(deviceContextClosedHandler);
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

    @Override
    public NodeConnectorRef lookupNodeConnectorRef(Long portNumber) {
        return nodeConnectorCache.get(portNumber);
    }

    @Override
    public void storeNodeConnectorRef(final Long portNumber, final NodeConnectorRef nodeConnectorRef) {
        nodeConnectorCache.put(
                Preconditions.checkNotNull(portNumber),
                Preconditions.checkNotNull(nodeConnectorRef));
    }

    @Override
    public void updatePacketInRateLimit(long upperBound) {
        packetInLimiter.changeWaterMarks((int) (LOW_WATERMARK_FACTOR * upperBound), (int) (HIGH_WATERMARK_FACTOR * upperBound));
    }

    @Override
    public ItemLifeCycleRegistry getItemLifeCycleSourceRegistry() {
        return itemLifeCycleSourceRegistry;
    }

    @Override
    public void setRpcContext(RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    @Override
    public RpcContext getRpcContext() {
        return rpcContext;
    }


}
