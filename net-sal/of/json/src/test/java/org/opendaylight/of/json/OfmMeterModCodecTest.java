/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.json.JSON;
import org.opendaylight.util.json.JsonFactory;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MeterBandFactory.createBand;
import static org.opendaylight.of.lib.msg.MeterBandType.*;
import static org.opendaylight.of.lib.msg.MeterFlag.*;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link OfmMeterModCodec}.
 *
 * @author Prashant Nayak
 */
public class OfmMeterModCodecTest extends AbstractCodecTest {

    private static final JsonFactory factory = OfJsonFactory.instance();
    private static final OfmMeterModCodec codec = (OfmMeterModCodec)
            factory.codec(OfmMeterMod.class);

    private static final String JSON_METER_MOD = "v13/ofmMeterMod";
    private static final String JSON_METER_MODS = "v13/ofmMeterMods";

    private static final MeterModCommand EXP_CMD = MeterModCommand.ADD;
    private static final MeterModCommand EXP_CMD_MODIFY =
            MeterModCommand.MODIFY;
    private static final MeterFlag[] EXP_FLAGS = {KBPS, BURST, STATS};
    private static final Set<MeterFlag> EXP_FLAGS_SET =
            new TreeSet<MeterFlag>(Arrays.asList(EXP_FLAGS));
    private static final MeterId EXP_ID = MeterId.valueOf(1);
    private static final MeterId EXP_ID_1 = MeterId.valueOf(2);

    private static final int EXP_BSIZE = 1000;

    private static final int EXP_RATE_B1 = 1500;
    private static final int EXP_RATE_B2 = 100;
    private static final int EXP_RATE_B3 = 100;

    private static final int EXP_PREC_B2 = 1;

    private static final ExperimenterId EXP_EXP_ID = ExperimenterId.HP;

    private static String meterJs = null;

    @BeforeClass
    public static void beforeClass() {
        JsonFactory factory = new OfJsonFactory();
        JSON.registerFactory(factory);
        meterJs = getJsonContents(JSON_METER_MOD);
    }

    /**
     * An end-to-end test of ofmMeterModCodec of a single meter using
     * JSON.fromJson and JSON.toJson.
     */
    @Test
    public void testOfmMeterMod() {
        OfmMeterMod meter = JSON.fromJson(meterJs, OfmMeterMod.class);
        print(JSON.toJson(meter, true));
        String actual = JSON.toJson(meter, true);
        assertEquals(normalizeEOL(meterJs), normalizeEOL(actual));
        validate(actual, OfmMeterModCodec.ROOT);
    }

    /**
     * An end-to-end test of ofmMeterModCodec of multiple meter mods using
     * JSON.fromJsonList and JSON.toJsonList.
     */
    @Test
    public void testMeters() {
        String metersJs = getJsonContents(JSON_METER_MODS);

        List<OfmMeterMod> meters = JSON
            .fromJsonList(metersJs, OfmMeterMod.class);
        String actual = JSON.toJsonList(meters,OfmMeterMod.class, true);
        assertEquals(normalizeEOL(metersJs),
                     normalizeEOL(JSON.toJsonList(meters,
                                                  OfmMeterMod.class, true)));
        validate(actual, OfmMeterModCodec.ROOTS);
    }

    @Test
    public void encode() {
        OfmMutableMeterMod meterMod = (OfmMutableMeterMod)
                MessageFactory.create(V_1_3, MessageType.METER_MOD);

        meterMod.meterId(EXP_ID).command(EXP_CMD).meterFlags(EXP_FLAGS_SET);

     // add the 3 bands
        meterMod.addBand(createBand(V_1_3, DROP, EXP_RATE_B1, EXP_BSIZE))
        .addBand(createBand(V_1_3, DSCP_REMARK, EXP_RATE_B2,
                            EXP_BSIZE, EXP_PREC_B2))
        .addBand(createBand(V_1_3, EXPERIMENTER, EXP_RATE_B3,
                            EXP_BSIZE, EXP_EXP_ID));

        String exp = getJsonContents(JSON_METER_MOD);
        String actual = codec.encode((OfmMeterMod) meterMod.toImmutable(), true);
                assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
        validate(actual, OfmMeterModCodec.ROOT);
    }

    @Test
    public void encodeList_v13() {
        List<OfmMeterMod> meterMods = new ArrayList<OfmMeterMod>();

        // ==== create the first meter mod object ====
        OfmMutableMeterMod meterMod = (OfmMutableMeterMod)
                MessageFactory.create(V_1_3, MessageType.METER_MOD);

        meterMod.meterId(EXP_ID).command(EXP_CMD).meterFlags(EXP_FLAGS_SET);

        // add the 3 bands
        meterMod.addBand(createBand(V_1_3, DROP, EXP_RATE_B1, EXP_BSIZE))
        .addBand(createBand(V_1_3, DSCP_REMARK, EXP_RATE_B2,
                               EXP_BSIZE, EXP_PREC_B2))
        .addBand(createBand(V_1_3, EXPERIMENTER, EXP_RATE_B3,
                               EXP_BSIZE, EXP_EXP_ID));

        meterMods.add((OfmMeterMod)meterMod.toImmutable());

     // ==== create the second meter mod object ====
        meterMod = (OfmMutableMeterMod)
                MessageFactory.create(V_1_3, MessageType.METER_MOD);

        meterMod.meterId(EXP_ID_1).command(EXP_CMD_MODIFY)
                .meterFlags(EXP_FLAGS_SET);

        // add the 3 bands
        meterMod.addBand(createBand(V_1_3, DROP, EXP_RATE_B1, EXP_BSIZE))
        .addBand(createBand(V_1_3, DSCP_REMARK, EXP_RATE_B2,
                               EXP_BSIZE, EXP_PREC_B2))
        .addBand(createBand(V_1_3, EXPERIMENTER, EXP_RATE_B3,
                               EXP_BSIZE, EXP_EXP_ID));
        meterMods.add((OfmMeterMod)meterMod.toImmutable());

        String exp = getJsonContents(JSON_METER_MODS);
        String actual = codec.encodeList(meterMods, true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
        validate(actual, OfmMeterModCodec.ROOTS);

    }

    @Test
    public void decodeList_v13() {
        String actual = getJsonContents(JSON_METER_MODS);

        List<OfmMeterMod> meterMods = codec.decodeList(actual);

        OfmMeterMod meterMod = meterMods.get(0);

        assertEquals(AM_NEQ, EXP_ID, meterMod.getMeterId());
        assertEquals(AM_NEQ, EXP_CMD, meterMod.getCommand());
        assertEquals(AM_NEQ, EXP_FLAGS_SET, meterMod.getFlags());

        Iterator<MeterBand> bIter = meterMod.getBands().iterator();

        checkBandHeader(bIter.next(), DROP, MeterBandDrop.class,
                        EXP_RATE_B1, EXP_BSIZE);

        checkDscpBand(bIter.next(), EXP_RATE_B2, EXP_BSIZE, EXP_PREC_B2);

        MeterBandExperimenter mbi = (MeterBandExperimenter)
                checkBandHeader(bIter.next(), EXPERIMENTER,
                     MeterBandExperimenter.class, EXP_RATE_B3, EXP_BSIZE);

        meterMod = meterMods.get(1);

        assertEquals(AM_NEQ, EXP_ID_1, meterMod.getMeterId());
        assertEquals(AM_NEQ, EXP_CMD_MODIFY, meterMod.getCommand());
        assertEquals(AM_NEQ, EXP_FLAGS_SET, meterMod.getFlags());

        bIter = meterMod.getBands().iterator();

        checkBandHeader(bIter.next(), DROP, MeterBandDrop.class,
                        EXP_RATE_B1, EXP_BSIZE);

        checkDscpBand(bIter.next(), EXP_RATE_B2, EXP_BSIZE, EXP_PREC_B2);

        mbi = (MeterBandExperimenter)
                checkBandHeader(bIter.next(), EXPERIMENTER,
                     MeterBandExperimenter.class, EXP_RATE_B3, EXP_BSIZE);

    }

    private MeterBand checkBandHeader(MeterBand mb, MeterBandType expType,
                                      Class<?> expClass,
                                      long expRate, long expBurst) {
             assertEquals(AM_NEQ, expType, mb.getType());
             assertTrue(AM_WRCL, expClass.isInstance(mb));
             assertEquals(AM_NEQ, expRate, mb.getRate());
             assertEquals(AM_NEQ, expBurst, mb.getBurstSize());
             return mb;
         }

    private void checkDscpBand(MeterBand mb, long expRate, long expBurst,
                                    int expPrecLevel) {
             checkBandHeader(mb, DSCP_REMARK, MeterBandDscpRemark.class,
                     expRate, expBurst);
             MeterBandDscpRemark mbi = (MeterBandDscpRemark) mb;
             assertEquals(AM_NEQ, expPrecLevel, mbi.getPrecLevel());
         }
}
