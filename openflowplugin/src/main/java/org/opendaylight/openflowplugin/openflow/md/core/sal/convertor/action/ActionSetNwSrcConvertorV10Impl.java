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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4;

/**
 * Utility class for converting a MD-SAL action subelement into the OF subelement
 */
public class ActionSetNwSrcConvertorV10Impl implements Convertor<SetNwSrcActionCase, Object> {
    
    @Override
    public Object convert(SetNwSrcActionCase source, BigInteger datapathid) {
        Address address = source.getSetNwSrcAction().getAddress();
        if (address instanceof Ipv4) {
            return new Ipv4Address(((Ipv4) address).getIpv4Address().getValue());
        } else {
            throw new IllegalArgumentException("Address is not supported by OF-1.0: "+address.getClass().getName());
        }
    }
}
