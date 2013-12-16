package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
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
    public List<FlowAndStatisticsMapList> toSALFlowStatsList(List<FlowStats> allFlowStats){
        
        List<FlowAndStatisticsMapList> convertedSALFlowStats = new ArrayList<FlowAndStatisticsMapList>();
        
        for(FlowStats flowStats : allFlowStats){
            convertedSALFlowStats.add(toSALFlowStats(flowStats));
        }
        
        return convertedSALFlowStats;
    }

    /**
     * Method convert Openflow switch specific flow statistics to the MD-SAL format 
     * flow statistics.
     * @param flowStats
     * @return
     */
    public FlowAndStatisticsMapList toSALFlowStats(FlowStats flowStats){
        FlowAndStatisticsMapListBuilder salFlowStatsBuilder = new FlowAndStatisticsMapListBuilder();
        salFlowStatsBuilder.setByteCount(new Counter64(flowStats.getByteCount()));
        salFlowStatsBuilder.setCookie(flowStats.getCookie());

        DurationBuilder time = new DurationBuilder();
        time.setSecond(new Counter32(flowStats.getDurationSec()));
        time.setNanosecond(new Counter32(flowStats.getDurationNsec()));
        salFlowStatsBuilder.setDuration(time.build());
        
        salFlowStatsBuilder.setFlags(
                new FlowModFlags(flowStats.getFlags().isOFPFFCHECKOVERLAP(),
                        flowStats.getFlags().isOFPFFRESETCOUNTS(),
                        flowStats.getFlags().isOFPFFNOPKTCOUNTS(),
                        flowStats.getFlags().isOFPFFNOBYTCOUNTS(),
                        flowStats.getFlags().isOFPFFSENDFLOWREM()));
        
        salFlowStatsBuilder.setHardTimeout(flowStats.getHardTimeout());
        salFlowStatsBuilder.setIdleTimeout(flowStats.getIdleTimeout());
        salFlowStatsBuilder.setPacketCount(new Counter64(flowStats.getPacketCount()));
        salFlowStatsBuilder.setPriority(flowStats.getPriority());
        salFlowStatsBuilder.setTableId(flowStats.getTableId());
        if(flowStats.getMatchV10() != null){
            salFlowStatsBuilder.setMatch(MatchConvertorImpl.fromOFMatchV10ToSALMatch(flowStats.getMatchV10()));
        }
        if(flowStats.getMatch() != null){
            salFlowStatsBuilder.setMatch(MatchConvertorImpl.fromOFMatchToSALMatch(flowStats.getMatch()));
        }
        if(flowStats.getInstructions()!= null){
            salFlowStatsBuilder.setInstructions(toSALInstruction(flowStats.getInstructions()));
        }
        
        return salFlowStatsBuilder.build();
        
    }
    
    /**
     * Method convert Openflow 1.3+ specific instructions to MD-SAL format
     * flow instruction
     * Note: MD-SAL won't augment this data directly to the data store, 
     * so key setting is not required. If user wants to augment this data
     * directly to the data store, key setting is required for each instructions. 
     * @param instructions
     * @return
     */
    public Instructions toSALInstruction(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.Instructions> instructions) {
        
        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        
        List<Instruction> salInstructionList = new ArrayList<Instruction>();
        
        for(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.Instructions switchInst : instructions){
            if(switchInst instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions){
                InstructionBuilder instBuilder = new InstructionBuilder();
                instBuilder.setInstruction(new ApplyActionsCaseBuilder().build());
                salInstructionList.add(instBuilder.build());
            }else if(switchInst instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ClearActions){
                InstructionBuilder instBuilder = new InstructionBuilder();
                instBuilder.setInstruction(new ClearActionsCaseBuilder().build());
                salInstructionList.add(instBuilder.build());
            }else if(switchInst instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.GotoTable){
                InstructionBuilder instBuilder = new InstructionBuilder();
                instBuilder.setInstruction(new GoToTableCaseBuilder().build());
                salInstructionList.add(instBuilder.build());
            }else if(switchInst instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteActions){
                InstructionBuilder instBuilder = new InstructionBuilder();
                instBuilder.setInstruction(new WriteActionsCaseBuilder().build());
                salInstructionList.add(instBuilder.build());
            }else if(switchInst instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteMetadata){
                InstructionBuilder instBuilder = new InstructionBuilder();
                instBuilder.setInstruction(new WriteMetadataCaseBuilder().build());
                salInstructionList.add(instBuilder.build());
            }
        }
        instructionsBuilder.setInstruction(salInstructionList);
        return instructionsBuilder.build();
    }
}
