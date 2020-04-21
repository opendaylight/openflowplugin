/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yangtools.yang.common.Uint16;

public class UdpDestinationPortEntrySerializer extends AbstractPortNumberWithMaskEntrySerilizer {
    public UdpDestinationPortEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.UDP_DST);
    }

    @Override
    protected Uint16 extractPort(final Layer4Match l4match) {
        PortNumber portNumber = l4match instanceof UdpMatch ? ((UdpMatch) l4match).getUdpDestinationPort() : null;
        return portNumber == null ? null : portNumber.getValue();
    }

    @Override
    protected Layer4Match extractEntry(Match match) {
        final Layer4Match l4match = match.getLayer4Match();
        if (l4match instanceof UdpMatch && ((UdpMatch) l4match).getUdpDestinationPort() != null) {
            return l4match;
        }
        return null;
    }

    @Override
    protected Uint16 extractMask(Layer4Match entry) {
        PortNumber portNumber = entry instanceof UdpMatch ? ((UdpMatch) entry).getUdpDestinationPortMask() : null;
        return portNumber == null ? null : portNumber.getValue();
    }
}
