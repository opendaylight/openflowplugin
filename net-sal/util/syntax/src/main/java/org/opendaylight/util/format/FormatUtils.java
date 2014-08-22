/*
 * Copyright (c) 2003 Hewlett-Packard Company
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.format;

/**
 * Utility methods for classes in the format package.
 *
 * @author Scott Pontillo
 */
public class FormatUtils {

    /**
     * Returns a string in the form '%{key[:arg0[,arg1,...,argn]]}',
     * where n is the length of the 'args' array. This method is to be used
     * with the ResourceBundleTokenTranslator and
     * RestrictedTokenMessageTranslator classes.
     *
     * @see ResourceBundleTokenTranslator
     * @see RestrictedTokenMessageTranslator
     *
     * @param key The key into the resource bundle that will be decoded later
     * by translate()
     * @param args The arguments to use for the MessageFormat
     * @return the translatable string
     */
    public static String constructTokenMessage(String key, String[] args) {
        if (key == null)
            throw new IllegalArgumentException("Key cannot be null!");

        if (args == null || args.length == 0) return "%{" + key + "}";
        
        StringBuffer tmp = new StringBuffer("%{" + key + ":");
        for (int i = 0; i < args.length; i++) {
            tmp.append(convertIllegalCharacters(args[i]));
            if (i != (args.length - 1)) 
                tmp.append(",");
        }

        return tmp.append("}").toString();
    }

    /**
     * Constructs a token suitable for use by a default
     * RestrictedTokenMessageTranslator and a ResourceBundleTokenTranslator.
     * Does not place any arguments into the token.
     *
     * @see #constructTokenMessage
     *
     * @param key to be converted to a token
     * @return the token message string in form of %{key}
     */
    public static String constructTokenMessage(String key) {
        if (key == null) 
            throw new IllegalArgumentException("Key cannot be null!");
        return "%{" + key + "}";
    }

    /**
     * Converts an arbitrary String to be used with a default
     * RestrictedTokenMessageTranslator and a ResourceBundleTokenTranslator.
     * Strips all control characters (characters where 0 <= charValue < 32)
     * and converts them to spaces.
     * @param s string to be converted
     * @return string with control characters converted to spaces
     */
    public static String convertIllegalCharacters(String s) {
        StringBuffer sb = new StringBuffer(s);
        for(int i = 0 ; i < sb.length() ; i++) {
            char c = sb.charAt(i);
            // Do not allow newlines or other control characters; replace with
            // a space.
            if ((c < 32) && (c >= 0)) sb.replace(i, i+1, " ");
            // Replace { } pairs with [ ] so the } is not matched as the end
            // of the token
            if (c == SimpleTokenMessageTranslator.DEFAULT_TOKEN_START) 
                sb.replace(i, i+1, "[");
            if (c == SimpleTokenMessageTranslator.DEFAULT_TOKEN_END) 
                sb.replace(i, i+1, "]");
            // Replace US-ASCII comma with unicode comma so it does not get
            // matched as a delimiter
            if (c == ResourceBundleTokenTranslator.DEFAULT_VALUE_SEPARATOR) 
                sb.replace(i, i+1, "\u201A");

        }
        return sb.toString();
    }

}
