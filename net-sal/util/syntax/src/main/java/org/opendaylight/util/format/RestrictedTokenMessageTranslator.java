/*
 * (c) Copyright 2002-2003 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.format;

/**
    Formats message strings by translating tokens by their replacement values
    within the given message string.  Token variables can be identified by
    the following format:<pre>
        %{foo}
    </pre>

    Note that this class is only "Restricted" in that it is stricter about
    the format of the tokens. More characters are actually allowed in the
    token itself.

    The % character and the {} enclosing characters can be tailored during
    construction or by calling one of the appropriate set methods.

    @author Thomas Vachuska
    @author Scott Pontillo
*/
public class RestrictedTokenMessageTranslator 
       extends SimpleTokenMessageTranslator {

    /** Constructs a new translator setup to recognize tokens using the
        default characters for the flag and start/end character pairs.  */
    public RestrictedTokenMessageTranslator () {
        super();
    }

    /** Constructs a new translator setup to recognize tokens using the
        specified characters for the flag and default start/end character
        pairs.  
     * @param tokenChar token identification character */
    public RestrictedTokenMessageTranslator (char tokenChar) {
        super(tokenChar);
    }

    /**
     * Constructs a new translator setup to recognize tokens using the
     * specified characters for the flag and start/end character pairs.
     * 
     * @param tokenChar token identification character
     * @param tokenStartChar token start character
     * @param tokenEndChar token end character
     */
    public RestrictedTokenMessageTranslator (char tokenChar, 
                                         char tokenStartChar, 
                                         char tokenEndChar) {
        super(tokenChar, tokenStartChar, tokenEndChar);
    }

    private static final int SCAN_POSSIBLE_TOKEN = 0;
    private static final int FIND_TOKEN_START = 1;
    private static final int SCAN_TOKEN_END = 2;


    @Override
    public String translate(String message, TokenTranslator translator) {
        StringBuffer result = new StringBuffer(message);

        int tokenStart = -1;
        int tokenEnd = -1;

        // Possible states:
        //   0: Scanning for %
        //   1: Check for {
        //   2: Checking for validity and looking for }
        int state = SCAN_POSSIBLE_TOKEN;

        //  Scan the message looking for substitution token indicator.
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);

            if (state == SCAN_POSSIBLE_TOKEN) {
                if (c == getTokenChar()) {
                    state = FIND_TOKEN_START;
                }
            } else if (state == FIND_TOKEN_START) {
                if (c == getTokenStartChar()) {
                    state = SCAN_TOKEN_END;
                    tokenStart = i+1;
                } else {
                    // Couldn't find token; start over
                    state = SCAN_POSSIBLE_TOKEN;
                }
            } else if (state == SCAN_TOKEN_END) {
                boolean isValid = isValidCharacter(c);
                if (!isValid) {
                    tokenEnd = i;
                    if (c == getTokenEndChar()) {
                        if (tokenStart - tokenEnd == 0) {
                            // Zero length token
                            state = SCAN_POSSIBLE_TOKEN;
                        } else {
                            // Valid token, so replace it
                            String token = 
                                result.substring(tokenStart, tokenEnd);

                            String value = translator.translate (token);

                            if (value != null) {
                                result.replace(tokenStart - 2,
                                               tokenEnd + 1, value);

                                i = i + value.length() - token.length() - 3;
                            }
                        }
                    }
                    // Invalid character within token string
                    state = SCAN_POSSIBLE_TOKEN;
                }
            } 
        }

        return new String (result);
    }

    /**
     * Returns true for any character except a control character or a right
     * curly brace. These are all characters that are valid in a "restricted"
     * token.
     * 
     * @param c character to be validated
     * @return true if the character is a valid token character
     */
    private boolean isValidCharacter(char c) {
        // Disallow control characters
        if(c < 32) return false;
        // Disallow right curly brace (indicates end of a token)
        if(c == '}') return false;
        return true;
    }

}
