/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;

/**
 * Builtin constraint validator for a boolean value.
 *
 * @author Thomas Vachuska
 */
public class BooleanConstraints implements Constraints {

    private String trueValue;
    private String falseValue;

    /** 
     * Default constructor.
     */
    public BooleanConstraints() {
    }

    /**
     * Constructs a new boolean parameter constraint.
     * 
     * @param trueValue value to use for true
     * @param falseValue value to use for false
     */
    public BooleanConstraints(String trueValue, String falseValue) {
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    /**
     * @see Constraints#isValid
     */
    @Override
    public boolean isValid(Serializable object) {
        return true;  // Boolean is always valid.
    }

    /**
     * Get the string image of boolean true value.
     * @return True value string.
     */
    public String getTrueValue() {
        return trueValue;
    }

    /**
     * Get the string image of boolean false value.
     * @return False value string.
     */
    public String getFalseValue() {
        return falseValue;
    }

}
