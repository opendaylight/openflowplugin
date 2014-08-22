/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.controller.Alert;
import org.opendaylight.util.TimeUtils;

import java.util.TimeZone;

import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for DefaultAlert.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public class DefaultAlertTest {
    private static final String MSG = "Some Message";

    private static final String STR_REP_PST =
            "{Wed Dec 31 16:00:00 PST 1969,INFO,\"Some Message\"}";

    private static final String STR_REP_IST =
            "{Thu Jan 01 05:30:00 IST 1970,INFO,\"Some Message\"}";

    @BeforeClass
    public static void classSetUp() {
        DefaultAlert.TIME = TimeUtils.getInstance(new TimeUtils.Now() {
            @Override public long currentTimeMillis() { return 0; }
        });
    }

    @After
    public void tearDown() {
        // reset timezone to value when VM was originally started
        TimeZone.setDefault(null);
    }

    @Test
    public void basicPST() {
        print(EOL + "basicPST()");
        TimeZone.setDefault(TimeZone.getTimeZone("PST"));
        Alert a = new DefaultAlert(Alert.Severity.INFO, MSG);
        print(a);
        Assert.assertEquals(AM_NEQ, STR_REP_PST, a.toString());
        TimeZone.setDefault(null);
    }

    @Test
    public void basicIST() {
        print(EOL + "basicIST()");
        TimeZone.setDefault(TimeZone.getTimeZone("IST"));
        Alert a = new DefaultAlert(Alert.Severity.INFO, MSG);
        print(a);
        Assert.assertEquals(AM_NEQ, STR_REP_IST, a.toString());
    }
}
