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
 * A factory that can produce {@link DeviceInfo}, {@link DeviceHandler} and {@link DeviceLoader} instances,
 * by delegation to pre-registered providers.
 *
 * @author Simon Hunt
 */
public class DeviceDriverFactory implements DeviceDriverBroker {

    private final Map<String, DeviceDriverProvider> registry = new HashMap<String, DeviceDriverProvider>();


    @Override
    public String toString() {
        return new StringBuilder("[DeviceInfoFactory: registry size = " + registry.size() + "]").toString();
    }
    
    //=== DeviceInfoBroker ===

    @Override
    public void addProvider(DeviceDriverProvider provider) {
        synchronized (registry) {
            DeviceDriverProvider already;
            // all or nothing
            for (String type: provider.getDeviceTypeNames()) {
                already = registry.get(type);
                if (already != null) {
                    throw new IllegalStateException("provider already exists for type: " + type +
                            " (" + already.getClass().getName() + ")");
                }
            }
            // (all or nothing)
            for (String type: provider.getDeviceTypeNames()) {
                registry.put(type, provider);
            }
        }
    }

    @Override
    public void removeProvider(DeviceDriverProvider provider) {
        synchronized (registry) {
            DeviceDriverProvider p;
            Set<String> toRemove = new HashSet<String>(provider.getDeviceTypeNames());
            // remove all references (or none)
            for (String type: provider.getDeviceTypeNames()) {
                p = registry.get(type);
                if (p == null) {
                    throw new IllegalStateException("Trying to remove provider for non-supported type: " + type);
                } else if (p != provider) {
                    throw new IllegalStateException("Trying to remove other provider reference for type: " + type);
                }
            }
            // if we get to here, we're clear to remove all entries
            for (String type: toRemove) {
                registry.remove(type);
            }
        }
    }

    @Override
    public DeviceDriverProvider getProvider(String typeName) {
        synchronized (registry) {
            return registry.get(typeName);
        }
    }

    // === DeviceInfoProvider ===

    @Override
    public Set<String> getDeviceTypeNames() {
        synchronized (registry) {
            return Collections.unmodifiableSet(registry.keySet());
        }
    }

    // The following methods all delegate to the appropriate registered provider

    @Override
    public DeviceInfo create(String typeName) {
        DeviceDriverProvider p = getProvider(typeName);
        return p == null ? null : p.create(typeName);
    }

    @Override
    public void switchType(MutableDeviceInfo mutableDeviceInfo, String typeName) {
        DeviceDriverProvider p = getProvider(typeName);
        // TODO: should we throw an exception if there is no provider that can handle the given replacement type?
        if (p != null) {
            p.switchType(mutableDeviceInfo, typeName);
        }
    }

    @Override
    public DeviceHandler create(String typeName, IpAddress ip) {
        DeviceDriverProvider p = getProvider(typeName);
        return p == null ? null : p.create(typeName, ip);
    }

    @Override
    public DeviceHandler create(DeviceInfo info, IpAddress ip) {
        DeviceDriverProvider p = getProvider(info.getTypeName());
        return p == null ? null : p.create(info, ip);
    }

    @Override
    public DeviceLoader create(String typeName, String uid) {
        DeviceDriverProvider p = getProvider(typeName);
        return p == null ? null : p.create(typeName, uid);
    }

    @Override
    public DeviceLoader create(DeviceInfo info, String uid) {
        DeviceDriverProvider p = getProvider(info.getTypeName());
        return p == null ? null : p.create(info, uid);
    }


    // === UNIT TEST Support methods

    Map<String, DeviceDriverProvider> getRegistry() { return registry; }

    int getRegistrySize() { return registry.size(); }

}
