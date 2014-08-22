/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.opendaylight.util.Tokenizer;
import org.opendaylight.util.format.TokenTranslator;
import org.opendaylight.util.syntax.parsers.AbstractParameterParser;
import org.opendaylight.util.syntax.parsers.Constraints;
import org.opendaylight.util.syntax.parsers.StringSet;
import org.opendaylight.util.syntax.parsers.TypedParser;
import org.opendaylight.util.syntax.parsers.Utilities;

import org.junit.Test;
import org.xml.sax.SAXParseException;

/**
 * JUnit tests to verify functionality of the SyntaxCompiler class.
 *
 * @author Thomas Vachuska
 */
public class SyntaxCompilerTest implements SyntaxKeywords {

    public static final String SCHEMA_LOCATION =
        "src/main/resources/org/opendaylight/util/syntax/syntax-schema.xsd";

    private static final String TEST_BUNDLE =
        "org.opendaylight.util.syntax.TestResources";

    private static Map<String, Class<?>> nodeClasses = 
        new HashMap<String, Class<?>>();

    static {
        nodeClasses.put("a", SyntaxPackage.class);
        nodeClasses.put("n", SyntaxNode.class);
        nodeClasses.put("k", SyntaxKeyword.class);
        nodeClasses.put("p", SyntaxParameter.class);
        nodeClasses.put("f", SyntaxFragment.class);
        nodeClasses.put("s", Syntax.class);
    }

    /**
     * Returns the input stream using the data in the string, if the string
     * starts with XML pre-amble, or it returns input stream created from the
     * file.
     * 
     * @param stringOrName string containing the data or file name that
     *        contains the data
     * @return input stream created from the data or the file name
     */
    private InputStream getInput(String stringOrName) {
        if (stringOrName.startsWith("<?xml"))
            return new ByteArrayInputStream(stringOrName.getBytes());
        try {
            return new FileInputStream("data/" + stringOrName);
        } catch (FileNotFoundException e) {
            fail("could not file file data/" + stringOrName +
                 "; pwd=" + new File(".").getAbsolutePath());
            return null;
        }
    }

    private SyntaxNode verify(SyntaxPackage sp, String nodeData) {
        Tokenizer st = new Tokenizer(nodeData, "|");
        st.next();  // consume the type
        String name = st.next();
        SyntaxNode node = sp.getNodes().get(name);
        assertNotNull("node " + name + " should be found", node);
        assertEquals("incorrect name:", name, node.getName());

        assertEquals("incorrect container:", st.next(), 
                     node.getContainer().getName());

        //  Check extension-specific properties.
        if (node instanceof SyntaxNodeExtension) {
            SyntaxNodeExtension se = (SyntaxNodeExtension) node;
            String parentName = st.next();
            if (parentName.equals("-")) {
                assertEquals("incorrect parent:", null, se.getParent());
            } else {
                assertEquals("incorrect parent:", parentName, 
                             se.getParent().getName());
            }
        }

        assertEquals("incorrect isFloating:", 
                     Utilities.isOn(st.next()), node.isFloating());

        //  Check keyword-specific properties.
        if (node instanceof SyntaxKeyword) {
            StringSet ss = ((SyntaxKeyword) node).getTokens();
            assertTrue("incorrect keyword", ss.contains(st.next()));
        }

        //  Check parameter-specific properties.
        if (node instanceof SyntaxParameter) {
            SyntaxParameter p = (SyntaxParameter) node;
            assertEquals("incorrect type: ", st.next(), 
                         p.getParser().getTypeToken());
            assertEquals("incorrect default: ", st.next(),
                         p.getDefaultValue() + "");
            assertEquals("incorrect min occurrences: ", 
                         Integer.parseInt(st.next()),
                         p.getMinOccurrences());
            assertEquals("incorrect max occurrences: ", 
                         Integer.parseInt(st.next()),
                         p.getMaxOccurrences());
        }

        //  Check fragment-specific properties.
        if (node instanceof SyntaxFragment) {
            SyntaxFragment f = (SyntaxFragment) node;
            assertEquals("incorrect anchored node count: ", 
                         Integer.parseInt(st.next()),
                         f.getAnchoredNodes().size());
            assertEquals("incorrect floating node count: ", 
                         Integer.parseInt(st.next()),
                         f.getFloatingNodes().size());
        }

        //  Check syntax-specific properties.
        if (node instanceof Syntax) {
            Syntax s = (Syntax) node;
            assertEquals("incorrect action:", st.next(), s.getActionName());
        }

        return node;
    }

    private void verify(SyntaxPackage sp, String name, String[] nodes) {
        assertNotNull("package should not be null", sp);
        assertEquals("invalid name:", name, sp.getName());

        int nodeCount = nodes != null ? nodes.length : 0;
        assertEquals("invalid node count:", nodeCount, sp.getNodes().size());
        if (nodes == null)
            return;
        
        int syntaxCount = 0;
        for (int i = 0; i < nodeCount; i++) {
            SyntaxNode sn = verify(sp, nodes[i]);
            if (sn instanceof Syntax)
                syntaxCount++;
        }
        assertEquals("invalid syntax count:", syntaxCount, 
                     sp.getSyntaxes().size());
    }

    public static SyntaxCompiler getCompiler() {
        SyntaxCompiler sc = new SyntaxCompiler();
        // sc.setSchemaLocation(TestTools.getFileName(SCHEMA_LOCATION));
        sc.setSchemaLocation(SCHEMA_LOCATION);
        return sc;
    }

    @Test
    public void testEmptyPackage() throws Exception {
        SyntaxCompiler sc = getCompiler();
        SyntaxPackage sp;
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"empty\" resources=\"" + TEST_BUNDLE + "\">\n" +
            "</package>\n";

        sp = sc.compile(getInput(data), Locale.US);
        verify(sp, "empty", null);
        assertEquals("incorrect name property:", "empty",
                     sp.getDB().getProperty(KW_NAME));
        assertEquals("incorrect resources property:", TEST_BUNDLE,
                     sp.getDB().getProperty(KW_RESOURCES));
        assertTrue("resource bundle should not be null", 
                   null != sp.getResourceBundle());
        assertTrue("parser pool should be null for TypedParser.class", 
                   null == sp.getParser("foobar", TypedParser.class));
    }

    @Test
    public void testBadSyntaxHintError() throws Exception {
        SyntaxCompiler sc = getCompiler();
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"bad-syntax-hint\">\n" +
            "    <parameter name=\"good\"/>\n" +
            "    <syntax name=\"bad\"/>\n" +
            "</package>\n";
        try {
            sc.compile(getInput(data), Locale.US);
            fail("should have thrown an exception");
        } catch (BadSyntaxException e) {
            assertEquals("incorrect exception:", "bad-syntax-hint.bad",
                         e.getSyntaxNode().getName());
        }
    }

    @Test
    public void testBadNesting() throws Exception {
        SyntaxCompiler sc = getCompiler();
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"invalid\">\n" +
            "    <parameter name=\"foo\" default=\"dude\">\n" +
            "        <syntax name=\"foo\" usage=\"foo\"/>\n" +
            "    </parameter>" +
            "</package>\n";
        try {
            sc.compile(getInput(data), Locale.US);
            fail("should have thrown an exception");
        } catch (SAXParseException e) {
            ;
        }
    }

    @Test
    public void testBadParserError() throws Exception {
        SyntaxCompiler sc = getCompiler();
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"bad-parser\">\n" +
            "    <parameter name=\"good\"/>\n" +
            "    <parameter name=\"bad\" parser=\"foobar\"/>\n" +
            "</package>\n";
        try {
            sc.compile(getInput(data), Locale.US);
            fail("should have thrown an exception");
        } catch (BadSyntaxException e) {
            assertEquals("incorrect exception:", "bad-parser.bad",
                         e.getSyntaxNode().getName());
        }
    }

    @Test
    public void testBadConstraintsParserError() throws Exception {
        SyntaxCompiler sc = getCompiler();
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"bad-constraints-parser\">\n" +
            "    <parameter name=\"good\"/>\n" +
            "    <parameter name=\"bad\" constraints=\"foobar\"/>\n" +
            "</package>\n";
        try {
            sc.compile(getInput(data), Locale.US);
            fail("should have thrown an exception");
        } catch (BadSyntaxException e) {
            assertEquals("incorrect exception:", "bad-constraints-parser.bad",
                         e.getSyntaxNode().getName());
        }
    }

    @Test
    public void testSimpleSyntax() throws Exception {
        SyntaxCompiler sc = getCompiler();
        SyntaxPackage sp;
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"simple\">\n" +
            "    <parameter name=\"pbar\" default=\"gal\"/>\n" +
            "    <parameter name=\"pfoo\" floating=\"yes\"\n" +
            "               default=\"dude\"/>\n" +
            "    <parameter name=\"pfoobar\" extends=\"pbar\"\n" +
            "               default=\"venus\"/>\n" +
            "    <syntax name=\"foo\" usage=\"hello there\"/>\n" +
            "    <syntax name=\"bar\" extends=\"foo\"\n" +
            "            actionName=\"fooAction\"/>\n" +
            "</package>\n";

        sp = sc.compile(getInput(data), Locale.US);
        verify(sp, "simple", new String[] {
            "p|simple.pbar|simple|-|no|string|gal|1|1",
            "p|simple.pfoo|simple|-|yes|string|dude|1|1",
            "p|simple.pfoobar|simple|simple.pbar|no|string|venus|1|1",
            "f|simple.foo|simple|-|no|2|0",
            "s|simple.bar|simple|simple.foo|no|2|0|fooAction"
        });
    }

    @Test
    public void testNestedSyntax() throws Exception {
        SyntaxCompiler sc = getCompiler();
        SyntaxPackage sp;
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"n\">\n" +
            "    <syntax name=\"foo\" usage=\"hello $pfoo $pbar\">\n" +
            "        <parameter name=\"pfoo\" floating=\"yes\"\n" +
            "                   default=\"dude\"/>\n" +
            "        <parameter name=\"pbar\" default=\"gal\"/>\n" +
            "    </syntax>\n" +
            "    <syntax name=\"bar\" extends=\"foo\"\n" +
            "            usage=\"$super $pfoobar\"\n" +
            "            actionName=\"fooAction\">\n" +
            "        <parameter name=\"pfoobar\" extends=\"n.foo.pbar\" \n" +
            "                   description=\"pfoobar\"\n" +
            "                   default=\"venus\"/>\n" +
            "   </syntax>\n" +
            "</package>\n";

        sp = sc.compile(getInput(data), Locale.US);
        verify(sp, "n", new String[] {
            "p|n.foo.pbar|n.foo|-|no|string|gal|1|1",
            "p|n.foo.pfoo|n.foo|-|yes|string|dude|1|1",
            "p|n.bar.pfoobar|n.bar|n.foo.pbar|no|string|venus|1|1",
            "f|n.foo|n|-|no|2|1",
            "s|n.bar|n|n.foo|no|3|1|fooAction"
        });
    }

    @Test
    public void testProperty() throws Exception {
        SyntaxCompiler sc = getCompiler();
        SyntaxPackage sp;
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"prop\">\n" +
            "    <parameter name=\"par\" default=\"doh\">\n" +
            "        <property name=\"foo\" value=\"bar\"/>\n" +
            "    </parameter>\n" +
            "    <syntax name=\"stx\" usage=\"hello there\">\n" +
            "        <property name=\"foo\" value=\"bar\"/>\n" +
            "    </syntax>\n" +
            "</package>\n";
        sp = sc.compile(getInput(data), Locale.US);
        verify(sp, "prop", new String[] {
            "p|prop.par|prop|-|no|string|doh|1|1",
            "f|prop.stx|prop|-|no|2|0"
        });
        assertEquals("incorrect property:", "bar", 
                     sp.getNode("prop.par").getDB().getProperty("foo"));
        assertEquals("incorrect property:", "bar", 
                     sp.getNode("prop.stx").getDB().getProperty("foo"));
    }

    @Test
    public void testKeywordTranslation() throws Exception {
        SyntaxCompiler sc = getCompiler();
        SyntaxPackage sp;
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"kw-xlation\"\n" +
            "         resources=\"" + TEST_BUNDLE + "\">\n" +
            "    <parameter name=\"par\" default=\"%foobar\"/>\n" +
            "    <syntax name=\"stx\" usage=\"hello %foo %bar\"/>\n" +
            "</package>\n";
        sp = sc.compile(getInput(data), Locale.US);
        verify(sp, "kw-xlation", new String[] {
            "p|kw-xlation.par|kw-xlation|-|no|string|FooBar|1|1",
            "f|kw-xlation.stx|kw-xlation|-|no|3|0"
        });
    }

    @Test
    public void testKeywordTranslationError() throws Exception {
        SyntaxCompiler sc = getCompiler();
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"bad-kw-xlation\"\n" +
            "         resources=\"" + TEST_BUNDLE + "\">\n" +
            "    <parameter name=\"par\" default=\"%foobar\"/>\n" +
            "    <syntax name=\"bad\" usage=\"hello %food %bar\"/>\n" +
            "</package>\n";
        try {
            sc.compile(getInput(data), Locale.US);
        } catch (BadSyntaxException e) {
            assertEquals("incorrect exception:", "bad-kw-xlation.bad",
                         e.getSyntaxNode().getName());
        }
    }

    @Test
    public void testBadResourceBudleError() throws Exception {
        SyntaxCompiler sc = getCompiler();
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"bad-kw-xlation\"\n" +
            "         resources=\"" + TEST_BUNDLE + "X\">\n" +
            "    <parameter name=\"par\" default=\"%foobars\"/>\n" +
            "    <syntax name=\"stx\" usage=\"hello %foo %bar\"/>\n" +
            "</package>\n";
        try {
            sc.compile(getInput(data), Locale.US);
        } catch (BadSyntaxException e) {
            assertEquals("incorrect exception:", "bad-kw-xlation",
                         e.getSyntaxNode().getName());
        }
    }

    @Test
    public void testBadUsageReferenceError() throws Exception {
        SyntaxCompiler sc = getCompiler();
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"bad-reference\">\n" +
            "    <syntax name=\"bad\" usage=\"hello $foo\"/>\n" +
            "</package>\n";
        try {
            sc.compile(getInput(data), Locale.US);
        } catch (BadSyntaxException e) {
            assertEquals("incorrect exception:", "bad-reference.bad",
                         e.getSyntaxNode().getName());
        }
    }

    @Test
    public void testBadExtendReferenceError() throws Exception {
        SyntaxCompiler sc = getCompiler();
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"bad-reference\">\n" +
            "    <syntax name=\"bad\" extends=\"foo\" usage=\"hello\"/>\n" +
            "</package>\n";
        try {
            sc.compile(getInput(data), Locale.US);
        } catch (BadSyntaxException e) {
            assertEquals("incorrect exception:", "bad-reference.bad",
                         e.getSyntaxNode().getName());
        }
    }

    @Test
    public void testBadTagError() throws Exception {
        SyntaxCompiler sc = getCompiler();
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"bad-tag\">\n" +
            "    <Xsyntax name=\"bad\" usage=\"hello $foo\"/>\n" +
            "</package>\n";
        try {
            sc.compile(getInput(data), Locale.US);
            fail("should have thrown an exception");
        } catch (SAXParseException e) {
            ;
        }
    }

    @Test
    public void testUnnamedParameter() throws Exception {
        SyntaxCompiler sc = getCompiler();
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"bad-parameter\">\n" +
            "    <parameter Xname=\"bad\"/>\n" +
            "</package>\n";
        try {
            sc.compile(getInput(data), Locale.US);
            fail("should have thrown an exception");
        } catch (BadSyntaxException e) {
            ;
        }
    }

    @Test
    public void testUnnamedSyntax() throws Exception {
        SyntaxCompiler sc = getCompiler();
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"bad-syntax\">\n" +
            "    <syntax Xname=\"bad\" usage=\"hello\"/>\n" +
            "</package>\n";
        try {
            sc.compile(getInput(data), Locale.US);
            fail("should have thrown an exception");
        } catch (BadSyntaxException e) {
            ;
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
        SyntaxCompiler sc = getCompiler();
        SyntaxPackage sp;
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"custom-parser\"\n" +
            "         resources=\"" + TEST_BUNDLE + "\">\n" +
            "    <parameterParser class=\"" + PARSER + "\"/>\n" +
            "    <constraintsParser class=\"" + PARSER + "\"/>\n" +
            "\n" +
            "    <parameter name=\"par\" parser=\"goo\" pfx=\"z\" \n" + 
            "               default=\"zhuzhu\"/>\n" +
            "    <syntax name=\"stx\" usage=\"hello $par\"/>\n" +
            "</package>\n";
        sp = sc.compile(getInput(data), Locale.US);
        verify(sp, "custom-parser", new String[] {
            "p|custom-parser.par|custom-parser|-|no|goo|zhuzhu|1|1",
            "f|custom-parser.stx|custom-parser|-|no|2|0"
        });
    }

    @Test
    public void testCustomConstraintsParser() throws Exception {
        SyntaxCompiler sc = getCompiler();
        SyntaxPackage sp;
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"custom-parser\"\n" +
            "         resources=\"" + TEST_BUNDLE + "\">\n" +
            "    <parameterParser class=\"" + PARSER + "\"/>\n" +
            "\n" +
            "    <parameter name=\"par\" parser=\"goo\"\n" + 
            "               constraints=\"string\" legalValues=\"yes|no\"\n" +
            "               default=\"yes\"/>\n" +
            "    <syntax name=\"stx\" usage=\"hello $par\"/>\n" +
            "</package>\n";
        sp = sc.compile(getInput(data), Locale.US);
        verify(sp, "custom-parser", new String[] {
            "p|custom-parser.par|custom-parser|-|no|goo|yes|1|1",
            "f|custom-parser.stx|custom-parser|-|no|2|0"
        });
    }

    @Test
    public void testInheritance() throws Exception {
        SyntaxCompiler sc = getCompiler();
        SyntaxPackage sp;
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"inherit\"\n" +
            "         resources=\"" + TEST_BUNDLE + "\">\n" +
            "    <syntax name=\"foobar\" usage=\"$foo $bar\">\n" +
            "        <parameter name=\"foo\" floating=\"yes\"/>\n" +
            "        <parameter name=\"bar\" floating=\"yes\"/>\n" +
            "    </syntax>\n" +
            "    <syntax name=\"stx\" extends=\"foobar\" floating=\"no\"\n" + 
            "            usage=\"hello $super\" actionName=\"stxA\"/>\n" +
            "    <syntax name=\"stx1\" extends=\"stx\" floating=\"no\"\n" + 
            "            usage=\"yo $super\"/>\n" +
            "</package>\n";
        sp = sc.compile(getInput(data), Locale.US);
        verify(sp, "inherit", new String[] {
            "p|inherit.foobar.foo|inherit.foobar|-|yes|string|null|1|1",
            "p|inherit.foobar.bar|inherit.foobar|-|yes|string|null|1|1",
            "f|inherit.foobar|inherit|-|yes|0|2",
            "s|inherit.stx|inherit|inherit.foobar|no|1|2|stxA",
            "s|inherit.stx1|inherit|inherit.stx|no|2|2|stxA"
        });
    }

    @Test
    public void testParameterDefaultInheritance() throws Exception {
        SyntaxCompiler sc = getCompiler();
        SyntaxPackage sp;
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"inherit\">\n" +
            "    <parameter name=\"foo\" default=\"dude\"/>\n" +
            "    <parameter name=\"bar\" extends=\"foo\"/>\n" +
            "</package>\n";
        sp = sc.compile(getInput(data), Locale.US);
        verify(sp, "inherit", new String[] {
            "p|inherit.foo|inherit|-|no|string|dude|1|1",
            "p|inherit.bar|inherit|inherit.foo|no|string|dude|1|1"
        });
    }
    
    @Test
    public void testRepeatingFragments() throws Exception {
        SyntaxCompiler sc = getCompiler();
        SyntaxPackage sp;
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"rfrag\">\n" +
            "    <parameter name=\"foo\"/>\n" +
            "    <syntax name=\"bar\" usage=\"-n $foo\"\n" +
            "            minOccurrences=\"1\" maxOccurrences=\"-1\"\n" +
            "            actionName=\"stxBar\"/>\n" +
            "</package>\n";
        sp = sc.compile(getInput(data), Locale.US);
        verify(sp, "rfrag", new String[] {
            "p|rfrag.foo|rfrag|-|no|string|null|1|1",
            "s|rfrag.bar|rfrag|-|no|2|0|stxBar"
        });
    }
    
}

