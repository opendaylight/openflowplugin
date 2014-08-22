/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractTest;
import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MeterBandFactory.
 *
 * @author Simon Hunt
 */
public class MeterBandFactoryTest extends AbstractTest {

    private static final String MB_DROP = "msg/struct/meterBandDropV3.hex";
    private static final String MB_DSCP = "msg/struct/meterBandDscpV3.hex";
    private static final String MB_EXPR = "msg/struct/meterBandExprV3.hex";

    private static final String E_PARSE_FAIL = "Failed to parse meter band.";

    private static final long EXP_RATE = 1000;
    private static final long EXP_BURST = 2048;

    private void verifyHeader(MeterBand mb, MeterBandType expType,
                              Class<?> expClass, long expRate, long expBurst) {
        assertEquals(AM_NEQ, expType, mb.getType());
        assertEquals(AM_NEQ, expRate, mb.getRate());
        assertEquals(AM_NEQ, expBurst, mb.getBurstSize());
        assertTrue(AM_WRCL, expClass.isInstance(mb));
    }

    @Test
    public void meterBandDrop03() {
        print(EOL + "meterBandDrop03");
        OfPacketReader pkt = getPacketReader(MB_DROP);
        try {
            MeterBand mb = MeterBandFactory.parseMeterBand(pkt, V_1_3);
            print(mb);

            // see sampleMeterBandDropV3.hex for expected values
            verifyHeader(mb, MeterBandType.DROP, MeterBandDrop.class,
                    EXP_RATE, EXP_BURST);

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }


    @Test
    public void meterBandDscp03() {
        print(EOL + "meterBandDscp03");
        OfPacketReader pkt = getPacketReader(MB_DSCP);

        try {
            MeterBand mb = MeterBandFactory.parseMeterBand(pkt, V_1_3);
            print(mb);

            // see sampleMeterBandDscpV3.hex for expected values
            verifyHeader(mb, MeterBandType.DSCP_REMARK,
                    MeterBandDscpRemark.class, EXP_RATE, EXP_BURST);
            MeterBandDscpRemark mbi = (MeterBandDscpRemark) mb;
            assertEquals(AM_NEQ, 4, mbi.getPrecLevel());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }


    @Test
    public void meterBandExpr03() {
        print(EOL + "meterBandExpr03");
        OfPacketReader pkt = getPacketReader(MB_EXPR);

        try {
            MeterBand mb = MeterBandFactory.parseMeterBand(pkt, V_1_3);
            print(mb);

            // see sampleMeterBandExprV3.hex for expected values
            verifyHeader(mb, MeterBandType.EXPERIMENTER,
                    MeterBandExperimenter.class, EXP_RATE, EXP_BURST);
            MeterBandExperimenter mbi = (MeterBandExperimenter) mb;
            assertEquals(AM_NEQ, ExperimenterId.NICIRA.encodedId(), mbi.getId());
            assertEquals(AM_NEQ, ExperimenterId.NICIRA, mbi.getExpId());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

}
