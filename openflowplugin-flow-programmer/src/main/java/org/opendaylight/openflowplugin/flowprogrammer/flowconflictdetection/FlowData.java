/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection;

import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.ArpData;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.EthernetData;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.IpData;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.Ipv4Data;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.Ipv6Data;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.L4Data;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.NxmData;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.VlanData;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.MplsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;

/**
 * This class save flow information from FlowBuilder or Flow.
 *
 * <p>
 *
 * @author Yi Yang (yi.y.yang@intel.com)
 *
 * <p>
 * @since 2015-08-25
 */

public class FlowData {
    NodeConnectorId inPort;
    public ArpData arpData;
    public EthernetData ethernetData;
    public IpData ipData;
    public Ipv4Data ipv4Data;
    public Ipv6Data ipv6Data;
    public L4Data l4Data;
    public NxmData nxmData;
    public VlanData vlanData;
    public MplsData mplsData;

    public FlowData() {
        this.inPort = null;
        this.arpData = null;
        this.ethernetData = null;
        this.ipData = null;
        this.ipv4Data = null;
        this.ipv6Data = null;
        this.l4Data = null;
        this.nxmData = null;
        this.vlanData = null;
        this.mplsData = null;
    }

    public FlowData(Match match) {
        VlanMatch vlanMatch = match.getVlanMatch();
        if (vlanMatch != null) {
           this.vlanData = VlanData.toVlanData(vlanMatch);
        }
        ProtocolMatchFields mplsMatch = match.getProtocolMatchFields();
        if (mplsMatch != null) {
           this.mplsData = MplsData.toMplsData(mplsMatch);
        }
        Layer3Match l3Match = match.getLayer3Match();
        if (l3Match != null) {
            if (l3Match instanceof ArpMatch) {
                this.arpData = ArpData.toArpData((ArpMatch)l3Match);
            }
            else if (l3Match instanceof Ipv4Match) {
                this.ipv4Data = Ipv4Data.toIpv4Data((Ipv4Match)l3Match);
            }
            else if (l3Match instanceof Ipv6Match) {
                this.ipv6Data = Ipv6Data.toIpv6Data((Ipv6Match)l3Match);
            }
        }
        EthernetMatch etherMatch = match.getEthernetMatch();
        this.ethernetData = EthernetData.toEthernetData(etherMatch);
        IpMatch ipMatch = match.getIpMatch();
        this.ipData = IpData.toIpData(ipMatch);
        Layer4Match l4Match = match.getLayer4Match();
        if (l4Match != null) {
            this.l4Data = L4Data.toL4Data(l4Match);
        }
        GeneralAugMatchNodesNodeTableFlow m = match.getAugmentation(GeneralAugMatchNodesNodeTableFlow.class);
        this.nxmData = NxmData.toNxmData(m);
        this.inPort = match.getInPort();
    }
}
