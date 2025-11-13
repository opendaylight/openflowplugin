/*
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.libraries.liblldp;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class containing the common utility functions needed for operating on networking data structures.
 */
public abstract class NetUtils {
    private static final Logger LOG = LoggerFactory.getLogger(NetUtils.class);

    /**
     * Constant holding the number of bytes in MAC Address.
     */
    public static final int MAC_ADDR_LENGTH_IN_BYTES = 6;

    /**
     * Constant holding the number of words in MAC Address.
     */
    public static final int MAC_ADDR_LENGTH_IN_WORDS = 3;

    /**
     * Constant holding the broadcast MAC address.
     */
    private static final byte[] BROADCAST_MAC_ADDR = {-1, -1, -1, -1, -1, -1};

    /**
     * Pre-compiled pattern of how an IPv4 address looks like.
     */
    private static final Pattern IPV4_PATTERN =
        Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])");

    /**
     * Converts a 4 bytes array into an integer number.
     *
     * @param ba
     *            the 4 bytes long byte array
     * @return the integer number
     */
    public static int byteArray4ToInt(final byte[] ba) {
        if (ba == null || ba.length != 4) {
            return 0;
        }
        return (0xff & ba[0]) << 24 | (0xff & ba[1]) << 16 | (0xff & ba[2]) << 8 | 0xff & ba[3];
    }

    /**
     * Converts a 6 bytes array into a long number MAC addresses.
     *
     * @param ba
     *            The 6 bytes long byte array.
     * @return The long number.
     *         Zero is returned if {@code ba} is {@code null} or
     *         the length of it is not six.
     */
    public static long byteArray6ToLong(final byte[] ba) {
        if (ba == null || ba.length != MAC_ADDR_LENGTH_IN_BYTES) {
            return 0L;
        }
        long num = 0L;
        int index = 0;
        do {
            num <<= Byte.SIZE;
            num |= 0xff & ba[index];
            index++;
        } while (index < MAC_ADDR_LENGTH_IN_BYTES);
        return num;
    }

    /**
     * Converts a long number to a 6 bytes array for MAC addresses.
     *
     * @param addr
     *            The long number.
     * @return The byte array.
     */
    public static byte[] longToByteArray6(long addr) {
        byte[] mac = new byte[MAC_ADDR_LENGTH_IN_BYTES];
        int index = MAC_ADDR_LENGTH_IN_BYTES - 1;
        do {
            mac[index] = (byte) addr;
            addr >>>= Byte.SIZE;
            index--;
        } while (index >= 0);
        return mac;
    }

    /**
     * Converts an integer number into a 4 bytes array.
     *
     * @param number
     *            the integer number
     * @return the byte array
     */
    public static byte[] intToByteArray4(final int number) {
        return new byte[] { (byte) (number >> 24 & 0xff), (byte) (number >> 16 & 0xff), (byte) (number >> 8 & 0xff),
            (byte) (number & 0xff) };
    }

    /**
     * Converts an IP address passed as integer value into the respective InetAddress object.
     *
     * @param address
     *            the IP address in integer form
     * @return the IP address in InetAddress form
     */
    public static InetAddress getInetAddress(final int address) {
        InetAddress ip = null;
        try {
            ip = InetAddress.getByAddress(NetUtils.intToByteArray4(address));
        } catch (final UnknownHostException e) {
            LOG.error("Failed to convert {}", address, e);
        }
        return ip;
    }

    /**
     * Return the InetAddress Network Mask given the length of the prefix bit
     * mask. The prefix bit mask indicates the contiguous leading bits that are
     * NOT masked out. Example: A prefix bit mask length of 8 will give an
     * InetAddress Network Mask of 255.0.0.0
     *
     * @param prefixMaskLength
     *            integer representing the length of the prefix network mask
     * @param isV6
     *            boolean representing the IP version of the returned address
     */
    public static InetAddress getInetNetworkMask(final int prefixMaskLength, final boolean isV6) {
        if (prefixMaskLength < 0 || (isV6 ? prefixMaskLength > 128 : prefixMaskLength > 32)) {
            return null;
        }
        byte[] v4Address = { 0, 0, 0, 0 };
        byte[] v6Address = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        byte[] address = isV6 ? v6Address : v4Address;
        int numBytes = prefixMaskLength / 8;
        int numBits = prefixMaskLength % 8;
        int index = 0;
        for (; index < numBytes; index++) {
            address[index] = (byte) 0xff;
        }
        if (numBits > 0) {
            int rem = 0;
            for (int j = 0; j < numBits; j++) {
                rem |= 1 << 7 - j;
            }
            address[index] = (byte) rem;
        }

        try {
            return InetAddress.getByAddress(address);
        } catch (final UnknownHostException e) {
            LOG.error("Failed to convert {}", address, e);
        }
        return null;
    }

    /**
     * Returns the prefix size in bits of the specified subnet mask. Example:
     * For the subnet mask ff.ff.ff.e0 it returns 25 while for ff.00.00.00 it
     * returns 8. If the passed subnetMask array is null, 0 is returned.
     *
     * @param subnetMask
     *            the subnet mask as byte array
     * @return the prefix length as number of bits
     */
    public static int getSubnetMaskLength(final byte[] subnetMask) {
        int maskLength = 0;
        if (subnetMask != null && (subnetMask.length == 4 || subnetMask.length == 16)) {
            int index = 0;
            while (index < subnetMask.length && subnetMask[index] == (byte) 0xFF) {
                maskLength += Byte.SIZE;
                index++;
            }
            if (index != subnetMask.length) {
                int bits = Byte.SIZE - 1;
                while (bits >= 0 && (subnetMask[index] & 1 << bits)  != 0) {
                    bits--;
                    maskLength++;
                }
            }
        }
        return maskLength;
    }

    /**
     * Returns the prefix size in bits of the specified subnet mask. Example:
     * For the subnet mask 255.255.255.128 it returns 25 while for 255.0.0.0 it
     * returns 8. If the passed subnetMask object is null, 0 is returned
     *
     * @param subnetMask
     *            the subnet mask as InetAddress
     * @return the prefix length as number of bits
     */
    public static int getSubnetMaskLength(final InetAddress subnetMask) {
        return subnetMask == null ? 0 : NetUtils.getSubnetMaskLength(subnetMask.getAddress());
    }

    /**
     * Given an IP address and a prefix network mask length, it returns the
     * equivalent subnet prefix IP address Example: for ip = "172.28.30.254" and
     * maskLen = 25 it will return "172.28.30.128".
     *
     * @param ip
     *            the IP address in InetAddress form
     * @param maskLen
     *            the length of the prefix network mask
     * @return the subnet prefix IP address in InetAddress form
     */
    public static InetAddress getSubnetPrefix(final InetAddress ip, final int maskLen) {
        int bytes = maskLen / 8;
        int bits = maskLen % 8;
        byte modifiedByte;
        byte[] sn = ip.getAddress();
        if (bits > 0) {
            modifiedByte = (byte) (sn[bytes] >> 8 - bits);
            sn[bytes] = (byte) (modifiedByte << 8 - bits);
            bytes++;
        }
        for (; bytes < sn.length; bytes++) {
            sn[bytes] = (byte) 0;
        }
        try {
            return InetAddress.getByAddress(sn);
        } catch (final UnknownHostException e) {
            return null;
        }
    }

    /**
     * Checks if the test address and mask conflicts with the filter address and mask.
     * For example:
     *
     * <p>testAddress: 172.28.2.23
     * testMask: 255.255.255.0
     * filterAddress: 172.28.1.10
     * testMask: 255.255.255.0
     * do conflict
     *
     * <p>testAddress: 172.28.2.23
     * testMask: 255.255.255.0
     * filterAddress: 172.28.1.10
     * testMask: 255.255.0.0
     * do not conflict
     *
     * <p>Null parameters are permitted.
     */
    public static boolean inetAddressConflict(final InetAddress testAddress, final InetAddress filterAddress,
            final InetAddress testMask, final InetAddress filterMask) {
        // Sanity check
        if (testAddress == null || filterAddress == null) {
            return false;
        }

        // Presence check
        if (isAny(testAddress) || isAny(filterAddress)) {
            return false;
        }

        int testMaskLen = testMask == null ? testAddress instanceof Inet4Address ? 32 : 128 : NetUtils
                .getSubnetMaskLength(testMask);
        int filterMaskLen = filterMask == null ? testAddress instanceof Inet4Address ? 32 : 128 : NetUtils
                .getSubnetMaskLength(filterMask);

        // Mask length check. Test mask has to be more specific than filter one
        if (testMaskLen < filterMaskLen) {
            return true;
        }

        // Subnet Prefix on filter mask length must be the same
        InetAddress prefix1 = getSubnetPrefix(testAddress, filterMaskLen);
        InetAddress prefix2 = getSubnetPrefix(filterAddress, filterMaskLen);
        return !prefix1.equals(prefix2);
    }

    /**
     * Returns true if the passed MAC address is all zero.
     *
     * @param mac
     *            the byte array representing the MAC address
     * @return true if all MAC bytes are zero
     */
    public static boolean isZeroMAC(final byte[] mac) {
        for (short i = 0; i < 6; i++) {
            if (mac[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the MAC address is the broadcast MAC address and false otherwise.
     */
    public static boolean isBroadcastMACAddr(final byte[] macAddress) {
        if (macAddress.length == MAC_ADDR_LENGTH_IN_BYTES) {
            for (int i = 0; i < 6; i++) {
                if (macAddress[i] != BROADCAST_MAC_ADDR[i]) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Returns true if the MAC address is a unicast MAC address and false otherwise.
     */
    public static boolean isUnicastMACAddr(final byte[] macAddress) {
        if (macAddress.length == MAC_ADDR_LENGTH_IN_BYTES) {
            return (macAddress[0] & 1) == 0;
        }
        return false;
    }

    /**
     * Returns true if the MAC address is a multicast MAC address and false
     * otherwise. Note that this explicitly returns false for the broadcast MAC
     * address.
     */
    public static boolean isMulticastMACAddr(final byte[] macAddress) {
        if (macAddress.length == MAC_ADDR_LENGTH_IN_BYTES && !isBroadcastMACAddr(macAddress)) {
            return (macAddress[0] & 1) != 0;
        }
        return false;
    }

    /**
     * Returns true if the passed InetAddress contains all zero.
     *
     * @param ip
     *            the IP address to test
     * @return true if the address is all zero
     */
    public static boolean isAny(final InetAddress ip) {
        for (byte b : ip.getAddress()) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean fieldsConflict(final int field1, final int field2) {
        return field1 != 0 && field2 != 0 && field1 != field2;
    }

    public static InetAddress parseInetAddress(final String addressString) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(addressString);
        } catch (final UnknownHostException e) {
            LOG.error("Failed to convert {}", addressString, e);
        }
        return address;
    }

    /**
     * Checks if the passed IP v4 address in string form is valid The address
     * may specify a mask at the end as "/MM".
     *
     * @param cidr
     *            the v4 address as A.B.C.D/MM
     */
    public static boolean isIPv4AddressValid(final String cidr) {
        if (cidr == null) {
            return false;
        }

        String[] values = cidr.split("/");
        if (!IPV4_PATTERN.matcher(values[0]).matches()) {
            return false;
        }
        if (values.length >= 2) {
            int prefix = Integer.parseInt(values[1]);
            if (prefix < 0 || prefix > 32) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the passed IP v6 address in string form is valid The address
     * may specify a mask at the end as "/MMM".
     *
     * @param cidr
     *            the v6 address as A::1/MMM
     */
    public static boolean isIPv6AddressValid(final String cidr) {
        if (cidr == null) {
            return false;
        }

        String[] values = cidr.split("/");
        try {
            // when given an IP address, InetAddress.getByName validates the ip
            // address
            InetAddress addr = InetAddress.getByName(values[0]);
            if (!(addr instanceof Inet6Address)) {
                return false;
            }
        } catch (final UnknownHostException ex) {
            return false;
        }

        if (values.length >= 2) {
            int prefix = Integer.parseInt(values[1]);
            if (prefix < 0 || prefix > 128) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the passed IP address in string form is a valid v4 or v6
     * address. The address may specify a mask at the end as "/MMM"
     *
     * @param cidr
     *            the v4 or v6 address as IP/MMM
     */
    public static boolean isIPAddressValid(final String cidr) {
        return NetUtils.isIPv4AddressValid(cidr) || NetUtils.isIPv6AddressValid(cidr);
    }

    /*
     * Following utilities are useful when you need to compare or bit shift java
     * primitive type variable which are inherently signed
     */

    /**
     * Returns the unsigned value of the passed byte variable.
     *
     * @param value
     *            the byte value
     * @return the int variable containing the unsigned byte value
     */
    public static int getUnsignedByte(final byte value) {
        return value & 0xFF;
    }

    /**
     * Return the unsigned value of the passed short variable.
     *
     * @param value
     *            the short value
     * @return the int variable containing the unsigned short value
     */
    public static int getUnsignedShort(final short value) {
        return value & 0xFFFF;
    }

    /**
     * Returns the highest v4 or v6 InetAddress.
     *
     * @param v6
     *            true for IPv6, false for Ipv4
     * @return The highest IPv4 or IPv6 address
     */
    public static InetAddress gethighestIP(final boolean v6) {
        try {
            return v6 ? InetAddress.getByName("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff") : InetAddress
                    .getByName("255.255.255.255");
        } catch (final UnknownHostException e) {
            return null;
        }
    }

    /**
     * Returns Broadcast MAC Address.
     *
     * @return the byte array containing  broadcast mac address
     */
    public static byte[] getBroadcastMACAddr() {
        return Arrays.copyOf(BROADCAST_MAC_ADDR, BROADCAST_MAC_ADDR.length);
    }
}
