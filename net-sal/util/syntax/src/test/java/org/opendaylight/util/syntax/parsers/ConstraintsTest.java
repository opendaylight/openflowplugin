/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import static org.opendaylight.util.junit.TestTools.ON_WINDOWS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

/**
 * JUnit tests to verify functionality of the built-in constraints
 * implementations.
 *
 * @author Thomas Vachuska
 */
public class ConstraintsTest {
    
    @Test
    public void testStringConstraints() {
        // Must have the default constructor for serialization.
        StringConstraints sc = new StringConstraints();

        sc = new StringConstraints(null, null, null, null, null, null);
        assertTrue("anything should be valid", sc.isValid("aaa"));
        assertTrue("anything should be valid", sc.isValid("zzz"));

        sc = new StringConstraints("abc", null, null, null, null, null);
        assertTrue("abc should be valid", sc.isValid("abc"));
        assertTrue("zzz should be valid", sc.isValid("zzz"));
        assertTrue("aba should not be valid", !sc.isValid("aba"));

        sc = new StringConstraints("abc", "xyz", null, null, null, null);
        assertTrue("abc should be valid", sc.isValid("abc"));
        assertTrue("xyz should be valid", sc.isValid("xyz"));
        assertTrue("aba should not be valid", !sc.isValid("aba"));
        assertTrue("xzy should not be valid", !sc.isValid("xzy"));

        sc = new StringConstraints("abc", "xyz", "cat|dog|bi*rd", null, 
                                   null, null);
        assertTrue("cat should be valid", sc.isValid("cat"));
        assertTrue("dog should be valid", sc.isValid("dog"));
        assertTrue("bird should be valid", sc.isValid("bird"));
        assertTrue("bi should be valid", sc.isValid("bi"));
        assertTrue("abc should not be valid", !sc.isValid("abc"));
        assertTrue("xyz should not be valid", !sc.isValid("zzz"));
        assertTrue("aba should not be valid", !sc.isValid("aba"));
        assertTrue("xzy should not be valid", !sc.isValid("xzy"));

        sc = new StringConstraints("abc", "xyz", "cat|dog|bi*rd", "dog|Dog", 
                                   null, null);
        assertTrue("cat should be valid", sc.isValid("cat"));
        assertTrue("dog should not be valid", !sc.isValid("dog"));
        assertTrue("bird should be valid", sc.isValid("bird"));
        assertTrue("bi should be valid", sc.isValid("bi"));
        assertTrue("abc should not be valid", !sc.isValid("abc"));
        assertTrue("xyz should not be valid", !sc.isValid("zzz"));
        assertTrue("aba should not be valid", !sc.isValid("aba"));
        assertTrue("xzy should not be valid", !sc.isValid("xzy"));

        sc = new StringConstraints(null, null, null, null, "j[0-9]+", null);
        assertTrue("j123 should be valid", sc.isValid("j123"));
        assertTrue("J123 should not be valid", !sc.isValid("J123"));

        sc = new StringConstraints(null, null, null, null, null, "j[0-9]+");
        assertTrue("j123 should not be valid", !sc.isValid("j123"));
        assertTrue("J123 should be valid", sc.isValid("J123"));

        sc = new StringConstraints(null, null, null, null, null, "-.+");
        assertTrue("-f should not be valid", !sc.isValid("-f"));
        assertTrue("-foo should not be valid", !sc.isValid("-foo"));
        assertTrue("- should be valid", sc.isValid("-"));
        assertTrue("foo should be valid", sc.isValid("foo"));

        sc = new StringConstraints(null, null, null, null, "j[0-9]+", ".*6.*");
        assertTrue("j123 should be valid", sc.isValid("j123"));
        assertTrue("J123 should not be valid", !sc.isValid("J123"));
        assertTrue("j1236 should not be valid", !sc.isValid("j1236"));

        
        // Must have the default constructor for serialization.
        new StringListConstraints();
    }

    @Test
    public void testNumberConstraints() {
        // Must have the default constructor for serialization.
        NumberConstraints nc = new NumberConstraints();

        Locale loc = Locale.US;
        nc = new NumberConstraints(loc, null, null, false, false, null, null, null);
        assertTrue("format should not be null", nc.getFormat() != null);
        assertTrue("123 should be valid", nc.isValid(new Integer(123)));
        assertTrue("123L should be valid", nc.isValid(new Long(123L)));
        assertTrue("128 should be valid", nc.isValid(new Byte((byte) 128)));
        assertTrue("1024 should be valid", nc.isValid(new Short((short) 1024)));
        assertTrue("999 should be valid", nc.isValid(new Float(999)));
        assertTrue("99 should be valid", nc.isValid(new Double(99)));
                   
        nc = new NumberConstraints(loc, "100", null, false, false, null, null, null);
        assertTrue("100 should be valid", nc.isValid(new Integer(100)));
        assertTrue("123 should be valid", nc.isValid(new Integer(123)));
        assertTrue("99 should not be valid", !nc.isValid(new Integer(99)));

        nc = new NumberConstraints(loc, null, "1000", false, false, null, null, null);
        assertTrue("99 should be valid", nc.isValid(new Integer(99)));
        assertTrue("100 should be valid", nc.isValid(new Integer(100)));
        assertTrue("1024 should not be valid", !nc.isValid(new Integer(1024)));

        nc = new NumberConstraints(loc, "100", "1000", false, false, null, null, null);
        assertTrue("100 should be valid", nc.isValid(new Integer(100)));
        assertTrue("500 should be valid", nc.isValid(new Integer(500)));
        assertTrue("1000 should be valid", nc.isValid(new Integer(1000)));
        assertTrue("99 should be valid", !nc.isValid(new Integer(99)));
        assertTrue("1024 should not be valid", !nc.isValid(new Integer(1024)));

        nc = new NumberConstraints(loc, null, null, true, false, null, null, null);
        assertTrue("100 should be valid", nc.isValid(new Integer(100)));
        assertTrue("100.001 should be valid", nc.isValid(new Float(100.001)));

        // Must have the default constructor for serialization.
        new NumberListConstraints();
    }

    
    @Test
    public void testUnitNumberConstraints() {
        // Must have the default constructor for serialization.
        NumberConstraints nc = new NumberConstraints();

        Locale loc = Locale.US;
        nc = new NumberConstraints(loc, null, null, false, false, null, "k*b,m*b,g*b", "1024,1048576,1073741824");
        assertEquals("unit name is incorrect", "mb", nc.getUnitName("256mb"));
        assertEquals("value is incorrect", 2048L, nc.getUnitBasedValue(2, "k"));
        assertEquals("value is incorrect", 4096L, nc.getUnitBasedValue(4, "kb"));

        nc = new NumberConstraints(loc, null, null, false, false, "[kmg]", "k*b,m*b,g*b", "1024,1048576,1073741824");
        assertEquals("unit name is incorrect", "mb", nc.getUnitName("256mb"));
        assertEquals("value is incorrect", 2048L, nc.getUnitBasedValue(2, "k"));
        assertEquals("value is incorrect", 4096L, nc.getUnitBasedValue(4, "kb"));
        assertEquals("value is incorrect", 2048.00, nc.getUnitBasedValue(2.0, "k"));

        nc = new NumberConstraints(loc, null, null, false, false, null, 
                                   "s*econds|S*econds,m*inutes|M*inutes,h*ours|H*ours,d*ays|D*ays,w*eeks|W*eeks",
                                   "1000,60000,3600000,86400000,604800000");
        assertEquals("unit name is incorrect", "weeks", nc.getUnitName("4weeks"));
        assertEquals("value is incorrect", 2419200000L, nc.getUnitBasedValue(4, "weeks"));
}

    @Test(expected=IllegalArgumentException.class)
    public void testBadUnitNumberConstraints1() {
        new NumberConstraints(Locale.US, null, null, false, false, null, "k,b", "1024,1048576,1073741824");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadUnitNumberConstraints2() {
        new NumberConstraints(Locale.US, null, null, false, false, null, "k,b", null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadUnitNumberConstraints3() {
        new NumberConstraints(Locale.US, null, null, false, false, null, null, "1024,1048576,1073741824");
    }

    public static final String FORMAT_STRING = "yyyy/MM/dd-HH:mm:ss";

    public static final SimpleDateFormat FORMAT = 
        new SimpleDateFormat(FORMAT_STRING);

    public static Date getDate(String s) {
        return FORMAT.parse(s, new ParsePosition(0));
    }

    @Test
    public void testDateConstraints() {
        // Must have the default constructor for serialization.
        DateConstraints dc = new DateConstraints();

        Locale loc = Locale.US;
        dc = new DateConstraints(null, loc, null, null);
        assertTrue("format should not be null", dc.getFormat() != null);
        assertTrue("should be correct", 
                   dc.isValid(getDate("2000/06/13-13:23:45")));
        assertTrue("should be correct", 
                   dc.isValid(getDate("2004/05/15-09:35:49")));

        dc = new DateConstraints(FORMAT_STRING, loc, 
                                 "2000/01/01-00:00:00", null);
        assertTrue("format should not be null", dc.getFormat() != null);
        assertTrue("should be correct", 
                   dc.isValid(getDate("2000/01/01-00:00:00")));
        assertTrue("should be correct", 
                   dc.isValid(getDate("2004/05/15-09:35:49")));
        assertTrue("should not be correct", 
                   !dc.isValid(getDate("1999/12/31-23:59:59")));

        dc = new DateConstraints(FORMAT_STRING, loc,
                                 null, "2000/01/01-00:00:00");
        assertTrue("format should not be null", dc.getFormat() != null);
        assertTrue("should be correct", 
                   dc.isValid(getDate("2000/01/01-00:00:00")));
        assertTrue("should be correct", 
                   dc.isValid(getDate("1999/12/31-23:59:59")));
        assertTrue("should not be correct", 
                   !dc.isValid(getDate("2000/01/01-00:00:01")));

        dc = new DateConstraints(FORMAT_STRING, loc,  
                                 "2000/01/01-00:00:00", "2004/01/01-00:00:00");
        assertTrue("format should not be null", dc.getFormat() != null);
        assertTrue("should be correct", 
                   dc.isValid(getDate("2000/01/01-00:00:00")));
        assertTrue("should be correct", 
                   dc.isValid(getDate("2003/12/31-23:59:59")));
        assertTrue("should be correct", 
                   dc.isValid(getDate("2004/01/01-00:00:00")));
        assertTrue("should not be correct", 
                   !dc.isValid(getDate("2004/01/01-00:00:01")));
        assertTrue("should not be correct", 
                   !dc.isValid(getDate("1999/12/31-23:59:59")));

        // Must have the default constructor for serialization.
        new DateListConstraints();
    }

    @Test
    public void testBooleanConstraints() {
        // Must have the default constructor for serialization.
        BooleanConstraints bc = new BooleanConstraints();

        bc = new BooleanConstraints("pravda", "lez");
        assertEquals("incorrect 'true'", "pravda", bc.getTrueValue());
        assertEquals("incorrect 'false'", "lez", bc.getFalseValue());
        assertTrue("should be correct", bc.isValid("PRAVDA"));
        assertTrue("should be correct", bc.isValid("LEZ"));
    }

    @Test
    public void testFileConstraints() throws Exception {
        // Must have the default constructor for serialization.
        FileConstraints fc = new FileConstraints();
        assertEquals("incorrect flags", "", fc.getFlags());
        assertTrue("String is not a File", !fc.isValid("foobar"));

        File d = new File(".");
        File f = new File("./foobar.test.dat");
        f.delete();

        fc = new FileConstraints("derw");
        assertEquals("incorrect flags", "derw", fc.getFlags());
        assertTrue("should be directory", fc.isValid(d));
        assertTrue("should not be file", !fc.isValid(f));
        
        fc = new FileConstraints("ferw");
        assertEquals("incorrect flags", "ferw", fc.getFlags());
        assertTrue("should not be directory", !fc.isValid(d));
        assertTrue("should not be file yet", !fc.isValid(f));
        f.createNewFile();
        assertTrue("should be file now", fc.isValid(f));
        f.setReadOnly();
        
        // FIXME: Verify this on Linux
        if (ON_WINDOWS)
            assertTrue("should not be writable now", !fc.isValid(f));
        f.delete();
    }

}
