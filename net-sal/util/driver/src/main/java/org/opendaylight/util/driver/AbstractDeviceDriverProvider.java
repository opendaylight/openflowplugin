/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import org.opendaylight.util.net.IpAddress;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base abstraction of a device driver provider for simplifying the task of implementing one.
 * 
 * @author Thomas Vachuska
 * @author Simon Hunt
 * @author Frank Wood
 */
public abstract class AbstractDeviceDriverProvider implements DeviceDriverProvider {

    // package private access makes these strings available to unit tests...
    static final String EMSG_TYPE_ALREADY_BOUND = "Device type already bound by that name: ";
    static final String EMSG_NOT_THIS_PROVIDER = "type.getProvider() does not return this provider";
    static final String EMSG_REMOVING_NON_BOUND_TYPE = "attempting to remove non-bound type: ";
    static final String EMSG_SWITCHING_NON_BOUND_TYPE = "attempting to switch to non-bound type: ";
    static final String EMSG_PARENT_NOT_BOUND = "Parent of this device type is not bound: ";


    /** Type name to type bindings. */
    private final Map<String, DefaultDeviceType> typeBindings =
        new HashMap<String, DefaultDeviceType>();

    @Override
    public Set<String> getDeviceTypeNames() {
        return Collections.unmodifiableSet(typeBindings.keySet());
    }

    /**
     * Adds the specified device type to the type bindings under the given name.
     * 
     * @param typeName device type name or alias (binding key)
     * @param type device type instance
     * @throws IllegalArgumentException if the provider declared by the type is not this provider
     * @throws IllegalStateException if a binding already exists for the given type name
     */
    protected void addType(String typeName, DefaultDeviceType type) {
        // since we are attempting to add a type to this provider, verify that
        // the provider returned from the type is this one...
        if (type.getProvider() != this)
            throw new IllegalArgumentException(EMSG_NOT_THIS_PROVIDER);
        
        // catch type name collisions
        DefaultDeviceType already = typeBindings.get(typeName);
        if (already != null)
            throw new IllegalStateException(EMSG_TYPE_ALREADY_BOUND + typeName);

        if (type.getParentType() != null &&
                typeBindings.get(type.getParentType().getTypeName()) == null)
            throw new IllegalArgumentException(EMSG_PARENT_NOT_BOUND + type.getParentType().getTypeName());

        typeBindings.put(typeName, type);
        if (type.getParentType() != null) {
            ((DefaultDeviceType)type.getParentType()).addChild(type);
        }
    }

    /**
     * Convenience method that calls {@link #addType(String, DefaultDeviceType)} using the
     * type name returned from the type.
     *
     * @param type device type instance to add
     */
    protected void addType(DefaultDeviceType type) {
        addType(type.getTypeName(), type);
    }

    /**
     * Removes the specified device type and all its name bindings. In addition,
     * <em>all</em> descendants of the type will be removed from the provider also!
     *
     * @param type device type instance to remove
     * @throws IllegalStateException if attempting to remove a type for which there is no binding
     */
    protected void removeType(DefaultDeviceType type) {
        Set<DefaultDeviceType> typesToRemove = new HashSet<DefaultDeviceType>();
        addDescendentsToSet(typesToRemove, type);

        Set<String> namesToRemove = new HashSet<String>();
        for (Map.Entry<String, DefaultDeviceType> entry : typeBindings.entrySet()) {
            if (typesToRemove.contains(entry.getValue()))
                namesToRemove.add(entry.getKey());
        }

        if (namesToRemove.size() == 0)
            throw new IllegalStateException(EMSG_REMOVING_NON_BOUND_TYPE + type.getTypeName());

        for (String key: namesToRemove) {
            typeBindings.remove(key);
        }

        // lastly, remove the type from its parent's list of children
        // todo: verify that parent is registered in this provider
        if (type.getParentType() != null) {
            ((DefaultDeviceType)type.getParentType()).removeChild(type);
        }
    }

    // recursive helper method to add descendent types to set for removal
    private void addDescendentsToSet(Set<DefaultDeviceType> typesToRemove, DefaultDeviceType type) {
        typesToRemove.add(type);
        if (type.getChildTypes().size() > 0) {
            for (DeviceType kid: type.getChildTypes()) {
                addDescendentsToSet(typesToRemove, (DefaultDeviceType) kid);
            }
        }
    }

    /**
     * Gets the device type for the specified name, or null if no such binding exists.
     * 
     * @param typeName requested device type name
     * @return device type of the given name, or null
     */
    protected DefaultDeviceType getDeviceType(String typeName) {
        return typeBindings.get(typeName);
    }


    /** Returns the number of entries in the type bindings map
     *
     * @return the number of bindings
     */
    public int getBindingsCount() {
        return typeBindings.size();
    }

    /** Returns the number of different types in the bindings map
     *
     * @return the number of types
     */
    public int getTypesCount() {
        Set<DefaultDeviceType> uniqueTypes = new HashSet<DefaultDeviceType>(typeBindings.values());
        return uniqueTypes.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[")
                .append(getClass().getName())
                .append(": bindings=").append(getBindingsCount())
                .append(", types=").append(getTypesCount())
                .append("]");
        return sb.toString();
    }

    @Override
    public DeviceInfo create(String typeName) {
        // Create a new default device info to hold the device properties.
        DefaultDeviceType type = getDeviceType(typeName);
        if (type == null)
            return null;

        DefaultDeviceInfo ddi = new DefaultDeviceInfo(type);

        // Stamp the device info with any initial data
        String data = type.getInstanceData();
        if (data != null)
            ddi.importData(data);

        return ddi;
    }

    @Override
    public DeviceHandler create(String typeName, IpAddress ip) {
        return create(create(typeName), ip);
    }

    @Override
    public DeviceHandler create(DeviceInfo info, IpAddress ip) {
        DeviceType dt = getDeviceType(info.getTypeName());
        return dt.createHandler(info, ip);
    }

    @Override
    public DeviceLoader create(String typeName, String uid) {
        return create(create(typeName), uid);
    }

    @Override
    public DeviceLoader create(DeviceInfo info, String uid) {
        DeviceType dt = getDeviceType(info.getTypeName());
        return dt.createLoader(info, uid);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation checks that the given type name exists in the type
     * bindings, and switches the type if it does. If the type is not bound,
     * then an exception is thrown.
     * @throws IllegalStateException if the specified type is not bound in this provider
     */
    @Override
    public void switchType(MutableDeviceInfo mutableDeviceInfo, String newTypeName) {
        DefaultDeviceType t = getDeviceType(newTypeName);
        if (t == null)
            throw new IllegalStateException(EMSG_SWITCHING_NON_BOUND_TYPE + newTypeName);

        mutableDeviceInfo.setDeviceType(t);
    }


    //=== UNIT TEST Support
    /** Returns a reference to the internal bindings map.
     *
     * @return the bindings map
     */
    Map<String, DefaultDeviceType> getBindings() {
        return typeBindings;
    }
}
