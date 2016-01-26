/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.statistics.manager.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatNodeRegistration;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatisticsManager;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;

/**
 * Unit tests for StatAbstractListenCommit.
 *
 * @author Thomas Pantelis
 */
public class StatAbstractListenCommitTest {

    @Mock
    private NotificationProviderService mockNotificationProviderService;

    @Mock
    private StatisticsManager mockStatisticsManager;

    @Mock
    private DataBroker mockDataBroker;

    @Mock
    private NotificationListener mockNotificationListener;

    @Mock
    private StatNodeRegistration statsNodeRegistration;


    @SuppressWarnings("rawtypes")
    private StatAbstractListenCommit statCommit;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        statCommit = new StatAbstractListenCommit(mockStatisticsManager, mockDataBroker,
                mockNotificationProviderService, DataObject.class, statsNodeRegistration) {
            @Override
            protected InstanceIdentifier getWildCardedRegistrationPath() {
                return InstanceIdentifier.create(DataObject.class);
            }

            @Override
            protected NotificationListener getStatNotificationListener() {
                return mockNotificationListener;
            }
        };
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testReadLatestConfiguration() {

        InstanceIdentifier<DataObject> path = InstanceIdentifier.create(DataObject.class);

        ReadOnlyTransaction mockReadTx = mock(ReadOnlyTransaction.class);
        doReturn(mockReadTx).when(mockDataBroker).newReadOnlyTransaction();

        Optional<DataObject> expected = Optional.of(mock(DataObject.class));
        doReturn(Futures.immediateCheckedFuture(expected)).when(mockReadTx).read(
                LogicalDatastoreType.CONFIGURATION, path);

        Optional<DataObject> actual = statCommit.readLatestConfiguration(path);

        assertSame("Optional instance", expected, actual);

        actual = statCommit.readLatestConfiguration(path);

        assertSame("Optional instance", expected, actual);

        verify(mockReadTx, never()).close();
        verify(mockDataBroker).newReadOnlyTransaction();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReadLatestConfigurationWithReadFailure() {

        InstanceIdentifier<DataObject> path = InstanceIdentifier.create(DataObject.class);

        ReadOnlyTransaction mockReadTx1 = mock(ReadOnlyTransaction.class);
        ReadOnlyTransaction mockReadTx2 = mock(ReadOnlyTransaction.class);
        ReadOnlyTransaction mockReadTx3 = mock(ReadOnlyTransaction.class);
        doReturn(mockReadTx1).doReturn(mockReadTx2).doReturn(mockReadTx3).when(mockDataBroker).newReadOnlyTransaction();

        doReturn(Futures.immediateFailedCheckedFuture(new ReadFailedException("mock"))).when(mockReadTx1).read(
                LogicalDatastoreType.CONFIGURATION, path);

        doReturn(Futures.immediateFailedCheckedFuture(new ReadFailedException("mock"))).when(mockReadTx2).read(
                LogicalDatastoreType.CONFIGURATION, path);

        Optional<DataObject> expected = Optional.of(mock(DataObject.class));
        doReturn(Futures.immediateCheckedFuture(expected)).when(mockReadTx3).read(
                LogicalDatastoreType.CONFIGURATION, path);

        Optional<DataObject> actual = statCommit.readLatestConfiguration(path);

        assertEquals("Optional isPresent", false, actual.isPresent());

        actual = statCommit.readLatestConfiguration(path);

        assertSame("Optional instance", expected, actual);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReadLatestConfigurationWithInterveningOnDataChanged() {

        InstanceIdentifier<DataObject> path = InstanceIdentifier.create(DataObject.class);

        ReadOnlyTransaction mockReadTx1 = mock(ReadOnlyTransaction.class);
        ReadOnlyTransaction mockReadTx2 = mock(ReadOnlyTransaction.class);
        doReturn(mockReadTx1).doReturn(mockReadTx2).when(mockDataBroker).newReadOnlyTransaction();

        final Optional<DataObject> expected1 = Optional.of(mock(DataObject.class));
        Answer<CheckedFuture<Optional<DataObject>, ReadFailedException>> answer =
                new Answer<CheckedFuture<Optional<DataObject>, ReadFailedException>>() {
                    @Override
                    public CheckedFuture<Optional<DataObject>, ReadFailedException> answer(
                            InvocationOnMock unused) {
                        statCommit.onDataChanged(mock(AsyncDataChangeEvent.class));
                        return Futures.immediateCheckedFuture(expected1);
                    }
                };

        doAnswer(answer).when(mockReadTx1).read(LogicalDatastoreType.CONFIGURATION, path);

        Optional<DataObject> expected2 = Optional.of(mock(DataObject.class));
        doReturn(Futures.immediateCheckedFuture(expected2)).when(mockReadTx2).read(
                LogicalDatastoreType.CONFIGURATION, path);

        Optional<DataObject> actual = statCommit.readLatestConfiguration(path);

        assertSame("Optional instance", expected1, actual);

        actual = statCommit.readLatestConfiguration(path);

        assertSame("Optional instance", expected2, actual);

        actual = statCommit.readLatestConfiguration(path);

        assertSame("Optional instance", expected2, actual);

        verify(mockReadTx1).close();
        verify(mockReadTx2, never()).close();
        verify(mockDataBroker, times(2)).newReadOnlyTransaction();
    }
}
