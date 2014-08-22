/*
 * (c) Copyright 2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.format;

/**
 * Abstraction of a translator whose treatment of illegal or invalid tokens is
 * customizable.
 * 
 * @author Thomas Vachuska
 */
public abstract class InvalidTokenTranslator implements TokenTranslator {

    /**
     * Specifies translation behaviour where invalid tokens will be translated
     * to a null value.
     */
    public static final int RETURN_NULL = 0;

    /**
     * Specifies translation behaviour where invalid tokens will be translated
     * to an empty string.
     */
    public static final int RETURN_EMPTY_STRING = 1;

    /**
     * Specifies translation behaviour where invalid tokens will be translated
     * to the token string itself, i.e. will not be translated.
     */
    public static final int RETURN_TOKEN_STRING = 2;

    /** Flag specifying how the translator will deal with illegal tokens. */
    protected int behaviour = RETURN_NULL;

    /** Representation of the empty string; truly empty by default. */
    protected String empty = "";

    /**
     * Constructs a translator that will translate illegal tokens to null
     * values.
     */
    public InvalidTokenTranslator() {
        this.behaviour = RETURN_NULL;
    }

    /**
     * Constructs a translator that will treat illegal tokens according to the
     * given behaviour.
     * 
     * @param behaviour behaviour of dealing with illegal tokens
     */
    public InvalidTokenTranslator(int behaviour) {
        this.behaviour = behaviour;
    }

    /**
     * Returns true if the translator will allow illegal tokens.
     * 
     * @return current behaviour of dealing with illegal tokens
     */
    public int getBehaviour() {
        return behaviour;
    }

    /**
     * Sets the behaviour for treatment of illegal tokens.
     * 
     * @param behaviour behaviour of dealing with illegal tokens
     */
    public void setBehaviour(int behaviour) {
        this.behaviour = behaviour;
    }

    /**
     * Sets the value of the empty string to be used in lieu of the invalid
     * token.
     * 
     * @return 'empty' string value
     */
    public String getEmptyString() {
        return empty;
    }

    /**
     * Sets the value of the empty string to be used in lieu of the invalid
     * token.
     * 
     * @param empty new 'empty' string value
     */
    public void setEmptyString(String empty) {
        this.empty = empty;
    }

    /**
     * Translates the illegal token into its value depending on the currently
     * configured mode of behaviour.
     * 
     * @param token invalid token
     * @return value of the invalid token
     */
    public String translateInvalidToken(String token) {
        if (behaviour == RETURN_NULL)
            return null;
        else if (behaviour == RETURN_EMPTY_STRING)
            return empty;
        else
            return token;
    }

}
