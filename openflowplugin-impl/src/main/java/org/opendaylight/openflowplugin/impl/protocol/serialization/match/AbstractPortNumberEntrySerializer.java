/*
 * Copyright (c) 2019 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yangtools.yang.common.Uint16;

public abstract class AbstractPortNumberEntrySerializer extends AbstractUint16EntrySerializer {
    protected AbstractPortNumberEntrySerializer(final int oxmClassCode, final int oxmFieldCode) {
        super(oxmClassCode, oxmFieldCode);
    }

    @Override
    protected final Uint16 extractEntry(final Match match) {
        final PortNumber port = extractPort(match);
        return port == null ? null : port.getValue();
    }

    protected abstract @Nullable PortNumber extractPort(Match match);
}
