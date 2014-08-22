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
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.mp.MBodyMeterConfig;
import org.opendaylight.of.lib.mp.MBodyMutableMeterConfig;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.json.JsonCodecException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.hex;
import static org.opendaylight.of.lib.CommonUtils.parseHexInt;
import static org.opendaylight.util.JSONUtils.fromKey;
import static org.opendaylight.util.JSONUtils.toKey;

/**
 * A JSON codec capable of encoding and decoding {@link MBodyMeterConfig}
 * objects.
 * 
 * @author Jesse Hummer
 */
public class MeterCodec extends OfJsonCodec<MBodyMeterConfig> {

    /** JSON key for meter */
    public static final String ROOT = "meter";
    /** JSON key for meters */
    public static final String ROOTS = "meters";

    /** JSON key for meter ID */
    private static final String ID = "id";
    /** JSON key for meter flags */
    private static final String FLAGS = "flags";

    /** JSON key for meter bands */
    private static final String BANDS = "bands";
    /** JSON key for meter band burst size */
    private static final String BURST_SIZE = "burst_size";
    /** JSON key for meter band rate */
    private static final String RATE = "rate";
    /** JSON key for meter band type */
    private static final String TYPE = "mtype";
    /** JSON key for meter band prec_level */
    private static final String PREC_LEVEL = "prec_level";
    /** JSON key for meter band experimenter */
    private static final String EXPERIMENTER = "experimenter";

    public MeterCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(MBodyMeterConfig meter) {
        ObjectNode meterNode = objectNode();

        meterNode.put(ID, meter.getMeterId().toLong());

        Set<MeterFlag> flags = meter.getFlags();
        if (flags != null) {
            ArrayNode flagsNode = arrayNode();
            for (MeterFlag f : flags) {
                flagsNode.add(toKey(f));
            }
            meterNode.put(FLAGS, flagsNode);
        }

        List<MeterBand> bands = meter.getBands();
        if (bands != null) {
            ArrayNode bandsNode = arrayNode();
            for (MeterBand band : bands) {
                ObjectNode bandNode = objectNode();
                bandNode.put(BURST_SIZE, band.getBurstSize());
                bandNode.put(RATE, band.getRate());
                bandNode.put(TYPE, toKey(band.getType()));
                if (band instanceof MeterBandDscpRemark) {
                    bandNode.put(PREC_LEVEL, ((MeterBandDscpRemark) band)
                            .getPrecLevel());
                } else if (band instanceof MeterBandExperimenter) {
                    bandNode.put(EXPERIMENTER, hex(((MeterBandExperimenter) band)
                            .getExpId().encodedId()));
                }
                bandsNode.add(bandNode);
            }
            meterNode.put(BANDS, bandsNode);
        }
        return meterNode;
    }

    @Override
    public MBodyMeterConfig decode(ObjectNode meterNode) {
        ProtocolVersion pv = version(meterNode); // will fail if
                                                 // decode(String) was not
                                                 // called!
        MBodyMutableMeterConfig meter = new MBodyMutableMeterConfig(pv);
        meter.meterId(MeterId.valueOf(meterNode.get(ID).longValue()));

        // Flags. Sample format:
        // "flags":["kbps","burst","stats"]
        JsonNode flagsNode = meterNode.get(FLAGS);
        if (flagsNode != null) {
            ArrayNode flagsArrayNode = (ArrayNode) flagsNode;
            HashSet<MeterFlag> flags = new HashSet<MeterFlag>();
            for (int i = 0; i < flagsArrayNode.size(); i++) {
                String f = flagsArrayNode.get(i).asText();
                if (toKey(MeterFlag.BURST).equals(f))
                    flags.add(MeterFlag.BURST);
                else if (toKey(MeterFlag.KBPS).equals(f))
                    flags.add(MeterFlag.KBPS);
                else if (toKey(MeterFlag.PKTPS).equals(f))
                    flags.add(MeterFlag.PKTPS);
                else if (toKey(MeterFlag.STATS).equals(f))
                    flags.add(MeterFlag.STATS);
                else
                    throw new JsonCodecException("Unknown meter flag: " + f);
            }
            meter.meterFlags(flags);
        }

        // Bands. Sample format:
        // "bands":[{"burst_size":1500,"rate":1000,"type":"drop"}]
        JsonNode bandsNode = meterNode.get(BANDS);
        if (bandsNode != null) {
            ArrayNode bandsArrayNode = (ArrayNode) bandsNode;
            for (int i = 0; i < bandsArrayNode.size(); i++) {
                ObjectNode bandNode = (ObjectNode) bandsArrayNode.get(i);
                long burstSize = bandNode.get(BURST_SIZE).longValue();
                long rate = bandNode.get(RATE).longValue();
                MeterBandType bandType = null;
                try {
                    bandType = fromKey(MeterBandType.class,
                            bandNode.get(TYPE).textValue());
                } catch (IllegalArgumentException e) {
                    throw new JsonCodecException("Invalid meter band type: " +
                            bandNode.get(TYPE).textValue(), e);
                }
                // options based on band type
                MeterBand meterBand = null;
                if (bandType.equals(MeterBandType.DROP)) {
                    meterBand = MeterBandFactory.createBand(pv, bandType, rate,
                            burstSize);
                } else if (bandType.equals(MeterBandType.DSCP_REMARK)) {
                    int precLevel = bandNode.get(PREC_LEVEL).intValue();
                    meterBand = MeterBandFactory.createBand(pv, bandType, rate,
                            burstSize, precLevel);
                } else if (bandType.equals(MeterBandType.EXPERIMENTER)) {
                    int experimenter = parseHexInt(bandNode.get(EXPERIMENTER).asText());
                    meterBand = MeterBandFactory.createBand(pv, bandType, rate,
                            burstSize, experimenter);
                } else {
                    // This will only occur if new types are added that this
                    // codec is not prepared to handle. If the JSON contains a
                    // type that MeterBandType does not know about, that will
                    // be handled during the creation of bandType.
                    throw new JsonCodecException("Unknown meter band type: " +
                            bandType);
                }

                meter.addBand(meterBand);
            }
        }

        return (MBodyMeterConfig) meter.toImmutable();
    }
}
