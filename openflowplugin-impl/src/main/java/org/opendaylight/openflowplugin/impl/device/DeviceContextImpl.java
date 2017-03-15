/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainClosedException;
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
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
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
import org.opendaylight.openflowplugin.impl.common.NodeStaticReplyTranslatorUtil;
import org.opendaylight.openflowplugin.impl.device.listener.MultiMsgCollectorImpl;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.registry.group.DeviceGroupRegistryImpl;
import org.opendaylight.openflowplugin.impl.registry.meter.DeviceMeterRegistryImpl;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.util.DeviceInitializationUtils;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.session.SwitchConnectionCookieOFImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.ExperimenterMessageFromDevBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
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

    private boolean initialized;
    private static final Long RETRY_DELAY = 100L;
    private static final int RETRY_COUNT = 3;

    private SalRoleService salRoleService = null;
    private final HashedWheelTimer hashedWheelTimer;
    private ConnectionContext primaryConnectionContext;
    private final DeviceState deviceState;
    private final DataBroker dataBroker;
    private final Map<SwitchConnectionDistinguisher, ConnectionContext> auxiliaryConnectionContexts;
    private TransactionChainManager transactionChainManager;
    private DeviceFlowRegistry deviceFlowRegistry;
    private DeviceGroupRegistry deviceGroupRegistry;
    private DeviceMeterRegistry deviceMeterRegistry;
    private final PacketInRateLimiter packetInLimiter;
    private final MessageSpy messageSpy;
    private final ItemLifeCycleKeeper flowLifeCycleKeeper;
    private NotificationPublishService notificationPublishService;
    private Timeout barrierTaskTimeout;
    private final MessageTranslator<PortGrouping, FlowCapableNodeConnector> portStatusTranslator;
    private final MessageTranslator<PacketInMessage, PacketReceived> packetInTranslator;
    private final MessageTranslator<FlowRemoved, org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved> flowRemovedTranslator;
    private final TranslatorLibrary translatorLibrary;
    private final ItemLifeCycleRegistry itemLifeCycleSourceRegistry;
    private ExtensionConverterProvider extensionConverterProvider;
    private final DeviceManager deviceManager;
    private boolean skipTableFeatures;
    private boolean switchFeaturesMandatory;
    private final DeviceInfo deviceInfo;
    private final ConvertorExecutor convertorExecutor;
    private volatile CONTEXT_STATE state;
    private ClusterInitializationPhaseHandler clusterInitializationPhaseHandler;
    private final DeviceManager myManager;

    DeviceContextImpl(
            @Nonnull final ConnectionContext primaryConnectionContext,
            @Nonnull final DataBroker dataBroker,
            @Nonnull final MessageSpy messageSpy,
            @Nonnull final TranslatorLibrary translatorLibrary,
            @Nonnull final DeviceManager manager,
            final ConvertorExecutor convertorExecutor,
            final boolean skipTableFeatures,
            final HashedWheelTimer hashedWheelTimer,
            final DeviceManager myManager) {
        this.primaryConnectionContext = primaryConnectionContext;
        this.deviceInfo = primaryConnectionContext.getDeviceInfo();
        this.hashedWheelTimer = hashedWheelTimer;
        this.myManager = myManager;
        this.deviceState = new DeviceStateImpl();
        this.dataBroker = dataBroker;
        this.auxiliaryConnectionContexts = new HashMap<>();
        this.messageSpy = Preconditions.checkNotNull(messageSpy);
        this.deviceManager = manager;

        this.packetInLimiter = new PacketInRateLimiter(primaryConnectionContext.getConnectionAdapter(),
                /*initial*/ 1000, /*initial*/2000, this.messageSpy, REJECTED_DRAIN_FACTOR);

        this.translatorLibrary = translatorLibrary;
        this.portStatusTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceInfo.getVersion(), PortGrouping.class.getName()));
        this.packetInTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceInfo.getVersion(), PacketIn.class.getName()));
        this.flowRemovedTranslator = translatorLibrary.lookupTranslator(
                new TranslatorKey(deviceInfo.getVersion(), FlowRemoved.class.getName()));

        this.itemLifeCycleSourceRegistry = new ItemLifeCycleRegistryImpl();
        this.flowLifeCycleKeeper = new ItemLifeCycleSourceImpl();
        this.itemLifeCycleSourceRegistry.registerLifeCycleSource(flowLifeCycleKeeper);
        this.state = CONTEXT_STATE.INITIALIZATION;
        this.convertorExecutor = convertorExecutor;
        this.skipTableFeatures = skipTableFeatures;
        this.initialized = false;
    }

    @Override
    public void initialSubmitTransaction() {
        if (initialized) {
            transactionChainManager.initialSubmitWriteTransaction();
        }
    }

    @Override
    public void addAuxiliaryConnectionContext(final ConnectionContext connectionContext) {
        final SwitchConnectionDistinguisher connectionDistinguisher = createConnectionDistinguisher(connectionContext);
        auxiliaryConnectionContexts.put(connectionDistinguisher, connectionContext);
    }

    private static SwitchConnectionDistinguisher createConnectionDistinguisher(final ConnectionContext connectionContext) {
        return new SwitchConnectionCookieOFImpl(connectionContext.getFeatures().getAuxiliaryId());
    }

    @Override
    public void removeAuxiliaryConnectionContext(final ConnectionContext connectionContext) {
        final SwitchConnectionDistinguisher connectionDistinguisher = createConnectionDistinguisher(connectionContext);
        LOG.debug("auxiliary connection dropped: {}, nodeId:{}", connectionContext.getConnectionAdapter()
                .getRemoteAddress(), getDeviceInfo().getLOGValue());
        auxiliaryConnectionContexts.remove(connectionDistinguisher);
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
    public <T extends DataObject> void addDeleteToTxChain(final LogicalDatastoreType store, final InstanceIdentifier<T> path) throws TransactionChainClosedException {
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
    public ConnectionContext getAuxiliaryConnectionContexts(final BigInteger cookie) {
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
        //1. translate to general flow (table, priority, match, cookie)
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved flowRemovedNotification =
                flowRemovedTranslator.translate(flowRemoved, deviceInfo, null);

        if(deviceManager.isFlowRemovedNotificationOn()) {
            // Trigger off a notification
            notificationPublishService.offerNotification(flowRemovedNotification);
        } else if(LOG.isDebugEnabled()) {
            LOG.debug("For nodeId={} isFlowRemovedNotificationOn={}", getDeviceInfo().getLOGValue(), deviceManager.isFlowRemovedNotificationOn());
        }

        final ItemLifecycleListener itemLifecycleListener = flowLifeCycleKeeper.getItemLifecycleListener();
        if (itemLifecycleListener != null) {
            //2. create registry key
            final FlowRegistryKey flowRegKey = FlowRegistryKeyFactory.create(flowRemovedNotification);
            //3. lookup flowId
            final FlowDescriptor flowDescriptor = deviceFlowRegistry.retrieveIdForFlow(flowRegKey);
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
        messageSpy.spyMessage(portStatus.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS);
        try {
            updatePortStatusMessage(portStatus);
        } catch (final Exception e) {
            LOG.warn("Error processing port status message for port {} on device {} : {}", portStatus.getPortNo().toString(),
                    getDeviceInfo().getNodeId().toString(), e);
            retryProcessPortStatusMessage(portStatus,RETRY_COUNT);
        }
    }

    private void retryProcessPortStatusMessage(final PortStatusMessage portStatus, final int retryCount){
        Thread thread = new Thread(new PortStatusRetryRunnable(portStatus, retryCount));
        thread.start();
    }

    private class PortStatusRetryRunnable implements Runnable {
        private final PortStatusMessage portStatusMessage;
        private final int retryCount;
        private int count ;

        PortStatusRetryRunnable (final PortStatusMessage portStatus,final int retry){
            portStatusMessage = portStatus;
            retryCount = retry;
            count = 0;
        }
        @Override
        public void run() {
            while(count < retryCount) {
                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_DELAY);
                } catch (InterruptedException e) {
                    LOG.warn("Caught interrupted exception while sleeping during a port retry task");
                }

                try {
                    updatePortStatusMessage(portStatusMessage);
                    break;
                } catch (final Exception e) {
                    LOG.warn("Error processing port status message for {} on port {} on device {} : {}",count,
                            portStatusMessage.getPortNo().toString(),getDeviceInfo().getNodeId().toString(), e);
                    count ++ ;
                }
            }
            LOG.warn("Failed to update port status for port {} on device {} even after 3 retries",
                    portStatusMessage.getPortNo().toString(),getDeviceInfo().getNodeId().toString());
        }
    }

    void updatePortStatusMessage(final PortStatusMessage portStatusMessage){
        final FlowCapableNodeConnector flowCapableNodeConnector = portStatusTranslator.translate(portStatusMessage,
                getDeviceInfo(), null);

        final KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> iiToNodeConnector =
                provideIIToNodeConnector(portStatusMessage.getPortNo(), portStatusMessage.getVersion());

        if (portStatusMessage.getReason().equals(PortReason.OFPPRADD) || portStatusMessage.getReason().
                equals(PortReason.OFPPRMODIFY)) {
            // because of ADD status node connector has to be created
            final NodeConnectorBuilder nConnectorBuilder = new NodeConnectorBuilder().setKey(iiToNodeConnector.getKey());
            nConnectorBuilder.addAugmentation(FlowCapableNodeConnectorStatisticsData.class,
                    new FlowCapableNodeConnectorStatisticsDataBuilder().build());
            nConnectorBuilder.addAugmentation(FlowCapableNodeConnector.class, flowCapableNodeConnector);
            writeToTransaction(LogicalDatastoreType.OPERATIONAL, iiToNodeConnector, nConnectorBuilder.build());
        } else if (portStatusMessage.getReason().equals(PortReason.OFPPRDELETE)) {
            addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, iiToNodeConnector);
        }
        submitTransaction();
    }

    private KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> provideIIToNodeConnector(final long portNo, final short version) {
        final InstanceIdentifier<Node> iiToNodes = getDeviceInfo().getNodeInstanceIdentifier();
        final BigInteger dataPathId = getDeviceInfo().getDatapathId();
        final NodeConnectorId nodeConnectorId = NodeStaticReplyTranslatorUtil.nodeConnectorId(dataPathId.toString(), portNo, version);
        return iiToNodes.child(NodeConnector.class, new NodeConnectorKey(nodeConnectorId));
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
        Verify.verify(CONTEXT_STATE.INITIALIZATION.equals(getState()));
        this.state = CONTEXT_STATE.WORKING;
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

    @Override
    public synchronized void shutdownConnection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Shutdown method for node {}", getDeviceInfo().getLOGValue());
        }
        if (CONTEXT_STATE.TERMINATION.equals(getState())) {
            LOG.debug("DeviceCtx for Node {} is in termination process.", getDeviceInfo().getLOGValue());
            return;
        }

        if (ConnectionContext.CONNECTION_STATE.RIP.equals(getPrimaryConnectionContext().getConnectionState())) {
            LOG.debug("ConnectionCtx for Node {} is in RIP state.", getDeviceInfo().getLOGValue());
            return;
        }

        // Terminate Auxiliary Connection
        for (final ConnectionContext connectionContext : auxiliaryConnectionContexts.values()) {
            LOG.debug("Closing auxiliary connection {}", connectionContext.getNodeId());
            connectionContext.closeConnection(false);
        }

        // Terminate Primary Connection
        getPrimaryConnectionContext().closeConnection(true);

        // Close all datastore registries
        if (initialized) {
            deviceGroupRegistry.close();
            deviceFlowRegistry.close();
            deviceMeterRegistry.close();
        }
    }

    @Override
    public ListenableFuture<Void> shuttingDownDataStoreTransactions() {
        return initialized
                ? this.transactionChainManager.shuttingDown()
                : Futures.immediateFuture(null);
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
    public CONTEXT_STATE getState() {
        return this.state;
    }

    @Override
    public ListenableFuture<Void> stopClusterServices(boolean connectionInterrupted) {
        final ListenableFuture<Void> deactivateTxManagerFuture = initialized
                ? transactionChainManager.deactivateTransactionManager()
                : Futures.immediateFuture(null);

        if (!connectionInterrupted) {
            final ListenableFuture<Void> makeSlaveFuture = Futures.transform(makeDeviceSlave(), new Function<RpcResult<SetRoleOutput>, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
                    return null;
                }
            });

            Futures.addCallback(makeSlaveFuture, new FutureCallback<Void>() {
                @Override
                public void onSuccess(@Nullable Void aVoid) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Role SLAVE was successfully propagated on device, node {}", deviceInfo.getLOGValue());
                    }
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    LOG.warn("Was not able to set role SLAVE to device on node {} ", deviceInfo.getLOGValue());
                    LOG.trace("Error occurred on device role setting, probably connection loss: ", throwable);
                }
            });

            return Futures.transform(deactivateTxManagerFuture, new AsyncFunction<Void, Void>() {
                @Override
                public ListenableFuture<Void> apply(Void aVoid) throws Exception {
                    // Add fallback to remove device from operational DS if setting slave fails
                    return Futures.withFallback(makeSlaveFuture, t ->
                            myManager.removeDeviceFromOperationalDS(deviceInfo));
                }
            });
        } else {
            return Futures.transform(deactivateTxManagerFuture, new AsyncFunction<Void, Void>() {
                @Override
                public ListenableFuture<Void> apply(Void aVoid) throws Exception {
                    return myManager.removeDeviceFromOperationalDS(deviceInfo);
                }
            });
        }
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
        if (CONTEXT_STATE.TERMINATION.equals(getState())){
            if (LOG.isDebugEnabled()) {
                LOG.debug("DeviceContext for node {} is already in TERMINATION state.", getDeviceInfo().getLOGValue());
            }
        } else {
            this.state = CONTEXT_STATE.TERMINATION;
        }
    }

    @Override
    public void putLifecycleServiceIntoTxChainManager(final LifecycleService lifecycleService){
        if (initialized) {
            this.transactionChainManager.setLifecycleService(lifecycleService);
        }
    }

    @Override
    public void replaceConnectionContext(final ConnectionContext connectionContext){
        // Act like we are initializing the context
        this.state = CONTEXT_STATE.INITIALIZATION;
        this.primaryConnectionContext = connectionContext;
        this.onPublished();
    }

    @Override
    public boolean isSkipTableFeatures() {
        return this.skipTableFeatures;
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
    public boolean onContextInstantiateService(final ConnectionContext connectionContext) {

        if (getPrimaryConnectionContext().getConnectionState().equals(ConnectionContext.CONNECTION_STATE.RIP)) {
            LOG.warn("Connection on device {} was interrupted, will stop starting master services.", deviceInfo.getLOGValue());
            return false;
        }

        LOG.info("Starting device context cluster services for node {}", deviceInfo.getLOGValue());

        lazyTransactionManagerInitialization();

        this.transactionChainManager.activateTransactionManager();

        try {
            DeviceInitializationUtils.initializeNodeInformation(this, switchFeaturesMandatory, this.convertorExecutor);
        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Device {} cannot be initialized: ", deviceInfo.getLOGValue(), e);
            return false;
        }

        Futures.addCallback(sendRoleChangeToDevice(OfpRole.BECOMEMASTER), new RpcResultFutureCallback());

        return this.clusterInitializationPhaseHandler.onContextInstantiateService(getPrimaryConnectionContext());
    }

    @VisibleForTesting
    void lazyTransactionManagerInitialization() {
        if (!this.initialized) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Transaction chain manager for node {} created", deviceInfo.getLOGValue());
            }
            this.transactionChainManager = new TransactionChainManager(dataBroker, deviceInfo);
            this.deviceFlowRegistry = new DeviceFlowRegistryImpl(dataBroker, deviceInfo.getNodeInstanceIdentifier());
            this.deviceGroupRegistry = new DeviceGroupRegistryImpl();
            this.deviceMeterRegistry = new DeviceMeterRegistryImpl();
            this.initialized = true;
        }
    }

    @Nullable
    @Override
    public <T> RequestContext<T> createRequestContext() {
        return new AbstractRequestContext<T>(deviceInfo.reserveXidForDeviceMessage()) {
            @Override
            public void close() {
            }
        };

    }

    ListenableFuture<RpcResult<SetRoleOutput>> sendRoleChangeToDevice(final OfpRole newRole) {
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

    private class RpcResultFutureCallback implements FutureCallback<RpcResult<SetRoleOutput>> {
        @Override
        public void onSuccess(@Nullable RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Role MASTER was successfully set on device, node {}", deviceInfo.getLOGValue());
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.warn("Was not able to set MASTER role on device, node {}", deviceInfo.getLOGValue());
            shutdownConnection();
        }
    }
}
