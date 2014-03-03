/**
 * Copyright (c) 2013 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action;

import java.math.BigInteger;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv6;

/**
 * Utility class for converting a MD-SAL action subelement into the OF subelement
 */
public class ActionSetNwSrcConvertorImpl implements Convertor<SetNwSrcActionCase, Object> {
    
    private static final String PREFIX_SEPARATOR = "/";
    
    @Override
    public Object convert(SetNwSrcActionCase source, BigInteger datapathid) {
        Address address = source.getSetNwSrcAction().getAddress();
        if (address instanceof Ipv4) {
            String[] addressParts = ((Ipv4) address).getIpv4Address().getValue().split(PREFIX_SEPARATOR);
            return new Ipv4Address(addressParts[0]);
        } else if (address instanceof Ipv6) {
            String[] addressParts = ((Ipv6) address).getIpv6Address().getValue().split(PREFIX_SEPARATOR);
            return new Ipv6Address(addressParts[0]);
        } else {
            throw new IllegalArgumentException("Address is not supported: "+address.getClass().getName());
        }
    }
}
