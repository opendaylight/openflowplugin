/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;

public class Ipv6NdTargetEntrySerializer extends AbstractPrimitiveEntrySerializer<Ipv6Address> {
    public Ipv6NdTargetEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV6_ND_TARGET,
            EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES);
    }

    @Override
    protected Ipv6Address extractEntry(final Match match) {
        final Layer3Match l3Match = match.getLayer3Match();
        return l3Match instanceof Ipv6Match ? ((Ipv6Match) l3Match).getIpv6NdTarget() : null;
    }

    @Override
    protected void serializeEntry(final Ipv6Address entry, final ByteBuf outBuffer) {
        writeIpv6Address(entry, outBuffer);
    }
}
