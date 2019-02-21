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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public class EthernetDestinationEntrySerializer extends AbstractMatchEntrySerializer {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        super.serialize(match, outBuffer);
        writeMacAddress(match.getEthernetMatch().getEthernetDestination().getAddress(), outBuffer);

        if (getHasMask(match)) {
            writeMask(ByteBufUtils.macAddressToBytes(
                    match.getEthernetMatch().getEthernetDestination().getMask().getValue()),
                    outBuffer,
                    getValueLength());
        }
    }

    @Override
    public boolean matchTypeCheck(Match match) {
        return match.getEthernetMatch() != null && match.getEthernetMatch().getEthernetDestination() != null;
    }

    @Override
    protected boolean getHasMask(Match match) {
        return match.getEthernetMatch().getEthernetDestination().getMask() != null;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.ETH_DST;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.MAC_ADDRESS_LENGTH;
    }
}
