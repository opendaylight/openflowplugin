/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;

import org.opendaylight.util.format.TokenTranslator;
import org.opendaylight.util.syntax.Parameters;
import org.opendaylight.util.syntax.ParserLoader;

/** 
 * Builtin parser for a list of date &amp; time values.  
 *
 * @author Thomas Vachuska
*/
public class DateListParameterParser extends ListParameterParser {

    private static final long serialVersionUID = 2110692331050921817L;

    /** 
     * Default constructor.
     */
    public DateListParameterParser() {
    }

    /**
     * Constructs a new string parameter parser using the given locale.
     *
     * @param locale Locale to associate with this parser.
     */
    public DateListParameterParser(Locale locale) {
        super(locale);
    }

    /**
     * @see TypedParser#getTypeToken
     */
    @Override
    public String getTypeToken() {
        return "dates";
    }
    
    /**
     * Returns an object describing particular constraints as defined in the
     * specified property database.
     *
     * @see ConstraintsParser#parse
     *
     * @return {@link DateListConstraints} instance created using the
     * specified properties.
     */
    @Override
    public Constraints parse(Properties db, TokenTranslator tr, 
                             ParserLoader parserLoader) {
        String format = tr.translate(db.getProperty("format"));
        String min = tr.translate(db.getProperty("minValue"));
        String max = tr.translate(db.getProperty("maxValue"));
        String separator = tr.translate(db.getProperty("separator"));
        String minLength = tr.translate(db.getProperty("minLength"));
        String maxLength = tr.translate(db.getProperty("maxLength"));

        //  Create the constraint only when we need to.
        if (format != null || min != null || max != null ||
            minLength != null || maxLength != null) {
            Integer minI = minLength != null ? new Integer(minLength) : null;
            Integer maxI = maxLength != null ? new Integer(maxLength) : null;
            return new DateListConstraints(format, locale, min, max, 
                                           separator, minI, maxI);
        }
        return null;
    }
    
    /** 
     * Returns the parsed object corresponding to the given list item, which
     * in this case is a {@link java.util.Date} instance represented by the
     * token.
     * 
     * @see ListParameterParser#parseItem
     *
     * @return The specified token instance.
     */
    @Override
    public Serializable parseItem(String token, Constraints constraints,
                                  Parameters soFar) {
        if (constraints != null && constraints instanceof DateConstraints) {
            DateConstraints dc = (DateConstraints) constraints;
            return Utilities.parse(dc.getFormat(), token);
        }
        return Utilities.parse(new SimpleDateFormat(DateConstraints.DEFAULT_FORMAT), token);
    }

}
