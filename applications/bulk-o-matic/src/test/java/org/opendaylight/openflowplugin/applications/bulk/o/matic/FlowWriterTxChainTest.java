/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.ExecutorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link FlowWriterTxChain}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowWriterTxChainTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlowWriterTxChainTest.class);
    private static final int FLOWS_PER_DPN = 100;

    @Mock
    private DataBroker mockDataBroker;
    @Mock
    private ExecutorService mockFlowPusher;
    @Mock
    private WriteTransaction wTx;

    private FlowWriterTxChain flowWriterTxChain;

    @Before
    public void setUp() throws Exception {

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable)invocation.getArguments()[0]).run();
                return null;
            }
        }).when(mockFlowPusher).execute(Matchers.<Runnable>any());

        final BindingTransactionChain mockedTxChain = mock(BindingTransactionChain.class);
        when(mockedTxChain.newWriteOnlyTransaction()).thenReturn(wTx);
        doReturn(mockedTxChain).when(mockDataBroker).createTransactionChain(Matchers.<TransactionChainListener>any());

        when(wTx.submit()).thenReturn(Futures.immediateCheckedFuture(null));

        flowWriterTxChain = new FlowWriterTxChain(mockDataBroker, mockFlowPusher);
    }
    @Test
    public void testAddFlows() throws Exception {
        flowWriterTxChain.addFlows(1, FLOWS_PER_DPN, 10, 10, 10, (short)0, (short)1, true);
        Mockito.verify(wTx, Mockito.times(FLOWS_PER_DPN)).put(Matchers.<LogicalDatastoreType>any(), Matchers.<InstanceIdentifier<DataObject>>any(), Matchers.<DataObject>any(), Matchers.anyBoolean());
    }

    @Test
    public void testDeleteFlows() throws Exception {
        flowWriterTxChain.deleteFlows(1, FLOWS_PER_DPN, 10, (short)0, (short)1);
        Mockito.verify(wTx, Mockito.times(FLOWS_PER_DPN)).delete(Matchers.<LogicalDatastoreType>any(), Matchers.<InstanceIdentifier<DataObject>>any());
    }

}