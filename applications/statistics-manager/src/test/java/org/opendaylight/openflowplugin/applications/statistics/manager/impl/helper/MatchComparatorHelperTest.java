package org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.junit.Test;

/**
 * test of {@link MatchComparatorHelper}
 */
public class MatchComparatorHelperTest {

    /**
     * mask for /32
     */
    private static final int DEFAULT_IPV4_MASK = 0xffffffff;

    /**
     * mask for /30
     */
    private static final int IPV4_30_MASK = 0xfffffffc;
    private static final int IP_ADDRESS = 0xC0A80101;

    /**
     * The test of conversion valid IP addres without mask to binary form.
     */
    @Test
    public void validIpWithoutMaskTest() {
        IntegerIpAddress intIp = MatchComparatorHelper.strIpToIntIp("192.168.1.1");
        assertEquals(IP_ADDRESS, intIp.getIp());
        assertEquals(DEFAULT_IPV4_MASK, intIp.getMask());
    }

    /**
     * The test of conversion of valid IP address with valid mask to binary form.
     */
    @Test
    public void validIpWithValidMaskTest() {
        IntegerIpAddress intIp = MatchComparatorHelper.strIpToIntIp("192.168.1.1/30");
        assertEquals(IP_ADDRESS, intIp.getIp());
        assertEquals(IPV4_30_MASK, intIp.getMask());
    }

    /**
     * The test of conversion of valid IP address invalid mask to binary form.
     */
    @Test
    public void validIpWithInvalidMaskTest() {
        try {
            MatchComparatorHelper.strIpToIntIp("192.168.1.1/40");
        } catch (IllegalStateException e) {
            assertEquals("Valid values for mask are from range 0 - 32. Value 40 is invalid.", e.getMessage());
            return;
        }
        fail("IllegalStateException was awaited (40 subnet is invalid)");
    }

    /**
     * The test of conversion invalid IP address with valid mask to binary form.
     */
    @Test
    public void invalidIpWithValidMaskTest() {
        try {
            MatchComparatorHelper.strIpToIntIp("257.168.1.1/25");
        } catch (IllegalArgumentException e) {
            assertEquals("'257.168.1.1' is not an IP string literal.", e.getMessage());
        }
    }

    @Test
    public void ethernetMatchEqualsTest() {
        final EthernetMatchBuilder statsEthernetBuilder = new EthernetMatchBuilder();
        final EthernetMatchBuilder storedEthernetBuilder = new EthernetMatchBuilder();

        assertEquals(true, MatchComparatorHelper.ethernetMatchEquals(null, null));

        statsEthernetBuilder.setEthernetSource(new EthernetSourceBuilder().setAddress(
                new MacAddress("11:22:33:44:55:66")).build());
        storedEthernetBuilder.setEthernetSource(new EthernetSourceBuilder().setAddress(
                new MacAddress("11:22:33:44:55:77")).build());
        assertEquals(false,
                MatchComparatorHelper.ethernetMatchEquals(statsEthernetBuilder.build(), storedEthernetBuilder.build()));

        storedEthernetBuilder.setEthernetSource(new EthernetSourceBuilder().setAddress(
                new MacAddress("11:22:33:44:55:66")).build());
        statsEthernetBuilder.setEthernetDestination(new EthernetDestinationBuilder().setAddress(
                new MacAddress("66:55:44:33:22:11")).build());
        storedEthernetBuilder.setEthernetDestination(new EthernetDestinationBuilder().setAddress(
                new MacAddress("77:55:44:33:22:11")).build());
        assertEquals(false,
                MatchComparatorHelper.ethernetMatchEquals(statsEthernetBuilder.build(), storedEthernetBuilder.build()));

        storedEthernetBuilder.setEthernetDestination(new EthernetDestinationBuilder().setAddress(
                new MacAddress("66:55:44:33:22:11")).build());
        statsEthernetBuilder.setEthernetType(new EthernetTypeBuilder().setType(new EtherType((long) 1)).build());
        storedEthernetBuilder.setEthernetType(new EthernetTypeBuilder().setType(new EtherType((long) 1)).build());
        assertEquals(true,
                MatchComparatorHelper.ethernetMatchEquals(statsEthernetBuilder.build(), storedEthernetBuilder.build()));

        statsEthernetBuilder.setEthernetType(null).build();
        assertEquals(false,
                MatchComparatorHelper.ethernetMatchEquals(statsEthernetBuilder.build(), storedEthernetBuilder.build()));

        storedEthernetBuilder.setEthernetType(null).build();
        assertEquals(true,
                MatchComparatorHelper.ethernetMatchEquals(statsEthernetBuilder.build(), storedEthernetBuilder.build()));

    }

    @Test
    public void ethernetMatchFieldsEqualsTest() {
        final EthernetSourceBuilder statsBuilder = new EthernetSourceBuilder();
        final EthernetSourceBuilder storedBuilder = new EthernetSourceBuilder();

        assertEquals(true, MatchComparatorHelper.ethernetMatchFieldsEquals(null, null));

        statsBuilder.setAddress(new MacAddress("11:22:33:44:55:66"));
        storedBuilder.setAddress(new MacAddress("11:22:33:44:55:77"));
        assertEquals(false,
                MatchComparatorHelper.ethernetMatchFieldsEquals(statsBuilder.build(), storedBuilder.build()));

        storedBuilder.setAddress(new MacAddress("11:22:33:44:55:66"));
        assertEquals(true, MatchComparatorHelper.ethernetMatchFieldsEquals(statsBuilder.build(), storedBuilder.build()));
    }

    @Test
    public void macAddressEqualsTest() {
        assertEquals(true, MatchComparatorHelper.macAddressEquals(null, null));
        assertEquals(true, MatchComparatorHelper.macAddressEquals(new MacAddress("11:22:33:44:55:66"), new MacAddress(
                "11:22:33:44:55:66")));
        assertEquals(false, MatchComparatorHelper.macAddressEquals(new MacAddress("11:22:33:44:55:66"), new MacAddress(
                "11:22:33:44:55:77")));
    }

    @Test
    public void checkNullValuesTest() {
        assertEquals(false, MatchComparatorHelper.checkNullValues(null, ""));
        assertEquals(false, MatchComparatorHelper.checkNullValues("", null));
        assertEquals(true, MatchComparatorHelper.checkNullValues(null, null));
        assertTrue(MatchComparatorHelper.checkNullValues("", "") == null);
    }

    @Test
    public void compareIpv4PrefixNullSafeTest() {
        assertEquals(true, MatchComparatorHelper.compareIpv4PrefixNullSafe(null, null));
        assertEquals(true, MatchComparatorHelper.compareIpv4PrefixNullSafe(new Ipv4Prefix("192.168.1.1/31"),
                new Ipv4Prefix("192.168.1.1/31")));

        assertEquals(false, MatchComparatorHelper.compareIpv4PrefixNullSafe(new Ipv4Prefix("192.168.1.1/31"),
                new Ipv4Prefix("191.168.1.1/31")));
    }

    private static final int ip_192_168_1_1 = 0xC0A80101;
    private static final int ip_192_168_1_4 = 0xC0A80104;

    @Test
    public void ipBasedMatchTest() {
        // are equals because only IP address is compared
        assertEquals(true, MatchComparatorHelper.ipBasedMatch(new IntegerIpAddress(ip_192_168_1_1, 32),
                new IntegerIpAddress(ip_192_168_1_1, 16)));
    }

    @Test
    public void ipAndMaskBasedMatchTest() {
        // true because both cases are network 192.168.1.0
        assertEquals(true, MatchComparatorHelper.ipBasedMatch(new IntegerIpAddress(ip_192_168_1_1, 31),
                new IntegerIpAddress(ip_192_168_1_1, 30)));

        // false because first is network 192.168.1.0 and second is 192.168.1.4
        assertEquals(false, MatchComparatorHelper.ipBasedMatch(new IntegerIpAddress(ip_192_168_1_1, 31),
                new IntegerIpAddress(ip_192_168_1_4, 30)));
    }

    @Test
    public void layer3MatchEqualsTest() {
        final Ipv4MatchBuilder statsBuilder = new Ipv4MatchBuilder();
        final Ipv4MatchBuilder storedBuilder = new Ipv4MatchBuilder();
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        statsBuilder.setIpv4Destination(new Ipv4Prefix("192.168.1.1/30"));
        storedBuilder.setIpv4Destination(new Ipv4Prefix("191.168.1.1/30"));
        assertEquals(false, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(null, null));
        assertEquals(true,
                MatchComparatorHelper.layer3MatchEquals(new ArpMatchBuilder().build(), new ArpMatchBuilder().build()));
    }

    @Test
    public void layer3MatchEqualsIpv4ArbitraryMaskTest(){
        final Ipv4MatchBuilder statsBuilder = new Ipv4MatchBuilder();
        final Ipv4MatchArbitraryBitMaskBuilder storedBuilder = new Ipv4MatchArbitraryBitMaskBuilder();
        assertEquals(true,MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(),storedBuilder.build()));
        statsBuilder.setIpv4Destination(new Ipv4Prefix("192.168.1.1/24"));
        storedBuilder.setIpv4DestinationAddressNoMask(new Ipv4Address("192.168.1.1"));
        storedBuilder.setIpv4DestinationArbitraryBitMask(new DottedQuad("255.255.255.0"));
        statsBuilder.setIpv4Source(new Ipv4Prefix("192.168.1.1/24"));
        storedBuilder.setIpv4SourceAddressNoMask(new Ipv4Address("192.168.1.1"));
        storedBuilder.setIpv4SourceArbitraryBitMask(new DottedQuad("255.255.255.0"));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(null, null));
        assertEquals(true,
                MatchComparatorHelper.layer3MatchEquals(new ArpMatchBuilder().build(), new ArpMatchBuilder().build()));

    }

    @Test
    public void layer3MatchEqualsIpv4ArbitraryEmptyBitMaskTest(){
        final Ipv4MatchBuilder statsBuilder = new Ipv4MatchBuilder();
        final Ipv4MatchArbitraryBitMaskBuilder storedBuilder = new Ipv4MatchArbitraryBitMaskBuilder();
        assertEquals(true,MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(),storedBuilder.build()));
        statsBuilder.setIpv4Destination(new Ipv4Prefix("192.168.1.1/32"));
        storedBuilder.setIpv4DestinationAddressNoMask(new Ipv4Address("192.168.1.1"));
        statsBuilder.setIpv4Source(new Ipv4Prefix("192.168.1.1/32"));
        storedBuilder.setIpv4SourceAddressNoMask(new Ipv4Address("192.168.1.1"));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(statsBuilder.build(), storedBuilder.build()));
        assertEquals(true, MatchComparatorHelper.layer3MatchEquals(null, null));
        assertEquals(true,
                MatchComparatorHelper.layer3MatchEquals(new ArpMatchBuilder().build(), new ArpMatchBuilder().build()));
    }

}
