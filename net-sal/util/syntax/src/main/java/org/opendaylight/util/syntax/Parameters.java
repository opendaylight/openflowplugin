/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.io.Serializable;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class to keep track of all {@link java.io.Serializable serializable}
 * parameter objects parsed from the command line arguments.
 * <p>
 * Some parameters can be marked as indexed, which means that there can be
 * more than one specified on the command-line. Such parameters can be
 * accessed using an index between 0 and the number of parameter occurrences,
 * where the index value corresponds to the order in which they were specified
 * on the command line.
 * 
 * @author Thomas Vachuska
 */
public class Parameters {

    /** Separator for indexed parameter names. */
    private static final String INDEX_SEPARATOR = "#";

    /** List of parameter names in the order in which they were added.  */
    private List<String> names = new ArrayList<String>();
    
    /** Map of name -> object bindings. */
    private Map<String, Object> map = new HashMap<String, Object>();

    /** Map of name -> count bindings. */
    private Map<String, ParsePosition> counts = new HashMap<String, ParsePosition>();

    /**
     * Default constructor.
     */
    public Parameters() {
    }
    
    /**
     * Get the count of the parameters.
     * @return Count of parameters in the map
     */
    public int size() {
        return map.size();
    }

    /**
     * Returns the ParsePosition object corresponding to the parameter name.
     * 
     * @param name parameter name
     * @return count of the occurrences of the specified parameter
     */
    private ParsePosition getCount(String name) {
        return counts.get(name);
    }

    /**
     * Determines whether a parameter with the specified name is present.
     * 
     * @param name Name of the parameter whose presence we are testing.
     * @return True if the parameter is present; false otherwise.
     */
    public boolean isPresent(String name) {
        return getOccurrences(name) > 0;
    }

    /**
     * Returns true if the given parameter values are indexed.
     * 
     * @param name Name of the parameter.
     * @return True if the parameter is indexed; false otherwise.
     */
    public boolean isIndexed(String name) {
        return getCount(name) != null;
    }

    /**
     * Returns the number of occurrences of the given indexed parameter.
     * 
     * @param name Name of the parameter whose presence we are testing.
     * @return Number of times the indexed parameter is present in the map or
     *         1 if the parameter is not indexed.
     */
    public int getOccurrences(String name) {
        ParsePosition count = getCount(name);
        if (count != null)
            return count.getIndex();
        else if (get(name) != null)
            return 1;
        else
            return 0;
    }

    /**
     * Returns the parameter object associated with the parameter name.
     * 
     * @param name Name of the parameter.
     * @return Serializable value mapped to the parameter name.
     */
    public Serializable get(String name) {
        return (Serializable) map.get(name);
    }

    /**
     * Returns the indexed parameter object associated with the parameter
     * name.
     * 
     * @param name Name of the parameter.
     * @param index Index (starting with 0), of the value associated with the
     *        parameter name.
     * @return Serializable value mapped to the parameter name.
     */
    public Serializable get(String name, int index) {
        return get(name + INDEX_SEPARATOR + index);
    }

    /**
     * Get list of values associated with the specified indexed parameter
     * name. Values are put on the list in the order in which they were
     * specified on the command-line.
     * 
     * @param name Name of the indexed parameter.
     * @return List of serializable values mapped to the indexed parameter
     *         name.
     */
    public List<Object> getList(String name) {
        ParsePosition count = getCount(name);
        if (count != null) {
            int length = count.getIndex();
            List<Object> values = new ArrayList<Object>(length);
            for (int i = 0; i < length; i++)
                values.add(get(name, i));
            return values;
        }

        Serializable value = get(name);
        if (value != null) {
            List<Object> values = new ArrayList<Object>(1);
            values.add(value);
            return values;
        }
        return null;
    }

    /**
     * Adds the specified name/object binding to the parameter map.
     * 
     * @param name Name of the parameter.
     * @param object Serializable value to be mapped to the parameter name.
     * @param isIndexed True if the parameter value is to be marked as
     *        indexed, i.e. there could be more than one value associated with
     *        the given parameter name.
     */
    public void add(String name, Serializable object, boolean isIndexed) {
        if (isIndexed) {
            add(name, object, -1);
        } else {
            names.add(name);
            map.put(name, object);
        }
    }

    /**
     * Adds the specified indexed name/object binding to the parameter map.
     * 
     * @param name Name of the parameter.
     * @param object Serializable value to be mapped to the parameter name.
     * @param index Index of the parameter to assign to the name or -1 if it
     *        should be automatically incremented.
     */
    private void add(String name, Serializable object, int index) {
        int newIndex = 0;
        ParsePosition count = getCount(name);
        if (index < 0) {
            if (count == null) {
                //  If there is no count and we were asked to auto-generate
                //  an index, let's create a new count and register it.
                count = new ParsePosition(1);
                counts.put(name, count);
            } else {
                newIndex = count.getIndex();
                count.setIndex(newIndex + 1);
            }
        } else {
            newIndex = index;
            if (count == null) {
                //  If there is no count and we were given an index, 
                //  let's create a new count and register it.
                count = new ParsePosition(1);
                counts.put(name, count);
            } else if (index >= count.getIndex()) {
                count.setIndex(index + 1);
            }
        }
        
        String indexedName = name + INDEX_SEPARATOR + newIndex;
        names.add(indexedName);
        map.put(indexedName, object);
    }

    /**
     * Adds all bindings from the given parameter map to this parameter map.
     * Parameters with the same name (and index) override the existing value.
     * 
     * @param parameters The parameters map whose name/object bindings are to
     *        be imported to this map.
     */
    public void add(Parameters parameters) {
        Iterator<String> it = parameters.names.iterator();
        while (it.hasNext()) {
            String name = it.next();
            Serializable value = (Serializable) parameters.map.get(name);
            int i = name.indexOf(INDEX_SEPARATOR);
            if (i > 0) {
                add(name.substring(0, i), value, -1);
            } else {
                add(name, value, false);
            }
        }
    }

    /**
     * Clears the contents of the parameter map.
     */
    void clear() {
        names.clear();
        map.clear();
        counts.clear();
    }

    /**
     * Returns an unmodifiable set of all parameters names in this map.
     * 
     * @return Set of parameter names.
     */
    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder('[');
        for (String name : names)
            sb.append(name).append("=").append(map.get(name)).append(';');
        return sb.append(']').toString();
    }

}
