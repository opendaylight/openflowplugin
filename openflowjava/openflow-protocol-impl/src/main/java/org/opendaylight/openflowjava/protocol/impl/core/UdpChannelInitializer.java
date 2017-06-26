/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.channel.socket.DatagramChannel;

/**
 * @author michal.polkorab
 *
 */
public class UdpChannelInitializer extends ProtocolChannelInitializer<DatagramChannel> {

    @Override
    protected void initChannel(DatagramChannel ch) throws Exception {
        ch.pipeline().addLast(PipelineHandlers.OF_DATAGRAMPACKET_HANDLER.name(),
                new OFDatagramPacketHandler(getSwitchConnectionHandler()));
        OFDatagramPacketDecoder ofDatagramPacketDecoder = new OFDatagramPacketDecoder();
        ofDatagramPacketDecoder.setDeserializationFactory(getDeserializationFactory());
        ch.pipeline().addLast(PipelineHandlers.OF_DATAGRAMPACKET_DECODER.name(),
                ofDatagramPacketDecoder);
        OFDatagramPacketEncoder ofDatagramPacketEncoder = new OFDatagramPacketEncoder();
        ofDatagramPacketEncoder.setSerializationFactory(getSerializationFactory());
        ch.pipeline().addLast(PipelineHandlers.OF_ENCODER.name(), ofDatagramPacketEncoder);
    }
}