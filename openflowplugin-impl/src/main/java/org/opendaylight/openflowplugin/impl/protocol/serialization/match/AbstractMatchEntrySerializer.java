/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializer;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

/**
 * Abstract base class for conveniently implementing {@link MatchEntrySerializer}.
 *
 * @param <E> Match entry type
 * @param <M> Match entry mask type, use Void a mask is not applicable
 */
public abstract class AbstractMatchEntrySerializer<E, M> implements MatchEntrySerializer {
    /**
     * Base class supporting writing out a particular match entry's header. This class should be subclassed only
     * in case the header contents depends on the match entry content in a more dynamic fashion than the presence
     * or absence of the mask. In all other cases using {@link ConstantHeaderWriter} is preferable.
     *
     * @param <E> Match entry type
     * @param <M> Match entry mask type, use Void a mask is not applicable
     */
    protected abstract static class HeaderWriter<E, M> {

        /**
         * Write out the header for a particular entry, containing specified mask, to the provided output buffer.
         *
         * @param entry match entry for which to write the header
         * @param mask mask as extracted from the match entry, may be null
         * @param outBuffer output buffer
         */
        protected abstract void writeHeader(@NonNull E entry, @Nullable M mask, @NonNull ByteBuf outBuffer);

        protected static final void writeHeader(final int oxmClassCode, final int oxmFieldCode, final int valueLength,
                final boolean hasMask, final ByteBuf outBuffer) {
            writeHeader(oxmClassCode, oxmFieldCode, valueLength, hasMask, 0, outBuffer);
        }

        protected static final void writeHeader(final int oxmClassCode, final int oxmFieldCode, final int valueLength,
                final boolean hasMask, final int extraLength, final ByteBuf outBuffer) {
            outBuffer.writeShort(oxmClassCode);

            int fieldAndMask = oxmFieldCode << 1;
            int length = valueLength;

            if (hasMask) {
                fieldAndMask |= 1;
                length *= 2;
            }

            outBuffer.writeByte(fieldAndMask);
            outBuffer.writeByte(length + extraLength);
        }
    }

    /**
     * Utility {@link HeaderWriter} optimized for cases where the header does not depend on the actual entry content
     * beyond presence/absence of a mask. This class pre-computes the possible header values for masked/unmasked cases
     * and stores them internally for reuse.
     *
     * @param <E> Match entry type
     * @param <M> Match entry mask type, use Void a mask is not applicable
     */
    /*
     * Implementation note:
     *
     * While it looks like we could save some memory by refactoring this class into two instances for the non-mask
     * and mask values, that actually would result in increased memory footprint because the JVM object header is
     * larger than the state we keep. We would also require another reference field in the serializer itself, which
     * would expand the size of that object, negating the benefit.
     *
     * Another refactor would see the case where an entry cannot have a mask split out into a separate class, making
     * that specialized object smaller and not contain the null check in writeHeader(). This can be considered, but
     * has to be thoroughly benchmarked with representative data, because introducing another implementation means
     * calls to writeHeader() are no longer monomorphic and are not as readily devirtualizable -- which has performance
     * implications which can easily negate any behefits from the specialization.
     */
    protected static final class ConstantHeaderWriter<E, M> extends HeaderWriter<E, M> {
        private final int withMask;
        private final int withoutMask;

        protected ConstantHeaderWriter(final int withMask, final int withoutMask) {
            this.withMask = withMask;
            this.withoutMask = withoutMask;
        }

        protected ConstantHeaderWriter(final int oxmClassCode, final int oxmFieldCode, final int valueLength) {
            this(oxmClassCode, oxmFieldCode, valueLength, 0);
        }

        protected ConstantHeaderWriter(final int oxmClassCode, final int oxmFieldCode, final int valueLength,
                final int extraLength) {
            this(constructHeader(oxmClassCode, oxmFieldCode, valueLength, extraLength, true),
                constructHeader(oxmClassCode, oxmFieldCode, valueLength, extraLength, false));
        }

        @Override
        protected void writeHeader(final E entry, final M mask, final ByteBuf outBuffer) {
            outBuffer.writeInt(mask != null ? withMask : withoutMask);
        }

        private static int constructHeader(final int oxmClassCode, final int oxmFieldCode, final int valueLength,
                final int extraLength, final boolean withMask) {
            final ByteBuf buf = Unpooled.buffer();
            writeHeader(oxmClassCode, oxmFieldCode, valueLength, withMask, extraLength, buf);
            final int header = buf.readInt();
            verify(buf.readableBytes() == 0);
            return header;
        }
    }

    private final HeaderWriter<E, M> headerWriter;

    protected AbstractMatchEntrySerializer(final HeaderWriter<E, M> headerWriter) {
        this.headerWriter = requireNonNull(headerWriter);
    }

    protected AbstractMatchEntrySerializer(final int oxmClassCode, final int oxmFieldCode, final int valueLength) {
        this(new ConstantHeaderWriter<>(oxmClassCode, oxmFieldCode, valueLength));
    }

    @Override
    public final void serializeIfPresent(final Match match, final ByteBuf outBuffer) {
        final E entry = extractEntry(match);
        if (entry != null) {
            final M mask = extractEntryMask(entry);
            headerWriter.writeHeader(entry, mask, outBuffer);
            serializeEntry(entry, mask, outBuffer);
        }
    }

    /**
     * Serialize byte mask to bytes. checking for mask length.
     *
     * @param mask byte mask
     * @param outBuffer output buffer
     * @param length mask length
     */
    protected static void writeMask(final byte[] mask, final ByteBuf outBuffer, final int length) {
        if (mask != null) {
            if (mask.length != length) {
                throw new IllegalArgumentException("incorrect length of mask: " + mask.length + ", expected: "
                        + length);
            }
            outBuffer.writeBytes(mask);
        }
    }

    /**
     * Serialize Ipv4 address to bytes.
     *
     * @param address Ipv4 address
     * @param outBuffer output buffer
     */
    protected static void writeIpv4Address(final Ipv4Address address, final ByteBuf outBuffer) {
        outBuffer.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(address));
    }

    /**
     * Serialize Ipv6 address to bytes.
     *
     * @param address Ipv6 address
     * @param outBuffer output buffer
     */
    protected static void writeIpv6Address(final Ipv6Address address, final ByteBuf outBuffer) {
        outBuffer.writeBytes(IetfInetUtil.INSTANCE.ipv6AddressBytes(address));
    }

    /**
     * Serialize Mac address to bytes.
     *
     * @param address Mac address
     * @param outBuffer output buffer
     */
    protected static void writeMacAddress(final MacAddress address, final ByteBuf outBuffer) {
        // 48 b + mask [OF 1.3.2 spec]
        outBuffer.writeBytes(IetfYangUtil.INSTANCE.bytesFor(address));
    }

    /**
     * Serialize Ipv4 prefix (address and mask).
     *
     * @param prefix Ipv4 prefix
     * @param outBuffer output buffer
     */
    protected static void writeIpv4Prefix(final @NonNull Ipv4Prefix prefix, final @Nullable Integer mask,
            final @NonNull ByteBuf outBuffer) {
        // Write address part of prefix
        writeIpv4Address(IetfInetUtil.INSTANCE.ipv4AddressFrom(prefix), outBuffer);

        // If prefix had mask, also write prefix
        if (mask != null) {
            outBuffer.writeInt(IpConversionUtil.maskForIpv4Prefix(mask));
        }
    }

    /**
     * Serialize Ipv6 prefix (address and mask).
     *
     * @param prefix Ipv6 prefix
     * @param outBuffer output buffer
     */
    protected static void writeIpv6Prefix(final @NonNull Ipv6Prefix prefix, final @Nullable Integer mask,
            final @NonNull ByteBuf outBuffer) {
        // Write address part of prefix
        writeIpv6Address(IpConversionUtil.extractIpv6Address(prefix), outBuffer);

        // If prefix had mask, also write prefix
        if (mask != null) {
            writeMask(IpConversionUtil.convertIpv6PrefixToByteArray(mask), outBuffer,
                EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES);
        }
    }

    /**
     * Extract the corresponding entry from a match.
     *
     * @param match Openflow match
     * @return Entry, null if not present
     */
    protected abstract @Nullable E extractEntry(Match match);

    /**
     * Extract the mask contained in an entry.
     *
     * @param entry entry to examine
     * @return Mask, null if not present
     */
    protected abstract @Nullable M extractEntryMask(@NonNull E entry);

    /**
     * Extract the corresponding entry from a match.
     *
     * @param entry entry to serialize
     * @param mask mask as extracted from entry
     * @param outBuffer output buffer
     */
    protected abstract void serializeEntry(@NonNull E entry, @Nullable M mask, @NonNull ByteBuf outBuffer);
}
