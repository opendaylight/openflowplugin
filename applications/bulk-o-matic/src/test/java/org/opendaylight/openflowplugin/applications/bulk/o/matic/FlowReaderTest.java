/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link FlowReader}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowReaderTest {

    @Mock
    private DataBroker mockDataBroker;
    @Mock
    private ReadTransaction readOnlyTransaction;
    @Mock
    private Node node;

    private FlowReader flowReader;

    @Before
    public void setUp() {
        doReturn(FluentFutures.immediateFluentFuture(Optional.of(node))).when(readOnlyTransaction)
            .read(any(LogicalDatastoreType.class), any(InstanceIdentifier.class));
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        flowReader = FlowReader.getNewInstance(mockDataBroker, 2, 5, true, false, (short) 1, (short) 2);
    }

    @Test
    public void testRun() {
        flowReader.run();
        Assert.assertEquals(10, flowReader.getFlowCount());
        Assert.assertEquals(FlowCounter.OperationStatus.SUCCESS.status(), flowReader.getReadOpStatus());
    }
}
