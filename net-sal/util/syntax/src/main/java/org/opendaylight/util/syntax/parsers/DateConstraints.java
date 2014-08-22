/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Builtin constraint validator for a date & time value.
 *
 * @author Thomas Vachuska
 */
public class DateConstraints implements Constraints {

    /**
     * Default format for date parsing "yyyy-MM-dd/HH:mm:ss"
     */
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd/HH:mm:ss";

    protected Date min = null;
    protected Date max = null;
    protected DateFormat format = new SimpleDateFormat(DEFAULT_FORMAT);

    /** 
     * Default constructor.
     */
    public DateConstraints() {
    }
        
    /**
     * Creates a date constraints enforcement entity.
     * 
     * @param format date format to be used for validation
     * @param locale context locale for validation
     * @param min If not null, it specifies the minimum temporal value the
     *        parsed argument can have to be a valid parameter.
     * @param max If not null, it specifies the maximum temporal value the
     *        parsed argument can have to be a valid parameter.
     */
    public DateConstraints(String format, Locale locale,
                           String min, String max) {
        if (format != null)
            this.format = new SimpleDateFormat(format, locale);
        this.min = Utilities.parse (this.format, min);
        this.max = Utilities.parse (this.format, max);
    }

    /**
     * @see Constraints#isValid
     */
    @Override
    public boolean isValid(Serializable o) {
        return o != null && o instanceof Date &&
            (min == null || min.compareTo((Date) o) <= 0) &&
            (max == null || max.compareTo((Date) o) >= 0);
    }

    /**
     * Get the format for parsing the date/time values.
     * 
     * @return current date format to be used for parsing and validation
     */
    public DateFormat getFormat() { 
        return format;
    }

}

