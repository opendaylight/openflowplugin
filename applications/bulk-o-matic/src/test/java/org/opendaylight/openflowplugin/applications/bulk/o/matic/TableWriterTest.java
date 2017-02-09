/**
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import static org.mockito.Mockito.doReturn;

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
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link FlowWriterSequential}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TableWriterTest {
    private static final Logger LOG = LoggerFactory.getLogger(TableWriterTest.class);

    private static final int TABLES_PER_DPN = 100;
    private static final int DPN_COUNT = 1;
    private static final short START_TABLE_ID = 0;
    private static final short END_TABLE_ID = 99;

    @Mock
    private DataBroker mockDataBroker;
    @Mock
    private ExecutorService mockTablePusher;
    @Mock
    private WriteTransaction wTx;

    private TableWriter tableWriter;

    @Before
    public void setUp() throws Exception {

        doReturn(wTx).when(mockDataBroker).newWriteOnlyTransaction();
        Mockito.when(wTx.submit()).thenReturn(Futures.immediateCheckedFuture(null));

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable)invocation.getArguments()[0]).run();
                return null;
            }
        }).when(mockTablePusher).execute(Matchers.<Runnable>any());

        tableWriter = new TableWriter(mockDataBroker, mockTablePusher);
    }
    @Test
    public void testAddTables() throws Exception {
        tableWriter.addTables(DPN_COUNT, START_TABLE_ID, END_TABLE_ID);
        Mockito.verify(wTx, Mockito.times(TABLES_PER_DPN)).put(Matchers.<LogicalDatastoreType>any(), Matchers.<InstanceIdentifier<DataObject>>any(), Matchers.<DataObject>any(), Matchers.anyBoolean());
    }

    @Test
    public void testDeleteTables() throws Exception {
        tableWriter.deleteTables(DPN_COUNT, START_TABLE_ID, END_TABLE_ID);
        Mockito.verify(wTx, Mockito.times(TABLES_PER_DPN)).delete(Matchers.<LogicalDatastoreType>any(), Matchers.<InstanceIdentifier<DataObject>>any());
    }
}