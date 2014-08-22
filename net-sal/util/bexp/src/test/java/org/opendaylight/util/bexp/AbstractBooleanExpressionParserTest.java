/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.bexp;

import static org.junit.Assert.*;

import org.opendaylight.util.bexp.AbstractBooleanExpressionParser;
import org.opendaylight.util.bexp.BooleanExpression;

import org.junit.Before;
import org.junit.Test;

/**
 * Suite of tests of the abstract boolean expression parser capabilities.
 *
 * @author Thomas Vachuska
 */
public class AbstractBooleanExpressionParserTest {
    
    TestBooleanExpressionParser parser;
    
    @Before
    public void setUp() throws Exception {
        parser = new TestBooleanExpressionParser(); 
    }
    
    private void validate(String expression, boolean expected) throws Exception {
        BooleanExpression bexp = parser.parse(expression);
        assertEquals(expression, expected, bexp.value());
    }

    @Test
    public void testConstants() {
        assertTrue("true always", BooleanExpression.TRUE.value());
        assertFalse("false always", BooleanExpression.FALSE.value());
    }
    
    @Test
    public void testBasicTrueFalse() throws Exception {
        validate("true", true);
        validate("false", false);
    }

    @Test
    public void testBasicNegation() throws Exception {
        validate("not true", false);
        validate("not false", true);
    }

    @Test
    public void testBasicConjunction() throws Exception {
        validate("true and true", true);
        validate("true and false", false);
        validate("false and true", false);
        validate("false and false", false);
    }

    @Test
    public void testBasicDisjunction() throws Exception {
        validate("true or true", true);
        validate("true or false", true);
        validate("false or true", true);
        validate("false or false", false);
    }

    @Test
    public void testBasicGrouping() throws Exception {
        validate("true and true or false", true);
        validate("true and (true or false)", true);
        validate("not true and (true or false)", false);
        validate("not (true and (true or false))", false);
        validate("not false and true or false", true);
    }

    @Test(expected=java.text.ParseException.class)
    public void testMissingKeyword() throws Exception {
        validate("true (true)", false);
    }

    @Test(expected=java.text.ParseException.class)
    public void testMissingNotArgument() throws Exception {
        validate("not", false);
    }

    @Test(expected=java.text.ParseException.class)
    public void testMissingAndArgument() throws Exception {
        validate("true and", false);
    }

    @Test(expected=java.text.ParseException.class)
    public void testMissingOrArgument() throws Exception {
        validate("false or", false);
    }

    @Test(expected=java.text.ParseException.class)
    public void testMissingOpen() throws Exception {
        validate("false or true)", false);
    }

    @Test(expected=java.text.ParseException.class)
    public void testMissingClose() throws Exception {
        validate("false or (true", false);
    }

    /**
     * Auxiliary boolean expression implementation
     */
    private static class TestBooleanExpression implements BooleanExpression {

        private boolean value;
        
        TestBooleanExpression(String token) {
            this.value = token.equals("true");
        }
        
        @Override
        public boolean value() {
            return value;
        }
        
    }
    
    /**
     * Auxiliary boolean expression parser implementation
     */
    private static class TestBooleanExpressionParser 
                    extends AbstractBooleanExpressionParser {

        @Override
        public BooleanExpression expression(String token) {
            return new TestBooleanExpression(token);
        }
    
    }

}
