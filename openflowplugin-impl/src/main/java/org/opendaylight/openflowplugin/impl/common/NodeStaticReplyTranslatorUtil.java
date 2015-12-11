/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.common;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.TableFeaturesReplyConvertor;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;

/**
 * <p>
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.common
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *         </p>
 *         Created: Mar 31, 2015
 */
public class NodeStaticReplyTranslatorUtil {

    private NodeStaticReplyTranslatorUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Method transforms OFjava multipart reply model {@link MultipartReplyDesc} object
     * to inventory data model {@link FlowCapableNode} object.
     *
     * @param reply
     * @return
     */
    public static FlowCapableNode nodeDescTranslator(@CheckForNull final MultipartReplyDesc reply, final IpAddress ipAddress) {
        Preconditions.checkArgument(reply != null);
        final FlowCapableNodeBuilder flowCapAugBuilder = new FlowCapableNodeBuilder();
        flowCapAugBuilder.setDescription(reply.getDpDesc());
        flowCapAugBuilder.setHardware(reply.getHwDesc());
        flowCapAugBuilder.setManufacturer(reply.getMfrDesc());
        flowCapAugBuilder.setSoftware(reply.getSwDesc());
        flowCapAugBuilder.setSerialNumber(reply.getSerialNum());
        flowCapAugBuilder.setTable(Collections.<Table>emptyList());
        flowCapAugBuilder.setMeter(Collections.<Meter>emptyList());
        flowCapAugBuilder.setGroup(Collections.<Group>emptyList());
        if (ipAddress != null) {
            flowCapAugBuilder.setIpAddress(ipAddress);
        }
        return flowCapAugBuilder.build();
    }

    /**
     * Method transforms OFjava multipart reply model {@link MultipartReplyMeterFeatures} object
     * to inventory data model {@link NodeMeterFeatures} object.
     *
     * @param reply
     * @return
     */
    public static NodeMeterFeatures nodeMeterFeatureTranslator(@CheckForNull final MultipartReplyMeterFeatures reply) {
        Preconditions.checkArgument(reply != null);
        final MeterFeaturesBuilder meterFeature = new MeterFeaturesBuilder();
        meterFeature.setMaxBands(reply.getMaxBands());
        meterFeature.setMaxColor(reply.getMaxColor());
        meterFeature.setMaxMeter(new Counter32(reply.getMaxMeter()));
        final List<Class<? extends MeterBand>> meterBandTypes = new ArrayList<>();
        if (reply.getBandTypes().isOFPMBTDROP()) {
            meterBandTypes.add(MeterBandDrop.class);
        }
        if (reply.getBandTypes().isOFPMBTDSCPREMARK()) {
            meterBandTypes.add(MeterBandDscpRemark.class);
        }
        meterFeature.setMeterBandSupported(Collections.unmodifiableList(meterBandTypes));

        final List<java.lang.Class<? extends MeterCapability>> mCapability = new ArrayList<>();
        if (reply.getCapabilities().isOFPMFBURST()) {
            mCapability.add(MeterBurst.class);
        }
        if (reply.getCapabilities().isOFPMFKBPS()) {
            mCapability.add(MeterKbps.class);

        }
        if (reply.getCapabilities().isOFPMFPKTPS()) {
            mCapability.add(MeterPktps.class);

        }
        if (reply.getCapabilities().isOFPMFSTATS()) {
            mCapability.add(MeterStats.class);

        }
        meterFeature.setMeterCapabilitiesSupported(Collections.unmodifiableList(mCapability));
        return new NodeMeterFeaturesBuilder().setMeterFeatures(meterFeature.build()).build();
    }

    /**
     * Method transforms OFjava reply model {@link MultipartReplyGroupFeatures} object
     * to inventory data model {@link NodeGroupFeatures} object.
     *
     * @param reply
     * @return
     */
    public static NodeGroupFeatures nodeGroupFeatureTranslator(@CheckForNull final MultipartReplyGroupFeatures reply) {
        Preconditions.checkArgument(reply != null);
        final GroupFeaturesBuilder groupFeature = new GroupFeaturesBuilder();
        groupFeature.setMaxGroups(reply.getMaxGroups());
        final List<Class<? extends GroupType>> supportedGroups = new ArrayList<>();
        if (reply.getTypes().isOFPGTALL()) {
            supportedGroups.add(GroupAll.class);
        }
        if (reply.getTypes().isOFPGTSELECT()) {
            supportedGroups.add(GroupSelect.class);
        }
        if (reply.getTypes().isOFPGTINDIRECT()) {
            supportedGroups.add(GroupIndirect.class);
        }
        if (reply.getTypes().isOFPGTFF()) {
            supportedGroups.add(GroupFf.class);
        }
        groupFeature.setGroupTypesSupported(supportedGroups);

        final List<Class<? extends GroupCapability>> gCapability = new ArrayList<>();
        if (reply.getCapabilities().isOFPGFCCHAINING()) {
            gCapability.add(Chaining.class);
        }
        if (reply.getCapabilities().isOFPGFCCHAININGCHECKS()) {
            gCapability.add(ChainingChecks.class);
        }
        if (reply.getCapabilities().isOFPGFCSELECTLIVENESS()) {
            gCapability.add(SelectLiveness.class);
        }
        if (reply.getCapabilities().isOFPGFCSELECTWEIGHT()) {
            gCapability.add(SelectWeight.class);
        }
        groupFeature.setGroupCapabilitiesSupported(gCapability);

        final List<Long> supportActionByGroups = new ArrayList<>();
        for (final ActionType actionType : reply.getActionsBitmap()) {
            long supportActionBitmap = 0;
            supportActionBitmap |= actionType.isOFPATOUTPUT() ? (1 << 0) : 0;
            supportActionBitmap |= actionType.isOFPATCOPYTTLOUT() ? (1 << 11) : 0;
            supportActionBitmap |= actionType.isOFPATCOPYTTLIN() ? (1 << 12) : 0;
            supportActionBitmap |= actionType.isOFPATSETMPLSTTL() ? (1 << 15) : 0;
            supportActionBitmap |= actionType.isOFPATDECMPLSTTL() ? (1 << 16) : 0;
            supportActionBitmap |= actionType.isOFPATPUSHVLAN() ? (1 << 17) : 0;
            supportActionBitmap |= actionType.isOFPATPOPVLAN() ? (1 << 18) : 0;
            supportActionBitmap |= actionType.isOFPATPUSHMPLS() ? (1 << 19) : 0;
            supportActionBitmap |= actionType.isOFPATPOPMPLS() ? (1 << 20) : 0;
            supportActionBitmap |= actionType.isOFPATSETQUEUE() ? (1 << 21) : 0;
            supportActionBitmap |= actionType.isOFPATGROUP() ? (1 << 22) : 0;
            supportActionBitmap |= actionType.isOFPATSETNWTTL() ? (1 << 23) : 0;
            supportActionBitmap |= actionType.isOFPATDECNWTTL() ? (1 << 24) : 0;
            supportActionBitmap |= actionType.isOFPATSETFIELD() ? (1 << 25) : 0;
            supportActionBitmap |= actionType.isOFPATPUSHPBB() ? (1 << 26) : 0;
            supportActionBitmap |= actionType.isOFPATPOPPBB() ? (1 << 27) : 0;
            supportActionByGroups.add(Long.valueOf(supportActionBitmap));
        }
        groupFeature.setActions(supportActionByGroups);
        return new NodeGroupFeaturesBuilder().setGroupFeatures(groupFeature.build()).build();
    }

    /**
     * Method transform {@link MultipartReplyTableFeatures} to list of {@link TableFeatures}. Every
     * table can have List of TableFeatures so add it directly to
     * {@link org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder}
     *
     * @param reply
     * @return
     */
    public static List<TableFeatures> nodeTableFeatureTranslator(@CheckForNull final MultipartReplyTableFeatures reply) {
        Preconditions.checkArgument(reply != null);
        return TableFeaturesReplyConvertor.toTableFeaturesReply(reply);
    }

    /**
     * Method build a ID Node Connector from version and port number.
     *
     * @param datapathId
     * @param portNo
     * @param version
     * @return
     */
    public static NodeConnectorId nodeConnectorId(@CheckForNull final String datapathId, final long portNo, final short version) {
        Preconditions.checkArgument(datapathId != null);
        final String logicalName = OpenflowPortsUtil.getPortLogicalName(version, portNo);
        return new NodeConnectorId(OFConstants.OF_URI_PREFIX + datapathId + ":" + (logicalName == null ? portNo : logicalName));
    }
}
