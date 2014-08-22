/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;
import java.util.Locale;
import java.util.Properties;
import org.opendaylight.util.format.TokenTranslator;
import org.opendaylight.util.syntax.Parameters;
import org.opendaylight.util.syntax.ParserLoader;

/** 
 * Builtin parser for number values.  
 *
 * @author Thomas Vachuska
 *
 */
public class NumberParameterParser extends AbstractParameterParser {

    private static final long serialVersionUID = -650633382551646969L;

    /** 
     * Default constructor.
     */
    public NumberParameterParser() {
    }

    /**
     * Constructs a new number parameter parser using the given locale.
     *
     * @param locale Locale to associate with this parser.
     */
    public NumberParameterParser(Locale locale) {
        super(locale);
    }

    /**
     * @see TypedParser#getTypeToken
     */
    @Override
    public String getTypeToken() {
        return "number";
    }
    
    /**
     * Creates an instance of {@link NumberConstraints}.
     *
     * @see ConstraintsParser#parse
     *
     * @return {@link NumberConstraints} instance created using the
     * specified properties.
     */
    @Override
    public Constraints parse(Properties db, TokenTranslator tr,
                             ParserLoader parserLoader) {
        String min = tr.translate(db.getProperty("minValue"));
        String max = tr.translate(db.getProperty("maxValue"));
        String allowDecimals = tr.translate(db.getProperty("allowDecimals"));
        String useGrouping = tr.translate(db.getProperty("useGrouping"));
        String unitPattern = tr.translate(db.getProperty("unitPattern"));
        String unitNames = tr.translate(db.getProperty("unitNames"));
        String unitValues = tr.translate(db.getProperty("unitValues"));

        //  Create the constraint only when we need to.
        if (min != null || max != null ||
            allowDecimals != null || useGrouping != null || 
            unitPattern != null || unitNames != null || unitValues != null) {
            return new NumberConstraints(locale, min, max,
                                         Utilities.isOn(allowDecimals), 
                                         Utilities.isOn(useGrouping),
                                         unitPattern, unitNames, unitValues);
        }
        return null;
    }
    
    /** 
     * Returns a non-null object if the parsing of the given token was
     * succesful and if the object value complied with the specified
     * constraints, if any were specified.
     *
     * @see ParameterParser#parse
     *
     * @return A validated {@link Number} instance or null if the
     * resulting Number value does not meet the constraints, if any.
     */
    @Override
    public Serializable parse(String token, Constraints constraints,
                              Parameters soFar) {
        if (constraints != null && constraints instanceof NumberConstraints) {
            NumberConstraints nc = (NumberConstraints) constraints;
            Number number = null;
            
            // If the constraints expect a postfix, let's deal with it
            String unit = nc.getUnitName(token);
            if (unit == null) {
                number = Utilities.parse(nc.getFormat(), token);
            } else {
                String ntoken = token.substring(0, token.length() - unit.length());
                number = Utilities.parse(nc.getFormat(), ntoken);
                number = nc.getUnitBasedValue(number, unit);
            }

            // Finally return the validated number
            if (nc.isValid(number))
                return number;
        } else {
            //  Try the fallback number format.
            return Utilities.parse(NumberConstraints.DEFAULT_FORMAT, token);
        }
        return null;
    }

}
