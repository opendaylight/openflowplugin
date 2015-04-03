package org.opendaylight.openflowplugin.impl.translator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import java.math.BigInteger;

/**
 * Created by tkubas on 4/1/15.
 */

@RunWith(MockitoJUnitRunner.class)
public class PacketReceivedTranslatorTest {

    @Mock
    ConnectionContext connectionContext;
    @Mock
    FeaturesReply featuresReply;
    @Mock
    DeviceState deviceState;
    @Mock
    DataBroker dataBroker;
    @Mock
    DeviceContext deviceContext;
    String data = "Test_Data";

    @Before
    public void setUp() throws Exception {
        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(connectionContext.getFeatures()).thenReturn(featuresReply);
        Mockito.when(featuresReply.getDatapathId()).thenReturn(BigInteger.TEN);
    }

    @Test
    public void testTranslate() throws Exception {
        PacketReceivedTranslator packetReceivedTranslator = new PacketReceivedTranslator();
        PacketInMessage packetInMessage = createPacketInMessage(data.getBytes(), 5);
        PacketReceived packetReceived = packetReceivedTranslator.translate(packetInMessage, deviceContext, null);
        Assert.assertArrayEquals(packetInMessage.getData(), packetReceived.getPayload());
        Assert.assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.SendToController",
                             packetReceived.getPacketInReason().getName());
    }

    private static PacketInMessage createPacketInMessage(final byte[] data,
                                                         final java.lang.Integer port) {
        PacketInReason reason = PacketInReason.OFPRACTION;
        return new PacketInMessageBuilder()
                .setVersion((short) OFConstants.OFP_VERSION_1_0)
                .setData(data).setReason(reason).build();

    }
}