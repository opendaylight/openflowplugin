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
    @Override
    protected VlanPcp extractEntry(Match match) {
        final VlanMatch vlanMatch = match.getVlanMatch();
        return vlanMatch == null ? null : vlanMatch.getVlanPcp();
    }

    @Override
    protected void serializeEntry(VlanPcp entry, Void mask, ByteBuf outBuffer) {
        outBuffer.writeByte(entry.getValue());
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.VLAN_PCP;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_BYTE_IN_BYTES;
    }
}
