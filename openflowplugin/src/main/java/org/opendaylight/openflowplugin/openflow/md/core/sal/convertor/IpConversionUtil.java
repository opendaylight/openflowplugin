/*
 * Copyright (c) 2015 Cisco Systems, Inc., Brocade, Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.net.InetAddresses;
import com.google.common.primitives.UnsignedBytes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;


/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 5.3.2015.
 * v6 routines added by Anton Ivanov on 14.6.2015
 */
public final class IpConversionUtil {

    public static final String PREFIX_SEPARATOR = "/";
    public static final Splitter PREFIX_SPLITTER = Splitter.on('/');
    private static final int INADDR4SZ = 4;
    private static final int INADDR6SZ = 16;
    private static final int INT16SZ = 2;
    private static final int IPV4_ADDRESS_LENGTH = 32;
    private static final int IPV6_ADDRESS_LENGTH = 128;
    private static final String DEFAULT_ARBITRARY_BIT_MASK = "255.255.255.255";

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

    private IpConversionUtil() {
        throw new UnsupportedOperationException("This class should not be instantiated.");
    }

    public static Iterator<String> splitToParts(final Ipv4Prefix ipv4Prefix) {
        return PREFIX_SPLITTER.split(ipv4Prefix.getValue()).iterator();
    }

    public static Iterator<String> splitToParts(final Ipv4Address ipv4Address) {
        /* Invalid (Ab)use of ip address as prefix!!! */
        return Iterators.forArray(ipv4Address.getValue(), String.valueOf(IPV4_ADDRESS_LENGTH));
    }

    public static Iterator<String> splitToParts(final Ipv6Prefix ipv6Prefix) {
        return PREFIX_SPLITTER.split(ipv6Prefix.getValue()).iterator();
    }

    public static Iterator<String> splitToParts(final Ipv6Address ipv6Address) {
        /* Invalid (Ab)use of ip address as prefix!!! */
        return Iterators.forArray(ipv6Address.getValue(), String.valueOf(IPV6_ADDRESS_LENGTH));
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

    public static Ipv4Prefix createPrefix(final Ipv4Address ipv4Address){
        return new Ipv4Prefix(ipv4Address.getValue() + PREFIX_SEPARATOR + IPV4_ADDRESS_LENGTH);
    }

    public static Ipv4Prefix createPrefix(final Ipv4Address ipv4Address, final String mask){
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

    public static Ipv4Prefix createPrefix(final Ipv4Address ipv4Address, final int intmask){
        return createPrefix(ipv4Address, String.valueOf(intmask));
    }

    public static Ipv4Prefix createPrefix(final Ipv4Address ipv4Address, final byte [] bytemask){
        return createPrefix(ipv4Address, String.valueOf(countBits(bytemask)));
    }

    public static Ipv6Prefix createPrefix(final Ipv6Address ipv6Address){
        return new Ipv6Prefix(ipv6Address.getValue() + PREFIX_SEPARATOR + IPV6_ADDRESS_LENGTH);
    }

    public static Ipv6Prefix createPrefix(final Ipv6Address ipv6Address, final String mask){
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

    public static Ipv6Prefix createPrefix(final Ipv6Address ipv6Address, final int intmask){
        return createPrefix(ipv6Address, String.valueOf(intmask));
    }

    public static Ipv6Prefix createPrefix(final Ipv6Address ipv6Address, final byte [] bytemask){
        /*
         * Ipv4Address has already validated the address part of the prefix,
         * It is mandated to comply to the same regexp as the address
         * There is absolutely no point rerunning additional checks vs this
         * Note - there is no canonical form check here!!!
         */
         return createPrefix(ipv6Address, String.valueOf(countBits(bytemask)));
    }

    public static Integer extractPrefix(final Ipv4Prefix ipv4Prefix) {
        Iterator<String> addressParts = splitToParts(ipv4Prefix);
        addressParts.next();
        Integer retval = null;
        if (addressParts.hasNext()) {
            retval = Integer.parseInt(addressParts.next());
        }
        return retval;
    }

    public static Integer extractPrefix(final Ipv6Prefix ipv6Prefix) {
        Iterator<String> addressParts = splitToParts(ipv6Prefix);
        addressParts.next();
        Integer retval = null;
        if (addressParts.hasNext()) {
            retval = Integer.parseInt(addressParts.next());
        }
        return retval;
    }

    public static Integer extractPrefix(final Ipv4Address ipv4Prefix) {
        return IPV4_ADDRESS_LENGTH;
    }

    public static Integer extractPrefix(final Ipv6Address ipv6Prefix) {
        return 128;
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
     * Convert Ipv6Address object to a valid Canonical v6 address in byte format
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

        String [] address =  (ipv6Address.getValue()).split("%");

        int colonp;
        char ch;
        boolean saw_xdigit;

        /* Isn't it fun - the above variable names are the same in BSD and Sun sources */

        int val;

        char[] src = address[0].toCharArray();

        byte[] dst = new byte[INADDR6SZ];

        int src_length = src.length;

        colonp = -1;
        int i = 0, j = 0;

        /* Leading :: requires some special handling. */

        /* Isn't it fun - the above comment is again the same in BSD and Sun sources,
         * We will derive our code from BSD. Shakespear always sounds better
         * in original Clingon. So does Dilbert.
         */

        if (src[i] == ':') {
            Preconditions.checkArgument(src[++i] == ':', "Invalid v6 address");
        }

        int curtok = i;
        saw_xdigit = false;


        val = 0;
        while (i < src_length) {
            ch = src[i++];
            int chval = Character.digit(ch, 16);

            /* Business as usual - ipv6 address digit.
             * We can remove all checks from the original BSD code because
             * the regexp has already verified that we are not being fed
             * anything bigger than 0xffff between the separators.
             */

            if (chval != -1) {
                val <<= 4;
                val |= chval;
                saw_xdigit = true;
                continue;
            }

            /* v6 separator */

            if (ch == ':') {
                curtok = i;
                if (!saw_xdigit) {
                    /* no need to check separator position validity - regexp does that */
                    colonp = j;
                    continue;
                }

                /* removed overrun check - the regexp checks for valid data */

                dst[j++] = (byte) ((val >>> 8) & 0xff);
                dst[j++] = (byte) (val & 0xff);
                saw_xdigit = false;
                val = 0;
                continue;
            }

            /* frankenstein - v4 attached to v6, mixed notation */

            if (ch == '.' && ((j + INADDR4SZ) <= INADDR6SZ)) {

                /* this has passed the regexp so it is fairly safe to parse it
                 * straight away. As v4 addresses do not suffer from the same
                 * defficiencies as the java v6 implementation we can invoke it
                 * straight away and be done with it
                 */

                Preconditions.checkArgument(j != (INADDR6SZ - INADDR4SZ - 1), "Invalid v4 in v6 mapping");

                InetAddress _inet_form = InetAddresses.forString(address[0].substring(curtok, src_length));

                Preconditions.checkArgument(_inet_form instanceof Inet4Address);
                System.arraycopy(_inet_form.getAddress(), 0, dst, j, INADDR4SZ);
                j += INADDR4SZ;

                saw_xdigit = false;
                break;
            }
            /* removed parser exit on invalid char - no need to do it, regexp checks it */
        }
        if (saw_xdigit) {
            Preconditions.checkArgument(j + INT16SZ <= INADDR6SZ, "Overrun in v6 parsing, should not occur");
            dst[j++] = (byte) ((val >> 8) & 0xff);
            dst[j++] = (byte) (val & 0xff);
        }

        if (colonp != -1) {
            int n = j - colonp;

            Preconditions.checkArgument(j != INADDR6SZ, "Overrun in v6 parsing, should not occur");
            for (i = 1; i <= n; i++) {
                dst[INADDR6SZ - i] = dst[colonp + n - i];
                dst[colonp + n - i] = 0;
            }
            j = INADDR6SZ;
        }

        Preconditions.checkArgument(j == INADDR6SZ, "Overrun in v6 parsing, should not occur");

        return dst;
    }

    public static String byteArrayV6AddressToString (final byte [] _binary_form) throws UnknownHostException{
        /* DO NOT DIY!!! - InetAddresses will actually print correct canonical
         * zero compressed form.
         */
        return InetAddresses.toAddrString(InetAddress.getByAddress(_binary_form));
    }

    private static int nextNibble(final int mask) {
        if (mask <= 0) {
            return 0;
        }
        if (mask > 8) {
            return 0xff;
        }
        return 0xff << (8 - mask);
    }

    /**
     * Convert Ipv6Prefix object to a valid Canonical v6 prefix in byte format
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

        int mask = 128;

        String [] address = null;

        boolean valid = true;

        address =  (ipv6Prefix.getValue()).split("/");
        try {
            mask = Integer.parseInt(address[1]);
            if (mask > 128) {
                valid = false;
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            valid = false;
        }

        Preconditions.checkArgument(valid, "Supplied netmask in %s is invalid", ipv6Prefix.getValue());


        int colonp;
        char ch;
        boolean saw_xdigit;

        /* Isn't it fun - the above variable names are the same in BSD and Sun sources */

        int val;

        char[] src = address[0].toCharArray();

        byte[] dst = new byte[INADDR6SZ + 1];

        int m = mask;

        int src_length = src.length;

        colonp = -1;
        int i = 0, j = 0;

        /* Leading :: requires some special handling. */

        /* Isn't it fun - the above comment is again the same in BSD and Sun sources,
         * We will derive our code from BSD. Shakespear always sounds better
         * in original Clingon. So does Dilbert.
         */

        if (src[i] == ':') {
            Preconditions.checkArgument(src[++i] == ':', "Invalid v6 address");
        }

        int curtok = i;
        saw_xdigit = false;


        val = 0;
        while (i < src_length) {
            ch = src[i++];
            int chval = Character.digit(ch, 16);

            /* Business as usual - ipv6 address digit.
             * We can remove all checks from the original BSD code because
             * the regexp has already verified that we are not being fed
             * anything bigger than 0xffff between the separators.
             */

            if (chval != -1) {
                val <<= 4;
                val |= chval;
                saw_xdigit = true;
                continue;
            }

            /* v6 separator */

            if (ch == ':') {
                curtok = i;
                if (!saw_xdigit) {
                    /* no need to check separator position validity - regexp does that */
                    colonp = j;
                    continue;
                }

                /* removed overrun check - the regexp checks for valid data */

                saw_xdigit = false;

                if (m < 0) {
                    /* stop parsing if we are past the mask */
                    break;
                }

                dst[j] = (byte) ((val >> 8) & nextNibble(m)); j++; m = m - 8;

                if (m < 0) {
                    /* stop parsing if we are past the mask */
                    break;
                }

                dst[j] = (byte) (val & nextNibble(m)); j++; m = m - 8;

                val = 0;
                continue;
            }

            /* frankenstein - v4 attached to v6, mixed notation */

            if (ch == '.' && ((j + INADDR4SZ) <= INADDR6SZ)) {

                /* this has passed the regexp so it is fairly safe to parse it
                 * straight away. As v4 addresses do not suffer from the same
                 * defficiencies as the java v6 implementation we can invoke it
                 * straight away and be done with it
                 */

                Preconditions.checkArgument(j != (INADDR6SZ - INADDR4SZ - 1), "Invalid v4 in v6 mapping");

                InetAddress _inet_form = InetAddresses.forString(address[0].substring(curtok, src_length));

                Preconditions.checkArgument(_inet_form instanceof Inet4Address);
                System.arraycopy(_inet_form.getAddress(), 0, dst, j, INADDR4SZ);
                j +=  INADDR4SZ;

                saw_xdigit = false;
                break;
            }
            /* removed parser exit on ivalid char - no need to do it, regexp checks it */
        }
        if (saw_xdigit) {
            Preconditions.checkArgument(j + INT16SZ <= INADDR6SZ, "Overrun in v6 parsing, should not occur");
            dst[j] = (byte) ((val >> 8) & nextNibble(m)) ; j++; m = m - 8;
            dst[j] = (byte) (val & nextNibble(m)); j++; m = m - 8;
        }

        if ((j < INADDR6SZ) && (m < 0)) {
            /* past the mask */
            for (i = j; i < INADDR6SZ; i++) {
                dst[i] = 0;
            }
        } else {
            /* normal parsing */
            if (colonp != -1) {
                int n = j - colonp;

                Preconditions.checkArgument(j != INADDR6SZ, "Overrun in v6 parsing, should not occur");
                for (i = 1; i <= n; i++) {
                    dst[INADDR6SZ - i] = dst[colonp + n - i];
                    dst[colonp + n - i] = 0;
                }
                j = INADDR6SZ;
            }
            Preconditions.checkArgument(j == INADDR6SZ, "Overrun in v6 parsing, should not occur");
        }

        dst[INADDR6SZ] = (byte) mask;
        return dst;
    }

    /**
     * Print a v6 prefix in byte array + 1 notation
     * @param _binary_form - prefix, in byte [] form, last byte is netmask
     * @return string of v6 prefix
     * @throws UnknownHostException unknown host exception
     */
    public static String byteArrayV6PrefixToString(final byte [] _binary_form) throws UnknownHostException {
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
                    Arrays.copyOf(_binary_form, INADDR6SZ)
                )
            )
        );
        sb.append('/');
        sb.append(_binary_form[INADDR6SZ] & 0xff);
        return sb.toString();
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
     * Canonicalize a v6 prefix while in binary form
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
        Iterator<String> addressParts = PREFIX_SPLITTER.split(ipv6Prefix.getValue()).iterator();
        return new Ipv6Address(addressParts.next());
    }

    public static Integer extractIpv6Prefix(final Ipv6Prefix ipv6Prefix) {
        Iterator<String> addressParts = PREFIX_SPLITTER.split(ipv6Prefix.getValue()).iterator();
        addressParts.next();

        Integer prefix = null;
        if (addressParts.hasNext()) {
            prefix = Integer.parseInt(addressParts.next());
        }
        return prefix;
    }

    public static int countBits(final byte[] mask) {
        int netmask = 0;
        for (byte b : mask) {
            netmask += Integer.bitCount(UnsignedBytes.toInt(b));
        }
        return netmask;
    }

    public static final byte[] convertArbitraryMaskToByteArray(DottedQuad mask) {
        String maskValue;
        if(mask.getValue() != null && mask != null){
           maskValue  = mask.getValue();
        }
        else maskValue = DEFAULT_ARBITRARY_BIT_MASK;
        InetAddress maskInIpFormat = null;
        try {
            maskInIpFormat = InetAddress.getByName(maskValue);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        byte[] bytes = maskInIpFormat.getAddress();
        return bytes;
    }
}
