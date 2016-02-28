/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper;

import com.google.common.net.InetAddresses;
import java.net.Inet4Address;

import com.google.common.annotations.VisibleForTesting;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.MacAddressFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;

/**
 * @author joe
 *
 */
public class MatchComparatorHelper {

    private static final int DEFAULT_SUBNET = 32;
    private static final int IPV4_MASK_LENGTH = 32;
    private static final int SHIFT_OCTET_1 = 24;
    private static final int SHIFT_OCTET_2 = 16;
    private static final int SHIFT_OCTET_3 = 8;
    private static final int SHIFT_OCTET_4 = 0;
    private static final int POSITION_OCTET_1 = 0;
    private static final int POSITION_OCTET_2 = 1;
    private static final int POSITION_OCTET_3 = 2;
    private static final int POSITION_OCTET_4 = 3;

    /*
     * Custom EthernetMatch is required because mac address string provided by user in EthernetMatch can be in any case
     * (upper or lower or mix). Ethernet Match which controller receives from switch is always an upper case string.
     * Default EthernetMatch equals doesn't use equalsIgnoreCase() and hence it fails. E.g User provided mac address
     * string in flow match is aa:bb:cc:dd:ee:ff and when controller fetch statistic data, openflow driver library
     * returns AA:BB:CC:DD:EE:FF and default eqauls fails here.
     */
    @VisibleForTesting
    static boolean ethernetMatchEquals(final EthernetMatch statsEthernetMatch, final EthernetMatch storedEthernetMatch) {
        boolean verdict = true;
        final Boolean checkNullValues = checkNullValues(statsEthernetMatch, storedEthernetMatch);
        if (checkNullValues != null) {
            verdict = checkNullValues;
        } else {
            verdict = ethernetMatchFieldsEquals(statsEthernetMatch.getEthernetSource(),
                    storedEthernetMatch.getEthernetSource());
            if (verdict) {
                verdict = ethernetMatchFieldsEquals(statsEthernetMatch.getEthernetDestination(),
                        storedEthernetMatch.getEthernetDestination());
            }
            if (verdict) {
                if (statsEthernetMatch.getEthernetType() == null) {
                    if (storedEthernetMatch.getEthernetType() != null) {
                        verdict = false;
                    }
                } else {
                    verdict = statsEthernetMatch.getEthernetType().equals(storedEthernetMatch.getEthernetType());
                }
            }
        }
        return verdict;
    }

    static boolean ethernetMatchFieldsEquals(final MacAddressFilter statsEthernetMatchFields,
            final MacAddressFilter storedEthernetMatchFields) {
        boolean verdict = true;
        final Boolean checkNullValues = checkNullValues(statsEthernetMatchFields, storedEthernetMatchFields);
        if (checkNullValues != null) {
            verdict = checkNullValues;
        } else {
            verdict = macAddressEquals(statsEthernetMatchFields.getAddress(), storedEthernetMatchFields.getAddress());
            if (verdict) {
                verdict = macAddressEquals(statsEthernetMatchFields.getMask(), storedEthernetMatchFields.getMask());
            }
        }
        return verdict;
    }

    static boolean macAddressEquals(final MacAddress statsMacAddress, final MacAddress storedMacAddress) {
        boolean verdict = true;
        final Boolean checkNullValues = checkNullValues(statsMacAddress, storedMacAddress);
        if (checkNullValues != null) {
            verdict = checkNullValues;
        } else {
            verdict = statsMacAddress.getValue().equalsIgnoreCase(storedMacAddress.getValue());
        }
        return verdict;
    }

    @VisibleForTesting
    static boolean layer3MatchEquals(final Layer3Match statsLayer3Match, final Layer3Match storedLayer3Match) {
        boolean verdict = true;
        if (statsLayer3Match instanceof Ipv4Match && storedLayer3Match instanceof Ipv4Match) {
            final Ipv4Match statsIpv4Match = (Ipv4Match) statsLayer3Match;
            final Ipv4Match storedIpv4Match = (Ipv4Match) storedLayer3Match;
            verdict = MatchComparatorHelper.compareIpv4PrefixNullSafe(storedIpv4Match.getIpv4Destination(),
                    statsIpv4Match.getIpv4Destination());
            if (verdict) {
                verdict = MatchComparatorHelper.compareIpv4PrefixNullSafe(statsIpv4Match.getIpv4Source(),
                        storedIpv4Match.getIpv4Source());
            }
        } else if(statsLayer3Match instanceof Ipv6Match && storedLayer3Match instanceof Ipv6Match) {
            final Ipv6Match statsIpv6Match = (Ipv6Match) statsLayer3Match;
            final Ipv6Match storedIpv6Match = (Ipv6Match) storedLayer3Match;
            verdict = MatchComparatorHelper.compareIpv6PrefixNullSafe(storedIpv6Match.getIpv6Destination(),
                    statsIpv6Match.getIpv6Destination());
            if (verdict) {
                verdict = MatchComparatorHelper.compareIpv6PrefixNullSafe(statsIpv6Match.getIpv6Source(),
                        storedIpv6Match.getIpv6Source());
            }
        }else {
            final Boolean nullCheckOut = checkNullValues(storedLayer3Match, statsLayer3Match);
            if (nullCheckOut != null) {
                verdict = nullCheckOut;
            } else {
                verdict = storedLayer3Match.equals(statsLayer3Match);
            }
        }

        return verdict;
    }


    /**
     * TODO: why don't we use the default Ipv4Prefix.equals()?
     *
     * @param statsIpAddress
     * @param storedIpAddress
     * @return true if IPv4prefixes equals
     */
    static boolean IpAddressEquals(final Ipv4Prefix statsIpAddress, final Ipv4Prefix storedIpAddress) {
        final IntegerIpAddress statsIpAddressInt = MatchComparatorHelper.strIpToIntIp(statsIpAddress.getValue());
        final IntegerIpAddress storedIpAddressInt = MatchComparatorHelper.strIpToIntIp(storedIpAddress.getValue());

        if (ipAndMaskBasedMatch(statsIpAddressInt, storedIpAddressInt)) {
            return true;
        }
        if (ipBasedMatch(statsIpAddressInt, storedIpAddressInt)) {
            return true;
        }
        return false;
    }

    static boolean ipAndMaskBasedMatch(final IntegerIpAddress statsIpAddressInt,
            final IntegerIpAddress storedIpAddressInt) {
        return ((statsIpAddressInt.getIp() & statsIpAddressInt.getMask()) == (storedIpAddressInt.getIp() & storedIpAddressInt
                .getMask()));
    }

    static boolean ipBasedMatch(final IntegerIpAddress statsIpAddressInt, final IntegerIpAddress storedIpAddressInt) {
        return (statsIpAddressInt.getIp() == storedIpAddressInt.getIp());
    }


    private static boolean IpAddressEquals(Ipv6Prefix statsIpv6, Ipv6Prefix storedIpv6) {
        final String[] statsIpMask = statsIpv6.getValue().split("/");
        final String[] storedIpMask = storedIpv6.getValue().split("/");
        if(! (statsIpMask.length > 1 && storedIpMask.length > 1 &&  statsIpMask[1].equals(storedIpMask[1]))){
            return false;
        }
        if(InetAddresses.forString(statsIpMask[0]).equals(InetAddresses.forString(storedIpMask[0]))){
            return true;
        }
        return false;
    }


    static Boolean checkNullValues(final Object v1, final Object v2) {
        Boolean verdict = null;
        if (v1 == null && v2 != null) {
            verdict = Boolean.FALSE;
        } else if (v1 != null && v2 == null) {
            verdict = Boolean.FALSE;
        } else if (v1 == null && v2 == null) {
            verdict = Boolean.TRUE;
        }

        return verdict;
    }

    static boolean compareIpv4PrefixNullSafe(final Ipv4Prefix statsIpv4, final Ipv4Prefix storedIpv4) {
        boolean verdict = true;
        final Boolean checkDestNullValuesOut = checkNullValues(storedIpv4, statsIpv4);
        if (checkDestNullValuesOut != null) {
            verdict = checkDestNullValuesOut;
        } else if (!IpAddressEquals(statsIpv4, storedIpv4)) {
            verdict = false;
        }

        return verdict;
    }

    private static boolean compareIpv6PrefixNullSafe(Ipv6Prefix statsIpv6, Ipv6Prefix storedIpv6) {
        boolean verdict = true;
        final Boolean checkDestNullValuesOut = checkNullValues(statsIpv6, storedIpv6);
        if (checkDestNullValuesOut != null) {
            verdict = checkDestNullValuesOut;
        } else if (!IpAddressEquals(statsIpv6, storedIpv6)) {
            verdict = false;
        }

        return verdict;
    }

    /**
     * Method return integer version of ip address. Converted int will be mask if mask specified
     */
    static IntegerIpAddress strIpToIntIp(final String ipAddresss) {

        final String[] parts = ipAddresss.split("/");
        final String ip = parts[0];
        int prefix;

        if (parts.length < 2) {
            prefix = DEFAULT_SUBNET;
        } else {
            prefix = Integer.parseInt(parts[1]);
            if (prefix < 0 || prefix > IPV4_MASK_LENGTH) {
                final StringBuilder stringBuilder = new StringBuilder(
                        "Valid values for mask are from range 0 - 32. Value ");
                stringBuilder.append(prefix);
                stringBuilder.append(" is invalid.");
                throw new IllegalStateException(stringBuilder.toString());
            }
        }

        IntegerIpAddress integerIpAddress = null;

        final Inet4Address addr = ((Inet4Address) InetAddresses.forString(ip));
        final byte[] addrBytes = addr.getAddress();
        // FIXME: what is meaning of anding with 0xFF? Probably could be removed.
        final int ipInt = ((addrBytes[POSITION_OCTET_1] & 0xFF) << SHIFT_OCTET_1)
                | ((addrBytes[POSITION_OCTET_2] & 0xFF) << SHIFT_OCTET_2)
                | ((addrBytes[POSITION_OCTET_3] & 0xFF) << SHIFT_OCTET_3)
                | ((addrBytes[POSITION_OCTET_4] & 0xFF) << SHIFT_OCTET_4);

        // FIXME: Is this valid?
        final int mask = 0xffffffff << DEFAULT_SUBNET - prefix;

        integerIpAddress = new IntegerIpAddress(ipInt, mask);

        return integerIpAddress;
    }

}
