/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.dt.MeterId;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageType.METER_MOD;
import static org.opendaylight.of.lib.msg.MeterBandFactory.createBand;
import static org.opendaylight.of.lib.msg.MeterBandType.*;
import static org.opendaylight.of.lib.msg.MeterFlag.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the OfmMeterMod message.
 *
 * @author Simon Hunt
 */
public class OfmMeterModTest extends OfmTest {

    // Test files...
    private static final String TF_MM_13 = "v13/meterMod";


    // ========================================================= PARSING ====

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

    private static final int EXP_MSG_LEN = 96;
    private static final MeterModCommand EXP_CMD = MeterModCommand.ADD;
    private static final MeterFlag[] EXP_FLAGS = {KBPS, BURST, STATS};
    private static final Set<MeterFlag> EXP_FLAGS_SET =
            new TreeSet<MeterFlag>(Arrays.asList(EXP_FLAGS));
    private static final MeterId EXP_ID = mid(0x25d);

    private static final int EXP_BSIZE = 2048;

    private static final int EXP_RATE_B1 = 4000;
    private static final int EXP_RATE_B2 = 512;
    private static final int EXP_RATE_B3 = 1024;
    private static final int EXP_RATE_B4 = 2048;
    private static final int EXP_RATE_B5 = 1000;

    private static final int EXP_PREC_B2 = 1;
    private static final int EXP_PREC_B3 = 2;
    private static final int EXP_PREC_B4 = 3;

    private static final ExperimenterId EXP_EXP_ID = ExperimenterId.NICIRA;

    @Test
    public void meterMod13() {
        print(EOL + "meterMod13()");
        OfmMeterMod msg = (OfmMeterMod) verifyMsgHeader(TF_MM_13, V_1_3,
                METER_MOD, EXP_MSG_LEN);

        // see v13/meterMod.hex for expected values
        assertEquals(AM_NEQ, EXP_CMD, msg.getCommand());
        verifyFlags(msg.getFlags(), EXP_FLAGS);
        assertEquals(AM_NEQ, EXP_ID, msg.getMeterId());
        assertEquals(AM_NEQ, 5, msg.getBands().size());
        Iterator<MeterBand> bIter = msg.getBands().iterator();

        checkBandHeader(bIter.next(), DROP, MeterBandDrop.class,
                EXP_RATE_B1, EXP_BSIZE);

        checkDscpBand(bIter.next(), EXP_RATE_B2, EXP_BSIZE, EXP_PREC_B2);
        checkDscpBand(bIter.next(), EXP_RATE_B3, EXP_BSIZE, EXP_PREC_B3);
        checkDscpBand(bIter.next(), EXP_RATE_B4, EXP_BSIZE, EXP_PREC_B4);

        MeterBandExperimenter mbi = (MeterBandExperimenter)
                checkBandHeader(bIter.next(), EXPERIMENTER,
                        MeterBandExperimenter.class, EXP_RATE_B5, EXP_BSIZE);
        assertEquals(AM_NEQ, EXP_EXP_ID, mbi.getExpId());
    }

    // NOTE: Meter Mod not supported in 1.0, 1.1, 1.2

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeMeterMod13() {
        print(EOL + "encodeMeterMod13()");

        OfmMutableMeterMod mod =
                (OfmMutableMeterMod) MessageFactory.create(V_1_3, METER_MOD);
        mod.clearXid();
        verifyMutableHeader(mod, V_1_3, METER_MOD, 0);

        mod.command(EXP_CMD).meterFlags(EXP_FLAGS_SET).meterId(EXP_ID);

        // add the 5 bands
        mod.addBand(createBand(V_1_3, DROP, EXP_RATE_B1, EXP_BSIZE))
        .addBand(createBand(V_1_3, DSCP_REMARK, EXP_RATE_B2, EXP_BSIZE, EXP_PREC_B2))
        .addBand(createBand(V_1_3, DSCP_REMARK, EXP_RATE_B3, EXP_BSIZE, EXP_PREC_B3))
        .addBand(createBand(V_1_3, DSCP_REMARK, EXP_RATE_B4, EXP_BSIZE, EXP_PREC_B4))
        .addBand(createBand(V_1_3, EXPERIMENTER, EXP_RATE_B5, EXP_BSIZE, EXP_EXP_ID));

        // now encode and verify
        encodeAndVerifyMessage(mod.toImmutable(), TF_MM_13);
    }

    @Test
    public void createWithCommand() {
        print(EOL + "createWithCommand()");
        OfmMutableMeterMod m = (OfmMutableMeterMod)
                MessageFactory.create(V_1_3, METER_MOD,
                        MeterModCommand.MODIFY);
        m.clearXid();
        verifyMutableHeader(m, V_1_3, METER_MOD, 0);
        assertEquals(AM_NEQ, MeterModCommand.MODIFY, m.getCommand());
    }

}
