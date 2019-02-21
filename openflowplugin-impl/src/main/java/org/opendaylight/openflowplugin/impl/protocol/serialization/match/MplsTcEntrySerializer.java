/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;

public class MplsTcEntrySerializer extends AbstractUint8EntrySerializer {
    public MplsTcEntrySerializer() {
        super(OxmMatchConstants.MPLS_TC, OxmMatchConstants.OPENFLOW_BASIC_CLASS);
    }

    @Override
    protected Short extractEntry(Match match) {
        final ProtocolMatchFields protoFields = match.getProtocolMatchFields();
        return protoFields == null ? null : protoFields.getMplsTc();
    }
}
