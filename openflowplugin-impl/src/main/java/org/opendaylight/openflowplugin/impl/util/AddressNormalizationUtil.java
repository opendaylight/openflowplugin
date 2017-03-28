/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */

package org.opendaylight.openflowplugin.impl.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import javax.annotation.Nullable;
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
 * The type Address normalization util.
 */
public class AddressNormalizationUtil {
    private static final Logger LOG = LoggerFactory.getLogger(AddressNormalizationUtil.class);

    private static final String NO_ETH_MASK = "ff:ff:ff:ff:ff:ff";
    private static final String PREFIX_SEPARATOR = "/";

    /**
     * Normalize protocol agnostic port uri.
     *
     * @param port            the port
     * @param protocolVersion the protocol version
     * @return the uri
     */
    @Nullable
    public static Uri normalizeProtocolAgnosticPort(@Nullable final Uri port, final short protocolVersion) {
        if (Objects.isNull(port)) {
            return null;
        }

        return OpenflowPortsUtil.getProtocolAgnosticPortUri(protocolVersion, InventoryDataServiceUtil
                .portNumberfromNodeConnectorId(OpenflowVersion.get(protocolVersion), port.getValue()));
    }

    /**
     * Normalize ipv 6 prefix ipv 6 prefix.
     *
     * @param ipv6Prefix the ipv 6 prefix
     * @return the ipv 6 prefix
     */
    @Nullable
    public static Ipv6Prefix normalizeIpv6Prefix(@Nullable final Ipv6Prefix ipv6Prefix) {
        if (Objects.isNull(ipv6Prefix)) {
            return null;
        }

        final byte[] address = IetfInetUtil.INSTANCE.ipv6AddressBytes(IpConversionUtil.extractIpv6Address(ipv6Prefix));
        final byte[] mask = IpConversionUtil.convertIpv6ArbitraryMaskToByteArray(IpConversionUtil.extractIpv6AddressMask(ipv6Prefix));
        return normalizeIpv6Address(address, mask);
    }

    /**
     * Normalize ipv 6 arbitrary ipv 6 prefix.
     *
     * @param ipv4Address the ipv 4 address
     * @param ipv4Mask    the ipv 4 mask
     * @return the ipv 6 prefix
     */
    @Nullable
    public static Ipv6Prefix normalizeIpv6Arbitrary(@Nullable final Ipv6Address ipv4Address, @Nullable final Ipv6ArbitraryMask ipv4Mask) {
        if (Objects.isNull(ipv4Address)) {
            return null;
        }

        final byte[] address = IetfInetUtil.INSTANCE.ipv6AddressBytes(ipv4Address);
        final byte[] mask = IpConversionUtil.convertIpv6ArbitraryMaskToByteArray(ipv4Mask);
        return normalizeIpv6Address(address, mask);
    }

    /**
     * Normalize ipv 4 prefix ipv 4 prefix.
     *
     * @param ipv4Prefix the ipv 4 prefix
     * @return the ipv 4 prefix
     */
    @Nullable
    public static Ipv4Prefix normalizeIpv4Prefix(@Nullable final Ipv4Prefix ipv4Prefix) {
        if (Objects.isNull(ipv4Prefix)) {
            return null;
        }

        final byte[] address = IetfInetUtil.INSTANCE.ipv4AddressBytes(IpConversionUtil.extractIpv4Address(ipv4Prefix));
        final byte[] mask = IpConversionUtil.convertArbitraryMaskToByteArray(IpConversionUtil.extractIpv4AddressMask(ipv4Prefix));
        return normalizeIpv4Address(address, mask);
    }

    /**
     * Normalize ipv 4 arbitrary ipv 4 prefix.
     *
     * @param ipv4Address the ipv 4 address
     * @param ipv4Mask    the ipv 4 mask
     * @return the ipv 4 prefix
     */
    @Nullable
    public static Ipv4Prefix normalizeIpv4Arbitrary(@Nullable final Ipv4Address ipv4Address, @Nullable final DottedQuad ipv4Mask) {
        if (Objects.isNull(ipv4Address)) {
            return null;
        }

        final byte[] address = IetfInetUtil.INSTANCE.ipv4AddressBytes(ipv4Address);
        final byte[] mask = IpConversionUtil.convertArbitraryMaskToByteArray(ipv4Mask);
        return normalizeIpv4Address(address, mask);
    }

    /**
     * Normalize ipv 4 address ipv 4 prefix.
     *
     * @param address the address
     * @param mask    the mask
     * @return the ipv 4 prefix
     */
    @Nullable
    public static Ipv4Prefix normalizeIpv4Address(@Nullable final byte[] address, @Nullable final byte[] mask) {
        final String addressPrefix = normalizeInetAddressWithMask(normalizeIpAddress(address, mask), mask);

        if (Objects.isNull(addressPrefix)) {
            return null;
        }

        return new Ipv4Prefix(addressPrefix);
    }


    /**
     * Normalize ipv 6 address ipv 6 prefix.
     *
     * @param address the address
     * @param mask    the mask
     * @return the ipv 6 prefix
     */
    @Nullable
    public static Ipv6Prefix normalizeIpv6Address(@Nullable final byte[] address, @Nullable final byte[] mask) {
        final String addressPrefix = normalizeInetAddressWithMask(normalizeIpAddress(address, mask), mask);

        if (Objects.isNull(addressPrefix)) {
            return null;
        }

        return new Ipv6Prefix(addressPrefix);
    }

    /**
     * Normalize ip address inet address.
     *
     * @param address the address
     * @param mask    the mask
     * @return the inet address
     */
    @Nullable
    public static InetAddress normalizeIpAddress(@Nullable final byte[] address, @Nullable final byte[] mask) {
        if (Objects.isNull(address)) {
            return null;
        }

        final byte[] result = new byte[address.length];

        for (int i = 0; i < address.length; i++) {
            result[i] = Objects.nonNull(mask) ?
                    (byte) (address[i] & mask[i]) :
                    address[i];
        }

        try {
            return InetAddress.getByAddress(result);
        } catch (UnknownHostException e) {
            LOG.warn("Failed to recognize the host while normalizing IP address from bytes ", e);
            return null;
        }
    }

    /**
     * Normalize inet address with mask string.
     *
     * @param address the address
     * @param mask    the mask
     * @return the string
     */
    @Nullable
    public static String normalizeInetAddressWithMask(@Nullable final InetAddress address, @Nullable final byte[] mask) {
        if (Objects.isNull(address)) {
            return null;
        }

        return address.getHostName() +
                (Objects.nonNull(mask)
                        ? PREFIX_SEPARATOR + String.valueOf(IpConversionUtil.countBits(mask))
                        : "");
    }

    /**
     * Normalize mac address mac address.
     *
     * @param macAddress the mac address
     * @return the mac address
     */
    @Nullable
    public static MacAddress normalizeMacAddress(@Nullable final MacAddress macAddress) {
        if (Objects.isNull(macAddress)) {
            return null;
        }

        return new MacAddress(macAddress.getValue().toLowerCase());
    }

    /**
     * Normalize mac address mask mac address.
     *
     * @param macAddress the mac address
     * @return the mac address
     */
    @Nullable
    public static MacAddress normalizeMacAddressMask(@Nullable final MacAddress macAddress) {
        final MacAddress normalizedMacAddress = normalizeMacAddress(macAddress);

        if (Objects.isNull(normalizedMacAddress)) {
            return null;
        }

        if (NO_ETH_MASK.equals(normalizedMacAddress.getValue())) {
            return null;
        }

        return normalizedMacAddress;
    }

}
