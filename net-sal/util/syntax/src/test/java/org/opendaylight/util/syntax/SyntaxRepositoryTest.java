/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.util.syntax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Suite of tests for the syntax aggregator facility.
 *
 * @author Thomas Vachuska
 */
public class SyntaxRepositoryTest {
    
    private static SyntaxRepository repo;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        repo = new SyntaxRepository();
        ClassLoader cl = repo.getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream("org/opendaylight/util/syntax/bar.xml");
        repo.addSyntaxDefinitions(is);
    }

    @Test
    public void testLists() throws Exception {
        // We only need to do a very light-weight test here as we are testing
        // the basic operation of the syntax repository and not the syntax
        // definitions themselves; those are tested elsewhere
        String args[] = new String[] { "mxexec", "-t", "foo", "-A", "anything" };
        SyntaxMatch match = repo.match(args);
        assertNotNull("match should be found", match);
        assertEquals("wrong command matched", 
                     "org.opendaylight.mx.foo.LegacyHandler:invoke",
                     match.syntax().getActionName());
        assertTrue("wrong args retained", Arrays.equals(args, match.args()));
        assertNotNull("parameters should be present", match.parameters());
        assertEquals("incorrect creator", repo, match.repo());
//        System.out.println(match);
    }

}
