/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import org.opendaylight.util.format.TokenTranslator;
import org.opendaylight.util.syntax.ParserLoader;
import org.opendaylight.util.syntax.SyntaxPackage;

import org.junit.Test;

/**
 * JUnit tests to verify functionality of the built-in parsers
 * implementations.
 *
 * @author Thomas Vachuska
 */
public class ParsersTest {

    private static class TT implements TokenTranslator {
        @Override
        public String translate(String string) {
            return string;
        }
    }

    @Test
    public void testStringParameterParser() {
        // Must have the default constructor for serialization.
        StringParameterParser spp = new StringParameterParser();
        assertEquals("type is incorrect:", "string", spp.getTypeToken());
        assertEquals("foo should be foo:", "foo", spp.parse("foo", null, null));

        Properties p = new Properties();
        TT tt = new TT();
        spp = new StringParameterParser(Locale.US);
        assertEquals("locale is incorrect:", Locale.US, spp.getLocale());

        StringConstraints sc = (StringConstraints) spp.parse(p, tt, null);
        assertTrue("sc should be null", sc == null);

        p.setProperty("minValue", "abc");
        sc = (StringConstraints) spp.parse(p, tt, null);
        assertTrue("sc should not be null", sc != null);
        assertEquals("invalid parsing:", "abc", spp.parse("abc", sc, null));
        assertEquals("invalid parsing:", "zzz", spp.parse("zzz", sc, null));
        assertEquals("invalid parsing:", null, spp.parse("aba", sc, null));

        p.setProperty("maxValue", "xyz");
        sc = (StringConstraints) spp.parse(p, tt, null);
        assertTrue("sc should not be null", sc != null);
        assertEquals("invalid parsing:", "abc", spp.parse("abc", sc, null));
        assertEquals("invalid parsing:", "xyz", spp.parse("xyz", sc, null));
        assertEquals("invalid parsing:", null, spp.parse("aba", sc, null));
        assertEquals("invalid parsing:", null, spp.parse("xzy", sc, null));

        p.setProperty("legalValues", "dog|cat|zebra");
        sc = (StringConstraints) spp.parse(p, tt, null);
        assertTrue("sc should not be null", sc != null);
        assertEquals("invalid parsing:", "dog", spp.parse("dog", sc, null));
        assertEquals("invalid parsing:", "cat", spp.parse("cat", sc, null));
        assertEquals("invalid parsing:", null, spp.parse("bird", sc, null));
        assertEquals("invalid parsing:", null, spp.parse("zebra", sc, null));
    }


    @SuppressWarnings("unchecked")
    private void testList(Serializable o, Object[] v) {
        ArrayList<Serializable> l = (ArrayList<Serializable>) o;
        Iterator<Serializable> it = l.iterator();
        int i = 0;
        assertEquals("incorrect list length:", v.length, l.size());
        while (it.hasNext()) {
            assertEquals("incorrect item[" + i + "]:", v[i], it.next());
            i++;
        }
    }

    @Test
    public void testStringListParameterParser() {
        // Must have the default constructor for serialization.
        StringListParameterParser slpp = new StringListParameterParser();
        assertEquals("type is incorrect:", "strings", slpp.getTypeToken());

        ArrayList<String> r = new ArrayList<String>();
        r.add("foo");
        assertEquals("foo should be foo:", r,
                     slpp.parse("foo", null, null));
        r.add("bar");
        assertEquals("foo,bar should be foo,bar:", r, 
                     slpp.parse("foo,bar", null, null));

        Properties p = new Properties();
        TT tt = new TT();
        slpp = new StringListParameterParser(Locale.US);
        assertEquals("locale is incorrect:", Locale.US, slpp.getLocale());

        StringListConstraints slc = 
            (StringListConstraints) slpp.parse(p, tt, null);
        assertTrue("slc should be null", slc == null);

        p.setProperty("minValue", "abc");
        slc = (StringListConstraints) slpp.parse(p, tt, null);
        assertTrue("slc should not be null", slc != null);
        testList(slpp.parse("abc,xyz,zzz", slc, null),
                 new String[]{"abc", "xyz", "zzz"});
        assertEquals("list is incorrect:", null, 
                     slpp.parse("aba,xyz,zzz", slc, null));

        p.setProperty("maxValue", "xyz");
        slc = (StringListConstraints) slpp.parse(p, tt, null);
        assertTrue("slc should not be null", slc != null);
        testList(slpp.parse("abc,xyz,ttt", slc, null),
                 new String[]{"abc", "xyz", "ttt"});
        assertEquals("list is incorrect:", null, 
                     slpp.parse("abc,xyz,zzz", slc, null));

        p.setProperty("legalValues", "cat|dog|zebra");
        slc = (StringListConstraints) slpp.parse(p, tt, null);
        assertTrue("slc should not be null", slc != null);
        testList(slpp.parse("cat,dog,cat", slc, null),
                 new String[]{"cat", "dog", "cat"});
        assertEquals("list is incorrect:", null, 
                     slpp.parse("cat,zebra,dog", slc, null));

        p.setProperty("maxLength", "2");
        slc = (StringListConstraints) slpp.parse(p, tt, null);
        assertTrue("slc should not be null", slc != null);
        testList(slpp.parse("cat,dog", slc, null),
                 new String[]{"cat", "dog"});
        assertEquals("list is incorrect:", null, 
                     slpp.parse("cat,dog,cat", slc, null));

        p.setProperty("minLength", "2");
        slc = (StringListConstraints) slpp.parse(p, tt, null);
        assertTrue("slc should not be null", slc != null);
        testList(slpp.parse("cat,dog", slc, null),
                 new String[]{"cat", "dog"});
        assertEquals("list is incorrect:", null, 
                     slpp.parse("cat,dog,cat", slc, null));
        assertEquals("list is incorrect:", null, 
                     slpp.parse("cat", slc, null));

        p.setProperty("separator", ".");
        slc = (StringListConstraints) slpp.parse(p, tt, null);
        assertTrue("slc should not be null", slc != null);
        testList(slpp.parse("cat.dog", slc, null),
                 new String[]{"cat", "dog"});
        assertEquals("list is incorrect:", null, 
                     slpp.parse("cat,dog", slc, null));
        assertEquals("list is incorrect:", null, 
                     slpp.parse("cat.dog.cat", slc, null));
        assertEquals("list is incorrect:", null, 
                     slpp.parse("cat", slc, null));
    }


    @Test
    public void testNumberParameterParser() {
        // Must have the default constructor for serialization.
        NumberParameterParser npp = new NumberParameterParser();
        assertEquals("type is incorrect:", "number", npp.getTypeToken());
        assertEquals("123 should be 123:", new Long(123), 
                     npp.parse("123", null, null));

        Properties p = new Properties();
        TT tt = new TT();
        npp = new NumberParameterParser(Locale.US);
        assertEquals("locale is incorrect:", Locale.US, npp.getLocale());

        NumberConstraints nc = (NumberConstraints) npp.parse(p, tt, null);
        assertTrue("nc should be null", nc == null);

        p.setProperty("minValue", "128");
        nc = (NumberConstraints) npp.parse(p, tt, null);
        assertTrue("nc should not be null", nc != null);
        assertEquals("invalid parsing:", new Long(128),
                     npp.parse("128", nc, null));
        assertEquals("invalid parsing:", new Long(999),
                     npp.parse("999", nc, null));
        assertEquals("invalid parsing:", null,
                     npp.parse("127", nc, null));

        p.setProperty("maxValue", "999");
        nc = (NumberConstraints) npp.parse(p, tt, null);
        assertTrue("nc should not be null", nc != null);
        assertEquals("invalid parsing:", new Long(128),
                     npp.parse("128", nc, null));
        assertEquals("invalid parsing:", new Long(999),
                     npp.parse("999", nc, null));
        assertEquals("invalid parsing:", null,
                     npp.parse("127", nc, null));
        assertEquals("invalid parsing:", null,
                     npp.parse("1000", nc, null));
        assertEquals("invalid parsing:", null,
                     npp.parse("500.25", nc, null));

        p.setProperty("allowDecimals", "on");
        nc = (NumberConstraints) npp.parse(p, tt, null);
        assertTrue("nc should not be null", nc != null);
        assertEquals("invalid parsing:", new Long(128),
                     npp.parse("128", nc, null));
        assertEquals("invalid parsing:", new Long(999),
                     npp.parse("999", nc, null));
        assertEquals("invalid parsing:", new Double(500.25),
                     npp.parse("500.25", nc, null));
        assertEquals("invalid parsing:", null,
                     npp.parse("127", nc, null));
        assertEquals("invalid parsing:", null,
                     npp.parse("1000", nc, null));

        p.setProperty("maxValue", "999999");
        p.setProperty("useGrouping", "on");
        nc = (NumberConstraints) npp.parse(p, tt, null);
        assertEquals("invalid parsing:", new Long(1000),
                     npp.parse("1,000", nc, null));
    }


    @Test
    public void testNumberListParameterParser() {
        // Must have the default constructor for serialization.
        NumberListParameterParser nlpp = new NumberListParameterParser();
        assertEquals("type is incorrect:", "numbers", nlpp.getTypeToken());

        ArrayList<Long> r = new ArrayList<Long>();
        r.add(new Long(123));
        assertEquals("123 should be 123:", r,
                     nlpp.parse("123", null, null));
        r.add(new Long(999));
        assertEquals("123,999 should be 123,999:", r, 
                     nlpp.parse("123,999", null, null));


        Properties p = new Properties();
        TT tt = new TT();
        nlpp = new NumberListParameterParser(Locale.US);
        NumberListConstraints nlc = 
            (NumberListConstraints) nlpp.parse(p, tt, null);
        assertTrue("nlc should be null", nlc == null);

        p.setProperty("minValue", "100");
        nlc = (NumberListConstraints) nlpp.parse(p, tt, null);
        assertTrue("nlc should not be null", nlc != null);
        testList(nlpp.parse("100,999,1000", nlc, null),
                 new Number[]{new Long(100), new Long(999), new Long(1000)});
        assertEquals("list is incorrect:", null, 
                     nlpp.parse("99,999,1000", nlc, null));

        p.setProperty("maxValue", "999");
        nlc = (NumberListConstraints) nlpp.parse(p, tt, null);
        assertTrue("nlc should not be null", nlc != null);
        testList(nlpp.parse("100,999,500", nlc, null),
                 new Number[]{new Long(100), new Long(999), new Long(500)});
        assertEquals("list is incorrect:", null, 
                     nlpp.parse("100,999,1000", nlc, null));

        p.setProperty("minLength", "2");
        nlc = (NumberListConstraints) nlpp.parse(p, tt, null);
        assertTrue("nlc should not be null", nlc != null);
        testList(nlpp.parse("100,999", nlc, null),
                 new Number[]{new Long(100), new Long(999)});
        testList(nlpp.parse("100,999,500", nlc, null),
                 new Number[]{new Long(100), new Long(999), new Long(500)});
        assertEquals("list is incorrect:", null, 
                     nlpp.parse("200", nlc, null));

        p.setProperty("maxLength", "2");
        nlc = (NumberListConstraints) nlpp.parse(p, tt, null);
        assertTrue("nlc should not be null", nlc != null);
        testList(nlpp.parse("100,999", nlc, null),
                 new Number[]{new Long(100), new Long(999)});
        assertEquals("list is incorrect:", null, 
                     nlpp.parse("200", nlc, null));
        assertEquals("list is incorrect:", null, 
                     nlpp.parse("200,500,999", nlc, null));

        p.setProperty("separator", "~");
        nlc = (NumberListConstraints) nlpp.parse(p, tt, null);
        assertTrue("nlc should not be null", nlc != null);
        testList(nlpp.parse("100~999", nlc, null),
                 new Number[]{new Long(100), new Long(999)});
        assertEquals("list is incorrect:", null, 
                     nlpp.parse("100,200", nlc, null));

        p.setProperty("allowDecimals", "yes");
        nlc = (NumberListConstraints) nlpp.parse(p, tt, null);
        assertTrue("nlc should not be null", nlc != null);
        testList(nlpp.parse("100.001~998.999", nlc, null),
                 new Number[]{new Double(100.001), new Double(998.999)});

        p.setProperty("maxValue", "1,000,000,000.00");
        p.setProperty("useGrouping", "yes");
        nlc = (NumberListConstraints) nlpp.parse(p, tt, null);
        assertTrue("nlc should not be null", nlc != null);
        testList(nlpp.parse("1,000,000.001~998,321.999", nlc, null),
                 new Number[]{new Double(1000000.001), new Double(998321.999)});
    }


    public static final String FORMAT_STRING = "yyyy/MM/dd-HH:mm:ss";

    public static final SimpleDateFormat FORMAT = 
        new SimpleDateFormat(FORMAT_STRING);

    public static Date getDate(String s) {
        return FORMAT.parse(s, new ParsePosition(0));
    }

    @Test
    public void testDateParameterParser() {
        // Must have the default constructor for serialization.
        DateParameterParser dpp = new DateParameterParser();
        assertEquals("type is incorrect:", "date", dpp.getTypeToken());
        assertEquals("invalid parsing:", getDate("2004/05/16-12:39:40"), 
                     dpp.parse("2004-05-16/12:39:40", null, null));

        Properties p = new Properties();
        TT tt = new TT();
        dpp = new DateParameterParser(Locale.US);
        assertEquals("locale is incorrect:", Locale.US, dpp.getLocale());

        DateConstraints dc = (DateConstraints) dpp.parse(p, tt, null);
        assertTrue("dc should be null", dc == null);

        p.setProperty("format", "yyyy-MM-dd/HH-mm-ss");
        dc = (DateConstraints) dpp.parse(p, tt, null);
        assertTrue("dc should not be null", dc != null);

        p.setProperty("minValue", "2000-01-01/00-00-00");
        dc = (DateConstraints) dpp.parse(p, tt, null);
        assertTrue("dc should not be null", dc != null);
        assertEquals("invalid parsing:", getDate("2000/01/01-00:00:00"),
                     dpp.parse("2000-01-01/00-00-00", dc, null));
        assertEquals("invalid parsing:", getDate("2004/01/01-00:00:00"),
                     dpp.parse("2004-01-01/00-00-00", dc, null));
        assertEquals("invalid parsing:", null,
                     dpp.parse("1999-12-31/23-59-59", dc, null));

        p.setProperty("maxValue", "2004-12-31/23-59-59");
        dc = (DateConstraints) dpp.parse(p, tt, null);
        assertTrue("dc should not be null", dc != null);
        assertEquals("invalid parsing:", getDate("2000/01/01-00:00:00"),
                     dpp.parse("2000-01-01/00-00-00", dc, null));
        assertEquals("invalid parsing:", getDate("2004/12/31-23:59:59"),
                     dpp.parse("2004-12-31/23-59-59", dc, null));
        assertEquals("invalid parsing:", null,
                     dpp.parse("1999-12-31/23-59-59", dc, null));
        assertEquals("invalid parsing:", null,
                     dpp.parse("2005-01-01/00-00-00", dc, null));
    }


    @Test
    public void testDateListParameterParser() {
        // Must have the default constructor for serialization.
        DateListParameterParser dlpp = new DateListParameterParser();
        assertEquals("type is incorrect:", "dates", dlpp.getTypeToken());

        ArrayList<Date> r = new ArrayList<Date>();
        r.add(getDate("2000/01/01-00:00:00"));
        assertEquals("invalid parsing:", r,
                     dlpp.parse("2000-01-01/00:00:00", null, null));
        r.add(getDate("2004/12/31-23:59:59"));
        assertEquals("invalid parsing:", r, 
                     dlpp.parse("2000-01-01/00:00:00,2004-12-31/23:59:59",
                                null, null));

        Properties p = new Properties();
        TT tt = new TT();
        dlpp = new DateListParameterParser(Locale.US);
        DateListConstraints dlc = (DateListConstraints) dlpp.parse(p, tt, null);
        assertTrue("dlc should be null", dlc == null);

        p.setProperty("format", "yyyy-MM-dd/HH-mm-ss");
        dlc = (DateListConstraints) dlpp.parse(p, tt, null);
        assertTrue("dlc should not be null", dlc != null);
        testList(dlpp.parse("2000-01-01/00-00-00,2001-01-01/00-00-59", 
                            dlc, null),
                 new Date[]{getDate("2000/01/01-00:00:00"),
                            getDate("2001/01/01-00:00:59")});

        p.setProperty("minValue", "2000-01-01/00-00-00");
        dlc = (DateListConstraints) dlpp.parse(p, tt, null);
        assertTrue("dlc should not be null", dlc != null);
        testList(dlpp.parse("2000-01-01/00-00-00,2001-01-01/00-00-59", 
                            dlc, null),
                 new Date[]{getDate("2000/01/01-00:00:00"),
                            getDate("2001/01/01-00:00:59")});
        assertEquals("invalid parsing:", null, 
                     dlpp.parse("1999-12-31/23-59-59,2004-12-31/23-59-59",
                                dlc, null));

        p.setProperty("maxValue", "2005-01-01/00-00-00");
        dlc = (DateListConstraints) dlpp.parse(p, tt, null);
        assertTrue("dlc should not be null", dlc != null);
        testList(dlpp.parse("2000-01-01/00-00-00,2001-01-01/00-00-59", 
                            dlc, null),
                 new Date[]{getDate("2000/01/01-00:00:00"),
                            getDate("2001/01/01-00:00:59")});
        assertEquals("invalid parsing:", null, 
                     dlpp.parse("1999-12-31/23-59-59,2004-12-31/23-59-59",
                                dlc, null));
        assertEquals("invalid parsing:", null, 
                     dlpp.parse("2000-12-31/23-59-59,2005-01-01/00-00-59",
                                dlc, null));

        p.setProperty("minLength", "2");
        dlc = (DateListConstraints) dlpp.parse(p, tt, null);
        assertTrue("dlc should not be null", dlc != null);
        testList(dlpp.parse("2000-01-01/00-00-00,2001-01-01/00-00-59", 
                            dlc, null),
                 new Date[]{getDate("2000/01/01-00:00:00"),
                            getDate("2001/01/01-00:00:59")});
        testList(dlpp.parse("2000-01-01/00-00-00,2001-01-01/00-00-59," +
                            "2002-01-01/00-00-59", dlc, null),
                 new Date[]{getDate("2000/01/01-00:00:00"),
                            getDate("2001/01/01-00:00:59"),
                            getDate("2002/01/01-00:00:59")});
        assertEquals("invalid parsing:", null, 
                     dlpp.parse("2000-12-31/23-59-59", dlc, null));

        p.setProperty("maxLength", "2");
        dlc = (DateListConstraints) dlpp.parse(p, tt, null);
        assertTrue("dlc should not be null", dlc != null);
        testList(dlpp.parse("2000-01-01/00-00-00,2001-01-01/00-00-59", 
                            dlc, null),
                 new Date[]{getDate("2000/01/01-00:00:00"),
                            getDate("2001/01/01-00:00:59")});
        assertEquals("invalid parsing:", null, 
                     dlpp.parse("2000-12-31/23-59-59", dlc, null));
        assertEquals("invalid parsing:", null, 
                     dlpp.parse("2000-01-01/00-00-00,2001-01-01/00-00-59," +
                                "2002-01-01/00-00-59", dlc, null));

        p.setProperty("separator", "~");
        dlc = (DateListConstraints) dlpp.parse(p, tt, null);
        assertTrue("dlc should not be null", dlc != null);
        testList(dlpp.parse("2000-01-01/00-00-00~2001-01-01/00-00-59", 
                            dlc, null),
                 new Date[]{getDate("2000/01/01-00:00:00"),
                            getDate("2001/01/01-00:00:59")});
        assertEquals("invalid parsing:", null, 
                     dlpp.parse("2001-12-31/23-59-59,2004-12-31/23-59-59",
                                dlc, null));

    }

    @Test
    public void testBooleanParameterParser() {
        // Must have the default constructor for serialization.
        BooleanParameterParser bpp = new BooleanParameterParser();
        assertEquals("type is incorrect:", "boolean", bpp.getTypeToken());
        assertEquals("true should be true:", Boolean.TRUE, 
                     bpp.parse("true", null, null));
        assertEquals("false should be false:", Boolean.FALSE, 
                     bpp.parse("false", null, null));
        assertEquals("invalid parsing:", null, 
                     bpp.parse("trueness", null, null));

        Properties p = new Properties();
        TT tt = new TT();
        bpp = new BooleanParameterParser(Locale.US);
        assertEquals("locale is incorrect:", Locale.US, bpp.getLocale());

        BooleanConstraints bc = (BooleanConstraints) bpp.parse(p, tt, null);
        assertTrue("bc should be null", bc == null);
        assertEquals("invalid parsing:", Boolean.TRUE, 
                     bpp.parse("True", bc, null));
        assertEquals("invalid parsing:", Boolean.FALSE, 
                     bpp.parse("FALSE", bc, null));
        assertEquals("invalid parsing:", null, 
                     bpp.parse("falsetto", bc, null));

        p.setProperty("true", "pravda");
        p.setProperty("false", "lez");
        bc = (BooleanConstraints) bpp.parse(p, tt, null);
        assertTrue("bc should not be null", bc != null);
        assertEquals("invalid parsing:", Boolean.TRUE, 
                     bpp.parse("pravda", bc, null));
        assertEquals("invalid parsing:", Boolean.FALSE, 
                     bpp.parse("lez", bc, null));
        assertEquals("invalid parsing:", null, 
                     bpp.parse("true", bc, null));
        assertEquals("invalid parsing:", null, 
                     bpp.parse("false", bc, null));
    }

    @Test
    public void testFileParameterParser() {
        // Must have the default constructor for serialization.
        FileParameterParser fpp = new FileParameterParser();
        assertEquals("type is incorrect:", "file", fpp.getTypeToken());
        assertEquals(". should be OK:", new File("."), 
                     fpp.parse(".", null, null));
        assertEquals("./foobar.nix should not be OK:", null, 
                     fpp.parse("./foobar.nix", null, null));

        Properties p = new Properties();
        TT tt = new TT();
        fpp = new FileParameterParser(Locale.US);
        assertEquals("locale is incorrect:", Locale.US, fpp.getLocale());

        FileConstraints fc = (FileConstraints) fpp.parse(p, tt, null);
        assertTrue("fc should be null", fc == null);

        fc = (FileConstraints) fpp.parse(p, tt, null);
        assertTrue("fc should be null", fc == null);
        assertEquals(". should be OK:", new File("."), 
                     fpp.parse(".", fc, null));
        assertEquals("./foobar.nix should not be OK:", null, 
                     fpp.parse("./foobar.nix", fc, null));
        
        p.setProperty("flags", "derw");
        fc = (FileConstraints) fpp.parse(p, tt, null);
        assertTrue("fc should not be null", fc != null);
        assertEquals(". should be OK:", new File("."), 
                     fpp.parse(".", fc, null));
        assertEquals("./foobar.nix should not be OK:", null, 
                     fpp.parse("./foobar.nix", fc, null));

        File f = new File("./foobar");
        f.delete();

        p.setProperty("flags", "ferw");
        fc = (FileConstraints) fpp.parse(p, tt, null);
        assertTrue("fc should not be null", fc != null);
        assertEquals(". should not be OK:", null, 
                     fpp.parse(".", fc, null));
        assertEquals("./foobar should not be OK yet:", null, 
                     fpp.parse("./foobar", fc, null));
        try {
            f.createNewFile();
            assertEquals("./foobar should be OK now:", f, 
                         fpp.parse("./foobar", fc, null));
            f.delete();
        } catch (IOException e) {
            fail("should be able to create file");
        }
    }


    private ParserLoader getParserLoader() {
        SyntaxPackage sp = new SyntaxPackage(null, Locale.US);
        
        //  Prime the package catalogues with some builtin parsers.
        StringParameterParser stringParser =
            new StringParameterParser(Locale.US);
        sp.addParser(stringParser, ParameterParser.class);
        sp.addParser(stringParser, ConstraintsParser.class);

        NumberParameterParser numberParser =
            new NumberParameterParser(Locale.US);
        sp.addParser(numberParser, ParameterParser.class);
        sp.addParser(numberParser, ConstraintsParser.class);
        
        return sp;
    }
    
    private void validatePair(String token, String name, Serializable value, 
                              Constraints c, ParameterParser pp) {
        NameValuePair pair = (NameValuePair) pp.parse(token, c, null);
        if (name == null)
            assertTrue("pair should be null", null == pair);
        else {
            assertNotNull("pair should not be null", pair);
            assertEquals("invalid name", name, pair.getName());
            assertEquals("invalid value", value, pair.getValue());
        }
    }

    @Test
    public void testNameValueParameterParser() {
        // NameValuePair needs to have a default constructor for serialization.
        new NameValuePair();
        NameValueConstraints nvc = new NameValueConstraints();
        
        // We'll need a parser loader for this test.
        ParserLoader pl = getParserLoader();
        
        // Must have the default constructor for serialization.
        NameValueParameterParser nvpp = new NameValueParameterParser();
        assertEquals("type is incorrect:", "pair", nvpp.getTypeToken());
        
        Properties p = new Properties();
        TT tt = new TT();
        nvpp = new NameValueParameterParser(Locale.US);
        assertEquals("locale is incorrect:", Locale.US, nvpp.getLocale());

        // Must return no match when no constraints are given.
        assertEquals("should never match without constraints", null,
                     nvpp.parse("foo=bar", null, null));

        // No name or value constraints... should match...
        nvc = (NameValueConstraints) nvpp.parse(p, tt, pl);
        assertTrue("nvc should not be null", nvc != null);
        validatePair("abc=foo", "abc", "foo", nvc, nvpp);

        //  Start setting name constraints first...
        p.setProperty("minName", "abc");
        nvc = (NameValueConstraints) nvpp.parse(p, tt, pl);
        assertTrue("nvc should not be null", nvc != null);
        
        validatePair("abc=foo", "abc", "foo", nvc, nvpp);
        validatePair("zzz=bar", "zzz", "bar", nvc, nvpp);
        validatePair("aba=foo", null, null, nvc, nvpp);

        p.setProperty("maxName", "xyz");
        nvc = (NameValueConstraints) nvpp.parse(p, tt, pl);
        assertTrue("nvc should not be null", nvc != null);
        validatePair("abc=foo", "abc", "foo", nvc, nvpp);
        validatePair("xyz=bar", "xyz", "bar", nvc, nvpp);
        validatePair("aba=foo", null, null, nvc, nvpp);
        validatePair("xzy=foo", null, null, nvc, nvpp);

        p.setProperty("legalNames", "dog|cat|zebra");
        nvc = (NameValueConstraints) nvpp.parse(p, tt, pl);
        assertTrue("sc should not be null", nvc != null);
        validatePair("dog=ruff", "dog", "ruff", nvc, nvpp);
        validatePair("cat=meow", "cat", "meow", nvc, nvpp);
        validatePair("bird=chirp", null, null, nvc, nvpp);
        validatePair("zebra=eehaw", null, null, nvc, nvpp);
     
        //  Test alternate separator...
        p.setProperty("separator", ":");
        nvc = (NameValueConstraints) nvpp.parse(p, tt, pl);
        assertTrue("sc should not be null", nvc != null);
        validatePair("dog:ruff", "dog", "ruff", nvc, nvpp);
        validatePair("cat:meow", "cat", "meow", nvc, nvpp);
        validatePair("bird:chirp", null, null, nvc, nvpp);
        validatePair("zebra:eehaw", null, null, nvc, nvpp);
        validatePair("dog=ruff", null, null, nvc, nvpp);

        //  Test some value constraints...
        p.setProperty("separator", "=");
        p.setProperty("valueType", "number");
        nvc = (NameValueConstraints) nvpp.parse(p, tt, pl);
        assertTrue("sc should not be null", nvc != null);
        validatePair("dog=123", "dog", new Long(123), nvc, nvpp);
        validatePair("cat=321", "cat", new Long(321), nvc, nvpp);
        validatePair("cat=xya", null, null, nvc, nvpp);
    }

}
