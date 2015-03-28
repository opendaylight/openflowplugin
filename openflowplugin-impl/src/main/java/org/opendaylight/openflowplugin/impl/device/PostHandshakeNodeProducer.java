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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.XidGenerator;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.features.GroupFeaturesBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.MeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBurst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterKbps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterPktps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.device
 *
 * Class is a helper to prepare FlowCapableNode with whole internal future structure.
 * Everything is realized by {@link ListenableFuture} objects.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Mar 27, 2015
 */
class PostHandshakeNodeProducer {

    private static final Logger LOG = LoggerFactory.getLogger(PostHandshakeNodeProducer.class);

    private final Map<Long, Future<?>> futures;
    private final XidGenerator xidGenerator;
    private final ConnectionContext connectionContext;
    private SettableFuture<Node> deviceFuture;

    public PostHandshakeNodeProducer (@Nonnull final ConnectionContext connectionContext,
                                      @Nonnull final XidGenerator xidGenerator) {
        this.xidGenerator = Preconditions.checkNotNull(xidGenerator);
        this.connectionContext = Preconditions.checkNotNull(connectionContext);
        futures = new ConcurrentHashMap<>();
    }

    public ListenableFuture<Node> prepareFlowCapabeNode() {
        deviceFuture = SettableFuture.create();
        final NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setId(connectionContext.getNodeId());
        nodeBuilder.setKey(new NodeKey(connectionContext.getNodeId()));
        final Short version = connectionContext.getFeatures().getVersion();

        final long nodeDescXid = xidGenerator.generate().getValue();
        futures.put(nodeDescXid, queryDescription(connectionContext, nodeDescXid, nodeBuilder));

        final long meterFutureXid = xidGenerator.generate().getValue();
        futures.put(meterFutureXid, queryMeterFuture(connectionContext, meterFutureXid, nodeBuilder));

        final long groupFutureXid = xidGenerator.generate().getValue();
        futures.put(groupFutureXid, queryGroupFuture(connectionContext, groupFutureXid, nodeBuilder));

        final long tableFutureXid = xidGenerator.generate().getValue();
        futures.put(tableFutureXid, queryTableFuture(connectionContext, tableFutureXid, nodeBuilder));

        connectionContext.getConnectionAdapter().multipartRequest(MultipartRequestInputFactory
                .makeMultipartRequestInput(nodeDescXid, version, MultipartType.OFPMPDESC));
        connectionContext.getConnectionAdapter().multipartRequest(MultipartRequestInputFactory
                .makeMultipartRequestInput(meterFutureXid, version, MultipartType.OFPMPMETERFEATURES));
        connectionContext.getConnectionAdapter().multipartRequest(MultipartRequestInputFactory
                .makeMultipartRequestInput(groupFutureXid, version, MultipartType.OFPMPGROUPFEATURES));
        connectionContext.getConnectionAdapter().multipartRequest(MultipartRequestInputFactory
                .makeMultipartRequestInput(tableFutureXid, version, MultipartType.OFPMPTABLEFEATURES));

        return deviceFuture;
    }

    private ListenableFuture<Collection<MultipartReply>> queryTableFuture(final ConnectionContext connectionContext,
            final long tableFutureXid, final NodeBuilder nodeBuilder) {
        final ListenableFuture<Collection<MultipartReply>> nodeTableFuture = connectionContext.registerMultipartMsg(tableFutureXid);
        Futures.addCallback(nodeTableFuture, new FutureCallback<Collection<MultipartReply>>() {

            @Override
            public void onSuccess(final Collection<MultipartReply> result) {
                Preconditions.checkArgument(result != null && ( ! result.isEmpty()), "Node table future info result is null or empty!");
                final FlowCapableNode flowCapNode = nodeBuilder.getAugmentation(FlowCapableNode.class);
                final FlowCapableNodeBuilder flowCapAugBuilder = flowCapNode != null
                        ? new FlowCapableNodeBuilder(flowCapNode) : new FlowCapableNodeBuilder();
                for (final MultipartReply reply : result) {
                    final MultipartReplyTableFeaturesCase replyBody = (MultipartReplyTableFeaturesCase) reply.getMultipartReplyBody();
                    final MultipartReplyTableFeatures tableFutures = replyBody.getMultipartReplyTableFeatures();
                    // FIXME : add DataStore model in future commits in this chain
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.info("Failed to retrieve node table future info: {}", t.getMessage());
            }
        });

        return nodeTableFuture;
    }

    private ListenableFuture<Collection<MultipartReply>> queryGroupFuture(final ConnectionContext connectionContext,
            final long groupFutureXid, final NodeBuilder nodeBuilder) {
        final ListenableFuture<Collection<MultipartReply>> nodeGroupFuture = connectionContext.registerMultipartMsg(groupFutureXid);
        Futures.addCallback(nodeGroupFuture, new FutureCallback<Collection<MultipartReply>>() {

            @Override
            public void onSuccess(final Collection<MultipartReply> result) {
                Preconditions.checkArgument(result != null && ( ! result.isEmpty()), "Node group future info result is null or empty!");
                final NodeGroupFeaturesBuilder nodeGroupFeaturesBuilder = new NodeGroupFeaturesBuilder();
                final GroupFeaturesBuilder groupFeature = new GroupFeaturesBuilder();
                for (final MultipartReply reply : result) {
                    final MultipartReplyGroupFeaturesCase replyBody = (MultipartReplyGroupFeaturesCase) reply.getMultipartReplyBody();
                    final MultipartReplyGroupFeatures groupReplyFutures = replyBody.getMultipartReplyGroupFeatures();
                    groupFeature.setMaxGroups(groupReplyFutures.getMaxGroups());
                    final List<Class<? extends GroupType>> supportedGroups =  new ArrayList<>();
                    if(groupReplyFutures.getTypes().isOFPGTALL()){
                        supportedGroups.add(GroupAll.class);
                    }
                    if(groupReplyFutures.getTypes().isOFPGTSELECT()){
                        supportedGroups.add(GroupSelect.class);
                    }
                    if(groupReplyFutures.getTypes().isOFPGTINDIRECT()){
                        supportedGroups.add(GroupIndirect.class);
                    }
                    if(groupReplyFutures.getTypes().isOFPGTFF()){
                        supportedGroups.add(GroupFf.class);
                    }
                    groupFeature.setGroupTypesSupported(supportedGroups);

                    final List<Class<? extends GroupCapability>> gCapability = new ArrayList<>();
                    if(groupReplyFutures.getCapabilities().isOFPGFCCHAINING()){
                        gCapability.add(Chaining.class);
                    }
                    if(groupReplyFutures.getCapabilities().isOFPGFCCHAININGCHECKS()){
                        gCapability.add(ChainingChecks.class);
                    }
                    if(groupReplyFutures.getCapabilities().isOFPGFCSELECTLIVENESS()){
                        gCapability.add(SelectLiveness.class);
                    }
                    if(groupReplyFutures.getCapabilities().isOFPGFCSELECTWEIGHT()){
                        gCapability.add(SelectWeight.class);
                    }
                    groupFeature.setGroupCapabilitiesSupported(gCapability);
                    /* TODO :
                     *  My recommendation would be, its good to have a respective model of
                     * 'type bits', which will generate a class where all these flags will eventually
                     * be stored as boolean. It will be convenient for application to check the
                     * supported action, rather then doing bitmap operation.
                     */
                    final List<Long> supportActionByGroups = new ArrayList<>();
                    for (final ActionType actionType : groupReplyFutures.getActionsBitmap()) {
                        long supportActionBitmap = 0;
                        supportActionBitmap |= actionType.isOFPATOUTPUT()?(1 << 0): 0;
                        supportActionBitmap |= actionType.isOFPATCOPYTTLOUT()?(1 << 11): 0;
                        supportActionBitmap |= actionType.isOFPATCOPYTTLIN()?(1 << 12): 0;
                        supportActionBitmap |= actionType.isOFPATSETMPLSTTL()?(1 << 15): 0;
                        supportActionBitmap |= actionType.isOFPATDECMPLSTTL()?(1 << 16): 0;
                        supportActionBitmap |= actionType.isOFPATPUSHVLAN()?(1 << 17): 0;
                        supportActionBitmap |= actionType.isOFPATPOPVLAN()?(1 << 18): 0;
                        supportActionBitmap |= actionType.isOFPATPUSHMPLS()?(1 << 19): 0;
                        supportActionBitmap |= actionType.isOFPATPOPMPLS()?(1 << 20): 0;
                        supportActionBitmap |= actionType.isOFPATSETQUEUE()?(1 << 21): 0;
                        supportActionBitmap |= actionType.isOFPATGROUP()?(1 << 22): 0;
                        supportActionBitmap |= actionType.isOFPATSETNWTTL()?(1 << 23): 0;
                        supportActionBitmap |= actionType.isOFPATDECNWTTL()?(1 << 24): 0;
                        supportActionBitmap |= actionType.isOFPATSETFIELD()?(1 << 25): 0;
                        supportActionBitmap |= actionType.isOFPATPUSHPBB()?(1 << 26): 0;
                        supportActionBitmap |= actionType.isOFPATPOPPBB()?(1 << 27): 0;
                        supportActionByGroups.add(Long.valueOf(supportActionBitmap));
                    }
                    groupFeature.setActions(supportActionByGroups);
                }
                nodeGroupFeaturesBuilder.setGroupFeatures(groupFeature.build());
                nodeBuilder.addAugmentation(NodeGroupFeatures.class, nodeGroupFeaturesBuilder.build());
                checkForFinalization(groupFutureXid, nodeBuilder);
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.info("Failed to retrieve node group future info: {}", t.getMessage());
            }
        });

        return nodeGroupFuture;
    }

    private ListenableFuture<Collection<MultipartReply>> queryMeterFuture(final ConnectionContext connectionContext,
            final long meterFutureXid, final NodeBuilder nodeBuilder) {
        final ListenableFuture<Collection<MultipartReply>> nodeMeterFuture = connectionContext.registerMultipartMsg(meterFutureXid);
        Futures.addCallback(nodeMeterFuture, new FutureCallback<Collection<MultipartReply>>() {

            @Override
            public void onSuccess(final Collection<MultipartReply> result) {
                Preconditions.checkArgument(result != null && ( ! result.isEmpty()), "Node meter future info result is null or empty!");
                final NodeMeterFeaturesBuilder nodeMeterFeaturesBuilder = new NodeMeterFeaturesBuilder();
                final MeterFeaturesBuilder meterFeature = new MeterFeaturesBuilder();
                for (final MultipartReply reply : result) {
                    final MultipartReplyMeterFeaturesCase replyBody = (MultipartReplyMeterFeaturesCase) reply.getMultipartReplyBody();
                    final MultipartReplyMeterFeatures meterReplyFutures = replyBody.getMultipartReplyMeterFeatures();
                    meterFeature.setMaxBands(meterReplyFutures.getMaxBands());
                    meterFeature.setMaxColor(meterReplyFutures.getMaxColor());
                    meterFeature.setMaxMeter(new Counter32(meterReplyFutures.getMaxMeter()));
                    final List<Class<? extends MeterBand>> meterBandTypes = new ArrayList<>();
                    if (meterReplyFutures.getBandTypes().isOFPMBTDROP()) {
                        meterBandTypes.add(MeterBandDrop.class);
                    }
                    if (meterReplyFutures.getBandTypes().isOFPMBTDSCPREMARK()) {
                        meterBandTypes.add(MeterBandDscpRemark.class);
                    }
                    meterFeature.setMeterBandSupported(Collections.unmodifiableList(meterBandTypes));

                    final List<java.lang.Class<? extends MeterCapability>> mCapability = new ArrayList<>();
                    if (meterReplyFutures.getCapabilities().isOFPMFBURST()) {
                        mCapability.add(MeterBurst.class);
                    }
                    if(meterReplyFutures.getCapabilities().isOFPMFKBPS()){
                        mCapability.add(MeterKbps.class);

                    }
                    if(meterReplyFutures.getCapabilities().isOFPMFPKTPS()){
                        mCapability.add(MeterPktps.class);

                    }
                    if(meterReplyFutures.getCapabilities().isOFPMFSTATS()){
                        mCapability.add(MeterStats.class);

                    }
                    meterFeature.setMeterCapabilitiesSupported(Collections.unmodifiableList(mCapability));
                }
                nodeMeterFeaturesBuilder.setMeterFeatures(meterFeature.build());
                nodeBuilder.addAugmentation(NodeMeterFeatures.class, nodeMeterFeaturesBuilder.build());
                checkForFinalization(meterFutureXid, nodeBuilder);
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.info("Failed to retrieve node meter future info: {}", t.getMessage());
            }
        });

        return nodeMeterFuture;
    }

    private ListenableFuture<Collection<MultipartReply>> queryDescription(final ConnectionContext connectionContext,
            final long nodeDescXid, final NodeBuilder nodeBuilder) {
        final ListenableFuture<Collection<MultipartReply>> nodeDesc = connectionContext.registerMultipartMsg(nodeDescXid);
        final Short nrOfTables = connectionContext.getFeatures().getTables();
        Futures.addCallback(nodeDesc, new FutureCallback<Collection<MultipartReply>>() {

            @Override
            public void onSuccess(final Collection<MultipartReply> result) {
                Preconditions.checkArgument(result != null && ( ! result.isEmpty()), "Node static info result is null or empty!");
                final FlowCapableNode flowCapNode = nodeBuilder.getAugmentation(FlowCapableNode.class);
                final FlowCapableNodeBuilder flowCapAugBuilder = flowCapNode != null
                        ? new FlowCapableNodeBuilder(flowCapNode) : new FlowCapableNodeBuilder();
                final List<Table> tables = flowCapAugBuilder.getTable();
                for (int i = 0; i < nrOfTables.intValue(); i++) {
                    final Short id = Short.valueOf(((short) i));
                    tables.add(new TableBuilder().setId(id).setKey(new TableKey(id)).build());
                }
                flowCapAugBuilder.setTable(tables);
                for (final MultipartReply reply : result) {
                    final MultipartReplyDescCase replyBody = (MultipartReplyDescCase) reply.getMultipartReplyBody();
                    final MultipartReplyDesc description = replyBody.getMultipartReplyDesc();
                    flowCapAugBuilder.setDescription(choiseValues(flowCapAugBuilder.getDescription(), description.getDpDesc()));
                    flowCapAugBuilder.setHardware(choiseValues(flowCapAugBuilder.getHardware(), description.getHwDesc()));
                    flowCapAugBuilder.setManufacturer(choiseValues(flowCapAugBuilder.getManufacturer(), description.getMfrDesc()));
                    flowCapAugBuilder.setSoftware(choiseValues(flowCapAugBuilder.getSoftware(), description.getSwDesc()));
                    flowCapAugBuilder.setSerialNumber(choiseValues(flowCapAugBuilder.getSerialNumber(), description.getSerialNum()));
                }
                nodeBuilder.addAugmentation(FlowCapableNode.class, flowCapAugBuilder.build());
                checkForFinalization(nodeDescXid, nodeBuilder);
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.info("Failed to retrieve node static info: {}", t.getMessage());
                checkForFinalization(nodeDescXid, nodeBuilder);
            }
        });

        return nodeDesc;
    }

    private void checkForFinalization(final long xid, final NodeBuilder nodeBuilder) {
        futures.remove(xid);
        if (futures.isEmpty()) {
            deviceFuture.set(nodeBuilder.build());
        }
    }

    private static <T> T choiseValues(final T actual, final T newValue) {
        if (actual == null || newValue != null) {
            return newValue;
        } else {
            return actual;
        }
    }

}
