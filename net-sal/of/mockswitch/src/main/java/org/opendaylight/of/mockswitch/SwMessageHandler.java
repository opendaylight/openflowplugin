/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.ssl.SslHandler;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.msg.*;

import java.util.concurrent.CountDownLatch;

/**
 * Handles incoming OpenFlow messages for the mock-switch.
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Sudheer Duggisetty
 */
public class SwMessageHandler extends SimpleChannelUpstreamHandler {

    private volatile CountDownLatch internalMsgLatch;
    private final MockOpenflowSwitch sw;
    private boolean secure = false;
    private volatile boolean shuttingDown = false;

    /** Constructs a mock-switch message handler.
     *
     * @param sw the switch for which we handle messages
     */
    public SwMessageHandler(MockOpenflowSwitch sw) {
        this.sw = sw;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx,
                                 ChannelStateEvent e) throws Exception {
        if (secure) {
            // Get the SslHandler from the pipeline which were added in
            // SecureChatPipelineFactory.
            SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
            // Begin handshake.
            sslHandler.handshake();
        }
    }

    /**
     * Invoked when an exception was raised by an I/O thread or a
     * {@link org.jboss.netty.channel.ChannelHandler}.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        // We simply want the exception to propagate upwards.
        if (!shuttingDown)
            throw new RuntimeException(e.getCause());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        OpenflowMessage msg = (OpenflowMessage) e.getMessage();
        processMessage(msg);
    }

    /** Process a single OpenFlow message received by the mock-switch
     * from our test-harness controller.
     *
     * @param msg the message
     */
    private void processMessage(OpenflowMessage msg) {
        if (internalMsgLatch != null)
            internalMsgLatch.countDown();

        print("<<< mockswitch.recv <<< " + msg);

        switch (msg.getType()) {
            case HELLO:
                processHello((OfmHello) msg);
                break;

            case ERROR:
                processError((OfmError) msg);
                break;

            case FEATURES_REQUEST:
                processFeaturesRequest((OfmFeaturesRequest) msg);
                break;

            case MULTIPART_REQUEST:
                processMultipartRequest((OfmMultipartRequest) msg);
                break;

            case ECHO_REPLY:
                sw.reconcileEcho((OfmEchoReply) msg);
                break;

            case SET_CONFIG:
                // do nothing
                break;

            default:
                sw.msgRx(msg);
                break;
        }
    }

    protected void processHello(OfmHello msg) {
        sw.mainConn.inBoundHello(msg, new MockOpenflowSwitch.Memo(sw));
        if (sw.cfgHello.getBehavior() == CfgHello.Behavior.LAZY)
            sw.sendMyHello();
    }

    protected void processError(OfmError msg) {
        if (msg.getErrorType() != ErrorType.HELLO_FAILED)
            sw.msgRx(msg);
        sw.helloFailed(msg);

    }

    protected void processFeaturesRequest(OfmFeaturesRequest msg) {
        sw.sendFeaturesReply(msg);
    }

    protected void processMultipartRequest(OfmMultipartRequest msg) {
        if (!sw.handleMultipartRequest(msg))
            sw.msgRx(msg);
    }

    /** Prints the given string, if output is enabled.
     *
     * @param s the string to print
     */
    private void print(String s) {
        if (sw.showOutput)
            System.out.println(s);
    }

    /** Sets the internal message latch.
     *
     * @param latch the latch
     */
    void setInternalMsgLatch(CountDownLatch latch) {
        internalMsgLatch = latch;
    }

    /** Sets the secure flag.
     *
     * @param secure the secure flag is enabled or not.
     */
    void setSecureEnabled(boolean secure) {
        this.secure = secure;
    }

    /** The switch is shutting down. */
    void shuttingDown() {
        shuttingDown = true;
    }
}
