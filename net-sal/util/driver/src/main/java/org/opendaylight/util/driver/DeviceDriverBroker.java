/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * An aggregate {@link DeviceDriverProvider} able to delegate requests to registered providers.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public interface DeviceDriverBroker extends DeviceDriverProvider {

    /** Adds the given provider to the collection of providers tracked by this broker.
     *
     * @param provider the provider to add
     * @throws IllegalStateException if a provider already exists for one of the device types supported by
     *          the specified provider
     */
    public void addProvider(DeviceDriverProvider provider);

    /** Removes the given provider from the collection tracked by this broker.
     *
     * @param provider the provider to remove
     */
    public void removeProvider(DeviceDriverProvider provider);

    /** Returns a reference to the provider associated with the specified device type. This method may return
     * null if no registered providers support the specified type.
     *
     * @param typeName the device type name
     * @return the appropriate provider (or null)
     */
    public DeviceDriverProvider getProvider(String typeName);

}
