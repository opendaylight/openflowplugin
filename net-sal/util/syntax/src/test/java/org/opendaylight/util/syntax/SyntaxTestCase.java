/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.io.File;

import org.opendaylight.util.syntax.fixtures.SyntaxTester;

import org.junit.Test;


/**
 * Test for the syntax tester base-class and an example of how to use it.
 * 
 * @author Thomas Vachuska
 */
public class SyntaxTestCase extends SyntaxTester {

    @Test
    public void testSyntaxDefinitions() throws Exception {
        runTests(new File(getTestLocation()));
    }
    
    public String getTestLocation() {
        return DEFAULT_SCHEMA_LOCATION;
    }
        
}
