/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.vid._case.VlanVid;

/**
 * OxmVlanVid match entry serializer.
 *
 * @author michal.polkorab
 */
public class OxmVlanVidSerializer extends AbstractOxmMatchEntrySerializer {

    @Override
    public void serialize(MatchEntry entry, ByteBuf outBuffer) {
        super.serialize(entry, outBuffer);
        VlanVid vlanVid = ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid();
        int vlanVidValue = vlanVid.getVlanVid().toJava();
        if (vlanVid.isCfiBit()) {
            short cfi = 1 << 12; // 13-th bit
            vlanVidValue = vlanVidValue | cfi;
        }
        outBuffer.writeShort(vlanVidValue);
        if (entry.isHasMask()) {
            writeMask(vlanVid.getMask(), outBuffer, getValueLength());
        }
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.VLAN_VID;
    }

    @Override
    protected int getValueLength() {
        return Short.BYTES;
    }

}
