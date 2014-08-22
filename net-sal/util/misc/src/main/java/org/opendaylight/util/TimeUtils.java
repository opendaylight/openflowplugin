/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.TimeZone;

import static org.opendaylight.util.StringUtils.zeroFill;

/**
 * Provides utilities dealing with timestamps and time intervals.
 * <p>
 * This class has been designed with unit tests that need to make
 * assertions about times relative to "now" in mind. It provides an
 * interface that defines the notion of "{@link Now now}". Every instance
 * of {@code TimeUtils} must provide an implementation of that interface.
 * The {@link #getInstance() default} instance has an implementation
 * that always returns {@link System#currentTimeMillis()}.
 *
 * @see org.opendaylight.util.test.FakeTimeUtils
 *
 * @author Steve Britt
 * @author Simon Hunt
 * @author Frank Wood
 */
public final class TimeUtils {

    private static final String UTC = "UTC";
    // some error messages
    private static final String E_NEG_INTERVAL = "Negative time interval: ";
    private static final String E_NULL_PARAM = "Null parameter";
    private static final String E_NOT_YYYYMMDD = "Invalid (not YYYY-MM-DD): ";

    /** The number of hours in a day. */
    public static final long HOURS_PER_DAY = 24L;
    /** The number of minutes in an hour. */
    public static final long MINUTES_PER_HOUR = 60L;
    /** The number of seconds in a minute. */
    public static final long SECONDS_PER_MINUTE = 60L;
    /** The number of milliseconds in a second. */
    public static final long MILLIS_PER_SECOND = 1000L;
    /** The number of milliseconds in a hundredth of a second. */
    public static final long HUNDREDTHS_PER_MILLIS = 10L;
    /** The number of milliseconds in a minute. */
    public static final long MILLIS_PER_MINUTE =
            MILLIS_PER_SECOND * SECONDS_PER_MINUTE;
    /** The number of milliseconds in an hour. */
    public static final long MILLIS_PER_HOUR =
            MILLIS_PER_MINUTE * MINUTES_PER_HOUR;
    /** The number of milliseconds in a day. */
    public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * HOURS_PER_DAY;

    /** Regular expression that matches YYYY-MM-DD from 2000 onwards. */
    private static final String RE_DATE =
            "20[0-9]{2}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])";

    private static final String DASH = "-";
    private static final String COLON = ":";
    private static final String DOT = ".";

    /** Used a way to determine when the JVM started up. */
    private static final long START_MILLIS =
            ManagementFactory.getRuntimeMXBean().getStartTime();

    /** Resource bundle for localization. */
    private static final ResourceBundle res =
            ResourceUtils.getBundledResource(TimeUtils.class);


    /** Default Now implementation returns System.currentTimeMillis(). */
    private static final Now SYSTEM_CURRENT_TIME_MILLIS = new Now() {
        @Override public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    };

    /** Current notion of Now. */
    private final Now now;

    // No instantiation except here.
    private TimeUtils(Now now) {
        this.now = now;
    }

    /** Default implementation always uses System.currentTimeMillis(). */
    private static final TimeUtils DEFAULT =
            new TimeUtils(SYSTEM_CURRENT_TIME_MILLIS);

    /**
     * Returns the default instance.
     *
     * @return the default TimeUtils instance
     */
    public static TimeUtils getInstance() {
        return DEFAULT;
    }

    /**
     * Returns a specialized instance that has an alternate implementation
     * of {@link Now}. This is designed for use by unit tests that are testing
     * time-based methods and that want to provide a fully deterministic
     * notion of "now".
     *
     * @param altNow the alternative implementation of Now
     * @return a specialized TimeUtils instance
     */
    public static TimeUtils getInstance(Now altNow) {
        return new TimeUtils(altNow);
    }

    //=======================================================================
    // === Methods that are a function of "now"

    /**
     * Equivalent to invoking {@link System#currentTimeMillis()}.
     *
     * @return the current time in milliseconds (epoch time)
     */
    public long currentTimeMillis() {
        return now.currentTimeMillis();
    }

    /**
     * Returns the current time in hundredths of a second.
     *
     * @return the current time in hundredths (epoch time)
     */
    public long currentTimeHundredths() {
        return now.currentTimeMillis() / HUNDREDTHS_PER_MILLIS;
    }

    /**
     * Returns the current time in seconds.
     *
     * @return the current time in seconds (epoch time)
     */
    public long currentTimeSecs() {
        return now.currentTimeMillis() / MILLIS_PER_SECOND;
    }

    /**
     * Returns the number of milliseconds since the JVM started up.
     *
     * @return the elapsed milliseconds since the JVM started
     */
    public long upTimeMillis() {
        return currentTimeMillis() - START_MILLIS;
    }

    /**
     * Returns the number of hundredths of seconds since the JVM started up.
     *
     * @return the elapsed hundredths since the JVM started
     */
    public long upTimeHundredths() {
        return upTimeMillis() / HUNDREDTHS_PER_MILLIS;
    }

    /**
     * Returns the number of seconds since the JVM started up.
     *
     * @return the elapsed seconds since the JVM started
     */
    public long upTimeSecs() {
        return upTimeMillis() / MILLIS_PER_SECOND;
    }

    /**
     * Returns the number of milliseconds that have elapsed since the
     * specified date.
     *
     * @param date the date to examine
     * @return the number of milliseconds since that date
     */
    public long millisSince(Date date) {
        return now.currentTimeMillis() - date.getTime();
    }

    /**
     * Returns the given date as a <b>time</b> if it occurred within the
     * last 24 hours, or as a <b>date</b> if it occurred more
     * than 24 hours ago. Uses either {@link DateFormat#getTimeInstance} or
     * {@link DateFormat#getDateInstance} as appropriate.
     *
     * @param date the date to format
     * @return the formatted date
     */
    public String formatDateOrTime(Date date) {
        DateFormat df = (millisSince(date) > MILLIS_PER_DAY)
                ? DateFormat.getDateInstance() : DateFormat.getTimeInstance();
        return df.format(date);
    }

    /**
     * Returns the date timestamp of a number of hours prior to now.
     *
     * @param numHours the number of hours before now
     * @return the date for the specified number of hours ago
     */
    public Date hoursAgo(int numHours) {
        return new Date(now.currentTimeMillis() - numHours * MILLIS_PER_HOUR);
    }

    /**
     *  Returns the timestamp of a number of days prior to now.
     *
     * @param numDays the number of days before now
     * @return the date for the specified number of days ago
     */
    public Date daysAgo(int numDays) {
        return new Date(now.currentTimeMillis() - numDays * MILLIS_PER_DAY);
    }



    //=======================================================================
    // === Methods independent of "now"

    /**
     * Returns the date timestamp of a number of hours prior to the given date.
     *
     * @param when the date
     * @param numHours the number of hours earlier
     * @return the date for the specified number of hours earlier
     */
    public Date hoursBefore(Date when, int numHours) {
        return new Date(when.getTime() - numHours * MILLIS_PER_HOUR);
    }

    /**
     * Returns the timestamp of a number of days prior to the given date.
     *
     * @param when the date
     * @param numDays the number of days earlier
     * @return the date for the specified number of days earlier
     */
    public Date daysBefore(Date when, int numDays) {
        return new Date(when.getTime() - numDays * MILLIS_PER_DAY);
    }

    /**
     * Returns a human-friendly string breaking out the specified number of
     * milliseconds into a representative duration measured in some combination
     * of days, hours, minutes, and seconds.  Only two terms are included in
     * the returned string so what is returned depends upon the duration
     * specified.
     * <p>
     * For example if days is nonzero then a day and hour count
     * will be returned, whereas if days is zero but hours isn't then an hour
     * and minute count will be returned; if days and hours are both zero then
     * a minute and second count will be returned.
     *
     * @param ms time interval in milliseconds
     * @return formatted string representing time interval
     * @throws IllegalArgumentException if {@code ms} is negative
     */
    public String formatInterval(long ms) {
        return formatInterval(ms, true);
    }

    /**
     * Returns a human-friendly string breaking out the specified number of
     * milliseconds into a representative duration measured in some combination
     * of days, hours, minutes, and seconds.  Only two terms are included in
     * the returned string so what is returned depends upon the duration
     * specified.
     * <p>
     * For example if days is nonzero then a day and hour count
     * will be returned, whereas if days is zero but hours isn't then an hour
     * and minute count will be returned; if days and hours are both zero then
     * a minute and second count will be returned.
     * <p>
     * If {@code longFormat} is {@code true}, the returned string will spell
     * out the time element units in full, whereas if {@code false}, the
     * returned string will use abbreviated units.
     *
     * @param ms time interval in milliseconds
     * @param longFormat {@code true} spells out time element units;
     *                   {@code false} uses abbreviated units
     * @return a formatted string representing the time interval
     * @throws IllegalArgumentException if {@code ms} is negative
     */
    public String formatInterval(long ms, boolean longFormat) {
        // Validate the interval.
        if (ms < 0)
            throw new IllegalArgumentException(E_NEG_INTERVAL + ms);

        // Convert the interval to seconds and compute the "leftover" seconds
        // that don't fall into an even minute.
        long timeLeft = ms / MILLIS_PER_SECOND;
        long seconds = timeLeft % SECONDS_PER_MINUTE;

        // Convert the interval to minutes and compute the "leftover" minutes
        // that don't fall into an even hour.
        timeLeft /= SECONDS_PER_MINUTE;
        long minutes = timeLeft % MINUTES_PER_HOUR;

        // Convert the interval to hours and compute the "leftover" hours that
        // don't fall into an even day.
        timeLeft /= MINUTES_PER_HOUR;
        long hours = timeLeft % HOURS_PER_DAY;

        // Compute the number of full days.
        long days = timeLeft / HOURS_PER_DAY;
        return formatInterval(days, hours, minutes, seconds, longFormat);
    }

    private static final String ABBREV = "abbrev";
    private static final String ELAPSED = "elapsed";
    private static final String UNIT_DAY = "Day";
    private static final String UNIT_HOUR = "Hour";
    private static final String UNIT_MINUNTE = "Minute";
    private static final String UNIT_SECOND = "Second";
    private static final String SINGULAR = "";
    private static final String PLURAL = "s";

    /**
     * Returns a human-friendly string embedding the specified days, hours,
     * minutes, and seconds.
     *
     * @param days whole days of interval
     * @param hours whole hours of interval outside day count
     * @param minutes whole minutes of interval outside hour count
     * @param seconds whole seconds of interval outside minute count
     * @param longFormat when true causes returned string to spell out time
     *                   element units; when false time element units are
     *                   abbreviated
     * @return formatted string representing interval
     */
    private String formatInterval(long days, long hours, long minutes,
                                         long seconds, boolean longFormat) {
        long major;
        long minor;
        String majorUnit;
        String minorUnit;
        String majorS;
        String minorS;

        if (days > 0) {
            major = days;
            majorUnit = UNIT_DAY;
            minor = hours;
            minorUnit = UNIT_HOUR;
        } else if (hours > 0) {
            major = hours;
            majorUnit = UNIT_HOUR;
            minor = minutes;
            minorUnit = UNIT_MINUNTE;
        } else {
            major = minutes;
            majorUnit = UNIT_MINUNTE;
            minor = seconds;
            minorUnit = UNIT_SECOND;
        }
        majorS = major != 1 ? PLURAL : SINGULAR;
        minorS = minor != 1 ? PLURAL : SINGULAR;
        String key = (longFormat ? ELAPSED : ABBREV) + majorUnit + minorUnit;
        String fmt = res.getString(key);
        return longFormat
                ? StringUtils.format(fmt, major, majorS, minor, minorS)
                : StringUtils.format(fmt, major, minor);
    }

    /**
     * Convenience method that will create a {@link java.util.Date} instance
     * corresponding to the date specified in the given string.
     * The string must take the form {@code "YYYY-MM-DD"} and must be the
     * year 2000 or later.
     *
     * @param s the date string
     * @return a corresponding date instance
     */
    public Date makeDate(String s) {
        if (s==null)
            throw new NullPointerException(E_NULL_PARAM);
        if (!s.matches(RE_DATE))
            throw new IllegalArgumentException(E_NOT_YYYYMMDD + s);

        String[] pieces = s.split(DASH);
        int year = Integer.valueOf(pieces[0]);
        int month = Integer.valueOf(pieces[1]) - 1; // 0-based month (0..11)
        int day = Integer.valueOf(pieces[2]);
        return new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
    }

    /**
     * Returns a string representation of the time now, in the form
     * {@code "hh:mm:ss"}.
     *
     * @return the formatted time stamp
     */
    public String hhmmss() {
        return formatEpoch(currentTimeMillis(), false);
    }

    /**
     * Returns a string representation of the given timestamp, in the form
     * {@code "hh:mm:ss"}.
     *
     * @param epochMs epoch time stamp
     * @return the formatted time stamp
     */
    public String hhmmss(long epochMs) {
        return formatEpoch(epochMs, false);
    }

    /**
     * Returns a string representation of the time now, in the form
     * {@code "hh:mm:ss.nnn"}.
     *
     * @return the formatted time stamp
     */
    public String hhmmssnnn() {
        return formatEpoch(currentTimeMillis(), true);
    }

    /**
     * Returns a string representation of the given timestamp, in the form
     * {@code "hh:mm:ss.nnn"}.
     *
     * @param epochMs the epoch time stamp
     * @return the formatted time stamp
     */
    public String hhmmssnnn(long epochMs) {
        return formatEpoch(epochMs, true);
    }

    /**
     * Formats an epoch timestamp.
     *
     * @param epochMs the timestamp in millis
     * @param includeMs include millis in the result
     * @return a formatted timestamp
     */
    private String formatEpoch(long epochMs, boolean includeMs) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(epochMs);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        StringBuilder sb = new StringBuilder();
        sb.append(zeroFill(hour, 2)).append(COLON)
                .append(zeroFill(min, 2)).append(COLON)
                .append(zeroFill(sec, 2));
        if (includeMs)
            sb.append(DOT).append(zeroFill(cal.get(Calendar.MILLISECOND), 3));
        return sb.toString();
    }

    // ===== rfc822Timestamp =================================================

    private static final String RFC_822_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private static final long ZERO_BC_MILLIS = -62135769600000L;

    /**
     * Converts a Date into an RFC-822 UTC timestamp String.
     * <p>
     * Dates prior to 1 AD (which is the same as 0 BC) are not supported and
     * will result in an IllegalArgumentException.
     * 
     * @param date a Date object to be converted
     * @return an RFC-822 UTC formated timestamp String.
     * @throws IllegalArgumentException if a date before 0 BC (or 1 AD) is
     *         passed
     */
    public static String rfc822Timestamp(Date date) {
        if (date.getTime() < ZERO_BC_MILLIS)
            throw new IllegalArgumentException("date must be greater than zero BC");
        SimpleDateFormat sdf = new SimpleDateFormat(RFC_822_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(UTC));
        return sdf.format(date);
    }

    /**
     * Converts a long representing an offset in milliseconds from the "epoch"
     * into an RFC-822 UTC timestamp String.
     * <p>
     * Dates prior to 1 AD (which is the same as 0 BC) are not supported and
     * will result in an IllegalArgumentException.
     * 
     * @param timeInMillis a long representing an offset in milliseconds from
     *        the "epoch"
     * @return an RFC-822 UTC formated timestamp String.
     * @throws IllegalArgumentException if a time before 0 BC (or 1 AD) is
     *         passed
     */
    public static String rfc822Timestamp(long timeInMillis) {
        if (timeInMillis < ZERO_BC_MILLIS)
            throw new IllegalArgumentException(
                    "timeInMillis must be greater than zero BC");
        return rfc822Timestamp(new Date(timeInMillis));
    }

    /**
     * Converts an RFC-822 UTC timestamp String into a Date.
     * <p>
     * Given the RFC-822 format, it is not possible to pass a date prior to 1
     * AD (which is the same as 0 BC).
     * 
     * @param timestamp an RFC-822 UTC timestamp String to be converted
     * @return a Date object representing the given RFC-822 UTC formated
     *         timestamp String.
     * @throws ParseException if the <code>date</code> string is not a valid
     *         RFC-822 UTC timestamp
     */
    public static Date rfc822Timestamp(String timestamp) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(RFC_822_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(UTC));
        return sdf.parse(timestamp);
    }

    // =======================================================================

    /** Defines the notion of "now" as expressed by the implementation
     * of {@link #currentTimeMillis()}.
     */
    public static interface Now {
        /** Returns the number of milliseconds since epoch.
         * <p>
         * Note that the {@link TimeUtils#getInstance() default} (production)
         * instance has an implementation that returns
         * {@link System#currentTimeMillis()};
         * <p>
         * Alternate implementations may provide more control over the
         * values returned, to allow for deterministic unit tests.
         *
         * @return epoch time stamp
         */
        public long currentTimeMillis();
    }

}
