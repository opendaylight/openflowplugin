/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Suite of tests used to observe the behaviours of Java Pattern class.
 *
 * @author Thomas Vachuska
 */
public class PatternTest {

    @Test 
    public void testReluctantPrefix() {
        Pattern p = Pattern.compile(".+?([a-z].*)$");
        Matcher m = p.matcher("256mb");
        assertTrue(m.matches());
        assertEquals("mb", m.group(1));
    }
   
}
