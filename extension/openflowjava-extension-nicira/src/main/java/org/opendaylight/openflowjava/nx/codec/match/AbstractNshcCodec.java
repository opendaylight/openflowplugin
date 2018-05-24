/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.experimenter.id._case.nx.exp.match.entry.value.NshcCaseValueBuilder;

public abstract class AbstractNshcCodec extends AbstractExperimenterMatchCodec {

    private static final int VALUE_LENGTH = EncodeConstants.SIZE_OF_INT_IN_BYTES;

    @Override
    protected void serializeValue(NxExpMatchEntryValue value, boolean hasMask, ByteBuf outBuffer) {
        NshcCaseValue nshcCaseValue = (NshcCaseValue) value;
        outBuffer.writeInt(nshcCaseValue.getNshc().intValue());
        if (hasMask) {
            outBuffer.writeInt(nshcCaseValue.getMask().intValue());
        }
    }

    @Override
    protected NxExpMatchEntryValue deserializeValue(ByteBuf message, boolean hasMask) {
        Long nshc = message.readUnsignedInt();
        Long mask = hasMask ? message.readUnsignedInt() : null;
        return new NshcCaseValueBuilder().setNshc(nshc).setMask(mask).build();
    }

    @Override
    protected long getExperimenterId() {
        return NiciraConstants.NX_NSH_VENDOR_ID;
    }

    @Override
    public int getValueLength() {
        return VALUE_LENGTH;
    }

}
