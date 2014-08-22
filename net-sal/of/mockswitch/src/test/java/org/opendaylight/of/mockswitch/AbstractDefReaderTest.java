/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.junit.Test;
import org.opendaylight.util.ValidationException;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.opendaylight.of.mockswitch.AbstractDefReader.LogicalLine;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for AbstractDefReader.
 *
 * @author Simon Hunt
 */
public class AbstractDefReaderTest {

    private static final String FILE_PREFIX = "org/opendaylight/of/mockswitch/";
    private static final String DEF = ".def";

    private AbstractDefReader adr;

    private void verifyLine(LogicalLine ll, String ln, String text) {
        assertEquals(AM_NEQ, ln, ll.lineSpan());
        assertEquals(AM_NEQ, text, ll.getText());
    }

    // a simple reader with no constraints on the non-comment line format
    private static class SimpleDefReader extends AbstractDefReader {
        protected SimpleDefReader(String path) {
            super(path);
        }
        @Override
        protected String parseLogicalLine(LogicalLine line) {
            return null;  // always accept the lines
        }
    }

    private AbstractDefReader getSimpleReader(String name) {
        print(EOL + name + "()");
        AbstractDefReader adr = new SimpleDefReader(FILE_PREFIX + name + DEF);
        print(adr.toDebugString());
        return adr;
    }

    @Test
    public void reader01() {
        adr = getSimpleReader("reader01");
        List<LogicalLine> lines = adr.getLines();
        assertEquals(AM_UXS, 4, lines.size());
        Iterator<LogicalLine> iter = lines.iterator();
        verifyLine(iter.next(), "5", "Non-comment 1");
        verifyLine(iter.next(), "7", "Non-comment 2");
        verifyLine(iter.next(), "9-11", "Non-comment 3 with continuation " +
                                        "that has an indented set of stuff");
        verifyLine(iter.next(), "14", "Last line");
        assertFalse(iter.hasNext());
    }

    @Test
    public void reader02() {
        adr = getSimpleReader("reader02");
        assertEquals(AM_NEQ, "{SimpleDefReader: path=\"org/opendaylight/of/mockswitch/" +
                "reader02.def\",#logical=0,#lines=1}", adr.toString());
        assertEquals(AM_UXS, 0, adr.getLines().size());
    }


    // == a little more complexity, allowing only lines of the form:
    // key: some value

    private static final Pattern RE_KV = Pattern.compile("(\\w+):\\s+(.*)");

    private static class KVPairDefReader extends AbstractDefReader {
        protected KVPairDefReader(String path) {
            super(path);
        }
        @Override
        protected String parseLogicalLine(LogicalLine line) {
            return RE_KV.matcher(line.getText()).matches() ? null
                    : "invalid format: \"" + line.getText() + "\"";
        }
    }

    private AbstractDefReader getKvReader(String name) {
        print(EOL + name + "()");
        AbstractDefReader adr = new KVPairDefReader(FILE_PREFIX + name + DEF);
        print(adr.toDebugString());
        return adr;
    }

    @Test
    public void readerKv01() {
        adr = getKvReader("readerKv01");
        Iterator<LogicalLine> iter = adr.getLines().iterator();
        verifyLine(iter.next(), "3", "foo: bar");
        verifyLine(iter.next(), "5-8", "goo: 1 22 333 4444");
        verifyLine(iter.next(), "10", "zoo: zebra");
        assertFalse(iter.hasNext());
    }

    @Test
    public void readerKv02() {
        try {
            adr = getKvReader("readerKv02");
        } catch (ValidationException ve) {
            assertEquals(AM_NEQ, "Failed to parse org/opendaylight/of/mockswitch/readerKv02.def",
                    ve.getMessage());
            Iterator<String> iter = ve.getIssues().iterator();
            assertEquals(AM_NEQ, "Line 8: invalid format: \"globby bits\"",
                    iter.next());
            assertEquals(AM_NEQ, "Line 15: invalid format: \"gotcha; foibles\"",
                    iter.next());
            assertFalse(AM_HUH, iter.hasNext());
        }
    }

    @Test
    public void readerKv03() {
        try {
            adr = getKvReader("readerKv03");
        } catch (ValidationException ve) {
            assertEquals(AM_NEQ, "Failed to parse org/opendaylight/of/mockswitch/readerKv03.def",
                    ve.getMessage());

            Iterator<String> iter = ve.getIssues().iterator();
            assertEquals(AM_NEQ,
                    "Line 3: invalid format: \"some input line\"", iter.next());
            assertEquals(AM_NEQ,
                    "Line 4-5: invalid format: \"another input line continued " +
                            "on the next line\"",
                    iter.next());
            assertEquals(AM_NEQ,
                    "Line 8: invalid format: \"last line\"", iter.next());
            assertFalse(AM_HUH, iter.hasNext());
        }
    }

}
