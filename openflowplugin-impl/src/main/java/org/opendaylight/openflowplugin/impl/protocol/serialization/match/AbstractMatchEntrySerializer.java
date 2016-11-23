/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;

public abstract class AbstractMatchEntrySerializer implements OFSerializer<Match>, HeaderSerializer<Match> {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
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

    @Override
    public void serializeHeader(Match match, ByteBuf outBuffer) {
        serialize(match, outBuffer);
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
     * Checks if current match is this match type
     * @param match Openflow match
     * @return true if matched
     */
    abstract boolean matchTypeCheck(final Match match);

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
