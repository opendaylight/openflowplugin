/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.InetAddresses;
import com.google.common.primitives.UnsignedBytes;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.MacAddressFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author joe
 * @author sai.marapareddy@gmail.com
 *
 */
public class MatchComparatorHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MatchComparatorHelper.class);
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
    private static final String DEFAULT_ARBITRARY_BIT_MASK = "255.255.255.255";
    private static final String DEFAULT_IPV6_ARBITRARY_BIT_MASK = "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff";
    private static final String PREFIX_SEPARATOR = "/";
    private static final int IPV4_ADDRESS_LENGTH = 32;
    private static final int IPV6_ADDRESS_LENGTH = 128;
    private static final int BYTE_SIZE = 8;

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
        } else if (statsLayer3Match instanceof Ipv6Match && storedLayer3Match instanceof Ipv6Match) {
            final Ipv6Match statsIpv6Match = (Ipv6Match) statsLayer3Match;
            final Ipv6Match storedIpv6Match = (Ipv6Match) storedLayer3Match;
            verdict = MatchComparatorHelper.compareIpv6PrefixNullSafe(storedIpv6Match.getIpv6Destination(),
                    statsIpv6Match.getIpv6Destination());
            if (verdict) {
                verdict = MatchComparatorHelper.compareIpv6PrefixNullSafe(statsIpv6Match.getIpv6Source(),
                        storedIpv6Match.getIpv6Source());
            }
        } else if (statsLayer3Match instanceof  Ipv4MatchArbitraryBitMask && storedLayer3Match instanceof Ipv4MatchArbitraryBitMask) {
            // At this moment storedIpv4MatchArbitraryBitMask & statsIpv4MatchArbitraryBitMask will always have non null arbitrary masks.
            // In case of no / null arbitrary mask, statsLayer3Match will be an instance of Ipv4Match.
            // Eg:- stats -> 1.0.1.0/255.0.255.0  stored -> 1.1.1.0/255.0.255.0
            final Ipv4MatchArbitraryBitMask statsIpv4MatchArbitraryBitMask= (Ipv4MatchArbitraryBitMask) statsLayer3Match;
            final Ipv4MatchArbitraryBitMask storedIpv4MatchArbitraryBitMask = (Ipv4MatchArbitraryBitMask) storedLayer3Match;
            if ((storedIpv4MatchArbitraryBitMask.getIpv4DestinationAddressNoMask() != null |
                    storedIpv4MatchArbitraryBitMask.getIpv4SourceAddressNoMask() != null)) {
                if (storedIpv4MatchArbitraryBitMask.getIpv4DestinationAddressNoMask() != null) {
                    String storedDstIpAddress = normalizeIpv4Address(storedIpv4MatchArbitraryBitMask.getIpv4DestinationAddressNoMask(),
                            storedIpv4MatchArbitraryBitMask.getIpv4DestinationArbitraryBitmask());
                    String statsDstIpAddress = normalizeIpv4Address(statsIpv4MatchArbitraryBitMask.getIpv4DestinationAddressNoMask(),
                            statsIpv4MatchArbitraryBitMask.getIpv4DestinationArbitraryBitmask());
                    if (MatchComparatorHelper.compareStringNullSafe(storedIpv4MatchArbitraryBitMask.getIpv4DestinationArbitraryBitmask().getValue(),
                            statsIpv4MatchArbitraryBitMask.getIpv4DestinationArbitraryBitmask().getValue())) {
                        verdict = MatchComparatorHelper.compareStringNullSafe(storedDstIpAddress,
                                statsDstIpAddress);
                    } else {
                        verdict = false;
                        return verdict;
                    }
                }
                if (storedIpv4MatchArbitraryBitMask.getIpv4SourceAddressNoMask() != null) {
                    String storedSrcIpAddress = normalizeIpv4Address(storedIpv4MatchArbitraryBitMask.getIpv4SourceAddressNoMask()
                            ,storedIpv4MatchArbitraryBitMask.getIpv4SourceArbitraryBitmask());
                    String statsSrcIpAddress = normalizeIpv4Address(statsIpv4MatchArbitraryBitMask.getIpv4SourceAddressNoMask()
                            ,statsIpv4MatchArbitraryBitMask.getIpv4SourceArbitraryBitmask());
                    if (MatchComparatorHelper.compareStringNullSafe(storedIpv4MatchArbitraryBitMask.getIpv4SourceArbitraryBitmask().getValue(),
                            statsIpv4MatchArbitraryBitMask.getIpv4SourceArbitraryBitmask().getValue())) {
                        verdict = MatchComparatorHelper.compareStringNullSafe(storedSrcIpAddress,
                                statsSrcIpAddress);
                    } else {
                        verdict = false;
                    }
                }
            } else {
                final Boolean nullCheckOut = checkNullValues(storedLayer3Match, statsLayer3Match);
                if (nullCheckOut != null) {
                    verdict = nullCheckOut;
                } else {
                    verdict = storedLayer3Match.equals(statsLayer3Match);
                }
            }
        } else if (statsLayer3Match instanceof Ipv4Match && storedLayer3Match instanceof Ipv4MatchArbitraryBitMask) {
            // Here stored netmask is an instance of Ipv4MatchArbitraryBitMask, when it is pushed in to switch
            // it automatically converts it in to cidr format in case of certain subnet masks ( consecutive ones or zeroes)
            // Eg:- stats src/dest -> 1.1.1.0/24  stored src/dest -> 1.1.1.0/255.255.255.0
            final Ipv4Match statsIpv4Match = (Ipv4Match) statsLayer3Match;
            final Ipv4MatchArbitraryBitMask storedIpv4MatchArbitraryBitMask = (Ipv4MatchArbitraryBitMask) storedLayer3Match;
            if (storedIpv4MatchArbitraryBitMask.getIpv4DestinationAddressNoMask() != null) {
                Ipv4Prefix ipv4PrefixDestination;
                if (storedIpv4MatchArbitraryBitMask.getIpv4DestinationArbitraryBitmask() != null) {
                    byte[] destByteMask = convertArbitraryMaskToByteArray(storedIpv4MatchArbitraryBitMask.getIpv4DestinationArbitraryBitmask());
                    ipv4PrefixDestination = createPrefix(storedIpv4MatchArbitraryBitMask.getIpv4DestinationAddressNoMask(), destByteMask);
                } else {
                    ipv4PrefixDestination = createPrefix(storedIpv4MatchArbitraryBitMask.getIpv4DestinationAddressNoMask());
                }
                verdict = MatchComparatorHelper.compareIpv4PrefixNullSafe(ipv4PrefixDestination, statsIpv4Match.getIpv4Destination());
                if (verdict == false) {
                    return verdict;
                }
            }
            if (storedIpv4MatchArbitraryBitMask.getIpv4SourceAddressNoMask() != null) {
                Ipv4Prefix ipv4PrefixSource;
                if (storedIpv4MatchArbitraryBitMask.getIpv4SourceArbitraryBitmask() != null) {
                    byte[] srcByteMask = convertArbitraryMaskToByteArray(storedIpv4MatchArbitraryBitMask.getIpv4SourceArbitraryBitmask());
                    ipv4PrefixSource = createPrefix(storedIpv4MatchArbitraryBitMask.getIpv4SourceAddressNoMask(), srcByteMask);
                } else {
                    ipv4PrefixSource = createPrefix(storedIpv4MatchArbitraryBitMask.getIpv4SourceAddressNoMask());
                }
                verdict = MatchComparatorHelper.compareIpv4PrefixNullSafe(ipv4PrefixSource, statsIpv4Match.getIpv4Source());
            }
        } else if (statsLayer3Match instanceof Ipv6MatchArbitraryBitMask && storedLayer3Match instanceof Ipv6MatchArbitraryBitMask) {
            // At this moment storedIpv6MatchArbitraryBitMask & statsIpv6MatchArbitraryBitMask will always have non null arbitrary masks.
            // In case of no / null arbitrary mask, statsLayer3Match will be an instance of Ipv6Match.
            // Eg:- stats src/dest  -> 2001:2001:2001:2001:2001:2001:2001:2001/FFFF:FFFF:FFFF:FFFF:0000:FFFF:FFFF:FFF0
            //     stored src/dest  -> 2001:2001:2001:2001:2001:2001:2001:2001/FFFF:FFFF:FFFF:FFFF:0000:FFFF:FFFF:FFF0
            final Ipv6MatchArbitraryBitMask statsIpv6MatchArbitraryBitMask= (Ipv6MatchArbitraryBitMask) statsLayer3Match;
            final Ipv6MatchArbitraryBitMask storedIpv6MatchArbitraryBitMask = (Ipv6MatchArbitraryBitMask) storedLayer3Match;
            if ((storedIpv6MatchArbitraryBitMask.getIpv6DestinationAddressNoMask() != null |
                    storedIpv6MatchArbitraryBitMask.getIpv6SourceAddressNoMask() != null)) {
                if (storedIpv6MatchArbitraryBitMask.getIpv6DestinationAddressNoMask() != null) {
                    String storedDstIpAddress = normalizeIpv6Address(storedIpv6MatchArbitraryBitMask.getIpv6DestinationAddressNoMask(),
                            storedIpv6MatchArbitraryBitMask.getIpv6DestinationArbitraryBitmask());
                    String statsDstIpAddress = normalizeIpv6Address(statsIpv6MatchArbitraryBitMask.getIpv6DestinationAddressNoMask(),
                            statsIpv6MatchArbitraryBitMask.getIpv6DestinationArbitraryBitmask());
                    String storedDstMask = extractIpv6CanonicalForm(storedIpv6MatchArbitraryBitMask.
                            getIpv6DestinationArbitraryBitmask().getValue()).getHostAddress();
                    String statsDstMask = extractIpv6CanonicalForm(statsIpv6MatchArbitraryBitMask.
                            getIpv6DestinationArbitraryBitmask().getValue()).getHostAddress();
                    if (MatchComparatorHelper.compareStringNullSafe(storedDstMask,statsDstMask)) {
                        verdict = MatchComparatorHelper.compareStringNullSafe(storedDstIpAddress,
                                statsDstIpAddress);
                    } else {
                        verdict = false;
                        return verdict;
                    }
                }
                if (storedIpv6MatchArbitraryBitMask.getIpv6SourceAddressNoMask() != null) {
                    String storedSrcIpAddress = normalizeIpv6Address(storedIpv6MatchArbitraryBitMask.getIpv6SourceAddressNoMask()
                            ,storedIpv6MatchArbitraryBitMask.getIpv6SourceArbitraryBitmask());
                    String statsSrcIpAddress = normalizeIpv6Address(statsIpv6MatchArbitraryBitMask.getIpv6SourceAddressNoMask()
                            ,statsIpv6MatchArbitraryBitMask.getIpv6SourceArbitraryBitmask());
                    String storedSrcMask = extractIpv6CanonicalForm(storedIpv6MatchArbitraryBitMask.
                            getIpv6SourceArbitraryBitmask().getValue()).getHostAddress();
                    String statsSrcMask = extractIpv6CanonicalForm(statsIpv6MatchArbitraryBitMask.
                            getIpv6SourceArbitraryBitmask().getValue()).getHostAddress();
                    if (MatchComparatorHelper.compareStringNullSafe(storedSrcMask, statsSrcMask)) {
                        verdict = MatchComparatorHelper.compareStringNullSafe(storedSrcIpAddress,
                                statsSrcIpAddress);
                    } else {
                        verdict = false;
                    }
                }
            } else {
                final Boolean nullCheckOut = checkNullValues(storedLayer3Match, statsLayer3Match);
                if (nullCheckOut != null) {
                    verdict = nullCheckOut;
                } else {
                    verdict = storedLayer3Match.equals(statsLayer3Match);
                }
            }
        } else if (statsLayer3Match instanceof Ipv6Match && storedLayer3Match instanceof Ipv6MatchArbitraryBitMask) {
            // Here stored netmask is an instance of Ipv6MatchArbitraryBitMask, when it is pushed in to switch
            // it automatically converts it in to cidr format in case of certain subnet masks ( consecutive ones or zeroes)
            // Eg:- stats src/dest -> 2001:2001:2001:2001:2001:2001:2001:2001/124
            // stored src/dest -> 2001:2001:2001:2001:2001:2001:2001:2001/FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFF0
            final Ipv6Match statsIpv6Match = (Ipv6Match) statsLayer3Match;
            final Ipv6MatchArbitraryBitMask storedIpv6MatchArbitraryBitMask = (Ipv6MatchArbitraryBitMask) storedLayer3Match;
            if (storedIpv6MatchArbitraryBitMask.getIpv6DestinationAddressNoMask() != null) {
                Ipv6Prefix ipv6PrefixDestination;
                if (storedIpv6MatchArbitraryBitMask.getIpv6DestinationArbitraryBitmask() != null) {
                    byte[] destByteMask = convertIpv6ArbitraryMaskToByteArray(storedIpv6MatchArbitraryBitMask.getIpv6DestinationArbitraryBitmask());
                    ipv6PrefixDestination = createPrefix(storedIpv6MatchArbitraryBitMask.getIpv6DestinationAddressNoMask(), destByteMask);
                } else {
                    ipv6PrefixDestination = createPrefix(storedIpv6MatchArbitraryBitMask.getIpv6DestinationAddressNoMask());
                }
                verdict = MatchComparatorHelper.compareIpv6PrefixNullSafe(ipv6PrefixDestination, statsIpv6Match.getIpv6Destination());
                if (verdict == false) {
                    return verdict;
                }
            }
            if (storedIpv6MatchArbitraryBitMask.getIpv6SourceAddressNoMask() != null) {
                Ipv6Prefix ipv6PrefixSource;
                if (storedIpv6MatchArbitraryBitMask.getIpv6SourceArbitraryBitmask() != null) {
                    byte[] srcByteMask = convertIpv6ArbitraryMaskToByteArray(storedIpv6MatchArbitraryBitMask.getIpv6SourceArbitraryBitmask());
                    ipv6PrefixSource = createPrefix(storedIpv6MatchArbitraryBitMask.getIpv6SourceAddressNoMask(), srcByteMask);
                } else {
                    ipv6PrefixSource = createPrefix(storedIpv6MatchArbitraryBitMask.getIpv6SourceAddressNoMask());
                }
                verdict = MatchComparatorHelper.compareIpv6PrefixNullSafe(ipv6PrefixSource, statsIpv6Match.getIpv6Source());
            }
        } else if (statsLayer3Match instanceof ArpMatch && storedLayer3Match instanceof ArpMatch) {
            verdict = arpMatchEquals((ArpMatch)statsLayer3Match, (ArpMatch)storedLayer3Match);
        } else {
            final Boolean nullCheckOut = checkNullValues(storedLayer3Match, statsLayer3Match);
            if (nullCheckOut != null) {
                verdict = nullCheckOut;
            } else {
                verdict = storedLayer3Match.equals(statsLayer3Match);
            }
        }
        return verdict;
    }

    static boolean arpMatchEquals(final ArpMatch statsArpMatch, final ArpMatch storedArpMatch) {

        Integer statsOp = statsArpMatch.getArpOp();
        Integer storedOp = storedArpMatch.getArpOp();

        Boolean nullCheck = checkNullValues(statsOp, storedOp);
        if (nullCheck != null) {
            if (nullCheck == false) {
                return false;
            }
        } else if (!statsOp.equals(storedOp)) {
            return false;
        }

        Ipv4Prefix statsIp = statsArpMatch.getArpSourceTransportAddress();
        Ipv4Prefix storedIp = storedArpMatch.getArpSourceTransportAddress();
        if (!compareIpv4PrefixNullSafe(statsIp, storedIp)) {
            return false;
        }

        statsIp = statsArpMatch.getArpTargetTransportAddress();
        storedIp = storedArpMatch.getArpTargetTransportAddress();
        if (!compareIpv4PrefixNullSafe(statsIp, storedIp)) {
            return false;
        }

        MacAddressFilter statsMac = statsArpMatch.getArpSourceHardwareAddress();
        MacAddressFilter storedMac = storedArpMatch.getArpSourceHardwareAddress();
        if (!ethernetMatchFieldsEquals(statsMac, storedMac)) {
            return false;
        }

        statsMac = statsArpMatch.getArpTargetHardwareAddress();
        storedMac = storedArpMatch.getArpTargetHardwareAddress();
        if (!ethernetMatchFieldsEquals(statsMac, storedMac)) {
            return false;
        }

        return true;
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
        if (! (statsIpMask.length > 1 && storedIpMask.length > 1 &&  statsIpMask[1].equals(storedIpMask[1]))){
            return false;
        }

        final int prefix = Integer.parseInt(statsIpMask[1]);
        final int byteIndex = prefix/BYTE_SIZE;
        final int lastByteBits = BYTE_SIZE - (prefix % BYTE_SIZE);
        final InetAddress statsIp = InetAddresses.forString(statsIpMask[0]);
        final InetAddress storedIp = InetAddresses.forString(storedIpMask[0]);
        byte[] statsIpArr = Arrays.copyOfRange(statsIp.getAddress(),0,byteIndex+1);
        byte[] storedIpArr = Arrays.copyOfRange(storedIp.getAddress(),0,byteIndex+1);
        statsIpArr[byteIndex] = (byte) (statsIpArr[byteIndex] & (0XFF << lastByteBits));
        storedIpArr[byteIndex] = (byte) (storedIpArr[byteIndex] & (0XFF << lastByteBits));
        if(Arrays.equals(statsIpArr,storedIpArr)) {
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

    static boolean compareStringNullSafe(final String stringA, final String stringB) {
        boolean verdict = true;
        final Boolean checkDestNullValuesOut = checkNullValues(stringA,stringB);
        if (checkDestNullValuesOut != null) {
            verdict = checkDestNullValuesOut;
        } else if (!stringA.equals(stringB)) {
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

    static boolean isArbitraryBitMask(byte[] byteMask) {
        if (byteMask == null) {
            return false;
        } else {
            ArrayList<Integer> integerMaskArrayList = new ArrayList<Integer>();
            String maskInBits;
            // converting byte array to bits
            maskInBits = new BigInteger(1, byteMask).toString(2);
            ArrayList<String> stringMaskArrayList = new ArrayList<String>(Arrays.asList(maskInBits.split("(?!^)")));
            for(String string:stringMaskArrayList){
                integerMaskArrayList.add(Integer.parseInt(string));
            }
            return checkArbitraryBitMask(integerMaskArrayList);
        }
    }

    static boolean checkArbitraryBitMask(ArrayList<Integer> arrayList) {
        if (arrayList.size()>0 && arrayList.size()< IPV4_MASK_LENGTH ) {
            // checks 0*1* case - Leading zeros in arrayList are truncated
            return true;
        } else {
            //checks 1*0*1 case
            for(int i=0; i<arrayList.size()-1;i++) {
                if(arrayList.get(i) ==0 && arrayList.get(i+1) == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    static final byte[] convertArbitraryMaskToByteArray(DottedQuad mask) {
        String maskValue;
        if (mask.getValue() != null) {
            maskValue  = mask.getValue();
        } else {
            maskValue = DEFAULT_ARBITRARY_BIT_MASK;
        }
        InetAddress maskInIpFormat = null;
        try {
            maskInIpFormat = InetAddress.getByName(maskValue);
        } catch (UnknownHostException e) {
            LOG.error("Failed to recognize the host while converting mask ", e);
        }
        byte[] bytes = maskInIpFormat.getAddress();
        return bytes;
    }

    private static final byte[] convertIpv6ArbitraryMaskToByteArray ( final Ipv6ArbitraryMask mask) {
        String maskValue;
        if (mask.getValue() != null) {
            maskValue = mask.getValue();
        } else {
            maskValue = DEFAULT_IPV6_ARBITRARY_BIT_MASK;
        }
        InetAddress maskInIpv6Format = null;
        try {
            maskInIpv6Format = InetAddress.getByName(maskValue);
        } catch (UnknownHostException e) {
            LOG.error("Failed to convert string mask value to ipv6 format ", e);
        }
        return maskInIpv6Format.getAddress();
    }

    static String normalizeIpv4Address(Ipv4Address ipAddress, DottedQuad netMask) {
        String actualIpAddress="";
        String[] netMaskParts = netMask.getValue().split("\\.");
        String[] ipAddressParts = ipAddress.getValue().split("\\.");

        for (int i=0; i<ipAddressParts.length;i++) {
            int integerFormatIpAddress=Integer.parseInt(ipAddressParts[i]);
            int integerFormatNetMask=Integer.parseInt(netMaskParts[i]);
            int ipAddressPart=(integerFormatIpAddress) & (integerFormatNetMask);
            actualIpAddress += ipAddressPart;
            if (i != ipAddressParts.length -1 ) {
                actualIpAddress = actualIpAddress+".";
            }
        }
        return actualIpAddress;
    }

    private static String normalizeIpv6Address(final Ipv6Address ipAddress, final Ipv6ArbitraryMask netMask) {
        byte[] ipAddressParts = convertIpv6ToBytes(ipAddress.getValue());
        byte[] netMaskParts  = convertIpv6ToBytes(netMask.getValue());
        byte[] actualIpv6Bytes = new byte[16];

        for (int i=0; i<ipAddressParts.length;i++) {
            byte ipAddressPart= (byte) (ipAddressParts[i] & netMaskParts[i]);
            actualIpv6Bytes[i] = ipAddressPart;
        }
        InetAddress ipv6Address = null;
        try {
            ipv6Address = InetAddress.getByAddress(actualIpv6Bytes);
        } catch (UnknownHostException e) {
            LOG.error("Failed to recognize the host while normalizing IPv6 address from bytes ", e);
        }
        return ipv6Address.getHostAddress();
    }

    private static byte[] convertIpv6ToBytes(final String ipv6Address) {
        return extractIpv6CanonicalForm(ipv6Address).getAddress();
    }

    private static InetAddress extractIpv6CanonicalForm(final String ipv6Address) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(ipv6Address);
        } catch (UnknownHostException e) {
            LOG.error("Failed to recognize the host while converting IPv6 to bytes ", e);
        }
        return address;
    }

    static Ipv4Prefix createPrefix(final Ipv4Address ipv4Address, final byte [] bytemask){
        return createPrefix(ipv4Address, String.valueOf(countBits(bytemask)));
    }

    private static Ipv6Prefix createPrefix(final Ipv6Address ipv6Address, final byte [] bytemask) {
        return createPrefix(ipv6Address, String.valueOf(countBits(bytemask)));
    }

    private static Ipv6Prefix createPrefix(final Ipv6Address ipv6Address, final String mask) {
        if (mask != null && !mask.isEmpty()) {
            return new Ipv6Prefix(ipv6Address.getValue() + PREFIX_SEPARATOR + mask);
        } else {
            return new Ipv6Prefix(ipv6Address.getValue() + PREFIX_SEPARATOR + IPV6_ADDRESS_LENGTH);
        }
    }

    private static Ipv6Prefix createPrefix(final Ipv6Address ipv6Address) {
        return new Ipv6Prefix(ipv6Address.getValue() + PREFIX_SEPARATOR + IPV6_ADDRESS_LENGTH);
    }

    static int countBits(final byte[] mask) {
        int netmask = 0;
        for (byte b : mask) {
            netmask += Integer.bitCount(UnsignedBytes.toInt(b));
        }
        return netmask;
    }

    static Ipv4Prefix createPrefix(final Ipv4Address ipv4Address){
        return new Ipv4Prefix(ipv4Address.getValue() + PREFIX_SEPARATOR + IPV4_ADDRESS_LENGTH);
    }

    static Ipv4Prefix createPrefix(final Ipv4Address ipv4Address, final String mask){
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
}
