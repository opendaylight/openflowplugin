/*
 * (c) Copyright 2009-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import org.opendaylight.util.junit.TestTools.*;
import org.opendaylight.util.junit.ThrowableTester.Instruction;
import org.opendaylight.util.junit.ThrowableTester.Validator;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.util.junit.TestTools.*;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Not as bizzare as you may think.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class TestToolsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        setUpTools();
        scrubTestData(TEST_DIR);
        createFileTree(new String[] {
                TEST_DIR,
                TEST_DIR + "foo/",
                TEST_DIR + "foo/gigi.txt",
                TEST_DIR + "foo/frou.txt",
                TEST_DIR + "bar/",
                TEST_DIR + "bar/fifi.txt"
                });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        String sof = System.getProperty("show.output", "false");
        System.setProperty("show.output", "true");
        print("You should see this in test output!  No reason for alarm!");
        print("You should {} see {} in {} output! {}", "also", "this", "test",
              "Still no reason for alarm!");

        try {
            scrubTestData(TEST_DIR + "foo", true);
        } finally {
            System.setProperty("show.output", sof);
        }
    }

    @Test
    public void testRandom() {
        // Make sure that two consecutive random numbers are not equal.
        // Do this twice and allow only one such match as a way to tolerate a
        // random fluke.
        assertTrue("consecutive randoms should not be equal",
                   (nextGaussian(random(), 100, 50) != nextGaussian(random(), 100, 50)) ||
                       nextGaussian(random(), 100, 50) != nextGaussian(random(), 100, 50));
    }

    @Test
    public void testDelay() {
        long now = System.currentTimeMillis();
        delay(1100);
        assertTrue("at least 1 second should have expired", then(now) >= 1000);
    }

    @Test
    public void testTweak() {
        byte data[] = new byte[64];
        tweakBytes(random(), data, data.length/2);
        int count = 0;
        for (byte b : data)
            if (b != '\0')
                count++;
        // This can't be equals, as some bytes may get tweaked more than once
        assertTrue("incorrect number of tweaked bytes",
                   count > 0 && count <= data.length/2);
    }

    @Test
    public void testTweakRange() {
        byte data[] = new byte[64];
        int half = data.length/2;
        tweakBytes(random(), data, half/2, 0, half);
        int count = 0;
        int i = 0;
        for (byte b : data) {
            if (i++ >= half)
                assertEquals(AM_NEQ, 0, b);
            else if (b != '\0')
                count++;
        }
        // This can't be equals, as some bytes may get tweaked more than once
        assertTrue("incorrect number of tweaked bytes",
                   count > 0 && count <= half/2);
    }

    private static final String ONE = "one";
    private static final String TWO = "two";
    private static final String THREE = "three";
    private static final String FOUR = "four";
    private static final Object[] MAP_SRC = new Object[] {
            ONE, 1, TWO, 2, THREE, 3, FOUR, 4,
    };

    private static final String IB = "incorrect binding";
    private static final String IBC = "incorrect binding count";

    @Test
    public void testCreateMap() {
        final Map<String, Object> bindings = createMap(MAP_SRC);
        assertEquals(IBC, 4, bindings.size());
        assertEquals(IB, 1, bindings.get(ONE));
        assertEquals(IB, 2, bindings.get(TWO));
        assertEquals(IB, 3, bindings.get(THREE));
        assertEquals(IB, 4, bindings.get(FOUR));
    }

    @Test
    public void testGetAbsolutePath() {
        assumeTrue(TestTools.ON_WINDOWS);
        String p = getAbsolutePath(new File("\\tmp\\foo\\.\\bar.txt"));
        assertEquals("incorrect absolute path", "/tmp/foo/bar.txt",
                     p.contains(":") ? p.substring(2) : p);
    }

    @Test
    public void testTraceString() {
        Throwable t = new RuntimeException("test");
        assertTrue("incorrect trace image", trace(t).contains("testTrace"));
        assertTrue("incorrect test trace image", testTrace(t).contains("testTrace"));
    }

    private static final String IFL = "incorrect file length";

    @Test
    public void testCheckSum() throws Exception {
        String yo = "Hello World!";
        assertEquals("incorrect check-sum", 472456355L, getCheckSum(yo.getBytes()));

        File f = new File(TEST_DIR + "checkSumFile");
        writeContent(f, yo.getBytes(), false);   // DON'T use 'EOL' here
        assertEquals("incorrect check-sum", 472456355L, getCheckSum(f.getPath()));
    }

    @Test
    public void testWriteContent() throws Exception {
        File f = new File(TEST_DIR + "appendFoo");
        writeContent(f, "line one!\n".getBytes(), false);   // DON'T use 'EOL' here
        assertEquals(IFL, 10, f.length());
        writeContent(f, "line two!\n".getBytes(), true);    // DON'T use 'EOL' here
        assertEquals(IFL, 20, f.length());
    }

    @Test
    public void testWriteContentFromStream() throws Exception {
        File f = new File(TEST_DIR + "appendBar");
        String data = "What's up?!";
        ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes());
        writeContent(f, bis, false);
        assertTrue("file should exist", f.exists());
        assertEquals("file is wrong size", 11L, f.length());

        BufferedReader br = new BufferedReader(new FileReader(f));
        // Trim off the extra EOL that slurp puts after the last line
        String result = slurp(br).trim();
        assertEquals("incorrect data written", data, result);
        br.close();
    }


    @Test(expected=java.io.IOException.class)
    public void testbadWriteContent() throws Exception {
        File f = new File(TEST_DIR + "/asdas/asdadsa/asdasd/appendFoo");
        writeContent(f, "line one!\n".getBytes(), false);
    }

    @Test
    public void testNoMemLeak() {
        Object s = new Object();
        WeakReference<Object> ref = new WeakReference<Object>(s);
        s = null;
        assertNotLeaked("Object leaked", ref);
    }

    @Test(expected=AssertionError.class)
    public void testMemLeak() {
        Object s = new Object();
        WeakReference<Object> ref = new WeakReference<Object>(s);
        assertNotLeaked("Object leaked", 1000, ref);
    }

    private static enum Foo { FOO, BAR, BAZ, GOO, ZOO }

    @Test
    public void verifySetContainsNone() {
        Set<Foo> set = new TreeSet<Foo>();
        verifySetContains(set);
    }

    @Test
    public void verifySetContainsOne() {
        Set<Foo> set = new TreeSet<Foo>();
        set.add(Foo.GOO);
        verifySetContains(set, Foo.GOO);
    }

    @Test
    public void verifySetContainsSome() {
        Set<Foo> set = new TreeSet<Foo>();
        Collections.addAll(set, Foo.FOO, Foo.BAZ, Foo.ZOO);
        verifySetContains(set, Foo.FOO, Foo.BAZ, Foo.ZOO);
    }

    @Test
    public void verifySetContainsAll() {
        Set<Foo> set = new TreeSet<Foo>();
        Collections.addAll(set, Foo.FOO, Foo.BAR, Foo.BAZ, Foo.GOO, Foo.ZOO);
        verifySetContains(set, Foo.FOO, Foo.BAR, Foo.BAZ, Foo.GOO, Foo.ZOO);
    }

    @Test
    public void stopWatch() {
        print(EOL + "stopWatch()");
        StopWatch watch = new StopWatch("FooBar");
        print(watch);
        assertEquals(AM_NEQ, "{StopWatch[FooBar]: running...}", watch.toString());
        assertEquals(AM_NEQ, 0, watch.getElapsed());
        delay(10);
        watch.stop();
        print(watch);
        long elapsed = watch.getElapsed();
        assertTrue(AM_HUH, elapsed >= 10 && elapsed < 100);
        delay(50);
        watch.stop();
        print(watch);
        assertEquals(AM_NEQ, elapsed, watch.getElapsed());
    }

    @Test
    public void testGetSetPrivateField() throws Exception {
        Object object = new Object() {
            @SuppressWarnings("unused")
            private String field = "Hello World";
        };

        final String fieldName = "field";

        Assert.assertEquals("Hello World",
                            TestTools.getPrivateField(fieldName, object));

        final String newValue = "Goodbye world";
        TestTools.setPrivateField(fieldName, newValue, object);
        Assert.assertEquals(newValue,
                            TestTools.getPrivateField(fieldName, object));
    }

    @Test
    public void testAssertContains() {
        String str = "Hello World";
        String infix = "lo Wo";
        TestTools.assertContains(infix, str);
    }

    @Test
    public void testAssertContainsFail() {
        final String validString = "Hello World";
        final String validInfix = "lo Wo";
        final String invalidStringNull = null;
        final String invalidInfix = "invalid-infix";
        final String invalidInfixNull = null;

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                TestTools.assertContains(validInfix, invalidStringNull);
            }
        });

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                TestTools.assertContains(invalidInfixNull, validString);
            }
        });

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectdError = "Expected <" + invalidInfix
                        + "> contained in <" + validString + ">";
                Assert.assertEquals(expectdError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                TestTools.assertContains(invalidInfix, validString);
            }
        }, errorValidator);
    }

    @Test
    public void testAssertStartsWith() {
        String str = "Hello World";
        String prefix = "Hello";
        TestTools.assertStartsWith(prefix, str);
    }

    @Test
    public void testAssertStartsWithFail() {
        final String validString = "Hello World";
        final String validPrefix = "Hello";
        final String invalidStringNull = null;
        final String invalidPrefix = "invalid-infix";
        final String invalidPrefixNull = null;

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                TestTools.assertStartsWith(validPrefix, invalidStringNull);
            }
        });

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                TestTools.assertStartsWith(invalidPrefixNull, validString);
            }
        });

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectdError = "Expected <" + invalidPrefix
                        + "> as prefix of <" + validString + ">";
                Assert.assertEquals(expectdError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                TestTools.assertStartsWith(invalidPrefix, validString);
            }
        }, errorValidator);
    }

    @Test
    public void testAssertEndsWith() {
        String str = "Hello World";
        String suffix = "World";
        TestTools.assertEndsWith(suffix, str);
    }

    @Test
    public void testAssertEndsWithFail() {
        final String validString = "Hello World";
        final String validSuffix = "World";
        final String invalidStringNull = null;
        final String invalidSuffix = "invalid-infix";
        final String invalidSuffixNull = null;

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                TestTools.assertEndsWith(validSuffix, invalidStringNull);
            }
        });

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                TestTools.assertEndsWith(invalidSuffixNull, validString);
            }
        });

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {

            @Override
            public void assertThrowable(AssertionError error) {
                String expectdError = "Expected <" + invalidSuffix
                        + "> as suffix of <" + validString + ">";
                Assert.assertEquals(expectdError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                TestTools.assertEndsWith(invalidSuffix, validString);
            }
        }, errorValidator);
    }

    @Test
    public void testPlatformFlags() {
        print(EOL + "testPlatformFlags()");
        print("os.name = [{}]", System.getProperty("os.name"));
        if (TestTools.ON_WINDOWS) {
            print("We are on Windoze");
            assertFalse(AM_HUH, TestTools.ON_LINUX);
        } else if (TestTools.ON_LINUX) {
            print("We are on Linux");
            assertFalse(AM_HUH, TestTools.ON_WINDOWS);
        } else {
            print("WHAT are we running on???");
        }
    }
}
