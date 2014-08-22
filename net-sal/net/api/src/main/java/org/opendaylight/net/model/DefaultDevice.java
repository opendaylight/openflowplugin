/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.opendaylight.util.driver.DeviceInfo;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.net.supplier.SupplierId;

/**
 * Provides a default implementation of the {@link Device} API.
 *
 * @author Julie Britt
 */
public class DefaultDevice extends AbstractModel implements Device {
    private static final String E_NULL_PARAM = "Parameters cannot be null";

    /**
     * Information about this device.
     */
    protected DeviceInfo deviceInfo;

    /**
     * Indicates whether this device is online.
     */
    protected boolean isOnline;

    /**
     * The set of URIs given by the supplier describing the mechanisms
     * through which this device is known.
     */
    protected final Set<URI> managementURIs = new HashSet<>();
    private final DeviceId deviceId;
    private String name;

    /**
     * Constructs a device using the given information.
     *
     * @param supplierId device supplier id
     * @param deviceId   device id
     * @param uris       for communicating with the device
     * @param info       information about the device
     * @throws NullPointerException if there is no information
     */
    public DefaultDevice(SupplierId supplierId, DeviceId deviceId, Set<URI> uris, DeviceInfo info) {
        super(supplierId);
        if (deviceId == null || info == null || uris == null || supplierId == null)
            throw new NullPointerException(E_NULL_PARAM);
        this.deviceId = deviceId;
        this.deviceInfo = info;
        this.isOnline = true;
        managementURIs.addAll(uris);
    }

    /**
     * Constructs a device using the given information.
     *
     * @param deviceId   device id
     * @param uris       for communicating with the device
     * @param info       information about the device
     * @throws NullPointerException if there is no information
     */
    public DefaultDevice(DeviceId deviceId, Set<URI> uris, DeviceInfo info) {
        this(null, deviceId, uris, info);
    }

    @Override
    public DeviceInfo info() {
        return deviceInfo;
    }

    /**
     * Indicates that the device is on or offline.
     *
     * @param state on or offline
     */
    public void setOnline(boolean state) {
        isOnline = state;
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public DataPathId dpid() {
        for (URI uri : managementURIs) {
            if (uri.getScheme().equals(UriSchemes.OPENFLOW.toString()))
                return DataPathId.valueOf(uri.getSchemeSpecificPart());
        }
        return null;
    }

    @Override
    public Device realizedBy() {
        // TODO: Discovery will determine if new device realizes existing or is realized by existing.  Stretch goal for Bantha.
        return null;
    }

    /**
     * Add a management URI to the list of possible ways to communicate
     * with this device.
     *
     * @param uri to add to list
     * @return true if the uri was added; false if it was already there
     */
    public boolean addManagementURI(URI uri) {
        return managementURIs.add(uri);
    }

    @Override
    public Set<URI> managementURIs() {
        return managementURIs;
    }

    @Override
    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Type type() {
        return null;
    }

    @Override
    public DeviceId id() {
        return deviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultDevice other = (DefaultDevice) o;
        if (!(this.deviceId.equals(other.deviceId)
                && this.isOnline == other.isOnline))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        } else if (!this.name.equals(other.name))
            return false;
        if (!(this.deviceInfo.getTypeName().equals(other.deviceInfo.getTypeName())
                && this.deviceInfo.exportData().equals(other.deviceInfo.exportData())))
            return false;
        if (this.managementURIs.size() != other.managementURIs().size())
            return false;
        for (URI uri : this.managementURIs) {
            if (!other.managementURIs.contains(uri))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = deviceId.hashCode();
        result = isOnline ? (31 * result) : result;
        result = 31 * result + managementURIs.hashCode();
        result = 31 * result + deviceInfo.getTypeName().hashCode();
        result = 31 * result + deviceInfo.exportData().hashCode();
        result = (name != null) ? (31 * result + name.hashCode()) : result;
        return result;
    }

    @Override
    public String toString() {
        return "DefaultDevice{" +
                "deviceId=" + deviceId +
                ", name='" + name +
                ", deviceInfo=" + deviceInfo +
                ", isOnline=" + isOnline +
                ", managementURIs=" + managementURIs +
                '}';
    }

}
