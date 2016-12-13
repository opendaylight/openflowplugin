/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmDeserializerHelper;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MatchEntryDeserializer;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;

public abstract class AbstractMatchEntryDeserializer implements MatchEntryDeserializer {

    /**
     * Processes match entry header and returns if it have mask, or not
     * @param in input buffer
     * @return true if match entry has mask, false otherwise
     */
    protected static boolean processHeader(ByteBuf in) {
        in.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES); // skip oxm_class
        boolean hasMask = (in.readUnsignedByte() & 1) != 0;
        in.skipBytes(EncodeConstants.SIZE_OF_BYTE_IN_BYTES); // skip match entry length
        return hasMask;
    }

    /**
     * Read Ipv4Prefix from message
     * @param message buffered message
     * @param hasMask determines if prefix has mask or not
     * @return IPv4 prefix
     */
    protected static Ipv4Prefix readPrefix(ByteBuf message, boolean hasMask) {
        final Ipv4Address address = ByteBufUtils.readIetfIpv4Address(message);
        int mask = 32;

        if (hasMask) {
            mask = IpConversionUtil.countBits(
                    OxmDeserializerHelper.convertMask(message, EncodeConstants.GROUPS_IN_IPV4_ADDRESS));
        }

        return IpConversionUtil.createPrefix(address, mask);
    }

}
