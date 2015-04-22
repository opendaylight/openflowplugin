package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.TunIPv4SrcConvertor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.ExtensionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.tun.ipv4.src.grouping.NxmNxTunIpv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.tun.ipv4.src.grouping.NxmNxTunIpv4SrcBuilder;

public class TunIpv4SrcConverterTest {

    private static final Ipv4Address IPV4_ADDRESS = new Ipv4Address("1.2.3.4");
    private static final Long IPV4_LONG = new Long(16909060L);

    @Test
    public void testIpv4toLong() {
        TunIPv4SrcConvertor converter = new TunIPv4SrcConvertor();
        Long result = converter.ipv4ToLong(IPV4_ADDRESS);
        assertEquals("Does not match",IPV4_LONG,result);
    }

    @Test
    public void testLongtoIpv4() {
        TunIPv4SrcConvertor converter = new TunIPv4SrcConvertor();
        Ipv4Address result = converter.longToIpv4Address(16909060L);
        assertEquals("Does not match",IPV4_ADDRESS,result);
    }

}
