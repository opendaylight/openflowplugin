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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;
import org.opendaylight.yangtools.yang.common.Empty;

public class VlanVidEntrySerializer extends AbstractMatchEntrySerializer<VlanId, Empty> {
    private static final byte[] VLAN_VID_MASK = new byte[]{16, 0};

    public VlanVidEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.VLAN_VID,
            Short.BYTES);
    }

    @Override
    protected VlanId extractEntry(Match match) {
        final VlanMatch vlanMatch = match.getVlanMatch();
        return vlanMatch == null ? null : vlanMatch.getVlanId();
    }

    @Override
    protected Empty extractEntryMask(VlanId entry) {
        return Boolean.TRUE.equals(entry.isVlanIdPresent())
                && (entry.getVlanId() == null || entry.getVlanId().getValue().shortValue() == 0) ? Empty.getInstance()
                        : null;
    }

    @Override
    protected void serializeEntry(VlanId entry, Empty mask, ByteBuf outBuffer) {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId vlanId = entry.getVlanId();

        int vlanVidValue = vlanId != null ? vlanId.getValue().toJava() : 0;

        if (Boolean.TRUE.equals(entry.isVlanIdPresent())) {
            short cfi = 1 << 12;
            vlanVidValue = vlanVidValue | cfi;
        }
        outBuffer.writeShort(vlanVidValue);

        if (mask != null) {
            writeMask(VLAN_VID_MASK, outBuffer, Short.BYTES);
        }
    }
}
