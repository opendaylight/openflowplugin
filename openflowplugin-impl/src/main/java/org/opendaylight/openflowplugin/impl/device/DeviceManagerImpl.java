/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.HashedWheelTimer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.openflowplugin.impl.common.NodeStaticReplyTranslatorUtil;
import org.opendaylight.openflowplugin.impl.device.listener.OpenflowProtocolListenerFullImpl;
import org.opendaylight.openflowplugin.impl.rpc.RequestContextImpl;
import org.opendaylight.openflowplugin.impl.services.OFJResult2RequestCtxFuture;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyImpl;
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

    private static final long TICK_DURATION = 500; // 0.5 sec.
    private ScheduledThreadPoolExecutor spyPool;
    private int spyRate = 10;

    private final DataBroker dataBroker;
    private final HashedWheelTimer hashedWheelTimer;
    private RequestContextStack emptyRequestContextStack;
    private TranslatorLibrary translatorLibrary;
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    private NotificationProviderService notificationService;
    private final List<DeviceContext> synchronizedDeviceContextsList = Collections
            .<DeviceContext>synchronizedList(new ArrayList<DeviceContext>());
    private final MessageIntelligenceAgency messageIntelligenceAgency = new MessageIntelligenceAgencyImpl();

    public DeviceManagerImpl(@Nonnull final DataBroker dataBroker) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, 10);
        /* merge empty nodes to oper DS to predict any problems with missing parent for Node */
        final WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.merge(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(Nodes.class), new NodesBuilder().build());
        tx.submit();

        emptyRequestContextStack = new RequestContextStack() {
            @Override
            public <T> void forgetRequestContext(final RequestContext<T> requestContext) {
                //NOOP
            }

            @Override
            public <T> SettableFuture<RpcResult<T>> storeOrFail(final RequestContext<T> data) {
                return data.getFuture();
            }

            @Override
            public <T> RequestContext<T> createRequestContext() {
                return new RequestContextImpl<>(this);
            }
        };
        spyPool = new ScheduledThreadPoolExecutor(1);
        spyPool.scheduleAtFixedRate(messageIntelligenceAgency, spyRate, spyRate, TimeUnit.SECONDS);
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceContext deviceContext) {
        // final phase - we have to add new Device to MD-SAL DataStore
        Preconditions.checkNotNull(deviceContext != null);
        ((DeviceContextImpl) deviceContext).submitTransaction();
        new BarrierTaskBuilder(deviceContext).buildAndFireBarrierTask();
    }

    @Override
    public void deviceConnected(@CheckForNull final ConnectionContext connectionContext) {
        Preconditions.checkArgument(connectionContext != null);

        final DeviceState deviceState = new DeviceStateImpl(connectionContext.getFeatures(), connectionContext.getNodeId());

        final DeviceContext deviceContext = new DeviceContextImpl(connectionContext, deviceState, dataBroker, hashedWheelTimer, messageIntelligenceAgency);

        deviceContext.setNotificationService(notificationService);
        deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, deviceState.getNodeInstanceIdentifier(), new NodeBuilder().setId(deviceState.getNodeId()).build());

        connectionContext.setDeviceDisconnectedHandler(deviceContext);
        deviceContext.setTranslatorLibrary(translatorLibrary);
        deviceContext.addDeviceContextClosedHandler(this);

        final OpenflowProtocolListenerFullImpl messageListener = new OpenflowProtocolListenerFullImpl(
                connectionContext.getConnectionAdapter(), deviceContext);

        deviceContext.attachOpenflowMessageListener(messageListener);

        ListenableFuture<List<RpcResult<List<MultipartReply>>>> deviceFeaturesFuture = null;

        if (connectionContext.getFeatures().getVersion() == OFConstants.OFP_VERSION_1_0) {
            final CapabilitiesV10 capabilitiesV10 = connectionContext.getFeatures().getCapabilitiesV10();

            DeviceStateUtil.setDeviceStateBasedOnV10Capabilities(deviceState, capabilitiesV10);
            //FIXME: next two lines are hack to make OF10 + cbench working (they don't send reply for description request)
            createEmptyFlowCapableNodeInDs(deviceContext);
            makeEmptyTables(deviceContext, deviceContext.getDeviceState().getNodeInstanceIdentifier(), connectionContext.getFeatures().getTables());

            deviceFeaturesFuture = Futures.immediateFuture(null);//createDeviceFeaturesForOF10(messageListener, deviceContext, deviceState);

            for (final PortGrouping port : connectionContext.getFeatures().getPhyPort()) {
                final short ofVersion = deviceContext.getDeviceState().getVersion();
                final TranslatorKey translatorKey = new TranslatorKey(ofVersion, PortGrouping.class.getName());
                final MessageTranslator<PortGrouping, FlowCapableNodeConnector> translator = deviceContext.oook().lookupTranslator(translatorKey);
                final FlowCapableNodeConnector fcNodeConnector = translator.translate(port, deviceContext, null);

                final BigInteger dataPathId = deviceContext.getPrimaryConnectionContext().getFeatures().getDatapathId();
                final NodeConnectorId nodeConnectorId = NodeStaticReplyTranslatorUtil.nodeConnectorId(dataPathId.toString(), port.getPortNo(), ofVersion);
                final NodeConnectorBuilder ncBuilder = new NodeConnectorBuilder().setId(nodeConnectorId);
                ncBuilder.addAugmentation(FlowCapableNodeConnector.class, fcNodeConnector);
                ncBuilder.addAugmentation(FlowCapableNodeConnectorStatisticsData.class, new FlowCapableNodeConnectorStatisticsDataBuilder().build());
                final NodeConnector connector = ncBuilder.build();
                final InstanceIdentifier<NodeConnector> connectorII = deviceState.getNodeInstanceIdentifier().child(NodeConnector.class, connector.getKey());
                deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, connectorII, connector);
                //FlowCapableNodeConnectorBuilder
            }
        } else if (connectionContext.getFeatures().getVersion() == OFConstants.OFP_VERSION_1_3) {
            final Capabilities capabilities = connectionContext.getFeatures().getCapabilities();
            DeviceStateUtil.setDeviceStateBasedOnV13Capabilities(deviceState, capabilities);
            deviceFeaturesFuture = createDeviceFeaturesForOF13(messageListener, deviceContext, deviceState);
        }

        Futures.addCallback(deviceFeaturesFuture, new FutureCallback<List<RpcResult<List<MultipartReply>>>>() {
            @Override
            public void onSuccess(final List<RpcResult<List<MultipartReply>>> result) {
                deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
            }

            @Override
            public void onFailure(final Throwable t) {
                // FIXME : remove session
                LOG.trace("Device capabilities gathering future failed.");
            }
        });
    }


    private ListenableFuture<RpcResult<List<MultipartReply>>> processReplyDesc(OpenflowProtocolListenerFullImpl messageListener,
                                                                               DeviceContext deviceContext,
                                                                               DeviceState deviceState) {
        final ListenableFuture<RpcResult<List<MultipartReply>>> replyDesc = getNodeStaticInfo(messageListener,
                MultipartType.OFPMPDESC, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());
        return replyDesc;
    }

    private ListenableFuture<List<RpcResult<List<MultipartReply>>>> createDeviceFeaturesForOF10(OpenflowProtocolListenerFullImpl messageListener,
                                                                                                DeviceContext deviceContext,
                                                                                                DeviceState deviceState) {
        return Futures.allAsList(Arrays.asList(processReplyDesc(messageListener, deviceContext, deviceState)));
    }

    private ListenableFuture<List<RpcResult<List<MultipartReply>>>> createDeviceFeaturesForOF13(OpenflowProtocolListenerFullImpl messageListener,
                                                                                                final DeviceContext deviceContext,
                                                                                                final DeviceState deviceState) {
        final ListenableFuture<RpcResult<List<MultipartReply>>> replyDesc = processReplyDesc(messageListener, deviceContext, deviceState);

        final ListenableFuture<RpcResult<List<MultipartReply>>> replyMeterFeature = getNodeStaticInfo(messageListener,
                MultipartType.OFPMPMETERFEATURES, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final ListenableFuture<RpcResult<List<MultipartReply>>> replyGroupFeatures = getNodeStaticInfo(messageListener,
                MultipartType.OFPMPGROUPFEATURES, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final ListenableFuture<RpcResult<List<MultipartReply>>> replyTableFeatures = getNodeStaticInfo(messageListener,
                MultipartType.OFPMPTABLEFEATURES, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final ListenableFuture<RpcResult<List<MultipartReply>>> replyPortDescription = getNodeStaticInfo(messageListener,
                MultipartType.OFPMPPORTDESC, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        return Futures.allAsList(Arrays.asList(replyDesc,
                replyMeterFeature,
                replyGroupFeatures,
//                        replyTableFeatures,
                replyPortDescription));

    }

    @Override
    public TranslatorLibrary oook() {
        return translatorLibrary;
    }

    @Override
    public void setTranslatorLibrary(final TranslatorLibrary translatorLibrary) {
        this.translatorLibrary = translatorLibrary;
    }

    private ListenableFuture<RpcResult<List<MultipartReply>>> getNodeStaticInfo(final MultiMsgCollector multiMsgCollector, final MultipartType type, final DeviceContext deviceContext,
                                                                                final InstanceIdentifier<Node> nodeII, final short version) {

        final Xid xid = deviceContext.getNextXid();
        final RequestContext<List<MultipartReply>> requestContext = emptyRequestContextStack.createRequestContext();
        requestContext.setXid(xid);

        LOG.trace("Hooking xid {} to device context - precaution.", requestContext.getXid().getValue());
        deviceContext.hookRequestCtx(requestContext.getXid(), requestContext);


        multiMsgCollector.registerMultipartXid(xid.getValue());
        Futures.addCallback(requestContext.getFuture(), new FutureCallback<RpcResult<List<MultipartReply>>>() {
            @Override
            public void onSuccess(final RpcResult<List<MultipartReply>> rpcResult) {
                final List<MultipartReply> result = rpcResult.getResult();
                if (result != null) {
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
            public void onFailure(final Throwable t) {
                LOG.info("Failed to retrieve static node {} info: {}", type, t.getMessage());
            }
        });

        final ListenableFuture<RpcResult<Void>> rpcFuture = JdkFutureAdapters.listenInPoolThread(deviceContext.getPrimaryConnectionContext().getConnectionAdapter()
                .multipartRequest(MultipartRequestInputFactory.makeMultipartRequestInput(xid.getValue(), version, type)));
        final OFJResult2RequestCtxFuture OFJResult2RequestCtxFuture = new OFJResult2RequestCtxFuture(requestContext, deviceContext);
        OFJResult2RequestCtxFuture.processResultFromOfJava(rpcFuture);

        return requestContext.getFuture();
    }

    // FIXME : remove after ovs tableFeatures fix
    private static void makeEmptyTables(final DeviceContext dContext, final InstanceIdentifier<Node> nodeII, final Short nrOfTables) {
        LOG.debug("About to create {} empty tables.", nrOfTables);
        for (int i = 0; i < nrOfTables; i++) {
            final short tId = (short) i;
            final InstanceIdentifier<Table> tableII = nodeII.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tId));
            final TableBuilder tableBuilder = new TableBuilder().setId(tId).addAugmentation(FlowTableStatisticsData.class, new FlowTableStatisticsDataBuilder().build());
            dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tableII, tableBuilder.build());
        }
    }

    private static void translateAndWriteReply(final MultipartType type, final DeviceContext dContext,
                                               final InstanceIdentifier<Node> nodeII, final Collection<MultipartReply> result) {
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
    }

    @Override
    public void setNotificationService(final NotificationProviderService notificationServiceParam) {
        notificationService = notificationServiceParam;
    }

    @Override
    public void close() throws Exception {
        for (DeviceContext deviceContext : synchronizedDeviceContextsList) {
            deviceContext.close();
        }
    }

    private static void createEmptyFlowCapableNodeInDs(final DeviceContext deviceContext) {
        FlowCapableNodeBuilder flowCapableNodeBuilder = new FlowCapableNodeBuilder();
        final InstanceIdentifier<FlowCapableNode> fNodeII = deviceContext.getDeviceState().getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);
        deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, fNodeII, flowCapableNodeBuilder.build());
    }

    @Override
    public void onDeviceContextClosed(final DeviceContext deviceContext) {
        synchronizedDeviceContextsList.remove(deviceContext);
    }
}
