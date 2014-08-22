/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

/**
 * Constants used for the scheme portion of a URI. The URI reference at the
 * highest level has the syntax 
 *      [scheme:]scheme-specific-part[#fragment]
 * URIs are used to identify a device.  This file contains constants that are
 * used when creating URIs that identify devices.
 *
 * @author Steve Dean
 */
public enum UriSchemes {
    OPENFLOW    ("of"),
    IP          ("ip"),
    SNMP        ("snmp"),
    NETCONF     ("netconf");

    private String key;
    private UriSchemes(String key) { this.key = key; }
    @Override
    public String toString() { return key; }
}
