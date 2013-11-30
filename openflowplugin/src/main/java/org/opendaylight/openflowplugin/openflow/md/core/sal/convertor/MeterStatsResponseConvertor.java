package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;

import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStatBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenter;

/**
 * Class is an utility class for converting group related statistics messages coming from switch to MD-SAL
 * messages.
 * @author avishnoi@in.ibm.com
 *
 */
public class MeterStatsResponseConvertor {

    /**
     * Method converts list of OF Meter Stats to SAL Meter Stats.
     * @param allMeterStats
     * @return List of MeterStats
     */
    public List<MeterStats> toSALMeterStatsList(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.MeterStats> allMeterStats){
        List<MeterStats> convertedSALMeters = new ArrayList<MeterStats>();
        for(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.MeterStats meter: allMeterStats){
            convertedSALMeters.add(toSALMeterStats(meter));
        }
        return convertedSALMeters;
        
    }
    
    /**
     * Method convert MeterStats message from library to MD SAL defined MeterStats  
     * @param meterStats MeterStats from library
     * @return MeterStats -- MeterStats defined in MD-SAL
     */
    public MeterStats toSALMeterStats(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.MeterStats meterStats){
        
        MeterStatsBuilder salMeterStats = new MeterStatsBuilder();
        salMeterStats.setByteInCount(new Counter64(meterStats.getByteInCount()));
        
        DurationBuilder time = new DurationBuilder();
        time.setSecond(new Counter32(meterStats.getDurationSec()));
        time.setNanosecond(new Counter32(meterStats.getDurationNsec()));
        salMeterStats.setDuration(time.build());
        
        salMeterStats.setFlowCount(new Counter32(meterStats.getFlowCount()));
        salMeterStats.setMeterId(meterStats.getMeterId().intValue());
        salMeterStats.setPacketInCount(new Counter64(meterStats.getPacketInCount()));
        
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol
        .rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter
        .meter.stats.MeterBandStats> allMeterBandStats = meterStats.getMeterBandStats();
        
        MeterBandStatsBuilder meterBandStatsBuilder = new MeterBandStatsBuilder();
        List<BandStat> listAllBandStats = new ArrayList<BandStat>();
        for(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol
                .rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter
                .meter.stats.MeterBandStats meterBandStats : allMeterBandStats){
            BandStatBuilder bandStatBuilder = new BandStatBuilder(); 
            bandStatBuilder.setByteBandCount(new Counter64(meterBandStats.getByteBandCount()));
            bandStatBuilder.setPacketBandCount(new Counter64(meterBandStats.getPacketBandCount()));
            listAllBandStats.add(bandStatBuilder.build());
        }
        meterBandStatsBuilder.setBandStat(listAllBandStats);
        salMeterStats.setMeterBandStats(meterBandStatsBuilder.build());
        return salMeterStats.build();
    }
    
    /**
     * Method convert list of OF Meter config Stats to SAL Meter Config stats 
     * @param allMeterConfigs
     * @return list of MeterConfigStats
     */
    public List<MeterConfigStats> toSALMeterConfigList(List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config.MeterConfig> allMeterConfigs){
        
        List<MeterConfigStats> listMeterConfigStats = new ArrayList<MeterConfigStats>();
        for( org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config.MeterConfig meterConfig : allMeterConfigs){
            MeterConfigStatsBuilder meterConfigStatsBuilder = new MeterConfigStatsBuilder();
            meterConfigStatsBuilder.setMeterId(new MeterId(meterConfig.getMeterId()));
            //TODO: Set flag should be bitmap and not once enum value.
            //Need to discuss with openflowjava team.
            //meterConfigStatsBuilder.setFlags(value);
            
            MeterBandHeadersBuilder meterBandHeadersBuilder = new MeterBandHeadersBuilder();
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config.meter.config.Bands> bands = meterConfig.getBands();
            
            List<MeterBandHeader> listBandHeaders = new ArrayList<MeterBandHeader>();
            for(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config.meter.config.Bands band : bands){
                MeterBandHeaderBuilder meterBandHeaderBuilder = new MeterBandHeaderBuilder();
                if(band.getMeterBand() instanceof MeterBandDrop){
                    MeterBandDrop dropBand = (MeterBandDrop)band.getMeterBand();
                    DropBuilder dropBuilder = new DropBuilder();
                    dropBuilder.setBurstSize(dropBand.getBurstSize());
                    dropBuilder.setRate(dropBand.getRate());
                    meterBandHeaderBuilder.setBandType(dropBuilder.build());
                    
                    meterBandHeaderBuilder.setBurstSize(dropBand.getBurstSize());
                    meterBandHeaderBuilder.setRate(dropBand.getRate());
                    listBandHeaders.add(meterBandHeaderBuilder.build());
                }else if (band.getMeterBand() instanceof MeterBandDscpRemark){
                    MeterBandDscpRemark dscpRemarkBand = (MeterBandDscpRemark)band.getMeterBand();
                    DscpRemarkBuilder dscpRemarkBuilder = new DscpRemarkBuilder();
                    dscpRemarkBuilder.setBurstSize(dscpRemarkBand.getBurstSize());
                    dscpRemarkBuilder.setRate(dscpRemarkBand.getRate());
                    meterBandHeaderBuilder.setBandType(dscpRemarkBuilder.build());
                    
                    meterBandHeaderBuilder.setBurstSize(dscpRemarkBand.getBurstSize());
                    meterBandHeaderBuilder.setRate(dscpRemarkBand.getRate());
                    listBandHeaders.add(meterBandHeaderBuilder.build());
                    
                }else if (band.getMeterBand() instanceof MeterBandExperimenter){
                    MeterBandExperimenter experimenterBand = (MeterBandExperimenter)band.getMeterBand();
                    ExperimenterBuilder experimenterBuilder = new ExperimenterBuilder();
                    experimenterBuilder.setBurstSize(experimenterBand.getBurstSize());
                    experimenterBuilder.setRate(experimenterBand.getRate());
                    meterBandHeaderBuilder.setBandType(experimenterBuilder.build());
                    
                    meterBandHeaderBuilder.setBurstSize(experimenterBand.getBurstSize());
                    meterBandHeaderBuilder.setRate(experimenterBand.getRate());
                    listBandHeaders.add(meterBandHeaderBuilder.build());
                    
                }
            }
            meterBandHeadersBuilder.setMeterBandHeader(listBandHeaders);
            meterConfigStatsBuilder.setMeterBandHeaders(meterBandHeadersBuilder.build());
            listMeterConfigStats.add(meterConfigStatsBuilder.build());
        }
        
        return listMeterConfigStats;
    }
}
