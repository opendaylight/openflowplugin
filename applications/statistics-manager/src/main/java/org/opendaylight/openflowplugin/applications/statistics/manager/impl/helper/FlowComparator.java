/*
 * Copyright IBM Corporation, 2013.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.MacAddressFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;

/**
 * Utility class for comparing flows.
 */
public final class FlowComparator {

    private static class EqualityException extends RuntimeException {
    }

    private static final int DEFAULT_SUBNET = 32;
    private static final int SHIFT_OCTET_1 = 24;
    private static final int SHIFT_OCTET_2 = 16;
    private static final int SHIFT_OCTET_3 = 8;
    private static final int SHIFT_OCTET_4 = 0;
    private static final int POSITION_OCTET_1 = 0;
    private static final int POSITION_OCTET_2 = 1;
    private static final int POSITION_OCTET_3 = 2;
    private static final int POSITION_OCTET_4 = 3;

    private FlowComparator() {
        throw new UnsupportedOperationException("Utilities class should not be instantiated");
    }

    public static boolean flowEquals(final Flow statsFlow, final Flow storedFlow) {
        if (statsFlow == null || storedFlow == null) {
            return false;
        }
        try {
            compareFlowsByContainerName(statsFlow, storedFlow);
            compareFlowsByPriority(statsFlow, storedFlow);
            compareFlowsByMatch(statsFlow, storedFlow);
            compareFlowsByTableId(statsFlow, storedFlow);
        } catch (EqualityException e) {
            return false;
        }
        return true;
    }

    private static void compareFlowsByContainerName(final Flow statsFlow, final Flow storedFlow) throws EqualityException {
        if (statsFlow.getContainerName()== null) {
            if (storedFlow.getContainerName()!= null) {
                throw new EqualityException();
            }
        } else if(!statsFlow.getContainerName().equals(storedFlow.getContainerName())) {
            throw new EqualityException();
        }
    }

    private static void compareFlowsByPriority(final Flow statsFlow, final Flow storedFlow) throws EqualityException {
        if (storedFlow.getPriority() == null) {
            if (statsFlow.getPriority() != null && statsFlow.getPriority()!= 0x8000) {
                throw new EqualityException();
            }
        } else if(!statsFlow.getPriority().equals(storedFlow.getPriority())) {
            throw new EqualityException();
        }
    }

    private static void compareFlowsByMatch(final Flow statsFlow, final Flow storedFlow) throws EqualityException {
        if (statsFlow.getMatch()== null) {
            if (storedFlow.getMatch() != null) {
                throw new EqualityException();
            }
        } else if(!matchEquals(statsFlow.getMatch(), storedFlow.getMatch())) {
            throw new EqualityException();
        }
    }

    private static void compareFlowsByTableId(final Flow statsFlow, final Flow storedFlow) throws EqualityException {
        if (statsFlow.getTableId() == null) {
            if (storedFlow.getTableId() != null) {
                throw new EqualityException();
            }
        } else if(!statsFlow.getTableId().equals(storedFlow.getTableId())) {
            throw new EqualityException();
        }
    }

    /**
     * Explicit equals method to compare the 'match' for flows stored in the data-stores and flow fetched from the switch.
     * Flow installation process has three steps
     * 1) Store flow in config data store
     * 2) and send it to plugin for installation
     * 3) Flow gets installed in switch
     *
     * The flow user wants to install and what finally gets installed in switch can be slightly different.
     * E.g, If user installs flow with src/dst ip=10.0.0.1/24, when it get installed in the switch
     * src/dst ip will be changes to 10.0.0.0/24 because of netmask of 24. When statistics manager fetch
     * stats it gets 10.0.0.0/24 rather then 10.0.0.1/24. Custom match takes care of by using masked ip
     * while comparing two ip addresses.
     *
     * Sometimes when user don't provide few values that is required by flow installation request, like
     * priority,hard timeout, idle timeout, cookies etc, plugin usages default values before sending
     * request to the switch. So when statistics manager gets flow statistics, it gets the default value.
     * But the flow stored in config data store don't have those defaults value. I included those checks
     * in the customer flow/match equal function.
     *
     *
     * @param statsFlow
     * @param storedFlow
     * @return
     */
    public static boolean matchEquals(final Match statsFlow, final Match storedFlow) {
        if (statsFlow == storedFlow) {
            return true;
        }
        try {
            compareMathesAsWholeObject(statsFlow, storedFlow);
            compareMatchesByEthernet(statsFlow, storedFlow);
            compareMatchesByIcmpv4(statsFlow, storedFlow);
            compareMatchesByInPhyPort(statsFlow, storedFlow);
            compareMatchesByInPort(statsFlow, storedFlow);
            compareMatchesByIp(statsFlow, storedFlow);
            compareMatchesByLayer3(statsFlow, storedFlow);
            compareMatchesByLayer4(statsFlow, storedFlow);
            compareMatchesByMetadata(statsFlow, storedFlow);
            compareMatchesByProtocolFields(statsFlow, storedFlow);
            compareMatchesByTunnel(statsFlow, storedFlow);
            compareMatchesByVlan(statsFlow, storedFlow);
        } catch (IllegalStateException e) {
            return false;
        }
        return true;
    }

    private static void compareMathesAsWholeObject(final Match statsFlow, final Match storedFlow) {
        if (storedFlow == null && statsFlow != null) {
            throw new EqualityException();
        }
        if (statsFlow == null && storedFlow != null) {
            throw new EqualityException();
        }
    }

    private static void compareMatchesByVlan(Match statsFlow, Match storedFlow) {
        if (storedFlow.getVlanMatch()== null) {
            if (statsFlow.getVlanMatch() != null) {
                throw new EqualityException();
            }
        } else if(!storedFlow.getVlanMatch().equals(statsFlow.getVlanMatch())) {
            throw new EqualityException();
        }
    }

    private static void compareMatchesByTunnel(Match statsFlow, Match storedFlow) {
        if (storedFlow.getTunnel()== null) {
            if (statsFlow.getTunnel() != null) {
                throw new EqualityException();
            }
        } else if(!storedFlow.getTunnel().equals(statsFlow.getTunnel())) {
            throw new EqualityException();
        }
    }

    private static void compareMatchesByProtocolFields(Match statsFlow, Match storedFlow) {
        if (storedFlow.getProtocolMatchFields() == null) {
            if (statsFlow.getProtocolMatchFields() != null) {
                throw new EqualityException();
            }
        } else if(!storedFlow.getProtocolMatchFields().equals(statsFlow.getProtocolMatchFields())) {
            throw new EqualityException();
        }
    }

    private static void compareMatchesByMetadata(Match statsFlow, Match storedFlow) {
        if (storedFlow.getMetadata() == null) {
            if (statsFlow.getMetadata() != null) {
                throw new EqualityException();
            }
        } else if(!storedFlow.getMetadata().equals(statsFlow.getMetadata())) {
            throw new EqualityException();
        }
    }

    private static void compareMatchesByLayer4(Match statsFlow, Match storedFlow) {
        if (storedFlow.getLayer4Match()== null) {
            if (statsFlow.getLayer4Match() != null) {
                throw new EqualityException();
            }
        } else if(!storedFlow.getLayer4Match().equals(statsFlow.getLayer4Match())) {
            throw new EqualityException();
        }
    }

    private static void compareMatchesByLayer3(Match statsFlow, Match storedFlow) {
        if (storedFlow.getLayer3Match()== null) {
            if (statsFlow.getLayer3Match() != null) {
                throw new EqualityException();
            }
        } else if(!layer3MatchEquals(statsFlow.getLayer3Match(),storedFlow.getLayer3Match())) {
            throw new EqualityException();
        }
    }

    private static void compareMatchesByIp(Match statsFlow, Match storedFlow) {
        if (storedFlow.getIpMatch()== null) {
            if (statsFlow.getIpMatch() != null) {
                throw new EqualityException();
            }
        } else if(!storedFlow.getIpMatch().equals(statsFlow.getIpMatch())) {
            throw new EqualityException();
        }
    }

    private static void compareMatchesByInPort(Match statsFlow, Match storedFlow) {
        if (storedFlow.getInPort()== null) {
            if (statsFlow.getInPort() != null) {
                throw new EqualityException();
            }
        } else if(!storedFlow.getInPort().equals(statsFlow.getInPort())) {
            throw new EqualityException();
        }
    }

    private static void compareMatchesByInPhyPort(Match statsFlow, Match storedFlow) {
        if (storedFlow.getInPhyPort() == null) {
            if (statsFlow.getInPhyPort() != null) {
                throw new EqualityException();
            }
        } else if(!storedFlow.getInPhyPort().equals(statsFlow.getInPhyPort())) {
            throw new EqualityException();
        }
    }

    private static void compareMatchesByEthernet(Match statsFlow, Match storedFlow) {
        if (storedFlow.getEthernetMatch() == null) {
            if (statsFlow.getEthernetMatch() != null) {
                throw new EqualityException();
            }
        } else if(!ethernetMatchEquals(statsFlow.getEthernetMatch(),storedFlow.getEthernetMatch())) {
            throw new EqualityException();
        }
    }

    private static void compareMatchesByIcmpv4(Match statsFlow, Match storedFlow) {
        if (storedFlow.getIcmpv4Match()== null) {
            if (statsFlow.getIcmpv4Match() != null) {
                throw new EqualityException();
            }
        } else if(!storedFlow.getIcmpv4Match().equals(statsFlow.getIcmpv4Match())) {
            throw new EqualityException();
        }
    }

    /*
     * Custom EthernetMatch is required because mac address string provided by user in EthernetMatch can be in
     * any case (upper or lower or mix). Ethernet Match which controller receives from switch is always
     * an upper case string. Default EthernetMatch equals doesn't use equalsIgnoreCase() and hence it fails.
     * E.g User provided mac address string in flow match is aa:bb:cc:dd:ee:ff and when controller fetch
     * statistic data, openflow driver library returns AA:BB:CC:DD:EE:FF and default eqauls fails here.
     */
    @VisibleForTesting
    static boolean ethernetMatchEquals(final EthernetMatch statsEthernetMatch, final EthernetMatch storedEthernetMatch){
        boolean verdict = true;
        final Boolean checkNullValues = checkNullValues(statsEthernetMatch, storedEthernetMatch);
        if (checkNullValues != null) {
            verdict = checkNullValues;
        } else {
            if(verdict){
                verdict = ethernetMatchFieldsEquals(statsEthernetMatch.getEthernetSource(),storedEthernetMatch.getEthernetSource());
            }
            if(verdict){
                verdict = ethernetMatchFieldsEquals(statsEthernetMatch.getEthernetDestination(),storedEthernetMatch.getEthernetDestination());
            }
            if(verdict){
                if(statsEthernetMatch.getEthernetType() == null){
                    if(storedEthernetMatch.getEthernetType() != null){
                        verdict = false;
                    }
                }else{
                    verdict = statsEthernetMatch.getEthernetType().equals(storedEthernetMatch.getEthernetType());
                }
            }
        }
        return verdict;
    }

    private static boolean ethernetMatchFieldsEquals(final MacAddressFilter statsEthernetMatchFields,
                                                        final MacAddressFilter storedEthernetMatchFields){
        boolean verdict = true;
        final Boolean checkNullValues = checkNullValues(statsEthernetMatchFields, storedEthernetMatchFields);
        if (checkNullValues != null) {
            verdict = checkNullValues;
        } else {
            if(verdict){
                verdict = macAddressEquals(statsEthernetMatchFields.getAddress(),storedEthernetMatchFields.getAddress());
            }
            if(verdict){
                verdict = macAddressEquals(statsEthernetMatchFields.getMask(),storedEthernetMatchFields.getMask());
            }
        }
        return verdict;
    }

    private static boolean macAddressEquals(final MacAddress statsMacAddress, final MacAddress storedMacAddress){
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
    static boolean layer3MatchEquals(final Layer3Match statsLayer3Match, final Layer3Match storedLayer3Match){
        boolean verdict = true;
        if(statsLayer3Match instanceof Ipv4Match && storedLayer3Match instanceof Ipv4Match){
            final Ipv4Match statsIpv4Match = (Ipv4Match)statsLayer3Match;
            final Ipv4Match storedIpv4Match = (Ipv4Match)storedLayer3Match;

            if (verdict) {
                verdict = compareNullSafe(
                        storedIpv4Match.getIpv4Destination(), statsIpv4Match.getIpv4Destination());
            }
            if (verdict) {
                verdict = compareNullSafe(
                        statsIpv4Match.getIpv4Source(), storedIpv4Match.getIpv4Source());
            }
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

    private static boolean compareNullSafe(final Ipv4Prefix statsIpv4, final Ipv4Prefix storedIpv4) {
        boolean verdict = true;
        final Boolean checkDestNullValuesOut = checkNullValues(storedIpv4, statsIpv4);
        if (checkDestNullValuesOut != null) {
            verdict = checkDestNullValuesOut;
        } else if(!IpAddressEquals(statsIpv4, storedIpv4)){
            verdict = false;
        }

        return verdict;
    }

    private static Boolean checkNullValues(final Object v1, final Object v2) {
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

    /**
     * TODO: why don't we use the default Ipv4Prefix.equals()?
     *
     * @param statsIpAddress
     * @param storedIpAddress
     * @return true if IPv4prefixes equals
     */
    private static boolean IpAddressEquals(final Ipv4Prefix statsIpAddress, final Ipv4Prefix storedIpAddress) {
        final IntegerIpAddress statsIpAddressInt = strIpToIntIp(statsIpAddress.getValue());
        final IntegerIpAddress storedIpAddressInt = strIpToIntIp(storedIpAddress.getValue());

        if(ipAndMaskBasedMatch(statsIpAddressInt,storedIpAddressInt)){
            return true;
        }
        if(ipBasedMatch(statsIpAddressInt,storedIpAddressInt)){
            return true;
        }
        return false;
    }

    private static boolean ipAndMaskBasedMatch(final IntegerIpAddress statsIpAddressInt,final IntegerIpAddress storedIpAddressInt){
        return ((statsIpAddressInt.getIp() & statsIpAddressInt.getMask()) ==  (storedIpAddressInt.getIp() & storedIpAddressInt.getMask()));
    }

    private static boolean ipBasedMatch(final IntegerIpAddress statsIpAddressInt,final IntegerIpAddress storedIpAddressInt){
        return (statsIpAddressInt.getIp() == storedIpAddressInt.getIp());
    }

    /**
     * Method return integer version of ip address. Converted int will be mask if
     * mask specified
     */
    private static IntegerIpAddress strIpToIntIp(final String ipAddresss){

        final String[] parts = ipAddresss.split("/");
        final String ip = parts[0];
        int prefix;

        if (parts.length < 2) {
            prefix = DEFAULT_SUBNET;
        } else {
            prefix = Integer.parseInt(parts[1]);
        }

        IntegerIpAddress integerIpAddress = null;

            final Inet4Address addr = ((Inet4Address) InetAddresses.forString(ip));
            final byte[] addrBytes = addr.getAddress();
            //FIXME: what is meaning of anding with 0xFF? Probably could be removed.
            final int ipInt = ((addrBytes[POSITION_OCTET_1] & 0xFF) << SHIFT_OCTET_1) |
                    ((addrBytes[POSITION_OCTET_2] & 0xFF) << SHIFT_OCTET_2) |
                    ((addrBytes[POSITION_OCTET_3] & 0xFF) << SHIFT_OCTET_3)  |
                    ((addrBytes[POSITION_OCTET_4] & 0xFF) << SHIFT_OCTET_4);

            // FIXME: Is this valid?
            final int mask = 0xffffffff << DEFAULT_SUBNET - prefix;

            integerIpAddress = new IntegerIpAddress(ipInt, mask);


        return integerIpAddress;
    }

    private static class IntegerIpAddress{
        int ip;
        int mask;
        public IntegerIpAddress(final int ip, final int mask) {
            this.ip = ip;
            this.mask = mask;
        }
        public int getIp() {
            return ip;
        }
        public int getMask() {
            return mask;
        }
    }
}
