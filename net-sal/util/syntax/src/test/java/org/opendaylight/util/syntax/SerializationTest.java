/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * JUnit tests to verify serializability of SyntaxNode and its derivatives.
 *
 * @author Thomas Vachuska
 */
@SuppressWarnings("serial")
public class SerializationTest {

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


    private static class MySyntaxPackage extends SyntaxPackage {
        MySyntaxPackage() { }
    }

    private static class MySyntaxKeyword extends SyntaxKeyword {
        MySyntaxKeyword() { }
    }

    private static class MySyntaxParameter extends SyntaxParameter {
        MySyntaxParameter() { }
    }

    private static class MySyntax extends Syntax {
        MySyntax() {  }
    }

    @Test
    public void testDefaultConstructors() {
        new MySyntaxPackage();
        new MySyntaxKeyword();
        new MySyntaxParameter();
        new MySyntax();
    }

}

