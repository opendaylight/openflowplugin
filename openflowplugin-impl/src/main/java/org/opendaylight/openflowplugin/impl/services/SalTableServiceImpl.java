/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
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
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public final class SalTableServiceImpl extends AbstractMultipartService<UpdateTableInput> implements SalTableService {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalTableServiceImpl.class);
    private final TxFacade txFacade;
    private final NodeId nodeId;

    public SalTableServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
                               final NodeId nodeId) {
        super(requestContextStack, deviceContext);
        this.txFacade = deviceContext;
        this.nodeId = nodeId;
    }

    @Override
    public Future<RpcResult<UpdateTableOutput>> updateTable(final UpdateTableInput input) {
        final ListenableFuture<RpcResult<List<MultipartReply>>> multipartFuture = handleServiceCall(input);
        final SettableFuture<RpcResult<UpdateTableOutput>> finalFuture = SettableFuture.create();

        class CallBackImpl implements FutureCallback<RpcResult<List<MultipartReply>>> {
            @Override
            public void onSuccess(final RpcResult<List<MultipartReply>> result) {

                if (result.isSuccessful()) {
                    final List<MultipartReply> multipartReplies = result.getResult();
                    if (multipartReplies.isEmpty()) {
                        LOG.debug("Multipart reply to table features request shouldn't be empty list.");
                        finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed()
                                .withError(ErrorType.RPC, "Multipart reply list is empty.").build());
                    } else {
                        final Long xid = multipartReplies.get(0).getXid();
                        LOG.debug(
                                "OnSuccess, rpc result successful, multipart response for rpc update-table with xid {} obtained.",
                                xid);
                        final UpdateTableOutputBuilder updateTableOutputBuilder = new UpdateTableOutputBuilder();
                        updateTableOutputBuilder.setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        finalFuture.set(RpcResultBuilder.success(updateTableOutputBuilder.build()).build());
                        try {
                            writeResponseToOperationalDatastore(multipartReplies);
                        } catch (Exception e) {
                            LOG.warn("Not able to write to operational datastore: {}", e.getMessage());
                        }
                    }
                } else {
                    LOG.debug("OnSuccess, rpc result unsuccessful, multipart response for rpc update-table was unsuccessful.");
                    finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed().withRpcErrors(result.getErrors())
                            .build());
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.error("Failure multipart response for table features request. Exception: {}", t);
                finalFuture.set(RpcResultBuilder.<UpdateTableOutput>failed()
                        .withError(ErrorType.RPC, "Future error", t).build());
            }
        }

        Futures.addCallback(multipartFuture, new CallBackImpl());

        return finalFuture;
    }

    /**
     * @param multipartReplies
     */
    private void writeResponseToOperationalDatastore(final List<MultipartReply> multipartReplies) throws Exception {

        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures> salTableFeatures = convertToSalTableFeatures(multipartReplies);

        final InstanceIdentifier<FlowCapableNode> flowCapableNodeII = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(getDeviceInfo().getNodeId())).augmentation(FlowCapableNode.class);
        for (final org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures tableFeatureData : salTableFeatures) {
            final Short tableId = tableFeatureData.getTableId();
            final KeyedInstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures, TableFeaturesKey> tableFeaturesII = flowCapableNodeII
                    .child(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures.class,
                            new TableFeaturesKey(tableId));
            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tableFeaturesII,
                    tableFeatureData);
        }

        txFacade.submitTransaction();
    }

    protected static List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures> convertToSalTableFeatures(
            final List<MultipartReply> multipartReplies) {
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures> salTableFeaturesAll = new ArrayList<>();
        for (final MultipartReply multipartReply : multipartReplies) {
            if (multipartReply.getType().equals(MultipartType.OFPMPTABLEFEATURES)) {
                final MultipartReplyBody multipartReplyBody = multipartReply.getMultipartReplyBody();
                if (multipartReplyBody instanceof MultipartReplyTableFeaturesCase) {
                    final MultipartReplyTableFeaturesCase tableFeaturesCase = ((MultipartReplyTableFeaturesCase) multipartReplyBody);
                    final MultipartReplyTableFeatures salTableFeatures = tableFeaturesCase
                            .getMultipartReplyTableFeatures();

                    final Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures>> salTableFeaturesPartial =
                            ConvertorManager.getInstance().convert(salTableFeatures);

                    if (salTableFeaturesPartial.isPresent()) {
                        salTableFeaturesAll.addAll(salTableFeaturesPartial.get());
                    }

                    LOG.debug("TableFeature {} for xid {}.", salTableFeatures, multipartReply.getXid());
                }
            }
        }
        return salTableFeaturesAll;
    }

    private MultipartRequestInputBuilder createMultipartHeader(final MultipartType multipart, final Long xid) {
        final MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(multipart);
        mprInput.setVersion(getVersion());
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));
        return mprInput;
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final UpdateTableInput input) {
        final MultipartRequestTableFeaturesCaseBuilder caseBuilder = new MultipartRequestTableFeaturesCaseBuilder();
        final MultipartRequestTableFeaturesBuilder requestBuilder = new MultipartRequestTableFeaturesBuilder();

        final Optional<List<TableFeatures>> ofTableFeatureList = ConvertorManager.getInstance().convert(input.getUpdatedTable());
        requestBuilder.setTableFeatures(ofTableFeatureList.orElse(Collections.emptyList()));
        caseBuilder.setMultipartRequestTableFeatures(requestBuilder.build());

        // Set request body to main multipart request
        final MultipartRequestInputBuilder mprInput = createMultipartHeader(MultipartType.OFPMPTABLEFEATURES,
            xid.getValue());
        mprInput.setMultipartRequestBody(caseBuilder.build());

        return mprInput.build();
    }
}
