/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yangtools.yang.common.Uint32;

public class InPortEntrySerializer extends AbstractPrimitiveEntrySerializer<NodeConnectorId> {
    public InPortEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IN_PORT,Integer.BYTES);
    }

    @Override
    protected NodeConnectorId extractEntry(final Match match) {
        return match.getInPort();
    }

    @Override
    protected void serializeEntry(final NodeConnectorId entry, final ByteBuf outBuffer) {
        Uint32 value = InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.OF13, entry.getValue());
        if (value == null) {
            throw new IllegalArgumentException("Not a valid port number: " + entry.getValue());
        }
        outBuffer.writeInt(value.intValue());
    }
}
