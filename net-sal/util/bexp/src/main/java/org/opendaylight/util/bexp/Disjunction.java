/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.bexp;

/**
 * Binary disjunction (OR) of two other boolean expressions.
 *
 * @author Thomas Vachuska
 */
public class Disjunction implements BooleanExpression {
    
    private BooleanExpression bexp1, bexp2;

    /**
     * Create a disjunction of the two specified boolean expressions. Note
     * that the second expression may never be evaluated if the first
     * evaluates to true.
     * 
     * @param bexp1 first (left) boolean expression
     * @param bexp2 second (right) boolean expression
     */
    public Disjunction(BooleanExpression bexp1, BooleanExpression bexp2) {
        this.bexp1 = bexp1;
        this.bexp2 = bexp2;
    }
    
    @Override
    public boolean value() {
        return bexp1.value() || bexp2.value();
    }
    
}
