/**
 * Copyright (c) 2013 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action;

import com.google.common.base.Splitter;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv6;

/**
 * Utility class for converting a MD-SAL action subelement into the OF subelement
 */
public class ActionSetNwDstConvertorImpl implements Convertor<SetNwDstActionCase, Object> {
    private static final Splitter PREFIX_SPLITTER = Splitter.on('/');

    @Override
    public Class<?> getType() {
        return SetNwDstActionCase.class;
    }

    @Override
    public Object convert(final SetNwDstActionCase source) {
        Address address = source.getSetNwDstAction().getAddress();
        if (address instanceof Ipv4) {
            Iterable<String> addressParts = PREFIX_SPLITTER.split(((Ipv4) address).getIpv4Address().getValue());
            return new Ipv4Address(addressParts.iterator().next());
        } else if (address instanceof Ipv6) {
            Iterable<String> addressParts = PREFIX_SPLITTER.split(((Ipv6) address).getIpv6Address().getValue());
            return new Ipv6Address(addressParts.iterator().next());
        } else {
            throw new IllegalArgumentException("Address is not supported: "+address.getClass().getName());
        }
    }
}
