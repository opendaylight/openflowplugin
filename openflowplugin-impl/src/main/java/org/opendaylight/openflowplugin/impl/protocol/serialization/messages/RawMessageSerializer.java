package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.common.types.rev170913.RawMessage;

public class RawMessageSerializer extends AbstractMessageSerializer<RawMessage> {
    @Override
    public void serializeHeader(final RawMessage message, final ByteBuf outBuffer) {
        outBuffer.writeBytes(message.getPayload());
    }

    @Override
    protected byte getMessageType() {
        return -1;
    }
}