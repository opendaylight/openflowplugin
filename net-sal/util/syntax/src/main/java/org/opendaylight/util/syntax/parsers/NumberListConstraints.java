/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.util.Locale;

/** 
 * Builtin constraints validator for a list of number values.  
 *
 * @author Thomas Vachuska
 *
 */
public class NumberListConstraints extends NumberConstraints
                    implements ListConstraints {

    private String separator = null;
    private Integer minLength = null;
    private Integer maxLength = null;
        
    /** 
     * Default constructor.
     */
    public NumberListConstraints() {
    }

    /**
     * Creates a constraints validator for lists of numbers.
     * 
     * @param locale context locale for validation
     * @param min If not null, it specifies the minimum numerical value the
     *        individual parsed argument can have to be a valid parameter.
     * @param max If not null, it specifies the maximum numerical value the
     *        individual parsed argument can have to be a valid parameter.
     * @param allowDecimals Boolean indicating whether decimal values are
     *        allowed.
     * @param useGrouping Boolean indicating whether grouping separator is
     *        allowed.
     * @param unitPattern regexp pattern for finding the start of units;
     *        defaults to [:alpha:] character set allowed.
     * @param unitNames comma-separated list of string-sets
     * @param unitValues comma-separated list of unit multiplier values
     * @param separator Single character delimiter for list items.
     * @param minLength Minimum length of the list of string tokens.
     * @param maxLength Maximum length of the list of string tokens.
     */
    public NumberListConstraints(Locale locale, String min, String max,
                                 boolean allowDecimals, boolean useGrouping,
                                 String unitPattern, String unitNames,
                                 String unitValues, String separator, 
                                 Integer minLength, Integer maxLength) {
        super(locale, min, max, allowDecimals, useGrouping, unitPattern,
              unitNames, unitValues);
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
