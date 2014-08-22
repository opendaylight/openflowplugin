/*
 * (c) Copyright 2013-2014 Hewlett-Packard Development Company, L.P.
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
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.json.JsonCodecException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.hex;
import static org.opendaylight.of.lib.CommonUtils.parseHexInt;
import static org.opendaylight.of.lib.msg.MessageType.METER_MOD;
import static org.opendaylight.util.JSONUtils.fromKey;
import static org.opendaylight.util.JSONUtils.toKey;

/**
 * A JSON codec capable of encoding and decoding {@link OfmMeterMod} objects.
 *
 * @author Prashant Nayak
 */
public class OfmMeterModCodec extends OfJsonCodec<OfmMeterMod> {

    /** JSON key for meter */
    public static final String ROOT = "meter";
    /** JSON key for meters */
    public static final String ROOTS = "meters";

    // Used externally as the location for injecting a command tag.
    /** JSON key for meter mod ID */
    public static final String METER_MOD_ID = "id";
    // Referenced externally for injecting a command tag.
    /** JSON key for meter mod command */
    public static final String METER_MOD_COMMAND = "command";
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

    protected OfmMeterModCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(OfmMeterMod meterMod) {

        ObjectNode meterNode = objectNode();

        meterNode.put(METER_MOD_ID, meterMod.getMeterId().toLong());
        meterNode.put(METER_MOD_COMMAND, toKey(meterMod.getCommand()));

        Set<MeterFlag> flags = meterMod.getFlags();
        if (flags != null) {
            ArrayNode flagsNode = arrayNode();
            for (MeterFlag f : flags) {
                flagsNode.add(toKey(f));
            }
            meterNode.put(FLAGS, flagsNode);
        }

        meterNode.put(BANDS, encodeBands(meterMod));

        return meterNode;
    }

    private ArrayNode encodeBands(OfmMeterMod ofmMeterMod) {
        List<MeterBand> bands = ofmMeterMod.getBands();
        ArrayNode bandsNode = arrayNode();
        if (bands != null) {
            for (MeterBand band : bands) {
                ObjectNode bandNode = objectNode();
                bandNode.put(BURST_SIZE, band.getBurstSize());
                bandNode.put(RATE, band.getRate());
                bandNode.put(TYPE, toKey(band.getType()));
                if (band instanceof MeterBandDscpRemark) {
                    bandNode.put(PREC_LEVEL,
                            ((MeterBandDscpRemark) band).getPrecLevel());
                } else if (band instanceof MeterBandExperimenter) {
                    bandNode.put(EXPERIMENTER,
                            hex(((MeterBandExperimenter) band).getExpId()
                                    .encodedId()));
                }
                bandsNode.add(bandNode);
            }

        }
        return bandsNode;
    }

    @Override
    public OfmMeterMod decode(ObjectNode meterNode) {
        ProtocolVersion pv = version(meterNode);

        OfmMutableMeterMod meter = (OfmMutableMeterMod) MessageFactory
                .create(pv, METER_MOD);

        meter.meterId(MeterId.valueOf(meterNode.get(METER_MOD_ID).longValue()));
        meter.command(fromKey(MeterModCommand.class,
                meterNode.get(METER_MOD_COMMAND).asText()));

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

        List<MeterBand> bands = decodeBands(pv, meterNode);

        for (MeterBand band : bands) {
            meter.addBand(band);
        }
        return (OfmMeterMod) meter.toImmutable();
    }

    private List<MeterBand> decodeBands(ProtocolVersion pv, JsonNode meterNode) {
        List<MeterBand> bands = new ArrayList<MeterBand>();
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
                    throw new JsonCodecException("Invalid meter band type: "
                            + bandNode.get(TYPE).textValue(), e);
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
                    int experimenter = parseHexInt(bandNode.get(EXPERIMENTER)
                            .asText());
                    meterBand = MeterBandFactory.createBand(pv, bandType, rate,
                            burstSize, experimenter);
                } else {
                    // This will only occur if new types are added that this
                    // codec is not prepared to handle. If the JSON contains a
                    // type that MeterBandType does not know about, that will
                    // be handled during the creation of bandType.
                    throw new JsonCodecException("Unknown meter band type: "
                            + bandType);
                }

                bands.add(meterBand);
            }
        }
        return bands;
    }

}
