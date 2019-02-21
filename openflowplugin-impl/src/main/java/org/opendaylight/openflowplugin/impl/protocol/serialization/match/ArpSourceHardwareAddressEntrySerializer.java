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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;

public class ArpSourceHardwareAddressEntrySerializer
        extends AbstractMacAddressFilterEntrySerializer<ArpSourceHardwareAddress> {
    public ArpSourceHardwareAddressEntrySerializer() {
        super(OxmMatchConstants.ARP_SHA, OxmMatchConstants.OPENFLOW_BASIC_CLASS);
    }

    @Override
    protected ArpSourceHardwareAddress extractEntry(Match match) {
        final Layer3Match l3Match = match.getLayer3Match();
        return l3Match instanceof ArpMatch ? ((ArpMatch) l3Match).getArpSourceHardwareAddress() : null;
    }
}
