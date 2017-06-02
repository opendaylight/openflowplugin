/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.FlowStatsResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupDescStatsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupFeaturesUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Chaining;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.ChainingChecks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupAll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupFf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupIndirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupSelect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectLiveness;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectWeight;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterConfigStatsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterFeaturesUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBurst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterKbps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterPktps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.BytesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.PacketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class converts multipart reply messages to the notification objects defined
 * by statistics provider (manager ).
 *
 * @author avishnoi@in.ibm.com
 *
 */
public class MultipartReplyTranslator implements IMDMessageTranslator<OfHeader,  List<DataObject>> {

    protected static final Logger LOG = LoggerFactory
            .getLogger(MultipartReplyTranslator.class);
    private final ConvertorExecutor convertorExecutor;

    public MultipartReplyTranslator(ConvertorExecutor convertorExecutor) {
        this.convertorExecutor = convertorExecutor;
    }


    @Override
    public  List<DataObject> translate(final SwitchConnectionDistinguisher cookie, final SessionContext sc, final OfHeader msg) {

        List<DataObject> listDataObject = new CopyOnWriteArrayList<DataObject>();

        OpenflowVersion ofVersion = OpenflowVersion.get(sc.getPrimaryConductor().getVersion());
        final VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(sc.getPrimaryConductor().getVersion());
        data.setDatapathId(sc.getFeatures().getDatapathId());

        if(msg instanceof MultipartReplyMessage){
            MultipartReplyMessage mpReply = (MultipartReplyMessage)msg;
            NodeId node = MultipartReplyTranslator.nodeIdFromDatapathId(sc.getFeatures().getDatapathId());
            switch (mpReply.getType()){
            case OFPMPFLOW: {
                LOG.debug("Received flow statistics reponse from openflow {} switch",msg.getVersion()==1?"1.0":"1.3+");
                FlowStatsResponseConvertorData flowData = new FlowStatsResponseConvertorData(data.getVersion());
                flowData.setDatapathId(data.getDatapathId());
                flowData.setMatchPath(MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
                FlowsStatisticsUpdateBuilder message = new FlowsStatisticsUpdateBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                MultipartReplyFlowCase caseBody = (MultipartReplyFlowCase)mpReply.getMultipartReplyBody();
                MultipartReplyFlow replyBody = caseBody.getMultipartReplyFlow();

                final Optional<List<FlowAndStatisticsMapList>> flowAndStatisticsMapLists = convertorExecutor.convert(replyBody.getFlowStats(), flowData);

                message.setFlowAndStatisticsMapList(flowAndStatisticsMapLists.orElse(Collections.emptyList()));
                LOG.debug("Converted flow statistics : {}",message.build().toString());
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPAGGREGATE: {
                LOG.debug("Received aggregate flow statistics reponse from openflow {} switch",msg.getVersion()==1?"1.0":"1.3+");
                AggregateFlowStatisticsUpdateBuilder message = new AggregateFlowStatisticsUpdateBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));

                MultipartReplyAggregateCase caseBody = (MultipartReplyAggregateCase)mpReply.getMultipartReplyBody();
                MultipartReplyAggregate replyBody = caseBody.getMultipartReplyAggregate();
                message.setByteCount(new Counter64(replyBody.getByteCount()));
                message.setPacketCount(new Counter64(replyBody.getPacketCount()));
                message.setFlowCount(new Counter32(replyBody.getFlowCount()));

                LOG.debug("Converted aggregate flow statistics : {}",message.build().toString());
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPPORTSTATS: {

                LOG.debug("Received port statistics multipart response");

                NodeConnectorStatisticsUpdateBuilder message = new NodeConnectorStatisticsUpdateBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));

                MultipartReplyPortStatsCase caseBody = (MultipartReplyPortStatsCase)mpReply.getMultipartReplyBody();
                MultipartReplyPortStats replyBody = caseBody.getMultipartReplyPortStats();

                List<NodeConnectorStatisticsAndPortNumberMap> statsMap =
                        new ArrayList<NodeConnectorStatisticsAndPortNumberMap>();
                for (PortStats portStats: replyBody.getPortStats()){

                    NodeConnectorStatisticsAndPortNumberMapBuilder statsBuilder =
                            new NodeConnectorStatisticsAndPortNumberMapBuilder();
                    statsBuilder.setNodeConnectorId(
                            InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(sc.getFeatures().getDatapathId(),
                                    portStats.getPortNo(), ofVersion));

                    BytesBuilder bytesBuilder = new BytesBuilder();
                    bytesBuilder.setReceived(portStats.getRxBytes());
                    bytesBuilder.setTransmitted(portStats.getTxBytes());
                    statsBuilder.setBytes(bytesBuilder.build());

                    PacketsBuilder packetsBuilder = new PacketsBuilder();
                    packetsBuilder.setReceived(portStats.getRxPackets());
                    packetsBuilder.setTransmitted(portStats.getTxPackets());
                    statsBuilder.setPackets(packetsBuilder.build());

                    DurationBuilder durationBuilder = new DurationBuilder();
                    if (portStats.getDurationSec() != null) {
                        durationBuilder.setSecond(new Counter32(portStats.getDurationSec()));
                    }
                    if (portStats.getDurationNsec() != null) {
                        durationBuilder.setNanosecond(new Counter32(portStats.getDurationNsec()));
                    }
                    statsBuilder.setDuration(durationBuilder.build());
                    statsBuilder.setCollisionCount(portStats.getCollisions());
                    statsBuilder.setKey(new NodeConnectorStatisticsAndPortNumberMapKey(statsBuilder.getNodeConnectorId()));
                    statsBuilder.setReceiveCrcError(portStats.getRxCrcErr());
                    statsBuilder.setReceiveDrops(portStats.getRxDropped());
                    statsBuilder.setReceiveErrors(portStats.getRxErrors());
                    statsBuilder.setReceiveFrameError(portStats.getRxFrameErr());
                    statsBuilder.setReceiveOverRunError(portStats.getRxOverErr());
                    statsBuilder.setTransmitDrops(portStats.getTxDropped());
                    statsBuilder.setTransmitErrors(portStats.getTxErrors());

                    statsMap.add(statsBuilder.build());
                }
                message.setNodeConnectorStatisticsAndPortNumberMap(statsMap);

                LOG.debug("Converted ports statistics : {}",message.build().toString());

                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPGROUP:{
                LOG.debug("Received group statistics multipart reponse");
                GroupStatisticsUpdatedBuilder message = new GroupStatisticsUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                MultipartReplyGroupCase caseBody = (MultipartReplyGroupCase)mpReply.getMultipartReplyBody();
                MultipartReplyGroup replyBody = caseBody.getMultipartReplyGroup();
                final Optional<List<GroupStats>> groupStatsList = convertorExecutor.convert(replyBody.getGroupStats(), data);
                message.setGroupStats(groupStatsList.orElse(Collections.emptyList()));
                LOG.debug("Converted group statistics : {}",message.toString());
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPGROUPDESC:{
                LOG.debug("Received group description statistics multipart reponse");

                GroupDescStatsUpdatedBuilder message = new GroupDescStatsUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                MultipartReplyGroupDescCase caseBody = (MultipartReplyGroupDescCase)mpReply.getMultipartReplyBody();
                MultipartReplyGroupDesc replyBody = caseBody.getMultipartReplyGroupDesc();

                final Optional<List<GroupDescStats>> groupDescStatsList = convertorExecutor.convert(replyBody.getGroupDesc(), data);
                message.setGroupDescStats(groupDescStatsList.orElse(Collections.emptyList()));
                LOG.debug("Converted group statistics : {}",message.toString());
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPGROUPFEATURES: {
                LOG.debug("Received group features multipart reponse");
                GroupFeaturesUpdatedBuilder message = new GroupFeaturesUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));
                MultipartReplyGroupFeaturesCase caseBody = (MultipartReplyGroupFeaturesCase)mpReply.getMultipartReplyBody();
                MultipartReplyGroupFeatures replyBody = caseBody.getMultipartReplyGroupFeatures();
                List<Class<? extends GroupType>> supportedGroups =
                        new ArrayList<Class<? extends GroupType>>();

                if(replyBody.getTypes().isOFPGTALL()){
                    supportedGroups.add(GroupAll.class);
                }
                if(replyBody.getTypes().isOFPGTSELECT()){
                    supportedGroups.add(GroupSelect.class);
                }
                if(replyBody.getTypes().isOFPGTINDIRECT()){
                    supportedGroups.add(GroupIndirect.class);
                }
                if(replyBody.getTypes().isOFPGTFF()){
                    supportedGroups.add(GroupFf.class);
                }
                message.setGroupTypesSupported(supportedGroups);
                message.setMaxGroups(replyBody.getMaxGroups());

                List<Class<? extends GroupCapability>> supportedCapabilities =
                        new ArrayList<Class<? extends GroupCapability>>();

                if(replyBody.getCapabilities().isOFPGFCCHAINING()){
                    supportedCapabilities.add(Chaining.class);
                }
                if(replyBody.getCapabilities().isOFPGFCCHAININGCHECKS()){
                    supportedCapabilities.add(ChainingChecks.class);
                }
                if(replyBody.getCapabilities().isOFPGFCSELECTLIVENESS()){
                    supportedCapabilities.add(SelectLiveness.class);
                }
                if(replyBody.getCapabilities().isOFPGFCSELECTWEIGHT()){
                    supportedCapabilities.add(SelectWeight.class);
                }

                message.setGroupCapabilitiesSupported(supportedCapabilities);

                message.setActions(getGroupActionsSupportBitmap(replyBody.getActionsBitmap()));
                listDataObject.add(message.build());

                return listDataObject;
            }
            case OFPMPMETER: {
                LOG.debug("Received meter statistics multipart reponse");
                MeterStatisticsUpdatedBuilder message = new MeterStatisticsUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));

                MultipartReplyMeterCase caseBody = (MultipartReplyMeterCase)mpReply.getMultipartReplyBody();
                MultipartReplyMeter replyBody = caseBody.getMultipartReplyMeter();

                final Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats>> meterStatsList =
                        convertorExecutor.convert(replyBody.getMeterStats(), data);

                message.setMeterStats(meterStatsList.orElse(Collections.emptyList()));
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPMETERCONFIG:{
                LOG.debug("Received meter config statistics multipart reponse");

                MeterConfigStatsUpdatedBuilder message = new MeterConfigStatsUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));

                MultipartReplyMeterConfigCase caseBody = (MultipartReplyMeterConfigCase)mpReply.getMultipartReplyBody();
                MultipartReplyMeterConfig replyBody = caseBody.getMultipartReplyMeterConfig();

                final Optional<List<MeterConfigStats>> meterConfigStatsList = convertorExecutor.convert(replyBody.getMeterConfig(), data);

                message.setMeterConfigStats(meterConfigStatsList.orElse(Collections.emptyList()));
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPMETERFEATURES:{
                LOG.debug("Received meter features multipart reponse");
                //Convert OF message and send it to SAL listener
                MeterFeaturesUpdatedBuilder message = new MeterFeaturesUpdatedBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));

                MultipartReplyMeterFeaturesCase caseBody = (MultipartReplyMeterFeaturesCase)mpReply.getMultipartReplyBody();
                MultipartReplyMeterFeatures replyBody = caseBody.getMultipartReplyMeterFeatures();
                message.setMaxBands(replyBody.getMaxBands());
                message.setMaxColor(replyBody.getMaxColor());
                message.setMaxMeter(new Counter32(replyBody.getMaxMeter()));

                List<Class<? extends MeterCapability>> supportedCapabilities =
                        new ArrayList<Class<? extends MeterCapability>>();
                if(replyBody.getCapabilities().isOFPMFBURST()){
                    supportedCapabilities.add(MeterBurst.class);
                }
                if(replyBody.getCapabilities().isOFPMFKBPS()){
                    supportedCapabilities.add(MeterKbps.class);

                }
                if(replyBody.getCapabilities().isOFPMFPKTPS()){
                    supportedCapabilities.add(MeterPktps.class);

                }
                if(replyBody.getCapabilities().isOFPMFSTATS()){
                    supportedCapabilities.add(MeterStats.class);

                }
                message.setMeterCapabilitiesSupported(supportedCapabilities);

                List<Class<? extends MeterBand>> supportedMeterBand =
                        new ArrayList<Class <? extends MeterBand>>();
                if(replyBody.getBandTypes().isOFPMBTDROP()){
                    supportedMeterBand.add(MeterBandDrop.class);
                }
                if(replyBody.getBandTypes().isOFPMBTDSCPREMARK()){
                    supportedMeterBand.add(MeterBandDscpRemark.class);
                }
                message.setMeterBandSupported(supportedMeterBand);
                listDataObject.add(message.build());

                return listDataObject;
            }
            case OFPMPTABLE: {
                LOG.debug("Received flow table statistics reponse from openflow {} switch",msg.getVersion()==1?"1.0":"1.3+");

                FlowTableStatisticsUpdateBuilder message = new FlowTableStatisticsUpdateBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));

                MultipartReplyTableCase caseBody = (MultipartReplyTableCase)mpReply.getMultipartReplyBody();
                MultipartReplyTable replyBody = caseBody.getMultipartReplyTable();
                List<TableStats> swTablesStats = replyBody.getTableStats();

                List<FlowTableAndStatisticsMap> salFlowStats = new ArrayList<FlowTableAndStatisticsMap>();
                for(TableStats swTableStats : swTablesStats){
                    FlowTableAndStatisticsMapBuilder statisticsBuilder  = new FlowTableAndStatisticsMapBuilder();

                    statisticsBuilder.setActiveFlows(new Counter32(swTableStats.getActiveCount()));
                    statisticsBuilder.setPacketsLookedUp(new Counter64(swTableStats.getLookupCount()));
                    statisticsBuilder.setPacketsMatched(new Counter64(swTableStats.getMatchedCount()));
                    statisticsBuilder.setTableId(new TableId(swTableStats.getTableId()));
                    salFlowStats.add(statisticsBuilder.build());
                }

                message.setFlowTableAndStatisticsMap(salFlowStats);

                LOG.debug("Converted flow table statistics : {}",message.build().toString());
                listDataObject.add(message.build());
                return listDataObject;
            }
            case OFPMPQUEUE: {
                LOG.debug("Received queue statistics multipart response");

                QueueStatisticsUpdateBuilder message = new QueueStatisticsUpdateBuilder();
                message.setId(node);
                message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
                message.setTransactionId(generateTransactionId(mpReply.getXid()));

                MultipartReplyQueueCase caseBody = (MultipartReplyQueueCase)mpReply.getMultipartReplyBody();
                MultipartReplyQueue replyBody = caseBody.getMultipartReplyQueue();

                List<QueueIdAndStatisticsMap> statsMap =
                        new ArrayList<QueueIdAndStatisticsMap>();

                for (QueueStats queueStats: replyBody.getQueueStats()){

                    QueueIdAndStatisticsMapBuilder statsBuilder =
                            new QueueIdAndStatisticsMapBuilder();
                    statsBuilder.setNodeConnectorId(
                            InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(sc.getFeatures().getDatapathId(),
                                    queueStats.getPortNo(), ofVersion));
                    statsBuilder.setTransmissionErrors(new Counter64(queueStats.getTxErrors()));
                    statsBuilder.setTransmittedBytes(new Counter64(queueStats.getTxBytes()));
                    statsBuilder.setTransmittedPackets(new Counter64(queueStats.getTxPackets()));

                    DurationBuilder durationBuilder = new DurationBuilder();
                    durationBuilder.setSecond(new Counter32(queueStats.getDurationSec()));
                    durationBuilder.setNanosecond(new Counter32(queueStats.getDurationNsec()));
                    statsBuilder.setDuration(durationBuilder.build());

                    statsBuilder.setQueueId(new QueueId(queueStats.getQueueId()));
                    statsBuilder.setNodeConnectorId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(sc.getFeatures().getDatapathId(),
                            queueStats.getPortNo(), ofVersion));

                    statsMap.add(statsBuilder.build());
                }
                message.setQueueIdAndStatisticsMap(statsMap);

                LOG.debug("Converted queue statistics : {}",message.build().toString());
                listDataObject.add(message.build());
                return listDataObject;
            }

            default:
                return listDataObject;
            }
        }

        return listDataObject;
    }

    private static NodeId nodeIdFromDatapathId(final BigInteger datapathId) {
        String current = datapathId.toString();
        return new NodeId("openflow:" + current);
    }

    private static TransactionId generateTransactionId(final Long xid){
        BigInteger bigIntXid = BigInteger.valueOf(xid);
        return new TransactionId(bigIntXid);
    }

    /*
     * Method returns the bitmap of actions supported by each group.
     * TODO: My recommendation would be, its good to have a respective model of
     * 'type bits', which will generate a class where all these flags will eventually
     * be stored as boolean. It will be convenient for application to check the
     * supported action, rather then doing bitmap operation.
     * @param actionsSupported
     * @return
     */
    private static List<Long> getGroupActionsSupportBitmap(final List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType> actionsSupported){
        List<Long> supportActionByGroups = new ArrayList<Long>();
        for(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType supportedActions : actionsSupported){
            long supportActionBitmap = 0;
            supportActionBitmap |= supportedActions.isOFPATOUTPUT()?(1 << 0): 0;
            supportActionBitmap |= supportedActions.isOFPATCOPYTTLOUT()?(1 << 11): 0;
            supportActionBitmap |= supportedActions.isOFPATCOPYTTLIN()?(1 << 12): 0;
            supportActionBitmap |= supportedActions.isOFPATSETMPLSTTL()?(1 << 15): 0;
            supportActionBitmap |= supportedActions.isOFPATDECMPLSTTL()?(1 << 16): 0;
            supportActionBitmap |= supportedActions.isOFPATPUSHVLAN()?(1 << 17): 0;
            supportActionBitmap |= supportedActions.isOFPATPOPVLAN()?(1 << 18): 0;
            supportActionBitmap |= supportedActions.isOFPATPUSHMPLS()?(1 << 19): 0;
            supportActionBitmap |= supportedActions.isOFPATPOPMPLS()?(1 << 20): 0;
            supportActionBitmap |= supportedActions.isOFPATSETQUEUE()?(1 << 21): 0;
            supportActionBitmap |= supportedActions.isOFPATGROUP()?(1 << 22): 0;
            supportActionBitmap |= supportedActions.isOFPATSETNWTTL()?(1 << 23): 0;
            supportActionBitmap |= supportedActions.isOFPATDECNWTTL()?(1 << 24): 0;
            supportActionBitmap |= supportedActions.isOFPATSETFIELD()?(1 << 25): 0;
            supportActionBitmap |= supportedActions.isOFPATPUSHPBB()?(1 << 26): 0;
            supportActionBitmap |= supportedActions.isOFPATPOPPBB()?(1 << 27): 0;
            supportActionByGroups.add(Long.valueOf(supportActionBitmap));
        }
        return supportActionByGroups;
    }

}
