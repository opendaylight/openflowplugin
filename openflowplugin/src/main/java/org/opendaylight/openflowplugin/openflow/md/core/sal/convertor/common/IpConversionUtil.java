/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import com.google.common.primitives.UnsignedBytes;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IpConversionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(IpConversionUtil.class);
    public static final String PREFIX_SEPARATOR = "/";
    public static final Splitter PREFIX_SPLITTER = Splitter.on('/');
    private static final Splitter PREFIX_TRIM_SPLITTER = PREFIX_SPLITTER.trimResults().omitEmptyStrings();
    private static final Splitter PERCENT_SPLITTER = Splitter.on('%').trimResults().omitEmptyStrings();
    private static final Pattern BITMASK_SPLIT_PATTERN = Pattern.compile("(?!^)");
    private static final int INADDR4SZ = 4;
    private static final int INADDR6SZ = 16;
    private static final int INT16SZ = 2;
    private static final int IPV4_ADDRESS_LENGTH = 32;
    private static final int IPV6_ADDRESS_LENGTH = 128;
    private static final Ipv6ArbitraryMask DEFAULT_IPV6_ARBITRARY_BITMASK =
            new Ipv6ArbitraryMask("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");

    /*
     * Prefix bytearray lookup table. We concatenate the prefixes
     * to a single byte array and perform offset lookups to ensure
     * the table is contiguous and save some space.
     */
    private static final byte[] PREFIX_BYTEARRAYS;

    static {
        final byte[] a = new byte[(INADDR6SZ * Byte.SIZE + 1) * INADDR6SZ];

        int offset = 0;
        for (int p = 0; p <= INADDR6SZ * Byte.SIZE; ++p) {
            int prefix = p;
            for (int i = 0; i < INADDR6SZ; ++i) {
                a[offset++] = (byte) nextNibble(prefix);
                prefix -= Byte.SIZE;
            }
        }

        PREFIX_BYTEARRAYS = a;
    }

    private static final DottedQuad[] IPV4_BITMASKS;

    static {
        final DottedQuad[] quads = new DottedQuad[IPV4_ADDRESS_LENGTH + 1];

        for (int i = 0; i <= IPV4_ADDRESS_LENGTH; ++i) {
            final int maskBits = maskForIpv4Prefix(i);
            quads[i] = new DottedQuad(new StringBuilder(15)
                .append(maskBits >>> 24).append('.')
                .append(maskBits >>> 16 & 0xff).append('.')
                .append(maskBits >>>  8 & 0xff).append('.')
                .append(maskBits & 0xff).toString());
        }

        IPV4_BITMASKS = quads;
    }


    private IpConversionUtil() {
        throw new UnsupportedOperationException("This class should not be instantiated.");
    }

    public static Iterator<String> splitToParts(final Ipv4Prefix ipv4Prefix) {
        return PREFIX_SPLITTER.split(ipv4Prefix.getValue()).iterator();
    }

    public static Iterator<String> splitToParts(final Ipv6Prefix ipv6Prefix) {
        return PREFIX_SPLITTER.split(ipv6Prefix.getValue()).iterator();
    }

    /* This forest of functions has a purpose:
     *
     * 1. There are multiple coding styles around the plugin, this is necessary in order to have
     *   one mechanism to convert them all, one mechanism to find them...
     * 2. I hope that one day yangtools will actually deliver code fit for purpose in a packet
     *   processing application (presently it is not. When this happens, these can be optimized
     *   for "side-load" of pre-vetted data. Example. IP Address (v4 or v6) is prevetted left of the
     *   prefix. It should be loadable into Prefix without _RERUNNING_ 100ms+ of regexps. When (and if)
     *   that happens, it will be a simple fix here without chasing it across the whole plugin.
    */

    public static Ipv4Prefix createPrefix(final Ipv4Address ipv4Address) {
        return IetfInetUtil.INSTANCE.ipv4PrefixFor(ipv4Address);
    }

    public static Ipv4Prefix createPrefix(final Ipv4Address ipv4Address, final String mask) {
        /*
         * Ipv4Address has already validated the address part of the prefix,
         * It is mandated to comply to the same regexp as the address
         * There is absolutely no point rerunning additional checks vs this
         * Note - there is no canonical form check here!!!
         */
        if (null != mask && !mask.isEmpty()) {
            return new Ipv4Prefix(ipv4Address.getValue() + PREFIX_SEPARATOR + mask);
        } else {
            return new Ipv4Prefix(ipv4Address.getValue() + PREFIX_SEPARATOR + IPV4_ADDRESS_LENGTH);
        }
    }

    public static Ipv4Prefix createPrefix(final Ipv4Address ipv4Address, final int intmask) {
        return IetfInetUtil.INSTANCE.ipv4PrefixFor(ipv4Address, intmask);
    }

    public static Ipv4Prefix createPrefix(final Ipv4Address ipv4Address, final byte [] bytemask) {
        if (bytemask == null) {
            return createPrefix(ipv4Address);
        }

        return IetfInetUtil.INSTANCE.ipv4PrefixFor(ipv4Address, countBits(bytemask));
    }

    public static Ipv6Prefix createPrefix(final Ipv6Address ipv6Address) {
        return IetfInetUtil.INSTANCE.ipv6PrefixFor(ipv6Address);
    }

    public static Ipv6Prefix createPrefix(final Ipv6Address ipv6Address, final String mask) {
        /*
         * Ipv6Address has already validated the address part of the prefix,
         * It is mandated to comply to the same regexp as the address
         * There is absolutely no point rerunning additional checks vs this
         * Note - there is no canonical form check here!!!
         */
        if (Strings.isNullOrEmpty(mask)) {
            return new Ipv6Prefix(ipv6Address.getValue() + PREFIX_SEPARATOR + String.valueOf(IPV6_ADDRESS_LENGTH));
        } else {
            return new Ipv6Prefix(ipv6Address.getValue() + PREFIX_SEPARATOR + mask);
        }
    }

    public static Ipv6Prefix createPrefix(final Ipv6Address ipv6Address, final int intmask) {
        return IetfInetUtil.INSTANCE.ipv6PrefixFor(ipv6Address, intmask);
    }

    public static Ipv6Prefix createPrefix(final Ipv6Address ipv6Address, final byte [] bytemask) {
        if (bytemask == null) {
            return createPrefix(ipv6Address);
        }

        return IetfInetUtil.INSTANCE.ipv6PrefixFor(ipv6Address, countBits(bytemask));
    }

    public static DottedQuad createArbitraryBitMask(final byte[] bitmask)  {
        if (bitmask == null) {
            return IPV4_BITMASKS[IPV4_ADDRESS_LENGTH];
        }

        final String hostAddress;
        try {
            hostAddress = InetAddress.getByAddress(bitmask).getHostAddress();
        } catch (UnknownHostException e) {
            LOG.error("Failed to create the dottedQuad notation for the given mask {}", Arrays.toString(bitmask), e);
            return null;
        }

        return new DottedQuad(hostAddress);
    }

    public static Ipv6ArbitraryMask createIpv6ArbitraryBitMask(final byte[] bitmask) {
        if (bitmask == null) {
            return DEFAULT_IPV6_ARBITRARY_BITMASK;
        }

        final String hostAddress;
        try {
            hostAddress = InetAddress.getByAddress(bitmask).getHostAddress();
        } catch (UnknownHostException e) {
            LOG.error("Failed to create the Ipv6ArbitraryMask notation for the given mask {}", Arrays.toString(bitmask),
                e);
            return null;
        }

        return new Ipv6ArbitraryMask(hostAddress);
    }

    public static Integer extractPrefix(final Ipv4Prefix ipv4Prefix) {
        return IetfInetUtil.INSTANCE.splitIpv4Prefix(ipv4Prefix).getValue();
    }

    public static Integer extractPrefix(final Ipv6Prefix ipv6Prefix) {
        return IetfInetUtil.INSTANCE.splitIpv6Prefix(ipv6Prefix).getValue();
    }

    public static Integer extractPrefix(final Ipv4Address ipv4Prefix) {
        return IPV4_ADDRESS_LENGTH;
    }

    public static Integer extractPrefix(final Ipv6Address ipv6Prefix) {
        return 128;
    }

    /**
     * Check if the supplied Ipv4Prefix has a prefix shorter than IPv4 address length.
     *
     * @param ipv4Prefix Ipv4 prefix
     * @return prefix if there is one, else null
     */
    public static Integer hasIpv4Prefix(final Ipv4Prefix ipv4Prefix) {
        return hasPrefix(extractPrefix(ipv4Prefix), IPV4_ADDRESS_LENGTH);
    }

    private static Integer hasPrefix(final Integer prefix, final int addressLength) {
        return prefix != null && prefix < addressLength ? prefix : null;
    }

    public static int maskForIpv4Prefix(final int prefixLength) {
        return (int) (0xffffffffL << IPV4_ADDRESS_LENGTH - prefixLength);
    }

    /*
     * BIG FAT WARNING!!!
     * Read all of the following before you touch any v6 code or decide to
     * optimize it by invoking a "simple" Guava call
     *
     * Java IPv6 is fundamentally broken and Google libraries do not fix it.
     * 1. Java will allways implicitly rewrite v4 mapped into v6 as a v4 address
     *      and there is absolutely no way to override this behaviour
     * 2. Guava libraries cannot parse non-canonical IPv6. They will throw an
     *      exception. Even if they did, they re-use the same broken java code
     *      underneath.
     *
     * This is why we have to parse v6 by ourselves.
     *
     * The following conversion code is based on inet_cidr_pton_ipv6 in NetBSD
     *
     * The original BSD code is licensed under standard BSD license. While we
     * are not obliged to provide an attribution, credit where credit is due.
     * As far as why it is similar to Sun's sun.net.util please ask Sun why
     * their code has the same variable names, comments and code flow.
     *
     */


     /**
     * Convert Ipv6Address object to a valid Canonical v6 address in byte format.
     *
     * @param ipv6Address - v6 Address object
     * @return - byte array of size 16. Last byte contains netmask
     */
    public static byte[] canonicalBinaryV6Address(final Ipv6Address ipv6Address) {
        /*
         * Do not modify this routine to take direct strings input!!!
         * Key checks have been removed based on the assumption that
         * the input is validated via regexps in Ipv6Prefix()
         */

        return canonicalBinaryV6AddressFromString(ipv6Address.getValue());
    }


    private static byte[] canonicalBinaryV6AddressFromString(final String ipv6Address) {
        List<String> partsV6Address = PERCENT_SPLITTER.splitToList(ipv6Address);

        int colonp;
        char ch;
        boolean sawXdigit;

        /* Isn't it fun - the above variable names are the same in BSD and Sun sources */

        char[] src = partsV6Address.get(0).toCharArray();

        byte[] dst = new byte[INADDR6SZ];

        colonp = -1;
        int index1 = 0;
        int index2 = 0;

        /* Leading :: requires some special handling. */

        /* Isn't it fun - the above comment is again the same in BSD and Sun sources,
         * We will derive our code from BSD. Shakespear always sounds better
         * in original Clingon. So does Dilbert.
         */

        if (src[index1] == ':') {
            Preconditions.checkArgument(src[++index1] == ':', "Invalid v6 address");
        }

        int curtok = index1;
        sawXdigit = false;

        int srcLength = src.length;
        int val = 0;
        while (index1 < srcLength) {
            ch = src[index1++];
            int chval = Character.digit(ch, 16);

            /* Business as usual - ipv6 address digit.
             * We can remove all checks from the original BSD code because
             * the regexp has already verified that we are not being fed
             * anything bigger than 0xffff between the separators.
             */

            if (chval != -1) {
                val <<= 4;
                val |= chval;
                sawXdigit = true;
                continue;
            }

            /* v6 separator */

            if (ch == ':') {
                curtok = index1;
                if (!sawXdigit) {
                    /* no need to check separator position validity - regexp does that */
                    colonp = index2;
                    continue;
                }

                /* removed overrun check - the regexp checks for valid data */

                dst[index2++] = (byte) (val >>> 8 & 0xff);
                dst[index2++] = (byte) (val & 0xff);
                sawXdigit = false;
                val = 0;
                continue;
            }

            /* frankenstein - v4 attached to v6, mixed notation */

            if (ch == '.' && index2 + INADDR4SZ <= INADDR6SZ) {

                /* this has passed the regexp so it is fairly safe to parse it
                 * straight away. As v4 addresses do not suffer from the same
                 * defficiencies as the java v6 implementation we can invoke it
                 * straight away and be done with it
                 */

                Preconditions.checkArgument(index2 != INADDR6SZ - INADDR4SZ - 1, "Invalid v4 in v6 mapping");

                InetAddress inetForm = InetAddresses.forString(partsV6Address.get(0).substring(curtok, srcLength));

                Preconditions.checkArgument(inetForm instanceof Inet4Address);
                System.arraycopy(inetForm.getAddress(), 0, dst, index2, INADDR4SZ);
                index2 += INADDR4SZ;

                sawXdigit = false;
                break;
            }
            /* removed parser exit on invalid char - no need to do it, regexp checks it */
        }
        if (sawXdigit) {
            Preconditions.checkArgument(index2 + INT16SZ <= INADDR6SZ, "Overrun in v6 parsing, should not occur");
            dst[index2++] = (byte) (val >> 8 & 0xff);
            dst[index2++] = (byte) (val & 0xff);
        }

        if (colonp != -1) {
            int to = index2 - colonp;

            Preconditions.checkArgument(index2 != INADDR6SZ, "Overrun in v6 parsing, should not occur");
            for (index1 = 1; index1 <= to; index1++) {
                dst[INADDR6SZ - index1] = dst[colonp + to - index1];
                dst[colonp + to - index1] = 0;
            }
            index2 = INADDR6SZ;
        }

        Preconditions.checkArgument(index2 == INADDR6SZ, "Overrun in v6 parsing, should not occur");

        return dst;
    }

    public static String byteArrayV6AddressToString(final byte [] binaryForm) throws UnknownHostException {
        /* DO NOT DIY!!! - InetAddresses will actually print correct canonical
         * zero compressed form.
         */
        return InetAddresses.toAddrString(InetAddress.getByAddress(binaryForm));
    }

    private static int nextNibble(final int mask) {
        if (mask <= 0) {
            return 0;
        }
        if (mask > 8) {
            return 0xff;
        }
        return 0xff << 8 - mask;
    }

    /**
     * Convert Ipv6Prefix object to a valid Canonical v6 prefix in byte format.
     *
     * @param ipv6Prefix - v6 prefix object
     * @return - byte array of size 16 + 1. Last byte contains netmask
     */
    public static byte[] canonicalBinaryV6Prefix(final Ipv6Prefix ipv6Prefix) {
        /*
         * Do not modify this routine to take direct strings input!!!
         * Key checks have been removed based on the assumption that
         * the input is validated via regexps in Ipv6Prefix()
         */

        int initialMask = 128;

        List<String> partsV6Prefix = PREFIX_TRIM_SPLITTER.splitToList(ipv6Prefix.getValue());

        boolean valid = true;

        try {
            initialMask = Integer.parseInt(partsV6Prefix.get(1));
            if (initialMask > 128) {
                valid = false;
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            valid = false;
        }

        Preconditions.checkArgument(valid, "Supplied netmask in %s is invalid", ipv6Prefix.getValue());


        int colonp;
        char ch;
        boolean sawXdigit;

        /* Isn't it fun - the above variable names are the same in BSD and Sun sources */

        char[] src = partsV6Prefix.get(0).toCharArray();

        byte[] dst = new byte[INADDR6SZ + 1];

        int mask = initialMask;

        colonp = -1;
        int index1 = 0;
        int index2 = 0;

        /* Leading :: requires some special handling. */

        /* Isn't it fun - the above comment is again the same in BSD and Sun sources,
         * We will derive our code from BSD. Shakespear always sounds better
         * in original Clingon. So does Dilbert.
         */

        if (src[index1] == ':') {
            Preconditions.checkArgument(src[++index1] == ':', "Invalid v6 address");
        }

        int curtok = index1;
        sawXdigit = false;

        int srcLength = src.length;
        int val = 0;
        while (index1 < srcLength) {
            ch = src[index1++];
            int chval = Character.digit(ch, 16);

            /* Business as usual - ipv6 address digit.
             * We can remove all checks from the original BSD code because
             * the regexp has already verified that we are not being fed
             * anything bigger than 0xffff between the separators.
             */

            if (chval != -1) {
                val <<= 4;
                val |= chval;
                sawXdigit = true;
                continue;
            }

            /* v6 separator */

            if (ch == ':') {
                curtok = index1;
                if (!sawXdigit) {
                    /* no need to check separator position validity - regexp does that */
                    colonp = index2;
                    continue;
                }

                /* removed overrun check - the regexp checks for valid data */

                sawXdigit = false;

                if (mask < 0) {
                    /* stop parsing if we are past the mask */
                    break;
                }

                dst[index2] = (byte) (val >> 8 & nextNibble(mask));
                index2++;
                mask = mask - 8;

                if (mask < 0) {
                    /* stop parsing if we are past the mask */
                    break;
                }

                dst[index2] = (byte) (val & nextNibble(mask));
                index2++;
                mask = mask - 8;

                val = 0;
                continue;
            }

            /* frankenstein - v4 attached to v6, mixed notation */

            if (ch == '.' && index2 + INADDR4SZ <= INADDR6SZ) {

                /* this has passed the regexp so it is fairly safe to parse it
                 * straight away. As v4 addresses do not suffer from the same
                 * defficiencies as the java v6 implementation we can invoke it
                 * straight away and be done with it
                 */

                Preconditions.checkArgument(index2 != INADDR6SZ - INADDR4SZ - 1, "Invalid v4 in v6 mapping");

                InetAddress inetForm = InetAddresses.forString(partsV6Prefix.get(0).substring(curtok, srcLength));

                Preconditions.checkArgument(inetForm instanceof Inet4Address);
                System.arraycopy(inetForm.getAddress(), 0, dst, index2, INADDR4SZ);
                index2 +=  INADDR4SZ;

                sawXdigit = false;
                break;
            }
            /* removed parser exit on ivalid char - no need to do it, regexp checks it */
        }
        if (sawXdigit) {
            Preconditions.checkArgument(index2 + INT16SZ <= INADDR6SZ, "Overrun in v6 parsing, should not occur");
            dst[index2] = (byte) (val >> 8 & nextNibble(mask));
            index2++;
            mask = mask - 8;
            dst[index2] = (byte) (val & nextNibble(mask));
            index2++;
            mask = mask - 8;
        }

        if (index2 < INADDR6SZ && mask < 0) {
            /* past the mask */
            for (index1 = index2; index1 < INADDR6SZ; index1++) {
                dst[index1] = 0;
            }
        } else {
            /* normal parsing */
            if (colonp != -1) {
                int to = index2 - colonp;

                Preconditions.checkArgument(index2 != INADDR6SZ, "Overrun in v6 parsing, should not occur");
                for (index1 = 1; index1 <= to; index1++) {
                    dst[INADDR6SZ - index1] = dst[colonp + to - index1];
                    dst[colonp + to - index1] = 0;
                }
                index2 = INADDR6SZ;
            }
            Preconditions.checkArgument(index2 == INADDR6SZ, "Overrun in v6 parsing, should not occur");
        }

        dst[INADDR6SZ] = (byte) initialMask;
        return dst;
    }

    /**
     * Print a v6 prefix in byte array + 1 notation.
     *
     * @param binaryForm - prefix, in byte [] form, last byte is netmask
     * @return string of v6 prefix
     * @throws UnknownHostException unknown host exception
     */
    public static String byteArrayV6PrefixToString(final byte [] binaryForm) throws UnknownHostException {
        /* NO DIY!!! - InetAddresses will actually print correct canonical
         * zero compressed form
         */
        StringBuilder sb = new StringBuilder();
        /* Yang RFC specifies that the normalized form is RFC 5952, note - java
         * core type is not RFC compliant, guava is.
         */
        sb.append(
            InetAddresses.toAddrString(
                InetAddress.getByAddress(
                    Arrays.copyOf(binaryForm, INADDR6SZ)
                )
            )
        );
        sb.append('/');
        sb.append(binaryForm[INADDR6SZ] & 0xff);
        return sb.toString();
    }

    /**
     * Check if the supplied Ipv6Prefix has a prefix shorter than IPv6 address length.
     *
     * @param ipv6Prefix Ipv6 prefix
     * @return prefix if there is one, else null
     */
    public static Integer hasIpv6Prefix(final Ipv6Prefix ipv6Prefix) {
        return hasPrefix(extractIpv6Prefix(ipv6Prefix), IPV6_ADDRESS_LENGTH);
    }

    private static int ipv6PrefixByteArrayOffset(final int mask) {
        if (mask < 0) {
            return 0;
        }

        final int ret = mask * INADDR6SZ;
        if (ret < PREFIX_BYTEARRAYS.length) {
            return ret;
        } else {
            return PREFIX_BYTEARRAYS.length - INADDR6SZ;
        }
    }

    /**
     * Canonicalize a v6 prefix while in binary form.
     *
     * @param prefix - prefix, in byte [] form
     * @param mask - mask - number of bits
     */
    public static void canonicalizeIpv6Prefix(final byte [] prefix, final int mask) {
        final int offset = ipv6PrefixByteArrayOffset(mask);

        for (int i = 0; i < INADDR6SZ; i++) {
            prefix[i] &= PREFIX_BYTEARRAYS[offset + i];
        }
    }

    public static byte[] convertIpv6PrefixToByteArray(final int prefix) {
        final int offset = ipv6PrefixByteArrayOffset(prefix);

        return Arrays.copyOfRange(PREFIX_BYTEARRAYS, offset, offset + INADDR6SZ);
    }

    public static Ipv6Address extractIpv6Address(final Ipv6Prefix ipv6Prefix) {
        return IetfInetUtil.INSTANCE.ipv6AddressFrom(ipv6Prefix);
    }

    public static Ipv4Address extractIpv4Address(final Ipv4Prefix ipv4Prefix) {
        return IetfInetUtil.INSTANCE.ipv4AddressFrom(ipv4Prefix);
    }

    public static DottedQuad extractIpv4AddressMask(final Ipv4Prefix ipv4Prefix) {
        final String value = ipv4Prefix.getValue();
        return IPV4_BITMASKS[Integer.parseInt(value.substring(value.indexOf('/') + 1))];
    }

    @Nullable
    public static Ipv6ArbitraryMask extractIpv6AddressMask(final Ipv6Prefix ipv6Prefix) {
        Iterator<String> addressParts = PREFIX_SPLITTER.split(ipv6Prefix.getValue()).iterator();
        addressParts.next();
        int maskLength = 0;
        if (addressParts.hasNext()) {
            maskLength = Integer.parseInt(addressParts.next());
        }
        BitSet ipmask = new BitSet(128);
        ipmask.set(0,maskLength,true);
        ipmask.set(maskLength + 1,128,false);
        byte[] finalmask = new byte[16];
        System.arraycopy(ipmask.toByteArray(),0,finalmask,0,ipmask.toByteArray().length);
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByAddress(finalmask);
        } catch (UnknownHostException e) {
            LOG.error("Failed to convert the Ipv6 subnetmask from integer to mask value ", e);
            return null;
        }
        return new Ipv6ArbitraryMask(inetAddress.getHostAddress());
    }

    public static Integer extractIpv6Prefix(final Ipv6Prefix ipv6Prefix) {
        return IetfInetUtil.INSTANCE.splitIpv6Prefix(ipv6Prefix).getValue();
    }

    public static int countBits(final byte[] mask) {
        int netmask = 0;
        for (byte b : mask) {
            netmask += Integer.bitCount(UnsignedBytes.toInt(b));
        }
        return netmask;
    }

    @Nullable
    @SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    public static byte[] convertArbitraryMaskToByteArray(final DottedQuad mask) {
        final String maskValue;
        if (mask != null && mask.getValue() != null) {
            maskValue = mask.getValue();
        } else {
            maskValue = IPV4_BITMASKS[IPV4_ADDRESS_LENGTH].getValue();
        }

        final InetAddress maskInIpFormat;
        try {
            maskInIpFormat = InetAddress.getByName(maskValue);
        } catch (UnknownHostException e) {
            LOG.error("Failed to resolve the ip address of the mask ", e);
            return null;
        }
        byte[] bytes = maskInIpFormat.getAddress();
        return bytes;
    }

    public static boolean isArbitraryBitMask(final byte[] byteMask) {
        return isArbitraryBitMask(byteMask, IPV4_ADDRESS_LENGTH);
    }

    private static boolean isArbitraryBitMask(final byte[] byteMask, final int addressLength) {
        if (byteMask == null) {
            return false;
        }

        ArrayList<Integer> integerMaskArrayList = new ArrayList<>();
        String maskInBits;
        // converting byte array to bits
        maskInBits = new BigInteger(1, byteMask).toString(2);
        for (String string : BITMASK_SPLIT_PATTERN.split(maskInBits)) {
            integerMaskArrayList.add(Integer.parseInt(string));
        }

        final int size = integerMaskArrayList.size();
        // checks 0*1* case - Leading zeros in arrayList are truncated
        if (size > 0 && size < addressLength) {
            return true;
        }

        // checks 1*0*1 case
        for (int i = 0; i < size - 1; i++) {
            if (integerMaskArrayList.get(i) == 0 && integerMaskArrayList.get(i + 1) == 1) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    public static byte[] convertIpv6ArbitraryMaskToByteArray(final Ipv6ArbitraryMask mask) {
        final String maskValue;
        if (mask != null && mask.getValue() != null) {
            maskValue = mask.getValue();
        } else {
            maskValue = DEFAULT_IPV6_ARBITRARY_BITMASK.getValue();
        }

        final InetAddress maskInIpFormat;
        try {
            maskInIpFormat = InetAddress.getByName(maskValue);
        } catch (UnknownHostException e) {
            LOG.error("Failed to convert mask string to ipv6 format mask ",e);
            return null;
        }
        return maskInIpFormat.getAddress();
    }

    public static boolean isIpv6ArbitraryBitMask(final byte[] byteMask) {
        return isArbitraryBitMask(byteMask, IPV6_ADDRESS_LENGTH);
    }

    private static String compressedIpv6FormatFromString(final String ipv6Address) {
        try {
            return byteArrayV6AddressToString(canonicalBinaryV6AddressFromString(ipv6Address));
        } catch (UnknownHostException e) {
            LOG.warn("Failed to compress IPv6 address {} because it is invalid", ipv6Address);
            return ipv6Address;
        }
    }

    public static Ipv6Address compressedIpv6AddressFormat(final Ipv6Address ipv6Address) {
        return new Ipv6Address(compressedIpv6FormatFromString(ipv6Address.getValue()));
    }

    public static Ipv6ArbitraryMask compressedIpv6MaskFormat(final Ipv6ArbitraryMask ipv6Mask) {
        return new Ipv6ArbitraryMask(compressedIpv6FormatFromString(ipv6Mask.getValue()));
    }
}
