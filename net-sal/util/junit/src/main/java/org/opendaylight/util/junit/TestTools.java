/*
 * (c) Copyright 2009-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import static org.junit.Assert.*;

/**
 * Set of miscellaneous generic test aids.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 * @author Frank Wood
 */
public class TestTools {

    /**
     * Flag for tests to figure out whether they are running on windows.
     * Unfortunately, Windows OS makes it at times impossible to ignore.
     */
    public static final boolean ON_WINDOWS = System.getProperty("os.name")
            .contains("Windows");

    /** Flag for tests to figure out whether they are running on Linux. */
    public static final boolean ON_LINUX = System.getProperty("os.name")
            .contains("Linux");

    /**
     * Project, derived from the environment variable JOB_NAME is used for
     * optional segmentation of the test directory to make sure concurrent
     * builds do not clobber each other's data.
     */
    private static final String PROJECT = System.getenv("JOB_NAME");

    /**
     * Performance scaling factor, derived from the environment variable
     * PERF_SCALE is used for scaling down performance thresholds to be
     * appropriate for the execution environment.
     */
    private static final String SCALE_FACTOR = System.getenv("PERF_SCALE");

    // Note: Do not use "c:/tmp" as external apps will use this directory
    //       if it exists causing the scrubTestData call to fail since
    //       one or more apps may have an open file in use in c:/tmp.
    /** Standard location for test data - {@code [c:]/tmp/junit-data/}. */
    public static final String TEST_DIR =
            (ON_WINDOWS ? "c:/tmptst/" : (System.getProperty("java.io.tmpdir") + "/")
                    + (PROJECT != null ? PROJECT + "/" : "") + "junit-data/");

    /** The platform dependent line separator (new line character). */
    public static final String EOL = System.getProperty("line.separator");

    /** assert message: No exception thrown. */
    public static final String AM_NOEX = "No exception thrown";

    /** assert message: Wrong exception thrown. */
    public static final String AM_WREX = "Wrong exception thrown";

    /** assert message: Wrong exception message. */
    public static final String AM_WREXMSG = "Wrong exception message";

    /** assert message: Unexpected exception thrown. */
    public static final String AM_UNEX = "Unexpected exception thrown";

    /** assert message: Wrong class. */
    public static final String AM_WRCL = "Wrong class";

    /** assert message: Collection not empty. */
    public static final String AM_CNE = "Collection not empty";

    /** assert message: Not same reference. */
    public static final String AM_NSR = "Not same reference";

    /** assert message: Not equivalent. */
    public static final String AM_NEQ = "Not equivalent";

    /** assert message: Value mismatch. */
    public static final String AM_VMM = "Value mismatch";

    /** assert message: Arrays not different. */
    public static final String AM_AND = "Arrays not different";

    /** assert message: a not less than b. */
    public static final String AM_A_NLT_B = "a not less than b";

    /** assert message: b not greater than a. */
    public static final String AM_B_NGT_A = "b not greater than a";

    /** assert message: Unexpected sort order. */
    public static final String AM_UXSO = "Unexpected sort order";

    /** assert message: Unexpected size. */
    public static final String AM_UXS = "Unexpected size";

    /** assert message: Unexpected size. */
    public static final String AM_UXF = "Unexpected format";

    /** assert message: Unexpected constant count. */
    public static final String AM_UXCC = "Unexpected constant count";

    /** assert message: Unexpected object type. */
    public static final String AM_UXOT = "Unexpected object type";

    /** assert message: Out of bounds. */
    public static final String AM_OOB = "Out of bounds";

    /** assert message: Huh?. */
    public static final String AM_HUH = "Huh?";

    private static final String UTF8 = "UTF-8";

    /** Format token used for parameter replacement. */
    private static final String FORMAT_TOKEN = "{}";

    /** String representation of null. */
    private static final String NULL_REP = "{null}";

    /** Name of the coverage metrics file. */
    private static final String COVERAGE_EM = "target/coverage.em";
    private static final String COVERAGE_XML = "target/site/emma/coverage.xml";

    private static MessageDigest digest;

    private static Random random = new Random();

    private static byte data[] = new byte[64];

    // no instantiation allowed
    private TestTools() { }

    // === VM Properties Related

    /**
     * Predicate that returns true if the given system property is set true.
     * For example:
     *
     * <pre>
     * if (TestTools.isPropertyTrue("foo.bar")) {
     *   ...
     * }
     * </pre>
     *
     * will be true if the option {@code -Dfoo.bar=true} was included in the
     * VM parameters.
     *
     * @param propertyName the name of the system property
     * @return true if the specified property is set true
     */
    public static boolean isPropertyTrue(String propertyName) {
        return Boolean.parseBoolean(System.getProperty(propertyName, "false"));
    }

    /**
     * Predicate that returns true if the given environment variable is set
     * true. For example:
     *
     * <pre>
     * if (TestTools.isEnvTrue("FOO_BAR")) {
     *   ...
     * }
     * </pre>
     *
     * will be true if the environment variable FOO_BAR was set to
     * {@code true}.
     *
     * @param varName the name of the system property
     * @return true if the specified property is set true
     */
    public static boolean isEnvTrue(String varName) {
        String ev = System.getenv(varName);
        return ev != null && Boolean.parseBoolean(ev);
    }

    /**
     * Predicate that returns true if the specified type of test output is
     * requested via environment variable {@code SHOW_OUTPUT} or via JVM
     * property {@code show.output}. Either of the above are expected to be
     * set to a comma-separated list of {@code ejb}, {@code sql} or
     * {@code test} values; setting either value to {@code false} suppresses
     * all test output and setting it to {@code true} enables all test output.
     *
     * @param type type of test output
     * @return true if specified type of test output is set to verbose
     */
    public static boolean showOutput(String type) {
        String ev = System.getenv("SHOW_OUTPUT");
        String ep = System.getProperty("show.output");

        // Translate empty string value to 'true' for the property.
        if (ep != null && ep.length() == 0)
            ep = "true";

        // If both the property or the environment variable are null, assume
        // no output requested. Otherwise, if either of them lists the
        // requested type of output or it's value is set to true, return true.
        return !(ev == null && ep == null) &&
                ((ev != null && (ev.contains(type) || ev.equals("true")))
                  || (ep != null && (ep.contains(type) || ep.equals("true"))));
    }

    /**
     * Predicate that returns true if the unit test output is requested via
     * environment variable {@code SHOW_OUTPUT} or via JVM property
     * {@code show.output}. Either of the above are expected to be set to a
     * comma-separated list of {@code ejb}, {@code sql} or {@code test}
     * values; setting either value to {@code false} suppresses all test
     * output and setting it to {@code true} enables all test output.
     *
     * @return true if test output is set to verbose
     */
    public static boolean showOutput() {
        return showOutput("test");
    }

    /**
     * Predicate that returns true if the <em>"keep.temp.files"</em> system
     * property is set.
     *
     * @return true if VM parameter defined: {@code -Dkeep.temp.files=true}
     */
    public static boolean keepTempFiles() {
        return isPropertyTrue("keep.temp.files");
    }

    /**
     * Predicate that returns true if the test is running under coverage.
     * It can be used to skip performance sensitive tests as follows:
     *
     * <pre>
     * assumeTrue(!TestTools.isUnderCoverage());
     * </pre>
     *
     * will be true if the environment variable FOO_BAR was set to
     * {@code true}.
     *
     * @return true if running under coverage
     */
    public static boolean isUnderCoverage() {
        return new File(COVERAGE_EM).exists() &&
                !(new File(COVERAGE_XML).exists());
    }

    /**
     * Predicate that returns true if the environment variable
     * {@code IGNORE_SPEED_TESTS} is true.
     * <p>
     * "Speed" tests typically (and
     * ironically) take a long time (many seconds) to run. Tests which do
     * this should include the following clause in their class set up:
     * <pre>
     * &#64;BeforeClass
     * public static void classSetUp() {
     *     Assume.assumeTrue(!isUnderCoverage() && !ignoreSpeedTests());
     * }
     * </pre>
     * Developers can set the environment variable:
     * <pre>
     * export IGNORE_SPEED_TESTS=true
     * </pre>
     * so that these tests won't run.
     *
     * @return true if ignoring speed tests
     */
    public static boolean ignoreSpeedTests() {
        return isEnvTrue("IGNORE_SPEED_TESTS");
    }


    /**
     * Perfoms assertion that the actual value is greater than or equal to the
     * expected value adjusted for the current
     * {@link TestTools#perfScale() perf scale}.
     *
     * @param label unit/metric label
     * @param expected expected value
     * @param actual actual value
     */
    public static void assertAboveThreshold(String label,
                                            double expected, double actual) {
        // TODO: add unit test (safely)
        double adjusted = expected * perfScale();
        assertTrue(actual + " [" + label + "] does not meet required " + adjusted,
                   actual >= adjusted);
    }


    /**
     * Sets up root Logger level depending on the value of {@link #showOutput}.
     * If test output is suppressed, all log entries with severity lower than
     * {@code SEVERE} will be suppressed.
     */
    public static void setUpLogger() {
        if (!showOutput())
            Logger.getLogger("").setLevel(Level.SEVERE);
    }

    /**
     * Restores the root Logger level to {@code INFO}.
     */
    public static void restoreLogger() {
        Logger.getLogger("").setLevel(Level.INFO);
    }


    /**
     * Utility to log a message to the console. The given object will be
     * forwarded to {@code System.out.println()} only if the
     * <em>"show.output"</em> system property is set.
     * <p>
     * You can enable output by configuring the following VM parameter:
     *
     * <pre>
     * -Dshow.output=true
     * </pre>
     *
     * @param o the object to print
     */
    public static void print(Object o) {
        if (showOutput())
            System.out.println(o);
    }

    /**
     * Utility to format a message using {} placeholder tokens and log it to
     * the console. The message is forwarded to {@code System.out.println()}
     * only if the <em>"show.output"</em> system property is set.
     * <p>
     * You can enable output by configuring the following VM parameter:
     *
     * <pre>
     * -Dshow.output=true
     * </pre>
     *
     * @param format format string containing {} placeholder tokens
     * @param o the objects to participate in the formatted message
     */
    public static void print(String format, Object... o) {
        if (!showOutput())
            return;
        if (o.length == 0)
            print(format);

        // Format the message using the format string as the seed.  Stop either
        // when the list of objects is exhausted or when there are no other
        // place-holder tokens.
        int i = 0;
        int p = -1;
        String rep;
        StringBuilder sb = new StringBuilder(format);
        while (i < o.length && (p = sb.indexOf(FORMAT_TOKEN, p + 1)) >= 0) {
            rep = o[i] == null ? NULL_REP : o[i].toString();
            sb.replace(p, p+2, rep);
            i++;
        }
        System.out.println(sb);
    }

    // === Stack trace related

    /**
     * Get a string formatted to contain the stack trace of the given
     * throwable.
     *
     * @param t throwable whose stack trace is to be formatted
     * @return string containing the stack trace
     */
    public static String trace(Throwable t) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(bos, UTF8));
            t.printStackTrace(pw);
            pw.close();
            return bos.toString(UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 unsupported?!", e);
        }
    }

    // Constant representing a trigger point for when to cut off trace
    // display.
    private static final CharSequence STOP_TRACE_TRIGGER = ".reflect";

    private static final CharSequence TRACE_FILTER = "org.junit.Assert";

    /**
     * Get a string formatted to contain the filtered stack trace of the given
     * throwable. Any stack trace elements at or below any code in
     * {@code .reflect.} named packages will be excluded.
     *
     * @param t throwable whose stack trace is to be formatted
     * @return string containing the stack trace
     */
    public static String testTrace(Throwable t) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(bos, UTF8));

            pw.printf("%s%s", t.toString(), EOL);
            for (StackTraceElement st : t.getStackTrace()) {
                if (st.getClassName().contains(STOP_TRACE_TRIGGER))
                    break;
                if (!st.getClassName().contains(TRACE_FILTER))
                    pw.printf("\tat %s.%s(%s:%d)%s", st.getClassName(),
                              st.getMethodName(), st.getFileName(),
                              st.getLineNumber(), EOL);
            }

            pw.close();
            return bos.toString(UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 unsupported?!", e);
        }
    }


    /**
     * Asserts that the specified exception has the given message and/or
     * underlying cause.
     *
     * @param t throwable to be tested
     * @param m expected message
     * @param c expected cause
     */
    public static void assertException(Throwable t, String m, Throwable c) {
        assertEquals("incorrect exception message", m, t.getMessage());
        assertEquals("incorrect exception cause", c, t.getCause());
    }


    // === Timing Related

    /**
     * Utility method to pause the current thread for the specified number of
     * milliseconds.
     *
     * @param ms number of milliseconds to pause
     */
    public static void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            fail("unexpected interrupt");
        }
    }

    /**
     * Returns a time in the past, anchored off the current time.
     *
     * @param offset millisecond offset into the past
     * @return time 'offset' milliseconds in the past
     */
    public static long then(long offset) {
        return System.currentTimeMillis() - offset;
    }

    /**
     * Periodically runs the given runnable, which should contain a series of
     * test assertions until all the assertions succeed, in which case it will
     * return, or until the the time expires, in which case it will throw the
     * first failed assertion error.
     *
     * @param start start time, in millis since start of epoch from which the
     *        duration will be measured
     * @param delay initial delay (in milliseconds) before the first assertion
     *        attempt
     * @param step delay (in milliseconds) between successive assertion
     *        attempts
     * @param duration number of milliseconds beyond the given start time,
     *        after which the failed assertions will be propagated and allowed
     *        to fail the test
     * @param assertions runnable housing the test assertions
     */
    public static void assertAfter(long start, int delay, int step,
                                   int duration, Runnable assertions) {
        delay(delay);
        while (true) {
            try {
                assertions.run();
                break;
            } catch (AssertionError e) {
                if (System.currentTimeMillis() - start > duration)
                    throw e;
            }
            delay(step);
        }
    }

    /**
     * Periodically runs the given runnable, which should contain a series of
     * test assertions until all the assertions succeed, in which case it will
     * return, or until the the time expires, in which case it will throw the
     * first failed assertion error.
     * <p>
     * The start of the period is the current time.
     *
     * @param delay initial delay (in milliseconds) before the first assertion
     *        attempt
     * @param step delay (in milliseconds) between successive assertion
     *        attempts
     * @param duration number of milliseconds beyond the current time time,
     *        after which the failed assertions will be propagated and allowed
     *        to fail the test
     * @param assertions runnable housing the test assertions
     */
    public static void assertAfter(int delay, int step, int duration,
                                   Runnable assertions) {
        assertAfter(System.currentTimeMillis(), delay, step, duration,
                    assertions);
    }

    /**
     * Periodically runs the given runnable, which should contain a series of
     * test assertions until all the assertions succeed, in which case it will
     * return, or until the the time expires, in which case it will throw the
     * first failed assertion error.
     * <p>
     * The start of the period is the current time and the first assertion
     * attempt is delayed by the value of {@code step} parameter.
     *
     * @param step delay (in milliseconds) between successive assertion
     *        attempts
     * @param duration number of milliseconds beyond the current time time,
     *        after which the failed assertions will be propagated and allowed
     *        to fail the test
     * @param assertions runnable housing the test assertions
     */
    public static void assertAfter(int step, int duration,
                                   Runnable assertions) {
        assertAfter(step, step, duration, assertions);
    }

    /**
     * Periodically runs the given runnable, which should contain a series of
     * test assertions until all the assertions succeed, in which case it will
     * return, or until the the time expires, in which case it will throw the
     * first failed assertion error.
     * <p>
     * The start of the period is the current time and each successive
     * assertion attempt is delayed by at least 10 milliseconds unless the
     * {@code duration} is less than that, in which case the one and only
     * assertion is made after that delay.
     *
     * @param duration number of milliseconds beyond the current time,
     *        after which the failed assertions will be propagated and allowed
     *        to fail the test
     * @param assertions runnable housing the test assertions
     */
    public static void assertAfter(int duration, Runnable assertions) {
        int step = Math.min(duration, Math.max(10, duration / 10));
        assertAfter(step, duration, assertions);
    }

    // === Memory related

    /**
     * Keep allocating memory until the condition is satisfied.
     *
     * @param delay startup delay (milliseconds before assertions are
     *        checked).
     * @param step number of milliseconds between each assertions/allocation
     *        step.
     * @param duration number of milliseconds beyond the current time, after
     *        which the failed assertions will be propagated and allowed to
     *        fail the test
     * @param assertions runnable housing the test assertions
     */
    public static void assertAfterAlloc(int delay, int step, int duration,
                                        Runnable assertions) {
        long start = System.currentTimeMillis();
        delay(delay);

        // Setup the memory container and initial allocation size.
        int size = 100000;
        List<byte[]> alloc = new ArrayList<byte[]>();

        while (true) {
            try {
                assertions.run();
                break;
            } catch (AssertionError e) {
                if ((System.currentTimeMillis() - start) > duration)
                    throw e;
            }
            delay(step);

            // We failed our assertions, grab more memory...
            try {System.gc();} catch (OutOfMemoryError error) {}

            try {System.runFinalization();} catch (OutOfMemoryError error) {}

            try {
                alloc.add(new byte[size]);
                size = (int) (1.3 * size);
            }
            catch (OutOfMemoryError error) {
                size = size / 2;
            }
        }
    }

    /**
     * Asserts that the object can be garbage collected.
     * Tries to garbage collect the ref's referent.
     * <pre>
     * Object s = new Object();
     * // ...
     * WeakReference&lt;Object&gt; ref = new WeakReference&lt;Object&gt;(s);
     * s = null;
     * assertNotLeaked("Object leaked", ref);
     * </pre>
     * @param text memory leak error text
     * @param duration number of milliseconds beyond the current time,
     *        after which the failed assertions will be propagated and allowed
     *        to fail the test
     * @param ref referent to object that should be garbage collected
     */
    public static void assertNotLeaked(final String text, int duration,
                                       final Reference<?> ref) {
        assertAfterAlloc(100, 200, duration, new Runnable() {
            @Override
            public void run() {
                assertNull(text, ref.get());
            }
        });
    }

    /**
     * See comment for {@link #assertNotLeaked(String, int, Reference)}.
     *
     * @param text memory leak error text
     * @param ref referent to object that should be garbage collected
     */
    public static void assertNotLeaked(String text, Reference<?> ref) {
        assertNotLeaked(text, 10000, ref);
    }


    /**
     * Verifies that two objects are consistently equal with each other; that
     * is to say, o1.equals(o2), o2.equals(o1), and
     * o1.hashCode()==o2.hashCode() each return true.
     *
     * @param o1 object 1
     * @param o2 object 2
     */
    public static void verifyEqual(Object o1, Object o2) {
        assertTrue(AM_NEQ, o1.equals(o2));
        assertTrue(AM_NEQ, o2.equals(o1));
        assertTrue(AM_NEQ, o1.hashCode() == o2.hashCode());
    }

    /** Verifies that two objects are consistently NOT equal with each other;
     * that is to say, o1.equals(o2), o2.equals(o1), and
     * o1.hashCode()==o2.hashCode()
     * each return false.
     *
     * @param o1 object 1
     * @param o2 object 2
     */
    public static void verifyNotEqual(Object o1, Object o2) {
        assertFalse(AM_NEQ, o1.equals(o2));
        assertFalse(AM_NEQ, o2.equals(o1));
        assertFalse(AM_NEQ, o1.hashCode() == o2.hashCode());
    }

    /** Verifies that the given set of enumeration constants contains
     * precisely those constants specified as the remainder of the arguments.
     *
     * @param set the set to test
     * @param constants the constants to check for
     */
    public static void verifySetContains(Set<?> set,
                                         Enum<?>... constants) {
        assertEquals(AM_UXS, constants.length, set.size());
        for (Enum<?> e: constants)
            assertTrue("missing constant", set.contains(e));
    }

    // === Random number generation related

    /**
     * Get the random generator created for miscellaneous test purposes.
     *
     * @return test random generator
     */
    public static Random random() {
        return random;
    }

    /**
     * Tweak the specified number of bytes within the given byte array.
     *
     * @param random random number generator
     * @param bytes byte array to be tweaked
     * @param count number of bytes to tweak
     */
    public static void tweakBytes(Random random, byte[] bytes, int count) {
        tweakBytes(random, bytes, count, 0, bytes.length);
    }

    /**
     * Tweak the specified number of bytes within the specified range of the
     * given byte array.
     *
     * @param random random number generator
     * @param bytes byte array to be tweaked
     * @param count number of bytes to tweak
     * @param start index at beginning of range (inclusive)
     * @param end index at end of range (exclusive)
     */
    public static void tweakBytes(Random random, byte[] bytes, int count,
                                  int start, int end) {
        int len = end - start;
        for (int i = 0; i < count; i++)
            bytes[start + random.nextInt(len)] = (byte) random.nextInt();
    }

    /**
     * Generate a pseudo-random integer from the specified distribution.
     *
     * @param random random number generator
     * @param mean mean of the gaussian distribution
     * @param std standard deviation of the distribution
     * @return pseudo-random integer
     */
    public static int nextGaussian(Random random, int mean, int std) {
        return mean + (int) (random.nextGaussian() * std);
    }

    // === Hash generation related

    /**
     * Setup test tools for random hash generation.
     *
     * @throws java.security.NoSuchAlgorithmException if the SHA-256 algorithm
     *         cannot be loaded
     */
    public static synchronized void setUpTools()
            throws NoSuchAlgorithmException {
        if (digest == null) {
            // Prepare scratch data for computing a hash digest
            digest = MessageDigest.getInstance("SHA-256");
            tweakBytes(random, data, data.length);
        }
    }

    // === File related

    /**
     * Returns the absolute path for the given file and undoes the Windoze
     * sillies, such as {@code \} and {@code \.\}
     *
     * @param file file whose path to translate into absolute path
     * @return the absolute file name using Unix-style forward slashes
     */
    public static String getAbsolutePath(File file) {
        String path = file.getAbsolutePath();
        path = path.replace('\\', '/');
        return path.replaceAll("/./", "/");
    }

    /**
     * Writes random binary content into the specified file. The number of
     * bytes will be random between the given minimum and maximum.
     *
     * @param file file to write data to
     * @param minSize minimum number of bytes to write
     * @param maxSize maximum number of bytes to write
     * @throws IOException if there is an issue
     */
    public static void writeRandomContent(File file, int minSize,
                                          int maxSize) throws IOException {
        int size = minSize
                + (minSize == maxSize ? 0 : random.nextInt(maxSize - minSize));
        final FileOutputStream fos = new FileOutputStream(file);
        try {
            byte[] buffer = new byte[1024];
            int written = 0;
            while (written < size) {
                tweakBytes(random, buffer, 256);
                int len = Math.min(size - written, buffer.length - 1);
                fos.write(buffer, 0, len);
                written += len;
            }
        } finally {
            fos.close();
        }
    }

    /**
     * Writes or appends the specified content bytes to the given file.
     *
     * @param file file to append content to
     * @param content input stream containing data to be written
     * @param append true to cause append; false to cause re-write
     * @throws IOException if there is an issue
     */
    public static void writeContent(File file, InputStream content,
                                    boolean append) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, append);
        try {
            byte buffer[] = new byte[4096];
            int l;
            while ((l = content.read(buffer)) >= 0)
                fos.write(buffer, 0, l);
        } finally {
            fos.close();
            content.close();
        }
    }


    /**
     * Writes or appends the specified content bytes to the given file.
     *
     * @param file file to append content to
     * @param content bytes to be written
     * @param append true to cause append; false to cause re-write
     * @throws IOException if there is an issue
     */
    public static void writeContent(File file, byte[] content,
                                    boolean append) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, append);
        try {
            fos.write(content);
        } finally {
            fos.close();
        }
    }


    /**
     * Consume the data from the specified buffered reader and return it as a
     * string.
     *
     * @param br buffered reader
     * @return string containing data from the buffered reader
     */
    public static String slurp(BufferedReader br) {
        if (br == null)
            return null;

        StringBuilder output = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null)
                output.append(line).append(EOL);
            return new String(output);
        } catch (IOException e) {
            return null;
        } finally {
            try {
                br.close();
            } catch (IOException ioe) {
            }
        }
    }


    // === Directory related

    /**
     * Makes sure that the the temporary test directory is empty.
     *
     * @param path directory path to be cleaned up
     * @throws Exception thrown if any issues are encountered while pruning
     *         test files
     */
    public static void scrubTestData(String path) throws Exception {
        scrubTestData(path, false);
    }

    /**
     * Makes sure that the the temporary test directory is empty.
     *
     * @param path directory path to be cleaned up
     * @param inclusive true if the top level directory should be removed;
     *        false otherwise
     * @throws Exception thrown if any issues are encountered while pruning
     *         test files
     */
    public static void scrubTestData(String path, boolean inclusive)
                                                            throws Exception {
        final File d = new File(path);
        if (d.exists()) {
            String[] fileNames = d.list();
            if (fileNames != null) {
                for (String fn : fileNames) {
                    File f = new File(d, fn);
                    if (f.isDirectory())
                        scrubTestData(f.getAbsolutePath() + "/", true);
                    else if (!f.delete())
                        fail("unable to delete test file <" + f + ">" +
                                (ON_WINDOWS ? "; note that this is likely due to the bloody Windoze search indexer squatting on the file" : ""));
                }
            }

            if (inclusive) {
                if (!d.delete())
                    fail("unable to delete test directory <" + path + ">");
                assertFalse("test directory <" + path + "> should not exist",
                            d.exists());
            }
        }
    }

    /**
     * Create a directory tree of files using the given list of file paths.
     *
     * @param paths list of file paths to be used for creating a tree of files
     *        and directories.  If the file path ends in a "/", a directory is
     *        created.  Otherwise a data file is created and populated with
     *        random data whose length is between minSize and maxSize.
     * @param minSize minimum number of bytes for generated file size
     * @param maxSize maximum number of bytes for generated file size
     * @return list of resulting files that were created
     * @throws IOException if there is an issue
     */
    public static File[] createFileTree(String[] paths,
                                        int minSize, int maxSize)
                                       throws IOException {
        final List<File> files = new ArrayList<File>(paths.length);
        for (String p : paths) {
            File f = new File(p);
            if (p.endsWith("/")) {
                if (f.mkdirs())
                    files.add(f);
            } else {
                File parent = f.getParentFile();
                if (!parent.exists() && !parent.mkdirs())
                    fail("unable to create test dir <" + parent + ">");
                print(f);
                if (f.createNewFile()) {
                    writeRandomContent(f, minSize, maxSize);
                    files.add(f);
                }
            }
        }
        return files.toArray(new File[files.size()]);
    }

    /**
     * Create a directory tree of files using the given list of file paths.
     *
     * @param paths list of file paths to be used for creating a tree of files
     *        and directories.  If the file path ends in a "/", a directory is
     *        created.  Otherwise a data file is created and populated with
     *        random data whose length is between 32 and 1024 bytes.
     * @return list of resulting files that were created
     * @throws IOException if there is an issue
     */
    public static File[] createFileTree(String[] paths) throws IOException {
        return createFileTree(paths, 32, 1024);
    }

    /**
     * Get the check-sum (CRC32) of the specified bytes of data.
     *
     * @param data byte array of data
     * @return check-sum of the data on the given stream
     */
    public static long getCheckSum(byte[] data) {
        return getCheckSum(new ByteArrayInputStream(data));
    }

    /**
     * Get the check-sum (CRC32) of the specified input stream.
     *
     * @param is input stream
     * @return check-sum of the data on the given stream
     */
    public static long getCheckSum(InputStream is) {
        try {
            CheckedInputStream cis = new CheckedInputStream(is, new CRC32());
            byte[] data = new byte[1024];
            while (cis.read(data) >= 0)
                /* empty body */
                ;
            return cis.getChecksum().getValue();
        } catch (IOException e) {
            fail("Unable to read input stream due to " + e);
            return 0;
        }
    }

    /**
     * Get the check-sum (CRC32) of the specified file.
     *
     * @param path file path
     * @return check-sum of the given file
     * @throws FileNotFoundException if the specified file is not found
     */
    public static long getCheckSum(String path) throws FileNotFoundException {
        return getCheckSum(new FileInputStream(path));
    }


    // === Miscellany

    /**
     * Creates a map from the specified object array.
     *
     * @param bindings object array expected to contain an even number of
     *        objects. The odd entries (even index) are expected to be
     *        {@link java.lang.String} instances that will be used as the keys
     *        and the even entries (odd index) are expected to be
     *        {@link java.lang.Object} instances that will be used as the
     *        values in the map.
     * @return map of key/value bindings
     */
    public static Map<String, Object> createMap(Object[] bindings) {
        Map<String, Object> map = new HashMap<String, Object>(bindings.length / 2);
        for (int i = 0; i < bindings.length; i += 2)
            map.put((String) bindings[i], bindings[i + 1]);
        return map;
    }


    // === Precision related

    /**
     * Asserts that the actual value is equal to the expected value within the
     * specified tolerance. For example:
     *
     * <pre>
     * private static final float TOLERANCE = 0.00001f;
     * float someFloat = object.getSomeComputedFloat();
     * assertEqualsWithinTolerance(TOLERANCE, 5.0f, someFloat);
     * </pre>
     *
     * The above assertion will pass if:
     *
     * <pre>
     * 4.99999 &lt;= someFloat &lt;= 5.00001
     * </pre>
     *
     * @param tolerance the tolerance
     * @param expected the expected value
     * @param actual the actual value
     */
    public static void assertEqualsWithinTolerance(float tolerance,
                                                   float expected,
                                                   float actual) {
        if (actual < expected-tolerance || actual > expected+tolerance) {
            String sb = "[" + (expected - tolerance) + " <= X <= " +
                    (expected + tolerance) + "] failed: <" + actual + ">";
            fail(sb);
        }
    }

    /**
     * Asserts that the check-sum of the given file is equal to the expected
     * checksum.
     *
     * @param checksum expected check-sum
     * @param path file path whose check-sum is to be validated
     */
    public static void assertCheckSum(long checksum, String path) {
        try {
            assertEquals(path + " has incorrect checksum",
                         checksum, getCheckSum(path));
        } catch (FileNotFoundException e) {
            fail("unable to verify checksum; " + path + " not found");
        }
    }


    /**
     * Read in the contents of a text file from the given path.
     *
     * @param path path of resource containing the data
     * @param classLoader class-loader to be used for locating resource
     * @return the contents of the file as a string
     */
    public static String getFileContents(String path, ClassLoader classLoader) {
        InputStream is = classLoader.getResourceAsStream(path);
        String expected = null;
        if (is == null)
            fail("unable to find expected data for " + path);
        try {
            // using Apache Commons IOUtils to read byte array from input stream
            byte[] bytes = IOUtils.toByteArray(is);

            expected = new String(bytes, UTF8).replaceAll("\0", "");
        } catch (IOException e) {
            fail("Unable to read expected data for " + path);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                fail("Unable to close expected data for " + path);
            }
        }
        return expected;
    }

    /**
     * Validate the expected data contained in the file/resource named by the
     * data path against the specified actual data.  Note that any differences
     * in line terminators will be ignored.
     *
     * @param expectedDataPath path of the resource containing expected data
     * @param actual actual data
     * @param classLoader class-loader to be used for locating the expected
     *        data resources
     */
    public static void assertData(String expectedDataPath,
                                  String actual,
                                  ClassLoader classLoader) {
        String expected = getFileContents(expectedDataPath, classLoader);
        assertEquals("incorrect data for " + expectedDataPath,
                     normalizeEOL(expected), normalizeEOL(actual));
    }

    /**
     * Normalizes any compound line breaks (\r\n or \n\r) to only \n.
     *
     * @param string string to be normalized
     * @return normalized string
     */
    public static String normalizeEOL(String string) {
        return string.replaceAll("(\n\r|\r\n|\r)", "\n");
    }


    // ======================================================================

    /** A simple stop-watch class that captures system time at
     * construction and calculates elapsed time (ms) once the
     * {@link #stop()} method is called. Elapsed time can be
     * retrieved via the {@link #getElapsed()} method, or can be observed
     * in the string representation of the class.
     * <p>
     * An example of usage:
     * <pre>
     *     TestTools.StopWatch watch = new StopWatch("Foo");
     *
     *     ... do work to be timed ...
     *
     *     TestTools.print(watch.stop());
     * </pre>
     * May print something like this:
     * <pre>
     *     "{StopWatch[Foo]: 240 ms}"
     * </pre>
     */
    public static class StopWatch {
        private final String label;
        private final long start;
        private long elapsed;
        private boolean stopped = false;

        /** Constructs a stop-watch with the given identifying label.
         *
         * @param label an identifying label
         */
        public StopWatch(String label) {
            this.label = label;
            start = System.currentTimeMillis();
        }

        /** Constructs a stop-watch. */
        public StopWatch() {
            this(null);
        }

        /** Stops the stop-watch. If the stop-watch is already stopped,
         * this has no effect.
         *
         * @return self; for method chaining
         */
        public StopWatch stop() {
            if (!stopped)
                elapsed = System.currentTimeMillis() - start;
            stopped = true;
            return this;
        }

        /** Returns the elapsed time in milliseconds. This will be 0 until
         * the watch is stopped.
         *
         * @return the elapsed time in milliseconds
         */
        public long getElapsed() {
            return elapsed;
        }

        @Override
        public String toString() {
            return toString(0);
        }

        /**
         * Returns a display string based on the current state.  If the watch
         * has been stopped and {@code n} is > 0, the per second value will be
         * computed for the given {@code n} and included. For example, {@code n}
         * could be the number of iterations computed during the elapsed time.
         *
         * @param n the iterations, count, etc.
         * @return the display string
         */
        public String toString(int n) {
            StringBuilder sb = new StringBuilder("{StopWatch");
            if (label != null)
                sb.append("[").append(label).append("]");
            sb.append(": ");
            if (stopped) {
                sb.append(elapsed).append(" ms");
                if (0 < n)
                    sb.append(", for n=").append(n)
                        .append(" ~")
                        .append((int) (n / (elapsed / 1000.0)))
                        .append("/sec");
            }
            else
                sb.append("running...");
            sb.append("}");
            return sb.toString();
        }
    }

    /**
     * Returns the scaling factor for any performance-sensitive test
     * thresholds to be adjusted for the execution environment.
     *
     * @return scaling factor; defaults to 1
     */
    public static double perfScale() {
        return Double.parseDouble(SCALE_FACTOR != null ? SCALE_FACTOR : "1");
    }

    /**
     * Updates the value of a private field using reflection. The private
     * field must be declared in the object's class and not in a predecessor.
     * <p>
     * The purpose of this method is to set mocks for dependency objects
     * created internally by the class being tested or injected by frameworks
     * like OSGi. Reflection should not be used to update fields which by
     * design are private.
     *
     * @param fieldName field name
     * @param fieldValue new field's value
     * @param obj instance to update the field on
     * @throws SecurityException If a crypt manager, s, is present and any
     *         of the following conditions is met: - invocation of
     *         s.checkMemberAccess(this, Member.DECLARED) denies access to the
     *         declared field - the caller's class loader is not the same as
     *         or an ancestor of the class loader for the current class and
     *         invocation of s.checkPackageAccess() denies access to the
     *         package of this class.
     * @throws NoSuchFieldException if a field with the specified name is not
     *         found.
     * @throws IllegalArgumentException if {@code fieldName} is {@code null}.
     * @throws IllegalAccessException if the underlying field is inaccessible.
     * @throws NullPointerException if either the {@code fieldName} or
     *         {@code obj} is {@code null}.
     */
    public static <T> void setPrivateField(String fieldName, Object fieldValue,
        T obj) throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        if (obj == null)
            throw new NullPointerException("obj cannot be null");

        @SuppressWarnings("unchecked")
        Class<? super T> objClass = (Class<? super T>) obj.getClass();
        setPrivateField(fieldName, fieldValue, obj, objClass);
    }

    /**
     * Updates the value of a private field using reflection. The private
     * field must be declared in the {@code declaredFieldClass} class and not
     * in a predecessor or ancestor.
     * <p>
     * The purpose of this method is to set mocks for dependency objects
     * created internally by the class being tested or injected by frameworks
     * like OSGi. Reflection should not be used to update fields which by
     * design are private.
     *
     * @param fieldName field name
     * @param fieldValue new field's value
     * @param obj instance to update the field on
     * @param declaredFieldClass class where the field is located
     * @throws SecurityException If a crypt manager, s, is present and any
     *         of the following conditions is met: - invocation of
     *         s.checkMemberAccess(this, Member.DECLARED) denies access to the
     *         declared field - the caller's class loader is not the same as
     *         or an ancestor of the class loader for the current class and
     *         invocation of s.checkPackageAccess() denies access to the
     *         package of this class.
     * @throws NoSuchFieldException if a field with the specified name is not
     *         found.
     * @throws IllegalArgumentException if {@code fieldName} is {@code null}.
     * @throws IllegalAccessException if the underlying field is inaccessible.
     * @throws NullPointerException if either the {@code fieldName} or
     *         {@code obj} is {@code null}.
     */
    public static <T> void setPrivateField(String fieldName, Object fieldValue,
         T obj, Class<? super T> declaredFieldClass) throws SecurityException,
             NoSuchFieldException, IllegalArgumentException,
                  IllegalAccessException {

        if (obj == null)
            throw new NullPointerException("obj cannot be null");

        if (fieldName == null)
            throw new NullPointerException("fieldName cannot be null");

        if (fieldName.isEmpty())
            throw new NullPointerException("fieldName cannot be empty");

        if (declaredFieldClass == null)
            throw new NullPointerException("declaredFieldClass cannot be null");

        Field field = declaredFieldClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, fieldValue);
    }

    /**
     * Gets a private field using reflection. The private field must be
     * declared in the object's class and not in a predecessor.
     *
     * @param fieldName field name
     * @param obj instance to get the field from
     * @return the private field's value
     * @throws SecurityException If a crypt manager, s, is present and any
     *         of the following conditions is met: - invocation of
     *         s.checkMemberAccess(this, Member.DECLARED) denies access to the
     *         declared field - the caller's class loader is not the same as
     *         or an ancestor of the class loader for the current class and
     *         invocation of s.checkPackageAccess() denies access to the
     *         package of this class.
     * @throws NoSuchFieldException if a field with the specified name is not
     *         found.
     * @throws IllegalArgumentException if {@code fieldName} is {@code null}.
     * @throws IllegalAccessException if the underlying field is inaccessible.
     * @throws NullPointerException if either the {@code fieldName} or
     *         {@code obj} is {@code null}.
     */
    public static <T, E> E getPrivateField(String fieldName, T obj)
        throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        if (obj == null)
            throw new NullPointerException("obj cannot be null");

        @SuppressWarnings("unchecked")
        Class<? super T> objClass = (Class<? super T>) obj.getClass();
        return getPrivateField(fieldName, obj, objClass);
    }

    /**
     * Gets a private field using reflection. The private field must be
     * declared in the {@code declaredFieldClass} class and not in a
     * predecessor or ancestor.
     *
     * @param fieldName field name
     * @param obj instance to get the field from
     * @param declaredFieldClass class where the field is located
     * @return the private field's value
     * @throws SecurityException If a crypt manager, s, is present and any
     *         of the following conditions is met: - invocation of
     *         s.checkMemberAccess(this, Member.DECLARED) denies access to the
     *         declared field - the caller's class loader is not the same as
     *         or an ancestor of the class loader for the current class and
     *         invocation of s.checkPackageAccess() denies access to the
     *         package of this class.
     * @throws NoSuchFieldException if a field with the specified name is not
     *         found.
     * @throws IllegalArgumentException if {@code fieldName} is {@code null}.
     * @throws IllegalAccessException if the underlying field is inaccessible.
     * @throws NullPointerException if either the {@code fieldName} or
     *         {@code obj} is {@code null}.
     */
    public static <T, E> E getPrivateField(String fieldName, T obj,
        Class<? super T> declaredFieldClass) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {

        if (obj == null)
            throw new NullPointerException("obj cannot be null");

        if (fieldName == null)
            throw new NullPointerException("fieldName cannot be null");

        if (fieldName.isEmpty())
            throw new NullPointerException("fieldName cannot be empty");

        if (declaredFieldClass == null)
            throw new NullPointerException("declaredFieldClass cannot be null");

        Field field = declaredFieldClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        E value = (E) field.get(obj);
        return value;
    }

    /**
     * Asserts that {@code str} contains {@code infix}.
     * 
     * @param infix expected content
     * @param str string to assert
     */
    public static void assertContains(String infix, String str) {
        assertNotNull(infix);
        assertNotNull(str);
        assertTrue("Expected <" + infix + "> contained in <" + str + ">",
                   str.contains(infix));
    }

    /**
     * Asserts that {@code str} starts with {@code prefix}.
     * 
     * @param prefix expected prefix
     * @param str string to assert
     */
    public static void assertStartsWith(String prefix, String str) {
        assertNotNull(prefix);
        assertNotNull(str);
        assertTrue("Expected <" + prefix + "> as prefix of <" + str + ">",
                   str.startsWith(prefix));
    }

    /**
     * Asserts that {@code str} ends with {@code suffix}.
     * 
     * @param suffix expected prefix
     * @param str string to assert
     */
    public static void assertEndsWith(String suffix, String str) {
        assertNotNull(suffix);
        assertNotNull(str);
        assertTrue("Expected <" + suffix + "> as suffix of <" + str + ">",
                   str.endsWith(suffix));
    }


    /**
     * Counts the number of items in the specified iterator.
     *
     * @param it iterator to count
     * @param <T> item type
     * @return number of items
     */
    public static <T> int sizeOf(Iterator<T> it) {
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

}
