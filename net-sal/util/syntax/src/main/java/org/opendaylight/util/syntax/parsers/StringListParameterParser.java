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
 *  Builtin parser for a list of string values.  
 *
 * @author Thomas Vachuska
 *
 */
public class StringListParameterParser extends ListParameterParser {

    private static final long serialVersionUID = 4850850801044044148L;

    /** 
     * Default constructor.
     */
    public StringListParameterParser() {
    }

    /**
     * Constructs a new string parameter parser using the given locale.
     *
     * @param locale Locale to associate with this parser.
     */
    public StringListParameterParser(Locale locale) {
        super(locale);
    }
    
    /**
     * @see TypedParser#getTypeToken
     */
    @Override
    public String getTypeToken() {
        return "strings";
    }
    
    /**
     * Returns an object describing particular constraints as defined in the
     * specified property database.
     *
     * @see ConstraintsParser#parse
     *
     * @return {@link StringListConstraints} instance created using the
     * specified properties.
     */
    @Override
    public Constraints parse(Properties db, TokenTranslator tr,
                             ParserLoader parserLoader) {
        String min = tr.translate(db.getProperty("minValue"));
        String max = tr.translate(db.getProperty("maxValue"));
        String legalValues = tr.translate(db.getProperty("legalValues"));
        String illegalValues = tr.translate(db.getProperty("illegalValues"));
        String legalERE = tr.translate(db.getProperty("legalERE"));
        String illegalERE = tr.translate(db.getProperty("illegalERE"));
        String separator = tr.translate(db.getProperty("separator"));
        String minLength = tr.translate(db.getProperty("minLength"));
        String maxLength = tr.translate(db.getProperty("maxLength"));

        //  Create the constraint only when we need to.
        if (min != null || max != null || 
                legalValues != null || illegalValues != null ||
                legalERE != null || illegalERE != null ||
                minLength != null || maxLength != null) {
            Integer minI = minLength != null ? new Integer(minLength) : null;
            Integer maxI = maxLength != null ? new Integer(maxLength) : null;
            return new StringListConstraints(min, max, 
                                             legalValues, illegalValues,
                                             legalERE, illegalERE,
                                             separator, minI, maxI);
        }
        return null;
    }
    
    /** 
     * Returns the parsed object corresponding to the given list item, which
     * in this case is the token itself.
     * 
     * @see ListParameterParser#parseItem
     *
     * @return The specified token instance.
     */
    @Override
    public Serializable parseItem(String token, Constraints constraints,
                                  Parameters soFar) {
        return token;
    }

}
