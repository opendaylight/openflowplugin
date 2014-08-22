/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.usage;

import static org.opendaylight.util.junit.TestTools.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Locale;

import org.opendaylight.util.syntax.Syntax;
import org.opendaylight.util.syntax.SyntaxCompiler;
import org.opendaylight.util.syntax.SyntaxCompilerTest;
import org.opendaylight.util.syntax.SyntaxNode;
import org.opendaylight.util.syntax.SyntaxPackage;

import org.junit.Test;

/**
 * JUnit tests to verify functionality of the usage string generation
 * facilities.
 *
 * @author Thomas Vachuska
 */
public class UsageStringTest {

    private static final String TEST_BUNDLE =
        "org.opendaylight.util.syntax.TestResources";

    private static final String FOO =
        "You know we are on a wrong track altogether. We must not think of " +
        "the things we could do with, but only of the things that we can't " +
        "do without."; 
 
    private static final String EOL = getDefaultEndLine();
    
    /**
     * Returns the input stream using the data in the string, if the string
     * starts with XML pre-amble, or it returns input stream created from the
     * file.
     * 
     * @param stringOrName string with data or name of file containing the
     *        data
     * @return input stream from the data or the file content
     */
    private InputStream getInput(String stringOrName) {
        if (stringOrName.startsWith("<?xml"))
            return new ByteArrayInputStream(stringOrName.getBytes());
        try {
            return new FileInputStream("data/" + stringOrName);
        } catch (FileNotFoundException e) {
            assertTrue("could not file file data/" + stringOrName, false);
            return null;
        }
    }

    private static String getDefaultEndLine() {
        return System.getProperty("line.separator"); // "\r\n";
    }

    private void verify(SyntaxPackage sp, String name, String usage) {
        SyntaxNode node = sp.getNode(name);
        assertNotNull("node should not be null", node);
        assertEquals("incorrect toString [" + node.toString() + "] vs. [" + 
                     usage + "]:", usage, node.toString());
    }


    @Test
    @SuppressWarnings("serial")
    public void testPlainTextBasics() throws Exception {
        SyntaxCompiler sc = SyntaxCompilerTest.getCompiler();
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"inherit\"\n" +
            "         resources=\"" + TEST_BUNDLE + "\">\n" +
            "    <syntax name=\"foobar\" usage=\"$foo $bar\" helpTopics=\"yoyo, yo\">\n" +
            "        <parameter name=\"foo\" floating=\"yes\"\n" +
            "                   description=\"foo_count\"\n" +
            "                   helpText=\"Indicates the number of days the BAR entity needs to be greeted in the morning, day and evening.\"/>\n" +
            "        <parameter name=\"bar\" floating=\"yes\"\n" +
            "                   description=\"bar_entity\"\n" +
            "                   helpText=\"Indicates the BAR entity that needs to be greeted each morning, day and evening.\"/>\n" +
            "    </syntax>\n" +
            "    <syntax name=\"stx\" extends=\"foobar\" floating=\"no\"\n" + 
            "            usage=\"hello $super\" actionName=\"stxA\"\n" +
            "            description=\"This command simply greets anyone who it meets and does so with eagerness not matched by any other.\"\n" +
            "            helpText=\"" + FOO + "\"/>\n" +
            "    <syntax name=\"stx1\" extends=\"foobar\" floating=\"no\"\n" + 
            "            usage=\"yo $super\" helpTopics=\"yoyo, yo\"\n" +
            "            actionName=\"stxB\"/>\n" +
            "</package>\n";
        final SyntaxPackage sp = sc.compile(getInput(data), Locale.US);
        verify(sp, "inherit.stx", 
               "hello\n\t[foo_count]\n\t[bar_entity]");

        Syntax s = (Syntax) sp.getNode("inherit.stx");
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(ba);
        
        SyntaxUsage su = new SyntaxUsage(Locale.US);
        su.addPackages(new HashSet<SyntaxPackage>() {{ add(sp); }});
        assertEquals("incorrect primary topic count: " + su.getPrimaryTopics(), 
                     2, su.getPrimaryTopics().size());
        assertEquals("incorrect topic count:" + su.getTopics(), 
                     3, su.getTopics().size());
        
        ba.reset();
        su.printHelp("yoyo", pw);
        pw.flush();
        print("[" + ba + "]");
        assertEquals("incorrect help", 
                     "usage: yo\n" + 
                     "\t[foo_count]\n" + 
                     "\t[bar_entity]" + EOL + 
                     EOL +
                     "    foo_count     Indicates the number of days the BAR entity needs to be" + EOL +  
                     "                  greeted in the morning, day and evening." + EOL +
                     "    bar_entity    Indicates the BAR entity that needs to be greeted each" + EOL + 
                     "                  morning, day and evening." + EOL + 
                     "-------------------------------------------------------------------------------" + EOL, 
                     ba.toString());

        ba.reset();
        su.printUsage("hello", pw, SyntaxUsage.TEXT);
        pw.flush();
        print("[" + ba + "]");
        assertEquals("incorrect usage", 
                     "usage: hello\n\t[foo_count]\n\t[bar_entity]" + EOL + EOL, 
                     ba.toString());
        
        ba.reset();
        su.printDescription(pw, s, SyntaxUsage.TEXT);
        pw.flush();
        print("[" + ba + "]");
        assertEquals("incorrect description", 
                     "This command simply greets anyone who it meets and " +
                     "does so with eagerness not" + EOL + 
                     "matched by any other." + EOL + EOL,
                     ba.toString());

        ba.reset();
        su.printUsageDetails(pw, s, SyntaxUsage.TEXT);
        pw.flush();
        print("[" + ba + "]");
        assertEquals("incorrect usage detail", 
                     "    foo_count     " + 
                     "Indicates the number of days the BAR entity needs to be" + EOL +
                     "                  " +
                     "greeted in the morning, day and evening." + EOL +
                     "    bar_entity    " + 
                     "Indicates the BAR entity that needs to be greeted each" + EOL +
                     "                  " +
                     "morning, day and evening." + EOL,
                     ba.toString());
        
        ba.reset();
        su.printHelpText(pw, s, SyntaxUsage.TEXT);
        pw.flush();
        print("[" + ba + "]");
        assertEquals("incorrect help text", 
                     "You know we are on a wrong track altogether. " + 
                     "We must not think of the things" + EOL +
                     "we could do with, but only of the things that we " + 
                     "can't do without." + EOL,
                     ba.toString());

        ba.reset();
        su.printHelp(pw, s);
        pw.flush();
        print("[" + ba + "]");
        assertEquals("incorrect help",
                     "usage: hello\n\t[foo_count]\n\t[bar_entity]" + EOL + EOL +
                     "This command simply greets anyone who it meets and does so with eagerness not" + EOL +
                     "matched by any other." + EOL + EOL +
                     "    foo_count     Indicates the number of days the BAR entity needs to be" + EOL +
                     "                  greeted in the morning, day and evening." + EOL +
                     "    bar_entity    Indicates the BAR entity that needs to be greeted each" + EOL +
                     "                  morning, day and evening." + EOL +
                     "You know we are on a wrong track altogether. We must not think of the things" + EOL +
                     "we could do with, but only of the things that we can't do without." + EOL,
                     ba.toString()); 
    }

    @Test
    public void testFirstTwoAnchorsBeforeAnyFloaters() throws Exception {
        SyntaxCompiler sc = SyntaxCompilerTest.getCompiler();
        SyntaxPackage sp;
        String data = "<?xml version=\"1.0\"?>\n" +
            "<package name=\"test\"\n" +
            "         resources=\"" + TEST_BUNDLE + "\">\n" +
            "    <syntax name=\"a\" usage=\"foo -a $bar\" actionName=\"A\">\n" +
            "        <parameter name=\"bar\" floating=\"yes\"/>\n" +
            "    </syntax>\n" +
            "    <syntax name=\"b\" usage=\"bar $foo -b\" actionName=\"B\">\n" +
            "        <parameter name=\"foo\" floating=\"yes\"/>\n" +
            "    </syntax>\n" +
            "    <syntax name=\"c\" usage=\"bar $foo -c\" actionName=\"B\">\n" +
            "        <parameter name=\"foo\" floating=\"no\"/>\n" +
            "    </syntax>\n" +
            "</package>\n";
        sp = sc.compile(getInput(data), Locale.US);
        verify(sp, "test.a", "foo -a\n\t[bar]"); 
        verify(sp, "test.b", "bar -b\n\t[foo]"); 
        verify(sp, "test.c", "bar foo -c"); 
    }

}

