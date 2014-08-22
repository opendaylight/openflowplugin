/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

/**
 * A common declaration of property keys used in the implementation of my device info supporting classes.
 *
 * @author Simon Hunt
 */
public class CoreDevicePropertyKeys {

    // TODO: javadoc these constants

    public static final String VENDOR = "device.vendor";
    public static final String PRODUCT = "device.product";
    public static final String MODEL = "device.model";
    public static final String SERIAL_NUMBER = "device.serial";
    public static final String FIRMWARE_VERSION = "device.fw";

    public static final String NAME = "device.name";
    public static final String DESCRIPTION = "device.desc";
    public static final String LOCATION = "device.loc";
    public static final String CONTACT = "device.contact";
    
    public static final String IP_ADDRESS = "device.ip";
    public static final String UNIQUE_ID = "device.uid";

}
