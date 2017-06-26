/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

/**
 * @author michal.polkorab
 *
 */
public final class OxmDeserializerHelper {

    private OxmDeserializerHelper() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Converts binary data into binary mask (for match entries)
     * @param input input ByteBuf
     * @param matchEntryLength mask length
     * @return binary mask
     */
    public static byte[] convertMask(final ByteBuf input, final int matchEntryLength) {
        byte[] mask = new byte[matchEntryLength];
        input.readBytes(mask);
        return mask;
    }

    /**
     * Converts binary data into mac address
     * @param input input ByteBuf
     * @return mac address
     */
    public static MacAddress convertMacAddress(final ByteBuf input) {
        byte[] address = new byte[EncodeConstants.MAC_ADDRESS_LENGTH];
        input.readBytes(address);
        return IetfYangUtil.INSTANCE.macAddressFor(address);
    }
}
