/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import static org.opendaylight.util.StringUtils.UTF8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import org.opendaylight.util.StringUtils;
import org.opendaylight.util.WebUtils.Tag;

/**
 * A default implementation of {@link DeviceInfo} which will likely be
 * sufficient for most needs. A {@link Properties} instance is used as the
 * backing store, with key/value pairs representing the data.
 * 
 * @author Simon Hunt
 * @author Frank Wood
 */
public class DefaultDeviceInfo extends AbstractDeviceInfo {

    /** name of root tag for internal properties instance. */
    static final String PROPERTIES_ROOT_NAME = "properties";

    private int generation;
    private final XMLConfiguration props;

    /**
     * Construct a new instance.
     * 
     * @param deviceType the underlying (initial) device type
     */
    public DefaultDeviceInfo(DefaultDeviceType deviceType) {
        super(deviceType);

        props = new XMLConfiguration();
        props.setRootElementName(PROPERTIES_ROOT_NAME);
        props.setDelimiterParsingDisabled(true);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[DefaultDeviceInfo:");
        if (getDeviceType() instanceof DefaultDeviceType) {
            sb.append(" ").append(((DefaultDeviceType)getDeviceType()).toShortDebugString());
        } else {
            sb.append(" type=").append(getDeviceType().getClass().getName());
            sb.append(" (").append(getDeviceType().getTypeName()).append(")");
        }
        sb.append(" #rootProps = ").append(props.getRootNode().getChildrenCount());
        sb.append("]");
        return sb.toString();
    }

    /**
     * Get the device info properties as an XML-encoded string.
     * 
     * @return XML encoded string of device properties
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(getClass().getName()).append(StringUtils.EOL);
        sb.append(exportData());
        sb.append("DeviceType:").append(StringUtils.EOL);
        if (getDeviceType() instanceof DefaultDeviceType) {
            sb.append(((DefaultDeviceType)getDeviceType()).toDebugString(2));
        } else {
            sb.append(getDeviceType());
        }
        return sb.toString();
    }


    // === DeviceInfo methods

    /**
     * This method delegates to {@link DeviceType#evolve} on the backing
     * device type, passing this instance as the context.
     * <p>
     * It is the responsibility of the delegate to properly update the
     * generation number if the device type evolved. If the new instance is
     * also of {@link DefaultDeviceInfo} class, this update should occur via
     * {@link #incrementGeneration}.
     * 
     * @return a possibly evolved instance of this device info
     */
    @Override
    public DeviceInfo evolve() {
        return getDeviceType().evolve(this);
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    /**
     * Increments the generation number of the device info by 1. Intended for use by
     * device type implementations to record generation change.
     */
    public void incrementGeneration() {
        this.generation++;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation produces an XML-encoded string of internal
     * properties.
     */
    @Override
    public String exportData() {
        try {
            ByteArrayOutputStream bbo = new ByteArrayOutputStream();
            props.save(bbo, UTF8);
            return bbo.toString(UTF8);
        } catch (ConfigurationException e) {
            return new Tag("properties").attr("error", e.getMessage()).toString();
        } catch (UnsupportedEncodingException e) {
            return new Tag("properties").attr("error", e.getMessage()).toString();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation loads the internal properties from the specified
     * XML-encoded string.
     */
    @Override
    public boolean importData(String data) {
        try {
            props.load(new ByteArrayInputStream(data.getBytes(UTF8)), UTF8);
            return true;
        } catch (ConfigurationException e) {
            return false;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    // === expose methods on our backing Properties instance

    /**
     * Returns the set of keys.
     * 
     * @return the set of keys
     */
    public Set<String> keys() {
        Set<String> keySet = new HashSet<String>();
        @SuppressWarnings({ "cast", "unchecked" })
        Iterator<String> iter = (Iterator<String>)props.getKeys();
        while (iter.hasNext())
            keySet.add(iter.next());
        return keySet;
    }    
    
    /**
     * Gives an indication whether the property with the specified key is
     * present or not.
     * 
     * @param key the key
     * @return true if property is present; false otherwise
     */
    public boolean hasProperty(String key) {
        return props.containsKey(key);
    }

    /**
     * Returns the string value stored under the given key, or null if the key
     * does not exist.
     * 
     * @param key the key
     * @return the value, or null
     */
    public String get(String key) {
        return props.getString(key);
    }

    /**
     * Returns the string value stored under the given key. If no such key
     * exists, the given default value is returned instead.
     * 
     * @param key the key
     * @param defaultValue the default value
     * @return the value associated with the key, if it exists, else default value
     */
    public String get(String key, String defaultValue) {
        return props.getString(key, defaultValue);
    }

    /**
     * Returns the integer value stored under the given key, or null if the
     * key does not exist.
     * 
     * @param key the key
     * @return the value, or null
     */
    public int getInt(String key) {
        try {
            return props.getInt(key);
        } catch (NoSuchElementException e) {
            return 0;
        }
    }

    /**
     * Returns the string value stored under the given key. If no such key
     * exists, the given default value is returned instead.
     * 
     * @param key the key
     * @param defaultValue the default value
     * @return the value associated with the key, if it exists, else default
     *         value
     */
    public int getInt(String key, int defaultValue) {
            return props.getInt(key, defaultValue);
    }
    
    /**
     * Returns the long value stored under the given key, or null if the
     * key does not exist.
     * 
     * @param key the key
     * @return the value, or null
     */
    public long getLong(String key) {
        try {
            return props.getLong(key);
        } catch (NoSuchElementException e) {
            return 0L;
        }
    }

    /**
     * Returns the long value stored under the given key. If no such key
     * exists, the given default value is returned instead.
     * 
     * @param key the key
     * @param defaultValue the default value
     * @return the value associated with the key, if it exists, else default
     *         value
     */
    public long getLong(String key, long defaultValue) {
            return props.getLong(key, defaultValue);
    }

    /**
     * Returns the boolean value stored under the given key, or null if the
     * key does not exist.
     *
     * @param key the key
     * @return the value, or null
     */
    public boolean getBoolean(String key) {
        try {
            return props.getBoolean(key);
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Returns the boolean value stored under the given key. If no such key
     * exists, the given default value is returned instead.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return the value associated with the key, if it exists, else default
     *         value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
            return props.getBoolean(key, defaultValue);
    }

    /**
     * Sets the given value for the given key.
     * 
     * @param key the key
     * @param value the value
     */
    public void set(String key, String value) {
        props.setProperty(key, value);
    }

    /**
     * Sets the given value for the given key.
     * 
     * @param key the key
     * @param value the value
     */
    public void set(String key, int value) {
        props.setProperty(key, value);
    }
    
    /**
     * Sets the given value for the given key.
     * 
     * @param key the key
     * @param value the value
     */
    public void set(String key, long value) {
        props.setProperty(key, value);
    }

    /**
     * Sets the given boolean value for the given key.
     *
     * @param key the key
     * @param value the value
     */
    public void set(String key, boolean value) {
        props.setProperty(key, value);
    }

    /**
     * Clears the property for the given key. Note that a property
     * is automatically cleared during the internal call to
     * {@link XMLConfiguration#setProperty(String, Object)}
     * 
     * @param key the key
     */
    public void clear(String key) {
        props.clearTree(key);
    }

    /** Returns the count of "subnodes" under the node for the given key.
     *
     * @param key the key
     * @return the count of subnodes
     */
    // TODO: unit test this
    public int getNodeCount(String key) {
        Configuration subset = props.subset(key);

        Set<String> keySet = new HashSet<String>();
        @SuppressWarnings({ "cast", "unchecked" })
        Iterator<String> iter = (Iterator<String>)subset.getKeys();
        while (iter.hasNext())
            keySet.add(iter.next());

        return keySet.size();
    }
}
