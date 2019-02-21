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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6Match;
import org.opendaylight.yangtools.yang.common.Uint8;

public class Icmpv6CodeEntrySerializer extends AbstractUint8EntrySerializer {
    public Icmpv6CodeEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.ICMPV6_CODE);
    }

    @Override
    protected Uint8 extractEntry(final Match match) {
        final Icmpv6Match icmpMatch = match.getIcmpv6Match();
        return icmpMatch == null ? null : icmpMatch.getIcmpv6Code();
    }
}
