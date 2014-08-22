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
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.opendaylight.util.format.TokenTranslator;
import org.opendaylight.util.syntax.Parameters;
import org.opendaylight.util.syntax.ParserLoader;

/**
 * Builtin parser for date & time values.
 *
 *  @author Thomas Vachuska
 */
public class DateParameterParser extends AbstractParameterParser {

    private static final long serialVersionUID = 5384699348069922442L;

    /** 
     * Default constructor.
     */
    public DateParameterParser() {
    }

    /**
     * Constructs a new number parameter parser using the given locale.
     *
     * @param locale Locale to associate with this parser.
     */
    public DateParameterParser(Locale locale) {
        super(locale);
    }

    /**
     * @see TypedParser#getTypeToken
     */
    @Override
    public String getTypeToken() {
        return "date";
    }

    /**
     * Creates an instance of {@link DateConstraints}.
     *
     * @see ConstraintsParser#parse
     *
     * @return {@link DateConstraints} instance created using the
     * specified properties.
     */
    @Override
    public Constraints parse(Properties db, TokenTranslator tr,
                             ParserLoader parserLoader) {
        String format = tr.translate(db.getProperty("format"));
        String min = tr.translate(db.getProperty("minValue"));
        String max = tr.translate(db.getProperty("maxValue"));

        //  Create the constraint only when we need to.
        if (min != null || max != null || format != null)
            return new DateConstraints(format, locale, min, max);
        return null;
    }


    /** 
     * Returns a non-null object if the parsing of the given token was
     * succesful and if the object value complied with the specified
     * constraints, if any were specified.
     *
     * @see ParameterParser#parse
     *
     * @return A validated {@link java.util.Date} instance or null if
     * the resulting Date value does not meet the constraints, if any.
     */
    @Override
    public Serializable parse(String token, Constraints constraints, 
                              Parameters soFar) {
        if (constraints != null && constraints instanceof DateConstraints) {
            DateConstraints dc = (DateConstraints) constraints;
            Date date = Utilities.parse(dc.getFormat(), token);
            if (dc.isValid (date))
                return date;
        } else {
            //  Try the fallback date format.
            return Utilities.parse(new SimpleDateFormat(DateConstraints.DEFAULT_FORMAT), token);
        }
        return null;
    }

}
