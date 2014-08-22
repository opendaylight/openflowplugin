/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import static org.opendaylight.util.StringUtils.EOL;
import org.opendaylight.util.net.IpAddress;

/**
 * A default implementation of the device identity facet. This implementation
 * makes the assumption that the context is a {@link DefaultDeviceInfo}
 * implementation.
 * 
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public class DefaultDeviceIdentity extends AbstractDefaultFacet 
                                        implements DeviceIdentity {

    /**
     * Constructs a device identity facet that is projected onto the specified
     * device info context.
     * 
     * @param context the device info of which this is a facet
     */
    public DefaultDeviceIdentity(DeviceInfo context) {
        super(context);
    }

    @Override
    public String getUniqueId() {
        return getContext().get(CoreDevicePropertyKeys.UNIQUE_ID);
    }

    @Override
    public String getVendor() {
        String s = getContext().get(CoreDevicePropertyKeys.VENDOR);
        return s != null ? s : getContext().getType().getVendor();
    }

    @Override
    public String getFamily() {
        return getContext().getType().getFamily();
    }

    @Override
    public String getProductNumber() {
        String s = getContext().get(CoreDevicePropertyKeys.PRODUCT);
        return s != null ? s : getContext().getType().getProduct();
    }

    @Override
    public String getModelNumber() {
        String s = getContext().get(CoreDevicePropertyKeys.MODEL);
        return s != null ? s : getContext().getType().getModel();
    }

    @Override
    public String getSerialNumber() {
        return getContext().get(CoreDevicePropertyKeys.SERIAL_NUMBER);
    }

    @Override
    public String getDriverFirmwareVersion() {
        return getContext().getType().getFw();
    }

    @Override
    public String getFirmwareVersion() {
        return getContext().get(CoreDevicePropertyKeys.FIRMWARE_VERSION);
    }

    @Override
    public String getName() {
        return getContext().get(CoreDevicePropertyKeys.NAME);
    }

    @Override
    public String getDescription() {
        return getContext().get(CoreDevicePropertyKeys.DESCRIPTION);
    }

    @Override
    public String getLocation() {
        return getContext().get(CoreDevicePropertyKeys.LOCATION);
    }

    @Override
    public String getContact() {
        return getContext().get(CoreDevicePropertyKeys.CONTACT);
    }

    @Override
    public IpAddress getIpAddress() {
        String ip = getContext().get(CoreDevicePropertyKeys.IP_ADDRESS);
        return ip != null ? IpAddress.valueOf(ip) : null;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[").append(this.getClass().getSimpleName())
                .append(": id = ").append(getUniqueId())
                .append(" name = ").append(getName())
                .append(" modelNumber = ").append(getModelNumber())
                .append("]");
        return sb.toString();
    }

    public String toDebugString() {
        StringBuilder sb= new StringBuilder("Device Identity for ").append(this.getContext())
                .append("...").append(EOL)
                .append(INDENT).append("Unique ID   : ").append(getUniqueId()).append(EOL)
                .append(INDENT).append("IP address  : ").append(getIpAddress()).append(EOL)
                .append(INDENT).append("Vendor      : ").append(getVendor()).append(EOL)
                .append(INDENT).append("Family      : ").append(getFamily()).append(EOL)
                .append(INDENT).append("Product #   : ").append(getProductNumber()).append(EOL)
                .append(INDENT).append("Model #     : ").append(getModelNumber()).append(EOL)
                .append(INDENT).append("Serial #    : ").append(getSerialNumber()).append(EOL)
                .append(INDENT).append("F/W version : ").append(getFirmwareVersion()).append(EOL)
                .append(INDENT).append("Name        : ").append(getName()).append(EOL)
                .append(INDENT).append("Description : ").append(getDescription()).append(EOL)
                .append(INDENT).append("Location    : ").append(getLocation()).append(EOL)
                .append(INDENT).append("Contact     : ").append(getContact()).append(EOL);

        return sb.toString();
    }

    private static final String INDENT = "  ";
}
