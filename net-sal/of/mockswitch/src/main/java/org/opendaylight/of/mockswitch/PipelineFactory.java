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

/**
 * A simple implementation of a pipeline factory that creates pipelines
 * with OpenFlow message list encoder and decoder and a message handler.
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Sudheer Duggisetty
 */
public class PipelineFactory implements ChannelPipelineFactory {

    private SwMessageHandler msgHandler;

    /** Sets the message handler.
     *
     * @param msgHandler the message handler to be inserted into pipelines
     */
    public void setMsgHandler(SwMessageHandler msgHandler) {
        this.msgHandler = msgHandler;
    }

    /**
     * Creates and returns a message decoder to be used in the pipeline.
     * This default implementation returns a standard decoder.
     *
     * @return the decoder
     */
    protected OfmDecoder createOfmDecoder() {
        return new OfmDecoder();
    }

    /**
     * Creates and returns a message encoder to be used in the pipeline.
     * This default implementation returns a standard encoder.
     *
     * @return the encoder
     */
    protected OfmEncoder createOfmEncoder() {
        return new OfmEncoder();
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        if (msgHandler == null)
            throw new IllegalStateException("msgHandler not set!");

        ChannelPipeline p = Channels.pipeline();
        p.addLast("decoder", createOfmDecoder());
        p.addLast("encoder", createOfmEncoder());
        p.addLast("messageHandler", msgHandler);
        return p;
    }
}
