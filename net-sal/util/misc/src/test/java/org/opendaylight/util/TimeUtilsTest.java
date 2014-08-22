/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.TimeUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link org.opendaylight.util.TimeUtils}.
 *
 * @author Steve Britt
 * @author Simon Hunt
 */
public class TimeUtilsTest {

    private static final long MS_3_SECS = 3000;
    private static final long MS_10_MINS = 600000;

    private static TimeUtils altTimeUtils;
    private static long stPatricksDay;
    private static Date stPaddy;

    private static Date threeSecondsToMidnight;
    private static Date tenMinutesToMidnight;
    private static Date elevenFiftyTheNightBefore;

    private long ms;
    private String result;

    @BeforeClass
    public static void beforeClass() {
        Calendar cal = Calendar.getInstance();
        cal.set(2011, Calendar.MARCH, 17, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        stPatricksDay = cal.getTime().getTime();
        stPaddy = new Date(stPatricksDay);
        print("Setting 'Now' to St. Patrick's Day, 2011");
        print(" --> " + stPaddy);
        altTimeUtils = TimeUtils.getInstance(new Now() {
            @Override
            public long currentTimeMillis() {
                return stPatricksDay;
            }
        });
        threeSecondsToMidnight = new Date(stPatricksDay - MS_3_SECS);
        tenMinutesToMidnight = new Date(stPatricksDay - MS_10_MINS);
        elevenFiftyTheNightBefore = new Date(stPatricksDay -
                                                MILLIS_PER_DAY - MS_10_MINS);
    }

    // ======================================================================
    //  Test the default TimeUtils implementation

    @Test
    public void defaultTimeUtils() {
        print(EOL + "defaultTimeUtils()");
        TimeUtils tu = TimeUtils.getInstance();
        long exp = System.currentTimeMillis();
        long act = tu.currentTimeMillis();
        print("SYS:{}  TU:{}  <diff:{}>", exp, act, exp - act);
        assertEqualsWithinTolerance(5, exp, act);
    }

    @Test
    public void variousTimeUnits() {
        print(EOL + "variousUnits()");
        TimeUtils tu = TimeUtils.getInstance();
        
        long exp = System.currentTimeMillis() / HUNDREDTHS_PER_MILLIS;
        long act = tu.currentTimeHundredths();
        print("100ths SYS:{}  TU:{}  <diff:{}>", exp, act, exp - act);
        assertEqualsWithinTolerance(5, exp, act);
        
        exp = System.currentTimeMillis() / MILLIS_PER_SECOND;
        act = tu.currentTimeSecs();
        print("Secs SYS:{}  TU:{}  <diff:{}>", exp, act, exp - act);
        assertEqualsWithinTolerance(1, exp, act);
    }
    
    @Test
    public void upTime() {
        print(EOL + "upTime()");
        TimeUtils tu = TimeUtils.getInstance();

        Task.delay(3); // make sure we have a non-zero up time
        
        long upTimeMillis = tu.upTimeMillis();
        Task.delay(100);

        assertTrue(0 < tu.upTimeMillis());
        
        long expMillis = upTimeMillis + 100;
        long expHundredths = expMillis / HUNDREDTHS_PER_MILLIS;
        long expSecs = expMillis / MILLIS_PER_SECOND;
        long actMillis = tu.upTimeMillis();
        long actHundredths = tu.upTimeHundredths();
        long actSecs = tu.upTimeSecs();
        
        print("Millis SYS:{}  TU:{}  <diff:{}>", expMillis, actMillis,
              expMillis - actMillis);
        assertEqualsWithinTolerance(30, expMillis, actMillis);
        
        print("100ths SYS:{}  TU:{}  <diff:{}>", expHundredths, actHundredths,
              expHundredths - actHundredths);
        assertEqualsWithinTolerance(3, expHundredths, actHundredths);
        
        print("Secs SYS:{}  TU:{}  <diff:{}>", expSecs, actSecs,
              expSecs - actSecs);
        assertEqualsWithinTolerance(1, expSecs, actSecs);
    }

    // ======================================================================

    @Test
    public void formatDateOrTime() {

        Locale.setDefault(Locale.US);

        print(EOL + "formatDateOrTime()");
        print(EOL + threeSecondsToMidnight);
        result = altTimeUtils.formatDateOrTime(threeSecondsToMidnight);
        print(result);
        assertEquals(AM_NEQ, "11:59:57 PM", result);

        print(EOL + tenMinutesToMidnight);
        result = altTimeUtils.formatDateOrTime(tenMinutesToMidnight);
        print(result);
        assertEquals(AM_NEQ, "11:50:00 PM", result);

        print(EOL + elevenFiftyTheNightBefore);
        result = altTimeUtils.formatDateOrTime(elevenFiftyTheNightBefore);
        print(result);
        assertEquals(AM_NEQ, "Mar 15, 2011", result);
    }

    @Test
    public void millisSince() {
        print(EOL + "millisSince()");
        ms = altTimeUtils.millisSince(threeSecondsToMidnight);
        assertEquals(AM_NEQ, MS_3_SECS, ms);
        ms = altTimeUtils.millisSince(tenMinutesToMidnight);
        assertEquals(AM_NEQ, MS_10_MINS, ms);
        ms = altTimeUtils.millisSince(elevenFiftyTheNightBefore);
        assertEquals(AM_NEQ, MILLIS_PER_DAY + MS_10_MINS, ms);
    }


    @Test(expected = IllegalArgumentException.class)
    public void negativeUptime() {
        // Test with a negative interval.
        altTimeUtils.formatInterval(-1);
    }


    private void verifyInterval(String expResult, long days, long hours,
                                long mins, long secs, long millis) {
        long intervalMs = days * MILLIS_PER_DAY +
                hours * MILLIS_PER_HOUR +
                mins * MILLIS_PER_MINUTE +
                secs * MILLIS_PER_SECOND +
                millis;

        String result = altTimeUtils.formatInterval(intervalMs);
        print("  " + result);
        assertEquals(AM_NEQ, expResult, result);
    }

    @Test
    public void formatInterval() {
        /* Implementation Note:
         *  Milliseconds will be truncated no matter how close to
         *  a complete second.  Also, any value that exceeds a "complete"
         *  unit of the next largest gradient (e.g. 100 seconds exceeds
         *  1 minute) should be properly accounted for when producing the
         *  interval string (e.g. 100 seconds would produce a string
         *  citing 1 minute and 40 seconds).
         */
        print (EOL + "formatInterval()");
        // 6 days, 23 hours, 14 minutes, 9.042 seconds
        verifyInterval("6 Days, 23 Hours", 6, 23, 14, 9, 42);
        // 1 day, 7 hours, 19 minutes, 21.001 seconds
        verifyInterval("1 Day, 7 Hours", 1, 7, 19, 21, 1);
        // 193 days, 1 hour, 3 minutes, 37.999 seconds
        verifyInterval("193 Days, 1 Hour", 193, 1, 0, 216, 1999);
         // 1 day, 1 hour, 59 minutes, 59.496 seconds
        verifyInterval("1 Day, 1 Hour", 1, 1, 59, 59, 496);
         // 11 hours, 21 minutes, 2.317 seconds
        verifyInterval("11 Hours, 21 Minutes", 0, 11, 21, 2, 317);
         // 1 hour, 37 minutes, 27.014 seconds
        verifyInterval("1 Hour, 37 Minutes", 0, 1, 37, 27, 14);
         // 3 hours, 1 minute, 44.999 seconds
        verifyInterval("3 Hours, 1 Minute", 0, 3, 1, 44, 999);
          // 1 hour, 1 minute, 53.864 seconds
        verifyInterval("1 Hour, 1 Minute", 0, 1, 1, 53, 864);
          // 5 minutes, 40.717 seconds
        verifyInterval("5 Minutes, 40 Seconds", 0, 0, 4, 100, 717);
          // 38 minutes, 1.021 seconds
        verifyInterval("38 Minutes, 1 Second", 0, 0, 38, 1, 21);
          // 1 minute, 38.646 seconds
        verifyInterval("1 Minute, 38 Seconds", 0, 0, 1, 38, 646);
          // 1 minute, 1.922 seconds
        verifyInterval("1 Minute, 1 Second", 0, 0, 1, 1, 922);
          // 1 day, 4 hours, 8 minutes, 11 seconds
        verifyInterval("1 Day, 4 Hours", 0, 27, 65, 190, 1001);
    }

    @Test
    public void hoursAgo() {
        print(EOL + "hoursAgo()");
        Date d = altTimeUtils.hoursAgo(0);
        print(d);
        assertEquals(AM_NEQ, stPaddy, d);

        d = altTimeUtils.hoursAgo(1);
        print(d);
        assertEquals(AM_VMM, stPatricksDay - MILLIS_PER_HOUR, d.getTime());

        d = altTimeUtils.hoursAgo(7);
        print(d);
        assertEquals(AM_VMM, stPatricksDay - MILLIS_PER_HOUR * 7, d.getTime());

        d = altTimeUtils.hoursAgo(-4);
        print(d);
        assertEquals(AM_VMM, stPatricksDay + MILLIS_PER_HOUR * 4, d.getTime());
    }

    @Test
    public void daysAgo() {
        print(EOL + "daysAgo()");
        Date d = altTimeUtils.daysAgo(0);
        print(d);
        assertEquals(AM_NEQ, stPaddy, d);

        d = altTimeUtils.daysAgo(1);
        print(d);
        assertEquals(AM_VMM, stPatricksDay - MILLIS_PER_DAY, d.getTime());

        d = altTimeUtils.daysAgo(7);
        print(d);
        assertEquals(AM_VMM, stPatricksDay - MILLIS_PER_DAY * 7, d.getTime());

        d = altTimeUtils.daysAgo(-4);
        print(d);
        assertEquals(AM_VMM, stPatricksDay + MILLIS_PER_DAY * 4, d.getTime());
    }

    @Test
    public void hoursBefore() {
        print(EOL + "hoursBefore()");
        Date d = altTimeUtils.hoursBefore(elevenFiftyTheNightBefore, 0);
        print(d);
        assertEquals(AM_VMM, elevenFiftyTheNightBefore, d);

        d = altTimeUtils.hoursBefore(elevenFiftyTheNightBefore, 1);
        print(d);
        assertEquals(AM_VMM, elevenFiftyTheNightBefore.getTime() -
                MILLIS_PER_HOUR, d.getTime());

        d = altTimeUtils.hoursBefore(elevenFiftyTheNightBefore, 7);
        print(d);
        assertEquals(AM_VMM, elevenFiftyTheNightBefore.getTime() -
                MILLIS_PER_HOUR * 7, d.getTime());

        d = altTimeUtils.hoursBefore(elevenFiftyTheNightBefore, -4);
        print(d);
        assertEquals(AM_VMM, elevenFiftyTheNightBefore.getTime() +
                MILLIS_PER_HOUR * 4, d.getTime());
    }

    @Test
    public void daysBefore() {
        print(EOL + "daysBefore()");
        Date d = altTimeUtils.daysBefore(elevenFiftyTheNightBefore, 0);
        print(d);
        assertEquals(AM_VMM, elevenFiftyTheNightBefore, d);

        d = altTimeUtils.daysBefore(elevenFiftyTheNightBefore, 1);
        print(d);
        assertEquals(AM_VMM, elevenFiftyTheNightBefore.getTime() -
                MILLIS_PER_DAY, d.getTime());

        d = altTimeUtils.daysBefore(elevenFiftyTheNightBefore, 7);
        print(d);
        assertEquals(AM_VMM, elevenFiftyTheNightBefore.getTime() -
                MILLIS_PER_DAY * 7, d.getTime());

        d = altTimeUtils.daysBefore(elevenFiftyTheNightBefore, -4);
        print(d);
        assertEquals(AM_VMM, elevenFiftyTheNightBefore.getTime() +
                MILLIS_PER_DAY * 4, d.getTime());
    }

    @Test(expected = NullPointerException.class)
    public void makeDateNull() {
        altTimeUtils.makeDate(null);
    }

    private static final String[] BAD_DATE_FORMATS = {
            "",
            "1234",
            "6-Jun-2010",
            "1999-05-20", // year has to be 2000 or greater
            "2010-13-01",
            "2010-12-32",
    };

    @Test
    public void makeDateBadFormats() {
        print(EOL + "makeDateBadFormats()");
        for (String s: BAD_DATE_FORMATS) {
            try {
                altTimeUtils.makeDate(s);
                fail(AM_NOEX);
            } catch (IllegalArgumentException iae) {
                print("EX> {}", iae);
            } catch (Exception e) {
                print(e);
                fail(AM_WREX);
            }
        }
    }

    private static final String[] GOOD_DATES = {
            "2010-01-01",
            "2000-12-31",
            "2064-06-24", // my 100th birthday
    };

    @Test
    public void makeDateGoodFormats() {
        print (EOL + "makeDateGoodFormats()");
        for (String s: GOOD_DATES) {
            try {
                Date d = altTimeUtils.makeDate(s);
                print("  {} => {}", s, d);
            } catch (Exception e) {
                print(e);
                fail(AM_UNEX);
            }
        }
    }

    private void validateHhmmss(String label, Date d, String exp) {
        String s = altTimeUtils.hhmmss(d.getTime());
        print("{}: {}", label, s);
        assertEquals(AM_NEQ, exp, s);
    }

    @Test
    public void hhmmss() {
        print(EOL + "hhmmss()");
        validateHhmmss("3s to midnight", threeSecondsToMidnight, "23:59:57");
        validateHhmmss("10m to midnight", tenMinutesToMidnight, "23:50:00");
    }

    private void validateHhmmssnnn(String label, Date d, long delta,
                                   String exp) {
        long ts = d.getTime() + delta;
        String s = altTimeUtils.hhmmssnnn(ts);
        print("{}: {}", label, s);
        assertEquals(AM_NEQ, exp, s);
    }

    @Test
    public void hhmmssnnn() {
        print(EOL + "hhmmssnnn()");
        validateHhmmssnnn("3s to midnight",
                threeSecondsToMidnight, 24, "23:59:57.024");
        validateHhmmssnnn("10m to midnight",
                tenMinutesToMidnight, 7, "23:50:00.007");
    }

    private static final String BAD_STRING_FOO = "foo";
    private static final String BAD_STRING_FORMAT = "Fri Dec 31 15:59:59 PST 1";
    private static final long BEFORE_BC_LONG = -62135769600001L;
    private static final Date BEFORE_BC_DATE = new Date(BEFORE_BC_LONG);
    private static final String BEFORE_EPOCH_STRING = "1926-06-16T02:47:48.660Z";
    private static final long BEFORE_EPOCH_LONG = -1374181931340L;
    private static final Date BEFORE_EPOCH_DATE = new Date(BEFORE_EPOCH_LONG);
    private static final String EPOCH_STRING = "1970-01-01T00:00:00.000Z";
    private static final long EPOCH_LONG = 0L;
    private static final Date EPOCH_DATE = new Date(EPOCH_LONG);
    private static final String AFTER_EPOCH_STRING = "2013-07-18T21:12:11.340Z";
    private static final long AFTER_EPOCH_LONG = 1374181931340L;
    private static final Date AFTER_EPOCH_DATE = new Date(AFTER_EPOCH_LONG);

    @Test
    public void testRfc822TimestampStringFromDate() {
        assertEquals("before epoch", BEFORE_EPOCH_STRING,
                     TimeUtils.rfc822Timestamp(BEFORE_EPOCH_DATE));
        assertEquals("on epoch", EPOCH_STRING,
                     TimeUtils.rfc822Timestamp(EPOCH_DATE));
        assertEquals("after epoch", AFTER_EPOCH_STRING,
                     TimeUtils.rfc822Timestamp(AFTER_EPOCH_DATE));
    }

    @Test(expected = NullPointerException.class)
    public void testRfc822TimestampStringFromNullDate() {
        Date d = null;
        TimeUtils.rfc822Timestamp(d);
        fail("Should have thrown NullPointerException");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRfc822TimestampStringFromBcDate() {
        TimeUtils.rfc822Timestamp(BEFORE_BC_DATE);
        fail("Should have thrown IllegalArgumentException");
    }

    @Test
    public void testRfc822TimestampStringFromLong() {
        assertEquals("before epoch", BEFORE_EPOCH_STRING,
                     TimeUtils.rfc822Timestamp(BEFORE_EPOCH_LONG));
        assertEquals("on epoch", EPOCH_STRING,
                     TimeUtils.rfc822Timestamp(EPOCH_LONG));
        assertEquals("after epoch", AFTER_EPOCH_STRING,
                     TimeUtils.rfc822Timestamp(AFTER_EPOCH_LONG));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRfc822TimestampStringFromBcLong() {
        TimeUtils.rfc822Timestamp(BEFORE_BC_LONG);
        fail("Should have thrown IllegalArgumentException");
    }

    @Test
    public void testRfc822TimestampDateFromString() throws ParseException {
        assertEquals("before epoch", BEFORE_EPOCH_DATE,
                     TimeUtils.rfc822Timestamp(BEFORE_EPOCH_STRING));
        assertEquals("on epoch", EPOCH_DATE,
                     TimeUtils.rfc822Timestamp(EPOCH_STRING));
        assertEquals("after epoch", AFTER_EPOCH_DATE,
                     TimeUtils.rfc822Timestamp(AFTER_EPOCH_STRING));
    }

    @Test(expected = ParseException.class)
    public void testRfc822TimestampDateFromBadStringFoo() throws ParseException {
        TimeUtils.rfc822Timestamp(BAD_STRING_FOO);
        fail("Should have thrown ParseException");
    }

    @Test(expected = ParseException.class)
    public void testRfc822TimestampDateFromBadStringFormat()
            throws ParseException {
        TimeUtils.rfc822Timestamp(BAD_STRING_FORMAT);
        fail("Should have thrown ParseException");
    }
}
