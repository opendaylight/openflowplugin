/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.util.HashedWheelTimer;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainHolder;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.DeviceInitializationContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.common.txchain.TransactionChainManager;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterProviderKeeper;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.extension.api.path.MessagePath;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.impl.device.initialization.AbstractDeviceInitializer;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProvider;
import org.opendaylight.openflowplugin.impl.device.listener.MultiMsgCollectorImpl;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.openflowplugin.impl.registry.group.DeviceGroupRegistryImpl;
import org.opendaylight.openflowplugin.impl.registry.meter.DeviceMeterRegistryImpl;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.services.util.RequestContextUtil;
import org.opendaylight.openflowplugin.impl.util.MatchUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.ExperimenterMessageFromDevBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceivedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsDataBuilder;
import org.opendaylight.yangtools.util.concurrent.NotificationManager;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceContextImpl implements DeviceContext, ExtensionConverterProviderKeeper, DeviceInitializationContext {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceContextImpl.class);
    private static final Logger OF_EVENT_LOG = LoggerFactory.getLogger("OfEventLog");

    // TODO: drain factor should be parametrized
    private static final float REJECTED_DRAIN_FACTOR = 0.25f;
    // TODO: low water mark factor should be parametrized
    private static final float LOW_WATERMARK_FACTOR = 0.75f;
    // TODO: high water mark factor should be parametrized
    private static final float HIGH_WATERMARK_FACTOR = 0.95f;

    // Timeout in milliseconds after what we will give up on initializing device
    private static final int DEVICE_INIT_TIMEOUT = 9000;

    // Timeout in milliseconds after what we will give up on closing transaction chain
    private static final int TX_CHAIN_CLOSE_TIMEOUT = 10000;

    private static final int LOW_WATERMARK = 1000;
    private static final int HIGH_WATERMARK = 2000;

    private final MultipartWriterProvider writerProvider;
    private final HashedWheelTimer hashedWheelTimer;
    private final DeviceState deviceState;
    private final DataBroker dataBroker;
    private final Collection<RequestContext<?>> requestContexts = ConcurrentHashMap.newKeySet();
    private final MessageSpy messageSpy;
    private final MessageTranslator<PortGrouping, FlowCapableNodeConnector> portStatusTranslator;
    private final MessageTranslator<PacketInMessage, PacketReceived> packetInTranslator;
    private final MessageTranslator<FlowRemoved, org.opendaylight.yang.gen.v1.urn.opendaylight
            .flow.service.rev130819.FlowRemoved> flowRemovedTranslator;
    private final TranslatorLibrary translatorLibrary;
    private final ConvertorExecutor convertorExecutor;
    private final DeviceInitializerProvider deviceInitializerProvider;
    private final PacketInRateLimiter packetInLimiter;
    private final DeviceInfo deviceInfo;
    private final ConnectionContext primaryConnectionContext;
    private final boolean skipTableFeatures;
    private final boolean switchFeaturesMandatory;
    private final boolean isFlowRemovedNotificationOn;
    private final boolean useSingleLayerSerialization;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean hasState = new AtomicBoolean(false);
    private final AtomicBoolean isInitialTransactionSubmitted = new AtomicBoolean(false);
    private final ContextChainHolder contextChainHolder;
    private NotificationPublishService notificationPublishService;
    private TransactionChainManager transactionChainManager;
    private DeviceFlowRegistry deviceFlowRegistry;
    private DeviceGroupRegistry deviceGroupRegistry;
    private DeviceMeterRegistry deviceMeterRegistry;
    private ExtensionConverterProvider extensionConverterProvider;
    private ContextChainMastershipWatcher contextChainMastershipWatcher;
    private final NotificationManager<String, Runnable> queuedNotificationManager;
    private final boolean isStatisticsPollingOn;

    DeviceContextImpl(@NonNull final ConnectionContext primaryConnectionContext,
                      @NonNull final DataBroker dataBroker,
                      @NonNull final MessageSpy messageSpy,
                      @NonNull final TranslatorLibrary translatorLibrary,
                      final ConvertorExecutor convertorExecutor,
                      final boolean skipTableFeatures,
                      final HashedWheelTimer hashedWheelTimer,
                      final boolean useSingleLayerSerialization,
                      final DeviceInitializerProvider deviceInitializerProvider,
                      final boolean isFlowRemovedNotificationOn,
                      final boolean switchFeaturesMandatory,
                      final ContextChainHolder contextChainHolder,
                      final NotificationManager queuedNotificationManager,
                      final boolean isStatisticsPollingOn) {
        this.primaryConnectionContext = primaryConnectionContext;
        this.deviceInfo = primaryConnectionContext.getDeviceInfo();
        this.hashedWheelTimer = hashedWheelTimer;
        this.deviceInitializerProvider = deviceInitializerProvider;
        this.isFlowRemovedNotificationOn = isFlowRemovedNotificationOn;
        this.switchFeaturesMandatory = switchFeaturesMandatory;
        this.deviceState = new DeviceStateImpl();
        this.dataBroker = dataBroker;
        this.messageSpy = messageSpy;
        this.isStatisticsPollingOn = isStatisticsPollingOn;
        this.contextChainHolder = contextChainHolder;

        this.packetInLimiter = new PacketInRateLimiter(primaryConnectionContext.getConnectionAdapter(),
                /*initial*/ LOW_WATERMARK, /*initial*/HIGH_WATERMARK, this.messageSpy, REJECTED_DRAIN_FACTOR);

        this.translatorLibrary = translatorLibrary;
        this.portStatusTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceInfo.getVersion(), PortGrouping.class.getName()));
        this.packetInTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceInfo.getVersion(), org.opendaylight.yang.gen.v1.urn.opendaylight.openflow
                        .protocol.rev130731
                        .PacketIn.class.getName()));
        this.flowRemovedTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceInfo.getVersion(), FlowRemoved.class.getName()));

        this.convertorExecutor = convertorExecutor;
        this.skipTableFeatures = skipTableFeatures;
        this.useSingleLayerSerialization = useSingleLayerSerialization;
        this.queuedNotificationManager = queuedNotificationManager;
        writerProvider = MultipartWriterProviderFactory.createDefaultProvider(this);
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
    public ReadTransaction getReadTransaction() {
        return dataBroker.newReadOnlyTransaction();
    }

    @Override
    public boolean isTransactionsEnabled() {
        return isInitialTransactionSubmitted.get();
    }

    @Override
    public <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                          final InstanceIdentifier<T> path,
                                                          final T data) {
        if (initialized.get()) {
            transactionChainManager.writeToTransaction(store, path, data, false);
        }
    }

    @Override
    public <T extends DataObject> void writeToTransactionWithParentsSlow(final LogicalDatastoreType store,
                                                                         final InstanceIdentifier<T> path,
                                                                         final T data) {
        if (initialized.get()) {
            transactionChainManager.writeToTransaction(store, path, data, true);
        }
    }

    @Override
    public <T extends DataObject> void addDeleteToTxChain(final LogicalDatastoreType store,
                                                          final InstanceIdentifier<T> path) {
        if (initialized.get()) {
            transactionChainManager.addDeleteOperationToTxChain(store, path);
        }
    }

    @Override
    public boolean submitTransaction() {
        return initialized.get() && transactionChainManager.submitTransaction();
    }

    @Override
    public boolean syncSubmitTransaction() {
        return initialized.get() && transactionChainManager.submitTransaction(true);
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
    public boolean isStatisticsPollingOn() {
        return isStatisticsPollingOn;
    }

    @Override
    public DeviceMeterRegistry getDeviceMeterRegistry() {
        return deviceMeterRegistry;
    }

    @Override
    public void processReply(final OfHeader ofHeader) {
        messageSpy.spyMessage(
                ofHeader.implementedInterface(),
                ofHeader instanceof Error
                        ? MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_FAILURE
                        : MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_SUCCESS);
    }

    @Override
    public void processReply(final Xid xid, final List<? extends OfHeader> ofHeaderList) {
        ofHeaderList.forEach(header -> messageSpy.spyMessage(
                header.implementedInterface(),
                header instanceof Error
                        ? MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_FAILURE
                        : MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Override
    public void processFlowRemovedMessage(final FlowRemoved flowRemoved) {
        if (isMasterOfDevice()) {
            //1. translate to general flow (table, priority, match, cookie)
            final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819
                    .FlowRemoved flowRemovedNotification = flowRemovedTranslator
                    .translate(flowRemoved, deviceInfo, null);

            if (isFlowRemovedNotificationOn) {
                // Trigger off a notification
                notificationPublishService.offerNotification(flowRemovedNotification);
            }
        } else {
            LOG.debug("Controller is not owner of the device {}, skipping Flow Removed message",
                    deviceInfo.getLOGValue());
        }
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void processPortStatusMessage(final PortStatusMessage portStatus) {
        messageSpy.spyMessage(portStatus.implementedInterface(), MessageSpy.StatisticsGroup
                .FROM_SWITCH_PUBLISHED_SUCCESS);

        if (initialized.get()) {
            try {
                writePortStatusMessage(portStatus);
            } catch (final Exception e) {
                LOG.warn("Error processing port status message for port {} on device {}",
                        portStatus.getPortNo(), getDeviceInfo(), e);
            }
        } else if (!hasState.get()) {
            primaryConnectionContext.handlePortStatusMessage(portStatus);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void writePortStatusMessage(final PortStatus portStatusMessage) {
        String datapathId = deviceInfo.getDatapathId().toString().intern();
        queuedNotificationManager.submitNotification(datapathId, () -> {
            try {
                acquireWriteTransactionLock();
                final FlowCapableNodeConnector flowCapableNodeConnector = portStatusTranslator
                        .translate(portStatusMessage, getDeviceInfo(), null);
                OF_EVENT_LOG.debug("Node Connector Status, Node: {}, PortNumber: {}, PortName: {}, Reason: {}",
                        deviceInfo.getDatapathId(), portStatusMessage.getPortNo(), portStatusMessage.getName(),
                        portStatusMessage.getReason());

                final KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> iiToNodeConnector = getDeviceInfo()
                        .getNodeInstanceIdentifier()
                        .child(NodeConnector.class, new NodeConnectorKey(InventoryDataServiceUtil
                                .nodeConnectorIdfromDatapathPortNo(
                                        deviceInfo.getDatapathId(),
                                        portStatusMessage.getPortNo(),
                                        OpenflowVersion.get(deviceInfo.getVersion()))));

                writeToTransaction(LogicalDatastoreType.OPERATIONAL, iiToNodeConnector, new NodeConnectorBuilder()
                        .withKey(iiToNodeConnector.getKey())
                        .addAugmentation(new FlowCapableNodeConnectorStatisticsDataBuilder().build())
                        .addAugmentation(flowCapableNodeConnector)
                        .build());
                syncSubmitTransaction();
                if (PortReason.OFPPRDELETE.equals(portStatusMessage.getReason())) {
                    addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, iiToNodeConnector);
                    syncSubmitTransaction();
                }
            } catch (final Exception e) {
                LOG.warn("Error processing port status message for port {} on device {}",
                        portStatusMessage.getPortNo(), datapathId, e);
            } finally {
                releaseWriteTransactionLock();
            }
        });
    }

    @Override
    public void processPacketInMessage(final PacketInMessage packetInMessage) {
        if (isMasterOfDevice()) {
            final PacketReceived packetReceived = packetInTranslator.translate(packetInMessage, getDeviceInfo(), null);
            handlePacketInMessage(packetReceived, packetInMessage.implementedInterface(), packetReceived.getMatch());
        } else {
            LOG.debug("Controller is not owner of the device {}, skipping packet_in message", deviceInfo.getLOGValue());
        }
    }

    private Boolean isMasterOfDevice() {
        final ContextChain contextChain = contextChainHolder.getContextChain(deviceInfo);
        boolean result = false;
        if (contextChain != null) {
            result = contextChain.isMastered(ContextChainMastershipState.CHECK, false);
        }
        return result;
    }

    private void handlePacketInMessage(final PacketIn packetIn,
                                       final Class<?> implementedInterface,
                                       final Match match) {
        messageSpy.spyMessage(implementedInterface, MessageSpy.StatisticsGroup.FROM_SWITCH);
        final ConnectionAdapter connectionAdapter = getPrimaryConnectionContext().getConnectionAdapter();

        if (packetIn == null) {
            LOG.debug("Received a null packet from switch {}", connectionAdapter.getRemoteAddress());
            messageSpy.spyMessage(implementedInterface, MessageSpy.StatisticsGroup.FROM_SWITCH_TRANSLATE_SRC_FAILURE);
            return;
        }

        final OpenflowVersion openflowVersion = OpenflowVersion.get(deviceInfo.getVersion());

        // Try to get ingress from match
        final NodeConnectorRef nodeConnectorRef = packetIn.getIngress() != null
                ? packetIn.getIngress() : Optional.ofNullable(match)
                .map(Match::getInPort)
                .map(nodeConnectorId -> InventoryDataServiceUtil
                        .portNumberfromNodeConnectorId(
                                openflowVersion,
                                nodeConnectorId))
                .map(portNumber -> InventoryDataServiceUtil
                        .nodeConnectorRefFromDatapathIdPortno(
                                deviceInfo.getDatapathId(),
                                portNumber,
                                openflowVersion))
                .orElse(null);

        messageSpy.spyMessage(implementedInterface, MessageSpy.StatisticsGroup.FROM_SWITCH_TRANSLATE_OUT_SUCCESS);

        if (!packetInLimiter.acquirePermit()) {
            LOG.debug("Packet limited");
            // TODO: save packet into emergency slot if possible
            messageSpy.spyMessage(implementedInterface, MessageSpy.StatisticsGroup
                    .FROM_SWITCH_PACKET_IN_LIMIT_REACHED_AND_DROPPED);
            return;
        }

        final ListenableFuture<?> offerNotification = notificationPublishService
                .offerNotification(new PacketReceivedBuilder(packetIn)
                        .setIngress(nodeConnectorRef)
                        .setMatch(MatchUtil.transformMatch(match,
                                org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received
                                        .Match.class))
                        .build());

        if (NotificationPublishService.REJECTED.equals(offerNotification)) {
            LOG.debug("notification offer rejected");
            messageSpy.spyMessage(implementedInterface, MessageSpy.StatisticsGroup.FROM_SWITCH_NOTIFICATION_REJECTED);
            packetInLimiter.drainLowWaterMark();
            packetInLimiter.releasePermit();
            return;
        }

        Futures.addCallback(offerNotification, new FutureCallback<Object>() {
            @Override
            public void onSuccess(final Object result) {
                messageSpy.spyMessage(implementedInterface, MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_SUCCESS);
                packetInLimiter.releasePermit();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                messageSpy.spyMessage(implementedInterface, MessageSpy.StatisticsGroup
                        .FROM_SWITCH_NOTIFICATION_REJECTED);
                LOG.debug("notification offer failed: {}", throwable.getMessage());
                LOG.trace("notification offer failed..", throwable);
                packetInLimiter.releasePermit();
            }
        }, MoreExecutors.directExecutor());
    }

    @Override
    public void processExperimenterMessage(final ExperimenterMessage notification) {
        if (isMasterOfDevice()) {
            // lookup converter
            final ExperimenterDataOfChoice vendorData = notification.getExperimenterDataOfChoice();
            final MessageTypeKey<? extends ExperimenterDataOfChoice> key = new MessageTypeKey<>(
                    getDeviceInfo().getVersion(),
                    (Class<? extends ExperimenterDataOfChoice>) vendorData.implementedInterface());
            final ConvertorMessageFromOFJava<ExperimenterDataOfChoice, MessagePath> messageConverter =
                    extensionConverterProvider.getMessageConverter(key);
            if (messageConverter == null) {
                LOG.warn("custom converter for {}[OF:{}] not found",
                        notification.getExperimenterDataOfChoice().implementedInterface(),
                        getDeviceInfo().getVersion());
                return;
            }
            // build notification
            final ExperimenterMessageOfChoice messageOfChoice;
            messageOfChoice = messageConverter.convert(vendorData, MessagePath.MESSAGE_NOTIFICATION);
            final ExperimenterMessageFromDevBuilder experimenterMessageFromDevBld = new
                    ExperimenterMessageFromDevBuilder()
                    .setNode(new NodeRef(getDeviceInfo().getNodeInstanceIdentifier()))
                    .setExperimenterMessageOfChoice(messageOfChoice);
            // publish
            notificationPublishService.offerNotification(experimenterMessageFromDevBld.build());
        } else {
            LOG.debug("Controller is not owner of the device {}, skipping experimenter message",
                    deviceInfo.getLOGValue());
        }
    }

    @Override
    // The cast to PacketInMessage is safe as the implemented interface is verified before the cas tbut FB doesn't
    // recognize it.
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    public boolean processAlienMessage(final OfHeader message) {
        final Class<? extends DataContainer> implementedInterface = message.implementedInterface();

        if (org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketInMessage.class
                .equals(implementedInterface)) {
            final org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709
                    .PacketInMessage packetInMessage = (org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service
                .rev130709.PacketInMessage) message;

            handlePacketInMessage(packetInMessage, implementedInterface, packetInMessage.getMatch());
            return true;
        }

        return false;
    }

    @Override
    public TranslatorLibrary oook() {
        return translatorLibrary;
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
        primaryConnectionContext.getConnectionAdapter().setPacketInFiltering(false);
    }

    @Override
    public <T extends OfHeader> MultiMsgCollector<T> getMultiMsgCollector(final RequestContext<List<T>>
                                                                                      requestContext) {
        return new MultiMsgCollectorImpl<>(this, requestContext);
    }

    @Override
    public void updatePacketInRateLimit(final long upperBound) {
        packetInLimiter.changeWaterMarks((int) (LOW_WATERMARK_FACTOR * upperBound),
                (int) (HIGH_WATERMARK_FACTOR * upperBound));
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
    public ListenableFuture<?> closeServiceInstance() {
        final ListenableFuture<?> listenableFuture = initialized.get()
                ? transactionChainManager.deactivateTransactionManager()
                : Futures.immediateFuture(null);

        hashedWheelTimer.newTimeout((timerTask) -> {
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
    public void registerMastershipWatcher(@NonNull final ContextChainMastershipWatcher newWatcher) {
        this.contextChainMastershipWatcher = newWatcher;
    }

    @NonNull
    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }

    @Override
    public void acquireWriteTransactionLock() {
        transactionChainManager.acquireWriteTransactionLock();
    }

    @Override
    public void releaseWriteTransactionLock() {
        transactionChainManager.releaseWriteTransactionLock();
    }

    @Override
    public void close() {
        // Close all datastore registries and transactions
        if (initialized.getAndSet(false)) {
            deviceGroupRegistry.close();
            deviceFlowRegistry.close();
            deviceMeterRegistry.close();

            final ListenableFuture<?> txChainShuttingDown = transactionChainManager.shuttingDown();

            Futures.addCallback(txChainShuttingDown, new FutureCallback<Object>() {
                @Override
                public void onSuccess(final Object result) {
                    transactionChainManager.close();
                    transactionChainManager = null;
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    transactionChainManager.close();
                    transactionChainManager = null;
                }
            }, MoreExecutors.directExecutor());
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
    public void instantiateServiceInstance() {
        lazyTransactionManagerInitialization();
    }

    // TODO: exception handling should be fixed by using custom checked exception, never RuntimeExceptions
    @Override
    @SuppressWarnings({"checkstyle:IllegalCatch"})
    public void initializeDevice() {
        LOG.debug("Device initialization started for device {}", deviceInfo);
        try {
            final List<PortStatusMessage> portStatusMessages = primaryConnectionContext
                    .retrieveAndClearPortStatusMessages();
            portStatusMessages.forEach(this::writePortStatusMessage);
            submitTransaction();
        } catch (final Exception ex) {
            throw new RuntimeException(String.format("Error processing port status messages from device %s: %s",
                    deviceInfo.toString(), ex.toString()), ex);
        }

        final Optional<AbstractDeviceInitializer> initializer = deviceInitializerProvider
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
                        deviceInfo.toString(), String.valueOf(DEVICE_INIT_TIMEOUT / 1000), ex.toString()), ex);
            } catch (ExecutionException | InterruptedException ex) {
                throw new RuntimeException(
                        String.format("Device %s cannot be initialized: %s", deviceInfo.toString(), ex.toString()), ex);
            }
        } else {
            throw new RuntimeException(String.format("Unsupported version %s for device %s",
                    deviceInfo.getVersion(),
                    deviceInfo.toString()));
        }

        final ListenableFuture<List<Optional<FlowCapableNode>>> deviceFlowRegistryFill =
                getDeviceFlowRegistry().fill();
        Futures.addCallback(deviceFlowRegistryFill,
                new DeviceFlowRegistryCallback(deviceFlowRegistryFill, contextChainMastershipWatcher),
                MoreExecutors.directExecutor());
    }

    @VisibleForTesting
    void lazyTransactionManagerInitialization() {
        if (!this.initialized.get()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Transaction chain manager for node {} created", deviceInfo);
            }
            this.transactionChainManager = new TransactionChainManager(dataBroker, deviceInfo.getNodeId().getValue());
            this.deviceFlowRegistry = new DeviceFlowRegistryImpl(deviceInfo.getVersion(), dataBroker,
                    deviceInfo.getNodeInstanceIdentifier());
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

        final AbstractRequestContext<T> abstractRequestContext = new AbstractRequestContext<>(xid) {
            @Override
            public void close() {
                requestContexts.remove(this);
            }
        };

        requestContexts.add(abstractRequestContext);
        return abstractRequestContext;
    }

    @Override
    public void onStateAcquired(final ContextChainState state) {
        hasState.set(true);
    }

    private class DeviceFlowRegistryCallback implements FutureCallback<List<Optional<FlowCapableNode>>> {
        private final ListenableFuture<List<Optional<FlowCapableNode>>> deviceFlowRegistryFill;
        private final ContextChainMastershipWatcher contextChainMastershipWatcher;

        DeviceFlowRegistryCallback(
                ListenableFuture<List<Optional<FlowCapableNode>>> deviceFlowRegistryFill,
                ContextChainMastershipWatcher contextChainMastershipWatcher) {
            this.deviceFlowRegistryFill = deviceFlowRegistryFill;
            this.contextChainMastershipWatcher = contextChainMastershipWatcher;
        }

        @Override
        public void onSuccess(List<Optional<FlowCapableNode>> result) {
            if (LOG.isDebugEnabled()) {
                // Count all flows we read from datastore for debugging purposes.
                // This number do not always represent how many flows were actually added
                // to DeviceFlowRegistry, because of possible duplicates.
                long flowCount = 0;
                if (result != null) {
                    for (Optional<FlowCapableNode> optNode : result) {
                        if (optNode.isPresent()) {
                            flowCount += optNode.get().nonnullTable().values().stream()
                                    .filter(Objects::nonNull)
                                    .flatMap(table -> table.nonnullFlow().values().stream())
                                    .filter(Objects::nonNull)
                                    .count();
                        }
                    }
                }

                LOG.debug("Finished filling flow registry with {} flows for node: {}", flowCount, deviceInfo);
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            if (deviceFlowRegistryFill.isCancelled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Cancelled filling flow registry with flows for node: {}", deviceInfo);
                }
            } else {
                LOG.warn("Failed filling flow registry with flows for node: {}", deviceInfo, throwable);
            }
            contextChainMastershipWatcher.onNotAbleToStartMastership(
                    deviceInfo,
                    "Was not able to fill flow registry on device",
                    false);
        }
    }

}
