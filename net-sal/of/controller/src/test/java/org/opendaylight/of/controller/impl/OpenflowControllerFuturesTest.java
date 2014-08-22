/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.Test;
import org.opendaylight.of.common.MessageSink;
import org.opendaylight.of.controller.AlertSink;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.cache.NotedAgeOutHashMap;
import org.opendaylight.util.net.IpAddress;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.mp.MultipartType.FLOW;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageFuture.Result.*;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.junit.TestTools.*;


/**
 * Unit tests for {@link org.opendaylight.of.controller.impl.OpenflowController} futures.
 *
 * @author Frank Wood
 */
public class OpenflowControllerFuturesTest extends AbstractControllerTest {

    private static class TestController extends OpenflowController {
        private boolean sendThrowsException = false;

        TestController(PortStateTracker pst, MessageSink sink, AlertSink as) {
            super(DEFAULT_CTRL_CFG, pst, sink, as, PH_SINK, PH_CB, FM_ADV);
        }

        TestController(PortStateTracker pst, MessageSink sink, AlertSink as,
                       boolean sendThrowsException) {
            super(DEFAULT_CTRL_CFG, pst, sink, as, PH_SINK, PH_CB, FM_ADV);
            this.sendThrowsException = sendThrowsException;
        }

        @Override
        void send(OpenflowMessage msg, DataPathId dpid, int auxId)
                throws OpenflowException {
            if (sendThrowsException)
                throw new OpenflowException("test send error");
        }
    }

    private static class TestSink implements MessageSink {
        @Override
        public void dataPathAdded(DataPathId dpid, ProtocolVersion negotiated,
                IpAddress ip) {}

        @Override
        public void dataPathRemoved(DataPathId dpid, ProtocolVersion negotiated,
                IpAddress ip) {}

        @Override
        public void dataPathRevoked(DataPathId dpid, ProtocolVersion negotiated,
                                    IpAddress ip) { }

        @Override
        public void msgRx(OpenflowMessage msg, DataPathId dpid, int auxId,
                ProtocolVersion negotiated) {}
    }

    private final PortStateTracker pst = new PortStateTracker(lmgr);
    private final MessageSink sink = new TestSink();
    private final AlertSink as = new AlertLogger();
    private SocketChannel sc;

    private OpenflowController setup(DataPathId... dpids) throws IOException {
        return setup(new TestController(pst, sink, as), dpids);
    }

    private OpenflowController setup(OpenflowController ctlr,
            DataPathId... dpids) throws IOException {
        ctlr.suppressSetConfig(true);
        sc = SocketChannel.open();
        for (DataPathId dpid: dpids) {
            OpenflowConnection conn = new OpenflowConnection(sc, null);
            conn.dpid = dpid;
            ctlr.handshakeComplete(conn);
        }
        return ctlr;
    }

    private void tearDown(OpenflowController ctlr, DataPathId... dpids)
            throws IOException {
        for (DataPathId dpid: dpids)
            ctlr.getDpInfo(dpid).mainConn.close();
        sc.close(); // nop - never really opened
    }

    private NotedAgeOutHashMap<Long, DataPathMessageFuture>
            futures(OpenflowController ctlr, DataPathId dpid) {
        return ctlr.getDpInfo(dpid).pendingFutures;
    }

    @Test
    public void singleMessage() throws IOException, OpenflowException {
        OpenflowController ctlr = setup(SW13P32_DPID);

        assertEquals(AM_NEQ, 1, ctlr.infoCacheSize());
        assertEquals(AM_NEQ, 0, futures(ctlr, SW13P32_DPID).size());

        OpenflowMessage req = create(V_1_3, ECHO_REQUEST).toImmutable();
        DataPathMessageFuture f = new DataPathMessageFuture(req, SW13P32_DPID);

        ctlr.sendFuture(f, req);

        assertEquals(AM_NEQ, 1, futures(ctlr, SW13P32_DPID).size());
        assertEquals(AM_NEQ, f, futures(ctlr, SW13P32_DPID).get(req.getXid()));

        OpenflowMessage reply = create(req, ECHO_REPLY).toImmutable();

        assertEquals(AM_NEQ, f, ctlr.findFuture(reply, SW13P32_DPID));

        ctlr.successFuture(f, reply);
        assertEquals(AM_NEQ, reply, f.reply());
        assertEquals(AM_NEQ, SUCCESS, f.result());

        assertEquals(AM_NEQ, 0, futures(ctlr, SW13P32_DPID).size());

        tearDown(ctlr);
    }
    
    @Test
    public void singleMessagePrune() throws IOException, OpenflowException {
        OpenflowController ctlr = setup(SW13P32_DPID);

        futures(ctlr, SW13P32_DPID).setAgeOut(10);

        assertEquals(AM_NEQ, 1, ctlr.infoCacheSize());
        assertEquals(AM_NEQ, 0, futures(ctlr, SW13P32_DPID).size());

        OpenflowMessage req = create(V_1_3, ECHO_REQUEST).toImmutable();
        DataPathMessageFuture f = new DataPathMessageFuture(req, SW13P32_DPID);

        ctlr.sendFuture(f, req);

        assertEquals(AM_NEQ, 1, futures(ctlr, SW13P32_DPID).size());
        assertEquals(AM_NEQ, f, futures(ctlr, SW13P32_DPID).get(req.getXid()));
        
        delay(20);
        ctlr.prunePendingFutures();
        
        assertEquals(AM_NEQ, 0, futures(ctlr, SW13P32_DPID).size());

        tearDown(ctlr);
    }

    @Test
    public void multiSwitchPrune() throws IOException, OpenflowException {
        DataPathId[] dpids = { SW13P32_DPID, SW10P4_DPID };
        OpenflowController ctlr = setup(dpids);

        futures(ctlr, dpids[0]).setAgeOut(100);
        futures(ctlr, dpids[1]).setAgeOut(100);
        
        OpenflowMessage[] reqs = new OpenflowMessage[dpids.length];
        DataPathMessageFuture[] f = new DataPathMessageFuture[dpids.length];

        for (int i=0; i<dpids.length; i++) {
            reqs[i] = create(V_1_3, ECHO_REQUEST).toImmutable();
            f[i] = new DataPathMessageFuture(reqs[i], dpids[i]);
            ctlr.sendFuture(f[i], reqs[i]);
        }

        for (int i=0; i<dpids.length; i++) {
            assertEquals(AM_NEQ, 1, futures(ctlr, dpids[i]).size());
            assertEquals(AM_NEQ, f[i],
                    futures(ctlr, dpids[i]).get(reqs[i].getXid()));
        }

        OpenflowMessage reply = create(reqs[0], ECHO_REPLY).toImmutable();

        ctlr.successFuture(f[0], reply);
        assertEquals(AM_NEQ, reply, f[0].reply());
        assertEquals(AM_NEQ, SUCCESS, f[0].result());

        delay(150);
        ctlr.prunePendingFutures();
        
        for (DataPathId dpid: dpids)
            assertEquals(AM_NEQ, 0, futures(ctlr, dpid).size());

        tearDown(ctlr);
    }
    
    @Test
    public void barrierSuccessMessage() throws IOException, OpenflowException {
        OpenflowController ctlr = setup(SW13P32_DPID);

        assertEquals(AM_NEQ, 1, ctlr.infoCacheSize());
        assertEquals(AM_NEQ, 0, futures(ctlr, SW13P32_DPID).size());

        OpenflowMessage fmReq = create(V_1_3, FLOW_MOD).toImmutable();
        DataPathMessageFuture f =
                new DataPathMessageFuture(fmReq, SW13P32_DPID);

        OpenflowMessage bReq = create(fmReq, BARRIER_REQUEST).toImmutable();

        ctlr.sendFuture(f, fmReq, bReq);

        assertEquals(AM_NEQ, 1, futures(ctlr, SW13P32_DPID).size());
        assertEquals(AM_NEQ, f,
                futures(ctlr, SW13P32_DPID).get(fmReq.getXid()));

        OpenflowMessage bReply = create(bReq, BARRIER_REPLY).toImmutable();

        assertEquals(AM_NEQ, f, ctlr.findFuture(bReply, SW13P32_DPID));

        ctlr.successFuture(f, bReply);
        assertEquals(AM_NEQ, bReply, f.reply());
        assertEquals(AM_NEQ, SUCCESS, f.result());

        assertEquals(AM_NEQ, 0, futures(ctlr, SW13P32_DPID).size());

        tearDown(ctlr);
    }

    @Test
    public void barrierFailMessage() throws IOException, OpenflowException {
        OpenflowController ctlr = setup(SW13P32_DPID);

        assertEquals(AM_NEQ, 1, ctlr.infoCacheSize());
        assertEquals(AM_NEQ, 0, futures(ctlr, SW13P32_DPID).size());

        OpenflowMessage fmReq = create(V_1_3, FLOW_MOD).toImmutable();
        DataPathMessageFuture f =
                new DataPathMessageFuture(fmReq, SW13P32_DPID);

        OpenflowMessage bReq = create(fmReq, BARRIER_REQUEST).toImmutable();

        ctlr.sendFuture(f, fmReq, bReq);

        assertEquals(AM_NEQ, 1, futures(ctlr, SW13P32_DPID).size());
        assertEquals(AM_NEQ, f,
                futures(ctlr, SW13P32_DPID).get(fmReq.getXid()));

        OpenflowMessage err = create(fmReq, ERROR).toImmutable();

        assertEquals(AM_NEQ, f, ctlr.findFuture(err, SW13P32_DPID));

        ctlr.failFuture(f, (OfmError) err);
        assertEquals(AM_NEQ, err, f.reply());
        assertEquals(AM_NEQ, OFM_ERROR, f.result());

        assertEquals(AM_NEQ, 0, futures(ctlr, SW13P32_DPID).size());

        OpenflowMessage bReply = create(bReq, BARRIER_REPLY).toImmutable();
        assertNull(AM_HUH, ctlr.findFuture(bReply, SW13P32_DPID));

        tearDown(ctlr);
    }

    @Test
    public void multiPartMessage() throws IOException, OpenflowException {
        OpenflowController ctlr = setup(SW13P32_DPID);

        assertEquals(AM_NEQ, 1, ctlr.infoCacheSize());
        assertEquals(AM_NEQ, 0, futures(ctlr, SW13P32_DPID).size());

        OpenflowMessage req =
                create(V_1_3, MULTIPART_REQUEST, FLOW).toImmutable();
        DataPathMessageFuture f = new DataPathMessageFuture(req, SW13P32_DPID);

        ctlr.sendFuture(f, req);

        assertEquals(AM_NEQ, 1, futures(ctlr, SW13P32_DPID).size());
        assertEquals(AM_NEQ, f, futures(ctlr, SW13P32_DPID).get(req.getXid()));

        OfmMutableMultipartReply reply0 = (OfmMutableMultipartReply)
                create(req, MULTIPART_REPLY);
        reply0.setMoreFlag();

        OfmMultipartReply replyLast = (OfmMultipartReply)
                create(req, MULTIPART_REPLY).toImmutable();

        assertEquals(AM_NEQ, f,
                ctlr.findFuture(reply0.toImmutable(), SW13P32_DPID));

        assertEquals(AM_NEQ, 1, futures(ctlr, SW13P32_DPID).size());

        assertEquals(AM_NEQ, f, ctlr.findFuture(replyLast, SW13P32_DPID));
        ctlr.successFuture(f, replyLast);
        assertEquals(AM_NEQ, replyLast, f.reply());
        assertEquals(AM_NEQ, SUCCESS, f.result());

        assertEquals(AM_NEQ, 0, futures(ctlr, SW13P32_DPID).size());

        tearDown(ctlr);
    }

    @Test
    public void multiSwitchMessages() throws IOException, OpenflowException {
        DataPathId[] dpids = { SW13P32_DPID, SW10P4_DPID, SW10P12_DPID };
        OpenflowController ctlr = setup(dpids);

        assertEquals(AM_NEQ, 3, ctlr.infoCacheSize());
        for (DataPathId dpid: dpids)
            assertEquals(AM_NEQ, 0, futures(ctlr, dpid).size());

        OpenflowMessage[] reqs = new OpenflowMessage[dpids.length];
        DataPathMessageFuture[] f = new DataPathMessageFuture[dpids.length];

        for (int i=0; i<dpids.length; i++) {
            reqs[i] = create(V_1_3, ECHO_REQUEST).toImmutable();
            f[i] = new DataPathMessageFuture(reqs[i], dpids[i]);
            ctlr.sendFuture(f[i], reqs[i]);
        }

        for (int i=0; i<dpids.length; i++) {
            assertEquals(AM_NEQ, 1, futures(ctlr, dpids[i]).size());
            assertEquals(AM_NEQ, f[i],
                    futures(ctlr, dpids[i]).get(reqs[i].getXid()));
        }

        OpenflowMessage[] replies = new OpenflowMessage[dpids.length];
        replies[0] = create(reqs[0], ECHO_REPLY).toImmutable();
        replies[1] = create(reqs[1], ERROR).toImmutable();

        assertEquals(AM_NEQ, f[0], ctlr.findFuture(replies[0], dpids[0]));
        assertEquals(AM_NEQ, f[1], ctlr.findFuture(replies[1], dpids[1]));

        ctlr.successFuture(f[0], replies[0]);
        assertEquals(AM_NEQ, replies[0], f[0].reply());
        assertEquals(AM_NEQ, SUCCESS, f[0].result());

        ctlr.failFuture(f[1], (OfmError) replies[1]);
        assertEquals(AM_NEQ, replies[1], f[1].reply());
        assertEquals(AM_NEQ, OFM_ERROR, f[1].result());

        OpenflowException e = new OpenflowException();
        ctlr.failFuture(f[2], e);
        assertEquals(AM_NEQ, e, f[2].cause());
        assertEquals(AM_NEQ, EXCEPTION, f[2].result());

        for (DataPathId dpid: dpids)
            assertEquals(AM_NEQ, 0, futures(ctlr, dpid).size());

        tearDown(ctlr);
    }

    @Test
    public void dupTxid() throws IOException, OpenflowException {
        OpenflowController ctlr = setup(SW10P4_DPID);

        assertEquals(AM_NEQ, 1, ctlr.infoCacheSize());
        assertEquals(AM_NEQ, 0, futures(ctlr, SW10P4_DPID).size());

        OpenflowMessage req = create(V_1_3, ECHO_REQUEST).toImmutable();
        DataPathMessageFuture f = new DataPathMessageFuture(req, SW10P4_DPID);

        ctlr.sendFuture(f, req);
        try {
            ctlr.sendFuture(f, req);
            fail("duplicate transaction ID should throw exception");
        } catch (OpenflowException e) {
            assertEquals(AM_NEQ, e, f.cause());
        }

        assertEquals(AM_NEQ, EXCEPTION, f.result());
        assertEquals(AM_NEQ, 0, futures(ctlr, SW10P4_DPID).size());

        tearDown(ctlr);
    }

    @Test
    public void sendFail() throws IOException {
        OpenflowController ctlr =
                setup(new TestController(pst, sink, as, true), SW10P4_DPID);

        assertEquals(AM_NEQ, 1, ctlr.infoCacheSize());
        assertEquals(AM_NEQ, 0, futures(ctlr, SW10P4_DPID).size());

        OpenflowMessage req = create(V_1_3, ECHO_REQUEST).toImmutable();
        DataPathMessageFuture f = new DataPathMessageFuture(req, SW10P4_DPID);

        try {
            ctlr.sendFuture(f, req);
            fail("send should throw exception");
        } catch (OpenflowException e) {
            assertEquals(AM_NEQ, e, f.cause());
        }

        assertEquals(AM_NEQ, EXCEPTION, f.result());
        assertEquals(AM_NEQ, 0, futures(ctlr, SW10P4_DPID).size());

        tearDown(ctlr);
    }

    @Test
    public void multiFutures() throws IOException, OpenflowException {
        OpenflowController ctlr = setup(SW13P32_DPID);

        assertEquals(AM_NEQ, 1, ctlr.infoCacheSize());
        assertEquals(AM_NEQ, 0, futures(ctlr, SW13P32_DPID).size());

        OpenflowMessage req0 = create(V_1_3, ECHO_REQUEST).toImmutable();
        DataPathMessageFuture f0 =
                new DataPathMessageFuture(req0, SW13P32_DPID);

        ctlr.sendFuture(f0, req0);

        OpenflowMessage req1 = create(V_1_3, ECHO_REQUEST).toImmutable();
        DataPathMessageFuture f1 =
                new DataPathMessageFuture(req1, SW13P32_DPID);

        ctlr.sendFuture(f1, req1);

        assertEquals(AM_NEQ, 2, futures(ctlr, SW13P32_DPID).size());
        assertEquals(AM_NEQ, f0,
                futures(ctlr, SW13P32_DPID).get(req0.getXid()));
        assertEquals(AM_NEQ, f1,
                futures(ctlr, SW13P32_DPID).get(req1.getXid()));

        OpenflowMessage reply0 = create(req0, ECHO_REPLY).toImmutable();

        assertEquals(AM_NEQ, f0, ctlr.findFuture(reply0, SW13P32_DPID));

        ctlr.successFuture(f0, reply0);
        assertEquals(AM_NEQ, reply0, f0.reply());
        assertEquals(AM_NEQ, SUCCESS, f0.result());

        assertEquals(AM_NEQ, 1, futures(ctlr, SW13P32_DPID).size());

        OpenflowMessage reply1 = create(req1, ECHO_REPLY).toImmutable();

        assertNull(AM_HUH, ctlr.findFuture(reply0, SW13P32_DPID));
        assertEquals(AM_NEQ, f1, ctlr.findFuture(reply1, SW13P32_DPID));

        ctlr.successFuture(f1, reply1);
        assertEquals(AM_NEQ, reply1, f1.reply());
        assertEquals(AM_NEQ, SUCCESS, f1.result());

        assertEquals(AM_NEQ, 0, futures(ctlr, SW13P32_DPID).size());

        tearDown(ctlr);
    }

}
