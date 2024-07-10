/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link FlowWriterTxChain}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowWriterTxChainTest {

    private static final int FLOWS_PER_DPN = 100;

    @Mock
    private DataBroker mockDataBroker;
    @Mock
    private ExecutorService mockFlowPusher;
    @Mock
    private WriteTransaction writeTransaction;

    private FlowWriterTxChain flowWriterTxChain;

    @Before
    public void setUp() {

        Mockito.doAnswer(invocation -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(mockFlowPusher).execute(ArgumentMatchers.any());

        final TransactionChain mockedTxChain = mock(TransactionChain.class);
        when(mockedTxChain.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        doReturn(mockedTxChain).when(mockDataBroker).createMergingTransactionChain();

        doReturn(CommitInfo.emptyFluentFuture()).when(writeTransaction).commit();

        flowWriterTxChain = new FlowWriterTxChain(mockDataBroker, mockFlowPusher);
    }

    @Test
    public void testAddFlows() {
        flowWriterTxChain.addFlows(1, FLOWS_PER_DPN, 10, 10, 10, (short) 0, (short) 1, true);
        Mockito.verify(writeTransaction, Mockito.times(FLOWS_PER_DPN)).mergeParentStructurePut(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    public void testDeleteFlows() {
        flowWriterTxChain.deleteFlows(1, FLOWS_PER_DPN, 10, (short) 0, (short) 1);
        Mockito.verify(writeTransaction, Mockito.times(FLOWS_PER_DPN))
                .delete(ArgumentMatchers.any(),
                        ArgumentMatchers.<InstanceIdentifier<DataObject>>any());
    }
}
