/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.connection.ConnectionAdapterImpl;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContextOFImpl;
import org.opendaylight.openflowplugin.openflow.md.core.session.SwitchConnectionCookieOFImpl;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueProcessorLightImpl;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInputBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Jakub Toth jatoth@cisco.com on 3/10/14.
 */

public class PacketInV10TranslatorTest{
    private static final Logger log = LoggerFactory
            .getLogger(PacketInV10TranslatorTest.class);

    /**
     * test
     * {@link PacketInV10Translator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * - all parameteres are null - translates packetIn from OF-API model to
     * MD-SAL model, supports OF-1.0
     */
    @Test
    public void testTranslateWithAllNullParam(){
        SwitchConnectionDistinguisher cookie = null;
        SessionContext sessionContext = null;
        OfHeader msg = null;

        PacketInV10Translator packetInV10Translator = new PacketInV10Translator();

        List<DataObject> salPacketIn = packetInV10Translator.translate(cookie,
                sessionContext, msg);

        Assert.assertEquals(true, salPacketIn.isEmpty());

        log.info("Test with all null parameters done.");
    }

    /**
     * test
     * {@link PacketInV10Translator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * - DPID is null- translates packetIn from OF-API model to MD-SAL model,
     * supports OF-1.0
     */
    @Test
    public void testTranslateDPIDNull(){
        SessionContextOFImpl sessionContextOFImpl = new SessionContextOFImpl();

        SwitchConnectionDistinguisher cookie = this.settingCookie();

        byte[] data = this.messageData();
        PacketInMessage message = this.createPacketInMessage(data, null);

        PacketInV10Translator packetInV10Translator = new PacketInV10Translator();

        List<DataObject> salPacketIn = packetInV10Translator.translate(cookie,
                sessionContextOFImpl, message);

        Assert.assertEquals(true, salPacketIn.isEmpty());
        log.info("Test with null DPID parameter done.");
    }

    /**
     * test
     * {@link PacketInV10Translator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * - inPort is null- translates packetIn from OF-API model to MD-SAL model,
     * supports OF-1.0
     */
    @Test
    public void testTranslateInPortNull(){
        BigInteger datapathId = this.dataPathId();

        GetFeaturesOutputBuilder featuresBuilder = new GetFeaturesOutputBuilder();
        featuresBuilder.setDatapathId(datapathId);

        SessionContextOFImpl sessionContextOFImpl = new SessionContextOFImpl();
        sessionContextOFImpl.setFeatures(featuresBuilder.build());

        SwitchConnectionDistinguisher cookie = this.settingCookie();

        byte[] data = this.messageData();

        PacketInMessage message = this.createPacketInMessage(data, null);

        PacketInV10Translator packetInV10Translator = new PacketInV10Translator();

        List<DataObject> salPacketIn = packetInV10Translator.translate(cookie,
                sessionContextOFImpl, message);

        Assert.assertEquals(true, salPacketIn.isEmpty());

        log.info("Test with null inPort parameter done.");
    }

    /**
     * test
     * {@link PacketInV10Translator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * - translates packetIn from OF-API model to MD-SAL model, supports OF-1.0
     * 
     * @throws IOException
     */
    @Test
    public void testTranslate(){
        BigInteger datapathId = this.dataPathId();

        byte[] data = this.messageData();

        PacketInMessage message = this.createPacketInMessage(data, 5);

        GetFeaturesOutput featuresOutput = this
                .createGetFeatureOutput(datapathId);

        NioSocketChannel nioSocketChannel = new NioSocketChannel();
        this.registerChannel(nioSocketChannel);

        SetConfigInput setConfigInput = this.createSetConfigInput();

        ConnectionAdapterImpl connectionAdapter = new ConnectionAdapterImpl(
                nioSocketChannel, new InetSocketAddress(51));
        connectionAdapter.setConfig(setConfigInput);

        ConnectionConductorImpl connectionConductor = new ConnectionConductorImpl(
                connectionAdapter);

        this.initConnectionConductor(connectionConductor, featuresOutput);

        SessionContextOFImpl sessionContextOFImpl = new SessionContextOFImpl();
        sessionContextOFImpl.setFeatures(featuresOutput);
        sessionContextOFImpl.setPrimaryConductor(connectionConductor);

        SwitchConnectionDistinguisher cookie = this.settingCookie();

        PacketInV10Translator packetInV10Translator = new PacketInV10Translator();

        OpenflowPortsUtil.init();

        List<DataObject> salPacketIn = packetInV10Translator.translate(cookie,
                sessionContextOFImpl, message);

        String expectedString = "[PacketReceived [_ingress=NodeConnectorRef [_value=KeyedInstanceIdentifier"
                + "{targetType=interface org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector,"
                + " path=[org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes,"
                + " org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node["
                + "key=NodeKey [_id=Uri [_value=openflow:"
                + datapathId.toString()
                + "]]],"
                + " org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector["
                + "key=NodeConnectorKey [_id=Uri [_value=openflow:"
                + datapathId.toString()
                + ":"
                + message.getInPort().toString()
                + "]]]]}], _packetInReason=class org.opendaylight.yang.gen.v1.urn.opendaylight."
                + "packet.service.rev130709.SendToController, _payload=[115, 101, 110, 100, 79, 117,"
                + " 116, 112, 117, 116, 77, 115, 103, 95, 84, 69, 83, 84], augmentation=[]]]";
        Assert.assertEquals(expectedString, salPacketIn.toString());

        log.info("Test translate done.");
    }

    /**
     * create datapathID
     * 
     * @return BigInteger
     */
    private BigInteger dataPathId(){
        byte[] datapathIdByte = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        for(int i = 0; i < datapathIdByte.length; i++){
            datapathIdByte[i] = 1;
        }
        return new BigInteger(1, datapathIdByte);
    }

    /**
     * generate message from string to byte[]
     * 
     * @return byte[]
     */
    private byte[] messageData(){
        String string = new String("sendOutputMsg_TEST");
        return string.getBytes();
    }

    /**
     * create PacketInMessage with setting Version, InPort, Data, Reason
     * 
     * @param data
     * @param port
     * @return PacketInMessage
     */
    private PacketInMessage createPacketInMessage(final byte[] data,
            final java.lang.Integer port){
        PacketInReason reason = PacketInReason.OFPRACTION;
        return new PacketInMessageBuilder()
                .setVersion((short) EncodeConstants.OF10_VERSION_ID)
                .setInPort(port).setData(data).setReason(reason).build();

    }

    /**
     * create cookie
     * 
     * @return SwitchConnectionDistinguisher
     */
    private SwitchConnectionDistinguisher settingCookie(){
        SwitchConnectionCookieOFImpl key = new SwitchConnectionCookieOFImpl();
        key.setAuxiliaryId((short) 1);
        key.init(42);
        return key;
    }

    /**
     * create GetFeatureOutput
     * 
     * @param datapathId
     * @return GetFeaturesOutput
     */
    private GetFeaturesOutput createGetFeatureOutput(final BigInteger datapathId){
        return new GetFeaturesOutputBuilder().setDatapathId(datapathId)
                .setVersion((short) 0x01).build();
    }

    /**
     * register eventLop
     * 
     * @param channel
     */
    private void registerChannel(final NioSocketChannel channel){
        EventLoopGroup eventLop = new NioEventLoopGroup();
        ChannelFuture regFuture = eventLop.register(channel);
    }

    /**
     * build SetConfigInput with setting Version and Xid
     * 
     * @return SetConfigInput
     */
    private SetConfigInput createSetConfigInput(){
        return new SetConfigInputBuilder().setVersion((short) 0x01)
                .setXid(0xfffffL).build();
    }

    /**
     * init connectionConductor
     * 
     * @param connectionConductor
     * @param featuresOutput
     */
    private void initConnectionConductor(
            final ConnectionConductorImpl connectionConductor,
            final GetFeaturesOutput featuresOutput){
        TranslatorKey paramK = new TranslatorKey(1, "PacketInMessage.class");
        Collection<IMDMessageTranslator<OfHeader, List<DataObject>>> coll = new ArrayList<>();
        coll.add(new PacketInV10Translator());
        Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> translatorMapping = new HashMap<>();
        translatorMapping.put(paramK, coll);

        QueueProcessorLightImpl queueProcessor = new QueueProcessorLightImpl();
        queueProcessor.setTranslatorMapping(translatorMapping);
        queueProcessor.init();
        connectionConductor.setQueueProcessor(queueProcessor);
        connectionConductor.init();
        connectionConductor
                .onHandshakeSuccessfull(featuresOutput, (short) 0x01);
    }
}
