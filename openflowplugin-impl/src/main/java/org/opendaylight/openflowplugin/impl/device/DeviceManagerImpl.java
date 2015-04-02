/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.*;
import io.netty.util.HashedWheelTimer;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.*;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextReadyHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.openflowplugin.impl.common.NodeStaticReplyTranslatorUtil;
import org.opendaylight.openflowplugin.impl.device.listener.OpenflowProtocolListenerFullImpl;
import org.opendaylight.openflowplugin.impl.rpc.RequestContextImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
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

    private final RpcManager rpcManager;
    private final DataBroker dataBroker;
    private final HashedWheelTimer hashedWheelTimer;
    private RequestContextStack dummyRequestContextStack;


    public DeviceManagerImpl (@Nonnull final RpcManager rpcManager, @Nonnull final DataBroker dataBroker) {
        this.rpcManager = Preconditions.checkNotNull(rpcManager);
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, 10);

        dummyRequestContextStack = new RequestContextStack() {
            @Override
            public <T> void forgetRequestContext(RequestContext<T> requestContext) {
                //NOOP
            }
            @Override
            public <T> SettableFuture<RpcResult<T>> storeOrFail(RequestContext<T> data) {
                return data.getFuture();
            }
            @Override
            public <T> RequestContext<T> createRequestContext() {
                return new RequestContextImpl<>(this);
            }
        };
    }

    @Override
    public void deviceConnected(@CheckForNull final ConnectionContext connectionContext) {
        Preconditions.checkArgument(connectionContext != null);
        final DeviceState deviceState = new DeviceStateImpl(connectionContext.getFeatures(), connectionContext.getNodeId());
        final DeviceContextImpl deviceContext = new DeviceContextImpl(connectionContext, deviceState, dataBroker, hashedWheelTimer);
        final OpenflowProtocolListenerFullImpl messageListener = new OpenflowProtocolListenerFullImpl(
                connectionContext.getConnectionAdapter(), deviceContext);
        connectionContext.getConnectionAdapter().setMessageListener(messageListener);

        final Xid nodeDescXid = deviceContext.getNextXid();
        final ListenableFuture<Collection<MultipartReply>> replyDesc = getNodeStaticInfo(nodeDescXid, messageListener,
                MultipartType.OFPMPDESC, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final Xid nodeMeterXid = deviceContext.getNextXid();
        final ListenableFuture<Collection<MultipartReply>> replyMeterFeature = getNodeStaticInfo(nodeMeterXid, messageListener,
                MultipartType.OFPMPMETERFEATURES, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final Xid nodeGroupXid = deviceContext.getNextXid();
        final ListenableFuture<Collection<MultipartReply>> replyGroupFeatures = getNodeStaticInfo(nodeGroupXid, messageListener,
                MultipartType.OFPMPGROUPFEATURES, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final Xid nodeTableXid = deviceContext.getNextXid();
        final ListenableFuture<Collection<MultipartReply>> replyTableFeatures = getNodeStaticInfo(nodeTableXid, messageListener,
                MultipartType.OFPMPTABLEFEATURES, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final ListenableFuture<List<Collection<MultipartReply>>> deviceFeaturesFuture =
                Futures.allAsList(Arrays.asList(replyDesc, replyMeterFeature, replyGroupFeatures, replyTableFeatures));
        Futures.addCallback(deviceFeaturesFuture, new FutureCallback<List<Collection<MultipartReply>>>() {
            @Override
            public void onSuccess(final List<Collection<MultipartReply>> result) {
                // FIXME : add statistics
                rpcManager.deviceConnected(deviceContext);
                deviceContext.submitTransaction();
            }

            @Override
            public void onFailure(final Throwable t) {
                // FIXME : remove session
            }
        });
    }

    @Override
    public void addRequestContextReadyHandler(final DeviceContextReadyHandler deviceContextReadyHandler) {
        // TODO Auto-generated method stub
    }

    private ListenableFuture<Collection<MultipartReply>> getNodeStaticInfo(final Xid xid,
            final MultiMsgCollector multiMsgCollector, final MultipartType type, final DeviceContext dContext,
            final InstanceIdentifier<Node> nodeII, final short version) {
        final ListenableFuture<Collection<MultipartReply>> future = multiMsgCollector.registerMultipartMsg(xid.getValue());

        RequestContext<List<MultipartReply>> requestContext = dummyRequestContextStack.createRequestContext();
        dContext.hookRequestCtx(xid, requestContext);
        Futures.addCallback(requestContext.getFuture(), new FutureCallback<RpcResult<List<MultipartReply>>>() {
            @Override
            public void onSuccess(final RpcResult<List<MultipartReply>> rpcResult) {
                List<MultipartReply> result = rpcResult.getResult();
                if (result != null) {
                    translateAndWriteReply(type, dContext, nodeII, result);
                } else {
                    Iterator<RpcError> rpcErrorIterator = rpcResult.getErrors().iterator();
                    while (rpcErrorIterator.hasNext()) {
                        Throwable t = rpcErrorIterator.next().getCause();
                        LOG.info("Failed to retrieve static node {} info: {}", type, t.getMessage());
                        LOG.trace("Detailed error:", t);
                    }
                    if (MultipartType.OFPMPTABLE.equals(type)) {
                        makeEmptyTables(dContext, nodeII, dContext.getPrimaryConnectionContext().getFeatures().getTables());
                    }
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.info("Failed to retrieve static node {} info: {}", type, t.getMessage());
            }
        });

        final Future<RpcResult<Void>> rpcFuture = dContext.getPrimaryConnectionContext().getConnectionAdapter()
                .multipartRequest(MultipartRequestInputFactory.makeMultipartRequestInput(xid.getValue(), version, type));
        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(rpcFuture), new FutureCallback<RpcResult<Void>>() {
            @Override
            public void onSuccess(final RpcResult<Void> result) {
                // NOOP
            }
            @Override
            public void onFailure(final Throwable t) {
                future.cancel(true);
            }
        });
        return future;
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
