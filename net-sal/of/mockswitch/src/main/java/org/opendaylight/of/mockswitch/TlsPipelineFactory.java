/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

/**
 * A simple implementation of a TLS pipeline factory that creates pipelines
 * with OpenFlow message list encoder and decoder and a message handler.
 *
 * @author Sudheer Duggisetty
 */
class TlsPipelineFactory implements ChannelPipelineFactory {

    private SwMessageHandler msgHandler;
    private MockOpenflowSwitch sw;

    /**
     * Constructs client side TLS pipeline factory.
     *
     * @param msgHandler the message handler
     * @param sw the associated mock switch
     */
    TlsPipelineFactory(SwMessageHandler msgHandler, MockOpenflowSwitch sw) {
        this.msgHandler = msgHandler;
        this.sw = sw;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = Channels.pipeline();
        DeviceSslContextFactory factory = new DeviceSslContextFactory(sw);
        SSLEngine engine = factory.getClientContext().createSSLEngine();
        engine.setUseClientMode(true);
        p.addLast("ssl", new SslHandler(engine));
        p.addLast("decoder", new OfmDecoder());
        p.addLast("encoder", new OfmEncoder());
        p.addLast("messageHandler", msgHandler);
        return p;
    }
}
