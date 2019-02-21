/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMask;

public class Ipv4SourceEntrySerializer extends AbstractIpv4PolymorphicEntrySerializer {
    public Ipv4SourceEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV4_SRC);
    }

    @Override
    Ipv4Prefix extractNormalEntry(final Ipv4Match normalMatch) {
        return normalMatch.getIpv4Source();
    }

    @Override
    Ipv4Address extractArbitraryEntryAddress(final Ipv4MatchArbitraryBitMask arbitraryMatch) {
        return arbitraryMatch.getIpv4SourceAddressNoMask();
    }

    @Override
    DottedQuad extractArbitraryEntryMask(final Ipv4MatchArbitraryBitMask arbitraryMatch) {
        return arbitraryMatch.getIpv4SourceArbitraryBitmask();
    }
}
