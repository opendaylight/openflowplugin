/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.listener;

import com.google.common.util.concurrent.Runnables;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.exception.DeviceDataException;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.impl.connection.testutil.MsgGeneratorTestUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;

/**
 * openflowplugin-api
 * org.opendaylight.openflowplugin.impl.openflow.device
 *
 * Test class for testing basic method functionality for {@link MultiMsgCollector}
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * @author <a href="mailto:tkubas@cisco.com">Timotej Kubas</a>
 *
 * Created: Mar 23, 2015
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiMsgCollectorImplTest {

    private MultiMsgCollectorImpl collector;
    private Runnable cleanUpCheck;

    @Mock
    DeviceReplyProcessor deviceProcessor;
    @Captor
    ArgumentCaptor<DeviceDataException> ddeCaptor;
    @Captor
    ArgumentCaptor<Xid> xidCaptor;
    @Captor
    ArgumentCaptor<List<MultipartReply>> mmCaptor;

    private final String hwTestValue = "test-value";
    private final String expectedExpirationMsg = "MultiMsgCollector can not wait for last multipart any more";
    private final String expectedTypMismatchMsg = "multipart message type mismatch";
    private final String expectedUnknownXidMsg = "unknown xid received";

    @Before
    public void setUp() {
        collector = new MultiMsgCollectorImpl(1);
        collector.setDeviceReplyProcessor(deviceProcessor);
        cleanUpCheck = Runnables.doNothing();
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(1100L);

        // flush cache action
        collector.registerMultipartXid(0L);
        cleanUpCheck.run();
        Mockito.verifyNoMoreInteractions(deviceProcessor);
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     *     success with message consisting of 1 part
     */
    @Test
    public void testAddMultipartMsgOne() {
        final long xid = 1L;
        collector.registerMultipartXid(xid);
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, false).build());

        Mockito.verify(deviceProcessor).processReply(xidCaptor.capture(), mmCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());

        List<MultipartReply> multipartReplyList = mmCaptor.getValue();
        Assert.assertEquals(1, multipartReplyList.size());
        Assert.assertEquals(MultipartType.OFPMPDESC, multipartReplyList.get(0).getType());
    }

    /**
     *  test of ${link MultiMsgCollector#addMultipartMsg} <br>
     *     success with message consisting of 2 parts
     */
    @Test
    public void testAddMultipartMsgTwo() {
        final long xid = 1L;
        collector.registerMultipartXid(xid);
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true).build());
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, false).build());

        Mockito.verify(deviceProcessor).processReply(xidCaptor.capture(), mmCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());

        List<MultipartReply> multipartReplyList = mmCaptor.getValue();
        Assert.assertEquals(2, multipartReplyList.size());
        Assert.assertEquals(MultipartType.OFPMPDESC, multipartReplyList.get(0).getType());
        Assert.assertEquals(MultipartType.OFPMPDESC, multipartReplyList.get(1).getType());
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     *     xid not registered before message
     */
    @Test
    public void testAddMultipartMsgNotExpectedXid() {
        final long xid = 1L;
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true).build());

        Mockito.verify(deviceProcessor).processException(xidCaptor.capture(), ddeCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());
        Assert.assertEquals(expectedUnknownXidMsg, ddeCaptor.getValue().getMessage());
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     *     message types are inconsistent - second message is final and should be rejected
     */
    @Test
    public void testAddMultipartMsgWrongType1() {
        final long xid = 1L;
        collector.registerMultipartXid(xid);
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true).build());
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, false)
                .setType(MultipartType.OFPMPPORTDESC).build());


        Mockito.verify(deviceProcessor).processException(xidCaptor.capture(), ddeCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());
        Assert.assertEquals(expectedTypMismatchMsg, ddeCaptor.getValue().getMessage());

        Mockito.reset(deviceProcessor);

        cleanUpCheck = new Runnable() {
            @Override
            public void run() {
                Mockito.verify(deviceProcessor).processException(xidCaptor.capture(), ddeCaptor.capture());
                Assert.assertEquals(xid, xidCaptor.getValue().getValue());
                Assert.assertEquals(expectedExpirationMsg, ddeCaptor.getValue().getMessage());
            }
        };
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     *     message types are inconsistent - second message is not final and should be rejected
     */
    @Test
    public void testAddMultipartMsgWrongType2() {
        final long xid = 1L;
        collector.registerMultipartXid(xid);
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true).build());
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true)
                .setType(MultipartType.OFPMPPORTDESC).build());

        Mockito.verify(deviceProcessor).processException(xidCaptor.capture(), ddeCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());
        Assert.assertEquals(expectedTypMismatchMsg, ddeCaptor.getValue().getMessage());

        Mockito.reset(deviceProcessor);

        cleanUpCheck = new Runnable() {
            @Override
            public void run() {
                Mockito.verify(deviceProcessor).processException(xidCaptor.capture(), ddeCaptor.capture());
                Assert.assertEquals(xid, xidCaptor.getValue().getValue());
                Assert.assertEquals(expectedExpirationMsg, ddeCaptor.getValue().getMessage());
            }
        };
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     *     message types are inconsistent - second message and third should be rejected
     */
    @Test
    public void testAddMultipartMsgWrongType3() {
        final long xid = 1L;
        collector.registerMultipartXid(xid);
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true).build());
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true)
                .setType(MultipartType.OFPMPPORTDESC).build());
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, false).build());

        Mockito.verify(deviceProcessor).processException(xidCaptor.capture(), ddeCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());
        Assert.assertEquals(expectedTypMismatchMsg, ddeCaptor.getValue().getMessage());

        Mockito.verify(deviceProcessor).processReply(xidCaptor.capture(), mmCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());

        List<MultipartReply> multipartReplyList = mmCaptor.getValue();
        Assert.assertEquals(2, multipartReplyList.size());
        Assert.assertEquals(MultipartType.OFPMPDESC, multipartReplyList.get(0).getType());
        Assert.assertEquals(MultipartType.OFPMPDESC, multipartReplyList.get(1).getType());
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     *     no second message arrived within expiration limit - first message should expire
     */
    @Test
    public void testAddMultipartMsgExpiration() throws InterruptedException {
        final long xid = 1L;
        collector.registerMultipartXid(xid);
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true).build());

        cleanUpCheck = new Runnable() {
            @Override
            public void run() {
                Mockito.verify(deviceProcessor).processException(xidCaptor.capture(), ddeCaptor.capture());
                Assert.assertEquals(xid, xidCaptor.getValue().getValue());
                Assert.assertEquals(expectedExpirationMsg, ddeCaptor.getValue().getMessage());
            }
        };
    }

}
