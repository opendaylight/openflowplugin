/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.bexp;

/**
 * Unary negation (NOT) of another boolean expression.
 *
 * @author Thomas Vachuska
 */
public class Negation implements BooleanExpression {
    
    private BooleanExpression bexp;

    /**
     * Create a negation of the specified boolean expression
     * 
     * @param bexp boolean expression to be negated
     */
    public Negation(BooleanExpression bexp) {
        this.bexp = bexp;
    }
    
    @Override
    public boolean value() {
        return !bexp.value();
    }
    
}
