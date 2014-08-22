/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.junit.Test;
import org.opendaylight.of.lib.msg.PortConfig;
import org.opendaylight.of.lib.msg.PortFeature;
import org.opendaylight.of.lib.msg.SupportedAction;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.msg.PortConfig.NO_FLOOD;
import static org.opendaylight.of.lib.msg.PortConfig.NO_FWD;
import static org.opendaylight.of.lib.msg.PortFeature.*;
import static org.opendaylight.of.lib.msg.SupportedAction.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for CfgFeat.
 *
 * @author Simon Hunt
 */
public class CfgFeatTest {
    private static final int N_PORTS = 12;
    private static final Set<SupportedAction> SUPP_ACTS =
            new HashSet<SupportedAction>(asList(OUTPUT, SET_NW_SRC, SET_NW_DST));
    private static final Set<PortConfig> PORT_CONF =
            new HashSet<PortConfig>(asList(NO_FWD, NO_FLOOD));
    private static final Set<PortFeature> PORT_FEAT =
            new HashSet<PortFeature>(asList(RATE_10MB_FD, COPPER));

    private CfgFeat feat;

    @Test(expected = NullPointerException.class)
    public void confNull() {
        new CfgFeat().setPortConfig(null);
    }

    @Test(expected = NullPointerException.class)
    public void featNull() {
        new CfgFeat().setPortFeatures(null);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        feat = new CfgFeat();
        feat.setSuppActs(SUPP_ACTS);
        feat.setPortCount(N_PORTS);
        feat.setPortConfig(PORT_CONF);
        feat.setPortFeatures(PORT_FEAT);
        assertEquals(AM_NEQ, SUPP_ACTS, feat.getSuppActs());
        assertEquals(AM_NEQ, N_PORTS, feat.getPortCount());
        assertEquals(AM_NEQ, PORT_CONF, feat.getPortConfig());
        assertEquals(AM_NEQ, PORT_FEAT, feat.getPortFeatures());
    }

    private void checkCurrent(PortFeature rate, long result) {
        long speed =  CfgFeat.pickCurrentSpeed(rate);
        print("{} => CURR: {}", rate, speed);
        assertEquals(AM_NEQ, result, CfgFeat.pickCurrentSpeed(rate));
    }

    private void checkMax(PortFeature rate, long result) {
        long speed =  CfgFeat.pickMaxSpeed(rate);
        print("{} => MAX: {}", rate, speed);
        assertEquals(AM_NEQ, result, speed);
    }

    @Test
    public void someFakeSpeeds() {
        checkCurrent(RATE_10MB_HD, 5000);
        checkMax(RATE_10MB_HD, 10000);

        checkCurrent(RATE_100MB_FD, 50000);
        checkMax(RATE_100MB_HD, 100000);

        checkCurrent(RATE_1GB_FD, 500000);
        checkMax(RATE_1GB_FD, 1000000);

        checkCurrent(RATE_10GB_FD, 5000000);
        checkMax(RATE_10GB_FD, 10000000);

        checkCurrent(RATE_40GB_FD, 20000000);
        checkMax(RATE_40GB_FD, 40000000);

        checkCurrent(RATE_100GB_FD, 50000000);
        checkMax(RATE_100GB_FD, 100000000);

        checkCurrent(RATE_1TB_FD, 500000000);
        checkMax(RATE_1TB_FD, 1000000000);
    }
}
