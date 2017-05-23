/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializer;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public abstract class AbstractMatchEntrySerializer implements HeaderSerializer<Match>, MatchEntrySerializer {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        serializeHeader(match, outBuffer);
    }

    @Override
    public void serializeHeader(Match match, ByteBuf outBuffer) {
        outBuffer.writeShort(getOxmClassCode());

        int fieldAndMask = getOxmFieldCode() << 1;
        int length = getValueLength();

        if (getHasMask(match)) {
            fieldAndMask |= 1;
            length *= 2;
        }

        outBuffer.writeByte(fieldAndMask);
        outBuffer.writeByte(length);
    }

    /**
     * Serialize byte mask to bytes. checking for mask length
     * @param mask byte mask
     * @param outBuffer output buffer
     * @param length mask length
     */
    protected static void writeMask(byte[] mask, ByteBuf outBuffer, int length) {
        if (mask != null && mask.length != length) {
            throw new IllegalArgumentException("incorrect length of mask: "+
                    mask.length + ", expected: " + length);
        }

        outBuffer.writeBytes(mask);
    }

    /**
     * Serialize Ipv4 address to bytes
     * @param address Ipv4 address
     * @param outBuffer output buffer
     */
    protected static void writeIpv4Address(final Ipv4Address address, final ByteBuf outBuffer) {
        outBuffer.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(address));
    }

    /**
     * Serialize Ipv6 address to bytes
     * @param address Ipv6 address
     * @param outBuffer output buffer
     */
    protected static void writeIpv6Address(final Ipv6Address address, final ByteBuf outBuffer) {
        outBuffer.writeBytes(IetfInetUtil.INSTANCE.ipv6AddressBytes(address));
    }

    /**
     * Serialize Mac address to bytes
     * @param address Mac address
     * @param outBuffer output buffer
     */
    protected static void writeMacAddress(final MacAddress address, final ByteBuf outBuffer) {
        outBuffer.writeBytes(IetfYangUtil.INSTANCE.bytesFor(address)); // 48 b + mask [OF 1.3.2 spec]
    }

    /**
     * Serialize Ipv4 prefix (address and mask)
     * @param prefix Ipv4 prefix
     * @param outBuffer output buffer
     */
    protected static void writeIpv4Prefix(final Ipv4Prefix prefix, final ByteBuf outBuffer) {
        // Split address to IP and mask
        final Iterator<String> addressParts = IpConversionUtil.splitToParts(prefix);

        // Write address part of prefix
        writeIpv4Address(new Ipv4Address(addressParts.next()), outBuffer);

        // If prefix had mask, also write prefix
        Optional.ofNullable(MatchConvertorUtil.extractIpv4Mask(addressParts)).ifPresent(mask ->
                writeMask(mask, outBuffer, EncodeConstants.GROUPS_IN_IPV4_ADDRESS));
    }

    /**
     * Serialize Ipv6 prefix (address and mask)
     * @param prefix Ipv6 prefix
     * @param outBuffer output buffer
     */
    protected static void writeIpv6Prefix(final Ipv6Prefix prefix, final ByteBuf outBuffer) {
        // Write address part of prefix
        writeIpv6Address(IpConversionUtil.extractIpv6Address(prefix), outBuffer);

        // If prefix had mask, also write prefix
        Optional.ofNullable(IpConversionUtil.hasIpv6Prefix(prefix)).ifPresent(mask ->
                writeMask(IpConversionUtil.convertIpv6PrefixToByteArray(mask), outBuffer,
                        EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES));
    }

    /**
     * @param match Openflow match
     * @return if field has or has not mask
     */
    protected abstract boolean getHasMask(final Match match);

    /**
     * @return numeric representation of oxm_field
     */
    protected abstract int getOxmFieldCode();

    /**
     * @return numeric representation of oxm_class
     */
    protected abstract int getOxmClassCode();

    /**
     * @return match entry value length (without mask length)
     */
    protected abstract int getValueLength();
}
