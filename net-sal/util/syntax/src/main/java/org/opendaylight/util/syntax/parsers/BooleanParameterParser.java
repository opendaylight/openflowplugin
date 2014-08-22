/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
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
 * Parameter parser for a Boolean value. Parses a command line string that
 * corresponds to a boolean value.
 *
 * @author Terry Robison
 * @author Thomas Vachuska
 */
public class BooleanParameterParser extends AbstractParameterParser {

    private static final long serialVersionUID = -1751595824519296117L;

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    /** 
     * Default constructor.
     */
    public BooleanParameterParser() {
    }

    /**
     * Constructs a new boolean parameter parser using the given locale.
     *
     * @param locale Locale to associate with this parser.
     */
    public BooleanParameterParser(Locale locale) {
        super(locale);
    }

    /**
     * @see TypedParser#getTypeToken
     */
    @Override
    public String getTypeToken() {
        return "boolean";
    }

    /**
     * Creates as an instance of {@link BooleanConstraints}.
     *
     * @see ConstraintsParser#parse
     *
     * @return {@link BooleanConstraints} instance created using the
     * specified properties.
     */
    @Override
    public Constraints parse(Properties db, TokenTranslator tr,
                             ParserLoader parserLoader) {
        String trueValue = tr.translate(db.getProperty(TRUE));
        String falseValue = tr.translate(db.getProperty(FALSE));
        if (trueValue != null && falseValue != null)
            return new BooleanConstraints(trueValue, falseValue);
        return null;
    }

    /** 
     * Returns a non-null object if the parsing of the given token was
     * succesful and if the object value complied with the specified
     * constraints, if any were specified.
     *
     * @see ParameterParser#parse
     *
     * @return A validated String object or null if the resulting Number value
     * does not meet the constraints, if any.
     */
    @Override
    public Serializable parse(String token, Constraints constraints, 
                              Parameters parameters) {
        if (constraints != null && constraints instanceof BooleanConstraints) {
            BooleanConstraints bc = (BooleanConstraints) constraints;
            if (token.equalsIgnoreCase(bc.getTrueValue()))
                return Boolean.TRUE;
            else if (token.equalsIgnoreCase(bc.getFalseValue()))
                return Boolean.FALSE;
        } else {
            if (token.equalsIgnoreCase(TRUE))
                return Boolean.TRUE;
            else if (token.equalsIgnoreCase(FALSE))
                return Boolean.FALSE;
        }
        return null;
    }
}
