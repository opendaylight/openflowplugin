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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;

public class ArpTargetHardwareAddressEntrySerializer extends AbstractMatchEntrySerializer {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        super.serialize(match, outBuffer);
        final ArpTargetHardwareAddress arpTargetHardwareAddress =
                ((ArpMatch) match.getLayer3Match()).getArpTargetHardwareAddress();
        writeMacAddress(arpTargetHardwareAddress.getAddress(), outBuffer);

        if (getHasMask(match)) {
            writeMask(ByteBufUtils.macAddressToBytes(
                    arpTargetHardwareAddress.getMask().getValue()),
                    outBuffer,
                    getValueLength());
        }
    }

    @Override
    public boolean matchTypeCheck(Match match) {
        return match.getLayer3Match() != null
                && match.getLayer3Match() instanceof ArpMatch
                && ((ArpMatch) match.getLayer3Match()).getArpTargetHardwareAddress() != null;
    }

    @Override
    protected boolean getHasMask(Match match) {
        return ((ArpMatch) match.getLayer3Match()).getArpTargetHardwareAddress().getMask() != null;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.ARP_THA;
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
