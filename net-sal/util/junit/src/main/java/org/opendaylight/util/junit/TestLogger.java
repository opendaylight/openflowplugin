/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.*;

/**
 * A logger class that can be used in unit tests to assert that messages
 * were written to the log. This class extends the {@link LoggerAdapter}
 * and overrides the commonly used <em>info</em>, <em>warn</em>, and
 * <em>error</em> methods.
 * <p>
 * The last message (and throwable, where applicable)
 * for each severity are stored temporarily so that an assertion can be made
 * to ensure that the expected message (and throwable) were logged. Note that
 * the assertions are "destructive-reads", resetting the given last message
 * (or throwable) back to null.
 *
 * @author Simon Hunt
 */
public class TestLogger extends LoggerAdapter {
    private static final String TEST_LOGGER_FMT = "<TestLogger> [{}] {}";
    private static final String TEST_LOGGER_T_FMT = TEST_LOGGER_FMT + ": {}";

    private static final String E_NO_MSG_LOGGED = "No message logged";
    private static final String E_NOT_SUBSTRING = "Not a substring of msg";


    private static enum Severity { INFO, WARN, ERROR }

    private Map<Severity, String> msgs = new HashMap<>();
    private Map<Severity, Throwable> throwables = new HashMap<>();

    private String presentMsg(Severity sev, boolean present) {
        // remember, assertion message expressed as opposite of condition..
        return sev.name() + (present ? "NOT " : "") + "present!";
    }

    /**
     * Asserts that an INFO message has (or has not) been logged.
     * This method is a NON-destructive read.
     *
     * @param present true to test for presence; false to test for absence
     */
    public void assertInfo(boolean present) {
        String s = presentMsg(Severity.INFO, present);
        assertEquals(s, present, msgs.containsKey(Severity.INFO));
    }

    /**
     * Asserts that a WARNING message has (or has not) been logged.
     * This method is a NON-destructive read.
     *
     * @param present true to test for presence; false to test for absence
     */
    public void assertWarning(boolean present) {
        String s = presentMsg(Severity.WARN, present);
        assertEquals(s, present, msgs.containsKey(Severity.WARN));
    }

    /**
     * Asserts that an ERROR message has (or has not) been logged.
     * This method is a NON-destructive read.
     *
     * @param present true to test for presence; false to test for absence
     */
    public void assertError(boolean present) {
        String s = presentMsg(Severity.ERROR, present);
        assertEquals(s, present, msgs.containsKey(Severity.ERROR));
    }

    /**
     * Asserts the last INFO message logged.
     * This method performs a destructive-read.
     *
     * @param expected the expected message
     */
    public void assertInfo(String expected) {
        Assert.assertEquals(TestTools.AM_NEQ, expected, msgs.remove(Severity.INFO));
    }

    /**
     * Asserts the last INFO message logged contains the given substring.
     * This method performs a destructive-read.
     *
     * @param substring the expected substring
     */
    public void assertInfoContains(String substring) {
        assertContains(Severity.INFO, substring);
    }

    /**
     * Asserts the last INFO throwable logged.
     * This method performs a destructive-read.
     *
     * @param t the expected throwable
     */
    public void assertInfoThrowable(Throwable t) {
        Assert.assertEquals(TestTools.AM_NEQ, t, throwables.remove(Severity.INFO));
    }

    /**
     * Asserts the last WARNING message logged.
     * This method performs a destructive-read.
     *
     * @param expected the expected message
     */
    public void assertWarning(String expected) {
        Assert.assertEquals(TestTools.AM_NEQ, expected, msgs.remove(Severity.WARN));
    }

    /**
     * Asserts the last WARNING message logged contains the given substring.
     * This method performs a destructive-read.
     *
     * @param substring the expected substring
     */
    public void assertWarningContains(String substring) {
        assertContains(Severity.WARN, substring);
    }

    /**
     * Asserts the last WARNING throwable logged.
     * This method performs a destructive-read.
     *
     * @param t the expected throwable
     */
    public void assertWarningThrowable(Throwable t) {
        Assert.assertEquals(TestTools.AM_NEQ, t, throwables.remove(Severity.WARN));
    }

    /**
     * Asserts the last ERROR message logged.
     * This method performs a destructive-read.
     *
     * @param expected the expected message
     */
    public void assertError(String expected) {
        Assert.assertEquals(TestTools.AM_NEQ, expected, msgs.remove(Severity.ERROR));
    }

    /**
     * Asserts the last ERROR message logged contains the given substring.
     * This method performs a destructive-read.
     *
     * @param substring the expected substring
     */
    public void assertErrorContains(String substring) {
        assertContains(Severity.ERROR, substring);
    }

    /**
     * Asserts the last ERROR throwable logged.
     * This method performs a destructive-read.
     *
     * @param t the expected throwable
     */
    public void assertErrorThrowable(Throwable t) {
        Assert.assertEquals(TestTools.AM_NEQ, t, throwables.remove(Severity.ERROR));
    }

    @Override
    public void info(String s) {
        log(Severity.INFO, format(s));
    }

    @Override
    public void info(String s, Object o) {
        log(Severity.INFO, format(s, o));
    }

    @Override
    public void info(String s, Object o, Object o1) {
        log(Severity.INFO, format(s, o, o1));
    }

    @Override
    public void info(String s, Object... objects) {
        log(Severity.INFO, format(s, objects));
    }

    @Override
    public void info(String s, Throwable throwable) {
        log(Severity.INFO, s, throwable);
    }

    @Override
    public void warn(String s) {
        log(Severity.WARN, format(s));
    }

    @Override
    public void warn(String s, Object o) {
        log(Severity.WARN, format(s, o));
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        log(Severity.WARN, format(s, o, o1));
    }

    @Override
    public void warn(String s, Object... objects) {
        log(Severity.WARN, format(s, objects));
    }

    @Override
    public void warn(String s, Throwable throwable) {
        log(Severity.WARN, s, throwable);
    }

    @Override
    public void error(String s) {
        log(Severity.ERROR, format(s));
    }

    @Override
    public void error(String s, Object o) {
        log(Severity.ERROR, format(s, o));
    }

    @Override
    public void error(String s, Object o, Object o1) {
        log(Severity.ERROR, format(s, o, o1));
    }

    @Override
    public void error(String s, Object... objects) {
        log(Severity.ERROR, format(s, objects));
    }

    @Override
    public void error(String s, Throwable throwable) {
        log(Severity.ERROR, s, throwable);
    }


    private void assertContains(Severity sev, String substring) {
        String msg = msgs.remove(sev);
        if (msg == null)
            fail(E_NO_MSG_LOGGED);
        assertTrue(E_NOT_SUBSTRING, msg.contains(substring));
    }

    private void log(Severity sev, String msg) {
        msgs.put(sev, msg);
        TestTools.print(TEST_LOGGER_FMT, sev, msg);
    }

    private void log(Severity sev, String msg, Throwable throwable) {
        msgs.put(sev, msg);
        throwables.put(sev, throwable);
        TestTools.print(TEST_LOGGER_T_FMT, sev, msg, throwable);
    }

    private static String format(String fmt, Object... o) {
        if (fmt == null)
            throw new NullPointerException("null format string");
        if (o.length == 0)
            return fmt;

        // Format the message using the format string as the seed.
        // Stop either when the list of objects is exhausted or when
        // there are no other place-holder tokens.
        final int ftlen = FORMAT_TOKEN.length();
        int i = 0;
        int p = -1;
        String rep;
        StringBuilder sb = new StringBuilder(fmt);
        while (i < o.length && (p = sb.indexOf(FORMAT_TOKEN, p + 1)) >= 0) {
            rep = o[i] == null ? NULL_REP : o[i].toString();
            sb.replace(p, p + ftlen, rep);
            i++;
        }
        return sb.toString();
    }

    private static final String FORMAT_TOKEN = "{}";
    private static final String NULL_REP = "{null}";
}
