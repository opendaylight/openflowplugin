/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.NxExpMatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NshcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.exp.match.nx.exp.match.entry.value.NshcCaseValueBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public abstract class AbstractNshcCodec extends AbstractExperimenterMatchCodec {
    @Override
    protected void serializeValue(final NxExpMatchEntryValue value, final boolean hasMask, final ByteBuf outBuffer) {
        NshcCaseValue nshcCaseValue = (NshcCaseValue) value;
        outBuffer.writeInt(nshcCaseValue.getNshc().intValue());
        if (hasMask) {
            outBuffer.writeInt(nshcCaseValue.getMask().intValue());
        }
    }

    @Override
    protected NxExpMatchEntryValue deserializeValue(final ByteBuf message, final boolean hasMask) {
        Uint32 nshc = readUint32(message);
        Uint32 mask = hasMask ? readUint32(message) : null;
        return new NshcCaseValueBuilder().setNshc(nshc).setMask(mask).build();
    }

    @Override
    protected Uint32 getExperimenterId() {
        return NiciraConstants.NX_NSH_VENDOR_ID;
    }

    @Override
    public int getValueLength() {
        return Integer.BYTES;
    }
}
