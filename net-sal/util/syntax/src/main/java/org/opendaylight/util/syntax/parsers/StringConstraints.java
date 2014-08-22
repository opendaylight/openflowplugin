/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;

/** 
 * Builtin constraints validator for a string value.  
 *
 * @author Thomas Vachuska
 *
 */
public class StringConstraints implements Constraints {
        
    protected String min = null;
    protected String max = null;
    protected String legalERE = null;
    protected String illegalERE = null;
    protected StringSet legalValues = null;
    protected StringSet illegalValues = null;

    /** 
     * Default constructor.
     */
    public StringConstraints() {
    }
        
    /**
     * Creates a string constraints enforcement entity.
     *
     * @param min If not null, it specifies the minimum lexicographical value
     * the parsed argument can have to be a valid parameter.
     * @param max If not null, it specifies the maximum lexicographical value
     * the parsed argument can have to be a valid parameter.
     * @param legalValues A string-set token, listing all legal values that
     * the parsed argument can take on. The values in the set are separated
     * via the pipe (|) character and abbreviated values can be specified
     * using the asterisk (*) character.
     * @param illegalValues A string-set token, listing all illegal values that
     * the parsed argument cannot take on. The values in the set are separated
     * via the pipe (|) character and abbreviated values can be specified
     * using the asterisk (*) character.
     * @param legalERE Regular expression which the parameter must match, 
     * or null.
     * @param illegalERE Regular expression which the parameter must not match,
     *  or null.
     */
    public StringConstraints(String min, String max, 
                             String legalValues, String illegalValues, 
                             String legalERE, String illegalERE) {
        this.min = min;
        this.max = max;
        this.legalERE = legalERE;
        this.illegalERE = illegalERE;
        if (legalValues != null)
            this.legalValues = new StringSet(legalValues, 
                                             StringSet.VALUE_DELIMITER,
                                             StringSet.ABBREV_DELIMITER);
        if (illegalValues != null)
            this.illegalValues = new StringSet(illegalValues, 
                                               StringSet.VALUE_DELIMITER,
                                               StringSet.ABBREV_DELIMITER);
    }

    /**
     * @see Constraints#isValid
     */
    @Override
    public boolean isValid(Serializable o) {
        if (o == null)
            return false;
        String s = o.toString();
        return (legalValues == null || legalValues.contains(s)) &&
            (illegalValues == null || !illegalValues.contains(s)) &&
            (min == null || min.compareTo(s) <= 0) &&
            (max == null || max.compareTo(s) >= 0) &&
            (legalERE == null || s.matches(legalERE)) && 
            (illegalERE == null || !s.matches(illegalERE)); 
    }
        
}
    
