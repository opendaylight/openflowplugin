/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.bexp;

/**
 * Abstraction of an arbitrary boolean expression.
 *
 * @author Thomas Vachuska
 */
public interface BooleanExpression {
    
    /** Boolean expression that is always true.  */
    public static final BooleanExpression TRUE = new BooleanExpression() {
        @Override public boolean value() { return true; }
    };
    
    /** Boolean expression that is always false.  */
    public static final BooleanExpression FALSE = new BooleanExpression() {
        @Override public boolean value() { return false; }
    };
    
    /**
     * Get the expression value.
     * 
     * @return true or false to reflect the value of the boolean expression
     */
    boolean value();

}
