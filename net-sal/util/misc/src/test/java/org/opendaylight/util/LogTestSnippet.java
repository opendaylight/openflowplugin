/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;

import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.StringUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link org.opendaylight.util.Log}.
 *
 * @author Simon Hunt
 */
public class LogTestSnippet {


    private void snippetOne() {
        snippetTwo();
    }

    private void snippetTwo() {
        snippetThree();
    }

    private void snippetThree() {
        snippetFour();
    }

    private void snippetFour() {
        throw new RuntimeException("Test Exception");
    }

    private void altOne(Exception e) {
        altTwo(e);
    }

    private void altTwo(Exception e) {
        altThree(e);
    }

    private void altThree(Exception e) {
        altFour(e);
    }

    private void altFour(Exception e) {
        altFive(e);
    }

    private void altFive(Exception e) {
        altSix(e);
    }

    private void altSix(Exception e) {
        throw new RuntimeException("Alt Test Exception", e);
    }

    private Logger log;

    @Before
    public void setUp() {
        log = Log.GENERAL.getLogger();
    }

    private static final ClassLoader CL = LogTestSnippet.class.getClassLoader();
    private static final String MSG_SNIP = "Oops: {}{}";

    private static final String FAIL_NO_EXP_FILE = "Couldn't open data file: ";

    private String expResult(String filename) {
        String path = "org/opendaylight/util/" + filename + ".txt";
        String contents = "";
        try {
            contents = getFileContents(path, CL);
            if (contents == null)
                fail(FAIL_NO_EXP_FILE + path);
        } catch (IOException e) {
            fail(FAIL_NO_EXP_FILE + path);
        }
        return contents;
    }

    private String exp;
    private String act;

    @Test
    public void snippet() {
        print(EOL + "snippet()");
        try {
            snippetOne();
        } catch (Exception e) {
            String s = Log.stackTraceSnippet(e);
            log.warn(MSG_SNIP, e, s);
            exp = normalizeEOL(expResult("snippetA"));
            act = normalizeEOL(s + EOL);
            // Note: add EOL to counter blank line at end of test file
            assertEquals(AM_NEQ, exp, act);
        }
    }

    @Test
    public void snippetWithCause() {
        print(EOL + "snippetWithCause()");
        try {
            snippetOne();
        } catch (Exception e) {
            try {
                altOne(e);
            } catch (Exception e2) {
                String s = Log.stackTraceSnippet(e2);
                log.warn(MSG_SNIP, e2, s);
                exp = normalizeEOL(expResult("snippetB"));
                act = normalizeEOL(s + EOL);
                //Note: add EOL to counter blank line at end of test file
                assertEquals(AM_NEQ, exp, act);
            }
        }
    }

}
