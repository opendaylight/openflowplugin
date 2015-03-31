/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.impl.connection.testutil.MsgGeneratorTestUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * openflowplugin-api
 * org.opendaylight.openflowplugin.impl.openflow.device
 * <p/>
 * Test class for testing basic method functionality for {@link org.opendaylight.openflowplugin.api.openflow.connection.MultiMsgCollector}
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * @author <a href="mailto:tkubas@cisco.com">Timotej Kubas</a>
 *         <p/>
 *         Created: Mar 23, 2015
 */
public class MultiMsgCollectorImplTest {

    private MultiMsgCollectorImpl collector;
    private static final long xid = 1l;

    @Before
    public void initialization() {
        collector = new MultiMsgCollectorImpl(3);
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.openflow.device.MultiMsgCollectorImpl#registerMultipartMsg(org.opendaylight.openflowplugin.api.openflow.device.Xid)}.
     *
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    public void testRegisterMultipartMsg() throws InterruptedException, ExecutionException, TimeoutException {
        final String hwTestValue = "test-value";
        final ListenableFuture<Collection<MultipartReply>> response = collector.registerMultipartMsg(xid);
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, false));

        validateDescReply(response, xid, Collections.singletonList(hwTestValue));
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.openflow.device.MultiMsgCollectorImpl#addMultipartMsg(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply)}.
     *
     * @throws TimeoutException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testAddMultipartMsg() throws InterruptedException, ExecutionException, TimeoutException {
        final String hwTestValue1 = "test-value1";
        final String hwTestValue2 = "test-value2";
        final ListenableFuture<Collection<MultipartReply>> response = collector.registerMultipartMsg(xid);
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue1, true));
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue2, false));

        validateDescReply(response, xid, Arrays.asList(hwTestValue1, hwTestValue2));
    }

    /**
     * Test could return NullPointerException if the body of addMultipartMsg not
     */
    @Test
    public void testAddMultipartMsgNotExpectedXid() {
        final String hwTestValue = "test-value";
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true));
    }

    /**
     * Test could return NullPointerException if the body of addMultipartMsg not
     *
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    public void testCheckExistMultipartMsgInCacheAfterTimeout() throws InterruptedException, TimeoutException {
        try {
            final ListenableFuture<Collection<MultipartReply>> response = collector.registerMultipartMsg(xid);
            assertNotNull(response);
            collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, "hw-test-value", true));
            Thread.sleep(4000);
            collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, "hw-test-value2", false));
            collector.registerMultipartMsg(5L);
            response.get(1, TimeUnit.SECONDS);
            fail("We expected timeout exception");
        } catch (final ExecutionException e) {
            assertNotNull(e.getMessage());
            assertNotNull(e.getMessage().contains("" + xid));
            assertNotNull(e.getCause() instanceof TimeoutException);
        }
    }

    /**
     * Test returns ExcetionException because we set different MultipartType as is expected for second
     * message. So Internal {@link MultiMsgCollectorImpl} validation has to rise IllgalStateException
     *
     * @throws InterruptedException
     */
    @Test
    public void testCheckErrorForBadMultipartMsgType() throws InterruptedException {
        final ListenableFuture<Collection<MultipartReply>> response = collector.registerMultipartMsg(xid);
        assertNotNull(response);
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, "hw-text-value", true));
        final MultipartReply nextMultipart = new MultipartReplyMessageBuilder(MsgGeneratorTestUtils
                .makeMultipartDescReply(xid, "hw-test-next", true)).setType(MultipartType.OFPMPEXPERIMENTER).build();
        collector.addMultipartMsg(nextMultipart);
        try {
            response.get();
            fail("We expect Illgal argument exception in ExecutionException");
        } catch (final ExecutionException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("OFPMPEXPERIMENTER"));
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    private void validateDescReply(final ListenableFuture<Collection<MultipartReply>> response, final long xid,
                                   final Collection<String> hwTestValues) throws InterruptedException, ExecutionException, TimeoutException {
        assertNotNull(response);
        assertNotNull(xid);
        assertNotNull(hwTestValues);

        final Collection<MultipartReply> multipartReplyColl = response.get(1L, TimeUnit.SECONDS);
        assertNotNull(multipartReplyColl);
        assertTrue(multipartReplyColl.size() > 0);
        for (final MultipartReply reply : multipartReplyColl) {
            assertEquals(xid, reply.getXid().longValue());
            assertTrue(reply.getMultipartReplyBody() instanceof MultipartReplyDescCase);
            final String replayHwTestString = ((MultipartReplyDescCase) reply.getMultipartReplyBody())
                    .getMultipartReplyDesc().getHwDesc();
            assertTrue(hwTestValues.contains(replayHwTestString));
        }
    }
}
