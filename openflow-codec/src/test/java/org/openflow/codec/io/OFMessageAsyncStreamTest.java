package org.openflow.codec.io;

import org.openflow.codec.io.OFMessageAsyncStream;
import org.openflow.codec.protocol.*;
import org.openflow.codec.protocol.factory.OFPBasicFactoryImpl;

import java.util.*;
import java.nio.channels.*;
import java.net.InetSocketAddress;

import org.junit.Assert;

import org.junit.Test;

/**
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 *
 */
public class OFMessageAsyncStreamTest {
    @Test
    public void testMarshalling() throws Exception {
        OFPMessage h = new OFPHello();

        ServerSocketChannel serverSC = ServerSocketChannel.open();
        serverSC.socket().bind(new java.net.InetSocketAddress(0));
        serverSC.configureBlocking(false);

        SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", serverSC.socket().getLocalPort()));
        SocketChannel server = serverSC.accept();
        OFMessageAsyncStream clientStream = new OFMessageAsyncStream(client, new OFPBasicFactoryImpl());
        OFMessageAsyncStream serverStream = new OFMessageAsyncStream(server, new OFPBasicFactoryImpl());

        clientStream.write(h);
        while (clientStream.needsFlush()) {
            clientStream.flush();
        }
        List<OFPMessage> l = serverStream.read();
        Assert.assertEquals(l.size(), 1);
        OFPMessage m = l.get(0);
        Assert.assertEquals(m.getLength(), h.getLength());
        Assert.assertEquals(m.getVersion(), h.getVersion());
        Assert.assertEquals(m.getType(), h.getType());
        Assert.assertEquals(m.getType(), h.getType());
    }
}
