/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.mp.MBodyExperimenter;
import org.opendaylight.of.lib.mp.MBodyMutableExperimenter;
import org.opendaylight.of.lib.msg.MeterBandType;
import org.opendaylight.of.lib.msg.MeterFlag;
import org.opendaylight.util.json.JsonCodecException;

import java.nio.ByteBuffer;

import static org.opendaylight.util.JSONUtils.toKey;


/**
* A JSON codec capable of encoding and decoding {@link MBodyExperimenter} 
* objects.
*
* @author Prashant Nayak
*/
public class MBodyExperimenterCodec extends OfJsonCodec<MBodyExperimenter>{

    
    /** JSON key for meter. */
    public static final String ROOT = "meter";
    /** JSON key for meters. */
    public static final String ROOTS = "meters";

    /** JSON key for meter mod ID. */
    private static final String METER_MOD_ID = "id";
    
    /** JSON key for meter flags. */
    private static final String FLAGS = "flags";

    /** JSON key for meter bands. */
    private static final String BANDS = "bands";
    /** JSON key for meter band burst size. */
    private static final String BURST_SIZE = "burst_size";
    /** JSON key for meter band rate. */
    private static final String RATE = "rate";
    /** JSON key for meter band type. */
    private static final String TYPE = "mtype";
    private static final String DROP = "drop";
    /*Constants for V1.0.*/
    private static final int MINIMUMLENGTH = 28;
    private static final int SET_KBPS_FLAG = 0x1;
    private static final int SET_DROP_FLAG = 0x4;
    private static final int DEF_VAL = 0;
    
    //SET_KBPS_FLAG = 0x1 and SET_DROP_FLAG = 0x4
    //changed it to suit the device
    private static final int KBPS_DROP_FLAG = 0x5;
    
    protected MBodyExperimenterCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(MBodyExperimenter expMod) {
        
        int flag = 0;
        int limiterId = DEF_VAL;
        int  capabilityFlags = DEF_VAL, dropRate = DEF_VAL, 
                markRate = DEF_VAL, burstSize = DEF_VAL, kbps = DEF_VAL, 
                drop = DEF_VAL, subtype = DEF_VAL;  
        short pad = DEF_VAL;
        
        ObjectNode expNode = mapper.createObjectNode();
       
        byte [] data = expMod.getData();    
        ByteBuffer bf =ByteBuffer.wrap(data, 0, MINIMUMLENGTH);
        
        subtype = bf.getInt();
        limiterId = bf.getInt();
                //Colarado devices return default meter id which is a
                //large number which is more than int. The default meter
                //cannot be deleted and a meter with id 0 cannot be added.
                //Hence overwriting with zero when id is negative
        if(limiterId < 0)
            limiterId = 0;
        capabilityFlags = bf.getInt();
        dropRate = bf.getInt();
        markRate = bf.getInt();
        burstSize = bf.getInt();
        pad= bf.getShort();
        
        subtype = DEF_VAL;
        markRate =  DEF_VAL;
        pad = DEF_VAL;
        
        expNode.put(METER_MOD_ID, limiterId);
        
        ObjectNode bandNode = objectNode();
        ArrayNode bandsNode = arrayNode();
        
        bandNode.put(BURST_SIZE, burstSize);
        bandNode.put(RATE, dropRate);
        
        ArrayNode flagsNode = arrayNode();
        //added extra check for default meter present on colorado - K_15_11_0003.swi 
        if(capabilityFlags == KBPS_DROP_FLAG || (capabilityFlags == 0x25 || capabilityFlags == 0x26) )
        {
            flagsNode.add(toKey(MeterFlag.KBPS));
            expNode.put(FLAGS, flagsNode);
            bandNode.put(TYPE, toKey(MeterBandType.DROP));    
        } else {
            throw new JsonCodecException("Unknown Command");
        }
        
        bandsNode.add(bandNode);
        expNode.put(BANDS, bandsNode);
              
        return expNode;
    }

    @Override
    public MBodyExperimenter decode(ObjectNode meterNode) {
        
        ProtocolVersion version  = CodecUtils.decodeProtocolVersion(
            meterNode.get(VERSION));
       
        int limiterId = DEF_VAL, capabilityFlags = DEF_VAL, dropRate = DEF_VAL, 
                markRate = DEF_VAL, burstSize = DEF_VAL, kbps = DEF_VAL, 
                drop = DEF_VAL, subtype = DEF_VAL;  
        short command = DEF_VAL, pad = DEF_VAL;
        limiterId = meterNode.get(METER_MOD_ID).intValue();
                //Colarado devices return default meter id which is a
                //large number which is more than int. The default meter
                //cannot be deleted and a meter with id 0 cannot be added.
                //Hence overwriting with zero when id is negative
        if(limiterId < 0)
            limiterId = 0;
        JsonNode flagsNode = meterNode.get(FLAGS);
        if (flagsNode != null) {
            for (JsonNode flag : flagsNode) {
                String f = flag.asText();    
                if (toKey(MeterFlag.KBPS).equalsIgnoreCase(f)){
                    kbps = SET_KBPS_FLAG; 
                } 
            }
            if (kbps == DEF_VAL) {
                throw new 
                JsonCodecException("Flag is not supported in openFlow 1.0");
            }
        }
        JsonNode bandsNode = meterNode.get(BANDS);
        if (bandsNode != null) {
            for(JsonNode band : bandsNode){
                burstSize = band.get(BURST_SIZE).intValue();
                dropRate = band.get(RATE).intValue();
                String type = band.get(TYPE).textValue();
                if(type.equals(DROP)){
                    drop = SET_DROP_FLAG;  
                } 
            }
            if (drop == DEF_VAL) {
                throw new 
                JsonCodecException("Band Type is not supported in openFlow 1.0");
            }
        }
        
        capabilityFlags = (kbps|drop);
        MBodyMutableExperimenter exp = new MBodyMutableExperimenter(version); 

        ByteBuffer bb = ByteBuffer.allocate(MINIMUMLENGTH);
        bb.putInt(subtype);
        bb.putInt(limiterId);
        bb.putInt(capabilityFlags);
        bb.putInt(dropRate);
        bb.putInt(markRate);
        bb.putInt(burstSize);
        bb.putShort(pad);
        exp.data(bb.array());
        return (MBodyExperimenter) (exp.toImmutable());
    }

}
