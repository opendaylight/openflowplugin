/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.junit.Assert.*;
import static org.opendaylight.util.StringUtils.EOL;

import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.*;

/**
 * The log formatter unit test.
 */
public class LogFormatterTest {
    
    private static final String BUNDLE = LogFormatterTest.class.getName();
    
    protected static Formatter fmtr;
    
    protected static boolean hasSuffix(String aString, String aSuffix) {
        return aString.endsWith(aSuffix + EOL);
    }

    /**
     * Create the static formatter class to share in the tests.
     */
    @BeforeClass
    public static void classLoadInitilization() {
        fmtr = new LogFormatter();
    }
    
    /**
     * Test curly brace replacement.
     */
    @Test
    public void testCurlyBraceReplacement() {
        String testMsg = "I have curlies {{}}!";
        assertEquals("Curly brace replacement didn't work", "I have curlies [[]]!",
                     LogFormatter.prepareCurlyBraceStringForLogging(testMsg));
    }
    
    /**
     * Basic string test.
     */
    @Test
    public void testPlainString() {
        String testMsg = "This is a test msg.";
        LogRecord rec = new LogRecord(Level.INFO, testMsg);
        rec.setSourceClassName(getClass().getName());
        rec.setSourceMethodName("testPlainString");
        String fmt=fmtr.format(rec);
        // System.out.print(fmt);
        assertTrue(hasSuffix(fmt, testMsg));
        assertTrue(fmt.contains(Level.INFO.getName()));
        assertTrue(fmt.contains(getClass().getSimpleName() + ".testPlainString"));
    }
    
    /**
     * Test with substitution parameters.
     */
    @Test
    public void testStringWithParams() {
        String testMsg = "This is a {0} message {1} string";
        LogRecord rec = new LogRecord(Level.CONFIG, testMsg);
        rec.setParameters(new Object[] {"configuration", "paramaters in the"});
        String fmt = fmtr.format(rec);
        // System.out.print(fmt);
        assertTrue(hasSuffix(fmt, "This is a configuration message paramaters in the string"));
        assertTrue(fmt.contains(Level.CONFIG.getName()));
    }
    
    /**
     * Test with chainged throwables.
     */
    @Test 
    public void testWithThrowable() {
        String testMsg = "This is a message with three exceptions (nested).";
        Throwable crap = new IllegalArgumentException("crap");
        Throwable creep = new IllegalArgumentException("creepy", crap);
        Throwable crud = new IllegalArgumentException("crud", creep);
        // Un-comment this code to compare output with java.util.logging.SimpleFormatter
        // Logger l = Logger.getLogger(TextLineFormatterUTest.class.getName());
        // l.log(Level.SEVERE, testMsg, crud);
        LogRecord rec = new LogRecord(Level.SEVERE, testMsg);
        rec.setThrown(crud);
        String fmt = fmtr.format(rec);
        // System.out.print(fmt);
        assertTrue(fmt.contains("crud"));
        assertTrue(fmt.contains("Caused by:"));
        assertTrue(fmt.contains("creepy"));
        assertTrue(fmt.contains("crap"));
    }
    
    /**
     * Test should throw an exception beecause parameter 0 is not a number.
     */
    @Test
    public void testStringWithIllegalParameters() {
        String testMsg = "This is a {0, number} message {1} string";
        LogRecord rec = new LogRecord(Level.WARNING, testMsg);
        rec.setParameters(new Object[] {"configuration", "paramaters in the"});
        String fmt = fmtr.format(rec);
        // System.out.print(fmt);
        assertTrue(fmt.contains("IllegalArgumentException"));   
        assertTrue(fmt.contains(Level.WARNING.getName()));
    }
    
    /**
     * Test loading a resource bundle.
     */
    @Test
    public void testResourceBundle() {
        ResourceBundle rb = ResourceBundle.getBundle(BUNDLE);
        assertNotNull(rb);
        LogRecord rec = new LogRecord(Level.SEVERE, "test_message");
        rec.setResourceBundle(rb);
        // String fmt = fmtr.format(rec);
        // System.out.print(fmt);
    }
    
    /**
     * Test loading and using a resource bundle for substitution.
     */
    @Test
    public void testResourceBundleWithParams() {
        LogRecord rec = new LogRecord(Level.SEVERE, "test_params");
        rec.setResourceBundleName(BUNDLE);
        rec.setParameters(new Object[] {"test", "good"});
        String fmt = fmtr.format(rec);
        // System.out.print(fmt);
        assertTrue(hasSuffix(fmt, "This test was good"));
    }
    
    /**
     * Test resource bundle with incorrect parameters being substituted.
     */
    @Test
    public void testResourceBundleIllegalParameters() {
        LogRecord rec = new LogRecord(Level.SEVERE, "test_number");
        rec.setResourceBundleName(BUNDLE);
        rec.setResourceBundle(ResourceBundle.getBundle(BUNDLE));
        rec.setParameters(new Object[] {"one"});
        String fmt = fmtr.format(rec);
        // System.out.print(fmt);
        assertTrue(fmt.contains("IllegalArgumentException"));   
    }
    
    /**
     * Test resource bundle with a missing message but (exception caught).
     */
    @Test
    public void testResourceBundleMissingMsg() {
        LogRecord rec = new LogRecord(Level.SEVERE, "missing_message");
        rec.setResourceBundle(ResourceBundle.getBundle(BUNDLE));
        rec.setResourceBundleName(BUNDLE);
        rec.setParameters(new Object[] {"zero", "one", "two", "three"});
        String fmt = fmtr.format(rec);
        // System.out.print(fmt);
        assertTrue(fmt.contains("not found in"));
        assertTrue(hasSuffix(fmt, "params [0]=zero, [1]=one, [2]=two, [3]=three"));
    }
    
    /**
     * Test resource bundle with a missing message containing parameters (exception caught).
     */
    @Test
    public void testResourceBundleMissingMsgContainsParams() {
        LogRecord rec = new LogRecord(Level.SEVERE, "mm {0} {1} {2} {3}");
        rec.setResourceBundle(ResourceBundle.getBundle(BUNDLE));
        rec.setResourceBundleName(BUNDLE);
        rec.setParameters(new Object[] {"zero", "one", "two", "three"});
        String fmt = fmtr.format(rec);
        // System.out.print(fmt);
        assertTrue(fmt.contains("not found in"));
        assertTrue(hasSuffix(fmt, "params [0]=zero, [1]=one, [2]=two, [3]=three"));
    }
    
    /**
     * Verify that class name extractor will handle "" class names (no exception).
     */
    @Test
    public void verifyClassNameExtractor() {
        LogRecord lr = new LogRecord(Level.INFO, "HelloWorld");
        lr.setSourceClassName("");
        LogFormatter f = new LogFormatter();
        f.format(lr);
    }
    
    /**
     * Verify the EOL constructor.
     */
    @Test
    public void multiLine() {
        LogRecord lr = new LogRecord(Level.INFO, "HelloWorld");
        LogFormatter f = new LogFormatter(LogFormatter.HDR_FMT, LogFormatter.FMT_BUFF_SIZE, true);
        String msg = f.format(lr);
        int firstIndex = msg.indexOf(EOL);
        int lastIndex = msg.lastIndexOf(EOL);
        assertTrue(lastIndex > firstIndex);
    }
}
