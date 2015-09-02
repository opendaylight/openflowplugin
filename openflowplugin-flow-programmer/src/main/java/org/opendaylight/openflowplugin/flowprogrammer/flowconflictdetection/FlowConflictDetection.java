/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection;

import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.FlowData;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.EthernetData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.Long;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * This class implements Openflow flow conflict detection, it is used to
 * detect if a newly-added flow has any conflict with any existing flow
 * in a table. We call it as a conflict if any two flows are likely to
 * match the same packet. A project must call it for every flow if it hopes
 * detect any potential conflict.
 *
 * <p>
 *
 * @author Yi Yang (yi.y.yang@intel.com)
 *
 * <p>
 * @since 2015-08-25
 */

public class FlowConflictDetection {
    private static class FlowHashKey {
        private NodeId nodeId;
        private short tableId;
        private short projectId;

        public FlowHashKey(NodeId nodeId, short tableId, short projectId) {
            this.nodeId = nodeId;
            this.tableId = tableId;
            this.projectId = projectId;
        }

        @Override
        public java.lang.String toString() {
            return (this.nodeId.getValue() + "-" + String.valueOf(this.tableId) + "-" + String.valueOf(this.projectId));
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof FlowHashKey)) {
                return false;
            }
        
            FlowHashKey that = (FlowHashKey) other;
        
            // Custom equality check here.
            return (this.nodeId.equals(that.nodeId)
                    && (this.tableId == that.tableId)
                    && (this.projectId == that.projectId)
                   );
        }

        @Override
        public int hashCode() {
            int hashCode = 1;
        
            hashCode = hashCode * 37 + this.nodeId.hashCode();
            hashCode = hashCode * 37 + this.tableId;
            hashCode = hashCode * 37 + this.projectId;
        
            return hashCode;
        }
    }

    public static final int MAX_TBL_ID = 256;
    public static final int MAX_PRJ_ID = 32;
    public static final int CUR_MAX_PRJ_ID = 5;
    public static final Long ETHERTYPE_IPV4 = Long.valueOf(0x0800);
    public static final Long ETHERTYPE_VLAN = Long.valueOf(0x8100);
    public static final Long ETHERTYPE_MPLS_UCAST = Long.valueOf(0x8847);
    public static final Long ETHERTYPE_ARP = Long.valueOf(0x0806);
    private static HashMap<FlowHashKey, HashMap<FlowId, FlowData>> flowDataMap = new HashMap<FlowHashKey, HashMap<FlowId, FlowData>>();

    private static final Logger LOG = LoggerFactory.getLogger(FlowConflictDetection.class);

    /**
     * This method saves a given flow for conflict detection in the
     * future, every project must call it for every flow, otherwise
     * conflict detection won't work normally.
     *
     * <p>
     * @param flow A given FlowBuilder
     * @param nodeId Openflow node ID
     * @param tableID ID of the table this flow will be added to
     * @param projectID The project the caller belongs to
     */
    public static void saveFlow(FlowBuilder flow, NodeId nodeId, short tableID, short projectID) {
        Match match = flow.getMatch();
        FlowData myFlowData = new FlowData(match);
        FlowHashKey flowHashKey = new FlowHashKey(nodeId, tableID, projectID);
        HashMap<FlowId, FlowData> flowDataHashMap = flowDataMap.get(flowHashKey);

        if (flowDataHashMap == null) {
            flowDataHashMap = new HashMap<FlowId, FlowData>();
            flowDataHashMap.put(flow.getId(), myFlowData);
            flowDataMap.put(flowHashKey, flowDataHashMap);
        }
        else {
            flowDataHashMap.put(flow.getId(), myFlowData);
        }
    }

    /**
     * This method clears all the flow data in local cache.
     */
    public static void clearAllFlows() {
        Iterator<Map.Entry<FlowHashKey, HashMap<FlowId, FlowData>>> iterator = flowDataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<FlowHashKey, HashMap<FlowId, FlowData>> entry = iterator.next();
            FlowHashKey tmpKey = entry.getKey();
            LOG.debug("FlowHashKey: {}", tmpKey.toString());
            Iterator<Map.Entry<FlowId, FlowData>> flowIterator = entry.getValue().entrySet().iterator();
            while (flowIterator.hasNext()) {
                Map.Entry<FlowId, FlowData> flowEntry = flowIterator.next();
                FlowId flowId = flowEntry.getKey();
                flowIterator.remove();
                LOG.debug("        FlowId {} removed", flowId);
            }
            iterator.remove();
            LOG.debug("FlowHashKey {} removed", tmpKey.toString());
        }
    }

    /**
     * This method dumps all the flow data in local cache.
     */
    public static void dumpAllFlows() {
        LOG.info("flowDataMap size: {}", flowDataMap.size());
        for (FlowHashKey tmpKey : flowDataMap.keySet()) {
            LOG.info("FlowHashKey: {}", tmpKey.toString());
            for (FlowId tmpFlowId : flowDataMap.get(tmpKey).keySet()) {
                LOG.info("        FlowId: {}", tmpFlowId.getValue());
            }
        }
    }

    /**
     * This method detects if a given flow has conflict with other
     * flows in the specified table or not, we call it as a conflict
     * if two flows are likely to match the same packet.
     *
     * <p>
     * @param flow A given flow
     * @param nodeId Openflow node ID
     * @param tableID ID of the table this flow will be added to
     * @param projectID The project the caller belongs to
     * @return true if there isn't conflict, false otherwise
     */
    public static boolean isFlowOkInProject(FlowBuilder flow, NodeId nodeId, short tableID, short projectID) {
        boolean isOk = true;
        FlowHashKey flowHashKey = new FlowHashKey(nodeId, tableID, projectID);
        Match match = flow.getMatch();
        FlowData myFlowData = new FlowData(match);

        LOG.debug("isFlowOkInProject: flowHashKey = {}", flowHashKey.toString());
        HashMap<FlowId, FlowData> flowDataHashMap = flowDataMap.get(flowHashKey);
        if (flowDataHashMap == null) {
            LOG.debug("isFlowOkInProject: flowDataHashMap empty."); 
            return isOk;
        }

        for (FlowData tmpFlowData : flowDataHashMap.values()) {
            /* Check if it is there */
            if (tmpFlowData == null) {
                continue;
            }
    
            /* Check if there is in_port conflict */
            if ((tmpFlowData.inPort != null)
                && (match.getInPort() != null)
                && !tmpFlowData.inPort.equals(match.getInPort())) {
                continue;
            }
    
            /* Check if there is L2 match conflict */
            EthernetMatch etherMatch = match.getEthernetMatch();
            if ((etherMatch != null) && (tmpFlowData.ethernetData != null)) {
                /* Check if there is EthernetMatch conflict */
                if (tmpFlowData.ethernetData.isSame(etherMatch) == false) {
                    continue;
                }
    
                EthernetType etherType = etherMatch.getEthernetType();
                Long type = etherType.getType().getValue();
                if (type == ETHERTYPE_VLAN) { // Vlan
                    VlanMatch vlanMatch = match.getVlanMatch();
                    if ((vlanMatch != null) && (tmpFlowData.vlanData != null)) {
                        if (tmpFlowData.vlanData.isSame(vlanMatch) == false) {
                            continue;
                        }
                    }
                }
                else if (type == ETHERTYPE_MPLS_UCAST) { // Mpls
                    ProtocolMatchFields mplsMatch = match.getProtocolMatchFields();
                    if ((mplsMatch != null) && (tmpFlowData.mplsData != null)) {
                        if (tmpFlowData.mplsData.isSame(mplsMatch) == false) {
                            continue;
                        }
                    }
                }
            }
    
            /* Check if there is IpMatch conflict */
            IpMatch ipMatch = match.getIpMatch();
            if ((ipMatch != null) && (tmpFlowData.ipData != null)) {
                if (tmpFlowData.ipData.isSame(ipMatch) == false) {
                    continue;
                }
            }
    
            /* Check if there is Layer3Match conflict */
            if (match.getLayer3Match() != null) {
                Layer3Match l3Match = match.getLayer3Match();
                if ((l3Match instanceof Ipv4Match) && (tmpFlowData.ipv4Data != null)) {
                    if (tmpFlowData.ipv4Data.isSame((Ipv4Match)l3Match) == false) {
                        continue;
                    }
                }
                else if ((l3Match instanceof Ipv6Match) && (tmpFlowData.ipv6Data != null)) {
                    if (tmpFlowData.ipv6Data.isSame((Ipv6Match)l3Match) == false) {
                        continue;
                    }
                }
                else if ((l3Match instanceof ArpMatch) && (tmpFlowData.arpData != null)) {
                    if (tmpFlowData.arpData.isSame((ArpMatch)l3Match) == false) {
                        continue;
                    }
                }
            }
    
            /* Check if there is Layer4Match conflict */
            if (match.getLayer4Match() != null) {
                Layer4Match l4Match = match.getLayer4Match();
                if (((l4Match instanceof TcpMatch)
                     || (l4Match instanceof UdpMatch)
                     || (l4Match instanceof SctpMatch))
                    && (tmpFlowData.l4Data != null)) {
                    if (tmpFlowData.l4Data.isSame(l4Match) == false) {
                        continue;
                    }
                }
            }
    
            /* Check if there is Nicira Extension Match conflict */
            GeneralAugMatchNodesNodeTableFlow m = match.getAugmentation(GeneralAugMatchNodesNodeTableFlow.class);
            if ((m != null) && (tmpFlowData.nxmData != null)) {
                if (tmpFlowData.nxmData.isSame(m) == false) {
                    continue;
                }
            }

            /* Arriving here means there is a conflict exisiting */
            isOk = false;
            LOG.debug("isFlowOkInProject: a conflict in project {}", projectID);
            return isOk;
        }

        /* Arriving here means there isn't any conflict exisiting */
        isOk = true;
        LOG.debug("isFlowOkInProject: no conflict in project {}", projectID);
        return isOk;
    }

    /**
     * This method detects if a given flow has conflict with other
     * projects' flows in the specified table or not.
     *
     * <p>
     * @param flow A given flow
     * @param nodeId Openflow node ID
     * @param tableID ID of the table this flow will be added to
     * @param projectID The project the caller belongs to
     * @return true if there isn't conflict, false otherwise
     */
    public static boolean isFlowOk(FlowBuilder flow, NodeId nodeId, short tableID, short projectID) {
        boolean isOk = true;

        for (short prjId = 0; prjId < CUR_MAX_PRJ_ID; prjId++) {
            if (prjId == projectID) {
                continue; // Won't detect for the caller's project
            }

            isOk = isFlowOkInProject(flow, nodeId, tableID, prjId);
            if (isOk == false) {
                LOG.debug("isFlowOk: return {}", isOk);
                return isOk;
            }
        }

        LOG.debug("isFlowOk: return {}", isOk);
        return isOk;
    }
}
