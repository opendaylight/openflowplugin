/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.opendaylight.util.format.TokenTranslator;
import org.opendaylight.util.syntax.parsers.AbstractParameterParser;
import org.opendaylight.util.syntax.parsers.Constraints;

import org.junit.Test;

/**
 * JUnit tests to verify functionality of the SyntaxMatcher class.
 *
 * @author Thomas Vachuska
 */
public class SyntaxMatcherTest implements SyntaxKeywords {

    private static final String TEST_BUNDLE =
        "org.opendaylight.util.syntax.TestResources";

    /**
     * Returns the input stream using the data in the string, if the string
     * starts with XML pre-amble, or it returns input stream created from the
     * file.
     * 
     * @param stringOrName string containing data or file name that contains
     *        data
     * @return input stream from the data in the string or named file
     */
    private InputStream getInput(String stringOrName) {
        if (stringOrName.startsWith("<?xml"))
            return new ByteArrayInputStream(stringOrName.getBytes());
        try {
            return new FileInputStream("data/" + stringOrName);
        } catch (FileNotFoundException e) {
            fail("could not find file data/" + stringOrName + 
                 "; pwd=" + new File(".").getAbsolutePath());
            return null;
        }
    }

    /**
     * Returns verified syntax matcher populated with a syntax package from
     * the given input stream using the data in the string, if the string
     * starts with XML pre-amble, or from the stream stream created from the
     * file.
     * 
     * @param stringOrName string with data or file name containing the data
     * @param packageCount package count to expect
     * @param syntaxCount syntax count to expect
     * @param locale locale to use
     * @return syntax matcher created from the data in the string or the file
     * @throws Exception thrown if issues are encountered while parsing the
     *         syntax definitions
     */
    private SyntaxMatcher getMatcher(String stringOrName, int packageCount,
                                     int syntaxCount, Locale locale) throws Exception {
        SyntaxCompiler sc = SyntaxCompilerTest.getCompiler();
        SyntaxPackage sp = sc.compile(getInput(stringOrName), locale);
        SyntaxMatcher sm = new SyntaxMatcher(Locale.US);
        sm.addPackage(sp);
        assertEquals("incorrect number of packages:", packageCount, sm
            .getPackages().size());
        assertEquals("incorrect number of syntaxes:", syntaxCount, sm
            .getSyntaxes().size());
        assertEquals("incorrect locale:", locale, sm.getLocale());
        return sm;
    }


    private static void debug(Object o) { 
        if (System.getenv("DEBUG") != null) 
            System.err.println(o); 
    }


    /**
     * Displays all node details; for debugging purposes only.
     * 
     * @param p parameters to be printed
     */
    public void dump(Parameters p) {
        debug("verifying parameters...");
        Iterator<String> it = p.getNames().iterator();
        while (it.hasNext()) {
            String name = it.next();
            debug("!!!" + name + "=" + p.get(name));
        }
    }

    private void verify(Parameters p, String data) {
        int d = data.indexOf("=");
        assertTrue("incorrect test data", d > 0);
        String name = data.substring(0, d);
        String value = data.substring(d + 1);
        debug("verifying parameter " + name);
        assertEquals("incorrect parameter value:", value,
                     p.get(name) + "");
    }

    private void verify(Syntax s, String name, Parameters p, String[] data) {
        debug("verifying [" + name + "]");
        if (name == null) {
            assertTrue("syntax should be null", s == null);
            return;
        }
        assertEquals("wrong syntax matched:", name, s.getName());
        assertEquals("incorrect number of parsed parameters:", data.length,
                      p.getNames().size());

        for (int i = 0; i < data.length; i++)
            verify(p, data[i]);
    }

    private void verify(BadUsageException e, int i, String snn, String sn) {
            assertEquals("incorrect BadUsageException offset:", i,
                         e.getErrorOffset());
            assertEquals("incorrect BadUsageException node:", snn,
                         e.getSyntaxNode().getName());
            assertEquals("incorrect BadUsageException syntax:", sn,
                         e.getSyntax().getName());
    }

    @Test
    public void testSimpleMatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"simple\">\n" +
            "    <syntax name=\"foo\" usage=\"hello\" actionName=\"fooA\"/>\n" +
            "    <syntax name=\"bar\" usage=\"world\" actionName=\"barA\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 2, Locale.US);
        Parameters p;
        Syntax s;
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "world" }, p);
            verify(s, "simple.bar", p, new String[] {});

            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "hello" }, p);
            verify(s, "simple.foo", p, new String[] {});

        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }
    }

    @Test
    public void testSimpleMismatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"simple\">\n" +
            "    <syntax name=\"foo\" usage=\"hello\" actionName=\"fooA\"/>\n" +
            "    <syntax name=\"bar\" usage=\"world\" actionName=\"barA\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 2, Locale.US);
        Parameters p;
        try {
            p = new Parameters();
            sm.getMatchingSyntax(new String[]{ "hi" }, p);
            fail("should get BadUsageException");
        } catch (BadUsageException e) {
            verify(e, 0, "hello", "simple.foo");
        }
    }


    @Test
    public void testNoArgsMismatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"simple\">\n" +
            "    <syntax name=\"foo\" usage=\"hello\" actionName=\"fooA\"/>\n" +
            "    <syntax name=\"bar\" usage=\"world\" actionName=\"barA\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 2, Locale.US);
        Parameters p;
        try {
            p = new Parameters();
            sm.getMatchingSyntax(new String[]{ }, p);
            fail("should get BadUsageException");
        } catch (BadUsageException e) {
            verify(e, 0, "simple.foo", "simple.foo");
        }
    }


    @Test
    public void testPackageSetAdd() throws Exception {
        String data1 = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"a\">\n" +
            "    <syntax name=\"foo\" usage=\"hello\" actionName=\"fooA\"/>\n" +
            "    <syntax name=\"bar\" usage=\"world\" actionName=\"barA\"/>\n" +
            "</package>\n";
        String data2 = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"b\">\n" +
            "    <syntax name=\"foo\" usage=\"yo\" actionName=\"fooB\"/>\n" +
            "    <syntax name=\"bar\" usage=\"globe\" actionName=\"barB\"/>\n" +
            "</package>\n";
        SyntaxCompiler sc = SyntaxCompilerTest.getCompiler();
        Set<SyntaxPackage> set = new HashSet<SyntaxPackage>();
        set.add(sc.compile(getInput(data1), Locale.US));
        set.add(sc.compile(getInput(data2), Locale.US));
        SyntaxMatcher sm = new SyntaxMatcher(Locale.US);
        sm.addPackages(set);
        assertEquals("incorrect number of packages:", 2, 
                     sm.getPackages().size());
        assertEquals("incorrect number of syntaxes:", 4, 
                     sm.getSyntaxes().size());

        Parameters p;
        Syntax s;
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "hello" }, p);
            verify(s, "a.foo", p, new String[] {});

            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "globe" }, p);
            verify(s, "b.bar", p, new String[] {});

        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }
    }

    @Test
    public void testSimpleParameterMatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"simple\">\n" +
            "    <parameter name=\"foo\"/>\n" +
            "    <syntax name=\"bar\" usage=\"hello $foo\"\n" +
            "            actionName=\"barA\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 1, Locale.US);
        Parameters p;
        Syntax s;
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "hello", "world" }, p);
            verify(s, "simple.bar", p, new String[] {
                "simple.foo=world"
            });

            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "hello", "dude"  }, p);
            verify(s, "simple.bar", p, new String[] {
                "simple.foo=dude"
            });

        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }
    }

    
    @Test
    public void testSimpleSyntaxMatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"simple\">\n" +
            "    <parameter name=\"foo1\"/>\n" +
            "    <parameter name=\"foo2\"/>\n" +
            "    <syntax name=\"bar1\" usage=\"hello $foo1\"/>\n" +
            "    <syntax name=\"bar2\" usage=\"yo $foo2\"/>\n" +
            "    <syntax name=\"bar\" usage=\"$bar1 $bar2\"\n" +
            "            actionName=\"barA\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 1, Locale.US);
        Parameters p;
        Syntax s;
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                    "hello", "world", "yo", "earth" }, p);
            verify(s, "simple.bar", p, new String[] {
                    "simple.foo1=world",
                    "simple.foo2=earth"
            });
        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }
        
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                    "hello", "world", "yo" }, p);
            fail("should get BadUsageException");
        } catch (BadUsageException e) {
            verify(e, 3, "simple.foo2", "simple.bar");
        }

        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                    "hello", "world" }, p);
            fail("should get BadUsageException");
        } catch (BadUsageException e) {
            verify(e, 2, "simple.bar2", "simple.bar");
        }

        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "hello" }, p);
            fail("should get BadUsageException");
        } catch (BadUsageException e) {
            verify(e, 1, "simple.foo1", "simple.bar");
        }
    }
    

    @Test
    public void testMinOccurencesParameterMatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"simple\">\n" +
            "    <parameter name=\"foo\" minOccurrences=\"2\"/>\n" +
            "    <syntax name=\"bar\" usage=\"hello $foo\"\n" +
            "            actionName=\"barA\"/>\n" +
            "    <parameter name=\"dude\" minOccurrences=\"2\"\n" +
            "                             maxOccurrences=\"-1\"/>\n" +
            "    <syntax name=\"bar1\" usage=\"yo $dude\"\n" +
            "            actionName=\"barA\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 2, Locale.US);
        Parameters p;
        Syntax s;
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                "hello", "world", "is", "not", "enough"
            }, p);
            dump(p);
            verify(s, "simple.bar", p, new String[] {
                "simple.foo#0=world",
                "simple.foo#1=is",
                "simple.foo#2=not",
                "simple.foo#3=enough"
            });

            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                "yo", "dude", "what's", "up"
            }, p);
            dump(p);
            verify(s, "simple.bar1", p, new String[] {
                "simple.dude#0=dude",
                "simple.dude#1=what's",
                "simple.dude#2=up",
            });
        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }

        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "hello", "dude"  }, p);
            fail("should get BadUsageException");
        } catch (BadUsageException e) {
            verify(e, 2, "simple.foo", "simple.bar");
        }
    }


    @Test
    public void testMaxOccurencesParameterMatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"simple\">\n" +
            "    <parameter name=\"foo\" minOccurrences=\"0\"\n" +
            "                            maxOccurrences=\"2\"/>\n" +
            "    <syntax name=\"bar\" usage=\"hello $foo\"\n" +
            "            actionName=\"barA\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 1, Locale.US);
        Parameters p;
        Syntax s;
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                "hello", "funny", "world"
            }, p);
            verify(s, "simple.bar", p, new String[] {
                "simple.foo#0=funny",
                "simple.foo#1=world"
            });

            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                "hello", "world"
            }, p);
            verify(s, "simple.bar", p, new String[] {
                "simple.foo#0=world",
            });

            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                "hello" 
            }, p);
            verify(s, "simple.bar", p, new String[] {});

        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }

        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "hello", "d", "e", "f" }, p);
            fail("should get BadUsageException");
        } catch (BadUsageException e) {
            verify(e, 3, "simple.foo", "simple.bar");
        }
    }

    
    @Test
    public void testMinOccurencesFragmentMatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"rfrag\">\n" +
            "    <parameter name=\"foo\"/>\n" +
            "    <syntax name=\"bar\" usage=\"hello $foo\"\n" +
            "            minOccurrences=\"2\"\n" +
            "            actionName=\"barA\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 1, Locale.US);
        Parameters p;
        Syntax s;
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                "hello", "world", "hello", "earth"
            }, p);
            dump(p);
            verify(s, "rfrag.bar", p, new String[] {
                    "rfrag.foo#0=world",
                    "rfrag.foo#1=earth",
            });

        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }

        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "hello", "dude"  }, p);
            fail("should get BadUsageException");
        } catch (BadUsageException e) {
            verify(e, 2, "rfrag.bar", "rfrag.bar");
        }
    }


    @Test
    public void testMaxOccurencesFragmentMatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"rfrag\">\n" +
            "    <parameter name=\"foo\"/>\n" +
            "    <syntax name=\"bar1\" usage=\"hello $foo\"\n" +
            "            minOccurrences=\"0\"" +
            "            maxOccurrences=\"2\"/>\n" +
            "    <syntax name=\"bar\" usage=\"yo $bar1\"\n" +
            "            actionName=\"barA\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 1, Locale.US);
        Parameters p;
        Syntax s;
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                "yo", "hello", "world", "hello", "earth"
            }, p);
            dump(p);
            verify(s, "rfrag.bar", p, new String[] {
                    "rfrag.foo#0=world",
                    "rfrag.foo#1=earth",
            });

            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                    "yo", "hello", "world"
            }, p);
            dump(p);
            verify(s, "rfrag.bar", p, new String[] {
                    "rfrag.foo#0=world",
            });

            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                    "yo"
            }, p);
            dump(p);
            verify(s, "rfrag.bar", p, new String[] { });

        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }

        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                "yo", "hello", "world", "hello", "earth", "hello", "freak"
            }, p);
            fail("should get BadUsageException");

        } catch (BadUsageException e) {
            verify(e, 5, "rfrag.foo", "rfrag.bar");
        }

        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ 
                "yo", "hello", "world", "hello", "earth", "hello"
            }, p);
            fail("should get BadUsageException");

        } catch (BadUsageException e) {
            verify(e, 5, "rfrag.foo", "rfrag.bar");
        }

    }


    @Test
    public void testPrioritizedSyntaxMatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"simple\">\n" +
            "    <parameter name=\"foo\"/>\n" +
            "    <syntax name=\"barA\" usage=\"hello $foo\"\n" +
            "            actionName=\"barA\" priority=\"1\"/>\n" +
            "    <syntax name=\"barB\" usage=\"hello $foo\"\n" +
            "            actionName=\"barB\" priority=\"0\"/>\n" +
            "    <syntax name=\"barC\" usage=\"hello $foo\"\n" +
            "            actionName=\"barC\" priority=\"2\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 3, Locale.US);
        Parameters p;
        Syntax s;
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "hello", "world" }, p);
            verify(s, "simple.barB", p, new String[] {
                "simple.foo=world"
            });

        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }
    }


    @Test
    public void testIncompleteAnchoredMatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"simple\">\n" +
            "    <syntax name=\"foo\" usage=\"world\"/>\n" +
            "    <syntax name=\"bar\" usage=\"hello $foo\"\n" +
            "            actionName=\"barA\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 1, Locale.US);
        Parameters p;
        try {
            p = new Parameters();
            sm.getMatchingSyntax(new String[]{ "hello" }, p);
            fail("should get BadUsageException");

        } catch (BadUsageException e) {
            verify(e, 1, "simple.foo", "simple.bar");
        }

        try {
            p = new Parameters();
            sm.getMatchingSyntax(new String[]{ "hello", "world", "X" }, p);
            fail("should get BadUsageException");

        } catch (BadUsageException e) {
            verify(e, 2, "world", "simple.bar");
        }
    }


    @Test
    public void testIncompleteMatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"simple\">\n" +
            "    <syntax name=\"foo\" usage=\"world\" floating=\"on\"/>\n" +
            "    <syntax name=\"barA\" usage=\"hello $foo\"\n" +
            "            actionName=\"barA\"/>\n" +
            "    <parameter name=\"p\" legalValues=\"yo\" floating=\"on\"/>\n" +
            "    <syntax name=\"barB\" usage=\"hi $p\"\n" +
            "            actionName=\"barB\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 2, Locale.US);
        Parameters p;
        try {
            p = new Parameters();
            sm.getMatchingSyntax(new String[]{ "hello", "worst" }, p);
            fail("should get BadUsageException");

        } catch (BadUsageException e) {
            verify(e, 1, "world", "simple.barA");
        }

        try {
            p = new Parameters();
            sm.getMatchingSyntax(new String[]{ "hi", "no" }, p);
            fail("should get BadUsageException");

        } catch (BadUsageException e) {
            verify(e, 1, "simple.p", "simple.barB");
        }
    }


    @Test
    public void testFloatingMatch() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"simple\">\n" +
            "    <syntax name=\"foo\" usage=\"world\" floating=\"on\"/>\n" +
            "    <syntax name=\"barA\" usage=\"hello $foo\"\n" +
            "            actionName=\"barA\"/>\n" +
            "    <parameter name=\"p\" legalValues=\"yo\" floating=\"on\"/>\n" +
            "    <syntax name=\"barB\" usage=\"hi $p\"\n" +
            "            actionName=\"barB\"/>\n" +
            "    <syntax name=\"barC\" usage=\"hi there $p\"\n" +
            "            actionName=\"barC\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 3, Locale.US);
        Parameters p;
        Syntax s;
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "hello", "world" }, p);
            verify(s, "simple.barA", p, new String[] {});

        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }

        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "hi", "yo" }, p);
            verify(s, "simple.barB", p, new String[] {
                "simple.p=yo"
            });

        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }

        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{ "hi", "yo", "there" }, p);
            verify(s, "simple.barC", p, new String[] {
                "simple.p=yo"
            });

            s = sm.getMatchingSyntax(new String[]{ "hi", "there", "yo" }, p);
            verify(s, "simple.barC", p, new String[] {
                "simple.p=yo"
            });

        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }
    }

    @SuppressWarnings("serial")
    public static class Goo implements Serializable {
        private String goo = null;
        public Goo() {}
        public Goo(String goo) { this.goo = goo; }
        @Override public String toString() { return goo; }
    }

    public static class GooConstraints implements Constraints {
        private String pfx = null;
        public GooConstraints() {}
        public GooConstraints(String pfx) { this.pfx = pfx; }
        @Override
        public boolean isValid(Serializable o) {
            return o != null && o instanceof Goo &&
                ((Goo) o).toString().startsWith(pfx);
        }
    }

    private static final String PARSER = 
        "org.opendaylight.util.syntax.SyntaxCompilerTest$GooParser";

    @SuppressWarnings("serial")
    public static class GooParser extends AbstractParameterParser {
        public GooParser() { }
        public GooParser(Locale locale) { super(locale); }
        @Override
        public String getTypeToken() { return "goo"; }

        @Override
        public Constraints parse(Properties db, TokenTranslator tr,
                                 ParserLoader parserLoader) {
            return new GooConstraints(tr.translate(db.getProperty("pfx")));
        }

        @Override
        public Serializable parse(String token, Constraints constraints,
                                  Parameters soFar) {
            Goo goo = new Goo(token);
            return constraints.isValid(goo) ? goo : null;
        }
    }


    @Test
    public void testCustomParameterParser() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"custom-parser\"\n" +
            "         resources=\"" + TEST_BUNDLE + "\">\n" +
            "    <parameterParser class=\"" + PARSER + "\"/>\n" +
            "    <constraintsParser class=\"" + PARSER + "\"/>\n" +
            "\n" +
            "    <parameter name=\"par\" parser=\"goo\" pfx=\"z\" \n" + 
            "               default=\"zhuzhu\"/>\n" +
            "    <syntax name=\"stxA\" usage=\"hello there $par\" \n" + 
            "            actionName=\"stxA\" priority=\"1\"/>\n" +
            "    <syntax name=\"stxB\" usage=\"hello\" \n" + 
            "            actionName=\"stxB\" priority=\"0\"/>\n" +
            "    <syntax name=\"stxC\" usage=\"hello\" \n" + 
            "            actionName=\"stxC\" priority=\"2\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 3, Locale.US);
        Parameters p;
        Syntax s;
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{"hello", "there", "zit"}, p);
            verify(s, "custom-parser.stxA", p, new String[] {
                "custom-parser.par=zit"
            });

        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }

        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{"hello", "there", "pit"}, p);
            fail("should get BadUsageException");

        } catch (BadUsageException e) {
            verify(e, 2, "custom-parser.par", "custom-parser.stxA");
        }
    }


    @Test
    public void testCustomConstraintsParser() throws Exception {
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"custom-parser\"\n" +
            "         resources=\"" + TEST_BUNDLE + "\">\n" +
            "    <parameterParser class=\"" + PARSER + "\"/>\n" +
            "    <constraintsParser class=\"" + PARSER + "\"/>\n" +
            "\n" +
            "    <parameter name=\"par\" parser=\"goo\"\n" + 
            "               constraints=\"string\" legalValues=\"yes|no\"\n" +
            "               default=\"yes\"/>\n" +
            "    <syntax name=\"stx\" usage=\"it is $par\" \n" + 
            "            actionName=\"stxA\" priority=\"1\"/>\n" +
            "</package>\n";
        SyntaxMatcher sm = getMatcher(data, 1, 1, Locale.US);
        Parameters p;
        Syntax s;
        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{"it", "is", "yes"}, p);
            verify(s, "custom-parser.stx", p, new String[] {
                "custom-parser.par=yes"
            });

        } catch (BadUsageException e) {
            e.printStackTrace();
            fail("should not get BadUsageException");
        }

        try {
            p = new Parameters();
            s = sm.getMatchingSyntax(new String[]{"it", "is", "nope"}, p);
            fail("should get BadUsageException");

        } catch (BadUsageException e) {
            verify(e, 2, "custom-parser.par", "custom-parser.stx");
        }
    }

}
