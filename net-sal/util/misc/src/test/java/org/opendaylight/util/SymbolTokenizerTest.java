/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.junit.Assert.*;



import org.junit.Before;
import org.junit.Test;
import org.opendaylight.util.SymbolTokenizer;

/**
 * Suite of tests for the symbol tokenizer utility.
 *
 * @author Thomas Vachuska
 */
public class SymbolTokenizerTest {
    
    protected SymbolTokenizer st;
    
    private static final String[] SYMBOLS = new String[] {
        "not", "and", "or", "(", ")"
    };

    private static final String[] PRE_DELIMITERS = new String[] {
        " (", ") ", ") ", null, null
    };

    private static final String[] POST_DELIMITERS = new String[] {
        " (", " (", " (", null, null
    };

    @Before
    public void setUp() {
        st = new SymbolTokenizer(SYMBOLS, PRE_DELIMITERS, POST_DELIMITERS); 
    }

    private void validate(String expected, boolean wasSymbol) {
        if (expected != null) {
            assertTrue("expecting more tokens", st.hasNext());
            assertEquals("unexpected token", expected, st.next());
            assertEquals("wrong wasSymbol value", wasSymbol, st.wasSymbol());
        } else {
            assertFalse("null string; no token", st.hasNext());
            assertEquals("expecting null token", null, st.next());
        }
    }
    
    @Test
    public void testBasics() {
        validate(null, false);
        assertNull("string should be null", st.string());
        assertEquals("incorrect position", 0, st.position());
        
        st.setString("nothing");
        for (int i = 0; i < 2; i++) {
            assertEquals("incorrect string", "nothing", st.string());
            assertEquals("incorrect position", 0, st.position());
            validate("nothing", false);
            assertEquals("incorrect position", 7, st.position());
            validate(null, false);
            st.reset();
        }
    }
    
    @Test
    public void testStartSymbol() {
        st.setString("not anything special");
        validate("not", true);
        validate("anything special", false);
        validate(null, false);

        st.setString("not (anything special)");
        validate("not", true);
        validate("(", true);
        validate("anything special", false);
        validate(")", true);
        validate(null, false);
    }

    @Test
    public void testNextSymbol() {
        st.setString("not anything special and extraordinary");
        validate("not", true);
        validate("anything special", false);
        validate("and", true);
        validate("extraordinary", false);
        validate(null, false);
    }

    @Test
    public void testMultiSymbols() {
        st.setString("not anything special and extraordinary or terrifying");
        validate("not", true);
        validate("anything special", false);
        validate("and", true);
        validate("extraordinary", false);
        validate("or", true);
        validate("terrifying", false);
        validate(null, false);
    }

    @Test
    public void testOtherConstructor() {
        st = new SymbolTokenizer("not  false and (( true or false ) ) ", 
                                  SYMBOLS, PRE_DELIMITERS, POST_DELIMITERS);
        validate("not", true);
        validate("false", false);
        validate("and", true);
        validate("(", true);
        validate("(", true);
        validate("true", false);
        validate("or", true);
        validate("false", false);
        validate(")", true);
        validate(")", true);
        validate(null, false);
    }

}
