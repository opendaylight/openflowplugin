/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6ExthdrCase;

/**
 * OxmIpv6Ext match entry serializer.
 *
 * @author michal.polkorab
 */
public class OxmIpv6ExtHdrSerializer extends AbstractOxmMatchEntrySerializer {
    @Override
    public void serialize(MatchEntry entry, ByteBuf outBuffer) {
        super.serialize(entry, outBuffer);
        Ipv6ExthdrCase entryValue = (Ipv6ExthdrCase) entry.getMatchEntryValue();
        Ipv6ExthdrFlags pseudoField = entryValue.getIpv6Exthdr().getPseudoField();
        int bitmap = ByteBufUtils.fillBitMask(0,
                pseudoField.isNonext(),
                pseudoField.isEsp(),
                pseudoField.isAuth(),
                pseudoField.isDest(),
                pseudoField.isFrag(),
                pseudoField.isRouter(),
                pseudoField.isHop(),
                pseudoField.isUnrep(),
                pseudoField.isUnseq());
        outBuffer.writeShort(bitmap);
        if (entry.isHasMask()) {
            outBuffer.writeBytes(entryValue.getIpv6Exthdr().getMask());
        }
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IPV6_EXTHDR;
    }

    @Override
    protected int getValueLength() {
        return Short.BYTES;
    }
}
