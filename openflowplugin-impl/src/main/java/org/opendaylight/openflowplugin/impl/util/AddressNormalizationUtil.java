/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import static java.util.Objects.requireNonNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used for converting OpenFlow port numbers, Ipv4 and Ipv6 addresses to normalized format.
 */
public enum AddressNormalizationUtil {
    VERSION_1_0(OpenflowVersion.OF10),
    VERSION_1_3(OpenflowVersion.OF13),
    UNSUPPORTED(OpenflowVersion.UNSUPPORTED);

    private static final Logger LOG = LoggerFactory.getLogger(AddressNormalizationUtil.class);

    private static final String NO_ETH_MASK = "ff:ff:ff:ff:ff:ff";
    private static final String PREFIX_SEPARATOR = "/";

    private final OpenflowVersion version;

    AddressNormalizationUtil(final OpenflowVersion version) {
        this.version = requireNonNull(version);
    }

    public static @NonNull AddressNormalizationUtil ofVersion(final OpenflowVersion version) {
        return switch (version) {
            case OF10 -> VERSION_1_0;
            case OF13 -> VERSION_1_3;
            case UNSUPPORTED -> UNSUPPORTED;
        };
    }

    /**
     * Extract port number from URI and convert it to OpenFlow specific textual representation.
     *
     * @param port            the OpenFlow port
     * @return normalized uri
     */
    public @Nullable Uri normalizeProtocolAgnosticPort(final @Nullable Uri port) {
        if (port != null) {
            final var portValue = InventoryDataServiceUtil.portNumberfromNodeConnectorId(version, port.getValue());
            if (portValue != null) {
                return OpenflowPortsUtil.getProtocolAgnosticPortUri(version.getVersion(), portValue);
            }
        }
        return null;
    }

    /**
     * Normalize Ipv6 address with prefix mask (ex. 1234:5678:9ABC::/76) and apply prefix mask to Ipv6 address.
     *
     * @param ipv6Prefix the Ipv6 prefix
     * @return normalized Ipv6 prefix
     */
    public static @Nullable Ipv6Prefix normalizeIpv6Prefix(@Nullable final Ipv6Prefix ipv6Prefix) {
        if (ipv6Prefix == null) {
            return null;
        }

        final byte[] address = IetfInetUtil.ipv6AddressBytes(IpConversionUtil.extractIpv6Address(ipv6Prefix));
        final byte[] mask =
                IpConversionUtil.convertIpv6PrefixToByteArray(IpConversionUtil.extractIpv6Prefix(ipv6Prefix));
        return normalizeIpv6Address(address, mask);
    }

    /**
     * Normalize Ipv6 address and arbitrary mask and apply arbitrary mask to Ipv6 address.
     *
     * @param ipv6Address the Ipv4 address
     * @param ipv4Mask    the Ipv4 mask
     * @return normalized Ipv6 prefix
     */
    public static @Nullable Ipv6Prefix normalizeIpv6Arbitrary(@Nullable final Ipv6Address ipv6Address,
                                                              @Nullable final Ipv6ArbitraryMask ipv4Mask) {
        if (ipv6Address == null) {
            return null;
        }

        final byte[] address = IetfInetUtil.ipv6AddressBytes(ipv6Address);
        final byte[] mask = IpConversionUtil.convertIpv6ArbitraryMaskToByteArray(ipv4Mask);
        return normalizeIpv6Address(address, mask);
    }

    /**
     * Normalize ipv 6 address without mask.
     *
     * @param ipv6Address the Ipv6 address
     * @return normalized Ipv6 address
     */
    public static @Nullable Ipv6Address normalizeIpv6AddressWithoutMask(@Nullable final Ipv6Address ipv6Address) {
        final Ipv6Prefix ipv6Prefix = normalizeIpv6Arbitrary(ipv6Address, null);
        return ipv6Prefix == null ? null : new Ipv6Address(ipv6Prefix.getValue().split(PREFIX_SEPARATOR)[0]);
    }

    /**
     * Normalize Ipv4 address with prefix mask (ex. 192.168.0.1/24) and apply prefix mask to Ipv4 address.
     *
     * @param ipv4Prefix the Ipv4 prefix
     * @return normalized Ipv4 prefix
     */
    public static @Nullable Ipv4Prefix normalizeIpv4Prefix(@Nullable final Ipv4Prefix ipv4Prefix) {
        if (ipv4Prefix == null) {
            return null;
        }

        final byte[] address = IetfInetUtil.ipv4AddressBytes(IpConversionUtil.extractIpv4Address(ipv4Prefix));
        final byte[] mask =
                IpConversionUtil.convertArbitraryMaskToByteArray(IpConversionUtil.extractIpv4AddressMask(ipv4Prefix));
        return normalizeIpv4Address(address, mask);
    }

    /**
     * Normalize Ipv4 address and arbitrary mask and apply arbitrary mask to Ipv4 address.
     *
     * @param ipv4Address the Ipv4 address
     * @param ipv4Mask    the Ipv4 mask
     * @return normalized Ipv4 prefix
     */
    public static @Nullable Ipv4Prefix normalizeIpv4Arbitrary(@Nullable final Ipv4Address ipv4Address,
                                                              @Nullable final DottedQuad ipv4Mask) {
        if (ipv4Address == null) {
            return null;
        }

        final byte[] address = IetfInetUtil.ipv4AddressBytes(ipv4Address);
        final byte[] mask = IpConversionUtil.convertArbitraryMaskToByteArray(ipv4Mask);
        return normalizeIpv4Address(address, mask);
    }

    /**
     * Normalize Ipv4 address and arbitrary mask in byte array format and apply arbitrary mask to Ipv4 address.
     *
     * @param address Ipv4 address byte array
     * @param mask    Ipv4 mask byte array
     * @return normalized Ipv4 prefix
     */
    public static @Nullable Ipv4Prefix normalizeIpv4Address(final byte @Nullable [] address,
                                                            final byte @Nullable [] mask) {
        final String addressPrefix = normalizeInetAddressWithMask(normalizeIpAddress(address, mask), mask);

        if (addressPrefix == null) {
            return null;
        }

        return new Ipv4Prefix(addressPrefix);
    }


    /**
     * Normalize Ipv6 address and arbitrary mask in byte array format and apply arbitrary mask to Ipv6 address.
     *
     * @param address Ipv6 address byte array
     * @param mask    Ipv6 mask byte array
     * @return normalized Ipv6 prefix
     */
    public static @Nullable Ipv6Prefix normalizeIpv6Address(final byte @Nullable [] address,
                                                            final byte @Nullable [] mask) {
        final String addressPrefix = normalizeInetAddressWithMask(normalizeIpAddress(address, mask), mask);

        if (addressPrefix == null) {
            return null;
        }

        return new Ipv6Prefix(addressPrefix);
    }

    /**
     * Normalize generic IP address and arbitrary mask in byte array format and apply arbitrary mask to IP address.
     *
     * @param address address byte array
     * @param mask    mask byte array
     * @return normalized Inet address
     */
    public static @Nullable InetAddress normalizeIpAddress(final byte @Nullable [] address,
                                                           final byte @Nullable [] mask) {
        if (address == null) {
            return null;
        }

        final byte[] result = new byte[address.length];

        for (int i = 0; i < address.length; i++) {
            result[i] = mask != null ? (byte) (address[i] & mask[i]) : address[i];
        }

        try {
            return InetAddress.getByAddress(result);
        } catch (UnknownHostException e) {
            LOG.warn("Failed to recognize the host while normalizing IP address from bytes ", e);
            return null;
        }
    }

    /**
     * Convert arbitrary mask to prefix mask and append it to textual representation of Inet address.
     *
     * @param address the address
     * @param mask    the mask
     * @return the string
     */
    public static @Nullable String normalizeInetAddressWithMask(final @Nullable InetAddress address,
                                                                final byte @Nullable [] mask) {
        if (address == null) {
            return null;
        }

        return address.getHostAddress()
                + (mask == null ? "" : PREFIX_SEPARATOR + String.valueOf(IpConversionUtil.countBits(mask)));
    }

    /**
     * Convert MAC address to it's lower case format.
     *
     * @param macAddress the MAC address
     * @return normalized MAC address
     */
    @Nullable
    public static MacAddress normalizeMacAddress(@Nullable final MacAddress macAddress) {
        return macAddress == null ? null : new MacAddress(macAddress.getValue().toLowerCase(Locale.ROOT));
    }

    /**
     * Convert MAC address mask to it's lower case format and if it is full F mask, return null.
     *
     * @param macAddress the MAC address
     * @return normalized MAC address
     */
    @Nullable
    public static MacAddress normalizeMacAddressMask(@Nullable final MacAddress macAddress) {
        final MacAddress normalizedMacAddress = normalizeMacAddress(macAddress);

        if (normalizedMacAddress == null) {
            return null;
        }

        if (NO_ETH_MASK.equals(normalizedMacAddress.getValue())) {
            return null;
        }

        return normalizedMacAddress;
    }
}
