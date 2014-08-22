/*
 * (c) Copyright 2008 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Utility class to provide a concise way to parse out string tokens and to
 * allow repeated (infinite) parsing of the same string pattern.
 * 
 * @author Thomas Vachuska
 */
public class Tokenizer {
    
    protected StringTokenizer st;
    protected String string;
    protected String delimiters;
    protected boolean circular = false;

    /**
     * Create a new tokenizer on the string and using the given delimiter
     * characters.
     * 
     * @param string string to be tokenized
     * @param delimiters string of delimiters 
     */
    public Tokenizer(String string, String delimiters) {
        this.string = string;
        this.delimiters = delimiters;
        reset();
    }

    /**
     * Create a new tokenizer on the string and using the given delimiter
     * characters.
     * 
     * @param string string to be tokenized
     * @param delimiters string of delimiters
     * @param circular true if the tokenizer is to be a circular one
     */
    public Tokenizer(String string, String delimiters, boolean circular) {
        this(string, delimiters);
        this.circular = circular;
    }

    /**
     * Reset the tokenizer to point to the beginning of the original string.
     */
    public void reset() {
        st = new StringTokenizer(string, delimiters);
    }
    
    /**
     * Determine if there are any more tokens to be parsed.
     * 
     * @return true if more tokens are available; false otherwise; always
     *         returns true for circular tokenizer
     */
    public boolean hasNext() {
        return circular || st.hasMoreTokens();
    }

    /**
     * Get the next token string.
     * 
     * @return next token string
     */
    public String next() {
        try {
            return st.nextToken();
        } catch (NoSuchElementException e) {
            if (!circular)
                throw e;
            reset();
            return st.nextToken();
        }
    }
    
    /**
     * Get the next token as an integer.
     * 
     * @return next token parsed into an integer value
     */
    public int nextInt() {
        return Integer.parseInt(next());
    }
    
    /**
     * Get the next token as a long.
     * 
     * @return next token parsed into a long value
     */
    public long nextLong() {
        return Long.parseLong(next());
    }
    
    /**
     * Returns true if the tokenizer is marked as a circular one.
     * 
     * @return true if tokenizer is circular; false otherwise
     */
    public boolean circular() {
        return circular;
    }
    
    /**
     * Get the underlying string used to construct the tokenizer.
     * 
     * @return string being parsed into tokens
     */
    public String string() {
        return string;
    }
    
    /**
     * Get the delimiters string.
     * 
     * @return the delimiters string
     */
    public String delimiters() {
        return delimiters;
    }
    
}
