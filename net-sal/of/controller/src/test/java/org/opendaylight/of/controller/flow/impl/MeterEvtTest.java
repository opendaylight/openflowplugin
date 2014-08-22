/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.flow.impl;

import org.junit.Test;
import org.opendaylight.of.controller.impl.AbstractTest;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.*;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.controller.flow.MeterEventType.METER_MOD_PUSHED;
import static org.opendaylight.of.controller.flow.MeterEventType.METER_MOD_PUSH_FAILED;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageType.METER_MOD;
import static org.opendaylight.of.lib.msg.MeterBandFactory.createBand;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MeterEvt.
 *
 * @author Simon Hunt
 */
public class MeterEvtTest extends AbstractTest {

    private static final DataPathId DPID = dpid("17/181920:2001ab");

    private static MeterBand makeBand(long rate, long burstSize) {
        return createBand(V_1_3, MeterBandType.DROP, rate, burstSize);
    }

    private static final MeterBand BAND1 = makeBand(1024, 240);
    private static final MeterBand BAND2 = makeBand(2048, 360);

    private static OfmMeterMod createMeterMod() {
        OfmMutableMeterMod m = (OfmMutableMeterMod)
                MessageFactory.create(V_1_3, METER_MOD, MeterModCommand.ADD);
        m.meterId(mid(37)).addBand(BAND1).addBand(BAND2);
        return (OfmMeterMod) m.toImmutable();
    }

    private static final OfmMeterMod OFM_METER_MOD = createMeterMod();

    @Test
    public void meterModPushed() {
        print(EOL + "meterModPushed()");
        MeterEvt e = new MeterEvt(METER_MOD_PUSHED, DPID, OFM_METER_MOD);
        print(e);
        assertEquals(AM_NEQ, METER_MOD_PUSHED, e.type());
        assertEquals(AM_NEQ, DPID, e.dpid());
        assertEquals(AM_NEQ, OFM_METER_MOD, e.meterMod());
        print(e.meterMod().toDebugString());
    }

    @Test
    public void meterModPushFailed() {
        print(EOL + "meterModPushFailed()");
        MeterEvt e = new MeterEvt(METER_MOD_PUSH_FAILED, DPID, OFM_METER_MOD);
        print(e);
        assertEquals(AM_NEQ, METER_MOD_PUSH_FAILED, e.type());
        assertEquals(AM_NEQ, DPID, e.dpid());
        assertEquals(AM_NEQ, OFM_METER_MOD, e.meterMod());
        print(e.meterMod().toDebugString());
    }

    // TODO: Test support for custom 1.0 meters using experimenter messages
}
