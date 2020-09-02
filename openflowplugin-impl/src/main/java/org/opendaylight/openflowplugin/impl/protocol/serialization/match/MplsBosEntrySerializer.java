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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;
import org.opendaylight.yangtools.yang.common.Uint8;

public class MplsBosEntrySerializer extends AbstractPrimitiveEntrySerializer<Uint8> {
    public MplsBosEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.MPLS_BOS, Byte.BYTES);
    }

    @Override
    protected Uint8 extractEntry(final Match match) {
        final ProtocolMatchFields protoFields = match.getProtocolMatchFields();
        return protoFields == null ? null : protoFields.getMplsBos();
    }

    @Override
    protected void serializeEntry(final Uint8 entry, final ByteBuf outBuffer) {
        outBuffer.writeBoolean(entry.byteValue() != 0);
    }
}
