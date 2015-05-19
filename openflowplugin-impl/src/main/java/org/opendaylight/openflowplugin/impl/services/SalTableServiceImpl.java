/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.TableFeaturesConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.TableFeaturesReplyConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class SalTableServiceImpl extends CommonService implements SalTableService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalTableServiceImpl.class);

    public SalTableServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<UpdateTableOutput>> updateTable(final UpdateTableInput input) {
        class FunctionImpl implements
                Function<RequestContext<List<MultipartReply>>, ListenableFuture<RpcResult<List<MultipartReply>>>> {

            @Override
            public ListenableFuture<RpcResult<List<MultipartReply>>> apply(final RequestContext<List<MultipartReply>> requestContext) {
                getMessageSpy().spyMessage(input.getImplementedInterface(),
                        MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);

                final SettableFuture<RpcResult<List<MultipartReply>>> result = SettableFuture.create();

                final MultipartRequestTableFeaturesCaseBuilder caseBuilder = new MultipartRequestTableFeaturesCaseBuilder();
                final MultipartRequestTableFeaturesBuilder requestBuilder = new MultipartRequestTableFeaturesBuilder();
                final List<TableFeatures> ofTableFeatureList = TableFeaturesConvertor.toTableFeaturesRequest(input
                        .getUpdatedTable());
                requestBuilder.setTableFeatures(ofTableFeatureList);
                caseBuilder.setMultipartRequestTableFeatures(requestBuilder.build());

                // Set request body to main multipart request
                final Xid xid = requestContext.getXid();
                final MultipartRequestInputBuilder mprInput = createMultipartHeader(MultipartType.OFPMPTABLEFEATURES,
                        xid.getValue());
                mprInput.setMultipartRequestBody(caseBuilder.build());
                final OutboundQueue outboundQueue = getDeviceContext().getPrimaryConnectionContext().getOutboundQueueProvider();

                final SettableFuture<RpcResult<List<MultipartReply>>> settableFuture = SettableFuture.create();
                final MultiMsgCollector multiMsgCollector = getDeviceContext().getMultiMsgCollector();
                multiMsgCollector.registerMultipartRequestContext(requestContext);

                final MultipartRequestInput multipartRequestInput = mprInput.build();
                outboundQueue.commitEntry(xid.getValue(), multipartRequestInput, new FutureCallback<OfHeader>() {
                    @Override
                    public void onSuccess(final OfHeader ofHeader) {
                        if (ofHeader instanceof MultipartReply) {
                            MultipartReply multipartReply = (MultipartReply) ofHeader;
                            multiMsgCollector.addMultipartMsg(multipartReply);
                        } else {
                            if (null != ofHeader) {
                                LOG.info("Unexpected response type received {}.", ofHeader.getClass());
                            } else {
                                LOG.info("Response received is null.");
                            }
                        }
                        getMessageSpy().spyMessage(multipartRequestInput.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);
                        settableFuture.set(RpcResultBuilder.<List<MultipartReply>>success().build());
                    }

                    @Override
                    public void onFailure(final Throwable throwable) {
                        RpcResultBuilder<List<MultipartReply>> rpcResultBuilder = RpcResultBuilder.<List<MultipartReply>>failed().withError(RpcError.ErrorType.APPLICATION, throwable.getMessage(), throwable);
                        RequestContextUtil.closeRequstContext(requestContext);
                        multiMsgCollector.registerMultipartRequestContext(requestContext);
                        getMessageSpy().spyMessage(multipartRequestInput.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
                        settableFuture.set(rpcResultBuilder.build());
                    }
                });
                return settableFuture;
            }
        }

        final ListenableFuture<RpcResult<List<MultipartReply>>> multipartFuture = handleServiceCallNew(new FunctionImpl());
        final SettableFuture<RpcResult<UpdateTableOutput>> finalFuture = SettableFuture.create();

        class CallBackImpl implements FutureCallback<RpcResult<List<MultipartReply>>> {
            private final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CallBackImpl.class);

            @Override
            public void onSuccess(final RpcResult<List<MultipartReply>> result) {

                if (result.isSuccessful()) {
                    final List<MultipartReply> multipartReplies = result.getResult();
                    if (multipartReplies.isEmpty()) {
                        LOGGER.debug("Multipart reply to table features request shouldn't be empty list.");
                        finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed()
                                .withError(ErrorType.RPC, "Multipart reply list is empty.").build());
                    } else {
                        final Long xid = multipartReplies.get(0).getXid();
                        LOGGER.debug(
                                "OnSuccess, rpc result successful, multipart response for rpc update-table with xid {} obtained.",
                                xid);
                        final UpdateTableOutputBuilder updateTableOutputBuilder = new UpdateTableOutputBuilder();
                        updateTableOutputBuilder.setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        finalFuture.set(RpcResultBuilder.success(updateTableOutputBuilder.build()).build());
                        writeResponseToOperationalDatastore(multipartReplies);
                    }
                } else {
                    LOGGER.debug("OnSuccess, rpc result unsuccessful, multipart response for rpc update-table was unsuccessful.");
                    finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed().withRpcErrors(result.getErrors())
                            .build());
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOGGER.debug("Failure multipart response for table features request. Exception: {}", t);
                finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed()
                        .withError(ErrorType.RPC, "Future error", t).build());
            }

            /**
             * @param multipartReplies
             */
            private void writeResponseToOperationalDatastore(final List<MultipartReply> multipartReplies) {

                final List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures> salTableFeatures = convertToSalTableFeatures(multipartReplies);

                final DeviceContext deviceContext = getDeviceContext();
                final NodeId nodeId = deviceContext.getPrimaryConnectionContext().getNodeId();
                final InstanceIdentifier<FlowCapableNode> flowCapableNodeII = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class);
                for (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures tableFeatureData : salTableFeatures) {
                    final Short tableId = tableFeatureData.getTableId();
                    KeyedInstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures, TableFeaturesKey> tableFeaturesII = flowCapableNodeII
                            .child(Table.class, new TableKey(tableId))
                            .child(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures.class,
                                    new TableFeaturesKey(tableId));
                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tableFeaturesII,
                            tableFeatureData);
                }

            }

            private List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures> convertToSalTableFeatures(
                    final List<MultipartReply> multipartReplies) {
                final List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures> salTableFeaturesAll = new ArrayList<>();
                for (MultipartReply multipartReply : multipartReplies) {
                    if (multipartReply.getType().equals(MultipartType.OFPMPTABLEFEATURES)) {
                        MultipartReplyBody multipartReplyBody = multipartReply.getMultipartReplyBody();
                        if (multipartReplyBody instanceof MultipartReplyTableFeaturesCase) {
                            MultipartReplyTableFeaturesCase tableFeaturesCase = ((MultipartReplyTableFeaturesCase) multipartReplyBody);
                            MultipartReplyTableFeatures salTableFeatures = tableFeaturesCase
                                    .getMultipartReplyTableFeatures();
                            List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures> salTableFeaturesPartial = TableFeaturesReplyConvertor
                                    .toTableFeaturesReply(salTableFeatures);
                            salTableFeaturesAll.addAll(salTableFeaturesPartial);
                            LOGGER.debug("TableFeature {} for xid {}.", salTableFeatures, multipartReply.getXid());
                        }
                    }
                }
                return salTableFeaturesAll;
            }

        }

        Futures.addCallback(multipartFuture, new CallBackImpl());

        return finalFuture;
    }

    private MultipartRequestInputBuilder createMultipartHeader(final MultipartType multipart, final Long xid) {
        final MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(multipart);
        mprInput.setVersion(getVersion());
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));
        return mprInput;
    }

}
