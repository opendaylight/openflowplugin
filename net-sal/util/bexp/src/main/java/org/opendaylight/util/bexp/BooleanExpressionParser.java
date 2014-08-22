/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.bexp;

import java.text.ParseException;

/**
 * Abstraction of a parser capable of translating an expression in the string
 * form into a boolean expression.
 * 
 * @author Thomas Vachuska
 */
public interface BooleanExpressionParser {

    /**
     * Parse the specified string expression and produce a corresponding
     * {@link BooleanExpression}
     * 
     * @param expressionString expression in the string form
     * @return boolean expression
     * @throws ParseException thrown when an error occurs while parsing the
     *         given expression string
     */
    BooleanExpression parse(String expressionString) throws ParseException;

}
