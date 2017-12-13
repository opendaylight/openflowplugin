/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MastershipChangeListener;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.common.txchain.TransactionChainManager;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterProviderKeeper;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.extension.api.path.MessagePath;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.impl.device.initialization.AbstractDeviceInitializer;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProvider;
import org.opendaylight.openflowplugin.impl.device.listener.MultiMsgCollectorImpl;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.registry.group.DeviceGroupRegistryImpl;
import org.opendaylight.openflowplugin.impl.registry.meter.DeviceMeterRegistryImpl;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.services.util.RequestContextUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.ExperimenterMessageFromDevBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceContextImpl implements DeviceContext, ExtensionConverterProviderKeeper {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceContextImpl.class);

    // TODO: drain factor should be parametrized
    private static final float REJECTED_DRAIN_FACTOR = 0.25f;
    // TODO: low water mark factor should be parametrized
    private static final float LOW_WATERMARK_FACTOR = 0.75f;
    // TODO: high water mark factor should be parametrized
    private static final float HIGH_WATERMARK_FACTOR = 0.95f;

    // Timeout in seconds after what we will give up on propagating role
    private static final int SET_ROLE_TIMEOUT = 10;

    // Timeout in milliseconds after what we will give up on initializing device
    private static final int DEVICE_INIT_TIMEOUT = 9000;

    // Timeout in milliseconds after what we will give up on closing transaction chain
    private static final int TX_CHAIN_CLOSE_TIMEOUT = 10000;

    // Timeout after what we will give up on waiting for master role
    private static final long CHECK_ROLE_MASTER_TIMEOUT = 20000;

    private static final int LOW_WATERMARK = 1000;
    private static final int HIGH_WATERMARK = 2000;
    private final MultipartWriterProvider writerProvider;
    private final HashedWheelTimer hashedWheelTimer;
    private final DeviceState deviceState;
    private final DataBroker dataBroker;
    private final Collection<RequestContext<?>> requestContexts = new HashSet<>();
    private final MessageSpy messageSpy;
    private final MessageTranslator<PortGrouping, FlowCapableNodeConnector> portStatusTranslator;
    private final MessageTranslator<PacketInMessage, PacketReceived> packetInTranslator;
    private final MessageTranslator<FlowRemoved, org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved> flowRemovedTranslator;
    private final TranslatorLibrary translatorLibrary;
    private final ConvertorExecutor convertorExecutor;
    private final DeviceManager myManager;
    private final DeviceInitializerProvider deviceInitializerProvider;
    private final PacketInRateLimiter packetInLimiter;
    private final DeviceInfo deviceInfo;
    private final ConnectionContext primaryConnectionContext;
    private final boolean useSingleLayerSerialization;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean hasState = new AtomicBoolean(false);
    private final AtomicBoolean isInitialTransactionSubmitted = new AtomicBoolean(false);
    private final AtomicReference<CONTEXT_STATE> state = new AtomicReference<>(CONTEXT_STATE.INITIALIZATION);
    private final HashedWheelTimer timer;
    private final Timeout slaveTask;
    private Timeout barrierTaskTimeout;
    private NotificationPublishService notificationPublishService;
    private TransactionChainManager transactionChainManager;
    private DeviceFlowRegistry deviceFlowRegistry;
    private DeviceGroupRegistry deviceGroupRegistry;
    private DeviceMeterRegistry deviceMeterRegistry;
    private ExtensionConverterProvider extensionConverterProvider;
    private SalRoleService salRoleService;
    private boolean skipTableFeatures;
    private boolean switchFeaturesMandatory;
    private MastershipChangeListener mastershipChangeListener;
    private AtomicReference<ListenableFuture<RpcResult<SetRoleOutput>>> lastRoleFuture = new AtomicReference<>(
            Futures.immediateFuture(null));

    DeviceContextImpl(
            @Nonnull final ConnectionContext primaryConnectionContext,
            @Nonnull final DataBroker dataBroker,
            @Nonnull final MessageSpy messageSpy,
            @Nonnull final TranslatorLibrary translatorLibrary,
            @Nonnull final DeviceManager contextManager,
            final ConvertorExecutor convertorExecutor,
            final boolean skipTableFeatures,
            final HashedWheelTimer hashedWheelTimer,
            final boolean useSingleLayerSerialization,
            final DeviceInitializerProvider deviceInitializerProvider,
            final HashedWheelTimer timer) {

        this.primaryConnectionContext = primaryConnectionContext;
        this.deviceInfo = primaryConnectionContext.getDeviceInfo();
        this.hashedWheelTimer = hashedWheelTimer;
        this.deviceInitializerProvider = deviceInitializerProvider;
        this.myManager = contextManager;
        this.deviceState = new DeviceStateImpl();
        this.dataBroker = dataBroker;
        this.messageSpy = messageSpy;

        this.packetInLimiter = new PacketInRateLimiter(primaryConnectionContext.getConnectionAdapter(),
                /*initial*/ LOW_WATERMARK, /*initial*/HIGH_WATERMARK, this.messageSpy, REJECTED_DRAIN_FACTOR);

        this.translatorLibrary = translatorLibrary;
        this.portStatusTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceInfo.getVersion(), PortGrouping.class.getName()));
        this.packetInTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceInfo.getVersion(), PacketIn.class.getName()));
        this.flowRemovedTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceInfo.getVersion(), FlowRemoved.class.getName()));

        this.convertorExecutor = convertorExecutor;
        this.skipTableFeatures = skipTableFeatures;
        this.useSingleLayerSerialization = useSingleLayerSerialization;
        writerProvider = MultipartWriterProviderFactory.createDefaultProvider(this);

        this.timer = timer;
        slaveTask = timer.newTimeout((t) -> makeDeviceSlave(), CHECK_ROLE_MASTER_TIMEOUT, TimeUnit.MILLISECONDS);

        LOG.info("Started timer for setting SLAVE role on device {} if no role will be set in {}s.",
                deviceInfo,
                CHECK_ROLE_MASTER_TIMEOUT / 1000L);
    }

    @Override
    public boolean initialSubmitTransaction() {
        if (!initialized.get()) {
            return false;
        }

        final boolean initialSubmit = transactionChainManager.initialSubmitWriteTransaction();
        isInitialTransactionSubmitted.set(initialSubmit);
        return initialSubmit;
    }

    @Override
    public DeviceState getDeviceState() {
        return deviceState;
    }

    @Override
    public ReadOnlyTransaction getReadTransaction() {
        return dataBroker.newReadOnlyTransaction();
    }

    @Override
    public boolean isTransactionsEnabled() {
        return isInitialTransactionSubmitted.get();
    }

    @Override
    public <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                          final InstanceIdentifier<T> path,
                                                          final T data){
        if (initialized.get()) {
            transactionChainManager.writeToTransaction(store, path, data, false);
        }
    }

    @Override
    public <T extends DataObject> void writeToTransactionWithParentsSlow(final LogicalDatastoreType store,
                                                                         final InstanceIdentifier<T> path,
                                                                         final T data){
        if (initialized.get()) {
            transactionChainManager.writeToTransaction(store, path, data, true);
        }
    }

    @Override
    public <T extends DataObject> void addDeleteToTxChain(final LogicalDatastoreType store, final InstanceIdentifier<T> path) {
        if (initialized.get()) {
            transactionChainManager.addDeleteOperationToTxChain(store, path);
        }
    }

    @Override
    public boolean submitTransaction() {
        return initialized.get() && transactionChainManager.submitTransaction();
    }

    @Override
    public ConnectionContext getPrimaryConnectionContext() {
        return primaryConnectionContext;
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
        messageSpy.spyMessage(
                ofHeader.getImplementedInterface(),
                (ofHeader instanceof Error)
                        ? MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE
                        : MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS);
    }

    @Override
    public void processReply(final Xid xid, final List<? extends OfHeader> ofHeaderList) {
        ofHeaderList.forEach(header -> messageSpy.spyMessage(
                header.getImplementedInterface(),
                (header instanceof Error)
                        ? MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE
                        : MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Override
    public void processFlowRemovedMessage(final FlowRemoved flowRemoved) {
        //1. translate to general flow (table, priority, match, cookie)
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved flowRemovedNotification =
                flowRemovedTranslator.translate(flowRemoved, deviceInfo, null);
        LOG.debug("For nodeId={} isNotificationFlowRemovedOn={}", getDeviceInfo().getLOGValue(), myManager.isFlowRemovedNotificationOn());
        if (myManager.isFlowRemovedNotificationOn()) {
            // Trigger off a notification
            notificationPublishService.offerNotification(flowRemovedNotification);
        }

        //2. create registry key
        final FlowRegistryKey flowRegKey = FlowRegistryKeyFactory.create(getDeviceInfo().getVersion(), flowRemovedNotification);

        //3. lookup flowId
        final FlowDescriptor flowDescriptor = deviceFlowRegistry.retrieveDescriptor(flowRegKey);

        //4. if flowId present:
        if (flowDescriptor != null) {
            // a) construct flow path and delete from inventory if statistics is enabled
            if (myManager.isStatisticsPollingOn()) {
                final KeyedInstanceIdentifier<Flow, FlowKey> flowPath = getDeviceInfo().getNodeInstanceIdentifier()
                        .augmentation(FlowCapableNode.class)
                        .child(Table.class, flowDescriptor.getTableKey())
                        .child(Flow.class, new FlowKey(flowDescriptor.getFlowId()));
                addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, flowPath);
                submitTransaction();
            }
            deviceFlowRegistry.addMark(flowRegKey);
        } else {
            LOG.debug("flow id not found: nodeId={} tableId={}, priority={}",
                    getDeviceInfo().getNodeId(), flowRegKey.getTableId(), flowRemovedNotification.getPriority());
        }
    }

    @Override
    public void processPortStatusMessage(final PortStatusMessage portStatus) {
        messageSpy.spyMessage(portStatus.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS);

        if (initialized.get()) {
            try {
                writePortStatusMessage(portStatus);
                LOG.debug("processPortStatusMessage ::  submit transaction for write port status message {} ",
                        portStatus);
                submitTransaction();
            } catch (final Exception e) {
                LOG.error("Error processing port status message for port {} on device {}",
                        portStatus.getPortNo(), getDeviceInfo().getLOGValue(), e);
            }
        } else if (!hasState.get()) {
            primaryConnectionContext.handlePortStatusMessage(portStatus);
        }
    }

    private void writePortStatusMessage(final PortStatus portStatusMessage) {
        LOG.debug("writePortStatusMessage for port  {} ",portStatusMessage);
        final FlowCapableNodeConnector flowCapableNodeConnector = portStatusTranslator
                .translate(portStatusMessage, getDeviceInfo(), null);

        final KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> iiToNodeConnector = getDeviceInfo()
                .getNodeInstanceIdentifier()
                .child(NodeConnector.class, new NodeConnectorKey(InventoryDataServiceUtil
                        .nodeConnectorIdfromDatapathPortNo(
                                deviceInfo.getDatapathId(),
                                portStatusMessage.getPortNo(),
                                OpenflowVersion.get(deviceInfo.getVersion()))));

        if (PortReason.OFPPRADD.equals(portStatusMessage.getReason()) || PortReason.OFPPRMODIFY.equals(portStatusMessage.getReason())) {
            // because of ADD status node connector has to be created
            writeToTransaction(LogicalDatastoreType.OPERATIONAL, iiToNodeConnector, new NodeConnectorBuilder()
                    .setKey(iiToNodeConnector.getKey())
                    .addAugmentation(FlowCapableNodeConnectorStatisticsData.class, new FlowCapableNodeConnectorStatisticsDataBuilder().build())
                    .addAugmentation(FlowCapableNodeConnector.class, flowCapableNodeConnector)
                    .build());
        } else if (PortReason.OFPPRDELETE.equals(portStatusMessage.getReason())) {
            LOG.debug("addDeleteToTxChain for port reason being same for node {} ",iiToNodeConnector);
            addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, iiToNodeConnector);
        }
    }

    @Override
    public void processPacketInMessage(final PacketInMessage packetInMessage) {
        messageSpy.spyMessage(packetInMessage.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH);
        final ConnectionAdapter connectionAdapter = getPrimaryConnectionContext().getConnectionAdapter();
        final PacketReceived packetReceived = packetInTranslator.translate(packetInMessage, getDeviceInfo(), null);

        if (packetReceived == null) {
            LOG.debug("Received a null packet from switch {}", connectionAdapter.getRemoteAddress());
            messageSpy.spyMessage(packetInMessage.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_SRC_FAILURE);
            return;
        } else {
            messageSpy.spyMessage(packetReceived.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_OUT_SUCCESS);
        }

        if (!packetInLimiter.acquirePermit()) {
            LOG.debug("Packet limited");
            // TODO: save packet into emergency slot if possible
            messageSpy.spyMessage(packetReceived.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PACKET_IN_LIMIT_REACHED_AND_DROPPED);
            return;
        }

        final ListenableFuture<?> offerNotification = notificationPublishService.offerNotification(packetReceived);
        if (NotificationPublishService.REJECTED.equals(offerNotification)) {
            LOG.debug("notification offer rejected");
            messageSpy.spyMessage(packetReceived.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_NOTIFICATION_REJECTED);
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
    public void processExperimenterMessage(final ExperimenterMessage notification) {
        // lookup converter
        final ExperimenterDataOfChoice vendorData = notification.getExperimenterDataOfChoice();
        final MessageTypeKey<? extends ExperimenterDataOfChoice> key = new MessageTypeKey<>(
                getDeviceInfo().getVersion(),
                (Class<? extends ExperimenterDataOfChoice>) vendorData.getImplementedInterface());
        final ConvertorMessageFromOFJava<ExperimenterDataOfChoice, MessagePath> messageConverter = extensionConverterProvider.getMessageConverter(key);
        if (messageConverter == null) {
            LOG.warn("custom converter for {}[OF:{}] not found",
                    notification.getExperimenterDataOfChoice().getImplementedInterface(),
                    getDeviceInfo().getVersion());
            return;
        }
        // build notification
        final ExperimenterMessageOfChoice messageOfChoice;
        try {
            messageOfChoice = messageConverter.convert(vendorData, MessagePath.MESSAGE_NOTIFICATION);
            final ExperimenterMessageFromDevBuilder experimenterMessageFromDevBld = new ExperimenterMessageFromDevBuilder()
                    .setNode(new NodeRef(getDeviceInfo().getNodeInstanceIdentifier()))
                    .setExperimenterMessageOfChoice(messageOfChoice);
            // publish
            notificationPublishService.offerNotification(experimenterMessageFromDevBld.build());
        } catch (final ConversionException e) {
            LOG.error("Conversion of experimenter notification failed", e);
        }
    }

    @Override
    public TranslatorLibrary oook() {
        return translatorLibrary;
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
    public void setNotificationPublishService(final NotificationPublishService notificationPublishService) {
        this.notificationPublishService = notificationPublishService;
    }

    @Override
    public MessageSpy getMessageSpy() {
        return messageSpy;
    }

    @Override
    public void onPublished() {
        Verify.verify(CONTEXT_STATE.INITIALIZATION.equals(state.get()));
        state.set(CONTEXT_STATE.WORKING);
        primaryConnectionContext.getConnectionAdapter().setPacketInFiltering(false);
    }

    @Override
    public <T extends OfHeader> MultiMsgCollector<T> getMultiMsgCollector(final RequestContext<List<T>> requestContext) {
        return new MultiMsgCollectorImpl<>(this, requestContext);
    }

    @Override
    public void updatePacketInRateLimit(final long upperBound) {
        packetInLimiter.changeWaterMarks((int) (LOW_WATERMARK_FACTOR * upperBound), (int) (HIGH_WATERMARK_FACTOR * upperBound));
    }

    @Override
    public void setExtensionConverterProvider(final ExtensionConverterProvider extensionConverterProvider) {
        this.extensionConverterProvider = extensionConverterProvider;
    }

    @Override
    public ExtensionConverterProvider getExtensionConverterProvider() {
        return extensionConverterProvider;
    }

    @VisibleForTesting
    TransactionChainManager getTransactionChainManager() {
        return this.transactionChainManager;
    }

    @Override
    public void setSwitchFeaturesMandatory(boolean switchFeaturesMandatory) {
        this.switchFeaturesMandatory = switchFeaturesMandatory;
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        LOG.info("Stopping device context cluster services for node {}", deviceInfo.getLOGValue());
        slaveTask.cancel();
        changeLastRoleFuture(null);

        final ListenableFuture<Void> listenableFuture = initialized.get()
                ? transactionChainManager.deactivateTransactionManager()
                : Futures.immediateFuture(null);

        hashedWheelTimer.newTimeout((t) -> {
            if (!listenableFuture.isDone() && !listenableFuture.isCancelled()) {
                listenableFuture.cancel(true);
            }
        }, TX_CHAIN_CLOSE_TIMEOUT, TimeUnit.MILLISECONDS);

        return listenableFuture;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    @Override
    public void registerMastershipChangeListener(@Nonnull final MastershipChangeListener mastershipChangeListener) {
        this.mastershipChangeListener = mastershipChangeListener;
    }

    @Nonnull
    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }

    @Override
    public void close() {
        if (CONTEXT_STATE.TERMINATION.equals(state.get())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("DeviceContext for node {} is already in TERMINATION state.", getDeviceInfo());
            }

            return;
        }

        state.set(CONTEXT_STATE.TERMINATION);
        slaveTask.cancel();
        changeLastRoleFuture(null);

        // Close all datastore registries and transactions
        if (initialized.get()) {
            initialized.set(false);
            deviceGroupRegistry.close();
            deviceFlowRegistry.close();
            deviceMeterRegistry.close();

            final ListenableFuture<Void> txChainShuttingDown = transactionChainManager.shuttingDown();

            try {
                txChainShuttingDown.get(TX_CHAIN_CLOSE_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                txChainShuttingDown.cancel(true);
                LOG.error("Failed to shut down transaction chain for device {}: {}", deviceInfo, e);
            }

            transactionChainManager.close();
            transactionChainManager = null;
        }

        requestContexts.forEach(requestContext -> RequestContextUtil
                .closeRequestContextWithRpcError(requestContext, "Connection closed."));
        requestContexts.clear();
    }

    @Override
    public boolean canUseSingleLayerSerialization() {
        return useSingleLayerSerialization && getDeviceInfo().getVersion() >= OFConstants.OFP_VERSION_1_3;
    }

    @Override
    public void setSalRoleService(@Nonnull SalRoleService salRoleService) {
        this.salRoleService = salRoleService;
    }

    @Override
    public void instantiateServiceInstance() {
        LOG.info("Starting device context cluster services for node {}", deviceInfo);
        lazyTransactionManagerInitialization();

        try {
            final List<PortStatusMessage> portStatusMessages = primaryConnectionContext
                    .retrieveAndClearPortStatusMessages();
            LOG.debug("instantiateServiceInstance for port status message {} ",portStatusMessages);
            portStatusMessages.forEach(this::writePortStatusMessage);
            submitTransaction();
        } catch (final Exception ex) {
            throw new RuntimeException(String.format("Error processing port status messages from device %s: %s",
                    deviceInfo.toString(),
                    ex.toString()));
        }

        final java.util.Optional<AbstractDeviceInitializer> initializer = deviceInitializerProvider
                .lookup(deviceInfo.getVersion());

        if (initializer.isPresent()) {
            final Future<Void> initialize = initializer
                    .get()
                    .initialize(this, switchFeaturesMandatory, skipTableFeatures, writerProvider, convertorExecutor);

            try {
                initialize.get(DEVICE_INIT_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ex) {
                initialize.cancel(true);
                throw new RuntimeException(String.format("Failed to initialize device %s in %ss: %s",
                        deviceInfo.toString(),
                        String.valueOf(DEVICE_INIT_TIMEOUT / 1000),
                        ex.toString()));
            } catch (ExecutionException | InterruptedException ex) {
                throw new RuntimeException(String.format("Device %s cannot be initialized: %s",
                        deviceInfo.toString(),
                        ex.toString()));
            }
        } else {
            throw new RuntimeException(String.format("Unsupported version %s for device %s",
                    deviceInfo.getVersion(),
                    deviceInfo.toString()));
        }

        Futures.addCallback(persistSendRoleChangeToDevice(OfpRole.BECOMEMASTER),
                new RpcResultFutureCallback(mastershipChangeListener));

        final ListenableFuture<List<Optional<FlowCapableNode>>> deviceFlowRegistryFill = getDeviceFlowRegistry().fill();
        Futures.addCallback(deviceFlowRegistryFill,
                new DeviceFlowRegistryCallback(deviceFlowRegistryFill, mastershipChangeListener));
    }

    @VisibleForTesting
    void lazyTransactionManagerInitialization() {
        if (!this.initialized.get()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Transaction chain manager for node {} created", deviceInfo.getLOGValue());
            }
            this.transactionChainManager = new TransactionChainManager(dataBroker, deviceInfo.getNodeId().getValue());
            this.deviceFlowRegistry = new DeviceFlowRegistryImpl(deviceInfo.getVersion(), dataBroker, deviceInfo.getNodeInstanceIdentifier());
            this.deviceGroupRegistry = new DeviceGroupRegistryImpl();
            this.deviceMeterRegistry = new DeviceMeterRegistryImpl();
        }

        transactionChainManager.activateTransactionManager();
        initialized.set(true);
    }

    @Nullable
    @Override
    public <T> RequestContext<T> createRequestContext() {
        final Long xid = deviceInfo.reserveXidForDeviceMessage();

        final AbstractRequestContext<T> abstractRequestContext = new AbstractRequestContext<T>(xid) {
            @Override
            public void close() {
                requestContexts.remove(this);
            }
        };

        requestContexts.add(abstractRequestContext);
        return abstractRequestContext;
    }

    private ListenableFuture<RpcResult<SetRoleOutput>> persistSendRoleChangeToDevice(final OfpRole newRole) {
        slaveTask.cancel();
        final ListenableFuture<RpcResult<SetRoleOutput>> newFuture = sendRoleChangeToDevice(newRole);
        changeLastRoleFuture(newFuture);
        return newFuture;
    }

    private void changeLastRoleFuture(final ListenableFuture<RpcResult<SetRoleOutput>> newFuture) {
        lastRoleFuture.getAndUpdate(lastFuture -> {
            if (Objects.nonNull(lastFuture) && !lastFuture.isCancelled() && !lastFuture.isDone()) {
                lastFuture.cancel(true);
            }

            return newFuture;
        });
    }

    private ListenableFuture<RpcResult<SetRoleOutput>> sendRoleChangeToDevice(final OfpRole newRole) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending new role {} to device {}", newRole, deviceInfo.getNodeId());
        }

        if (deviceInfo.getVersion() >= OFConstants.OFP_VERSION_1_3) {
            final SetRoleInput setRoleInput = (new SetRoleInputBuilder()).setControllerRole(newRole)
                    .setNode(new NodeRef(deviceInfo.getNodeInstanceIdentifier())).build();

            final Future<RpcResult<SetRoleOutput>> setRoleOutputFuture = this.salRoleService.setRole(setRoleInput);

            final TimerTask timerTask = timeout -> {
                if (!setRoleOutputFuture.isDone()) {
                    LOG.warn("New role {} was not propagated to device {} during {} sec", newRole, deviceInfo.getLOGValue(), SET_ROLE_TIMEOUT);
                    setRoleOutputFuture.cancel(true);
                }
            };

            hashedWheelTimer.newTimeout(timerTask, SET_ROLE_TIMEOUT, TimeUnit.SECONDS);
            return JdkFutureAdapters.listenInPoolThread(setRoleOutputFuture);
        }

        LOG.info("Device: {} with version: {} does not support role", deviceInfo.getLOGValue(), deviceInfo.getVersion());
        return Futures.immediateFuture(null);
    }

    @Override
    public ListenableFuture<RpcResult<SetRoleOutput>> makeDeviceSlave() {
        return persistSendRoleChangeToDevice(OfpRole.BECOMESLAVE);
    }

    @Override
    public void onStateAcquired(final ContextChainState state) {
        hasState.set(true);
    }

    private class RpcResultFutureCallback implements FutureCallback<RpcResult<SetRoleOutput>> {

        private final MastershipChangeListener mastershipChangeListener;

        RpcResultFutureCallback(final MastershipChangeListener mastershipChangeListener) {
            this.mastershipChangeListener = mastershipChangeListener;
        }

        @Override
        public void onSuccess(@Nullable RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
            this.mastershipChangeListener.onMasterRoleAcquired(
                    deviceInfo,
                    ContextChainMastershipState.MASTER_ON_DEVICE
            );
            if (LOG.isDebugEnabled()) {
                LOG.debug("Role MASTER was successfully set on device, node {}", deviceInfo.getLOGValue());
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            if (!(throwable instanceof CancellationException)) {
                mastershipChangeListener.onNotAbleToStartMastershipMandatory(
                        deviceInfo,
                        "Was not able to set MASTER role on device");
            }
        }
    }

    private class DeviceFlowRegistryCallback implements FutureCallback<List<Optional<FlowCapableNode>>> {
        private final ListenableFuture<List<Optional<FlowCapableNode>>> deviceFlowRegistryFill;
        private final MastershipChangeListener mastershipChangeListener;

        DeviceFlowRegistryCallback(
                ListenableFuture<List<Optional<FlowCapableNode>>> deviceFlowRegistryFill,
                MastershipChangeListener mastershipChangeListener) {
            this.deviceFlowRegistryFill = deviceFlowRegistryFill;
            this.mastershipChangeListener = mastershipChangeListener;
        }

        @Override
        public void onSuccess(@Nullable List<Optional<FlowCapableNode>> result) {
            if (LOG.isDebugEnabled()) {
                // Count all flows we read from datastore for debugging purposes.
                // This number do not always represent how many flows were actually added
                // to DeviceFlowRegistry, because of possible duplicates.
                long flowCount = Optional.fromNullable(result).asSet().stream()
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .flatMap(flowCapableNodeOptional -> flowCapableNodeOptional.asSet().stream())
                        .filter(Objects::nonNull)
                        .filter(flowCapableNode -> Objects.nonNull(flowCapableNode.getTable()))
                        .flatMap(flowCapableNode -> flowCapableNode.getTable().stream())
                        .filter(Objects::nonNull)
                        .filter(table -> Objects.nonNull(table.getFlow()))
                        .flatMap(table -> table.getFlow().stream())
                        .filter(Objects::nonNull)
                        .count();

                LOG.debug("Finished filling flow registry with {} flows for node: {}", flowCount, deviceInfo.getLOGValue());
            }
            this.mastershipChangeListener.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_FLOW_REGISTRY_FILL);
        }

        @Override
        public void onFailure(Throwable t) {
            if (deviceFlowRegistryFill.isCancelled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Cancelled filling flow registry with flows for node: {}", deviceInfo.getLOGValue());
                }
            } else {
                LOG.warn("Failed filling flow registry with flows for node: {} with exception: {}", deviceInfo.getLOGValue(), t);
            }
            mastershipChangeListener.onNotAbleToStartMastership(
                    deviceInfo,
                    "Was not able to fill flow registry on device",
                    false);
        }
    }

}
