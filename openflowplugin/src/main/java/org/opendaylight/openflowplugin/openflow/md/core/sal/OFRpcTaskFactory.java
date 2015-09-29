/**
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.sal.NotificationComposer;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.GroupConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.MeterConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.PortConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.TableFeaturesConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowHashIdMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.nodes.node.table.FlowHashIdMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.nodes.node.table.FlowHashIdMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.SwitchConfigFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.config._case.MultipartRequestMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table._case.MultipartRequestTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class OFRpcTaskFactory {
    protected static final Logger LOG = LoggerFactory.getLogger(OFRpcTaskFactory.class);

    private OFRpcTaskFactory() {
        // hiding implicit constructor
    }

    /**
     * @param taskContext task context
     * @param input flow object input
     * @param cookie switch connection distinguisher cookie value
     * @return UpdateFlow task
     */
    public static OFRpcTask<AddFlowInput, RpcResult<UpdateFlowOutput>> createAddFlowTask(
            OFRpcTaskContext taskContext, AddFlowInput input,
            SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<AddFlowInput, RpcResult<UpdateFlowOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie, AddFlowInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<UpdateFlowOutput>> call() {
                ListenableFuture<RpcResult<UpdateFlowOutput>> result = SettableFuture.create();

                // Convert the AddFlowInput to FlowModInput
                List<FlowModInputBuilder> ofFlowModInputs = FlowConvertor.toFlowModInputs(getInput(),
                        getVersion(), getSession().getFeatures().getDatapathId());
                LOG.debug("Number of flows to push to switch: {}", ofFlowModInputs.size());
                result = chainFlowMods(ofFlowModInputs, 0, getTaskContext(), getCookie());
                result = OFRpcTaskUtil.chainFutureBarrier(this, result);
                OFRpcTaskUtil.hookFutureNotification(this, result,
                        getRpcNotificationProviderService(),
                        createFlowAddedNotification(getInput()));
                return result;
            }

            @Override
            public Boolean isBarrier() {
                return getInput().isBarrier();
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * Recursive helper method for {@link OFRpcTaskFactory#createAddFlowTask(OFRpcTaskContext, AddFlowInput, SwitchConnectionDistinguisher)}
     * and {@link OFRpcTaskFactory#createUpdateFlowTask(OFRpcTaskContext, UpdateFlowInput, SwitchConnectionDistinguisher, ReadWriteTransaction)} to chain results
     * of multiple flowmods.
     * The next flowmod gets executed if the earlier one is successful.
     * All the flowmods should have the same xid, in-order to cross-reference
     * the notification
     * @param taskContext task context
     * @param ofFlowModInputs list of flow mod as input
     * @param index  starting index
     * @param cookie switch connection distinguisher
     * @return listenable future with update flow output
     *
     */
    protected static ListenableFuture<RpcResult<UpdateFlowOutput>> chainFlowMods(
            final List<FlowModInputBuilder> ofFlowModInputs, final int index,
            final OFRpcTaskContext taskContext, final SwitchConnectionDistinguisher cookie) {

        Future<RpcResult<UpdateFlowOutput>> resultFromOFLib =
                createResultForFlowMod(taskContext, ofFlowModInputs.get(index), cookie);

        ListenableFuture<RpcResult<UpdateFlowOutput>> result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

        if (ofFlowModInputs.size() > index + 1) {
            // there are more flowmods to chain
            return Futures.transform(result,
                    new AsyncFunction<RpcResult<UpdateFlowOutput>, RpcResult<UpdateFlowOutput>>() {
                        @Override
                        public ListenableFuture<RpcResult<UpdateFlowOutput>> apply(RpcResult<UpdateFlowOutput> input) throws Exception {
                            if (input.isSuccessful()) {
                                return chainFlowMods(ofFlowModInputs, index + 1, taskContext, cookie);
                            } else {
                                LOG.warn("Flowmod failed. Any chained flowmods are ignored. xid:{}",
                                        ofFlowModInputs.get(index).getXid());
                                return Futures.immediateFuture(input);
                            }
                        }
                    }
            );
        } else {
            return result;
        }
    }

    private static Future<RpcResult<UpdateFlowOutput>> createResultForFlowMod(
            OFRpcTaskContext taskContext, FlowModInputBuilder flowModInput,
            SwitchConnectionDistinguisher cookie) {
        flowModInput.setXid(taskContext.getSession().getNextXid());
        return taskContext.getMessageService().flowMod(flowModInput.build(), cookie);
    }


    /**
     * @param input flow input
     * @return flow added notification
     */
    protected static NotificationComposer<FlowAdded> createFlowAddedNotification(
            final AddFlowInput input) {
        return new NotificationComposer<FlowAdded>() {
            @Override
            public FlowAdded compose(TransactionId tXid) {
                FlowAddedBuilder newFlow = new FlowAddedBuilder((Flow) input);
                newFlow.setTransactionId(tXid);
                newFlow.setFlowRef(input.getFlowRef());
                return newFlow.build();
            }
        };
    }

    /**
     * @param taskContext task context
     * @param input update flow input
     * @param cookie switch connection distinguisher cookie value
     * @param rwTx  read write transaction
     * @return UpdateFlow task
     */
    public static OFRpcTask<UpdateFlowInput, RpcResult<UpdateFlowOutput>> createUpdateFlowTask(
            final OFRpcTaskContext taskContext, UpdateFlowInput input,
            SwitchConnectionDistinguisher cookie, final ReadWriteTransaction rwTx) {

        class OFRpcTaskImpl extends OFRpcTask<UpdateFlowInput, RpcResult<UpdateFlowOutput>> {
            final ReadWriteTransaction rwTx;
            InstanceIdentifier<Table> iiToTable = null;
            String flowId = null;
            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    final UpdateFlowInput in, final ReadWriteTransaction rwTx) {
                super(taskContext, cookie, in);
                final FlowRef flowRef = in.getFlowRef();
                if (flowRef != null) {
                    InstanceIdentifier<Flow> iiToFlow = (InstanceIdentifier<Flow>)(flowRef.getValue());
                    iiToTable = flowRef.getValue().firstIdentifierOf(Table.class);
                    FlowKey flowKey = iiToFlow.firstKeyOf(
                            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow.class, FlowKey.class);
                    if (flowKey != null) {
                        flowId = flowKey.getId().getValue();
                    }
                }
                this.rwTx = rwTx;
            }

            @Override
            public ListenableFuture<RpcResult<UpdateFlowOutput>> call() {
                ListenableFuture<RpcResult<UpdateFlowOutput>> result = null;

                UpdateFlowInput in = getInput();
                UpdatedFlow updated = in.getUpdatedFlow();
                OriginalFlow original = in.getOriginalFlow();
                Short version = getVersion();

                List<FlowModInputBuilder> allFlowMods = new ArrayList<>();
                List<FlowModInputBuilder> ofFlowModInputs;

                if (!FlowCreatorUtil.canModifyFlow(original, updated, version)) {
                    // We would need to remove original and add updated.

                    //remove flow
                    RemoveFlowInputBuilder removeflow = new RemoveFlowInputBuilder(original);
                    List<FlowModInputBuilder> ofFlowRemoveInput = FlowConvertor.toFlowModInputs(removeflow.build(),
                            version, getSession().getFeatures().getDatapathId());
                    // remove flow should be the first
                    allFlowMods.addAll(ofFlowRemoveInput);
                    AddFlowInputBuilder addFlowInputBuilder = new AddFlowInputBuilder(updated);
                    ofFlowModInputs = FlowConvertor.toFlowModInputs(addFlowInputBuilder.build(),
                            version, getSession().getFeatures().getDatapathId());
                } else {
                    ofFlowModInputs = FlowConvertor.toFlowModInputs(updated,
                            version, getSession().getFeatures().getDatapathId());
                }

                //deleting flow hash value from operational DS
                if (flowId != null) {
                    CheckedFuture<Optional<FlowHashIdMapping>, ReadFailedException> hashDeletionFuture
                        = readFlowHashIdMappingFromOperationalDS(rwTx);
                    Futures.addCallback(hashDeletionFuture, new FutureCallback<Optional<FlowHashIdMapping>>() {
                        @Override
                        public void onSuccess(Optional<FlowHashIdMapping> optFlowHashIdMapping) {
                          FlowHashIdMapKey flowHashIdMapKeyToDelete = null;
                          if (optFlowHashIdMapping.isPresent()) {
                              FlowHashIdMapping flowHashIdMapping = optFlowHashIdMapping.get();
                              for (FlowHashIdMap flowHashId : flowHashIdMapping.getFlowHashIdMap()) {
                                  if (flowHashId.getFlowId().getValue().equals(flowId)) {
                                      flowHashIdMapKeyToDelete = flowHashId.getKey();
                                      break;
                                  }
                              }
                          }
                          if (flowHashIdMapKeyToDelete != null) {
                              final KeyedInstanceIdentifier<FlowHashIdMap, FlowHashIdMapKey> iiToFlowHashIdToDelete = iiToTable
                                    .augmentation(FlowHashIdMapping.class).child(FlowHashIdMap.class, flowHashIdMapKeyToDelete);
                              rwTx.delete(LogicalDatastoreType.OPERATIONAL, iiToFlowHashIdToDelete);
                              rwTx.submit();
                          }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            LOG.debug("Reading flow-hash-id map from operational DS wasn't successful");
                        }
                    });

                }

                allFlowMods.addAll(ofFlowModInputs);
                LOG.debug("Number of flows to push to switch: {}", allFlowMods.size());
                result = chainFlowMods(allFlowMods, 0, getTaskContext(), getCookie());

                result = OFRpcTaskUtil.chainFutureBarrier(this, result);
                OFRpcTaskUtil.hookFutureNotification(this, result,
                        getRpcNotificationProviderService(),
                        createFlowUpdatedNotification(in));

                return result;
            }


            CheckedFuture<Optional<FlowHashIdMapping>, ReadFailedException> readFlowHashIdMappingFromOperationalDS(final ReadWriteTransaction rwTx) {
                InstanceIdentifier<FlowHashIdMapping> iiToFlowHashIdMapping = iiToTable
                        .augmentation(FlowHashIdMapping.class);
                    return rwTx.read(LogicalDatastoreType.OPERATIONAL, iiToFlowHashIdMapping);
            }

            @Override
            public Boolean isBarrier() {
                return getInput().getUpdatedFlow().isBarrier();
            }
        }
        return new OFRpcTaskImpl(taskContext, cookie, input, rwTx);
    }


    /**
     * @param input update flow input
     * @return flow update notification
     */
    protected static NotificationComposer<FlowUpdated> createFlowUpdatedNotification(final UpdateFlowInput input) {
        return new NotificationComposer<FlowUpdated>() {
            @Override
            public FlowUpdated compose(TransactionId tXid) {
                FlowUpdatedBuilder updFlow = new FlowUpdatedBuilder(input.getUpdatedFlow());
                updFlow.setTransactionId(tXid);
                updFlow.setFlowRef(input.getFlowRef());
                return updFlow.build();
            }
        };
    }

    /**
     * @param taskContext taks context
     * @param input group update input
     * @param cookie switch connection distinguisher cookie value
     * @return update group task
     */
    public static OFRpcTask<AddGroupInput, RpcResult<UpdateGroupOutput>> createAddGroupTask(
            final OFRpcTaskContext taskContext, AddGroupInput input,
            final SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<AddGroupInput, RpcResult<UpdateGroupOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie, AddGroupInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<UpdateGroupOutput>> call() {
                ListenableFuture<RpcResult<UpdateGroupOutput>> result = SettableFuture.create();

                // Convert the AddGroupInput to GroupModInput
                GroupModInputBuilder ofGroupModInput = GroupConvertor.toGroupModInput(getInput(),
                        getVersion(), getSession().getFeatures().getDatapathId());
                final Long xId = getSession().getNextXid();
                ofGroupModInput.setXid(xId);

                Future<RpcResult<UpdateGroupOutput>> resultFromOFLib = getMessageService()
                        .groupMod(ofGroupModInput.build(), getCookie());
                result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                result = OFRpcTaskUtil.chainFutureBarrier(this, result);
                OFRpcTaskUtil.hookFutureNotification(this, result,
                        getRpcNotificationProviderService(), createGroupAddedNotification(getInput()));

                return result;
            }

            @Override
            public Boolean isBarrier() {
                return getInput().isBarrier();
            }
        };

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }


    /**
     * @param input group add input
     * @return group added notification
     */
    protected static NotificationComposer<GroupAdded> createGroupAddedNotification(
            final AddGroupInput input) {
        return new NotificationComposer<GroupAdded>() {
            @Override
            public GroupAdded compose(TransactionId tXid) {
                GroupAddedBuilder groupMod = new GroupAddedBuilder((Group) input);
                groupMod.setTransactionId(tXid);
                groupMod.setGroupRef(input.getGroupRef());
                return groupMod.build();
            }
        };
    }

    /**
     * @param taskContext task context
     * @param input meter add input
     * @param cookie switch connection distinguisher
     * @return update meter task
     */
    public static OFRpcTask<AddMeterInput, RpcResult<UpdateMeterOutput>> createAddMeterTask(
            OFRpcTaskContext taskContext, AddMeterInput input,
            SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<AddMeterInput, RpcResult<UpdateMeterOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie, AddMeterInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<UpdateMeterOutput>> call() {
                ListenableFuture<RpcResult<UpdateMeterOutput>> result = SettableFuture.create();

                // Convert the AddGroupInput to GroupModInput
                MeterModInputBuilder ofMeterModInput = MeterConvertor.toMeterModInput(getInput(), getVersion());
                final Long xId = getSession().getNextXid();
                ofMeterModInput.setXid(xId);

                Future<RpcResult<UpdateMeterOutput>> resultFromOFLib = getMessageService()
                        .meterMod(ofMeterModInput.build(), getCookie());
                result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                result = OFRpcTaskUtil.chainFutureBarrier(this, result);
                OFRpcTaskUtil.hookFutureNotification(this, result,
                        getRpcNotificationProviderService(), createMeterAddedNotification(getInput()));

                return result;
            }

            @Override
            public Boolean isBarrier() {
                return getInput().isBarrier();
            }
        };

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param input add meter input
     * @return meter added notification composer
     */
    protected static NotificationComposer<MeterAdded> createMeterAddedNotification(
            final AddMeterInput input) {
        return new NotificationComposer<MeterAdded>() {
            @Override
            public MeterAdded compose(TransactionId tXid) {
                MeterAddedBuilder meterMod = new MeterAddedBuilder((Meter) input);
                meterMod.setTransactionId(tXid);
                meterMod.setMeterRef(input.getMeterRef());
                return meterMod.build();
            }
        };
    }

    /**
     * @param taskContext task context
     * @param input update group output
     * @param cookie switch connection distinguisher cookie value
     * @return UpdateFlow task
     */
    public static OFRpcTask<UpdateGroupInput, RpcResult<UpdateGroupOutput>> createUpdateGroupTask(
            OFRpcTaskContext taskContext, UpdateGroupInput input,
            SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<UpdateGroupInput, RpcResult<UpdateGroupOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    UpdateGroupInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<UpdateGroupOutput>> call() {
                ListenableFuture<RpcResult<UpdateGroupOutput>> result = null;

                // Convert the UpdateGroupInput to GroupModInput
                GroupModInputBuilder ofGroupModInput = GroupConvertor.toGroupModInput(
                        getInput().getUpdatedGroup(), getVersion(),
                        getSession().getFeatures().getDatapathId());
                final Long xId = getSession().getNextXid();
                ofGroupModInput.setXid(xId);

                Future<RpcResult<UpdateGroupOutput>> resultFromOFLib =
                        getMessageService().groupMod(ofGroupModInput.build(), getCookie());
                result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                result = OFRpcTaskUtil.chainFutureBarrier(this, result);
                OFRpcTaskUtil.hookFutureNotification(this, result,
                        getRpcNotificationProviderService(), createGroupUpdatedNotification(getInput()));

                return result;
            }

            @Override
            public Boolean isBarrier() {
                return getInput().getUpdatedGroup().isBarrier();
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param input group update input
     * @return group updated notification composer
     */
    protected static NotificationComposer<GroupUpdated> createGroupUpdatedNotification(
            final UpdateGroupInput input) {
        return new NotificationComposer<GroupUpdated>() {
            @Override
            public GroupUpdated compose(TransactionId tXid) {
                GroupUpdatedBuilder groupMod = new GroupUpdatedBuilder(input.getUpdatedGroup());
                groupMod.setTransactionId(tXid);
                groupMod.setGroupRef(input.getGroupRef());
                return groupMod.build();
            }
        };
    }

    /**
     * @param taskContext task context
     * @param input update meter input
     * @param cookie switch connection distinguisher cookie value
     * @return update meter task
     */
    public static OFRpcTask<UpdateMeterInput, RpcResult<UpdateMeterOutput>> createUpdateMeterTask(
            OFRpcTaskContext taskContext, UpdateMeterInput input,
            SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<UpdateMeterInput, RpcResult<UpdateMeterOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    UpdateMeterInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<UpdateMeterOutput>> call() {
                ListenableFuture<RpcResult<UpdateMeterOutput>> result = null;

                // Convert the UpdateMeterInput to MeterModInput
                MeterModInputBuilder ofMeterModInput = MeterConvertor.toMeterModInput(
                        getInput().getUpdatedMeter(), getVersion());
                final Long xId = getSession().getNextXid();
                ofMeterModInput.setXid(xId);

                Future<RpcResult<UpdateMeterOutput>> resultFromOFLib =
                        getMessageService().meterMod(ofMeterModInput.build(), getCookie());
                result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                result = OFRpcTaskUtil.chainFutureBarrier(this, result);
                OFRpcTaskUtil.hookFutureNotification(this, result,
                        getRpcNotificationProviderService(), createMeterUpdatedNotification(getInput()));
                return result;
            }

            @Override
            public Boolean isBarrier() {
                return getInput().getUpdatedMeter().isBarrier();
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param input meter update input
     * @return meter updated notification
     */
    protected static NotificationComposer<MeterUpdated> createMeterUpdatedNotification(
            final UpdateMeterInput input) {
        return new NotificationComposer<MeterUpdated>() {
            @Override
            public MeterUpdated compose(TransactionId tXid) {
                MeterUpdatedBuilder meterMod = new MeterUpdatedBuilder(input.getUpdatedMeter());
                meterMod.setTransactionId(tXid);
                meterMod.setMeterRef(input.getMeterRef());
                return meterMod.build();
            }
        };
    }


    /**
     * @param taskContext task context
     * @param input update flow input
     * @param cookie switch connection distinguisher cookie value
     * @return task remove flow task
     */
    public static OFRpcTask<RemoveFlowInput, RpcResult<UpdateFlowOutput>> createRemoveFlowTask(
            OFRpcTaskContext taskContext, RemoveFlowInput input,
            SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<RemoveFlowInput, RpcResult<UpdateFlowOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    RemoveFlowInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<UpdateFlowOutput>> call() {
                ListenableFuture<RpcResult<UpdateFlowOutput>> result = SettableFuture.create();

                // Convert the AddFlowInput to FlowModInput
                List<FlowModInputBuilder> ofFlowModInputs = FlowConvertor.toFlowModInputs(getInput(),
                        getVersion(), getSession().getFeatures().getDatapathId());

                result = chainFlowMods(ofFlowModInputs, 0, getTaskContext(), getCookie());
                result = OFRpcTaskUtil.chainFutureBarrier(this, result);

                OFRpcTaskUtil.hookFutureNotification(this, result,
                        getRpcNotificationProviderService(),
                        createFlowRemovedNotification(getInput()));
                return result;
            }

            @Override
            public Boolean isBarrier() {
                return getInput().isBarrier();
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param input remove flow input
     * @return flow removed notification
     */
    protected static NotificationComposer<FlowRemoved> createFlowRemovedNotification(
            final RemoveFlowInput input) {
        return new NotificationComposer<FlowRemoved>() {
            @Override
            public FlowRemoved compose(TransactionId tXid) {
                FlowRemovedBuilder removedFlow = new FlowRemovedBuilder((Flow) input);
                removedFlow.setTransactionId(tXid);
                removedFlow.setFlowRef(input.getFlowRef());
                return removedFlow.build();
            }
        };
    }


    /**
     * @param taskContext task context
     * @param input remove group input
     * @param cookie switch connection distinguisher cookie value
     * @return task remove group task
     */
    public static OFRpcTask<RemoveGroupInput, RpcResult<UpdateGroupOutput>> createRemoveGroupTask(
            final OFRpcTaskContext taskContext, RemoveGroupInput input,
            final SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<RemoveGroupInput, RpcResult<UpdateGroupOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    RemoveGroupInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<UpdateGroupOutput>> call() {
                ListenableFuture<RpcResult<UpdateGroupOutput>> result = SettableFuture.create();

                // Convert the AddGroupInput to GroupModInput
                GroupModInputBuilder ofGroupModInput = GroupConvertor.toGroupModInput(getInput(),
                        getVersion(), getSession().getFeatures().getDatapathId());
                final Long xId = getSession().getNextXid();
                ofGroupModInput.setXid(xId);

                Future<RpcResult<UpdateGroupOutput>> resultFromOFLib = getMessageService()
                        .groupMod(ofGroupModInput.build(), getCookie());
                result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                result = OFRpcTaskUtil.chainFutureBarrier(this, result);
                OFRpcTaskUtil.hookFutureNotification(this, result,
                        getRpcNotificationProviderService(), createGroupRemovedNotification(getInput()));

                return result;
            }

            @Override
            public Boolean isBarrier() {
                return getInput().isBarrier();
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param input group remove input
     * @return group removed notification
     */
    protected static NotificationComposer<GroupRemoved> createGroupRemovedNotification(
            final RemoveGroupInput input) {
        return new NotificationComposer<GroupRemoved>() {
            @Override
            public GroupRemoved compose(TransactionId tXid) {
                GroupRemovedBuilder removedGroup = new GroupRemovedBuilder((Group) input);
                removedGroup.setTransactionId(tXid);
                removedGroup.setGroupRef(input.getGroupRef());
                return removedGroup.build();
            }
        };
    }

    /**
     * @param taskContext task context
     * @param input meter removed input
     * @param cookie switch connection distinguisher cookie value
     * @return task meter remove task
     */
    public static OFRpcTask<RemoveMeterInput, RpcResult<UpdateMeterOutput>> createRemoveMeterTask(
            OFRpcTaskContext taskContext, RemoveMeterInput input,
            SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<RemoveMeterInput, RpcResult<UpdateMeterOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    RemoveMeterInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<UpdateMeterOutput>> call() {
                ListenableFuture<RpcResult<UpdateMeterOutput>> result = SettableFuture.create();

                // Convert the AddGroupInput to GroupModInput
                MeterModInputBuilder ofMeterModInput = MeterConvertor.toMeterModInput(getInput(), getVersion());
                final Long xId = getSession().getNextXid();
                ofMeterModInput.setXid(xId);

                Future<RpcResult<UpdateMeterOutput>> resultFromOFLib = getMessageService()
                        .meterMod(ofMeterModInput.build(), getCookie());
                result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                result = OFRpcTaskUtil.chainFutureBarrier(this, result);
                OFRpcTaskUtil.hookFutureNotification(this, result,
                        getRpcNotificationProviderService(), createMeterRemovedNotification(getInput()));

                return result;
            }

            @Override
            public Boolean isBarrier() {
                return getInput().isBarrier();
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);

    }

    /**
     * @param input remove meter input
     * @return meter removed notification composer
     */
    protected static NotificationComposer<MeterRemoved> createMeterRemovedNotification(
            final RemoveMeterInput input) {
        return new NotificationComposer<MeterRemoved>() {
            @Override
            public MeterRemoved compose(TransactionId tXid) {
                MeterRemovedBuilder meterRemoved = new MeterRemovedBuilder((Meter) input);
                meterRemoved.setTransactionId(tXid);
                meterRemoved.setMeterRef(input.getMeterRef());
                return meterRemoved.build();
            }
        };
    }

    /**
     * @param taskContext task context
     * @param input get all statistics input
     * @param cookie switch connection distinguisher cookie value
     * @return task get all group statistics task
     */
    public static OFRpcTask<GetAllGroupStatisticsInput, RpcResult<GetAllGroupStatisticsOutput>> createGetAllGroupStatisticsTask(
            final OFRpcTaskContext taskContext, GetAllGroupStatisticsInput input,
            SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<GetAllGroupStatisticsInput, RpcResult<GetAllGroupStatisticsOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetAllGroupStatisticsInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetAllGroupStatisticsOutput>> call() {
                final SettableFuture<RpcResult<GetAllGroupStatisticsOutput>> result = SettableFuture.create();

                if (taskContext.getSession().getPrimaryConductor().getVersion() == OFConstants.OFP_VERSION_1_0) {
                    RpcResult<GetAllGroupStatisticsOutput> rpcResult = RpcResultBuilder.success(
                            new GetAllGroupStatisticsOutputBuilder().build()).build();

                    return Futures.immediateFuture(rpcResult);
                } else {

                    // Generate xid to associate it with the request
                    final Long xid = taskContext.getSession().getNextXid();

                    // Create multipart request body for fetch all the group stats
                    MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
                    MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
                    mprGroupBuild.setGroupId(new GroupId(BinContent.intToUnsignedLong(
                            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731
                                    .Group.OFPGALL.getIntValue())));
                    caseBuilder.setMultipartRequestGroup(mprGroupBuild.build());

                    // Create multipart request header
                    MultipartRequestInputBuilder mprInput = createMultipartHeader(MultipartType.OFPMPGROUP,
                            taskContext, xid);

                    // Set request body to main multipart request
                    mprInput.setMultipartRequestBody(caseBuilder.build());

                    // Send the request, no cookies associated, use any connection

                    Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                            .multipartRequest(mprInput.build(), getCookie());
                    ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                    Futures.addCallback(resultLib, new ResultCallback<GetAllGroupStatisticsOutput>(result) {
                        @Override
                        public GetAllGroupStatisticsOutput createResult() {
                            GetAllGroupStatisticsOutputBuilder groupStatBuilder = new GetAllGroupStatisticsOutputBuilder()
                                    .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                            return groupStatBuilder.build();
                        }
                    });

                    return result;
                }
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get group description input
     * @param cookie switch connection distinguisher cookie value
     * @return task get group description task
     */
    public static OFRpcTask<GetGroupDescriptionInput, RpcResult<GetGroupDescriptionOutput>> createGetGroupDescriptionTask(
            final OFRpcTaskContext taskContext, GetGroupDescriptionInput input,
            SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<GetGroupDescriptionInput, RpcResult<GetGroupDescriptionOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetGroupDescriptionInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetGroupDescriptionOutput>> call()
                    throws Exception {
                final SettableFuture<RpcResult<GetGroupDescriptionOutput>> result = SettableFuture.create();

                if (taskContext.getSession().getPrimaryConductor().getVersion() == OFConstants.OFP_VERSION_1_0) {
                    RpcResult<GetGroupDescriptionOutput> rpcResult = RpcResultBuilder.success(
                            new GetGroupDescriptionOutputBuilder().build()).build();
                    return Futures.immediateFuture(rpcResult);
                } else {
                    final Long xid = taskContext.getSession().getNextXid();

                    MultipartRequestGroupDescCaseBuilder mprGroupDescCaseBuild =
                            new MultipartRequestGroupDescCaseBuilder();
                    MultipartRequestInputBuilder mprInput =
                            createMultipartHeader(MultipartType.OFPMPGROUPDESC, taskContext, xid);
                    mprInput.setMultipartRequestBody(mprGroupDescCaseBuild.build());
                    Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                            .multipartRequest(mprInput.build(), getCookie());
                    ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                    Futures.addCallback(resultLib, new ResultCallback<GetGroupDescriptionOutput>(result) {
                        @Override
                        public GetGroupDescriptionOutput createResult() {
                            GetGroupDescriptionOutputBuilder groupStatBuilder = new GetGroupDescriptionOutputBuilder()
                                    .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                            return groupStatBuilder.build();
                        }
                    });
                    return result;
                }
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get group feature input
     * @param cookie switch connection distinguisher cookie value
     * @return task get group feature task
     */
    public static OFRpcTask<GetGroupFeaturesInput, RpcResult<GetGroupFeaturesOutput>> createGetGroupFeaturesTask(
            final OFRpcTaskContext taskContext, GetGroupFeaturesInput input,
            SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<GetGroupFeaturesInput, RpcResult<GetGroupFeaturesOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetGroupFeaturesInput input) {
                super(taskContext, cookie, input);
                // TODO Auto-generated constructor stub
            }

            @Override
            public ListenableFuture<RpcResult<GetGroupFeaturesOutput>> call()
                    throws Exception {
                final SettableFuture<RpcResult<GetGroupFeaturesOutput>> result = SettableFuture.create();

                if (taskContext.getSession().getPrimaryConductor().getVersion() == OFConstants.OFP_VERSION_1_0) {
                    RpcResult<GetGroupFeaturesOutput> rpcResult = RpcResultBuilder.success(
                            new GetGroupFeaturesOutputBuilder().build()).build();
                    return Futures.immediateFuture(rpcResult);
                } else {
                    final Long xid = taskContext.getSession().getNextXid();

                    MultipartRequestGroupFeaturesCaseBuilder mprGroupFeaturesBuild =
                            new MultipartRequestGroupFeaturesCaseBuilder();
                    MultipartRequestInputBuilder mprInput =
                            createMultipartHeader(MultipartType.OFPMPGROUPFEATURES, taskContext, xid);
                    mprInput.setMultipartRequestBody(mprGroupFeaturesBuild.build());
                    Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                            .multipartRequest(mprInput.build(), getCookie());
                    ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                    Futures.addCallback(resultLib, new ResultCallback<GetGroupFeaturesOutput>(result) {
                        @Override
                        public GetGroupFeaturesOutput createResult() {
                            GetGroupFeaturesOutputBuilder groupFeatureBuilder = new GetGroupFeaturesOutputBuilder()
                                    .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                            return groupFeatureBuilder.build();
                        }
                    });
                    return result;
                }
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get group statistics input
     * @param cookie switch connection distinguisher cookie value
     * @return task get group statistics task
     */
    public static OFRpcTask<GetGroupStatisticsInput, RpcResult<GetGroupStatisticsOutput>> createGetGroupStatisticsTask(
            final OFRpcTaskContext taskContext, final GetGroupStatisticsInput input,
            SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<GetGroupStatisticsInput, RpcResult<GetGroupStatisticsOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetGroupStatisticsInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetGroupStatisticsOutput>> call()
                    throws Exception {
                final SettableFuture<RpcResult<GetGroupStatisticsOutput>> result = SettableFuture.create();

                if (taskContext.getSession().getPrimaryConductor().getVersion() == OFConstants.OFP_VERSION_1_0) {
                    RpcResult<GetGroupStatisticsOutput> rpcResult = RpcResultBuilder.success(
                            new GetGroupStatisticsOutputBuilder().build()).build();
                    return Futures.immediateFuture(rpcResult);
                } else {
                    final Long xid = taskContext.getSession().getNextXid();

                    MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
                    MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();
                    mprGroupBuild.setGroupId(new GroupId(input.getGroupId().getValue()));
                    caseBuilder.setMultipartRequestGroup(mprGroupBuild.build());

                    MultipartRequestInputBuilder mprInput =
                            createMultipartHeader(MultipartType.OFPMPGROUP, taskContext, xid);
                    mprInput.setMultipartRequestBody(caseBuilder.build());
                    Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                            .multipartRequest(mprInput.build(), getCookie());
                    ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                    Futures.addCallback(resultLib, new ResultCallback<GetGroupStatisticsOutput>(result) {
                        @Override
                        public GetGroupStatisticsOutput createResult() {
                            GetGroupStatisticsOutputBuilder groupStatisticsBuilder =
                                    new GetGroupStatisticsOutputBuilder()
                                            .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                            return groupStatisticsBuilder.build();
                        }
                    });
                    return result;
                }
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get meter config statistics input
     * @param cookie switch connection distinguisher cookie value
     * @return task get all mtere config statistics task
     */
    public static OFRpcTask<GetAllMeterConfigStatisticsInput, RpcResult<GetAllMeterConfigStatisticsOutput>> createGetAllMeterConfigStatisticsTask(
            final OFRpcTaskContext taskContext, final GetAllMeterConfigStatisticsInput input,
            SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<GetAllMeterConfigStatisticsInput, RpcResult<GetAllMeterConfigStatisticsOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetAllMeterConfigStatisticsInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetAllMeterConfigStatisticsOutput>> call()
                    throws Exception {
                final SettableFuture<RpcResult<GetAllMeterConfigStatisticsOutput>> result = SettableFuture.create();

                if (taskContext.getSession().getPrimaryConductor().getVersion() == OFConstants.OFP_VERSION_1_0) {
                    RpcResult<GetAllMeterConfigStatisticsOutput> rpcResult = RpcResultBuilder.success(
                            new GetAllMeterConfigStatisticsOutputBuilder().build()).build();
                    return Futures.immediateFuture(rpcResult);
                } else {
                    final Long xid = taskContext.getSession().getNextXid();

                    MultipartRequestMeterConfigCaseBuilder caseBuilder =
                            new MultipartRequestMeterConfigCaseBuilder();
                    MultipartRequestMeterConfigBuilder mprMeterConfigBuild =
                            new MultipartRequestMeterConfigBuilder();
                    mprMeterConfigBuild.setMeterId(new MeterId(BinContent.intToUnsignedLong(
                            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common
                                    .types.rev130731.Meter.OFPMALL.getIntValue())));
                    caseBuilder.setMultipartRequestMeterConfig(mprMeterConfigBuild.build());

                    MultipartRequestInputBuilder mprInput =
                            createMultipartHeader(MultipartType.OFPMPMETERCONFIG, taskContext, xid);
                    mprInput.setMultipartRequestBody(caseBuilder.build());
                    Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                            .multipartRequest(mprInput.build(), getCookie());
                    ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                    Futures.addCallback(resultLib, new ResultCallback<GetAllMeterConfigStatisticsOutput>(result) {
                        @Override
                        public GetAllMeterConfigStatisticsOutput createResult() {
                            GetAllMeterConfigStatisticsOutputBuilder allMeterConfStatBuilder =
                                    new GetAllMeterConfigStatisticsOutputBuilder()
                                            .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                            return allMeterConfStatBuilder.build();
                        }
                    });
                    return result;
                }
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get all meter statistics input
     * @param cookie switch connection distinguisher cookie value
     * @return task get all meter statistics task
     */
    public static OFRpcTask<GetAllMeterStatisticsInput, RpcResult<GetAllMeterStatisticsOutput>> createGetAllMeterStatisticsTask(
            final OFRpcTaskContext taskContext, final GetAllMeterStatisticsInput input,
            SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<GetAllMeterStatisticsInput, RpcResult<GetAllMeterStatisticsOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetAllMeterStatisticsInput input) {
                super(taskContext, cookie, input);
                // TODO Auto-generated constructor stub
            }

            @Override
            public ListenableFuture<RpcResult<GetAllMeterStatisticsOutput>> call()
                    throws Exception {
                final SettableFuture<RpcResult<GetAllMeterStatisticsOutput>> result = SettableFuture.create();

                if (taskContext.getSession().getPrimaryConductor().getVersion() == OFConstants.OFP_VERSION_1_0) {
                    RpcResult<GetAllMeterStatisticsOutput> rpcResult = RpcResultBuilder.success(
                            new GetAllMeterStatisticsOutputBuilder().build()).build();
                    return Futures.immediateFuture(rpcResult);
                } else {
                    final Long xid = taskContext.getSession().getNextXid();

                    MultipartRequestMeterCaseBuilder caseBuilder =
                            new MultipartRequestMeterCaseBuilder();
                    MultipartRequestMeterBuilder mprMeterBuild =
                            new MultipartRequestMeterBuilder();
                    mprMeterBuild.setMeterId(new MeterId(BinContent.intToUnsignedLong(
                            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common
                                    .types.rev130731.Meter.OFPMALL.getIntValue())));
                    caseBuilder.setMultipartRequestMeter(mprMeterBuild.build());

                    MultipartRequestInputBuilder mprInput =
                            createMultipartHeader(MultipartType.OFPMPMETER, taskContext, xid);
                    mprInput.setMultipartRequestBody(caseBuilder.build());
                    Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                            .multipartRequest(mprInput.build(), getCookie());
                    ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                    Futures.addCallback(resultLib, new ResultCallback<GetAllMeterStatisticsOutput>(result) {
                        @Override
                        public GetAllMeterStatisticsOutput createResult() {
                            GetAllMeterStatisticsOutputBuilder allMeterStatBuilder =
                                    new GetAllMeterStatisticsOutputBuilder()
                                            .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                            return allMeterStatBuilder.build();
                        }
                    });
                    return result;
                }
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get meter features input
     * @param cookie switch connection distinguisher cookie value
     * @return task get meter feature task
     */
    public static OFRpcTask<GetMeterFeaturesInput, RpcResult<GetMeterFeaturesOutput>> createGetMeterFeaturesTask(
            final OFRpcTaskContext taskContext, final GetMeterFeaturesInput input,
            SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<GetMeterFeaturesInput, RpcResult<GetMeterFeaturesOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetMeterFeaturesInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetMeterFeaturesOutput>> call()
                    throws Exception {
                final SettableFuture<RpcResult<GetMeterFeaturesOutput>> result = SettableFuture.create();

                if (taskContext.getSession().getPrimaryConductor().getVersion() == OFConstants.OFP_VERSION_1_0) {
                    RpcResult<GetMeterFeaturesOutput> rpcResult = RpcResultBuilder.success(
                            new GetMeterFeaturesOutputBuilder().build()).build();
                    return Futures.immediateFuture(rpcResult);
                } else {
                    final Long xid = taskContext.getSession().getNextXid();

                    MultipartRequestMeterFeaturesCaseBuilder mprMeterFeaturesBuild =
                            new MultipartRequestMeterFeaturesCaseBuilder();

                    MultipartRequestInputBuilder mprInput =
                            createMultipartHeader(MultipartType.OFPMPMETERFEATURES, taskContext, xid);
                    mprInput.setMultipartRequestBody(mprMeterFeaturesBuild.build());
                    Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                            .multipartRequest(mprInput.build(), getCookie());
                    ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                    Futures.addCallback(resultLib, new ResultCallback<GetMeterFeaturesOutput>(result) {
                        @Override
                        public GetMeterFeaturesOutput createResult() {
                            GetMeterFeaturesOutputBuilder meterFeaturesBuilder =
                                    new GetMeterFeaturesOutputBuilder()
                                            .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                            return meterFeaturesBuilder.build();
                        }
                    });
                    return result;
                }
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get meter statistics input
     * @param cookie switch connection distinguisher cookie value
     * @return task get meter statistics task
     */
    public static OFRpcTask<GetMeterStatisticsInput, RpcResult<GetMeterStatisticsOutput>> createGetMeterStatisticsTask(
            final OFRpcTaskContext taskContext, final GetMeterStatisticsInput input,
            SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<GetMeterStatisticsInput, RpcResult<GetMeterStatisticsOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetMeterStatisticsInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetMeterStatisticsOutput>> call()
                    throws Exception {
                final SettableFuture<RpcResult<GetMeterStatisticsOutput>> result = SettableFuture.create();

                if (taskContext.getSession().getPrimaryConductor().getVersion() == OFConstants.OFP_VERSION_1_0) {
                    RpcResult<GetMeterStatisticsOutput> rpcResult = RpcResultBuilder.success(
                            new GetMeterStatisticsOutputBuilder().build()).build();
                    return Futures.immediateFuture(rpcResult);
                } else {
                    final Long xid = taskContext.getSession().getNextXid();

                    MultipartRequestMeterCaseBuilder caseBuilder =
                            new MultipartRequestMeterCaseBuilder();
                    MultipartRequestMeterBuilder mprMeterBuild =
                            new MultipartRequestMeterBuilder();
                    mprMeterBuild.setMeterId(new MeterId(input.getMeterId().getValue()));
                    caseBuilder.setMultipartRequestMeter(mprMeterBuild.build());

                    MultipartRequestInputBuilder mprInput =
                            createMultipartHeader(MultipartType.OFPMPMETER, taskContext, xid);
                    mprInput.setMultipartRequestBody(caseBuilder.build());
                    Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                            .multipartRequest(mprInput.build(), getCookie());
                    ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                    Futures.addCallback(resultLib, new ResultCallback<GetMeterStatisticsOutput>(result) {
                        @Override
                        public GetMeterStatisticsOutput createResult() {
                            GetMeterStatisticsOutputBuilder meterStatBuilder =
                                    new GetMeterStatisticsOutputBuilder()
                                            .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                            return meterStatBuilder.build();
                        }
                    });
                    return result;
                }
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get all node connector statistics input
     * @param cookie switch connection distinguisher cookie value
     * @return task get all node connector statistics task
     */
    public static OFRpcTask<GetAllNodeConnectorsStatisticsInput, RpcResult<GetAllNodeConnectorsStatisticsOutput>>
    createGetAllNodeConnectorsStatisticsTask(
            final OFRpcTaskContext taskContext, final GetAllNodeConnectorsStatisticsInput input,
            SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<GetAllNodeConnectorsStatisticsInput, RpcResult<GetAllNodeConnectorsStatisticsOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetAllNodeConnectorsStatisticsInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetAllNodeConnectorsStatisticsOutput>> call()
                    throws Exception {
                final SettableFuture<RpcResult<GetAllNodeConnectorsStatisticsOutput>> result = SettableFuture.create();

                final Long xid = taskContext.getSession().getNextXid();

                MultipartRequestPortStatsCaseBuilder caseBuilder =
                        new MultipartRequestPortStatsCaseBuilder();
                MultipartRequestPortStatsBuilder mprPortStatsBuilder =
                        new MultipartRequestPortStatsBuilder();
                // Select all ports
                mprPortStatsBuilder.setPortNo(OFConstants.OFPP_ANY);
                caseBuilder.setMultipartRequestPortStats(mprPortStatsBuilder.build());

                MultipartRequestInputBuilder mprInput =
                        createMultipartHeader(MultipartType.OFPMPPORTSTATS, taskContext, xid);
                mprInput.setMultipartRequestBody(caseBuilder.build());
                Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                        .multipartRequest(mprInput.build(), getCookie());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<GetAllNodeConnectorsStatisticsOutput>(result) {
                    @Override
                    public GetAllNodeConnectorsStatisticsOutput createResult() {
                        GetAllNodeConnectorsStatisticsOutputBuilder allNodeConnectorStatBuilder =
                                new GetAllNodeConnectorsStatisticsOutputBuilder()
                                        .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return allNodeConnectorStatBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get node connector statistics input
     * @param cookie switch connection distinguisher cookie value
     * @return task get node connector statistics task
     */
    public static OFRpcTask<GetNodeConnectorStatisticsInput, RpcResult<GetNodeConnectorStatisticsOutput>>
    createGetNodeConnectorStatisticsTask(
            final OFRpcTaskContext taskContext, final GetNodeConnectorStatisticsInput input,
            SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<GetNodeConnectorStatisticsInput, RpcResult<GetNodeConnectorStatisticsOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetNodeConnectorStatisticsInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetNodeConnectorStatisticsOutput>> call()
                    throws Exception {
                final SettableFuture<RpcResult<GetNodeConnectorStatisticsOutput>> result = SettableFuture.create();

                final Long xid = taskContext.getSession().getNextXid();

                MultipartRequestPortStatsCaseBuilder caseBuilder =
                        new MultipartRequestPortStatsCaseBuilder();
                MultipartRequestPortStatsBuilder mprPortStatsBuilder =
                        new MultipartRequestPortStatsBuilder();
                // Set specific port
                mprPortStatsBuilder
                        .setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                                OpenflowVersion.get(taskContext.getSession().getFeatures().getVersion()),
                                input.getNodeConnectorId()));
                caseBuilder.setMultipartRequestPortStats(mprPortStatsBuilder.build());

                MultipartRequestInputBuilder mprInput =
                        createMultipartHeader(MultipartType.OFPMPPORTSTATS, taskContext, xid);
                mprInput.setMultipartRequestBody(caseBuilder.build());
                Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                        .multipartRequest(mprInput.build(), getCookie());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<GetNodeConnectorStatisticsOutput>(result) {
                    @Override
                    public GetNodeConnectorStatisticsOutput createResult() {
                        GetNodeConnectorStatisticsOutputBuilder allNodeConnectorStatBuilder =
                                new GetNodeConnectorStatisticsOutputBuilder()
                                        .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return allNodeConnectorStatBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get all flow statistics from flow table
     * @param cookie switch connection distinguisher cookie value
     * @return task get all flow statistics from flow table task
     */
    public static OFRpcTask<GetAllFlowStatisticsFromFlowTableInput, RpcResult<GetAllFlowStatisticsFromFlowTableOutput>>
    createGetAllFlowStatisticsFromFlowTableTask(
            final OFRpcTaskContext taskContext,
            final GetAllFlowStatisticsFromFlowTableInput input,
            SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<GetAllFlowStatisticsFromFlowTableInput, RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetAllFlowStatisticsFromFlowTableInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> call() throws Exception {
                final SettableFuture<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> result = SettableFuture.create();

                final Long xid = taskContext.getSession().getNextXid();

                MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
                mprFlowRequestBuilder.setTableId(input.getTableId().getValue());
                mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
                mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
                mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
                mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
                FlowCreatorUtil.setWildcardedFlowMatch(taskContext.getSession()
                        .getPrimaryConductor().getVersion(), mprFlowRequestBuilder);

                MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
                multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());

                MultipartRequestInputBuilder mprInput =
                        createMultipartHeader(MultipartType.OFPMPFLOW, taskContext, xid);
                mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());
                Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                        .multipartRequest(mprInput.build(), getCookie());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<GetAllFlowStatisticsFromFlowTableOutput>(result) {
                    @Override
                    public GetAllFlowStatisticsFromFlowTableOutput createResult() {
                        GetAllFlowStatisticsFromFlowTableOutputBuilder allFlowStatsFromFlowTableBuilder =
                                new GetAllFlowStatisticsFromFlowTableOutputBuilder()
                                        .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return allFlowStatsFromFlowTableBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get all flow statistics from all flow table
     * @param cookie switch connection distinguisher cookie value
     * @return task get all flow statistics from all flow table task
     */
    public static OFRpcTask<GetAllFlowsStatisticsFromAllFlowTablesInput, RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>>
    createGetAllFlowsStatisticsFromAllFlowTablesTask(
            final OFRpcTaskContext taskContext,
            final GetAllFlowsStatisticsFromAllFlowTablesInput input,
            SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<GetAllFlowsStatisticsFromAllFlowTablesInput,
                RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetAllFlowsStatisticsFromAllFlowTablesInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> call() throws Exception {
                final SettableFuture<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> result = SettableFuture.create();

                final Long xid = taskContext.getSession().getNextXid();

                // Create multipart request body for fetch all the group stats
                MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder =
                        new MultipartRequestFlowCaseBuilder();
                MultipartRequestFlowBuilder mprFlowRequestBuilder =
                        new MultipartRequestFlowBuilder();
                mprFlowRequestBuilder.setTableId(OFConstants.OFPTT_ALL);
                mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
                mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
                mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
                mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
                FlowCreatorUtil.setWildcardedFlowMatch(taskContext.getSession()
                        .getPrimaryConductor().getVersion(), mprFlowRequestBuilder);

                MultipartRequestInputBuilder mprInput =
                        createMultipartHeader(MultipartType.OFPMPFLOW, taskContext, xid);
                multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());
                mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());
                Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                        .multipartRequest(mprInput.build(), getCookie());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<GetAllFlowsStatisticsFromAllFlowTablesOutput>(result) {
                    @Override
                    public GetAllFlowsStatisticsFromAllFlowTablesOutput createResult() {
                        GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder allFlowStatsFromAllFlowTableBuilder =
                                new GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder()
                                        .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return allFlowStatsFromAllFlowTableBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get flow statistics from flow table
     * @param cookie switch connection distinguisher cookie value
     * @return task get flow statistics from flow table task
     */
    public static OFRpcTask<GetFlowStatisticsFromFlowTableInput, RpcResult<GetFlowStatisticsFromFlowTableOutput>>
    createGetFlowStatisticsFromFlowTableTask(
            final OFRpcTaskContext taskContext,
            final GetFlowStatisticsFromFlowTableInput input, SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<GetFlowStatisticsFromFlowTableInput, RpcResult<GetFlowStatisticsFromFlowTableOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetFlowStatisticsFromFlowTableInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetFlowStatisticsFromFlowTableOutput>> call() throws Exception {
                final SettableFuture<RpcResult<GetFlowStatisticsFromFlowTableOutput>> result = SettableFuture.create();

                final Long xid = taskContext.getSession().getNextXid();

                // Create multipart request body for fetch all the group stats
                MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
                MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
                mprFlowRequestBuilder.setTableId(input.getTableId());

                if (input.getOutPort() != null) {
                    mprFlowRequestBuilder.setOutPort(input.getOutPort().longValue());
                } else {
                    mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
                }

                if (input.getOutGroup() != null) {
                    mprFlowRequestBuilder.setOutGroup(input.getOutGroup());
                } else {
                    mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
                }

                if (input.getCookie() != null) {
                    mprFlowRequestBuilder.setCookie(input.getCookie().getValue());
                } else {
                    mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
                }

                if (input.getCookieMask() != null) {
                    mprFlowRequestBuilder.setCookieMask(input.getCookieMask().getValue());
                } else {
                    mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
                }

                // convert and inject match
                MatchReactor.getInstance().convert(input.getMatch(), taskContext.getSession()
                                .getPrimaryConductor().getVersion(), mprFlowRequestBuilder,
                        taskContext.getSession().getFeatures().getDatapathId());

                // Set request body to main multipart request
                multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());
                MultipartRequestInputBuilder mprInput =
                        createMultipartHeader(MultipartType.OFPMPFLOW, taskContext, xid);
                mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());
                Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                        .multipartRequest(mprInput.build(), getCookie());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<GetFlowStatisticsFromFlowTableOutput>(result) {
                    @Override
                    public GetFlowStatisticsFromFlowTableOutput createResult() {
                        GetFlowStatisticsFromFlowTableOutputBuilder flowStatsFromFlowTableBuilder =
                                new GetFlowStatisticsFromFlowTableOutputBuilder()
                                        .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return flowStatsFromFlowTableBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input get aggregate flow statistics from flow table for all flow input
     * @param cookie switch connection distinguisher cookie value
     * @return task get aggregate flow stats from flow table for all flow task
     */
    public static OFRpcTask<GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput, RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>>
    createGetAggregateFlowStatisticsFromFlowTableForAllFlowsTask(
            final OFRpcTaskContext taskContext,
            final GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput input, SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput, RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> call() throws Exception {
                final SettableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> result = SettableFuture.create();

                final Long xid = taskContext.getSession().getNextXid();

                // Create multipart request body for fetch all the group stats
                MultipartRequestAggregateCaseBuilder multipartRequestAggregateCaseBuilder = new MultipartRequestAggregateCaseBuilder();
                MultipartRequestAggregateBuilder mprAggregateRequestBuilder = new MultipartRequestAggregateBuilder();
                mprAggregateRequestBuilder.setTableId(input.getTableId().getValue());
                mprAggregateRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
                mprAggregateRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
                mprAggregateRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
                mprAggregateRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);

                FlowCreatorUtil.setWildcardedFlowMatch(taskContext.getSession()
                        .getPrimaryConductor().getVersion(), mprAggregateRequestBuilder);

                // Set request body to main multipart request
                multipartRequestAggregateCaseBuilder.setMultipartRequestAggregate(mprAggregateRequestBuilder.build());
                MultipartRequestInputBuilder mprInput =
                        createMultipartHeader(MultipartType.OFPMPAGGREGATE, taskContext, xid);
                mprInput.setMultipartRequestBody(multipartRequestAggregateCaseBuilder.build());
                Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                        .multipartRequest(mprInput.build(), getCookie());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>(result) {
                    @Override
                    public GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput createResult() {
                        GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutputBuilder flowStatsFromFlowTableBuilder =
                                new GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutputBuilder()
                                        .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return flowStatsFromFlowTableBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input aggregate flow statistics input
     * @param cookie switch connection distinguisher cookie value
     * @return task task to fetch the statistics
     */
    public static OFRpcTask<GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput, RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>>
    createGetAggregateFlowStatisticsFromFlowTableForGivenMatchTask(
            final OFRpcTaskContext taskContext,
            final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input, SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput, RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> call() throws Exception {
                final SettableFuture<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> result = SettableFuture.create();

                final Long xid = taskContext.getSession().getNextXid();

                MultipartRequestAggregateCaseBuilder multipartRequestAggregateCaseBuilder = new MultipartRequestAggregateCaseBuilder();
                MultipartRequestAggregateBuilder mprAggregateRequestBuilder = new MultipartRequestAggregateBuilder();
                mprAggregateRequestBuilder.setTableId(input.getTableId());
                mprAggregateRequestBuilder.setOutPort(input.getOutPort().longValue());
                // TODO: repeating code
                if (taskContext.getSession().getPrimaryConductor().getVersion() ==
                        OFConstants.OFP_VERSION_1_3) {
                    mprAggregateRequestBuilder.setCookie(input.getCookie().getValue());
                    mprAggregateRequestBuilder.setCookieMask(input.getCookieMask().getValue());
                    mprAggregateRequestBuilder.setOutGroup(input.getOutGroup());
                } else {
                    mprAggregateRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
                    mprAggregateRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
                    mprAggregateRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
                }

                MatchReactor.getInstance().convert(input.getMatch(), taskContext.getSession()
                                .getPrimaryConductor().getVersion(), mprAggregateRequestBuilder,
                        taskContext.getSession().getFeatures().getDatapathId());

                FlowCreatorUtil.setWildcardedFlowMatch(taskContext.getSession()
                        .getPrimaryConductor().getVersion(), mprAggregateRequestBuilder);

                // Set request body to main multipart request
                multipartRequestAggregateCaseBuilder.setMultipartRequestAggregate(mprAggregateRequestBuilder.build());
                MultipartRequestInputBuilder mprInput =
                        createMultipartHeader(MultipartType.OFPMPAGGREGATE, taskContext, xid);
                mprInput.setMultipartRequestBody(multipartRequestAggregateCaseBuilder.build());
                Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                        .multipartRequest(mprInput.build(), getCookie());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>(result) {
                    @Override
                    public GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput createResult() {
                        GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder aggregFlowStatsFromFlowTableBuilder =
                                new GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder()
                                        .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return aggregFlowStatsFromFlowTableBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input flow table statistics input
     * @param cookie switch connection distinguisher cookie value
     * @return task task to fetch table statistics
     */
    public static OFRpcTask<GetFlowTablesStatisticsInput, RpcResult<GetFlowTablesStatisticsOutput>> createGetFlowTablesStatisticsTask(
            final OFRpcTaskContext taskContext, final GetFlowTablesStatisticsInput input, SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<GetFlowTablesStatisticsInput, RpcResult<GetFlowTablesStatisticsOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetFlowTablesStatisticsInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetFlowTablesStatisticsOutput>> call() throws Exception {
                final SettableFuture<RpcResult<GetFlowTablesStatisticsOutput>> result = SettableFuture.create();

                final Long xid = taskContext.getSession().getNextXid();

                // Create multipart request body for fetch all the group stats
                MultipartRequestTableCaseBuilder multipartRequestTableCaseBuilder = new MultipartRequestTableCaseBuilder();
                MultipartRequestTableBuilder multipartRequestTableBuilder = new MultipartRequestTableBuilder();
                multipartRequestTableBuilder.setEmpty(true);
                multipartRequestTableCaseBuilder.setMultipartRequestTable(multipartRequestTableBuilder.build());

                // Set request body to main multipart request
                MultipartRequestInputBuilder mprInput =
                        createMultipartHeader(MultipartType.OFPMPTABLE, taskContext, xid);
                mprInput.setMultipartRequestBody(multipartRequestTableCaseBuilder.build());
                Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                        .multipartRequest(mprInput.build(), getCookie());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<GetFlowTablesStatisticsOutput>(result) {
                    @Override
                    public GetFlowTablesStatisticsOutput createResult() {
                        GetFlowTablesStatisticsOutputBuilder flowTableStatsBuilder =
                                new GetFlowTablesStatisticsOutputBuilder()
                                        .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return flowTableStatsBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input queue statistics input
     * @param cookie switch connection distinguisher cookie value
     * @return task task to fetch all queue statistics
     */
    public static OFRpcTask<GetAllQueuesStatisticsFromAllPortsInput, RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>> createGetAllQueuesStatisticsFromAllPortsTask(
            final OFRpcTaskContext taskContext, final GetAllQueuesStatisticsFromAllPortsInput input, SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<GetAllQueuesStatisticsFromAllPortsInput, RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetAllQueuesStatisticsFromAllPortsInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>> call() throws Exception {
                final SettableFuture<RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>> result = SettableFuture.create();

                final Long xid = taskContext.getSession().getNextXid();

                MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
                MultipartRequestQueueBuilder mprQueueBuilder = new MultipartRequestQueueBuilder();
                // Select all ports
                mprQueueBuilder.setPortNo(OFConstants.OFPP_ANY);
                // Select all the queues
                mprQueueBuilder.setQueueId(OFConstants.OFPQ_ALL);
                caseBuilder.setMultipartRequestQueue(mprQueueBuilder.build());

                // Set request body to main multipart request
                MultipartRequestInputBuilder mprInput =
                        createMultipartHeader(MultipartType.OFPMPQUEUE, taskContext, xid);
                mprInput.setMultipartRequestBody(caseBuilder.build());
                Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                        .multipartRequest(mprInput.build(), getCookie());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<GetAllQueuesStatisticsFromAllPortsOutput>(result) {
                    @Override
                    public GetAllQueuesStatisticsFromAllPortsOutput createResult() {
                        GetAllQueuesStatisticsFromAllPortsOutputBuilder allQueueStatsBuilder =
                                new GetAllQueuesStatisticsFromAllPortsOutputBuilder()
                                        .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return allQueueStatsBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input queue statist from specific port input
     * @param cookie switch connection distinguisher cookie value
     * @return task task to get queue statistics from specific port
     */
    public static OFRpcTask<GetAllQueuesStatisticsFromGivenPortInput, RpcResult<GetAllQueuesStatisticsFromGivenPortOutput>> createGetAllQueuesStatisticsFromGivenPortTask(
            final OFRpcTaskContext taskContext, final GetAllQueuesStatisticsFromGivenPortInput input, SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<GetAllQueuesStatisticsFromGivenPortInput, RpcResult<GetAllQueuesStatisticsFromGivenPortOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetAllQueuesStatisticsFromGivenPortInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetAllQueuesStatisticsFromGivenPortOutput>> call() throws Exception {
                final SettableFuture<RpcResult<GetAllQueuesStatisticsFromGivenPortOutput>> result = SettableFuture.create();

                final Long xid = taskContext.getSession().getNextXid();

                MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
                MultipartRequestQueueBuilder mprQueueBuilder = new MultipartRequestQueueBuilder();
                // Select all queues
                mprQueueBuilder.setQueueId(OFConstants.OFPQ_ALL);
                // Select specific port
                mprQueueBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                        OpenflowVersion.get(taskContext.getSession().getFeatures().getVersion()),
                        input.getNodeConnectorId()));
                caseBuilder.setMultipartRequestQueue(mprQueueBuilder.build());

                // Set request body to main multipart request
                MultipartRequestInputBuilder mprInput =
                        createMultipartHeader(MultipartType.OFPMPQUEUE, taskContext, xid);
                mprInput.setMultipartRequestBody(caseBuilder.build());
                Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                        .multipartRequest(mprInput.build(), getCookie());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<GetAllQueuesStatisticsFromGivenPortOutput>(result) {
                    @Override
                    public GetAllQueuesStatisticsFromGivenPortOutput createResult() {
                        GetAllQueuesStatisticsFromGivenPortOutputBuilder allQueueStatsBuilder =
                                new GetAllQueuesStatisticsFromGivenPortOutputBuilder()
                                        .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return allQueueStatsBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input queue statistics from given port
     * @param cookie switch connection distinguisher cookie value
     * @return task task to get queue statistics from given port
     */
    public static OFRpcTask<GetQueueStatisticsFromGivenPortInput, RpcResult<GetQueueStatisticsFromGivenPortOutput>> createGetQueueStatisticsFromGivenPortTask(
            final OFRpcTaskContext taskContext, final GetQueueStatisticsFromGivenPortInput input, SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<GetQueueStatisticsFromGivenPortInput, RpcResult<GetQueueStatisticsFromGivenPortOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    GetQueueStatisticsFromGivenPortInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<GetQueueStatisticsFromGivenPortOutput>> call() throws Exception {
                final SettableFuture<RpcResult<GetQueueStatisticsFromGivenPortOutput>> result = SettableFuture.create();

                final Long xid = taskContext.getSession().getNextXid();

                MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
                MultipartRequestQueueBuilder mprQueueBuilder = new MultipartRequestQueueBuilder();
                // Select specific queue
                mprQueueBuilder.setQueueId(input.getQueueId().getValue());
                // Select specific port
                mprQueueBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                        OpenflowVersion.get(taskContext.getSession().getFeatures().getVersion()),
                        input.getNodeConnectorId()));
                caseBuilder.setMultipartRequestQueue(mprQueueBuilder.build());

                // Set request body to main multipart request
                MultipartRequestInputBuilder mprInput =
                        createMultipartHeader(MultipartType.OFPMPQUEUE, taskContext, xid);
                mprInput.setMultipartRequestBody(caseBuilder.build());
                Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                        .multipartRequest(mprInput.build(), getCookie());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<GetQueueStatisticsFromGivenPortOutput>(result) {
                    @Override
                    public GetQueueStatisticsFromGivenPortOutput createResult() {
                        GetQueueStatisticsFromGivenPortOutputBuilder queueStatsFromPortBuilder =
                                new GetQueueStatisticsFromGivenPortOutputBuilder()
                                        .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return queueStatsFromPortBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    static MultipartRequestInputBuilder createMultipartHeader(MultipartType multipart,
                                                              OFRpcTaskContext taskContext, Long xid) {
        MultipartRequestInputBuilder mprInput = new MultipartRequestInputBuilder();
        mprInput.setType(multipart);
        mprInput.setVersion(taskContext.getSession().getPrimaryConductor().getVersion());
        mprInput.setXid(xid);
        mprInput.setFlags(new MultipartRequestFlags(false));
        return mprInput;
    }

    private abstract static class ResultCallback<T> implements FutureCallback<RpcResult<Void>> {

        private SettableFuture<RpcResult<T>> result;

        /**
         * @param result result
         */
        public ResultCallback(SettableFuture<RpcResult<T>> result) {
            this.result = result;
        }

        public abstract T createResult();

        @Override
        public void onSuccess(RpcResult<Void> resultArg) {
            result.set(RpcResultBuilder.success(createResult()).build());
        }

        @Override
        public void onFailure(Throwable t) {
            result.set(RpcResultBuilder.<T>failed().withWarning(
                    ErrorType.RPC,
                    OFConstants.ERROR_TAG_TIMEOUT,
                    "something wrong happened",
                    OFConstants.APPLICATION_TAG,
                    "", t).build());
        }
    }

    /**
     * @param taskContext task context
     * @param input update port input
     * @param cookie switch connection distinguisher cookie value
     * @return task task to update port
     */
    public static OFRpcTask<UpdatePortInput, RpcResult<UpdatePortOutput>> createUpdatePortTask(
            final OFRpcTaskContext taskContext, final UpdatePortInput input,
            final SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<UpdatePortInput, RpcResult<UpdatePortOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    UpdatePortInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<UpdatePortOutput>> call() {
                ListenableFuture<RpcResult<UpdatePortOutput>> result = SettableFuture.create();
                final Long xid = taskContext.getSession().getNextXid();
                Port inputPort = input.getUpdatedPort().getPort().getPort().get(0);

                PortModInput ofPortModInput = PortConvertor.toPortModInput(inputPort,
                        taskContext.getSession().getPrimaryConductor().getVersion());

                PortModInputBuilder mdInput = new PortModInputBuilder(ofPortModInput);
                mdInput.setXid(xid);

                Future<RpcResult<UpdatePortOutput>> resultFromOFLib = getMessageService()
                        .portMod(mdInput.build(), cookie);
                result = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    /**
     * @param taskContext task context
     * @param input update table input
     * @param cookie switch connection distinguisher cookie value
     * @return task task to udpate table input
     */
    public static OFRpcTask<UpdateTableInput, RpcResult<UpdateTableOutput>> createUpdateTableTask(
            final OFRpcTaskContext taskContext, final UpdateTableInput input,
            final SwitchConnectionDistinguisher cookie) {

        class OFRpcTaskImpl extends OFRpcTask<UpdateTableInput, RpcResult<UpdateTableOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    UpdateTableInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<UpdateTableOutput>> call() {
                final SettableFuture<RpcResult<UpdateTableOutput>> result = SettableFuture.create();

                final Long xid = taskContext.getSession().getNextXid();

                MultipartRequestTableFeaturesCaseBuilder caseBuilder = new MultipartRequestTableFeaturesCaseBuilder();
                MultipartRequestTableFeaturesBuilder requestBuilder = new MultipartRequestTableFeaturesBuilder();
                List<TableFeatures> ofTableFeatureList = TableFeaturesConvertor
                        .toTableFeaturesRequest(input.getUpdatedTable());
                requestBuilder.setTableFeatures(ofTableFeatureList);
                caseBuilder.setMultipartRequestTableFeatures(requestBuilder.build());

                // Set request body to main multipart request
                MultipartRequestInputBuilder mprInput =
                        createMultipartHeader(MultipartType.OFPMPTABLEFEATURES, taskContext, xid);
                mprInput.setMultipartRequestBody(caseBuilder.build());

                Future<RpcResult<Void>> resultFromOFLib = getMessageService()
                        .multipartRequest(mprInput.build(), getCookie());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(resultFromOFLib);

                Futures.addCallback(resultLib, new ResultCallback<UpdateTableOutput>(result) {
                    @Override
                    public UpdateTableOutput createResult() {
                        UpdateTableOutputBuilder queueStatsFromPortBuilder =
                                new UpdateTableOutputBuilder()
                                        .setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return queueStatsFromPortBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }

    public static OFRpcTask<SetConfigInput, RpcResult<SetConfigOutput>> createSetNodeConfigTask(final OFRpcTaskContext taskContext,
                                                                                                final SetConfigInput input,
                                                                                                final SwitchConnectionDistinguisher cookie) {
        class OFRpcTaskImpl extends OFRpcTask<SetConfigInput, RpcResult<SetConfigOutput>> {

            public OFRpcTaskImpl(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie,
                    SetConfigInput input) {
                super(taskContext, cookie, input);
            }

            @Override
            public ListenableFuture<RpcResult<SetConfigOutput>> call() throws Exception {

                final SettableFuture<RpcResult<SetConfigOutput>> result = SettableFuture.create();
                final Long xid = taskContext.getSession().getNextXid();

                SetConfigInputBuilder builder = new SetConfigInputBuilder();
                SwitchConfigFlag flag = SwitchConfigFlag.valueOf(input.getFlag());
                builder.setXid(xid);
                builder.setFlags(flag);
                builder.setMissSendLen(input.getMissSearchLength());
                builder.setVersion(getVersion());
                ListenableFuture<RpcResult<Void>> resultLib = JdkFutureAdapters.listenInPoolThread(taskContext.getSession().getPrimaryConductor().getConnectionAdapter().setConfig(builder.build()));
                Futures.addCallback(resultLib, new ResultCallback<SetConfigOutput>(result) {
                    @Override
                    public SetConfigOutput createResult() {
                        SetConfigOutputBuilder setConfigOutputBuilder = new SetConfigOutputBuilder();
                        setConfigOutputBuilder.setTransactionId(new TransactionId(BigInteger.valueOf(xid)));
                        return setConfigOutputBuilder.build();
                    }
                });
                return result;
            }
        }

        return new OFRpcTaskImpl(taskContext, cookie, input);
    }
}
