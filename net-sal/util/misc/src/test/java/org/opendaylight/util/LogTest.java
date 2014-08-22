/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.opendaylight.util.junit.TestTools;
import org.junit.Test;
import org.slf4j.Logger;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link org.opendaylight.util.Log}.
 *
 * @author Simon Hunt
 * @see LogTestSnippet
 */
public class LogTest {

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (Log l: Log.values())
            print("    {} => {}", l, l.getName());
        print(EOL);
        assertEquals(TestTools.AM_UXCC, 18, Log.values().length);
    }

    private void verifyLogger(Log l, String expName) {
        Logger log = l.getLogger();
        assertNotNull(log);
        String logName = log.getName();
        String name = l.getName();
        print("{} => {}", l, name);
        assertEquals(AM_NEQ, expName, name);
        assertEquals(AM_NEQ, name, logName);
    }

    @Test
    public void root() {
        verifyLogger(Log.ROOT, "hp");
    }

    @Test
    public void general() {
        verifyLogger(Log.GENERAL, "hp.general");
    }

    @Test
    public void bootstrap() {
        verifyLogger(Log.BOOTSTRAP, "hp.bootstrap");
    }

    @Test
    public void nbio() {
        verifyLogger(Log.NBIO, "hp.nbio");
    }

    @Test
    public void rs() {
        verifyLogger(Log.RS, "hp.rs");
    }

    @Test
    public void gui() {
        verifyLogger(Log.GUI, "hp.gui");
    }

    @Test
    public void ntg() {
        verifyLogger(Log.NTG, "hp.ntg");
    }

    @Test
    public void cid() {
        verifyLogger(Log.CID, "hp.cid");
    }

    @Test
    public void ejbProvider() {
        verifyLogger(Log.EJB_PROVIDER, "hp.ejbprov");
    }

    @Test
    public void snmp() {
        verifyLogger(Log.SNMP, "hp.snmp");
    }

    @Test
    public void keystone() {
        verifyLogger(Log.KEYSTONE, "hp.keystone");
    }

    @Test
    public void cass() {
        verifyLogger(Log.CASS, "hp.cass");
    }

    @Test
    public void cassConn() {
        verifyLogger(Log.CASS_CONN, "hp.cass.conn");
    }

    @Test
    public void cassWal() {
        verifyLogger(Log.CASS_WAL, "hp.cass.wal");
    }

    @Test
    public void cassWalOsd() {
        verifyLogger(Log.CASS_WAL_OSD, "hp.cass.wal.osd");
    }

    @Test
    public void cassWalGroup() {
        verifyLogger(Log.CASS_WAL_GROUP, "hp.cass.wal.grp");
    }

    @Test
    public void cassWalLog() {
        verifyLogger(Log.CASS_WAL_LOG, "hp.cass.wal.log");
    }

    @Test
    public void cassWalReplay() {
        verifyLogger(Log.CASS_WAL_REPLAY, "hp.cass.wal.replay");
    }

}
