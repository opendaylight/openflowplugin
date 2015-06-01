/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.ConnectionException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.openflowplugin.impl.common.NodeStaticReplyTranslatorUtil;
import org.opendaylight.openflowplugin.impl.connection.OutboundQueueProviderImpl;
import org.opendaylight.openflowplugin.impl.device.listener.OpenflowProtocolListenerFullImpl;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.MultipartReplyPortDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DeviceManagerImpl implements DeviceManager, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);

    private static final long TICK_DURATION = 10; // 0.5 sec.
    private ScheduledThreadPoolExecutor spyPool;
    private final int spyRate = 10;

    private final DataBroker dataBroker;
    private final HashedWheelTimer hashedWheelTimer;
    private TranslatorLibrary translatorLibrary;
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    private NotificationService notificationService;
    private NotificationPublishService notificationPublishService;

    private final List<DeviceContext> deviceContexts = new ArrayList<DeviceContext>();
    private final MessageIntelligenceAgency messageIntelligenceAgency;

    private final long barrierNanos = TimeUnit.MILLISECONDS.toNanos(500);
    private final int maxQueueDepth = 25600;

    public DeviceManagerImpl(@Nonnull final DataBroker dataBroker,
                             @Nonnull final MessageIntelligenceAgency messageIntelligenceAgency) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, 500);
        /* merge empty nodes to oper DS to predict any problems with missing parent for Node */
        final WriteTransaction tx = dataBroker.newWriteOnlyTransaction();

        final NodesBuilder nodesBuilder = new NodesBuilder();
        nodesBuilder.setNode(Collections.<Node>emptyList());
        tx.merge(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(Nodes.class), nodesBuilder.build());
        try {
            tx.submit().get();
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Creation of node failed.", e);
            throw new IllegalStateException(e);
        }

        this.messageIntelligenceAgency = messageIntelligenceAgency;
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceContext deviceContext) {
        // final phase - we have to add new Device to MD-SAL DataStore
        Preconditions.checkNotNull(deviceContext);
        try {
            ((DeviceContextImpl) deviceContext).initialSubmitTransaction();
        } catch (Exception e) {
            LOG.info("Failed to submit tx for node's initial data collection for node {}", deviceContext.getDeviceState().getNodeId().toString());
            LOG.debug("Initial node data collection for node {} not collected due :", deviceContext.getDeviceState().getNodeId().toString(), e);
        }

        deviceContext.onPublished();
    }

    @Override
    public void deviceConnected(@CheckForNull final ConnectionContext connectionContext) {
        Preconditions.checkArgument(connectionContext != null);

        // Cache this for clarity
        final ConnectionAdapter connectionAdapter = connectionContext.getConnectionAdapter();

        //FIXME: as soon as auxiliary connection are fully supported then this is needed only before device context published
        connectionAdapter.setPacketInFiltering(true);

        final Short version = connectionContext.getFeatures().getVersion();
        final OutboundQueueProvider outboundQueueProvider = new OutboundQueueProviderImpl(version);

        connectionContext.setOutboundQueueProvider(outboundQueueProvider);
        final OutboundQueueHandlerRegistration<OutboundQueueProvider> outboundQueueHandlerRegistration =
                connectionAdapter.registerOutboundQueueHandler(outboundQueueProvider, maxQueueDepth, barrierNanos);
        connectionContext.setOutboundQueueHandleRegistration(outboundQueueHandlerRegistration);

        final DeviceState deviceState = new DeviceStateImpl(connectionContext.getFeatures(), connectionContext.getNodeId());

        final DeviceContext deviceContext = new DeviceContextImpl(connectionContext, deviceState, dataBroker,
                hashedWheelTimer, messageIntelligenceAgency, outboundQueueProvider);
        deviceContext.setNotificationService(notificationService);
        deviceContext.setNotificationPublishService(notificationPublishService);
        final NodeBuilder nodeBuilder = new NodeBuilder().setId(deviceState.getNodeId()).setNodeConnector(Collections.<NodeConnector>emptyList());
        try {
            deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, deviceState.getNodeInstanceIdentifier(), nodeBuilder.build());
        } catch (Exception e) {
            LOG.debug("Failed to write node to DS ", e);
        }

        connectionContext.setDeviceDisconnectedHandler(deviceContext);
        deviceContext.setTranslatorLibrary(translatorLibrary);
        deviceContext.addDeviceContextClosedHandler(this);

        final OpenflowProtocolListenerFullImpl messageListener = new OpenflowProtocolListenerFullImpl(
                connectionAdapter, deviceContext);
        connectionAdapter.setMessageListener(messageListener);

        final ListenableFuture<List<RpcResult<List<MultipartReply>>>> deviceFeaturesFuture;
        if (OFConstants.OFP_VERSION_1_0 == version) {
            final CapabilitiesV10 capabilitiesV10 = connectionContext.getFeatures().getCapabilitiesV10();

            DeviceStateUtil.setDeviceStateBasedOnV10Capabilities(deviceState, capabilitiesV10);

            deviceFeaturesFuture = createDeviceFeaturesForOF10(deviceContext, deviceState);
            // create empty tables after device description is processed
            chainTableTrunkWriteOF10(deviceContext, deviceFeaturesFuture);

            final short ofVersion = deviceContext.getDeviceState().getVersion();
            final TranslatorKey translatorKey = new TranslatorKey(ofVersion, PortGrouping.class.getName());
            final MessageTranslator<PortGrouping, FlowCapableNodeConnector> translator = deviceContext.oook().lookupTranslator(translatorKey);
            final BigInteger dataPathId = deviceContext.getPrimaryConnectionContext().getFeatures().getDatapathId();

            for (final PortGrouping port : connectionContext.getFeatures().getPhyPort()) {
                final FlowCapableNodeConnector fcNodeConnector = translator.translate(port, deviceContext, null);

                final NodeConnectorId nodeConnectorId = NodeStaticReplyTranslatorUtil.nodeConnectorId(dataPathId.toString(), port.getPortNo(), ofVersion);
                final NodeConnectorBuilder ncBuilder = new NodeConnectorBuilder().setId(nodeConnectorId);
                ncBuilder.addAugmentation(FlowCapableNodeConnector.class, fcNodeConnector);
                ncBuilder.addAugmentation(FlowCapableNodeConnectorStatisticsData.class, new FlowCapableNodeConnectorStatisticsDataBuilder().build());
                final NodeConnector connector = ncBuilder.build();
                final InstanceIdentifier<NodeConnector> connectorII = deviceState.getNodeInstanceIdentifier().child(NodeConnector.class, connector.getKey());
                try {
                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, connectorII, connector);
                } catch (Exception e) {
                    LOG.debug("Failed to write node {} to DS ", deviceContext.getDeviceState().getNodeId().toString(), e);
                }

            }
        } else if (OFConstants.OFP_VERSION_1_3 == version) {
            final Capabilities capabilities = connectionContext.getFeatures().getCapabilities();
            LOG.debug("Setting capabilities for device {}", deviceContext.getDeviceState().getNodeId());
            DeviceStateUtil.setDeviceStateBasedOnV13Capabilities(deviceState, capabilities);
            deviceFeaturesFuture = createDeviceFeaturesForOF13(deviceContext, deviceState);
        } else {
            deviceFeaturesFuture = Futures.immediateFailedFuture(new ConnectionException("Unsupported version " + version));
        }

        Futures.addCallback(deviceFeaturesFuture, new FutureCallback<List<RpcResult<List<MultipartReply>>>>() {
            @Override
            public void onSuccess(final List<RpcResult<List<MultipartReply>>> result) {
                deviceContext.getDeviceState().setValid(true);
                deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
                LOG.trace("Device context level up called.");
            }

            @Override
            public void onFailure(final Throwable t) {
                // FIXME : remove session
                LOG.trace("Device capabilities gathering future failed.");
                LOG.trace("more info in exploration failure..", t);
            }
        });
    }

    private static void chainTableTrunkWriteOF10(final DeviceContext deviceContext, final ListenableFuture<List<RpcResult<List<MultipartReply>>>> deviceFeaturesFuture) {
        Futures.addCallback(deviceFeaturesFuture, new FutureCallback<List<RpcResult<List<MultipartReply>>>>() {
            @Override
            public void onSuccess(final List<RpcResult<List<MultipartReply>>> results) {
                boolean allSucceeded = true;
                for (final RpcResult<List<MultipartReply>> rpcResult : results) {
                    allSucceeded &= rpcResult.isSuccessful();
                }
                if (allSucceeded) {
                    createEmptyFlowCapableNodeInDs(deviceContext);
                    makeEmptyTables(deviceContext, deviceContext.getDeviceState().getNodeInstanceIdentifier(),
                            deviceContext.getDeviceState().getFeatures().getTables());
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                //NOOP
            }
        });
    }


    private static ListenableFuture<List<RpcResult<List<MultipartReply>>>> createDeviceFeaturesForOF10(final DeviceContext deviceContext,
                                                                                                       final DeviceState deviceState) {
        final ListenableFuture<RpcResult<List<MultipartReply>>> replyDesc = getNodeStaticInfo(MultipartType.OFPMPDESC,
                deviceContext,
                deviceState.getNodeInstanceIdentifier(),
                deviceState.getVersion());

        return Futures.allAsList(Arrays.asList(replyDesc));
    }

    private static ListenableFuture<List<RpcResult<List<MultipartReply>>>> createDeviceFeaturesForOF13(final DeviceContext deviceContext,
                                                                                                       final DeviceState deviceState) {

        final ListenableFuture<RpcResult<List<MultipartReply>>> replyDesc = getNodeStaticInfo(MultipartType.OFPMPDESC,
                deviceContext,
                deviceState.getNodeInstanceIdentifier(),
                deviceState.getVersion());

        //first process description reply, write data to DS and write consequent data if successful
        return Futures.transform(replyDesc, new AsyncFunction<RpcResult<List<MultipartReply>>, List<RpcResult<List<MultipartReply>>>>() {
            @Override
            public ListenableFuture<List<RpcResult<List<MultipartReply>>>> apply(final RpcResult<List<MultipartReply>> rpcResult) throws Exception {

                translateAndWriteReply(MultipartType.OFPMPDESC, deviceContext, deviceState.getNodeInstanceIdentifier(), rpcResult.getResult());

                final ListenableFuture<RpcResult<List<MultipartReply>>> replyMeterFeature = getNodeStaticInfo(MultipartType.OFPMPMETERFEATURES,
                        deviceContext,
                        deviceState.getNodeInstanceIdentifier(),
                        deviceState.getVersion());

                createSuccessProcessingCallback(MultipartType.OFPMPMETERFEATURES,
                        deviceContext,
                        deviceState.getNodeInstanceIdentifier(),
                        replyMeterFeature);

                final ListenableFuture<RpcResult<List<MultipartReply>>> replyGroupFeatures = getNodeStaticInfo(MultipartType.OFPMPGROUPFEATURES,
                        deviceContext,
                        deviceState.getNodeInstanceIdentifier(),
                        deviceState.getVersion());
                createSuccessProcessingCallback(MultipartType.OFPMPGROUPFEATURES,
                        deviceContext,
                        deviceState.getNodeInstanceIdentifier(),
                        replyGroupFeatures);

                final ListenableFuture<RpcResult<List<MultipartReply>>> replyTableFeatures = getNodeStaticInfo(MultipartType.OFPMPTABLEFEATURES,
                        deviceContext,
                        deviceState.getNodeInstanceIdentifier(),
                        deviceState.getVersion());
                createSuccessProcessingCallback(MultipartType.OFPMPTABLEFEATURES,
                        deviceContext,
                        deviceState.getNodeInstanceIdentifier(),
                        replyTableFeatures);

                final ListenableFuture<RpcResult<List<MultipartReply>>> replyPortDescription = getNodeStaticInfo(MultipartType.OFPMPPORTDESC,
                        deviceContext,
                        deviceState.getNodeInstanceIdentifier(),
                        deviceState.getVersion());
                createSuccessProcessingCallback(MultipartType.OFPMPPORTDESC,
                        deviceContext,
                        deviceState.getNodeInstanceIdentifier(),
                        replyPortDescription);


                return Futures.allAsList(Arrays.asList(
                        replyMeterFeature,
                        replyGroupFeatures,
//                        replyTableFeatures,
                        replyPortDescription));
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

    private static ListenableFuture<RpcResult<List<MultipartReply>>> getNodeStaticInfo(final MultipartType type, final DeviceContext deviceContext,
                                                                                       final InstanceIdentifier<Node> nodeII, final short version) {

        final OutboundQueue queue = deviceContext.getPrimaryConnectionContext().getOutboundQueueProvider();

        final Long reserved = deviceContext.getReservedXid();
        final RequestContext<List<MultipartReply>> requestContext = new AbstractRequestContext<List<MultipartReply>>(reserved) {
            @Override
            public void close() {
                //NOOP
            }
        };

        final Xid xid = requestContext.getXid();

        LOG.trace("Hooking xid {} to device context - precaution.", reserved);

        final MultiMsgCollector multiMsgCollector = deviceContext.getMultiMsgCollector(requestContext);
        queue.commitEntry(xid.getValue(), MultipartRequestInputFactory.makeMultipartRequestInput(xid.getValue(), version, type), new FutureCallback<OfHeader>() {
            @Override
            public void onSuccess(final OfHeader ofHeader) {
                if (ofHeader instanceof MultipartReply) {
                    final MultipartReply multipartReply = (MultipartReply) ofHeader;
                    multiMsgCollector.addMultipartMsg(multipartReply);
                } else if (null != ofHeader) {
                    LOG.info("Unexpected response type received {}.", ofHeader.getClass());
                } else {
                    multiMsgCollector.endCollecting();
                    LOG.info("Response received is null.");
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.info("Fail response from OutboundQueue for multipart type {}.", type);
                requestContext.close();
                if (MultipartType.OFPMPTABLEFEATURES.equals(type)) {
                    makeEmptyTables(deviceContext, nodeII, deviceContext.getPrimaryConnectionContext().getFeatures().getTables());
                }
            }
        });

        return requestContext.getFuture();
    }

    private static void createSuccessProcessingCallback(final MultipartType type, final DeviceContext deviceContext, final InstanceIdentifier<Node> nodeII, final ListenableFuture<RpcResult<List<MultipartReply>>> requestContextFuture) {
        Futures.addCallback(requestContextFuture, new FutureCallback<RpcResult<List<MultipartReply>>>() {
            @Override
            public void onSuccess(final RpcResult<List<MultipartReply>> rpcResult) {
                final List<MultipartReply> result = rpcResult.getResult();
                if (result != null) {
                    LOG.info("Static node {} info: {} collected", nodeII.toString(), type);
                    translateAndWriteReply(type, deviceContext, nodeII, result);
                } else {
                    final Iterator<RpcError> rpcErrorIterator = rpcResult.getErrors().iterator();
                    while (rpcErrorIterator.hasNext()) {
                        final RpcError rpcError = rpcErrorIterator.next();
                        LOG.info("Failed to retrieve static node {} info: {}", type, rpcError.getMessage());
                        if (null != rpcError.getCause()) {
                            LOG.trace("Detailed error:", rpcError.getCause());
                        }
                    }
                    if (MultipartType.OFPMPTABLEFEATURES.equals(type)) {
                        makeEmptyTables(deviceContext, nodeII, deviceContext.getPrimaryConnectionContext().getFeatures().getTables());
                    }
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.info("Request of type {} for static info of node {} failed.", type, nodeII);
            }
        });
    }

    // FIXME : remove after ovs tableFeatures fix
    private static void makeEmptyTables(final DeviceContext dContext, final InstanceIdentifier<Node> nodeII, final Short nrOfTables) {
        LOG.debug("About to create {} empty tables.", nrOfTables);
        for (int i = 0; i < nrOfTables; i++) {
            final short tId = (short) i;
            final InstanceIdentifier<Table> tableII = nodeII.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tId));
            final TableBuilder tableBuilder = new TableBuilder().setId(tId).addAugmentation(FlowTableStatisticsData.class, new FlowTableStatisticsDataBuilder().build());

            try {
                dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tableII, tableBuilder.build());
            } catch (Exception e) {
                LOG.debug("Failed to write node {} to DS ", dContext.getDeviceState().getNodeId().toString(), e);
            }

        }
    }

    private static void translateAndWriteReply(final MultipartType type, final DeviceContext dContext,
                                               final InstanceIdentifier<Node> nodeII, final Collection<MultipartReply> result) {
        try {
            for (final MultipartReply reply : result) {
                final MultipartReplyBody body = reply.getMultipartReplyBody();
                switch (type) {
                    case OFPMPDESC:
                        Preconditions.checkArgument(body instanceof MultipartReplyDescCase);
                        final MultipartReplyDesc replyDesc = ((MultipartReplyDescCase) body).getMultipartReplyDesc();
                        final FlowCapableNode fcNode = NodeStaticReplyTranslatorUtil.nodeDescTranslator(replyDesc);
                        final InstanceIdentifier<FlowCapableNode> fNodeII = nodeII.augmentation(FlowCapableNode.class);
                        dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, fNodeII, fcNode);
                        break;

                    case OFPMPTABLEFEATURES:
                        Preconditions.checkArgument(body instanceof MultipartReplyTableFeaturesCase);
                        final MultipartReplyTableFeatures tableFeatures = ((MultipartReplyTableFeaturesCase) body).getMultipartReplyTableFeatures();
                        final List<TableFeatures> tables = NodeStaticReplyTranslatorUtil.nodeTableFeatureTranslator(tableFeatures);
                        for (final TableFeatures table : tables) {
                            final Short tableId = table.getTableId();
                            final InstanceIdentifier<Table> tableII = nodeII.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tableId));
                            final TableBuilder tableBuilder = new TableBuilder().setId(tableId).setTableFeatures(Collections.singletonList(table));
                            tableBuilder.addAugmentation(FlowTableStatisticsData.class, new FlowTableStatisticsDataBuilder().build());
                            dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tableII, tableBuilder.build());
                        }
                        break;

                    case OFPMPMETERFEATURES:
                        Preconditions.checkArgument(body instanceof MultipartReplyMeterFeaturesCase);
                        final MultipartReplyMeterFeatures meterFeatures = ((MultipartReplyMeterFeaturesCase) body).getMultipartReplyMeterFeatures();
                        final NodeMeterFeatures mFeature = NodeStaticReplyTranslatorUtil.nodeMeterFeatureTranslator(meterFeatures);
                        final InstanceIdentifier<NodeMeterFeatures> mFeatureII = nodeII.augmentation(NodeMeterFeatures.class);
                        dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, mFeatureII, mFeature);
                        if (0L < mFeature.getMeterFeatures().getMaxMeter().getValue()) {
                            dContext.getDeviceState().setMeterAvailable(true);
                        }
                        break;

                    case OFPMPGROUPFEATURES:
                        Preconditions.checkArgument(body instanceof MultipartReplyGroupFeaturesCase);
                        final MultipartReplyGroupFeatures groupFeatures = ((MultipartReplyGroupFeaturesCase) body).getMultipartReplyGroupFeatures();
                        final NodeGroupFeatures gFeature = NodeStaticReplyTranslatorUtil.nodeGroupFeatureTranslator(groupFeatures);
                        final InstanceIdentifier<NodeGroupFeatures> gFeatureII = nodeII.augmentation(NodeGroupFeatures.class);
                        dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, gFeatureII, gFeature);
                        break;

                    case OFPMPPORTDESC:
                        Preconditions.checkArgument(body instanceof MultipartReplyPortDescCase);
                        final MultipartReplyPortDesc portDesc = ((MultipartReplyPortDescCase) body).getMultipartReplyPortDesc();
                        for (final PortGrouping port : portDesc.getPorts()) {
                            final short ofVersion = dContext.getDeviceState().getVersion();
                            final TranslatorKey translatorKey = new TranslatorKey(ofVersion, PortGrouping.class.getName());
                            final MessageTranslator<PortGrouping, FlowCapableNodeConnector> translator = dContext.oook().lookupTranslator(translatorKey);
                            final FlowCapableNodeConnector fcNodeConnector = translator.translate(port, dContext, null);

                            final BigInteger dataPathId = dContext.getPrimaryConnectionContext().getFeatures().getDatapathId();
                            final NodeConnectorId nodeConnectorId = NodeStaticReplyTranslatorUtil.nodeConnectorId(dataPathId.toString(), port.getPortNo(), ofVersion);
                            final NodeConnectorBuilder ncBuilder = new NodeConnectorBuilder().setId(nodeConnectorId);
                            ncBuilder.addAugmentation(FlowCapableNodeConnector.class, fcNodeConnector);

                            ncBuilder.addAugmentation(FlowCapableNodeConnectorStatisticsData.class, new FlowCapableNodeConnectorStatisticsDataBuilder().build());
                            final NodeConnector connector = ncBuilder.build();

                            final InstanceIdentifier<NodeConnector> connectorII = nodeII.child(NodeConnector.class, connector.getKey());
                            dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, connectorII, connector);
                        }

                        break;

                    default:
                        throw new IllegalArgumentException("Unnexpected MultipartType " + type);
                }
            }
        } catch (Exception e) {
            LOG.debug("Failed to write node {} to DS ", dContext.getDeviceState().getNodeId().toString(), e);
        }
    }

    @Override
    public void setNotificationService(final NotificationService notificationServiceParam) {
        notificationService = notificationServiceParam;
    }

    @Override
    public void setNotificationPublishService(final NotificationPublishService notificationService) {
        notificationPublishService = notificationService;
    }

    @Override
    public void close() throws Exception {
        for (final DeviceContext deviceContext : deviceContexts) {
            deviceContext.close();
        }
    }

    private static void createEmptyFlowCapableNodeInDs(final DeviceContext deviceContext) {
        final FlowCapableNodeBuilder flowCapableNodeBuilder = new FlowCapableNodeBuilder();
        final InstanceIdentifier<FlowCapableNode> fNodeII = deviceContext.getDeviceState().getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);
        try {
            deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, fNodeII, flowCapableNodeBuilder.build());
        } catch (Exception e) {
            LOG.debug("Failed to write node {} to DS ", deviceContext.getDeviceState().getNodeId().toString(), e);
        }
    }

    @Override
    public void onDeviceContextClosed(final DeviceContext deviceContext) {
        deviceContexts.remove(deviceContext);
    }

    @Override
    public void initialize() {
        spyPool = new ScheduledThreadPoolExecutor(1);
        spyPool.scheduleAtFixedRate(messageIntelligenceAgency, spyRate, spyRate, TimeUnit.SECONDS);
    }
}
