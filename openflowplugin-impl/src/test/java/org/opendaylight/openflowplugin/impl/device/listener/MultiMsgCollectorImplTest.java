/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.listener;

import com.google.common.util.concurrent.Runnables;
import java.math.BigInteger;
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
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.DeviceRequestFailedException;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.impl.connection.testutil.MsgGeneratorTestUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;

/**
 * openflowplugin-api
 * org.opendaylight.openflowplugin.impl.openflow.device
 * <p/>
 * Test class for testing basic method functionality for {@link MultiMsgCollector}
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * @author <a href="mailto:tkubas@cisco.com">Timotej Kubas</a>
 *         <p/>
 *         Created: Mar 23, 2015
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiMsgCollectorImplTest {

    private MultiMsgCollectorImpl collector;
    private Runnable cleanUpCheck;

    @Mock
    DeviceReplyProcessor deviceProcessor;
    @Captor
    ArgumentCaptor<DeviceRequestFailedException> ddeCaptor;
    @Captor
    ArgumentCaptor<Xid> xidCaptor;
    @Captor
    ArgumentCaptor<List<MultipartReply>> mmCaptor;
    @Mock
    RequestContext<List<MultipartReply>> requestContext;
    final Long xid = 1L;

    @Mock
    DeviceContext deviceContext;
    @Mock
    DeviceState deviceState;
    @Mock
    ConnectionContext primConnContext;
    @Mock
    FeaturesReply futures;

    private final String hwTestValue = "test-value";
    private final String expectedExpirationMsg = "MultiMsgCollector can not wait for last multipart any more";
    private final String expectedTypMismatchMsg = "multipart message type mismatch";
    private final String expectedUnknownXidMsg = "unknown xid received for multipart of type OFPMPDESC";

    @Before
    public void setUp() {
        collector = new MultiMsgCollectorImpl(deviceProcessor, requestContext);
        cleanUpCheck = Runnables.doNothing();
        Mockito.when(requestContext.getXid()).thenReturn(new Xid(xid));

        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState );
        Mockito.when(futures.getVersion()).thenReturn((short)0x04);
        Mockito.when(futures.getDatapathId()).thenReturn(BigInteger.ONE);
        Mockito.when(primConnContext.getFeatures()).thenReturn(futures);
        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(primConnContext);
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(1100L);

        // flush cache action
        cleanUpCheck.run();
        Mockito.verifyNoMoreInteractions(deviceProcessor);
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     * success with message consisting of 1 part
     */
    @Test
    public void testAddMultipartMsgOne() {
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, false).build());

        Mockito.verify(deviceProcessor).processReply(xidCaptor.capture(), mmCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());

        final List<MultipartReply> multipartReplyList = mmCaptor.getValue();
        Assert.assertEquals(1, multipartReplyList.size());
        Assert.assertEquals(MultipartType.OFPMPDESC, multipartReplyList.get(0).getType());
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     * success with message consisting of 2 parts
     */
    @Test
    public void testAddMultipartMsgTwo() {
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true).build());
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, false).build());

        Mockito.verify(deviceProcessor).processReply(xidCaptor.capture(), mmCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());

        final List<MultipartReply> multipartReplyList = mmCaptor.getValue();
        Assert.assertEquals(2, multipartReplyList.size());
        Assert.assertEquals(MultipartType.OFPMPDESC, multipartReplyList.get(0).getType());
        Assert.assertEquals(MultipartType.OFPMPDESC, multipartReplyList.get(1).getType());
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     * xid not registered before message
     */
    @Test(expected=IllegalArgumentException.class)
    public void testAddMultipartMsgNotExpectedXid() {
        final Long dif_xid = 5L;
        final MultipartReplyMessage mrMsg = MsgGeneratorTestUtils.makeMultipartDescReply(dif_xid, hwTestValue, true).build();
        collector.addMultipartMsg(mrMsg);
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     * message types are inconsistent - second message is final and should be rejected
     */
    @Test
    public void testAddMultipartMsgWrongType1() {
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true).build());
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, false)
                .setType(MultipartType.OFPMPPORTDESC).build());

        Mockito.verify(deviceProcessor).processReply(xidCaptor.capture(), mmCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());

        Mockito.reset(deviceProcessor);

        cleanUpCheck = new Runnable() {
            @Override
            public void run() {
                Mockito.verify(deviceProcessor, VerificationModeFactory.noMoreInteractions())
                    .processReply(xidCaptor.capture(), mmCaptor.capture());
                Assert.assertEquals(xid, xidCaptor.getValue().getValue());
            }
        };
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     * message types are inconsistent - second message is not final and should be rejected
     */
    @Test
    public void testAddMultipartMsgWrongType2() {
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true).build());
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true)
                .setType(MultipartType.OFPMPPORTDESC).build());
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, false).build());

        Mockito.verify(deviceProcessor).processReply(xidCaptor.capture(), mmCaptor.capture());
        Mockito.verify(deviceProcessor).processReply(xidCaptor.capture(), mmCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());

        Mockito.reset(deviceProcessor);

        cleanUpCheck = new Runnable() {
            @Override
            public void run() {
                Mockito.verify(deviceProcessor, VerificationModeFactory.noMoreInteractions())
                    .processReply(xidCaptor.capture(), mmCaptor.capture());
                Assert.assertEquals(xid, xidCaptor.getValue().getValue());
            }
        };
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     * message types are inconsistent - second message and third should be rejected
     */
    @Test
    public void testAddMultipartMsgWrongType3() {
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true).build());
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true)
                .setType(MultipartType.OFPMPPORTDESC).build());
        collector.addMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, false).build());

        Mockito.verify(deviceProcessor).processReply(xidCaptor.capture(), mmCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());

        Mockito.verify(deviceProcessor).processReply(xidCaptor.capture(), mmCaptor.capture());
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());

        final List<MultipartReply> multipartReplyList = mmCaptor.getValue();
        Assert.assertEquals(3, multipartReplyList.size());
        Assert.assertEquals(MultipartType.OFPMPDESC, multipartReplyList.get(0).getType());
        Assert.assertEquals(MultipartType.OFPMPPORTDESC, multipartReplyList.get(1).getType());
        Assert.assertEquals(MultipartType.OFPMPDESC, multipartReplyList.get(2).getType());
    }

    /**
     * test of ${link MultiMsgCollector#processingMultipartMsg} <br>
     * success with message consisting of 1 part
     */
    @Test
    public void testProcessingMultipartMsgOne() {
        collector.processingMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, false).build(), deviceContext );

        Mockito.verify(deviceProcessor).processReply(xidCaptor.capture(), mmCaptor.capture());
        Mockito.verify(deviceContext).submitTransaction();
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());

        final List<MultipartReply> multipartReplyList = mmCaptor.getValue();
        Assert.assertEquals(0, multipartReplyList.size());
    }

    /**
     * test of ${link MultiMsgCollector#addMultipartMsg} <br>
     * success with message consisting of 2 parts
     */
    @Test
    public void testProcessingMultipartMsgTwo() {
        collector.processingMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, true).build(), deviceContext);
        collector.processingMultipartMsg(MsgGeneratorTestUtils.makeMultipartDescReply(xid, hwTestValue, false).build(), deviceContext);

        Mockito.verify(deviceProcessor).processReply(xidCaptor.capture(), mmCaptor.capture());
        Mockito.verify(deviceContext).submitTransaction();
        Assert.assertEquals(xid, xidCaptor.getValue().getValue());

        final List<MultipartReply> multipartReplyList = mmCaptor.getValue();
        Assert.assertEquals(0, multipartReplyList.size());
    }
}
