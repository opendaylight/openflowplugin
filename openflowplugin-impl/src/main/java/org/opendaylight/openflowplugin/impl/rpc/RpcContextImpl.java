/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.singleton.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.impl.services.SendEchoImpl;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiAddFlow;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiRemoveFlow;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiUpdateFlow;
import org.opendaylight.openflowplugin.impl.services.sal.AddBundleMessagesImpl;
import org.opendaylight.openflowplugin.impl.services.sal.AddFlowsBatchImpl;
import org.opendaylight.openflowplugin.impl.services.sal.AddGroupImpl;
import org.opendaylight.openflowplugin.impl.services.sal.AddGroupsBatchImpl;
import org.opendaylight.openflowplugin.impl.services.sal.AddMeterImpl;
import org.opendaylight.openflowplugin.impl.services.sal.AddMetersBatchImpl;
import org.opendaylight.openflowplugin.impl.services.sal.ControlBundleImpl;
import org.opendaylight.openflowplugin.impl.services.sal.ProcessFlatBatchImpl;
import org.opendaylight.openflowplugin.impl.services.sal.RemoveFlowsBatchImpl;
import org.opendaylight.openflowplugin.impl.services.sal.RemoveGroupImpl;
import org.opendaylight.openflowplugin.impl.services.sal.RemoveGroupsBatchImpl;
import org.opendaylight.openflowplugin.impl.services.sal.RemoveMeterImpl;
import org.opendaylight.openflowplugin.impl.services.sal.RemoveMetersBatchImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SendBarrierImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SendExperimenterImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SendExperimenterMpRequestImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SetConfigImpl;
import org.opendaylight.openflowplugin.impl.services.sal.TransmitPacketImpl;
import org.opendaylight.openflowplugin.impl.services.sal.UpdateFlowsBatchImpl;
import org.opendaylight.openflowplugin.impl.services.sal.UpdateGroupImpl;
import org.opendaylight.openflowplugin.impl.services.sal.UpdateGroupsBatchImpl;
import org.opendaylight.openflowplugin.impl.services.sal.UpdateMeterImpl;
import org.opendaylight.openflowplugin.impl.services.sal.UpdateMetersBatchImpl;
import org.opendaylight.openflowplugin.impl.services.sal.UpdatePortImpl;
import org.opendaylight.openflowplugin.impl.services.sal.UpdateTableImpl;
import org.opendaylight.openflowplugin.impl.services.singlelayer.GetAsyncImpl;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SetAsyncImpl;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleAddFlow;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleRemoveFlow;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleUpdateFlow;
import org.opendaylight.openflowplugin.impl.statistics.services.GetAggregateFlowStatisticsFromFlowTableForGivenMatchImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetAllGroupStatisticsImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetAllMeterConfigStatisticsImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetAllMeterStatisticsImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetAllNodeConnectorsStatisticsImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetAllQueuesStatisticsFromAllPortsImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetAllQueuesStatisticsFromGivenPortImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetFlowTablesStatisticsImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetGroupDescriptionImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetGroupFeaturesImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetGroupStatisticsImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetMeterFeaturesImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetMeterStatisticsImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetNodeConnectorStatisticsImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.GetQueueStatisticsFromGivenPortImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.GetAggregateFlowStatisticsFromFlowTableForAllFlowsImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.GetAllFlowStatisticsFromFlowTableImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.GetAllFlowsStatisticsFromAllFlowTablesImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.GetFlowStatisticsFromFlowTableImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer.MultiGetFlowStatistics;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer.MultiGetGroupStatistics;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer.MultiGetMeterStatistics;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer.MultiGetNodeConnectorStatistics;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer.MultiGetQueueStatistics;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer.SingleGetFlowStatistics;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer.SingleGetGroupStatistics;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer.SingleGetMeterStatistics;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer.SingleGetNodeConnectorStatistics;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer.SingleGetQueueStatistics;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEcho;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTables;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundle;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPorts;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTable;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RpcContextImpl implements RpcContext {
    private static final Logger LOG = LoggerFactory.getLogger(RpcContextImpl.class);

    private final RpcProviderService rpcProviderRegistry;
    private final MessageSpy messageSpy;
    private final Semaphore tracker;
    private final boolean isStatisticsRpcEnabled;
    private final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;
    private final DeviceInfo deviceInfo;
    private final DeviceContext deviceContext;
    private final ExtensionConverterProvider extensionConverterProvider;
    private final ConvertorExecutor convertorExecutor;
    private final NotificationPublishService notificationPublishService;

    private ContextChainMastershipWatcher contextChainMastershipWatcher = null;
    private Registration rpcRegistration;

    RpcContextImpl(@NonNull final RpcProviderService rpcProviderRegistry,
                   final int maxRequests,
                   @NonNull final DeviceContext deviceContext,
                   @NonNull final ExtensionConverterProvider extensionConverterProvider,
                   @NonNull final ConvertorExecutor convertorExecutor,
                   @NonNull final NotificationPublishService notificationPublishService,
                   final boolean statisticsRpcEnabled) {
        this.deviceContext = deviceContext;
        deviceInfo = deviceContext.getDeviceInfo();
        nodeInstanceIdentifier = deviceContext.getDeviceInfo().getNodeInstanceIdentifier();
        messageSpy = deviceContext.getMessageSpy();
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.extensionConverterProvider = extensionConverterProvider;
        this.notificationPublishService = notificationPublishService;
        this.convertorExecutor = convertorExecutor;
        isStatisticsRpcEnabled = statisticsRpcEnabled;
        tracker = new Semaphore(maxRequests, true);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }

    @Override
    public void close() {
        unregisterRPCs();
    }

    private void unregisterRPCs() {
        if (rpcRegistration != null) {
            LOG.debug("Closing RPC registrations for device {}.", nodeInstanceIdentifier.getKey().getId().getValue());
            rpcRegistration.close();
            rpcRegistration = null;
        }
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        if (!tracker.tryAcquire()) {
            LOG.trace("Device queue {} at capacity", this);
            return null;
        }

        LOG.trace("Acquired semaphore for {}, available permits:{} ",
            nodeInstanceIdentifier.getKey().getId().getValue(), tracker.availablePermits());

        final var xid = deviceInfo.reserveXidForDeviceMessage();
        if (xid == null) {
            LOG.warn("Xid cannot be reserved for new RequestContext, node:{}",
                    nodeInstanceIdentifier.getKey().getId().getValue());
            tracker.release();
            return null;
        }

        return new AbstractRequestContext<>(xid) {
            @Override
            public void close() {
                tracker.release();
                LOG.trace("Removed request context with xid {}", getXid().getValue());
                messageSpy.spyMessage(RpcContextImpl.class, MessageSpy.StatisticsGroup.REQUEST_STACK_FREED);
            }
        };
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public void registerMastershipWatcher(final ContextChainMastershipWatcher newWatcher) {
        contextChainMastershipWatcher = newWatcher;
    }

    @Override
    public ListenableFuture<?> closeServiceInstance() {
        unregisterRPCs();
        return Futures.immediateVoidFuture();
    }

    @Override
    public void instantiateServiceInstance() {
        final var singleLayer = deviceContext.canUseSingleLayerSerialization();

        // flow-capable-transaction.yang
        final var sendBarrier = new SendBarrierImpl(this, deviceContext);

        // sal-experimenter-message.yang
        final var sendExperimenter = new SendExperimenterImpl(this, deviceContext, extensionConverterProvider);

        // sal-flow.yang
        final var addFlow = singleLayer ? new SingleAddFlow(this, deviceContext)
            : new MultiAddFlow(this, deviceContext, convertorExecutor);
        final var removeFlow = singleLayer ? new SingleRemoveFlow(this, deviceContext)
            : new MultiRemoveFlow(this, deviceContext, convertorExecutor);
        final var updateFlow = singleLayer ? new SingleUpdateFlow(this, deviceContext)
            : new MultiUpdateFlow(this, deviceContext, convertorExecutor);

        // sal-group.yang
        final var addGroup = new AddGroupImpl(this, deviceContext, convertorExecutor);
        final var removeGroup = new RemoveGroupImpl(this, deviceContext, convertorExecutor);
        final var updateGroup = new UpdateGroupImpl(this, deviceContext, convertorExecutor);

        // sal-meter.yang
        final var addMeter = new AddMeterImpl(this, deviceContext, convertorExecutor);
        final var removeMeter = new RemoveMeterImpl(this, deviceContext, convertorExecutor);
        final var updateMeter = new UpdateMeterImpl(this, deviceContext, convertorExecutor);

        // FIXME: Use multipart writer provider from device context
        final var multipartWriterProvider = MultipartWriterProviderFactory.createDefaultProvider(deviceContext);

        final var builder = ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(SendBarrier.class, sendBarrier)
            // node-config.yang
            .put(SetConfig.class, new SetConfigImpl(this, deviceContext))
            // packet-processing.yang
            .put(TransmitPacket.class, new TransmitPacketImpl(this, deviceContext, convertorExecutor))
            // sal-async-config.yang
            .put(GetAsync.class, new GetAsyncImpl(this, deviceContext))
            .put(SetAsync.class, new SetAsyncImpl(this, deviceContext))
            // sal-echo.yang
            .put(SendEcho.class, new SendEchoImpl(this, deviceContext))
            .put(SendExperimenter.class, sendExperimenter)
            // sal-bundle.yang (ONF extension?)
            .put(ControlBundle.class, new ControlBundleImpl(sendExperimenter))
            .put(AddBundleMessages.class, new AddBundleMessagesImpl(sendExperimenter))
            // sal-experimenter-mp-message.yang
            .put(SendExperimenterMpRequest.class, new SendExperimenterMpRequestImpl(this, deviceContext,
                extensionConverterProvider))
            .put(AddFlow.class, addFlow)
            .put(RemoveFlow.class, removeFlow)
            .put(UpdateFlow.class, updateFlow)
            .put(AddGroup.class, addGroup)
            .put(RemoveGroup.class, removeGroup)
            .put(UpdateGroup.class, updateGroup)
            .put(AddMeter.class, addMeter)
            .put(RemoveMeter.class, removeMeter)
            .put(UpdateMeter.class, updateMeter)
            // sal-port.yang
            .put(UpdatePort.class, new UpdatePortImpl(this, deviceContext, convertorExecutor))
            // sal-flat-batch.yang
            .put(ProcessFlatBatch.class, new ProcessFlatBatchImpl(
                // sal-flows-batch.yang
                // FIXME: register these?
                new AddFlowsBatchImpl(addFlow, sendBarrier),
                new RemoveFlowsBatchImpl(removeFlow, sendBarrier),
                new UpdateFlowsBatchImpl(updateFlow, sendBarrier),
                // sal-groups-batch.yang
                // FIXME: register these?
                new AddGroupsBatchImpl(addGroup, sendBarrier),
                new RemoveGroupsBatchImpl(removeGroup, sendBarrier),
                new UpdateGroupsBatchImpl(updateGroup, sendBarrier),
                // sal-meters-batch.yang
                // FIXME: register these?
                new AddMetersBatchImpl(addMeter, sendBarrier),
                new RemoveMetersBatchImpl(removeMeter, sendBarrier),
                new UpdateMetersBatchImpl(updateMeter, sendBarrier)))
            // sal-table.yang
            .put(UpdateTable.class, new UpdateTableImpl(this, deviceContext, convertorExecutor,
                multipartWriterProvider))
            // opendaylight-flow-statistics.yang
            .put(GetAggregateFlowStatisticsFromFlowTableForGivenMatch.class,
                new GetAggregateFlowStatisticsFromFlowTableForGivenMatchImpl(this, deviceContext, convertorExecutor))
            // opendaylight-direct-statistics.yang
            .put(GetFlowStatistics.class, singleLayer
                ? new SingleGetFlowStatistics(this, deviceContext, convertorExecutor, multipartWriterProvider)
                : new MultiGetFlowStatistics(this, deviceContext, convertorExecutor, multipartWriterProvider))
            .put(GetGroupStatistics.class, singleLayer
                ? new SingleGetGroupStatistics(this, deviceContext, convertorExecutor, multipartWriterProvider)
                : new MultiGetGroupStatistics(this, deviceContext, convertorExecutor, multipartWriterProvider))
            .put(GetQueueStatistics.class, singleLayer
                ? new SingleGetQueueStatistics(this, deviceContext, convertorExecutor, multipartWriterProvider)
                : new MultiGetQueueStatistics(this, deviceContext, convertorExecutor, multipartWriterProvider))
            .put(GetMeterStatistics.class, singleLayer
                ? new SingleGetMeterStatistics(this, deviceContext, convertorExecutor, multipartWriterProvider)
                : new MultiGetMeterStatistics(this, deviceContext, convertorExecutor, multipartWriterProvider))
            .put(GetNodeConnectorStatistics.class, singleLayer
                ? new SingleGetNodeConnectorStatistics(this, deviceContext, convertorExecutor, multipartWriterProvider)
                : new MultiGetNodeConnectorStatistics(this, deviceContext, convertorExecutor, multipartWriterProvider));

        // Support deprecated statistic related services for backward compatibility. The only exception from deprecation
        // is the aggregated flow statistic with match criteria input.
        if (isStatisticsRpcEnabled && !singleLayer) {
            final var compatibilityXidSeed = new AtomicLong();
            // FIXME: why is this separate?
            final var statsCompatXidSeed = new AtomicLong();

            builder
                // Legacy RPCs
                .put(GetAggregateFlowStatisticsFromFlowTableForAllFlows.class,
                    new GetAggregateFlowStatisticsFromFlowTableForAllFlowsImpl(this, deviceContext, convertorExecutor,
                        statsCompatXidSeed, notificationPublishService))
                .put(GetAllFlowStatisticsFromFlowTable.class,
                    new GetAllFlowStatisticsFromFlowTableImpl(this, deviceContext, convertorExecutor,
                        statsCompatXidSeed, notificationPublishService))
                .put(GetAllFlowsStatisticsFromAllFlowTables.class,
                    new GetAllFlowsStatisticsFromAllFlowTablesImpl(this, deviceContext, convertorExecutor,
                        statsCompatXidSeed, notificationPublishService))
                .put(GetFlowStatisticsFromFlowTable.class,
                    new GetFlowStatisticsFromFlowTableImpl(this, deviceContext, convertorExecutor, statsCompatXidSeed,
                        notificationPublishService))

                // register all statistics (deprecated) services
                .put(GetFlowTablesStatistics.class,
                    new GetFlowTablesStatisticsImpl(this, deviceContext, compatibilityXidSeed,
                        notificationPublishService))
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatistics.class,
                    new GetGroupStatisticsImpl(this, deviceContext, compatibilityXidSeed, notificationPublishService,
                        convertorExecutor))
                .put(GetAllGroupStatistics.class,
                    new GetAllGroupStatisticsImpl(this, deviceContext, compatibilityXidSeed, notificationPublishService,
                        convertorExecutor))
                .put(GetGroupDescription.class,
                    new GetGroupDescriptionImpl(this, deviceContext, compatibilityXidSeed, notificationPublishService,
                        convertorExecutor))
                .put(GetGroupFeatures.class,
                    new GetGroupFeaturesImpl(this, deviceContext, compatibilityXidSeed, notificationPublishService,
                        convertorExecutor))
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatistics.class,
                    new GetMeterStatisticsImpl(this, deviceContext, compatibilityXidSeed, notificationPublishService,
                        convertorExecutor))
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111
                        .GetAllMeterStatistics.class,
                    new GetAllMeterStatisticsImpl(this, deviceContext, compatibilityXidSeed, notificationPublishService,
                        convertorExecutor))
                .put(GetAllMeterConfigStatistics.class,
                    new GetAllMeterConfigStatisticsImpl(this, deviceContext, compatibilityXidSeed,
                        notificationPublishService, convertorExecutor))
                .put(GetMeterFeatures.class,
                    new GetMeterFeaturesImpl(this, deviceContext, compatibilityXidSeed, notificationPublishService,
                        convertorExecutor))
                .put(GetQueueStatisticsFromGivenPort.class,
                    new GetQueueStatisticsFromGivenPortImpl(this, deviceContext, compatibilityXidSeed,
                        notificationPublishService))
                .put(GetAllQueuesStatisticsFromAllPorts.class,
                    new GetAllQueuesStatisticsFromAllPortsImpl(this, deviceContext, compatibilityXidSeed,
                        notificationPublishService))
                .put(GetAllQueuesStatisticsFromGivenPort.class,
                    new GetAllQueuesStatisticsFromGivenPortImpl(this, deviceContext, compatibilityXidSeed,
                        notificationPublishService))
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214
                    .GetNodeConnectorStatistics.class,
                    new GetNodeConnectorStatisticsImpl(this, deviceContext, compatibilityXidSeed,
                        notificationPublishService))
                .put(GetAllNodeConnectorsStatistics.class,
                    new GetAllNodeConnectorsStatisticsImpl(this, deviceContext, compatibilityXidSeed,
                        notificationPublishService));
        }

        rpcRegistration = rpcProviderRegistry.registerRpcImplementations(builder.build(),
            Set.of(nodeInstanceIdentifier));

        final var local = contextChainMastershipWatcher;
        if (local != null) {
            local.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.RPC_REGISTRATION);
        }
    }
}
