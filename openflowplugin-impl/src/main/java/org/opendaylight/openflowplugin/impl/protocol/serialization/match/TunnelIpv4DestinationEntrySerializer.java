/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4Match;

// TODO: Why is this serialization exactly same as in Ipv4DestinationEntrySerializer?
public class TunnelIpv4DestinationEntrySerializer extends AbstractIpv4PrefixEntrySerializer {
    public TunnelIpv4DestinationEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV4_DST);
    }

    @Override
    protected Ipv4Prefix extractEntry(final Match match) {
        final Layer3Match l3Match = match.getLayer3Match();
        return l3Match instanceof TunnelIpv4Match ? ((TunnelIpv4Match) l3Match).getTunnelIpv4Destination() : null;
    }
}
