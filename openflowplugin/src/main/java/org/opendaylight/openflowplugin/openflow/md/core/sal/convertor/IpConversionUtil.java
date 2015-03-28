/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.base.Splitter;
import io.netty.buffer.ByteBufUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import java.util.Iterator;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 5.3.2015.
 */
public final class IpConversionUtil {

    public static final String PREFIX_SEPARATOR = "/";
    public static final Splitter PREFIX_SPLITTER = Splitter.on('/');

    private IpConversionUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    public static Iterator<String> splitToParts(final Ipv4Prefix ipv4Prefix) {
        return PREFIX_SPLITTER.split(ipv4Prefix.getValue()).iterator();
    }

    public static Iterator<String> splitToParts(final Ipv4Address ipv4Address) {
        return PREFIX_SPLITTER.split(ipv4Address.getValue()).iterator();
    }

    public static Iterator<String> splitToParts(final Ipv6Address ipv6Address) {
        return PREFIX_SPLITTER.split(ipv6Address.getValue()).iterator();
    }

    public static Ipv4Prefix createPrefix(Ipv4Address ipv4Address){
        Iterator<String> addressParts = splitToParts(ipv4Address);
        String address = addressParts.next();
        Ipv4Prefix retval = null;
        if (addressParts.hasNext()) {
            retval = new Ipv4Prefix(address + PREFIX_SEPARATOR + Integer.parseInt(addressParts.next()));
        } else {
            retval = new Ipv4Prefix(address + PREFIX_SEPARATOR + 32);
        }
        return retval;
    }
    public static Ipv4Prefix createPrefix(Ipv4Address ipv4Address, String mask){
        Iterator<String> addressParts = splitToParts(ipv4Address);
        String address = addressParts.next();
        Ipv4Prefix retval = null;
        if (null != mask && !mask.equals("")) {
            retval = new Ipv4Prefix(address + mask);
        } else {
            retval = new Ipv4Prefix(address + PREFIX_SEPARATOR + 32);
        }
        return retval;
    }
    public static Integer extractPrefix(Ipv4Address ipv4Address) {
        Iterator<String> addressParts = splitToParts(ipv4Address);
        addressParts.next();
        Integer retval = null;
        if (addressParts.hasNext()) {
            retval = Integer.parseInt(addressParts.next());
        }
        return retval;
    }
    public static Integer extractPrefix(Ipv6Address ipv6Address) {
        Iterator<String> addressParts = splitToParts(ipv6Address);
        addressParts.next();
        Integer retval = null;
        if (addressParts.hasNext()) {
            retval = Integer.parseInt(addressParts.next());
        }
        return retval;
    }


    public static byte[] convertIpv6PrefixToByteArray(final int prefix) {
        // TODO: Temporary fix. Has performance impacts.
        byte[] mask = new byte[16];
        int oneCount = prefix;
        for (int count = 0; count < 16; count++) {
            int byteBits = 0;
            if (oneCount >= 8) {
                byteBits = 8;
                oneCount = oneCount - 8;
            } else {
                byteBits = oneCount;
                oneCount = 0;
            }

            mask[count] = (byte) (256 - Math.pow(2, 8 - byteBits));
        }
        return mask;
    }

    public static Ipv6Address extractIpv6Address(final Ipv6Prefix ipv6Prefix) {
        Iterator<String> addressParts = PREFIX_SPLITTER.split(ipv6Prefix.getValue()).iterator();
        return new Ipv6Address(addressParts.next());
    }

    public static Integer extractIpv6Prefix(final Ipv6Prefix ipv6Prefix) {
        Iterator<String> addressParts = PREFIX_SPLITTER.split(ipv6Prefix.getValue()).iterator();
        addressParts.next();

        Integer prefix = null;
        if (addressParts.hasNext()) {
            prefix = Integer.parseInt(addressParts.next());
        }
        return prefix;
    }


}
