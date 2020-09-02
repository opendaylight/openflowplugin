/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6Label;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;

public class Ipv6LabelEntrySerializer extends AbstractMatchEntrySerializer<Ipv6Label, Ipv6FlowLabel> {
    public Ipv6LabelEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV6_FLABEL, Integer.BYTES);
    }

    @Override
    protected Ipv6Label extractEntry(final Match match) {
        final Layer3Match l3Match = match.getLayer3Match();
        return l3Match instanceof Ipv6Match ? ((Ipv6Match) l3Match).getIpv6Label() : null;
    }

    @Override
    protected Ipv6FlowLabel extractEntryMask(final Ipv6Label entry) {
        return entry.getFlabelMask();
    }

    @Override
    protected void serializeEntry(final Ipv6Label entry, final Ipv6FlowLabel mask, final ByteBuf outBuffer) {
        outBuffer.writeInt(entry.getIpv6Flabel().getValue().intValue());
        if (mask != null) {
            writeMask(ByteUtil.unsignedIntToBytes(mask.getValue()), outBuffer, Integer.BYTES);
        }
    }
}
