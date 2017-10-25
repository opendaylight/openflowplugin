/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

/**
 * Parent for Ipv4 address based match entry serializers
 * @author michal.polkorab
 */
public abstract class AbstractOxmIpv4AddressSerializer extends AbstractOxmMatchEntrySerializer {

    /**
     * @deprecated Use {@link #writeIpv4Address(Ipv4Address, ByteBuf)} instead.
     */
    @Deprecated
    protected static void writeIpv4Address(final String address, final ByteBuf out) {
        Iterable<String> addressGroups = ByteBufUtils.DOT_SPLITTER.split(address);
        for (String group : addressGroups) {
            out.writeByte(Short.parseShort(group));
        }
    }

    protected static void writeIpv4Address(final Ipv4Address address, final ByteBuf out) {
        out.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(address));
    }
}
