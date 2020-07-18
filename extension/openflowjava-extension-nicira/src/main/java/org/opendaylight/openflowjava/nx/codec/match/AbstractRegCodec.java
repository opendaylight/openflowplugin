/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.reg.grouping.RegValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.RegCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.RegCaseValueBuilder;

public abstract class AbstractRegCodec extends AbstractMatchCodec {
    private static final int VALUE_LENGTH = 4;

    @Override
    public MatchEntry deserialize(ByteBuf message) {
        final MatchEntryBuilder matchEntriesBuilder = deserializeHeaderToBuilder(message);
        final RegValuesBuilder regValuesBuilder = new RegValuesBuilder();
        regValuesBuilder.setValue(readUint32(message));

        if (matchEntriesBuilder.isHasMask()) {
            regValuesBuilder.setMask(readUint32(message));
        }

        return matchEntriesBuilder
            .setMatchEntryValue(new RegCaseValueBuilder()
                .setRegValues(regValuesBuilder.build())
                .build())
            .build();
    }

    @Override
    public void serialize(MatchEntry input, ByteBuf outBuffer) {
        serializeHeader(input, outBuffer);
        final RegCaseValue regCase = (RegCaseValue) input.getMatchEntryValue();
        outBuffer.writeInt(regCase.getRegValues().getValue().intValue());

        if (input.isHasMask()) {
            outBuffer.writeInt(regCase.getRegValues().getMask().intValue());
        }
    }

    @Override
    public int getOxmClassCode() {
        return OxmMatchConstants.NXM_1_CLASS;
    }

    @Override
    public int getValueLength() {
        return VALUE_LENGTH;
    }

    @Override
    public Class<? extends OxmClassBase> getOxmClass() {
        return Nxm1Class.class;
    }
}
