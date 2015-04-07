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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.openflowplugin.impl.common.NodeStaticReplyTranslatorUtil;
import org.opendaylight.openflowplugin.impl.device.listener.OpenflowProtocolListenerFullImpl;
import org.opendaylight.openflowplugin.impl.rpc.RequestContextImpl;
import org.opendaylight.openflowplugin.impl.services.OFJResult2RequestCtxFuture;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DeviceManagerImpl implements DeviceManager {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);

    private static final long TICK_DURATION = 500; // 0.5 sec.

    private final DataBroker dataBroker;
    private final HashedWheelTimer hashedWheelTimer;
    private RequestContextStack dummyRequestContextStack;
    private TranslatorLibrary translatorLibrary;
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;


    public DeviceManagerImpl(@Nonnull final DataBroker dataBroker) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, 10);
        /* merge empty nodes to oper DS to predict any problems with missing parent for Node */
        dataBroker.newReadWriteTransaction().merge(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(Nodes.class), new NodesBuilder().build());

        dummyRequestContextStack = new RequestContextStack() {
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
    }

    @Override
    public void deviceConnected(@CheckForNull final ConnectionContext connectionContext) {
        Preconditions.checkArgument(connectionContext != null);

        final DeviceState deviceState = new DeviceStateImpl(connectionContext.getFeatures(), connectionContext.getNodeId());

        final DeviceContextImpl deviceContext = new DeviceContextImpl(connectionContext, deviceState, dataBroker, hashedWheelTimer);

        deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, deviceState.getNodeInstanceIdentifier(), new NodeBuilder().setId(deviceState.getNodeId()).build());

        deviceContext.setTranslatorLibrary(translatorLibrary);

        final OpenflowProtocolListenerFullImpl messageListener = new OpenflowProtocolListenerFullImpl(
                connectionContext.getConnectionAdapter(), deviceContext);

        deviceContext.attachOpenflowMessageListener(messageListener);

        final ListenableFuture<RpcResult<List<MultipartReply>>> replyDesc = getNodeStaticInfo(messageListener,
                MultipartType.OFPMPDESC, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final ListenableFuture<RpcResult<List<MultipartReply>>> replyMeterFeature = getNodeStaticInfo(messageListener,
                MultipartType.OFPMPMETERFEATURES, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final ListenableFuture<RpcResult<List<MultipartReply>>> replyGroupFeatures = getNodeStaticInfo(messageListener,
                MultipartType.OFPMPGROUPFEATURES, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final ListenableFuture<RpcResult<List<MultipartReply>>> replyTableFeatures = getNodeStaticInfo(messageListener,
                MultipartType.OFPMPTABLEFEATURES, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final ListenableFuture<List<RpcResult<List<MultipartReply>>>> deviceFeaturesFuture =
                Futures.allAsList(Arrays.asList(replyDesc, replyMeterFeature, replyGroupFeatures, replyTableFeatures));

        Futures.addCallback(deviceFeaturesFuture, new FutureCallback<List<RpcResult<List<MultipartReply>>>>() {
            @Override
            public void onSuccess(final List<RpcResult<List<MultipartReply>>> result) {
                // wake up statistics
                deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
            }

            @Override
            public void onFailure(final Throwable t) {
                // FIXME : remove session
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

    private ListenableFuture<RpcResult<List<MultipartReply>>> getNodeStaticInfo(final MultiMsgCollector multiMsgCollector, final MultipartType type, final DeviceContext deviceContext,
                                                                                final InstanceIdentifier<Node> nodeII, final short version) {

        final Xid xid = deviceContext.getNextXid();
        final RequestContext<List<MultipartReply>> requestContext = dummyRequestContextStack.createRequestContext();
        requestContext.setXid(xid);
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
                    if (MultipartType.OFPMPTABLE.equals(type)) {
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
        for (int i = 0; i < nrOfTables; i++) {
            final short tId = (short) i;
            final InstanceIdentifier<Table> tableII = nodeII.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tId));
            dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tableII, new TableBuilder().setId(tId).build());
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
                        dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tableII, new TableBuilder().setId(tableId).setTableFeatures(Collections.singletonList(table)).build());
                    }
                    break;

                case OFPMPMETERFEATURES:
                    Preconditions.checkArgument(body instanceof MultipartReplyMeterFeaturesCase);
                    final MultipartReplyMeterFeatures meterFeatures = ((MultipartReplyMeterFeaturesCase) body).getMultipartReplyMeterFeatures();
                    final NodeMeterFeatures mFeature = NodeStaticReplyTranslatorUtil.nodeMeterFeatureTranslator(meterFeatures);
                    final InstanceIdentifier<NodeMeterFeatures> mFeatureII = nodeII.augmentation(NodeMeterFeatures.class);
                    dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, mFeatureII, mFeature);
                    break;

                case OFPMPGROUPFEATURES:
                    Preconditions.checkArgument(body instanceof MultipartReplyGroupFeaturesCase);
                    final MultipartReplyGroupFeatures groupFeatures = ((MultipartReplyGroupFeaturesCase) body).getMultipartReplyGroupFeatures();
                    final NodeGroupFeatures gFeature = NodeStaticReplyTranslatorUtil.nodeGroupFeatureTranslator(groupFeatures);
                    final InstanceIdentifier<NodeGroupFeatures> gFeatureII = nodeII.augmentation(NodeGroupFeatures.class);
                    dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, gFeatureII, gFeature);
                    break;

                default:
                    throw new IllegalArgumentException("Unnexpected MultipartType " + type);
            }
        }
    }
}
