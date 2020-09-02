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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;

public class IpDscpEntrySerializer extends AbstractPrimitiveEntrySerializer<Dscp> {
    public IpDscpEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IP_DSCP, Byte.BYTES);
    }

    @Override
    protected Dscp extractEntry(final Match match) {
        final IpMatch ipMatch = match.getIpMatch();
        return ipMatch == null ? null : ipMatch.getIpDscp();
    }

    @Override
    protected void serializeEntry(final Dscp entry, final ByteBuf outBuffer) {
        outBuffer.writeByte(entry.getValue().byteValue());
    }
}
