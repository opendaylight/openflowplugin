/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Utility for tokenizing a string along symbol boundaries. The symbols
 * themselves are returned as part of the token sequence and only white-space
 * is eliminated.
 * 
 * @author Thomas Vachuska
 */
public class SymbolTokenizer implements Iterator<String> {

    // String to be parsed
    private String string = null;

    // Symbols, their pre and post delimiters
    private String[] symbols;
    private String[] preDelimiters;
    private String[] postDelimiters;

    // Current parse position
    private ParsePosition position;

    // Next, i.e. future symbol found and the next parsing position to be 
    // applied once the symbol is fetched.
    private String nextSymbol;
    private int nextStart;
    
    // Keep track of whether the last token was a symbol or not. 
    private boolean wasSymbol = false;

    
    /**
     * Create a new symbol tokenizer using the specified symbols and any valid
     * delimiters.
     * 
     * @param symbols array of recognized symbol strings
     * @param preDelimiters string of characters that may precede each
     *        respective symbol; elements may be null if symbol has no special
     *        constraint
     * @param postDelimiters string of characters that may follow each
     *        respective symbol; elements may be null if symbol has no special
     *        constraint
     */
    public SymbolTokenizer(String[] symbols, String[] preDelimiters,
                           String[] postDelimiters) {
        this.symbols = Arrays.copyOf(symbols, symbols.length);
        this.preDelimiters = Arrays.copyOf(preDelimiters, preDelimiters.length);
        this.postDelimiters = Arrays.copyOf(postDelimiters, postDelimiters.length);
        this.position = new ParsePosition(0);
    }

    /**
     * Create a new symbol tokenizer using the specified symbols and any valid
     * delimiters and ready to tokenize the given string.
     * 
     * @param string string to be parsed
     * @param symbols array of recognized symbol strings
     * @param preDelimiters string of characters that may precede each
     *        respective symbol; elements may be null if symbol has no special
     *        constraint
     * @param postDelimiters string of characters that may follow each
     *        respective symbol; elements may be null if symbol has no special
     *        constraint
     */
    public SymbolTokenizer(String string, String[] symbols,
                           String[] preDelimiters, String[] postDelimiters) {
        this(symbols, preDelimiters, postDelimiters);
        setString(string);
    }
    
    /**
     * Get the string being parsed.
     * 
     * @return string being parsed by this tokenizer
     */
    public String string() {
        return string;
    }

    /**
     * Apply a new string to be parsed by this tokenizer and reset the
     * tokenizer.
     * 
     * @param string string to be parsed
     */
    public void setString(String string) {
        this.string = string;
        reset();
    }
    
    /**
     * Get the current parse position.
     * 
     * @return position within the string where the tokenizer will start
     *         parsing next
     */
    public int position() {
        return position.getIndex();
    }

    /**
     * Resets the tokenizer to the parse from the beginning of the current
     * string.
     */
    public void reset() {
        position.setIndex(0);
        nextSymbol = null;
        nextStart = -1;
    }

    /**
     * Determines whether there are more tokens, symbols or otherwise,
     * remaining to be parsed in the string.
     * 
     * @return true if there are more tokens, false otherwise
     */
    @Override
    public boolean hasNext() {
        if (string == null)
            return false;
        skipWhiteSpace();
        return string.length() - position.getIndex() > 0;
    }

    
    /**
     * Get the next token, symbol or otherwise.
     * 
     * @return next token; null if already at the end of the string
     */
    @Override
    public String next() {
        // If there's no string, there's no token
        if (string == null)
            return null;

        // If there's a symbol queued up, just adjust the parse position and
        // return the symbol.
        if (nextStart >= 0) {
            position.setIndex(nextStart);
            nextStart = -1;
            wasSymbol = true;
            return nextSymbol;
        }
        
        // Move past any intervening white space first.
        skipWhiteSpace();

        // Otherwise, start searching from the current parse position for the
        // next special symbol. Assume that the next token will not be a symbol
        int length = string.length();
        int start = position.getIndex();
        wasSymbol = false;
        
        // Advance through the string one character at a time
        for (int cp = start; cp < length; cp++) {
            // At each stop, attempt to match the current position against
            // each of the symbols
            for (int i = 0; i < symbols.length; i++)
                if (isSymbol(symbols[i], preDelimiters[i], postDelimiters[i], cp)) {
                    nextStart = cp + symbols[i].length();

                    // If the symbol matched from the starting position,
                    // which means there is no free-form argument token that
                    // is preceding it, we'll just return the matching symbol
                    if (cp == start) {
                        // Before spitting out the symbol, adjust the current
                        // parse position and nix the last character
                        position.setIndex(nextStart);
                        nextStart = -1;
                        wasSymbol = true;
                        return symbols[i];
                    }

                    // Otherwise, if there is a free-form argument token
                    // before the matching symbol, then remember the symbol,
                    // and spit out the argument token.
                    nextSymbol = symbols[i];
                    position.setIndex(cp);
                    return string.substring(start, cp).trim();
                }
        }

        // If we did not find any symbol between the starting position and
        // the end of the string, assume the rest is a free-form argument
        // token
        position.setIndex(length);
        return start < length ? string.substring(start).trim() : null;
    }
    
    /**
     * Indicates whether the last token returned via next() was a special
     * symbol or not.
     * 
     * @return true if the token was a symbol; false otherwise
     */
    public boolean wasSymbol() {
        return wasSymbol; 
    }

    /**
     * Skips past any white space and updates the current parse position.
     */
    private void skipWhiteSpace() {
        int l = string.length();
        int i = position.getIndex();
        while (i < l && (string.charAt(i) == ' ' || string.charAt(i) == '\t'))
            i++;
        position.setIndex(i);
    }
    

    /**
     * Determine whether the specified symbol matches the string at the
     * specified position and that it is surrounded by expected delimiters.
     * 
     * @param symbol symbol to match
     * @param pre string of expected prefix delimiters
     * @param post string of expected postfix delimiters
     * @param at position in the string where to perform the match
     * @return true if the symbol matches and is properly delimited; false
     *         otherwise
     */
    private boolean isSymbol(String symbol, String pre, String post, int at) {
        int sl = symbol.length();

        // First test if the symbol matches in the first place
        boolean ok = string.regionMatches(at, symbol, 0, sl);
        if (!ok)
            return false;

        // Now make sure that the symbol is properly surrounded/isolated
        char last = at > 0 ? string.charAt(at - 1) : '\0';
        ok = ok && (last == '\0' || pre == null || pre.indexOf(last) >= 0);

        char next = at + sl < string.length() ? string.charAt(at + sl) : '\0';
        ok = ok && (next == '\0' || post == null || post.indexOf(next) >= 0);
        return ok;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }

}
