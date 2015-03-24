/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDescBuilder;

/**
 * openflowplugin-api
 * org.opendaylight.openflowplugin.impl.openflow.device
 *
 * Test class for testing basic method functionality for {@link org.opendaylight.openflowplugin.api.openflow.device.MultiMsgCollector}
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * @author <a href="mailto:tkubas@cisco.com">Timotej Kubas</a>
 *
 * Created: Mar 23, 2015
 */
public class MultiMsgCollectorImplTest {

    private MultiMsgCollectorImpl collector;

    @Before
    public void initialization() {
        collector = new MultiMsgCollectorImpl(1);
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.openflow.device.MultiMsgCollectorImpl#registerMultipartMsg(org.opendaylight.openflowplugin.api.openflow.device.Xid)}.
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    public void testRegisterMultipartMsg() throws InterruptedException, ExecutionException, TimeoutException{
        final long xid = 45L;
        final String hwTestValue = "test-value";
        final ListenableFuture<Collection<MultipartReply>> response = collector.registerMultipartMsg(xid);
        collector.addMultipartMsg(makeMultipartDescReply(xid, hwTestValue, false));

        validateDescReply(response, xid, Collections.singletonList(hwTestValue));
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.openflow.device.MultiMsgCollectorImpl#addMultipartMsg(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply)}.
     * @throws TimeoutException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testAddMultipartMsg() throws InterruptedException, ExecutionException, TimeoutException{
        final long xid = 22L;
        final String hwTestValue1 = "test-value1";
        final String hwTestValue2 = "test-value2";
        final ListenableFuture<Collection<MultipartReply>> response = collector.registerMultipartMsg(xid);
        collector.addMultipartMsg(makeMultipartDescReply(xid, hwTestValue1, true));
        collector.addMultipartMsg(makeMultipartDescReply(xid, hwTestValue2, false));

        validateDescReply(response, xid, Arrays.asList(hwTestValue1, hwTestValue2));
    }

    /**
     * Test could return NullPointerException if the body of addMultipartMsg not
     */
    @Test
    public void testAddMultipartMsgNotExpectedXid() {
        final long xid = 23L;
        final String hwTestValue = "test-value";
        collector.addMultipartMsg(makeMultipartDescReply(xid, hwTestValue, true));
    }

    /**
     * Test could return NullPointerException if the body of addMultipartMsg not
     * @throws InterruptedException
     */
    @Test(timeout=20000)
    public void testCheckExistMultipartMsgInCacheAfterTimeout() throws InterruptedException, ExecutionException {
        final long xid = 24L;
        final ListenableFuture<Collection<MultipartReply>> response = collector.registerMultipartMsg(xid);
        assertNotNull(response);
        Thread.sleep(2000);
        collector.addMultipartMsg(makeMultipartDescReply(xid, "hw-text-value", false));
        try {
            response.get(1L, TimeUnit.SECONDS);
            fail("We expected timeout exception");
        }
        catch (final TimeoutException e) {
            // expected exception
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

    private MultipartReply makeMultipartDescReply(final long xid, final String value, final boolean isLast) {
        final MultipartReplyDesc descValue = new MultipartReplyDescBuilder().setHwDesc(value).build();
        final MultipartReplyDescCase replyBody = new MultipartReplyDescCaseBuilder()
                                                        .setMultipartReplyDesc(descValue).build();
        return new MultipartReplyMessageBuilder().setMultipartReplyBody(replyBody)
                .setXid(xid).setFlags(new MultipartRequestFlags(isLast)).build();
    }
}
