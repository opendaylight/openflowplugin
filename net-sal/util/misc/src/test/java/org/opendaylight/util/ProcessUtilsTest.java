/*
 * (c) Copyright 2010 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import static org.opendaylight.util.ProcessUtils.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

/**
 * Test suite of process launch utilities.
 *
 * @author Thomas Vachuska
 */
public class ProcessUtilsTest {

    @Test
    public void testExec() throws Exception {
        assertTrue("some output expected", exec("hostname").length() > 2);
        assertTrue("some output expected", exec(new String[] { "hostname" }).length() > 2);
        assertTrue("some output expected", exec("hostname", ".").length() > 2);
        assertTrue("some output expected", exec(new String[] { "hostname" }, ".").length() > 2);
    }

    @Test
    public void testExecute() throws Exception {
        assertTrue("some output expected", slurp(execute("hostname")).length() > 2);
        assertTrue("some output expected", slurp(execute("hostname", ".")).length() > 2);
        assertTrue("some output expected", slurp(execute(new String[] { "hostname" }, ".")).length() > 2);
    }

    @Test
    public void testNoOutputCommand() throws Exception {
        assumeNotNull(exec("true"));
        assertEquals("no output expected", "", slurp(execute("true")));
        assertEquals("no output expected", "", slurp(execute("false")));
        assertEquals("no output expected", "", exec("true"));
    }

    @Test
    public void testBadCommand() throws Exception {
        assertNull("null output expected", exec("qWeRtYuIoP"));
        assertNull("null output expected", slurp(execute("qWeRtYuIoP")));
        assertNull("null output expected", slurp(execute(new String[] { "qWeRtYuIoP" }, "")));
    }
    
    @Test
    public void testBadSlurp() throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream("yo".getBytes())));
        br.close();
        assertNull("null slurp expected", slurp(br));
    }

}
