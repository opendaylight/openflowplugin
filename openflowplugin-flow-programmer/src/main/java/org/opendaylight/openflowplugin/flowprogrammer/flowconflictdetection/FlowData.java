/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection;

import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.EthernetData;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.IpData;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.NxmData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;

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
    public EthernetData ethernetData;
    public IpData ipData;
    public NxmData nxmData;

    public FlowData() {
        this.inPort = null;
        this.ethernetData = null;
        this.ipData = null;
        this.nxmData = null;
    }

    public FlowData(Match match) {
        this.ethernetData = EthernetData.toEthernetData(match);
        this.ipData = IpData.toIpData(match);
        this.nxmData = NxmData.toNxmData(match);
        this.inPort = match.getInPort();
    }
}
