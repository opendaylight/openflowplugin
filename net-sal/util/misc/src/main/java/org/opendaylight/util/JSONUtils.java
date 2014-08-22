/*
 * (c) Copyright 2011-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Set of utilities for producing JSON-encoded data.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 * @author Scott Simes
 */
public class JSONUtils {

    private static final String E_NULL = "Parameter cannot be null";

    private static final String CR = "\r";
    private static final String ESC_QUOTE = "\"";
    private static final String SLASH_ESC_QUOTE = "\\\"";
    private static final String NEW_LINE = "\n";
    private static final String SLASH_NEW_LINE = "\\n";

    private static final String SPACE = " ";
    private static final String TWO_SPACES = "  ";
    private static final String OPEN_BRACE = "{";
    private static final String CLOSE_BRACE = "}";
    private static final String OPEN_BRACKET = "[";
    private static final String CLOSE_BRACKET = "]";
    private static final String COMMA = ",";
    private static final String QUOTE_COLON = "\": ";

    /** Prevent construction. */
    private JSONUtils() { }

    /**
     * Create a string representation of JSON key based on the Enum name.
     *
     * @param e Enum of any type
     * @return JSON key
     */
    public static String toKey(Enum<?> e)  {
        return e.name().toLowerCase();
    }

    /**
     * Create the Enum based on JSON key.
     *
     * @param c Enum type
     * @param key JSON key
     * @return the Enum
     */
    public static <E extends Enum<E>> E fromKey(Class<E> c, String key) {
        return Enum.valueOf(c, key.toUpperCase());
    }

    /**
     * Abstraction of a JSON primitive (string, int, boolean) value.
     */
    public static class Primitive {

        private final String data;
        private boolean inline = false;
        private boolean bareWord = false;

        /**
         * Create a JSON primitive. This private no-args constructor is for the
         * Item and Array subclasses to use.
         */
        private Primitive() {
            // Note: the data field is hidden by the subclasses and never used.
            data = null;
        }

        /**
         * Create a JSON primitive with the specified string value.
         *
         * @param item item string value
         * @throws NullPointerException if the parameter is null
         */
        public Primitive(String item) {
            if (item==null)
                throw new NullPointerException(E_NULL);
            data = item;
        }

        /**
         * Create a JSON primitive with the specified numeric value.
         *
         * @param item item numeric value
         */
        public Primitive(long item) {
            data = Long.toString(item);
        }

        /**
         * Create a JSON primitive with the specified boolean value.
         *
         * @param item the boolean value
         */
        public Primitive(boolean item) {
            data = Boolean.toString(item);
            bareWord = true;
        }

        /**
         * Create a JSON primitive with the specified enum value
         *
         * @param item item numeric value
         * @throws NullPointerException if the parameter is null
         */
        public Primitive(Enum<?> item) {
            if (item==null)
                throw new NullPointerException(E_NULL);
            data = item.toString();
        }

        /**
         * Mark the specified item to be formatted in-line.
         *
         * @return the item
         */
        public Primitive inline() {
            this.inline = true;
            return this;
        }

        /**
         * Indicate whether the item will be formatted in-line.
         *
         * @return true if item will use in-line formatting; false otherwise
         */
        public boolean isInline() {
            return inline;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            format(sb, 0, isInline());
            return sb.toString();
        }

        /**
         * Format the JSON entity to the specified string builder and using
         * the prescribed indent depth.
         *
         * @param sb string builder
         * @param indent number of spaces to indent
         * @param inline boolean latch to trigger in-line formatting from this
         *        point onward
         */
        void format(StringBuilder sb, int indent, boolean inline) {
            String safeData = data.replace(CR, StringUtils.EMPTY)
                                  .replace(NEW_LINE, SLASH_NEW_LINE)
                                  .replace(ESC_QUOTE, SLASH_ESC_QUOTE);

            String wrap = bareWord ? StringUtils.EMPTY : ESC_QUOTE;
            sb.append(wrap).append(safeData).append(wrap);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Primitive primitive = (Primitive) o;
            return !(data != null ? !data.equals(primitive.data)
                                  : primitive.data != null);
        }

        @Override
        public int hashCode() {
            return data != null ? data.hashCode() : 0;
        }
    }

    /**
     * Abstraction of a JSON object.
     */
    public static class Item extends Primitive {

        private final Map<String, Primitive> data = new TreeMap<String, Primitive>();

        @Override
        public Item inline() {
            return (Item) super.inline();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            final Item item = (Item) o;
            return data.equals(item.data);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + data.hashCode();
            return result;
        }

        /**
         * Returns the number of items contained within this item.
         * 
         * @return the number of contained items
         */
        public int size() {
            return data.size();
        }
        
        /**
         * Get the set of keys holding values within this item.
         * 
         * @return set of keys
         */
        public Set<String> keys() {
            return Collections.unmodifiableSet(data.keySet());
        }
        
        /**
         * Get the entity bound to the specified key in this item.
         * 
         * @param key entity key
         * @return entity bound to the given key
         */
        public Primitive get(String key) {
            return data.get(key);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            if (!isInline())
                sb.append(NEW_LINE);
            return sb.toString();
        }

        /**
         * Add a named item property to the object.
         *
         * @param name property name (as an enum constant)
         * @param item item to be added
         * @return the modified object
         * @throws NullPointerException if parameter is null
         */
        public Item add(Enum<?> name, Primitive item) {
            return add(name.toString(), item);
        }

        /**
         * Add a named item property to the object.
         *
         * @param name property name
         * @param item item to be added
         * @return the modified object
         * @throws NullPointerException if parameter is null
         */
        public Item add(String name, Primitive item) {
            if (name==null || item==null)
                throw new NullPointerException(E_NULL);
            data.put(name, item);
            return this;
        }

        /**
         * Add a named item property to the object.
         *
         * @param name property name (as an enum constant)
         * @param item item to be added
         * @return the modified object
         * @throws NullPointerException if parameter is null
         */
        public Item add(Enum<?> name, String item) {
            return add(name.toString(), item);
        }

        /**
         * Add a named item property to the object.
         *
         * @param name property name
         * @param item item to be added
         * @return the modified object
         * @throws NullPointerException if parameter is null
         */
        public Item add(String name, String item) {
            if (name==null || item==null)
                throw new NullPointerException(E_NULL);
            data.put(name, new Primitive(item));
            return this;
        }

        /**
         * Add a named item property to the object.
         *
         * @param name property name (as an enum constant)
         * @param item item to be added
         * @return the modified object
         * @throws NullPointerException if parameter is null
         */
        public Item add(Enum<?> name, long item) {
            return add(name.toString(), item);
        }

        /**
         * Add a named item property to the object.
         *
         * @param name property name
         * @param item item to be added
         * @return the modified object
         * @throws NullPointerException if parameter is null
         */
        public Item add(String name, long item) {
            if (name==null)
                throw new NullPointerException(E_NULL);
            data.put(name, new Primitive(item));
            return this;
        }

        /**
         * Add a named item property to the object.
         *
         * @param name property name (as an enum constant)
         * @param item item to be added
         * @return the modified object
         * @throws NullPointerException if parameter is null
         */
        public Item add(Enum<?> name, boolean item) {
            return add(name.toString(), item);
        }

        /**
         * Add a named item property to the object.
         *
         * @param name property name
         * @param item item to be added
         * @return the modified object
         * @throws NullPointerException if parameter is null
         */
        public Item add(String name, boolean item) {
            if(name == null)
                throw new NullPointerException(E_NULL);
            data.put(name, new Primitive(item));
            return this;
        }

        /**
         * Add a named property to the object.
         *
         * @param name property name (as an enum constant)
         * @param item item to be added
         * @return the modified object
         * @throws NullPointerException if parameter is null
         */
        public Item add(Enum<?> name, Enum<?> item) {
            return add(name.toString(), item);
        }

        /**
         * Add a named item property to the object.
         *
         * @param name property name
         * @param item item to be added
         * @return the modified object
         * @throws NullPointerException if parameter is null
         */
        public Item add(String name, Enum<?> item) {
            if (name==null || item==null)
                throw new NullPointerException(E_NULL);
            data.put(name, new Primitive(item));
            return this;
        }

        /**
         * Remove a named property from the object.
         *
         * @param name property name (as an enum constant)
         * @return the modified object
         * @throws NullPointerException if parameter is null
         */
        public Item remove(Enum<?> name) {
            return remove(name.toString());
        }

        /**
         * Remove a named property from the object.
         *
         * @param name property name
         * @return the modified object
         * @throws NullPointerException if parameter is null
         */
        public Item remove(String name) {
            if (name==null)
                throw new NullPointerException(E_NULL);
            data.remove(name);
            return this;
        }

        @Override
        void format(StringBuilder sb, int indent, boolean inline) {
            boolean il = inline || isInline();
            boolean isFirst = true;
            String tab = il ? SPACE
                            : (NEW_LINE + StringUtils.spaces(indent));

            sb.append(OPEN_BRACE);
            for (String item : data.keySet()) {
                if (isFirst)
                    isFirst = false;
                else
                    sb.append(COMMA);
                sb.append(tab);
                if (!il)
                    sb.append(TWO_SPACES);
                sb.append(ESC_QUOTE).append(item).append(QUOTE_COLON);
                data.get(item).format(sb, indent + 2, il);
            }
            sb.append(tab).append(CLOSE_BRACE);
        }

    }

    /**
     * Abstraction of a JSON array.
     */
    public static class Array extends Primitive {

        private final List<Primitive> data = new ArrayList<Primitive>();

        @Override
        public Array inline() {
            return (Array) super.inline();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            final Array array = (Array) o;
            return data.equals(array.data);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + data.hashCode();
            return result;
        }

        /** Returns the number of items in this array.
         *
         * @return the number of items
         */
        public int size() {
            return data.size();
        }
        
        /**
         * Get the entity at the specified index of this array.
         * 
         * @param index entity index
         * @return entity at the specified position in the list
         */
        public Primitive get(int index) {
            return data.get(index);
        }

        /**
         * Add an item to the array.
         *
         * @param item item to be added
         * @return the array
         * @throws NullPointerException if parameter is null
         */
        public Array add(Primitive item) {
            if (item==null)
                throw new NullPointerException(E_NULL);
            data.add(item);
            return this;
        }

        /**
         * Add an item to the array.
         *
         * @param item item to be added
         * @return the array
         * @throws NullPointerException if parameter is null
         */
        public Array add(String item) {
            if (item==null)
                throw new NullPointerException(E_NULL);
            data.add(new Primitive(item));
            return this;
        }

        /**
         * Add an item to the array.
         *
         * @param item item to be added
         * @return the array
         */
        public Array add(long item) {
            data.add(new Primitive(item));
            return this;
        }

        /**
         * Add an item to the array.
         *
         * @param item item to be added
         * @return the array
         */
        public Array add(boolean item) {
            data.add(new Primitive(item));
            return this;
        }

        /**
         * Add an item to the array.
         *
         * @param item item to be added
         * @return the array
         * @throws NullPointerException if parameter is null
         */
        public Array add(Enum<?> item) {
            if (item==null)
                throw new NullPointerException(E_NULL);
            data.add(new Primitive(item));
            return this;
        }

        /**
         * Add an array of items to the array.
         *
         * @param items items to be added
         * @return the array
         * @throws NullPointerException if parameter is null
         */
        public Array add(Primitive items[]) {
            if (items==null)
                throw new NullPointerException(E_NULL);
            data.addAll(Arrays.asList(items));
            return this;
        }

        @Override
        void format(StringBuilder sb, int indent, boolean inline) {
            boolean il = inline || isInline();
            boolean isFirst = true;
            String tab = il ? SPACE
                            : (NEW_LINE + StringUtils.spaces(indent));

            sb.append(OPEN_BRACKET);
            for (Primitive item : data) {
                if (isFirst)
                    isFirst = false;
                else
                    sb.append(COMMA);
                sb.append(tab);
                if (!il)
                    sb.append(TWO_SPACES);
                item.format(sb, indent + 2, il);
            }
            sb.append(tab).append(CLOSE_BRACKET);
        }

    }

}
