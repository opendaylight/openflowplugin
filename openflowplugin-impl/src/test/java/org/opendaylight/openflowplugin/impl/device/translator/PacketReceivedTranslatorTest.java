package org.opendaylight.openflowplugin.impl.device.translator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.impl.device.DeviceContextImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;

/**
 * Created by tkubas on 4/1/15.
 */

@RunWith(MockitoJUnitRunner.class)
public class PacketReceivedTranslatorTest {

    @Mock
    DeviceContext deviceContext;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testTranslate() throws Exception {
        PacketReceivedTranslator packetReceivedTranslator = new PacketReceivedTranslator();
//        packetReceivedTranslator.translate()
    }

    private static PacketInMessage createPacketInMessage(final byte[] data,
                                                         final java.lang.Integer port) {
        PacketInReason reason = PacketInReason.OFPRACTION;
        return new PacketInMessageBuilder()
                .setVersion((short) EncodeConstants.OF10_VERSION_ID)
                .setInPort(port).setData(data).setReason(reason).build();

    }
}