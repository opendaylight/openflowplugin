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
        String ipAddress = ipv4Address.getValue();
        long result = 0;
        String[] atoms = ipAddress.split("\\.");
        for (int i = 3; i >= 0; i--) {
            result |= (Long.parseLong(atoms[3 - i]) << (i * 8));
        }
        return result & 0xFFFFFFFF;
    }

    public static Ipv4Address longToIpv4Address(long ip) {
        long tmpIp = ip;
        StringBuilder sb = new StringBuilder(15);
        for (int i = 0; i < 4; i++) {
            sb.insert(0, Long.toString(tmpIp & 0xff));
            if (i < 3) {
                sb.insert(0, '.');
            }
            tmpIp >>= 8;
        }
        return new Ipv4Address(sb.toString());
    }

}
