/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

/**
 * Device identifier.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 * @author Steve Dean
 */
public class DeviceId implements ElementId {

    private final String fingerprint;

    // The unique fingerprint for the special 'NONE' device ID
    private static final String NONE_FINGERPRINT = "";

    /**
     * A static value used to indicate an unknown/unnecessary device ID.
     */
    public static final DeviceId NONE = new DeviceId(NONE_FINGERPRINT);

    /**
     * Private constructor to create a device ID based on the given fingerprint.
     *
     * @param fingerprint the given fingerprint
     */
    private DeviceId(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    /**
     * Returns the value of the given fingerprint as a device ID.
     *
     * @param fingerprint the given fingerprint
     * @return device ID representation
     */
    public static DeviceId valueOf(String fingerprint) {
        return new DeviceId(fingerprint);
    }

    /**
     * Returns the fingerprint for this device ID.
     *
     * @return fingerprint
     */
    public String fingerprint() {
        return fingerprint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceId deviceId = (DeviceId) o;
        return fingerprint.equals(deviceId.fingerprint);
    }

    @Override
    public int hashCode() {
        return fingerprint.hashCode();
    }

    @Override
    public String toString() {
        return fingerprint;
    }

}
