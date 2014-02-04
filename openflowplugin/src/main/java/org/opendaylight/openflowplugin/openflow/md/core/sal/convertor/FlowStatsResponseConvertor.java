/*
 * Copyright IBM Corporation, 2013.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;

/**
 * Class is an utility class for converting flow related statistics messages coming from openflow 
 * switch to MD-SAL messages.
 * @author avishnoi@in.ibm.com
 *
 */
public class FlowStatsResponseConvertor {
    
    /**
     * Method returns the list of MD-SAL format flow statistics, converted flow Openflow
     * specific flow statistics. 
     * @param allFlowStats
     * @return
     */
    public List<FlowAndStatisticsMapList> toSALFlowStatsList(List<FlowStats> allFlowStats,BigInteger datapathid){
        
        List<FlowAndStatisticsMapList> convertedSALFlowStats = new ArrayList<FlowAndStatisticsMapList>();
        
        for(FlowStats flowStats : allFlowStats){
            convertedSALFlowStats.add(toSALFlowStats(flowStats, datapathid));
        }
        
        return convertedSALFlowStats;
    }

    /**
     * Method convert Openflow switch specific flow statistics to the MD-SAL format 
     * flow statistics.
     * @param flowStats
     * @return
     */
    public FlowAndStatisticsMapList toSALFlowStats(FlowStats flowStats,BigInteger datapathid){
        FlowAndStatisticsMapListBuilder salFlowStatsBuilder = new FlowAndStatisticsMapListBuilder();
        salFlowStatsBuilder.setByteCount(new Counter64(flowStats.getByteCount()));
        salFlowStatsBuilder.setCookie(flowStats.getCookie());

        DurationBuilder time = new DurationBuilder();
        time.setSecond(new Counter32(flowStats.getDurationSec()));
        time.setNanosecond(new Counter32(flowStats.getDurationNsec()));
        salFlowStatsBuilder.setDuration(time.build());
        
        salFlowStatsBuilder.setHardTimeout(flowStats.getHardTimeout());
        salFlowStatsBuilder.setIdleTimeout(flowStats.getIdleTimeout());
        salFlowStatsBuilder.setPacketCount(new Counter64(flowStats.getPacketCount()));
        salFlowStatsBuilder.setPriority(flowStats.getPriority());
        salFlowStatsBuilder.setTableId(flowStats.getTableId());
        if(flowStats.getMatchV10() != null){
            salFlowStatsBuilder.setMatch(MatchConvertorImpl.fromOFMatchV10ToSALMatch(flowStats.getMatchV10(),datapathid));
            if(flowStats.getAction().size()!=0){
                salFlowStatsBuilder.setInstructions(OFToMDSalFlowConvertor.wrapOF10ActionsToInstruction(flowStats.getAction()));
            }
        }
        if(flowStats.getMatch() != null){
            salFlowStatsBuilder.setMatch(MatchConvertorImpl.fromOFMatchToSALMatch(flowStats.getMatch(),datapathid));
            salFlowStatsBuilder.setFlags(
                    new FlowModFlags(flowStats.getFlags().isOFPFFCHECKOVERLAP(),
                            flowStats.getFlags().isOFPFFRESETCOUNTS(),
                            flowStats.getFlags().isOFPFFNOPKTCOUNTS(),
                            flowStats.getFlags().isOFPFFNOBYTCOUNTS(),
                            flowStats.getFlags().isOFPFFSENDFLOWREM()));
        }
        if(flowStats.getInstruction()!= null){
            salFlowStatsBuilder.setInstructions(OFToMDSalFlowConvertor.toSALInstruction(flowStats.getInstruction()));
        }
        
        return salFlowStatsBuilder.build();
    }
}
