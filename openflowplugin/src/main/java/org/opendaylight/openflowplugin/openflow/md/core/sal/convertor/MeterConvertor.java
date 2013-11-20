package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;
/****
 *
 * This class is used for converting the data from SAL layer to OF Library Layer for Meter Mod Command.
 *
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags.Flags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Drop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeaders;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.Bands;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.BandsBuilder;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;


public final class MeterConvertor {

	 private static List<Bands> bands;
	 private static MeterModInputBuilder meterModInputBuilder;
	 private static MeterFlags flags;
	 private MeterConvertor(){

	 }

	 //Get all the data for the meter from the Yang/SAL-Layer
	 /**
	 * @param Yang Data source
	 * @return MeterModInput required by OF Library
	 */
	public static MeterModInput  toMeterModInput(org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter source ) {

		 meterModInputBuilder = new MeterModInputBuilder();


		 if(source instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput)
				 meterModInputBuilder.setCommand(MeterModCommand.OFPMCADD);
			if(source instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput)
				meterModInputBuilder.setCommand(MeterModCommand.OFPMCDELETE);
			if(source instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput)
				meterModInputBuilder.setCommand(MeterModCommand.OFPMCMODIFY);

			meterModInputBuilder.setMeterId(new MeterId(source.getMeterId().getValue()));

			getFlagsFromSAL(source.getFlags());
				meterModInputBuilder.setFlags(flags);
			getBandsFromSAL(source.getMeterBandHeaders());
		 		 meterModInputBuilder.setBands(bands);
		 return meterModInputBuilder.build();
	 }



	 private static void getBandsFromSAL(MeterBandHeaders meterBandHeaders) {

		Iterator <MeterBandHeader> bandHeadersIterator  = meterBandHeaders.getMeterBandHeader().iterator();
		MeterBandHeader meterBandHeader;

		BandsBuilder bandsB = null;
		//MeterBandHeaderBuilder meterBandHeaderBuilder = null ;
		bands = new ArrayList<Bands>();
		while(bandHeadersIterator.hasNext())
		{
				meterBandHeader = bandHeadersIterator.next();
				MeterBand meterBandItem = null;
				//The band types :drop,DSCP_Remark or experimenter.
				//meterBandHeaderBuilder = new MeterBandHeaderBuilder();


				if(meterBandHeader.getMeterBandTypes().getFlags().isOfpmbtDrop()){

					MeterBandDropBuilder meterBandDropBuilder = new MeterBandDropBuilder();
					meterBandDropBuilder.setType(MeterBandType.OFPMBTDROP);

					Drop drop = (Drop)meterBandHeader.getBandType();

					meterBandDropBuilder.setBurstSize(drop.getRate());
					meterBandDropBuilder.setRate(drop.getBurstSize());
					meterBandItem= meterBandDropBuilder.build();

				}
				else if(meterBandHeader.getMeterBandTypes().getFlags().isOfpmbtDscpRemark())
				{
					MeterBandDscpRemarkBuilder meterBandDscpRemarkBuilder = new MeterBandDscpRemarkBuilder();
					meterBandDscpRemarkBuilder.setType(MeterBandType.OFPMBTDSCPREMARK);

					DscpRemark dscpRemark = (DscpRemark)meterBandHeader.getBandType();

					meterBandDscpRemarkBuilder.setBurstSize(dscpRemark.getBurstSize());
					meterBandDscpRemarkBuilder.setRate(dscpRemark.getRate());
					meterBandDscpRemarkBuilder.setPrecLevel(dscpRemark.getPercLevel());
					meterBandItem = meterBandDscpRemarkBuilder.build();

				}
				else if(meterBandHeader.getMeterBandTypes().getFlags().isOfpmbtExperimenter())
				{
					MeterBandExperimenterBuilder meterBandExperimenterBuilder = new MeterBandExperimenterBuilder();
					meterBandExperimenterBuilder.setType(MeterBandType.OFPMBTEXPERIMENTER);
				        Experimenter experimenter = (Experimenter)meterBandHeader.getBandType();
					meterBandExperimenterBuilder.setBurstSize(experimenter.getBurstSize());
					meterBandExperimenterBuilder.setRate(experimenter.getRate());
					meterBandExperimenterBuilder.setExperimenter(experimenter.getExperimenter());
					meterBandItem = meterBandExperimenterBuilder.build();

				}

				bandsB = new BandsBuilder();
				bandsB.setMeterBand(meterBandItem).build();

				bands.add(bandsB.build()); //Bands list


			}

	}

	 //get it from plugin(SAL) layer
	 private static void getFlagsFromSAL(Flags flags2) {
	                        boolean meterBurst_SAL = false;
	                        boolean meterKbps_SAL = false;
				boolean meterPktps_SAL = false;
				boolean meterStats_SAL = false;


				if(flags2.isMeterBurst())	meterBurst_SAL = true;
				if(flags2.isMeterKbps())  meterKbps_SAL = true;
				if(flags2.isMeterPktps()) meterPktps_SAL = true;
				if(flags2.isMeterStats()) meterStats_SAL = true;

				flags = new MeterFlags(meterBurst_SAL,meterKbps_SAL,meterPktps_SAL,meterStats_SAL);


		}


}
