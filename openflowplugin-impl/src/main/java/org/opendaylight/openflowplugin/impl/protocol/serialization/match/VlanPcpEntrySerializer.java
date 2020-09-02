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
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;

public class VlanPcpEntrySerializer extends AbstractPrimitiveEntrySerializer<VlanPcp> {
    public VlanPcpEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.VLAN_PCP,
            Byte.BYTES);
    }

    @Override
    protected VlanPcp extractEntry(final Match match) {
        final VlanMatch vlanMatch = match.getVlanMatch();
        return vlanMatch == null ? null : vlanMatch.getVlanPcp();
    }

    @Override
    protected void serializeEntry(final VlanPcp entry, final ByteBuf outBuffer) {
        outBuffer.writeByte(entry.getValue().byteValue());
    }
}
