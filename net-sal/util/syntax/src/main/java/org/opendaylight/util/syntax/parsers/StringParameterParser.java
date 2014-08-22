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
 * Builtin parser for string values.  
 *
 * @author Thomas Vachuska
*/
public class StringParameterParser extends AbstractParameterParser {

    private static final long serialVersionUID = -8857405593276504731L;

    /** 
     * Default constructor.
     */
    public StringParameterParser() {
    }

    /**
     * Constructs a new string parameter parser using the given locale.
     *
     * @param locale Locale to associate with this parser.
     */
    public StringParameterParser(Locale locale) { 
        super(locale);
    }

    /**
     * @see TypedParser#getTypeToken
     */
    @Override
    public String getTypeToken() { 
        return "string";
    }
    
    /**
     * Creates an instance of {@link StringConstraints}.
     *
     * @see ConstraintsParser#parse
     *
     * @return {@link StringConstraints} instance created using the
     * specified properties.
     */
    @Override
    public Constraints parse(Properties db, TokenTranslator tr,
                             ParserLoader parserLoader) {
        String legalValues = tr.translate(db.getProperty("legalValues"));
        String illegalValues = tr.translate(db.getProperty("illegalValues"));
        String legalERE = tr.translate(db.getProperty("legalERE"));
        String illegalERE = tr.translate(db.getProperty("illegalERE"));
        String min = tr.translate(db.getProperty("minValue"));
        String max = tr.translate(db.getProperty("maxValue"));

        //  Create the constraint only when we need to.
        if (min != null || max != null || 
                legalValues != null || illegalValues != null ||
                legalERE != null || illegalERE != null)
            return new StringConstraints(min, max, legalValues, illegalValues,
                                         legalERE, illegalERE);
        return null;
    }
    
    /** 
     * Returns a non-null object if the parsing of the given token was
     * succesful and if the object value complied with the specified
     * constraints, if any were specified.
     *
     * @see ParameterParser#parse
     *
     * @return A validated {@link String} instance or null if the
     * resulting String value does not meet the constraints, if any.
    */
    @Override
    public Serializable parse(String token, Constraints constraints,
                              Parameters soFar) {
        if (constraints == null || constraints.isValid(token))
            return token;
        return null;
    }

}
