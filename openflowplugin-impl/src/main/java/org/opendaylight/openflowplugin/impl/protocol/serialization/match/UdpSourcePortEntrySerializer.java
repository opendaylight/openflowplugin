/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.PortNumberRange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;

public class UdpSourcePortEntrySerializer extends PortNumerRangeEntrySerializer {
    public UdpSourcePortEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.UDP_SRC);
    }

    @Override
    protected @Nullable PortNumberRange extractPortNumberRange(Match match) {
        final Layer4Match l4match = match.getLayer4Match();
        return l4match instanceof UdpMatch ? ((UdpMatch) l4match).getUdpSourcePort() : null;
    }
}
