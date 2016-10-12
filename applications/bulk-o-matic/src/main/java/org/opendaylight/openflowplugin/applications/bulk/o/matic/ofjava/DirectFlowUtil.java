package org.opendaylight.openflowplugin.applications.bulk.o.matic.ofjava;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.applications.bulk.o.matic.BulkOMaticUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MatchTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthTypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.type._case.EthTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.src._case.Ipv4SrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;

public class DirectFlowUtil {
    private static final Splitter PREFIX_SPLITTER = Splitter.on('/');
    private static final byte[] VLAN_VID_MASK = new byte[]{16, 0};
    private static final Class<? extends MatchTypeBase> DEFAULT_MATCH_TYPE = OxmMatchType.class;

    // Pre-calculated masks for the 33 possible values. Do not give them out, but clone() them as they may
    // end up being leaked and vulnerable.
    private static final byte[][] IPV4_MASKS;

    static {
        final byte[][] tmp = new byte[33][];
        for (int i = 0; i <= 32; ++i) {
            final int mask = 0xffffffff << (32 - i);
            tmp[i] = new byte[]{(byte) (mask >>> 24), (byte) (mask >>> 16), (byte) (mask >>> 8), (byte) mask};
        }

        IPV4_MASKS = tmp;
    }

    public static FlowModInputBuilder buildFlow(final Short tableId, final List<MatchEntry> match) {
        return new FlowModInputBuilder()
                .setTableId(new TableId(tableId.longValue()))
                .setMatch(new MatchBuilder().setMatchEntry(match).setType(DEFAULT_MATCH_TYPE).build())
                // We are adding flows here, so set this flow mod to do it
                .setCommand(FlowModCommand.OFPFCADD)
                // Openflowplugin defaults
                .setVersion(OFConstants.OFP_VERSION_1_3)
                .setCookie(OFConstants.DEFAULT_COOKIE)
                .setCookieMask(OFConstants.DEFAULT_COOKIE_MASK)
                .setBufferId(OFConstants.OFP_NO_BUFFER)
                .setOutPort(new PortNumber(OFConstants.ANY))
                .setOutGroup(OFConstants.ANY)
                .setIdleTimeout(0)
                .setHardTimeout(0)
                .setPriority(OFConstants.DEFAULT_FLOW_PRIORITY)
                .setFlags(new FlowModFlags(false, false, false, false, false));
    }

    public static List<MatchEntry> buildMatch(final Integer sourceIp) {
        final List<MatchEntry> entries = new ArrayList<>();

        // Add IPv4 source
        Ipv4Prefix ipv4Prefix = new Ipv4Prefix(BulkOMaticUtils.ipIntToStr(sourceIp));
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setOxmMatchField(Ipv4Src.class);

        Ipv4SrcCaseBuilder ipv4SrcCaseBuilder = new Ipv4SrcCaseBuilder();
        Ipv4SrcBuilder ipv4SrcBuilder = new Ipv4SrcBuilder();

        Iterator<String> addressParts = PREFIX_SPLITTER.split(ipv4Prefix.getValue()).iterator();
        Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
        ipv4SrcBuilder.setIpv4Address(ipv4Address);

        byte[] mask = null;

        final int prefix;
        if (addressParts.hasNext()) {
            int potentionalPrefix = Integer.parseInt(addressParts.next());
            prefix = potentionalPrefix < 32 ? potentionalPrefix : 0;
        } else {
            prefix = 0;
        }

        if (prefix != 0) {
            // clone() is necessary to protect our constants
            mask = IPV4_MASKS[prefix].clone();
        }

        boolean hasMask = null != mask;

        if (hasMask) {
            ipv4SrcBuilder.setMask(mask);
        }

        matchEntryBuilder.setHasMask(hasMask);
        ipv4SrcCaseBuilder.setIpv4Src(ipv4SrcBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv4SrcCaseBuilder.build());
        entries.add(matchEntryBuilder.build());

        // Add ethernet type
        matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(EthType.class);
        EthTypeCaseBuilder ethTypeCaseBuilder = new EthTypeCaseBuilder();
        EthTypeBuilder ethTypeBuilder = new EthTypeBuilder();
        EtherType etherType = new EtherType(2048);
        ethTypeBuilder.setEthType(etherType);
        ethTypeCaseBuilder.setEthType(ethTypeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ethTypeCaseBuilder.build());
        entries.add(matchEntryBuilder.build());

        return entries;
    }
}
