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
    @Override
    protected EthernetType extractEntry(Match match) {
        final EthernetMatch ethMatch = match.getEthernetMatch();
        return ethMatch == null ? null : ethMatch.getEthernetType();
    }

    @Override
    protected void serializeEntry(EthernetType entry, Void mask, ByteBuf outBuffer) {
        outBuffer.writeShort(entry.getType().getValue().shortValue());
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.ETH_TYPE;
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
