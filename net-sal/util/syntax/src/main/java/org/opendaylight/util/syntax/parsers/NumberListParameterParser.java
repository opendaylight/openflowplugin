/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;
import java.util.Properties;
import java.util.Locale;
import org.opendaylight.util.format.TokenTranslator;
import org.opendaylight.util.syntax.Parameters;
import org.opendaylight.util.syntax.ParserLoader;

/** 
 * Builtin parser for a list of number values.  
 *
 * @author Thomas Vachuska
 *
 */
public class NumberListParameterParser extends ListParameterParser {

    private static final long serialVersionUID = -3652110765836102470L;

    /** 
     * Default constructor.
     */
    public NumberListParameterParser() {
    }

    /**
     * Constructs a new string parameter parser using the given locale.
     *
     * @param locale Locale to associate with this parser.
     */
    public NumberListParameterParser(Locale locale) {
        super(locale);
    }

    /**
     * @see TypedParser#getTypeToken
     */
    @Override
    public String getTypeToken() {
        return "numbers";
    }
    
    /**
     * Returns an object describing particular constraints as defined in the
     * specified property database.
     *
     * @see ConstraintsParser#parse
     *
     * @return {@link NumberListConstraints} instance created using the
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
        String separator = tr.translate(db.getProperty("separator"));
        String minLength = tr.translate(db.getProperty("minLength"));
        String maxLength = tr.translate(db.getProperty("maxLength"));

        //  Create the constraint only when we need to.
        if (min != null || max != null || 
            allowDecimals != null || useGrouping != null ||
            unitPattern != null || unitNames != null || unitValues != null ||
            minLength != null || maxLength != null) {
            Integer minI = minLength != null ? new Integer(minLength) : null;
            Integer maxI = maxLength != null ? new Integer(maxLength) : null;
            return new NumberListConstraints(locale, min, max,
                                             Utilities.isOn(allowDecimals),
                                             Utilities.isOn(useGrouping),
                                             unitPattern, unitNames, unitValues,
                                             separator, minI, maxI);
        }
        return null;
    }
    
    /** 
     * Returns the parsed object corresponding to the given list item, which
     * in this case is a {@link Number} instance represented by the token.
     * 
     * @see ListParameterParser#parseItem
     *
     * @return The specified token instance.
     */
    @Override
    public Serializable parseItem(String token, Constraints constraints, 
                                  Parameters soFar) {
        if (constraints != null && constraints instanceof NumberConstraints) {
            NumberConstraints nc = (NumberConstraints) constraints;
            return Utilities.parse(nc.getFormat(), token);
        }
        return Utilities.parse(NumberConstraints.DEFAULT_FORMAT, token);
    }

}
