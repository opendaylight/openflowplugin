/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;

/**
 * Translates FeaturesReply messages (both OpenFlow v1.0 and OpenFlow v1.3).
 *
 * @author giuseppex.petralia@intel.com
 */
public class GetFeaturesOutputFactory implements OFSerializer<GetFeaturesOutput>, SerializerRegistryInjector {

    private static final byte MESSAGE_TYPE = 6;
    private static final byte PADDING = 2;

    @Override
    public void serialize(final GetFeaturesOutput message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeLong(message.getDatapathId().longValue());
        outBuffer.writeInt(message.getBuffers().intValue());
        outBuffer.writeByte(message.getTables().intValue());
        outBuffer.writeByte(message.getAuxiliaryId().intValue());
        outBuffer.writeZero(PADDING);
        writeCapabilities(message.getCapabilities(), outBuffer);
        outBuffer.writeInt(message.getReserved().intValue());
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
    }

    private static void writeCapabilities(final Capabilities capabilities, final ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, capabilities.getOFPCFLOWSTATS());
        map.put(1, capabilities.getOFPCTABLESTATS());
        map.put(2, capabilities.getOFPCPORTSTATS());
        map.put(3, capabilities.getOFPCGROUPSTATS());
        map.put(5, capabilities.getOFPCIPREASM());
        map.put(6, capabilities.getOFPCQUEUESTATS());
        map.put(8, capabilities.getOFPCPORTBLOCKED());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }
}
