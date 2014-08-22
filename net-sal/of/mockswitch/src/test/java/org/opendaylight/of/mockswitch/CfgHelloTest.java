/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.msg.HelloElemVersionBitmap;
import org.opendaylight.of.lib.msg.HelloElement;
import org.opendaylight.of.lib.msg.OfmHello;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.mockswitch.CfgHello.Behavior.EAGER;
import static org.opendaylight.of.mockswitch.CfgHello.Behavior.LAZY;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for CfgHello.
 *
 * @author Simon Hunt
 */
public class CfgHelloTest {

    private CfgHello cfg;
    private OfmHello msg;

    @Test(expected = NullPointerException.class)
    public void oneNullArgs() {
        new CfgHello(null);
    }

    @Test(expected = NullPointerException.class)
    public void twoNullArgs() {
        new CfgHello(null, (ProtocolVersion[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noVersionsEager() {
        new CfgHello(EAGER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noVersionsLazy() {
        new CfgHello(LAZY);
    }

    private void checkLegacy(ProtocolVersion pv) {
        cfg = new CfgHello(EAGER, pv).legacy();
        msg = cfg.createHelloMsg();
        print(msg.toDebugString());
        assertEquals(AM_NEQ, pv, msg.getVersion());
        assertNull(AM_HUH, msg.getElements());
        assertEquals(AM_NEQ, EAGER, cfg.getBehavior());
    }

    @Test
    public void legacy10() {
        print(EOL + "legacy10()");
        checkLegacy(V_1_0);
    }

    @Test
    public void legacy11() {
        print(EOL + "legacy11()");
        checkLegacy(V_1_1);
    }

    @Test
    public void legacy12() {
        print(EOL + "legacy12()");
        checkLegacy(V_1_2);
    }

    @Test
    public void legacy13() {
        print(EOL + "legacy13()");
        checkLegacy(V_1_3);
    }

    private void checkVersionBitmap(ProtocolVersion... vers) {
        final ProtocolVersion expMaxVer = ProtocolVersion.max(
                new HashSet<ProtocolVersion>(Arrays.asList(vers))
        );
        final int expBitmapSize = vers.length;

        cfg = new CfgHello(LAZY, vers);
        msg = cfg.createHelloMsg();
        print(msg.toDebugString());

        assertEquals(AM_NEQ, LAZY, cfg.getBehavior());
        assertEquals(AM_NEQ, expMaxVer, msg.getVersion());
        List<HelloElement> elems = msg.getElements();
        assertNotNull(AM_HUH, elems);
        assertEquals(AM_UXS,  1, elems.size());
        HelloElement e = elems.get(0);
        assertTrue(AM_WRCL, e instanceof HelloElemVersionBitmap);
        HelloElemVersionBitmap vb = (HelloElemVersionBitmap) e;
        Set<ProtocolVersion> bitmap = vb.getSupportedVersions();
        assertEquals(AM_UXS, expBitmapSize, bitmap.size());
        for (ProtocolVersion pv: vers)
            assertTrue("missing version", bitmap.contains(pv));
    }

    @Test
    public void helloV0() {
        print(EOL + "helloV0()");
        checkVersionBitmap(V_1_0);
    }

    @Test
    public void helloV01() {
        print(EOL + "helloV01()");
        checkVersionBitmap(V_1_0, V_1_1);
    }

    @Test
    public void helloV012() {
        print(EOL + "helloV012()");
        checkVersionBitmap(V_1_0, V_1_1, V_1_2);
    }

    @Test
    public void helloV0123() {
        print(EOL + "helloV0123()");
        checkVersionBitmap(V_1_0, V_1_1, V_1_2, V_1_3);
    }

    @Test
    public void helloV03() {
        print(EOL + "helloV03()");
        checkVersionBitmap(V_1_0, V_1_3);
    }
}
