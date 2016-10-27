/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowHashIdMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.PortKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.meter.update.UpdatedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.port.update.UpdatedPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TablePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeaturePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeaturePropertiesKey;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * simple NPE smoke test
 */
@RunWith(MockitoJUnitRunner.class)
public class ModelDrivenSwitchImplTest {

    private ModelDrivenSwitchImpl mdSwitchOF10;
    private ModelDrivenSwitchImpl mdSwitchOF13;

    @Mock
    private SessionContext context;
    @Mock
    private ConnectionConductor conductor;
    @Mock
    private IMessageDispatchService messageDispatchService;
    @Mock
    private GetFeaturesOutput features;
    @Mock
    private MessageSpy<DataContainer> messageSpy;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private ReadWriteTransaction rwTx;
    @Mock
    private EntityOwnershipService entityOwnershipService;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Mockito.when(context.getPrimaryConductor()).thenReturn(conductor);
        Mockito.when(context.getMessageDispatchService()).thenReturn(messageDispatchService);
        Mockito.when(conductor.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_0)
                .thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.when(context.getFeatures()).thenReturn(features);
        Mockito.when(features.getDatapathId()).thenReturn(BigInteger.valueOf(1));


        OFSessionUtil.getSessionManager().setRpcPool(MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10)));
        OFSessionUtil.getSessionManager().setMessageSpy(messageSpy);
        OFSessionUtil.getSessionManager().setDataBroker(dataBroker);

        CheckedFuture<Optional<FlowHashIdMapping>, ReadFailedException> dummyReadFuture
            = Futures.<Optional<FlowHashIdMapping>,ReadFailedException>immediateCheckedFuture(Optional.<FlowHashIdMapping>absent());
        Mockito.when(rwTx.read(Matchers.<LogicalDatastoreType>any(), Matchers.<InstanceIdentifier<FlowHashIdMapping>>any())).thenReturn(dummyReadFuture);
        Mockito.when(dataBroker.newReadWriteTransaction()).thenReturn(rwTx);

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        mdSwitchOF10 = new ModelDrivenSwitchImpl(null, null, context, convertorManager);
        mdSwitchOF13 = new ModelDrivenSwitchImpl(null, null, context, convertorManager);
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#addFlow(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testAddFlow() throws InterruptedException, ExecutionException {
        UpdateFlowOutputBuilder updateFlowOutput = new UpdateFlowOutputBuilder();
        updateFlowOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<UpdateFlowOutput> result = RpcResultBuilder.success(updateFlowOutput.build()).build();
        Mockito.when(
                messageDispatchService.flowMod(Matchers.any(FlowModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        AddFlowInputBuilder input = new AddFlowInputBuilder();
        input.setMatch(createMatch());

        Mockito.when(features.getVersion()).thenReturn((short)1);
        mdSwitchOF10.addFlow(input.build()).get();
        Mockito.when(features.getVersion()).thenReturn((short)4);
        mdSwitchOF13.addFlow(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).flowMod(
                Matchers.any(FlowModInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#removeFlow(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testRemoveFlow() throws InterruptedException, ExecutionException {
        UpdateFlowOutputBuilder updateFlowOutput = new UpdateFlowOutputBuilder();
        updateFlowOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<UpdateFlowOutput> result = RpcResultBuilder.success(updateFlowOutput.build()).build();
        Mockito.when(
                messageDispatchService.flowMod(Matchers.any(FlowModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        RemoveFlowInputBuilder input = new RemoveFlowInputBuilder();
        input.setMatch(createMatch());

        Mockito.when(features.getVersion()).thenReturn((short)1);
        mdSwitchOF10.removeFlow(input.build()).get();
        Mockito.when(features.getVersion()).thenReturn((short)4);
        mdSwitchOF13.removeFlow(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).flowMod(
                Matchers.any(FlowModInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));

    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#updateFlow(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testUpdateFlow() throws InterruptedException, ExecutionException {
        UpdateFlowOutputBuilder updateFlowOutput = new UpdateFlowOutputBuilder();
        updateFlowOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<UpdateFlowOutput> result = RpcResultBuilder.success(updateFlowOutput.build()).build();
        Mockito.when(
                messageDispatchService.flowMod(Matchers.any(FlowModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        UpdateFlowInputBuilder input = new UpdateFlowInputBuilder();
        UpdatedFlowBuilder updatedFlow = new UpdatedFlowBuilder();
        updatedFlow.setBarrier(false);
        updatedFlow.setMatch(createMatch());
        updatedFlow.setPriority(65);
        updatedFlow.setFlags(new FlowModFlags(true, false, true, false, true));
        input.setUpdatedFlow(updatedFlow.build());
        OriginalFlowBuilder originalFlowBuilder = new OriginalFlowBuilder();
        originalFlowBuilder.setMatch(createMatch());
        originalFlowBuilder.setPriority(65);
        originalFlowBuilder.setFlags(new FlowModFlags(true, false, true, false, true));
        input.setOriginalFlow(originalFlowBuilder.build());
        KeyedInstanceIdentifier<Flow, FlowKey> dummyIdentifier = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("openflow:1")))
            .augmentation(FlowCapableNode.class)
            .child(Table.class, new TableKey((short)0))
            .child(Flow.class, new FlowKey(new FlowId("1")));
        input.setFlowRef(new FlowRef(dummyIdentifier));

        Mockito.when(features.getVersion()).thenReturn((short)1);
        mdSwitchOF10.updateFlow(input.build()).get();
        Mockito.when(features.getVersion()).thenReturn((short)4);
        mdSwitchOF13.updateFlow(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).flowMod(
                Matchers.any(FlowModInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * addGroup(org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.
     * AddGroupInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testAddGroup() throws InterruptedException, ExecutionException {
        UpdateGroupOutputBuilder updateGroupOutput = new UpdateGroupOutputBuilder();
        updateGroupOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<UpdateGroupOutput> result = RpcResultBuilder.success(updateGroupOutput.build()).build();
        Mockito.when(
                messageDispatchService.groupMod(Matchers.any(GroupModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        AddGroupInputBuilder input = new AddGroupInputBuilder();
        input.setGroupType(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes.GroupFf);
        input.setGroupId(new GroupId(789L));

        mdSwitchOF10.addGroup(input.build()).get();
        mdSwitchOF13.addGroup(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).groupMod(
                Matchers.any(GroupModInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * updateGroup(org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.
     * UpdateGroupInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testUpdateGroup() throws InterruptedException, ExecutionException {
        UpdateGroupOutputBuilder updateGroupOutput = new UpdateGroupOutputBuilder();
        updateGroupOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<UpdateGroupOutput> result = RpcResultBuilder.success(updateGroupOutput.build()).build();
        Mockito.when(
                messageDispatchService.groupMod(Matchers.any(GroupModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        UpdateGroupInputBuilder input = new UpdateGroupInputBuilder();
        UpdatedGroupBuilder updatedGroupBuilder = new UpdatedGroupBuilder();
        updatedGroupBuilder.setGroupId(new GroupId(789L));
        updatedGroupBuilder.setGroupType(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes.GroupFf);
        input.setUpdatedGroup(updatedGroupBuilder.build());

        mdSwitchOF10.updateGroup(input.build()).get();
        mdSwitchOF13.updateGroup(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).groupMod(
                Matchers.any(GroupModInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * removeGroup(org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.
     * RemoveGroupInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testRemoveGroup() throws InterruptedException, ExecutionException {
        UpdateGroupOutputBuilder updateGroupOutput = new UpdateGroupOutputBuilder();
        updateGroupOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<UpdateGroupOutput> result = RpcResultBuilder.success(updateGroupOutput.build()).build();
        Mockito.when(
                messageDispatchService.groupMod(Matchers.any(GroupModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        RemoveGroupInputBuilder input = new RemoveGroupInputBuilder();
        input.setGroupId(new GroupId(789L));
        input.setGroupType(org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes.GroupFf);

        mdSwitchOF10.removeGroup(input.build()).get();
        mdSwitchOF13.removeGroup(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).groupMod(
                Matchers.any(GroupModInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * addMeter(org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.
     * AddMeterInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testAddMeter() throws InterruptedException, ExecutionException {
        UpdateMeterOutputBuilder updateMeterOutput = new UpdateMeterOutputBuilder();
        updateMeterOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<UpdateMeterOutput> result = RpcResultBuilder.success(updateMeterOutput.build()).build();
        Mockito.when(
                messageDispatchService.meterMod(Matchers.any(MeterModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        AddMeterInputBuilder input = new AddMeterInputBuilder();
        input.setMeterId(new MeterId(78L));

        mdSwitchOF10.addMeter(input.build()).get();
        mdSwitchOF13.addMeter(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).meterMod(
                Matchers.any(MeterModInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * updateMeter(org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.
     * UpdateMeterInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testUpdtateMeter() throws InterruptedException, ExecutionException {
        UpdateMeterOutputBuilder updateMeterOutput = new UpdateMeterOutputBuilder();
        updateMeterOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<UpdateMeterOutput> result = RpcResultBuilder.success(updateMeterOutput.build()).build();
        Mockito.when(
                messageDispatchService.meterMod(Matchers.any(MeterModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        UpdateMeterInputBuilder input = new UpdateMeterInputBuilder();
        UpdatedMeterBuilder updatedMeterBuilder = new UpdatedMeterBuilder();
        updatedMeterBuilder.setMeterId(new MeterId(89L));
        updatedMeterBuilder.setBarrier(false);
        input.setUpdatedMeter(updatedMeterBuilder.build());

        mdSwitchOF10.updateMeter(input.build()).get();
        mdSwitchOF13.updateMeter(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).meterMod(
                Matchers.any(MeterModInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * removeMeter(org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.
     * RemoveMeterInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testRemoveMeter() throws InterruptedException, ExecutionException {
        UpdateMeterOutputBuilder updateMeterOutput = new UpdateMeterOutputBuilder();
        updateMeterOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<UpdateMeterOutput> result = RpcResultBuilder.success(updateMeterOutput.build()).build();
        Mockito.when(
                messageDispatchService.meterMod(Matchers.any(MeterModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        RemoveMeterInputBuilder input = new RemoveMeterInputBuilder();
        input.setMeterId(new MeterId(89L));

        mdSwitchOF10.removeMeter(input.build()).get();
        mdSwitchOF13.removeMeter(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).meterMod(
                Matchers.any(MeterModInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getAllGroupStatistics(org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.
     * GetAllGroupStatisticsInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetAllGroupStatistics() throws InterruptedException, ExecutionException {
        GetAllGroupStatisticsOutputBuilder getAllGroupStatistcsOutput = new GetAllGroupStatisticsOutputBuilder();
        getAllGroupStatistcsOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetAllGroupStatisticsInputBuilder input = new GetAllGroupStatisticsInputBuilder();

        mdSwitchOF10.getAllGroupStatistics(input.build()).get();
        mdSwitchOF13.getAllGroupStatistics(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getGroupDescription(org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.
     * GetGroupDescriptionInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetGroupDescription() throws InterruptedException, ExecutionException {
        GetGroupDescriptionOutputBuilder getGroupDescOutput = new GetGroupDescriptionOutputBuilder();
        getGroupDescOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetGroupDescriptionInputBuilder input = new GetGroupDescriptionInputBuilder();

        mdSwitchOF10.getGroupDescription(input.build()).get();
        mdSwitchOF13.getGroupDescription(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getGroupFeatures(org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.
     * GetGroupFeaturesInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetGroupFeatures() throws InterruptedException, ExecutionException {
        GetGroupFeaturesOutputBuilder getGroupFeaturesOutput = new GetGroupFeaturesOutputBuilder();
        getGroupFeaturesOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetGroupFeaturesInputBuilder input = new GetGroupFeaturesInputBuilder();

        mdSwitchOF10.getGroupFeatures(input.build()).get();
        mdSwitchOF13.getGroupFeatures(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getGroupStatistics(org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.
     * GetGroupStatisticsInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    //TODO GetGroupStatistics why NPE?
    @Test
    public void testGetGroupStatistics() throws InterruptedException, ExecutionException {
        GetGroupStatisticsOutputBuilder getGroupStatsOutput = new GetGroupStatisticsOutputBuilder();
        getGroupStatsOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetGroupStatisticsInputBuilder input = new GetGroupStatisticsInputBuilder();
        input.setGroupId(new GroupId(42L));

        mdSwitchOF10.getGroupStatistics(input.build()).get();
        mdSwitchOF13.getGroupStatistics(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getAllMeterConfigStatistics(org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.
     * GetAllMeterConfigStatisticsInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetAllMeterConfigStatistics() throws InterruptedException, ExecutionException {
        GetAllMeterConfigStatisticsOutputBuilder getAllMeterConfigStatsOutput =
                new GetAllMeterConfigStatisticsOutputBuilder();
        getAllMeterConfigStatsOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetAllMeterConfigStatisticsInputBuilder input = new GetAllMeterConfigStatisticsInputBuilder();

        mdSwitchOF10.getAllMeterConfigStatistics(input.build()).get();
        mdSwitchOF13.getAllMeterConfigStatistics(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getAllMeterStatistics(org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.
     * GetAllMeterStatisticsInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetAllMeterStatistics() throws InterruptedException, ExecutionException {
        GetAllMeterStatisticsOutputBuilder getAllMeterStatisticsOutput =
                new GetAllMeterStatisticsOutputBuilder();
        getAllMeterStatisticsOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetAllMeterStatisticsInputBuilder input = new GetAllMeterStatisticsInputBuilder();

        mdSwitchOF10.getAllMeterStatistics(input.build()).get();
        mdSwitchOF13.getAllMeterStatistics(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getMeterFeatures(org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.
     * GetMeterFeaturesInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetMeterFeatures() throws InterruptedException, ExecutionException {
        GetMeterFeaturesOutputBuilder getMeterFeaturesOutput =
                new GetMeterFeaturesOutputBuilder();
        getMeterFeaturesOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetMeterFeaturesInputBuilder input = new GetMeterFeaturesInputBuilder();

        mdSwitchOF10.getMeterFeatures(input.build()).get();
        mdSwitchOF13.getMeterFeatures(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getMeterStatistics(org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.
     * GetMeterStatisticsInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetMeterStatistics() throws InterruptedException, ExecutionException {
        GetMeterStatisticsOutputBuilder getMeterStatsOutput = new GetMeterStatisticsOutputBuilder();
        getMeterStatsOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetMeterStatisticsInputBuilder input = new GetMeterStatisticsInputBuilder();
        input.setMeterId(new MeterId(42L));

        mdSwitchOF10.getMeterStatistics(input.build()).get();
        mdSwitchOF13.getMeterStatistics(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getAllNodeConnectorsStatistics(org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.
     * GetAllNodeConnectorsStatisticsInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetAllNodeConnectorsStatistics() throws InterruptedException, ExecutionException {
        GetAllNodeConnectorsStatisticsOutputBuilder getAllNodeConnectorsStatsOutput =
                new GetAllNodeConnectorsStatisticsOutputBuilder();
        getAllNodeConnectorsStatsOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();;
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetAllNodeConnectorsStatisticsInputBuilder input = new GetAllNodeConnectorsStatisticsInputBuilder();

        mdSwitchOF10.getAllNodeConnectorsStatistics(input.build()).get();
        mdSwitchOF13.getAllNodeConnectorsStatistics(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getNodeConnectorStatistics(org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.
     * GetNodeConnectorStatisticsInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetNodeConnectorStatistics() throws InterruptedException, ExecutionException {
        GetNodeConnectorStatisticsOutputBuilder getNodeConnectorStatsOutput =
                new GetNodeConnectorStatisticsOutputBuilder();
        getNodeConnectorStatsOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetNodeConnectorStatisticsInputBuilder input = new GetNodeConnectorStatisticsInputBuilder();
        input.setNodeConnectorId(new NodeConnectorId("openflow:12:8"));

        Mockito.when(features.getVersion()).thenReturn((short)1);
        mdSwitchOF10.getNodeConnectorStatistics(input.build()).get();
        Mockito.when(features.getVersion()).thenReturn((short)4);
        mdSwitchOF13.getNodeConnectorStatistics(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * updatePort(org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.
     * UpdatePortInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testUpdatePort() throws InterruptedException, ExecutionException {
        UpdatePortOutputBuilder updatePortOutput = new UpdatePortOutputBuilder();
        updatePortOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<UpdatePortOutput> result = RpcResultBuilder.success(updatePortOutput.build()).build();
        Mockito.when(
                messageDispatchService.portMod(Matchers.any(PortModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        UpdatePortInputBuilder input = new UpdatePortInputBuilder();

        PortBuilder portBuilder = new PortBuilder();
        List<Port> ports = new ArrayList<Port>();
        ports.add(createPort());
        portBuilder.setPort(ports);
        UpdatedPortBuilder updatedPortBuilder = new UpdatedPortBuilder();
        updatedPortBuilder.setPort(portBuilder.build());
        input.setUpdatedPort(updatedPortBuilder.build());

        mdSwitchOF10.updatePort(input.build()).get();
        mdSwitchOF13.updatePort(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).portMod(
                Matchers.any(PortModInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    private static Port createPort() {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.PortBuilder port =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.PortBuilder();

        port.setPortName("TestingPort01");
        port.setMask(new PortConfig(true, true, true, true));
        port.setConfiguration(new PortConfig(true, true, true, true));
        port.setAdvertisedFeatures(new PortFeatures(true, true, true, true,
                                                    false, false, false, false,
                                                    true, true, true, true,
                                                    false, false, false, false));
        port.setPortNumber(new PortNumberUni(42L));
        port.setHardwareAddress(new MacAddress("01:23:45:67:89:ab"));
        port.setBarrier(true);
        port.setContainerName("TestContainer");
        port.setPortModOrder(25L);
        port.setKey(new PortKey(25L));
        return port.build();
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * updateTable(org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.
     * UpdateTableInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testUpdateTable() throws InterruptedException, ExecutionException {
        UpdateTableOutputBuilder updateTableOutput = new UpdateTableOutputBuilder();
        updateTableOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        UpdateTableInputBuilder input = new UpdateTableInputBuilder();
        input.setUpdatedTable(createUpdateTable());

        mdSwitchOF10.updateTable(input.build()).get();
        mdSwitchOF13.updateTable(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    private static UpdatedTable createUpdateTable() {
        UpdatedTableBuilder updatedTableBuilder = new UpdatedTableBuilder();
        TableFeaturesBuilder tableFeaturesBuilder = new TableFeaturesBuilder();
        tableFeaturesBuilder.setConfig(new TableConfig(true));
        tableFeaturesBuilder.setKey(new TableFeaturesKey((short) 42));
        tableFeaturesBuilder.setMaxEntries(42L);
        tableFeaturesBuilder.setMetadataMatch(BigInteger.valueOf(42424242));
        tableFeaturesBuilder.setMetadataWrite(BigInteger.valueOf(42424242));
        tableFeaturesBuilder.setName("testTableFeatures");
        tableFeaturesBuilder.setTableId((short) 41);

        TablePropertiesBuilder tablePropertiesBuilder = new TablePropertiesBuilder();
        TableFeaturePropertiesBuilder tableFeaturePropertiesBuilder = new TableFeaturePropertiesBuilder();
        tableFeaturePropertiesBuilder.setKey(new TableFeaturePropertiesKey(45));
        tableFeaturePropertiesBuilder.setOrder(44);
        tableFeaturePropertiesBuilder.setTableFeaturePropType(new ApplySetfieldBuilder().build());
        List<TableFeatureProperties> tableFeatureProperties = new ArrayList<TableFeatureProperties>();
        tableFeatureProperties.add(tableFeaturePropertiesBuilder.build());
        tablePropertiesBuilder.setTableFeatureProperties(tableFeatureProperties);

        tableFeaturesBuilder.setTableProperties(tablePropertiesBuilder.build());
        List<TableFeatures> tableFeatures = new ArrayList<TableFeatures>();
        tableFeatures.add(tableFeaturesBuilder.build());
        updatedTableBuilder.setTableFeatures(tableFeatures);
        return updatedTableBuilder.build();
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getAllFlowStatisticsFromFlowTable(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.
     * GetAllFlowStatisticsFromFlowTableInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetAllFlowStatisticsFromFlowTable() throws InterruptedException, ExecutionException {
        GetAllFlowStatisticsFromFlowTableOutputBuilder allFlowStatisticsFromFlowTableOutput =
                new GetAllFlowStatisticsFromFlowTableOutputBuilder();
        allFlowStatisticsFromFlowTableOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetAllFlowStatisticsFromFlowTableInputBuilder input = new GetAllFlowStatisticsFromFlowTableInputBuilder();
        input.setTableId(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId((short) 42));

        mdSwitchOF10.getAllFlowStatisticsFromFlowTable(input.build()).get();
        mdSwitchOF13.getAllFlowStatisticsFromFlowTable(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getAllFlowStatisticsFromFlowTable(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.
     * GetAllFlowStatisticsFromFlowTableInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetAllFlowsStatisticsFromAllFlowTables() throws InterruptedException, ExecutionException {
        GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder allFlowStatisticsFromAllFlowTablesOutput =
                new GetAllFlowsStatisticsFromAllFlowTablesOutputBuilder();
        allFlowStatisticsFromAllFlowTablesOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetAllFlowsStatisticsFromAllFlowTablesInputBuilder input =
                                    new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder();

        mdSwitchOF10.getAllFlowsStatisticsFromAllFlowTables(input.build()).get();
        mdSwitchOF13.getAllFlowsStatisticsFromAllFlowTables(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getAllFlowStatisticsFromFlowTable(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.
     * GetAllFlowStatisticsFromFlowTableInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetFlowStatisticsFromFlowTables() throws InterruptedException, ExecutionException {
        GetFlowStatisticsFromFlowTableOutputBuilder flowStatisticsFromFlowTablesOutput =
                new GetFlowStatisticsFromFlowTableOutputBuilder();
        flowStatisticsFromFlowTablesOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetFlowStatisticsFromFlowTableInputBuilder input =
                                    new GetFlowStatisticsFromFlowTableInputBuilder();
        input.setMatch(createMatch());

        mdSwitchOF10.getFlowStatisticsFromFlowTable(input.build()).get();
        mdSwitchOF13.getFlowStatisticsFromFlowTable(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    private static Match createMatch() {
        MatchBuilder matchBuilder = new MatchBuilder();
        EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
        EthernetDestinationBuilder ethernetDestinationBuilder = new EthernetDestinationBuilder();
        ethernetDestinationBuilder.setAddress(new MacAddress("01:23:45:67:89:ab"));
        ethernetDestinationBuilder.setMask(new MacAddress("01:23:45:67:89:ab"));
        ethernetMatchBuilder.setEthernetDestination(ethernetDestinationBuilder.build());
        EthernetSourceBuilder ethernetSourceBuilder = new EthernetSourceBuilder();
        ethernetSourceBuilder.setAddress(new MacAddress("01:23:45:67:89:ab"));
        ethernetSourceBuilder.setMask(new MacAddress("01:23:45:67:89:ab"));
        ethernetMatchBuilder.setEthernetSource(ethernetSourceBuilder.build());
        ethernetMatchBuilder.setEthernetType(new EthernetTypeBuilder().setType(new EtherType(42L)).build());
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());
        return matchBuilder.build();
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getAggregateFlowStatisticsFromFlowTableForAllFlows(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.
     * GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetAggregateFlowStatisticsFromFlowTableForAllFlows() throws InterruptedException,
                                                                                ExecutionException {
        GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutputBuilder aggregateFlowStatisticsOutput =
                new GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutputBuilder();
        aggregateFlowStatisticsOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder input =
                           new GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder();
        input.setTableId(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId((short) 42));

        mdSwitchOF10.getAggregateFlowStatisticsFromFlowTableForAllFlows(input.build()).get();
        mdSwitchOF13.getAggregateFlowStatisticsFromFlowTableForAllFlows(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getAggregateFlowStatisticsFromFlowTableForGivenMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.
     * GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetAggregateFlowStatisticsFromFlowTableForGivenMatch() throws InterruptedException,
                                                                                ExecutionException {
        GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder aggregateFlowStatisticsForMatchOutput =
                new GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder();
        aggregateFlowStatisticsForMatchOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder input =
                           new GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder();
        input.setMatch(createMatch());
        input.setCookie(new FlowCookie(BigInteger.valueOf(123456)));
        input.setCookieMask(new FlowCookie(BigInteger.valueOf(123456)));
        input.setOutGroup(44L);
        input.setOutPort(BigInteger.valueOf(12563));

        mdSwitchOF10.getAggregateFlowStatisticsFromFlowTableForGivenMatch(input.build()).get();
        mdSwitchOF13.getAggregateFlowStatisticsFromFlowTableForGivenMatch(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getFlowTablesStatistics(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.
     * GetFlowTablesStatisticsInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetFlowTablesStatistics() throws InterruptedException, ExecutionException {
        GetFlowTablesStatisticsOutputBuilder flowTableStatisticsOutput =
                new GetFlowTablesStatisticsOutputBuilder();
        flowTableStatisticsOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetFlowTablesStatisticsInputBuilder input = new GetFlowTablesStatisticsInputBuilder();

        mdSwitchOF10.getFlowTablesStatistics(input.build()).get();
        mdSwitchOF13.getFlowTablesStatistics(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getAllQueuesStatisticsFromAllPorts(org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.
     * GetAllQueuesStatisticsFromAllPortsInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetAllQueuesStatisticsFromAllPorts() throws InterruptedException, ExecutionException {
        GetAllQueuesStatisticsFromAllPortsOutputBuilder allQueuesStatisticsAllPortsOutput =
                new GetAllQueuesStatisticsFromAllPortsOutputBuilder();
        allQueuesStatisticsAllPortsOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetAllQueuesStatisticsFromAllPortsInputBuilder input =
                new GetAllQueuesStatisticsFromAllPortsInputBuilder();

        mdSwitchOF10.getAllQueuesStatisticsFromAllPorts(input.build()).get();
        mdSwitchOF13.getAllQueuesStatisticsFromAllPorts(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getAllQueuesStatisticsFromGivenPort(org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.
     * GetAllQueuesStatisticsFromGivenPortInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetAllQueuesStatisticsFromGivenPort() throws InterruptedException, ExecutionException {
        GetAllQueuesStatisticsFromGivenPortOutputBuilder allQueuesStatisticsGivenPortsOutput =
                new GetAllQueuesStatisticsFromGivenPortOutputBuilder();
        allQueuesStatisticsGivenPortsOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetAllQueuesStatisticsFromGivenPortInputBuilder input =
                new GetAllQueuesStatisticsFromGivenPortInputBuilder();
        input.setNodeConnectorId(new NodeConnectorId("openflow:12:8"));

        Mockito.when(features.getVersion()).thenReturn((short)1);
        mdSwitchOF10.getAllQueuesStatisticsFromGivenPort(input.build()).get();
        Mockito.when(features.getVersion()).thenReturn((short)4);
        mdSwitchOF13.getAllQueuesStatisticsFromGivenPort(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * getQueueStatisticsFromGivenPort(org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.
     * GetQueueStatisticsFromGivenPortInput)}
     * .
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testGetQueueStatisticsFromGivenPort() throws InterruptedException, ExecutionException {
        GetQueueStatisticsFromGivenPortOutputBuilder queuesStatisticsGivenPortOutput =
                new GetQueueStatisticsFromGivenPortOutputBuilder();
        queuesStatisticsGivenPortOutput.setTransactionId(new TransactionId(BigInteger.valueOf(42)));
        RpcResult<Void> result = RpcResultBuilder.success((Void)null).build();
        Mockito.when(
                messageDispatchService.multipartRequest(Matchers.any(MultipartRequestInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        GetQueueStatisticsFromGivenPortInputBuilder input =
                new GetQueueStatisticsFromGivenPortInputBuilder();
        input.setNodeConnectorId(new NodeConnectorId("openflow:12:8"));
        input.setQueueId(new QueueId(55L));

        Mockito.when(features.getVersion()).thenReturn((short)1);
        mdSwitchOF10.getQueueStatisticsFromGivenPort(input.build()).get();
        Mockito.when(features.getVersion()).thenReturn((short)4);
        mdSwitchOF13.getQueueStatisticsFromGivenPort(input.build()).get();
        Mockito.verify(messageDispatchService, Mockito.times(2)).multipartRequest(
                Matchers.any(MultipartRequestInput.class),
                Matchers.any(SwitchConnectionDistinguisher.class));
    }
}
