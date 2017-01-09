/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;

public class VlanVidEntrySerializer extends AbstractMatchEntrySerializer {
    private static final byte[] VLAN_VID_MASK = new byte[]{16, 0};

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        super.serialize(match, outBuffer);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId vlanId =
                match.getVlanMatch().getVlanId().getVlanId();

        int vlanVidValue = Objects.nonNull(vlanId) ? vlanId.getValue() : 0;

        if (Boolean.TRUE.equals(match.getVlanMatch().getVlanId().isVlanIdPresent())) {
            short cfi = 1 << 12;
            vlanVidValue = vlanVidValue | cfi;
        }

        outBuffer.writeShort(vlanVidValue);

        if (getHasMask(match)) {
            writeMask(VLAN_VID_MASK, outBuffer, getValueLength());
        }
    }

    @Override
    public boolean matchTypeCheck(Match match) {
        return Objects.nonNull(match.getVlanMatch()) &&
                Objects.nonNull(match.getVlanMatch().getVlanId());
    }

    @Override
    protected boolean getHasMask(Match match) {
        final VlanId vlanId = match.getVlanMatch().getVlanId();
        return Boolean.TRUE.equals(vlanId.isVlanIdPresent()) &&
                (Objects.isNull(vlanId.getVlanId()) || vlanId.getVlanId().getValue() == 0);
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.VLAN_VID;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_SHORT_IN_BYTES;
    }

}
