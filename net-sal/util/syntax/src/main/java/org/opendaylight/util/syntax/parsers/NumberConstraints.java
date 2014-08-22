/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.util.Tokenizer;

/** 
 * Builtin constraint validator for a number value.
 *
 * @author Thomas Vachuska
 */
public class NumberConstraints implements Constraints {

    /** 
     * Default format to use for number parsing.
     */
    public static final NumberFormat DEFAULT_FORMAT = 
        NumberFormat.getInstance(Locale.US);
   
        
    protected NumberFormat format = DEFAULT_FORMAT;
    protected Number min = null;
    protected Number max = null;
    
    protected Pattern uPattern;
    protected StringSet[] uNames;
    protected Number[] uValues;
        
    /** 
     * Default constructor.
     */
    public NumberConstraints() {
    }
        
    /**
     * Creates a number constraints enforcement entity.
     * 
     * @param locale context locale for validation
     * @param min If not null, it specifies the minimum numerical value the
     *        parsed argument can have to be a valid parameter.
     * @param max If not null, it specifies the maximum numerical value the
     *        parsed argument can have to be a valid parameter.
     * @param allowDecimals Boolean indicating whether decimal values are
     *        allowed.
     * @param useGrouping Boolean indicating whether grouping separator is
     * @param unitPattern regexp pattern for finding the start of units;
     *        defaults to [:alpha:] character set allowed.
     * @param unitNames comma-separated list of string-sets
     * @param unitValues comma-separated list of unit multiplier values
     */
    public NumberConstraints(Locale locale, String min, String max,
                             boolean allowDecimals, boolean useGrouping,
                             String unitPattern, String unitNames,
                             String unitValues) {
        format = NumberFormat.getInstance(locale);
        format.setParseIntegerOnly(!allowDecimals);
        format.setGroupingUsed(useGrouping);
        this.min = Utilities.parse(format, min);
        this.max = Utilities.parse(format, max);
        
        if (unitNames != null && unitNames.length() > 0) {
            List<StringSet> list = new ArrayList<StringSet>();
            Tokenizer t = new Tokenizer(unitNames, ",");
            while (t.hasNext())
                list.add(new StringSet(t.next()));
            uNames = list.toArray(new StringSet[list.size()]);
        }

        if (unitValues != null && unitValues.length() > 0) {
            List<Number> list = new ArrayList<Number>();
            Tokenizer t = new Tokenizer(unitValues, ",");
            while (t.hasNext())
                list.add(Utilities.parse(format, t.next()));
            uValues = list.toArray(new Number[list.size()]);
        }

        if ((uNames == null && uValues != null) || (uNames != null && uValues == null))
            throw new IllegalArgumentException("Unit name and unit value lists have to both be specified");
        if (uNames != null && uValues != null && uNames.length != uValues.length)
            throw new IllegalArgumentException("Unit name and unit value lists don't have equal length");
        
        if (uNames != null) {
            // Provide a default unit search pattern, unless one has been given explicitly
            String regex = unitPattern != null ? unitPattern : "[a-zA-Z]";
            uPattern = Pattern.compile(".+?(" + regex + ".*)$");
        }
    }

    /** 
     * Get the number format associated with this constraint.
     *
     * @return Locale specific {@link java.text.NumberFormat} instance.
     */
    public NumberFormat getFormat() { 
        return format; 
    }
    
    /**
     * Get the post-fix unit specifier portion of the given token
     * 
     * @param token string from which the post-fix unit specifier should be
     *        extracted
     * @return post-fix unit specifier
     */
    public String getUnitName(String token) {
        if (uPattern == null)
            return null;
        Matcher m = uPattern.matcher(token);
        return m.matches() ? m.group(1) : null;
    }
    
    /**
     * Get the number value adjusted with the corresponding factor of the
     * given unit specifier. Returned number will be a double if either the
     * factor or the given number are float or a double themselves; otherwise
     * the result will be a long.
     * 
     * @param n base number value to be adjusted
     * @param unit unit specifier whose corresponding factor should be used to
     *        adjust the number value
     * @return adjusted number; double or a long
     */
    public Number getUnitBasedValue(Number n, String unit) {
        for (int i = 0; i < uNames.length; i++) {
            if (uNames[i].contains(unit)) {
                if (n instanceof Double || uValues[i] instanceof Double
                        || n instanceof Float || uValues[i] instanceof Float)
                    return uValues[i].doubleValue() * n.doubleValue();
                return uValues[i].longValue() * n.longValue();
            }
        }
        return n;
    }
    

    @Override
    public boolean isValid(Serializable o) {
        if (o != null && o instanceof Number) {
            Number n = (Number) o;
            return ((n instanceof Long && isValidLong (n)) ||
                    (n instanceof Integer && isValidInteger (n)) ||
                    (n instanceof Byte && isValidByte (n)) ||
                    (n instanceof Short && isValidShort (n)) ||
                    (n instanceof Double && isValidDouble (n)) ||
                    (n instanceof Float && isValidFloat (n)));
        }
        return false;
    }

    private boolean isValidInteger(Number n) {
        return ((min == null || min.intValue() <= n.intValue()) &&
                (max == null || max.intValue() >= n.intValue())); 
    }

    private boolean isValidLong(Number n) {
        return ((min == null || min.longValue() <= n.longValue()) &&
                (max == null || max.longValue() >= n.longValue())); 
    }

    private boolean isValidByte(Number n) {
        return ((min == null || min.byteValue() <= n.byteValue()) &&
                (max == null || max.byteValue() >= n.byteValue())); 
    }

    private boolean isValidShort(Number n) {
        return ((min == null || min.shortValue() <= n.shortValue()) &&
                (max == null || max.shortValue() >= n.shortValue())); 
    }

    private boolean isValidDouble(Number n) {
        return ((min == null || min.doubleValue() <= n.doubleValue()) &&
                (max == null || max.doubleValue() >= n.doubleValue())); 
    }

    private boolean isValidFloat(Number n) {
        return ((min == null || min.floatValue() <= n.floatValue()) &&
                (max == null || max.floatValue() >= n.floatValue())); 
    }

}
