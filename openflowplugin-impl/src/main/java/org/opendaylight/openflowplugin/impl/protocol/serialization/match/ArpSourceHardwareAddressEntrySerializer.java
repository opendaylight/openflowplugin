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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;

public class ArpSourceHardwareAddressEntrySerializer extends AbstractMatchEntrySerializer {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        super.serialize(match, outBuffer);
        final ArpSourceHardwareAddress arpSourceHardwareAddress =
                ArpMatch.class.cast(match.getLayer3Match()).getArpSourceHardwareAddress();
        writeMacAddress(arpSourceHardwareAddress.getAddress(), outBuffer);

        if (getHasMask(match)) {
            writeMask(ByteBufUtils.macAddressToBytes(
                    arpSourceHardwareAddress.getMask().getValue()),
                    outBuffer,
                    getValueLength());
        }
    }

    @Override
    public boolean matchTypeCheck(Match match) {
        return Objects.nonNull(match.getLayer3Match()) &&
                ArpMatch.class.isInstance(match.getLayer3Match()) &&
                Objects.nonNull(ArpMatch.class.cast(match.getLayer3Match()).getArpSourceHardwareAddress());
    }

    @Override
    protected boolean getHasMask(Match match) {
        return Objects.nonNull(ArpMatch.class.cast(match.getLayer3Match()).getArpSourceHardwareAddress().getMask());
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.ARP_SHA;
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
