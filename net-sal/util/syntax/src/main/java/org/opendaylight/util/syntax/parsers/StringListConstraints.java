/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

/** 
 * Builtin constraints validator for a list of string values.  
 *
 * @author Thomas Vachuska
 *
 */
public class StringListConstraints extends StringConstraints
                    implements ListConstraints {

    private String separator = ListConstraints.SEPARATOR;
    private Integer minLength = null;
    private Integer maxLength = null;

    /** 
     * Default constructor.
     */
    public StringListConstraints() {
    }
        
    /**
     * Creates a constraints validator for lists of strings.
     *
     * @param min If not null, it specifies the minimum lexicographical value
     * the individual parsed argument can have to be a valid parameter.
     * @param max If not null, it specifies the maximum lexicographical value
     * the individual parsed argument can have to be a valid parameter.
     * @param legalValues A string-set token, listing all legal values that
     * the individual parsed argument can take on. The values in the set are
     * separated via the pipe (|) character and abbreviated values can be
     * specified using the asterisk (*) character.
     * @param illegalValues A string-set token, listing all illegal values that
     * the individual parsed argument cannot take on. The values in the set are
     * separated via the pipe (|) character and abbreviated values can be
     * specified using the asterisk (*) character.
     * @param legalERE Regular expression which the parameter must match, 
     * or null.
     * @param illegalERE Regular expression which the parameter must not match,
     *  or null.
     * @param separator Single character delimiter for list items.
     * @param minLength Minimum length of the list of string tokens.
     * @param maxLength Maximum length of the list of string tokens.
     */
    public StringListConstraints(String min, String max, 
                                 String legalValues, String illegalValues,
                                 String legalERE, String illegalERE,
                                 String separator,
                                 Integer minLength, Integer maxLength) {
        super(min, max, legalValues, illegalValues, legalERE, illegalERE);
        this.minLength = minLength;
        this.maxLength = maxLength;
        if (separator != null)
            this.separator = separator;
    }

    /** 
     * @see ListConstraints#getSeparator 
     */
    @Override
    public String getSeparator() { 
        return separator; 
    }

    /** 
     * @see ListConstraints#getMinLength 
     */
    @Override
    public Integer getMinLength() { 
        return minLength;
    }

    /** 
     * @see ListConstraints#getMaxLength 
     */
    @Override
    public Integer getMaxLength() {
        return maxLength;
    }

}
