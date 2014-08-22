/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import org.junit.Before;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link org.opendaylight.util.junit.TestLogger}.
 *
 * @author Simon Hunt
 */
public class TestLoggerTest {

    private static final Throwable THROWABLE = new Throwable();

    private static final String MSG = "Some Message";
    private static final String MSG_1 = "Msg A={}";
    private static final String MSG_2 = "Msg A={}, B={}";
    private static final String MSG_3 = "Msg A={}, B={}, C={}";

    private static final int DOZEN = 12;
    private static final String FOO = "Foo";
    private static final String BAR = "Bar";

    private static final String EXP = "Some Message";
    private static final String EXP_1 = "Msg A=12";
    private static final String EXP_2 = "Msg A=12, B=Foo";
    private static final String EXP_3 = "Msg A=12, B=Foo, C=Bar";

    private static final String SUBSTRING = "B=Foo";

    private TestLogger log;

    @Before
    public void setUp() {
        log = new TestLogger();
    }

    @Test
    public void basic() {
        log.assertInfo(null);
        log.assertInfoThrowable(null);
        log.assertWarning(null);
        log.assertWarningThrowable(null);
        log.assertError(null) ;
        log.assertErrorThrowable(null);
    }

    @Test
    public void infoZero() {
        log.info(MSG);
        log.assertInfo(EXP);
        log.assertInfo(null);
    }

    @Test
    public void infoOne() {
        log.info(MSG_1, DOZEN);
        log.assertInfo(EXP_1);
        log.assertInfo(null);
    }

    @Test
    public void infoTwo() {
        log.info(MSG_2, DOZEN, FOO);
        log.assertInfo(EXP_2);
        log.assertInfo(null);
    }

    @Test
    public void infoThree() {
        log.info(MSG_3, DOZEN, FOO, BAR);
        log.assertInfo(EXP_3);
        log.assertInfo(null);
    }

    @Test
    public void infoContains() {
        log.info(MSG_3, DOZEN, FOO, BAR);
        log.assertInfoContains(SUBSTRING);
        log.assertInfo(null);
    }

    @Test
    public void failNoMessageSubstring() {
        try {
            log.assertInfoContains(SUBSTRING);
            fail(AM_NOEX);
        } catch (AssertionError e) {
            print("EX> {}", e);
            assertEquals(AM_NEQ, "No message logged", e.getMessage());
        }
    }

    @Test
    public void infoThrowable() {
        log.info(MSG, THROWABLE);
        log.assertInfo(EXP);
        log.assertInfoThrowable(THROWABLE);
        log.assertInfo(null);
        log.assertInfoThrowable(null);
    }

    @Test
    public void warnZero() {
        log.warn(MSG);
        log.assertWarning(EXP);
        log.assertWarning(null);
    }

    @Test
    public void warnOne() {
        log.warn(MSG_1, DOZEN);
        log.assertWarning(EXP_1);
        log.assertWarning(null);
    }

    @Test
    public void warnTwo() {
        log.warn(MSG_2, DOZEN, FOO);
        log.assertWarning(EXP_2);
        log.assertWarning(null);
    }

    @Test
    public void warnThree() {
        log.warn(MSG_3, DOZEN, FOO, BAR);
        log.assertWarning(EXP_3);
        log.assertWarning(null);
    }

    @Test
    public void warnContains() {
        log.warn(MSG_3, DOZEN, FOO, BAR);
        log.assertWarningContains(SUBSTRING);
        log.assertWarning(null);
    }

    @Test
    public void warnThrowable() {
        log.warn(MSG, THROWABLE);
        log.assertWarning(EXP);
        log.assertWarningThrowable(THROWABLE);
        log.assertWarning(null);
        log.assertWarningThrowable(null);
    }

    @Test
    public void errorZero() {
        log.error(MSG);
        log.assertError(EXP);
        log.assertError(null);
    }

    @Test
    public void errorOne() {
        log.error(MSG_1, DOZEN);
        log.assertError(EXP_1);
        log.assertError(null);
    }

    @Test
    public void errorTwo() {
        log.error(MSG_2, DOZEN, FOO);
        log.assertError(EXP_2);
        log.assertError(null);
    }

    @Test
    public void errorThree() {
        log.error(MSG_3, DOZEN, FOO, BAR);
        log.assertError(EXP_3);
        log.assertError(null);
    }

    @Test
    public void errorContains() {
        log.error(MSG_3, DOZEN, FOO, BAR);
        log.assertErrorContains(SUBSTRING);
        log.assertError(null);
    }

    @Test
    public void errorThrowable() {
        log.error(MSG, THROWABLE);
        log.assertError(EXP);
        log.assertErrorThrowable(THROWABLE);
        log.assertError(null);
        log.assertErrorThrowable(null);
    }

    @Test(expected = NullPointerException.class)
    public void formatNull() {
        log.info((String) null, FOO);
    }

    @Test
    public void nothingLogged() {
        log.assertInfo(false);
        log.assertWarning(false);
        log.assertError(false);
    }

    @Test
    public void infoLogged() {
        log.info(MSG_1, FOO);
        log.assertInfo(true);
        log.assertWarning(false);
        log.assertError(false);
    }

    @Test
    public void warningLogged() {
        log.warn(MSG_1, FOO);
        log.assertInfo(false);
        log.assertWarning(true);
        log.assertError(false);
    }

    @Test
    public void errorLogged() {
        log.error(MSG_1, FOO);
        log.assertInfo(false);
        log.assertWarning(false);
        log.assertError(true);
    }
}
