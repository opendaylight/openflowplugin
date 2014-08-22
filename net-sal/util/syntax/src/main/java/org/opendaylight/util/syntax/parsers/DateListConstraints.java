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
 * Builtin constraints validator for a list of date &amp; time values.  
 *
 * @author Thomas Vachuska
*/
public class DateListConstraints extends DateConstraints
                    implements ListConstraints {

    private String separator = null;
    private Integer minLength = null;
    private Integer maxLength = null;
        
    /** 
     * Default constructor.
     */
    public DateListConstraints() {
    }

    /**
     * Creates a constraints validator for lists of dates.
     *
     * @param format Format string as expected by {@link java.text.DateFormat}
     * @param locale Locale context for parsing/formating the date values.
     * @param min If not null, it specifies the minimum numerical value the
     * individual parsed argument can have to be a valid parameter.
     * @param max If not null, it specifies the maximum numerical value the
     * individual parsed argument can have to be a valid parameter.
     * @param separator Single character delimiter for list items.
     * @param minLength Minimum length of the list of string tokens.
     * @param maxLength Maximum length of the list of string tokens.
     */
    public DateListConstraints(String format, Locale locale,
                               String min, String max, String separator,
                               Integer minLength, Integer maxLength) {
        super (format, locale, min, max);
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
