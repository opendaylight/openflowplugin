/*
 * (c) Copyright 2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.format;

/**
 * Formats message strings by translating tokens by their replacement values
 * within the given message string. Token variables can be identified by the
 * following formats:
 * 
 * <pre>
 *         %foo
 *         %{foo}
 * </pre>
 * 
 * The % character and the {} enclosing characters can be tailored during
 * construction or by calling one of the appropriate set methods.
 * 
 * @author Thomas Vachuska
 */
public class SimpleTokenMessageTranslator implements TokenMessageTranslator {

    /** String constant of all legal alpha-numeric characters.  */
    public static final String ALPHANUMERIC = 
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /** 
     * String consisting of all alphanumeric characters, 
     * ',', '.', '-', and '_' 
     */
    public static final String ALPHANUMERIC_PUNCTUATION = 
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789,.-_:";

    protected String validCharacters = ALPHANUMERIC;


    /** Default token flag value. */
    public static final char DEFAULT_TOKEN_CHAR = '%';

    /** Default (optional) token start delimiter.  */
    public static final char DEFAULT_TOKEN_START = '{';

    /** Default (optional) token end delimiter.  */
    public static final char DEFAULT_TOKEN_END = '}';

    /** Token flag used by this translator.  */
    private char tokenChar = DEFAULT_TOKEN_CHAR;

    /** Optional token start delimiter used by this translator.  */
    private char tokenStartChar = DEFAULT_TOKEN_START;

    /** Optional token end delimiter used by this translator.  */
    private char tokenEndChar = DEFAULT_TOKEN_END;


    /** Constructs a new translator setup to recognise tokens using the
        default characters for the flag and start/end character pairs.  */
    public SimpleTokenMessageTranslator () {}

    /**
     * Constructs a new translator setup to recognise tokens using the
     * specified characters for the flag and default start/end character
     * pairs.
     * 
     * @param tokenChar token identification character
     */
    public SimpleTokenMessageTranslator (char tokenChar) {
        this.tokenChar = tokenChar;
    }

    /**
     * Constructs a new translator setup to recognise tokens using the
     * specified characters for the flag and start/end character pairs.
     * 
     * @param tokenChar token identification character
     * @param tokenStartChar start of token character
     * @param tokenEndChar end of token character
     */
    public SimpleTokenMessageTranslator (char tokenChar, 
                                         char tokenStartChar, 
                                         char tokenEndChar) {
        this.tokenChar = tokenChar;
        this.tokenStartChar = tokenStartChar;
        this.tokenEndChar = tokenEndChar;
    }


    /**
     * Returns the token flag used by this translator.
     * 
     * @return token identification character
     */
    public char getTokenChar () { return tokenChar; }

    /**
     * Sets the token flag used by this translator.
     * 
     * @param c token identification character
     */
    public void setTokenChar (char c) { tokenChar = c; }


    /**
     * Returns the optional token start delimiter used by this translator.
     * 
     * @return token start character
     */
    public char getTokenStartChar () { return tokenStartChar; }

    /**
     * Sets the optional token start delimiter used by this translator.
     * 
     * @param c new token start character
     */
    public void setTokenStartChar (char c) { tokenStartChar = c; }


    /**
     * Returns the optional token end delimiter used by this translator.
     * 
     * @return token end character
     */
    public char getTokenEndChar () { return tokenEndChar; }

    /**
     * Sets the optional token end delimiter used by this translator.
     * 
     * @param c new token end character
     */
    public void setTokenEndChar (char c) { tokenEndChar = c; }


    /** Sets the characters allowed in a token string */

    /** Replace all token strings within the specified message using the
        substitution services of the given translator.  */
    @Override
    public String translate (String message, TokenTranslator translator) {
        StringBuffer result = new StringBuffer(message);

        int tokenStart = -1;
        int tokenEnd = -1;
        boolean expectEnd = false;

        //  Scan the message looking for substitution token indicator.
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);

            if (tokenStart > 0) {
                //  If we are searching for an end of token via end-brace,
                //  make sure we get an end-brace indeed.
                if (expectEnd && c != tokenEndChar)
                    continue;

                //  If we are searching for an end of token right now, look
                //  for end of the string or for a non-alphanumeric character
                //  to terminate our token.
                boolean isLastChar = i == result.length() - 1;
                boolean isAlphanumeric = validCharacters.indexOf (c) >= 0;
                if ((!isAlphanumeric && i > tokenStart) || isLastChar) {
                    tokenEnd = isAlphanumeric ? i + 1 : i;

                    String token = expectEnd ? 
                        result.substring (tokenStart + 1, tokenEnd) :
                        result.substring (tokenStart, tokenEnd);

                    String value = translator.translate (token);
                    if (value != null) {
                        result.replace (tokenStart - 1,
                                        tokenEnd + (expectEnd ? 1 : 0), value);

                        //  If we are on a last character now, let's not mess
                        //  around with the indeces, etc. and just move on.
                        if (isLastChar) {
                            i = result.length();
                            continue;
                        } // if

                        i = i + (value.length() - token.length() -
                                 (expectEnd ? 2 : 1));

                        //  Reset the current character and other
                        //  state variables.
                        c = result.charAt(i);
                    } // if

                    tokenStart = -1;
                    expectEnd = false;

                } else if (!isAlphanumeric && i == tokenStart && 
                           c != tokenChar) {
                    //  If this is an open brace immediately following a token
                    //  indicator, maintain the original token start;
                    //  otherwise reset the token start to none, i.e. -1.
                    expectEnd = c == tokenStartChar;
                    tokenStart = expectEnd ? tokenStart : -1;
                } // if
            } // if

            //  Is this a start of a new token? Make sure we skip doubled-up
            //  indicator, i.e. %%, etc.
            if (c == tokenChar && i > tokenStart)
                tokenStart = i + 1;
            else if (c == tokenChar) {
                result.deleteCharAt (i);
                tokenStart = -1;
            } // if
        } // for

        return new String (result);
    } // translator

} // SimpleTokenMessageTranslator
