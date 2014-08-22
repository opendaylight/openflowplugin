/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.ResourceIdUtils.extract;
import static org.opendaylight.util.ResourceIdUtils.replace;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Set of tests for the resource ID manipulation utilities.
 *
 * @author Thomas Vachuska
 */
public class ResourceIdUtilsTest {

    @Test
    public void basics() {
        assertEquals("incorrect value", "foo",
                     extract("/dingo/foo/bar", "did", "/dingo/{did}/{fib}"));
        assertEquals("incorrect value", "bar",
                     extract("/dingo/foo/bar", "fib", "/dingo/{did}/{fib}"));
        
        assertEquals("incorrect path", "/dingo/foo/{fib}",
                     replace("foo", "did", "/dingo/{did}/{fib}"));
        assertEquals("incorrect path", "/dingo/{did}/bar",
                     replace("bar", "fib", "/dingo/{did}/{fib}"));
    }

    @Test
    public void errors() {
        assertNull("should be null",
                   extract("/foo/bar", "fib", "/dingo/{did}/{fib}"));
        assertNull("should be null",
                   extract("/foo/bar", "did", "/dingo/{did}/{fib}"));

        assertNull("should be null",
                   extract("/dingo/foo/bar", "FIB", "/dingo/{did}/{fib}"));
        assertNull("should be null",
                   extract("/dingo/bar", "fib", "/dingo/{did}/{fib}"));
        assertNull("should be null",
                   extract("/dingo/foo", "did", "/dingo/{did}/{fib}"));
        assertNull("should be null",
                   extract("/dingo/", "did", "/dingo/{did}/{fib}"));
    }
    
}
