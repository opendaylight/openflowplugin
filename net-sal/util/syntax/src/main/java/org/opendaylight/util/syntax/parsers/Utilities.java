/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Properties;

/** 
 * Builtin utilities to aid parsing activities.  
 *
 * @author Thomas Vachuska
 */
public abstract class Utilities {

    /**
     * Translates the given string value into a boolean.
     *
     * @param value String value that should be one of the following: true,
     * false, on, off, yes and no.
     * @return True if the string value is either true, on or yes.
     */
    public static boolean isOn(String value) {
        return value != null && 
            (value.equals("true") || value.equals("on") || value.equals("yes"));
    }

    /**
     * Parses the given string value into a {@link Number} instance.
     *
     * @param format Number format to use for the parsing.
     * @param string String value that should be parsed.
     * @return {@link Number} that was parsed or null if parsing failed or if
     * the string itself was null.
     */
    public static Number parse(NumberFormat format, String string) {
        if (string == null)
            return null;
        ParsePosition pos = new ParsePosition(0);
        Number number = format.parse(string, pos);
        return (string.length() == pos.getIndex()) ? number : null;
    }

    /**
     * Parses the given string value into a {@link Date} instance.
     *
     * @param format Date format to use for the parsing.
     * @param string String value that should be parsed.
     * @return {@link Date} that was parsed or null if parsing failed or if
     * the string itself was null.
     */
    public static Date parse(DateFormat format, String string) {
        if (string == null)
            return null;
        ParsePosition pos = new ParsePosition(0);
        Date date = format.parse(string, pos);
        return (string.length() == pos.getIndex()) ? date : null;
    }


    /**
     * Extracts the named property from the given set of properties.
     *
     * @param properties Date format to use for the parsing.
     * @param name Name of the property.
     * @param defaultValue Default integer value to return if the named
     * property is not found in the set of properties.
     * @return Integer value for the property or default value if the named
     * property is not found.
     */
    public static int get(Properties properties, String name,
                          int defaultValue) {
        String dv = Integer.toString(defaultValue);
        return Integer.parseInt(properties.getProperty(name, dv));

    }

    /**
     * Extracts the named property from the given set of properties.
     *
     * @param properties Date format to use for the parsing.
     * @param name Name of the property.
     * @param defaultValue Default boolean value to return if the named
     * property is not found in the set of properties.
     * @return Boolean value for the property or default value if the named
     * property is not found.
     */
    public static boolean get(Properties properties, String name, 
                              boolean defaultValue) {
        String dv = Boolean.toString(defaultValue);
        return isOn(properties.getProperty(name, dv));
    }

}
