/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;

/**
 * @author msunal
 *
 */
public final class IpConverter {

    public static long Ipv4AddressToLong(Ipv4Address ipv4Address) {
        
        return ipv4Address.getIntegerForm() & 0xFFFFFFFF;
    }

    public static Ipv4Address longToIpv4Address(long ip) {
        return new Ipv4Address((int) ip);
    }

}
