/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;

/**
 * Translates TableMod messages.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class TableModInputMessageFactory implements OFSerializer<TableModInput> {
    private static final byte MESSAGE_TYPE = 17;
    private static final byte PADDING_IN_TABLE_MOD_MESSAGE = 3;

    @Override
    public void serialize(final TableModInput message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeByte(message.getTableId().getValue().byteValue());
        outBuffer.writeZero(PADDING_IN_TABLE_MOD_MESSAGE);
        outBuffer.writeInt(createConfigBitmask(message.getConfig()));
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private static int createConfigBitmask(final TableConfig tableConfig) {
        return ByteBufUtils.fillBitMask(3, tableConfig.getOFPTCDEPRECATEDMASK());
    }
}
