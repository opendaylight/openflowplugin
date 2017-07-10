/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Verify;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.api.ConnectionException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MastershipChangeListener;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.api.openflow.registry.ItemLifeCycleRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleKeeper;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterProviderKeeper;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.extension.api.path.MessagePath;
import org.opendaylight.openflowplugin.impl.common.ItemLifeCycleSourceImpl;
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
import org.opendaylight.openflowplugin.impl.util.MatchUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.ExperimenterMessageFromDevBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.binding.DataContainer;
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

    private static final int LOW_WATERMARK = 1000;
    private static final int HIGH_WATERMARK = 2000;

    private final MultipartWriterProvider writerProvider;
    private final HashedWheelTimer hashedWheelTimer;
    private final DeviceState deviceState;
    private final DataBroker dataBroker;
    private final Collection<RequestContext<?>> requestContexts = new HashSet<>();
    private final MessageSpy messageSpy;
    private final ItemLifeCycleKeeper flowLifeCycleKeeper;
    private final MessageTranslator<PortGrouping, FlowCapableNodeConnector> portStatusTranslator;
    private final MessageTranslator<PacketInMessage, PacketReceived> packetInTranslator;
    private final MessageTranslator<FlowRemoved, org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved> flowRemovedTranslator;
    private final TranslatorLibrary translatorLibrary;
    private final ItemLifeCycleRegistry itemLifeCycleSourceRegistry;
    private final ConvertorExecutor convertorExecutor;
    private final DeviceInitializerProvider deviceInitializerProvider;
    private final PacketInRateLimiter packetInLimiter;
    private final DeviceInfo deviceInfo;
    private final boolean skipTableFeatures;
    private final boolean switchFeaturesMandatory;
    private final boolean isFlowRemovedNotificationOn;
    private final boolean useSingleLayerSerialization;
    private NotificationPublishService notificationPublishService;
    private TransactionChainManager transactionChainManager;
    private DeviceFlowRegistry deviceFlowRegistry;
    private DeviceGroupRegistry deviceGroupRegistry;
    private DeviceMeterRegistry deviceMeterRegistry;
    private ExtensionConverterProvider extensionConverterProvider;
    private ClusterInitializationPhaseHandler clusterInitializationPhaseHandler;
    private SalRoleService salRoleService;
    private boolean initialized;
    private boolean hasState;
    private boolean isInitialTransactionSubmitted;
    private volatile ConnectionContext primaryConnectionContext;
    private volatile ContextState state;

    DeviceContextImpl(
            @Nonnull final ConnectionContext primaryConnectionContext,
            @Nonnull final DataBroker dataBroker,
            @Nonnull final MessageSpy messageSpy,
            @Nonnull final TranslatorLibrary translatorLibrary,
            final ConvertorExecutor convertorExecutor,
            final boolean skipTableFeatures,
            final HashedWheelTimer hashedWheelTimer,
            final boolean useSingleLayerSerialization,
            final DeviceInitializerProvider deviceInitializerProvider,
            final boolean isFlowRemovedNotificationOn,
            final boolean switchFeaturesMandatory) {

        this.primaryConnectionContext = primaryConnectionContext;
        this.deviceInfo = primaryConnectionContext.getDeviceInfo();
        this.hashedWheelTimer = hashedWheelTimer;
        this.deviceInitializerProvider = deviceInitializerProvider;
        this.isFlowRemovedNotificationOn = isFlowRemovedNotificationOn;
        this.switchFeaturesMandatory = switchFeaturesMandatory;
        this.deviceState = new DeviceStateImpl();
        this.dataBroker = dataBroker;
        this.messageSpy = messageSpy;

        this.packetInLimiter = new PacketInRateLimiter(primaryConnectionContext.getConnectionAdapter(),
                /*initial*/ LOW_WATERMARK, /*initial*/HIGH_WATERMARK, this.messageSpy, REJECTED_DRAIN_FACTOR);

        this.translatorLibrary = translatorLibrary;
        this.portStatusTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceInfo.getVersion(), PortGrouping.class.getName()));
        this.packetInTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceInfo.getVersion(), org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
                        .PacketIn.class.getName()));
        this.flowRemovedTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceInfo.getVersion(), FlowRemoved.class.getName()));

        this.itemLifeCycleSourceRegistry = new ItemLifeCycleRegistryImpl();
        this.flowLifeCycleKeeper = new ItemLifeCycleSourceImpl();
        this.itemLifeCycleSourceRegistry.registerLifeCycleSource(flowLifeCycleKeeper);
        this.state = ContextState.INITIALIZATION;
        this.convertorExecutor = convertorExecutor;
        this.skipTableFeatures = skipTableFeatures;
        this.useSingleLayerSerialization = useSingleLayerSerialization;
        this.initialized = false;
        writerProvider = MultipartWriterProviderFactory.createDefaultProvider(this);
    }

    @Override
    public boolean initialSubmitTransaction() {
        return (initialized &&(isInitialTransactionSubmitted =
                transactionChainManager.initialSubmitWriteTransaction()));
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
        return isInitialTransactionSubmitted;
    }

    @Override
    public <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                          final InstanceIdentifier<T> path,
                                                          final T data){
        if (initialized) {
            transactionChainManager.writeToTransaction(store, path, data, false);
        }
    }

    @Override
    public <T extends DataObject> void writeToTransactionWithParentsSlow(final LogicalDatastoreType store,
                                                                         final InstanceIdentifier<T> path,
                                                                         final T data){
        if (initialized) {
            transactionChainManager.writeToTransaction(store, path, data, true);
        }
    }

    @Override
    public <T extends DataObject> void addDeleteToTxChain(final LogicalDatastoreType store, final InstanceIdentifier<T> path) {
        if (initialized) {
            transactionChainManager.addDeleteOperationTotTxChain(store, path);
        }
    }

    @Override
    public boolean submitTransaction() {
        return initialized && transactionChainManager.submitWriteTransaction();
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
                        ? MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_FAILURE
                        : MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_SUCCESS);
    }

    @Override
    public void processReply(final Xid xid, final List<? extends OfHeader> ofHeaderList) {
        ofHeaderList.forEach(header -> messageSpy.spyMessage(
                header.getImplementedInterface(),
                (header instanceof Error)
                        ? MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_FAILURE
                        : MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Override
    public void processFlowRemovedMessage(final FlowRemoved flowRemoved) {
        //1. translate to general flow (table, priority, match, cookie)
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved flowRemovedNotification =
                flowRemovedTranslator.translate(flowRemoved, deviceInfo, null);

        if (isFlowRemovedNotificationOn) {
            // Trigger off a notification
            notificationPublishService.offerNotification(flowRemovedNotification);
        }

        final ItemLifecycleListener itemLifecycleListener = flowLifeCycleKeeper.getItemLifecycleListener();
        if (itemLifecycleListener != null) {
            //2. create registry key
            final FlowRegistryKey flowRegKey = FlowRegistryKeyFactory.create(getDeviceInfo().getVersion(), flowRemovedNotification);
            //3. lookup flowId
            final FlowDescriptor flowDescriptor = deviceFlowRegistry.retrieveDescriptor(flowRegKey);
            //4. if flowId present:
            if (flowDescriptor != null) {
                // a) construct flow path
                final KeyedInstanceIdentifier<Flow, FlowKey> flowPath = getDeviceInfo().getNodeInstanceIdentifier()
                        .augmentation(FlowCapableNode.class)
                        .child(Table.class, flowDescriptor.getTableKey())
                        .child(Flow.class, new FlowKey(flowDescriptor.getFlowId()));
                // b) notify listener
                itemLifecycleListener.onRemoved(flowPath);
            } else {
                LOG.debug("flow id not found: nodeId={} tableId={}, priority={}",
                        getDeviceInfo().getNodeId(), flowRegKey.getTableId(), flowRemovedNotification.getPriority());
            }
        }
    }

    @Override
    public void processPortStatusMessage(final PortStatusMessage portStatus) {
        messageSpy.spyMessage(portStatus.getImplementedInterface(), MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_SUCCESS);

        if (initialized) {
            try {
                writePortStatusMessage(portStatus);
                submitTransaction();
            } catch (final Exception e) {
                LOG.warn("Error processing port status message for port {} on device {}",
                        portStatus.getPortNo(), getDeviceInfo().getLOGValue(), e);
            }
        } else if (!hasState) {
            primaryConnectionContext.handlePortStatusMessage(portStatus);
        }
    }

    private void writePortStatusMessage(final PortStatus portStatusMessage) {
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
            addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, iiToNodeConnector);
        }
    }

    @Override
    public void processPacketInMessage(final PacketInMessage packetInMessage) {
        final PacketReceived packetReceived = packetInTranslator.translate(packetInMessage, getDeviceInfo(), null);
        handlePacketInMessage(packetReceived, packetInMessage.getImplementedInterface(), packetReceived.getMatch());
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
        final Optional<NodeConnectorRef> nodeConnectorRef = Optional.ofNullable(match)
                .flatMap(m -> Optional.ofNullable(m.getInPort()))
                .flatMap(nodeConnectorId -> Optional.ofNullable(InventoryDataServiceUtil
                        .portNumberfromNodeConnectorId(
                                openflowVersion,
                                nodeConnectorId)))
                .map(portNumber -> InventoryDataServiceUtil
                        .nodeConnectorRefFromDatapathIdPortno(
                                deviceInfo.getDatapathId(),
                                portNumber,
                                openflowVersion));

        if (!nodeConnectorRef.isPresent()) {
            LOG.debug("Received packet from switch {}  but couldn't find an input port", connectionAdapter.getRemoteAddress());
            messageSpy.spyMessage(implementedInterface, MessageSpy.StatisticsGroup.FROM_SWITCH_TRANSLATE_SRC_FAILURE);
            return;
        }

        messageSpy.spyMessage(implementedInterface, MessageSpy.StatisticsGroup.FROM_SWITCH_TRANSLATE_OUT_SUCCESS);

        if (!packetInLimiter.acquirePermit()) {
            LOG.debug("Packet limited");
            // TODO: save packet into emergency slot if possible
            messageSpy.spyMessage(implementedInterface, MessageSpy.StatisticsGroup.FROM_SWITCH_PACKET_IN_LIMIT_REACHED_AND_DROPPED);
            return;
        }

        final ListenableFuture<?> offerNotification = notificationPublishService
                .offerNotification(new PacketReceivedBuilder(packetIn)
                        .setIngress(nodeConnectorRef.get())
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
            public void onFailure(final Throwable t) {
                messageSpy.spyMessage(implementedInterface, MessageSpy.StatisticsGroup.FROM_SWITCH_NOTIFICATION_REJECTED);
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
    public boolean processAlienMessage(final OfHeader message) {
        final Class<? extends DataContainer> implementedInterface = message.getImplementedInterface();

        if (Objects.nonNull(implementedInterface) && org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service
                .rev130709.PacketInMessage.class.equals(implementedInterface)) {
            final org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709
                    .PacketInMessage packetInMessage = org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service
                    .rev130709.PacketInMessage.class.cast(message);

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
        Verify.verify(ContextState.INITIALIZATION.equals(state));
        this.state = ContextState.WORKING;
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
    public ItemLifeCycleRegistry getItemLifeCycleSourceRegistry() {
        return itemLifeCycleSourceRegistry;
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
    public ListenableFuture<Void> stopClusterServices() {
        return initialized
                ? transactionChainManager.deactivateTransactionManager()
                : Futures.immediateFuture(null);
    }

    @Override
    public ServiceGroupIdentifier getServiceIdentifier() {
        return this.deviceInfo.getServiceIdentifier();
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    @Override
    public void close() {
        if (ContextState.TERMINATION.equals(state)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("DeviceContext for node {} is already in TERMINATION state.", getDeviceInfo().getLOGValue());
            }

            return;
        }

        state = ContextState.TERMINATION;

        // Close all datastore registries and transactions
        if (initialized) {
            initialized = false;
            deviceGroupRegistry.close();
            deviceFlowRegistry.close();
            deviceMeterRegistry.close();

            Futures.addCallback(transactionChainManager.shuttingDown(), new FutureCallback<Void>() {
                @Override
                public void onSuccess(@Nullable final Void result) {
                    transactionChainManager.close();
                    transactionChainManager = null;
                }

                @Override
                public void onFailure(final Throwable t) {
                    transactionChainManager.close();
                    transactionChainManager = null;
                }
            });
        }

        for (final Iterator<RequestContext<?>> iterator = Iterators
                .consumingIterator(requestContexts.iterator()); iterator.hasNext();) {
            RequestContextUtil.closeRequestContextWithRpcError(iterator.next(), "Connection closed.");
        }
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
    public void setLifecycleInitializationPhaseHandler(final ClusterInitializationPhaseHandler handler) {
        this.clusterInitializationPhaseHandler = handler;
    }

    @Override
    public boolean onContextInstantiateService(final MastershipChangeListener mastershipChangeListener) {
        LOG.info("Starting device context cluster services for node {}", deviceInfo.getLOGValue());
        lazyTransactionManagerInitialization();

        try {
            final List<PortStatusMessage> portStatusMessages = primaryConnectionContext
                    .retrieveAndClearPortStatusMessages();

            portStatusMessages.forEach(this::writePortStatusMessage);
            submitTransaction();
        } catch (final Exception ex) {
            LOG.warn("Error processing port status messages from device {}", getDeviceInfo().getLOGValue(), ex);
            return false;
        }

        try {
            final Optional<AbstractDeviceInitializer> initializer = deviceInitializerProvider
                    .lookup(deviceInfo.getVersion());

            if (initializer.isPresent()) {
                initializer
                        .get()
                        .initialize(this, switchFeaturesMandatory, skipTableFeatures, writerProvider, convertorExecutor)
                        .get(DEVICE_INIT_TIMEOUT, TimeUnit.MILLISECONDS);
            } else {
                throw new ExecutionException(new ConnectionException("Unsupported version " + deviceInfo.getVersion()));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
            LOG.warn("Device {} cannot be initialized: {}", deviceInfo.getLOGValue(), ex.getMessage());
            LOG.trace("Device {} cannot be initialized: ", deviceInfo.getLOGValue(), ex);
            return false;
        }

        Futures.addCallback(sendRoleChangeToDevice(OfpRole.BECOMEMASTER),
                new RpcResultFutureCallback(mastershipChangeListener));

        final ListenableFuture<List<com.google.common.base.Optional<FlowCapableNode>>> deviceFlowRegistryFill = getDeviceFlowRegistry().fill();
        Futures.addCallback(deviceFlowRegistryFill,
                new DeviceFlowRegistryCallback(deviceFlowRegistryFill, mastershipChangeListener));

        return this.clusterInitializationPhaseHandler.onContextInstantiateService(mastershipChangeListener);
    }

    @VisibleForTesting
    void lazyTransactionManagerInitialization() {
        if (!this.initialized) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Transaction chain manager for node {} created", deviceInfo.getLOGValue());
            }
            this.transactionChainManager = new TransactionChainManager(dataBroker, deviceInfo);
            this.deviceFlowRegistry = new DeviceFlowRegistryImpl(deviceInfo.getVersion(), dataBroker, deviceInfo.getNodeInstanceIdentifier());
            this.deviceGroupRegistry = new DeviceGroupRegistryImpl();
            this.deviceMeterRegistry = new DeviceMeterRegistryImpl();
            this.transactionChainManager.activateTransactionManager();
            this.initialized = true;
        }
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

    private ListenableFuture<RpcResult<SetRoleOutput>> sendRoleChangeToDevice(final OfpRole newRole) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending new role {} to device {}", newRole, deviceInfo.getNodeId());
        }

        final Future<RpcResult<SetRoleOutput>> setRoleOutputFuture;

        if (deviceInfo.getVersion() >= OFConstants.OFP_VERSION_1_3) {
            final SetRoleInput setRoleInput = (new SetRoleInputBuilder()).setControllerRole(newRole)
                    .setNode(new NodeRef(deviceInfo.getNodeInstanceIdentifier())).build();

            setRoleOutputFuture = this.salRoleService.setRole(setRoleInput);

            final TimerTask timerTask = timeout -> {
                if (!setRoleOutputFuture.isDone()) {
                    LOG.warn("New role {} was not propagated to device {} during {} sec", newRole, deviceInfo.getLOGValue(), SET_ROLE_TIMEOUT);
                    setRoleOutputFuture.cancel(true);
                }
            };

            hashedWheelTimer.newTimeout(timerTask, SET_ROLE_TIMEOUT, TimeUnit.SECONDS);
        } else {
            LOG.info("Device: {} with version: {} does not support role", deviceInfo.getLOGValue(), deviceInfo.getVersion());
            return Futures.immediateFuture(null);
        }

        return JdkFutureAdapters.listenInPoolThread(setRoleOutputFuture);
    }

    @Override
    public ListenableFuture<RpcResult<SetRoleOutput>> makeDeviceSlave() {
        return sendRoleChangeToDevice(OfpRole.BECOMESLAVE);
    }

    @Override
    public void onStateAcquired(final ContextChainState state) {
        hasState = true;
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
            mastershipChangeListener.onNotAbleToStartMastershipMandatory(
                    deviceInfo,
                    "Was not able to set MASTER role on device");
        }
    }

    private class DeviceFlowRegistryCallback implements FutureCallback<List<com.google.common.base.Optional<FlowCapableNode>>> {
        private final ListenableFuture<List<com.google.common.base.Optional<FlowCapableNode>>> deviceFlowRegistryFill;
        private final MastershipChangeListener mastershipChangeListener;

        DeviceFlowRegistryCallback(
                ListenableFuture<List<com.google.common.base.Optional<FlowCapableNode>>> deviceFlowRegistryFill,
                MastershipChangeListener mastershipChangeListener) {
            this.deviceFlowRegistryFill = deviceFlowRegistryFill;
            this.mastershipChangeListener = mastershipChangeListener;
        }

        @Override
        public void onSuccess(@Nullable List<com.google.common.base.Optional<FlowCapableNode>> result) {
            if (LOG.isDebugEnabled()) {
                // Count all flows we read from datastore for debugging purposes.
                // This number do not always represent how many flows were actually added
                // to DeviceFlowRegistry, because of possible duplicates.
                long flowCount = Optional.ofNullable(result)
                        .map(Collections::singleton)
                        .orElse(Collections.emptySet())
                        .stream()
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
