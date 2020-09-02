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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;

public class EthernetTypeEntrySerializer extends AbstractPrimitiveEntrySerializer<EthernetType> {
    public EthernetTypeEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ETH_TYPE,
            Short.BYTES);
    }

    @Override
    protected EthernetType extractEntry(final Match match) {
        final EthernetMatch ethMatch = match.getEthernetMatch();
        return ethMatch == null ? null : ethMatch.getEthernetType();
    }

    @Override
    protected void serializeEntry(final EthernetType entry, final ByteBuf outBuffer) {
        outBuffer.writeShort(entry.getType().getValue().shortValue());
    }
}
