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
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public class InPortEntrySerializer extends AbstractMatchEntrySerializer {

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        super.serialize(match, outBuffer);
        Long value = InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                OpenflowVersion.OF13,
                match.getInPort().getValue());
        if (value == null) {
            throw new IllegalArgumentException("Not a valid port number: " + match.getInPort().getValue());
        }
        outBuffer.writeInt(value.intValue());
    }

    @Override
    public boolean matchTypeCheck(Match match) {
        return Objects.nonNull(match.getInPort());
    }

    @Override
    protected boolean getHasMask(Match match) {
        return false;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IN_PORT;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_INT_IN_BYTES;
    }

}
