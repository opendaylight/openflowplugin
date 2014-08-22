/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Class representing a set of alternate string values.
 * 
 * @author Thomas Vachuska
 */
public class StringSet implements Serializable {

    private static final long serialVersionUID = -8133723757270531411L;

    /** Default value delimiter is the pipe (|) character. */
    public static final String VALUE_DELIMITER = "|";

    /** Default abbreviation delimiter is the asterisk (*) character. */
    public static final String ABBREV_DELIMITER = "*";

    /** Backing store to hold all alternate string values. */
    private TreeSet<String> set = new TreeSet<String>();

    /** Name used for display purposes; first added string. */
    private String name = null;

    /**
     * Default constructor, creates a set with no initial values.
     */
    public StringSet() {
    }

    /**
     * Create a set with strings using the default value and abbreviation
     * delimiters.
     * 
     * @param tokens A single string containing a series of tokens separated
     *        from each other via the default delimiters
     */
    public StringSet(String tokens) {
        addTokens(tokens, VALUE_DELIMITER, ABBREV_DELIMITER);
    }

    /**
     * Creates a set with the strings on the specified array as the initial
     * set of alternate values.
     * 
     * @param strings Array of individual strings to add to the string set
     *        directly, i.e. without any parsing and processing of
     *        abbreviations.
     */
    public StringSet(String strings[]) {
        for (int i = 0; i < strings.length; i++)
            this.add(strings[i]);
    }

    /**
     * Creates a set using the tokens parsed from the given token string using
     * the supplied delimiter. No checking for abbreviations is performed.
     * 
     * @param tokens A single string containing a series of tokens separated
     *        from each other via the specified delimiter.
     * @param delimiter A single character string to use as a token delimiter.
     */
    public StringSet(String tokens, String delimiter) {
        addTokens(tokens, delimiter);
    }

    /**
     * Creates a set using the tokens parsed from the given token string using
     * the supplied delimiter. Any tokens that contain the minimum
     * abbreviation delimiter will be split into all possible completion
     * values and will be added to the set also.
     * 
     * @param tokens A single string containing a series of tokens separated
     *        from each other via the specified delimiter.
     * @param delimiter A single character string to use as a token delimiter.
     * @param abbreviationDelimiter A single character string to use as an
     *        abbreviation delimiter.
     */
    public StringSet(String tokens, String delimiter,
                     String abbreviationDelimiter) {
        addTokens(tokens, delimiter, abbreviationDelimiter);
    }

    /**
     * Returns the display name of this string set.
     * 
     * @return string set display name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this string set.
     * 
     * @param name name of the string set
     */
    private void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a read-only set of values of this string set.
     * 
     * @return set of possible string values
     */
    public Set<String> getValues() {
        return Collections.unmodifiableSet(set);
    }

    /**
     * Adds the given string to the set. Also, if the set name isn't already
     * set to a non-null value, set it to this value.
     * 
     * @param string A string to add to the set, as-is, without any
     *        processing.
     * @return true if the string was added to the set; false it it was
     *         already in the set
     */
    public boolean add(String string) {
        return add(string, true);
    }

    /**
     * Adds the given string to the set, setting the name to the string value
     * if requested.
     * 
     * @param string the string to be added to this set.
     * @param checkName true to set the string set name to the given string
     * @return true if the string was added to the set; false it it was
     *         already in the set
     */
    public boolean add(String string, boolean checkName) {
        if (checkName && (getName() == null))
            setName(string);
        return set.add(string);
    }

    /**
     * Removes the given string from the set.
     * 
     * @param string the string to be removed from this set
     * @return true if the removal succeeded, false otherwise
     */
    public boolean remove(String string) {
        return set.remove(string);
    }

    /**
     * Checks if the string is contained in the set.
     * 
     * @param string string to be tested for membership in the set
     * @return true if the given string is contained in the set; false
     *         otherwise.
     */
    public boolean contains(String string) {
        return set.contains(string);
    }

    /**
     * Checks, using a case-insensitive method, if the string is contained in
     * the set.
     * 
     * @param string string to be tested for membership in the set
     * @return true if the given string is contained in the set; false
     *         otherwise
     */
    public boolean containsIgnoreCase(String string) {
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String s = it.next();
            if (s.equalsIgnoreCase(string))
                return true;
        }
        return false;
    }

    /**
     * Adds to the set a set the tokens parsed from the given token string
     * using the supplied delimiter.
     * 
     * @param tokens A single string containing a series of tokens separated
     *        from each other via the specified delimiter.
     * @param delimiter A single character string to use as a token delimiter.
     */
    public void addTokens(String tokens, String delimiter) {
        StringTokenizer st = new StringTokenizer(tokens, delimiter);
        while (st.hasMoreTokens())
            add(st.nextToken());
    }

    /**
     * Adds to set the tokens parsed from the given token string using the
     * supplied delimiter. Any tokens that contain the minimum abbreviation
     * delimiter will be split into all possible completion values and will be
     * added to the set also.
     * 
     * @param tokens A single string containing a series of tokens separated
     *        from each other via the specified delimiter.
     * @param delimiter A single character string to use as a token delimiter.
     * @param abbreviationDelimiter A single character string to use as an
     *        abbreviation delimiter.
     */
    public void addTokens(String tokens, String delimiter,
                          String abbreviationDelimiter) {
        String name = "";
        String separator = "";
        StringTokenizer st = new StringTokenizer(tokens, delimiter);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            name += (separator + token);
            separator = VALUE_DELIMITER;
            int ai = token.indexOf(abbreviationDelimiter);
            if (ai > 0) {
                String base = token.substring(0, ai);
                int length = token.length();
                for (int i = (++ai); i <= length; i++)
                    add(base + token.substring(ai, i), false);
            } else {
                add(token, false);
            }
        }
        if (getName() == null)
            setName(name);
    }

    /**
     * Returns the string image containing all alternate string values.
     */
    @Override
    public String toString() {
        StringBuffer image = new StringBuffer(VALUE_DELIMITER);
        synchronized (set) {
            Iterator<String> it = set.iterator();
            while (it.hasNext())
                image.append(it.next()).append(VALUE_DELIMITER);
        }
        return image.toString();
    }
}
