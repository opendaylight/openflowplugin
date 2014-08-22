/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.util.format;

import java.util.*;
import java.text.*;

/**
 * This is an implementation of the TokenTranslator interface that reads
 * tokens from a specified ResourceBundle. It can optionally use the
 * MessageFormat class to substitute values at arbitrary positions in the
 * translated String.
 *
 * @author Scott Pontillo
 */
public class ResourceBundleTokenTranslator extends InvalidTokenTranslator {

    private ResourceBundle rb = null;
    private boolean useMessageFormat = false;

    public static final char DEFAULT_KEY_SEPARATOR = ':';
    public static final char DEFAULT_VALUE_SEPARATOR = ',';

    private String keySeparator = DEFAULT_KEY_SEPARATOR + "";
    private String valueSeparator = DEFAULT_VALUE_SEPARATOR + "";

    public ResourceBundleTokenTranslator(ResourceBundle rb) {
        this.rb = rb;
    }

    public ResourceBundleTokenTranslator(ResourceBundle rb, int behaviour) {
        super (behaviour);
        this.rb = rb;
    }

    public ResourceBundleTokenTranslator(ResourceBundle rb, 
                                         boolean useMessageFormat) {
        this.rb = rb;
        setUseMessageFormat(useMessageFormat);
    }

    public ResourceBundleTokenTranslator(ResourceBundle rb, 
                                         boolean useMessageFormat,
                                         int behaviour) {
        super (behaviour);
        this.rb = rb;
        setUseMessageFormat(useMessageFormat);
    }

    /**
     * This method must be called if the MessageFormat functionality is
     * to be used.
     * @param tf true to use message format; false otherwise
     */
    public void setUseMessageFormat(boolean tf) {
        this.useMessageFormat = tf;
    }

    /**
     * Returns true if this ResourceBundleTokenTranslator is using
     * MesasgeFormat functionality; false otherwise.
     * 
     * @return true if the message format is to be used; false otherwise
     */
    public boolean getUseMessageFormat() {
        return useMessageFormat;
    }

    /**
     * Sets the character that is used to separate values. (Only used when
     * multiple values exist) Defaults to ','.
     * @param s new value separator
     */
    public void setValueSeparator(String s) {
        valueSeparator = s;
    }

    /**
     * Sets the character that is used to separate the key from the values.
     * Defaults to ':'.
     * @param s new key separator
     */
    public void setKeySeparator(String s) {
        keySeparator = s;
    }

    /**
     * Translates the given string based on the format:
     *
     * <key>[:<values>]
     *
     * Where <values> is a comma separated list of value strings. 
     *
     * Value strings will be passed into the MessageFormat class if 
     * setUseMessageFormat(true) has been called. Otherwise,
     * the string will not be parsed.
     */
    @Override
    public String translate(String token) {
        String name = null;

        try {
            if (token == null)
                return translateInvalidToken(null);

            if (useMessageFormat) {
                // Parse this token
                int separatorIndex = token.indexOf(keySeparator);
                
                if (separatorIndex == -1)
                    return rb.getString(token);

                String key = token.substring(0, separatorIndex);
                String values = token.substring(separatorIndex+1, token.length());
                
                name = rb.getString(key);
                    
                StringTokenizer stok = new StringTokenizer(values, valueSeparator);
                Object[] strings = new Object[stok.countTokens()];
                
                // Can use the MessageFormat string to determine
                // the types of each argument. 
                int i = 0;
                while (stok.hasMoreTokens())
                    strings[i++] = stok.nextToken();
                
                return MessageFormat.format(name, strings);
            }
            
            // Not using MessageFormat
            return rb.getString(token);
            
        } catch (RuntimeException rex) {
            return translateInvalidToken(token);
        }
    }

}
