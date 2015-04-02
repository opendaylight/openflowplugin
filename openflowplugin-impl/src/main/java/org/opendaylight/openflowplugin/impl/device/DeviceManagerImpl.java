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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextReadyHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.openflowplugin.impl.common.NodeStaticReplyTranslatorUtil;
import org.opendaylight.openflowplugin.impl.rpc.RequestContextImpl;
import org.opendaylight.openflowplugin.impl.rpc.RpcContextImpl;
import org.opendaylight.openflowplugin.impl.rpc.RpcManagerImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DeviceManagerImpl implements DeviceManager {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);

    private final BindingAwareBroker.ProviderContext providerContext;


    public DeviceManagerImpl (@Nonnull final ProviderContext providerContext) {
        this.providerContext = Preconditions.checkNotNull(providerContext);
    }

    @Override
    public void deviceConnected(@CheckForNull final ConnectionContext connectionContext) {
        Preconditions.checkArgument(connectionContext != null);
        final DataBroker dataBroker = providerContext.getSALService(DataBroker.class);
        final DeviceState deviceState = new DeviceStateImpl(connectionContext.getFeatures(), connectionContext.getNodeId());
        final DeviceContext deviceContext = new DeviceContextImpl(connectionContext, deviceState, dataBroker);

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
                final RpcManager rpcManager = new RpcManagerImpl(providerContext);
                final RequestContextStack rcs = new RpcContextImpl(providerContext, deviceContext);
                final RequestContext<?> requestContext = new RequestContextImpl<>(rcs);
                rpcManager.deviceConnected(deviceContext, requestContext);
                ((DeviceContextImpl) deviceContext).submitTransaction();
            }

            @Override
            public void onFailure(final Throwable t) {
                // FIXME : remove session
            }
        });
    }

    @Override
    public void sendMessage(final DataObject dataObject, final RequestContext requestContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public Xid sendRequest(final DataObject dataObject, final RequestContext requestContext) {
        // TODO Auto-generated method stub
        return null;
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

    private static void translateAndWriteReply(final MultipartType type, final DeviceContext dContext,
            final InstanceIdentifier<Node> nodeII, final Collection<MultipartReply> result) {
        for (final MultipartReply reply : result) {
            switch (type) {
            case OFPMPDESC:
                Preconditions.checkArgument(reply instanceof MultipartReplyDesc);
                final FlowCapableNode fcNode = NodeStaticReplyTranslatorUtil.nodeDescTranslator((MultipartReplyDesc) reply);
                final InstanceIdentifier<FlowCapableNode> fNodeII = nodeII.augmentation(FlowCapableNode.class);
                dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, fNodeII, fcNode);
                break;

            case OFPMPTABLEFEATURES:
                Preconditions.checkArgument(reply instanceof MultipartReplyTableFeatures);
                final List<TableFeatures> tables = NodeStaticReplyTranslatorUtil.nodeTableFeatureTranslator((MultipartReplyTableFeatures) reply);
                for (final TableFeatures table : tables) {
                    final Short tableId = table.getTableId();
                    final InstanceIdentifier<Table> tableII = nodeII.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(tableId));
                    dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tableII, new TableBuilder().setId(tableId).setTableFeatures(Collections.singletonList(table)).build());
                }
                break;

            case OFPMPMETERFEATURES:
                Preconditions.checkArgument(reply instanceof MultipartReplyMeterFeatures);
                final NodeMeterFeatures mFeature = NodeStaticReplyTranslatorUtil.nodeMeterFeatureTranslator((MultipartReplyMeterFeatures) reply);
                final InstanceIdentifier<NodeMeterFeatures> mFeatureII = nodeII.augmentation(NodeMeterFeatures.class);
                dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, mFeatureII, mFeature);
                break;

            case OFPMPGROUPFEATURES:
                Preconditions.checkArgument(reply instanceof MultipartReplyGroupFeatures);
                final NodeGroupFeatures gFeature = NodeStaticReplyTranslatorUtil.nodeGroupFeatureTranslator((MultipartReplyGroupFeatures) reply);
                final InstanceIdentifier<NodeGroupFeatures> gFeatureII = nodeII.augmentation(NodeGroupFeatures.class);
                dContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, gFeatureII, gFeature);
                break;

            default:
                throw new IllegalArgumentException("Unnexpected MultipartType " + type);
            }
        }
    }

}
