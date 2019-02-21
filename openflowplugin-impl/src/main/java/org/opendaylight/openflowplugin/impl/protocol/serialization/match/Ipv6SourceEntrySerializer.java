/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;

public class Ipv6SourceEntrySerializer extends AbstractIpv6PolymorphicEntrySerializer {
    public Ipv6SourceEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV6_SRC);
    }

    @Override
    Ipv6Prefix extractNormalEntry(final Ipv6Match normalMatch) {
        return normalMatch.getIpv6Source();
    }

    @Override
    Ipv6Address extractArbitraryEntryAddress(final Ipv6MatchArbitraryBitMask arbitraryMatch) {
        return arbitraryMatch.getIpv6SourceAddressNoMask();
    }

    @Override
    Ipv6ArbitraryMask extractArbitraryEntryMask(final Ipv6MatchArbitraryBitMask arbitraryMatch) {
        return arbitraryMatch.getIpv6SourceArbitraryBitmask();
    }
}
