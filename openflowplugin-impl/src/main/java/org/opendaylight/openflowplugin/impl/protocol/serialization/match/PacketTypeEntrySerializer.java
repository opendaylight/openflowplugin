/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.PacketTypeMatch;
import org.opendaylight.yangtools.yang.common.Uint32;

public class PacketTypeEntrySerializer extends AbstractUint32EntrySerializer {
    public PacketTypeEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.PACKET_TYPE);
    }

    @Override
    protected Uint32 extractEntry(final Match match) {
        final PacketTypeMatch typeMatch = match.getPacketTypeMatch();
        return typeMatch == null ? null : typeMatch.getPacketType();
    }
}
