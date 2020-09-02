/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.dst._case.EthDstBuilder;

/**
 * Translates OxmEthDst messages.
 *
 * @author michal.polkorab
 */
public class OxmEthDstDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmEthDstDeserializer() {
        super(EthDst.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        final EthDstBuilder ethBuilder = new EthDstBuilder()
                .setMacAddress(OxmDeserializerHelper.convertMacAddress(input));
        if (builder.isHasMask()) {
            ethBuilder.setMask(OxmDeserializerHelper.convertMask(input, EncodeConstants.MAC_ADDRESS_LENGTH));
        }
        builder.setMatchEntryValue(new EthDstCaseBuilder().setEthDst(ethBuilder.build()).build());
    }
}
