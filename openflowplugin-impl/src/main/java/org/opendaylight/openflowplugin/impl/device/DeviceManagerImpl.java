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
import io.netty.util.HashedWheelTimer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextReadyHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.openflowplugin.impl.common.NodeStaticReplyTranslatorUtil;
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


    public DeviceManagerImpl (@Nonnull final RpcManager rpcManager, @Nonnull final DataBroker dataBroker) {
        this.rpcManager = Preconditions.checkNotNull(rpcManager);
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, 10);
    }

    @Override
    public void deviceConnected(@CheckForNull final ConnectionContext connectionContext) {
        Preconditions.checkArgument(connectionContext != null);
        final DeviceState deviceState = new DeviceStateImpl(connectionContext.getFeatures(), connectionContext.getNodeId());
        final DeviceContext deviceContext = new DeviceContextImpl(connectionContext, deviceState, dataBroker, hashedWheelTimer);

        final Xid nodeDescXid = deviceContext.getNextXid();
        final ListenableFuture<Collection<MultipartReply>> replyDesc = getNodeStaticInfo(nodeDescXid, connectionContext,
                MultipartType.OFPMPDESC, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final Xid nodeMeterXid = deviceContext.getNextXid();
        final ListenableFuture<Collection<MultipartReply>> replyMeterFeature = getNodeStaticInfo(nodeMeterXid, connectionContext,
                MultipartType.OFPMPMETERFEATURES, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final Xid nodeGroupXid = deviceContext.getNextXid();
        final ListenableFuture<Collection<MultipartReply>> replyGroupFeatures = getNodeStaticInfo(nodeGroupXid, connectionContext,
                MultipartType.OFPMPGROUPFEATURES, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final Xid nodeTableXid = deviceContext.getNextXid();
        final ListenableFuture<Collection<MultipartReply>> replyTableFeatures = getNodeStaticInfo(nodeTableXid, connectionContext,
                MultipartType.OFPMPTABLEFEATURES, deviceContext, deviceState.getNodeInstanceIdentifier(), deviceState.getVersion());

        final ListenableFuture<List<Collection<MultipartReply>>> deviceFeaturesFuture =
                Futures.allAsList(Arrays.asList(replyDesc, replyMeterFeature, replyGroupFeatures, replyTableFeatures));
        Futures.addCallback(deviceFeaturesFuture, new FutureCallback<List<Collection<MultipartReply>>>() {
            @Override
            public void onSuccess(final List<Collection<MultipartReply>> result) {
                // FIXME : add statistics
                rpcManager.deviceConnected(deviceContext);
                ((DeviceContextImpl) deviceContext).submitTransaction();
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

    private static ListenableFuture<Collection<MultipartReply>> getNodeStaticInfo(final Xid xid, final ConnectionContext cContext,
            final MultipartType type, final DeviceContext dContext, final InstanceIdentifier<Node> nodeII, final short version) {
        final ListenableFuture<Collection<MultipartReply>> future = cContext.registerMultipartMsg(xid.getValue());
        Futures.addCallback(future, new FutureCallback<Collection<MultipartReply>>() {
            @Override
            public void onSuccess(final Collection<MultipartReply> result) {
                Preconditions.checkArgument(result != null);
                translateAndWriteReply(type, dContext, nodeII, result);
            }
            @Override
            public void onFailure(final Throwable t) {
                // TODO : ovs TableFeatures are broken for yet so we have to add workaround
                if (MultipartType.OFPMPTABLE.equals(type)) {
                    makeEmptyTables(dContext, nodeII, cContext.getFeatures().getTables());
                }
                LOG.info("Failed to retrieve static node {} info: {}", type, t.getMessage());
            }
        });
        final Future<RpcResult<Void>> rpcFuture = cContext.getConnectionAdapter()
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
