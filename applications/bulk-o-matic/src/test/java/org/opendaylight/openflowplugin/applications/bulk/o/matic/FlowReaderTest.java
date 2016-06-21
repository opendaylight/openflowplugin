/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link FlowReader}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowReaderTest {

    @Mock
    private DataBroker mockDataBroker;
    @Mock
    private ReadOnlyTransaction rTx;
    @Mock
    private Node node;

    private FlowReader flowReader;

    @Before
    public void setUp() throws Exception {
        when(rTx.read(Mockito.any(LogicalDatastoreType.class), Mockito.<InstanceIdentifier<Node>>any()))
                .thenReturn(Futures.immediateCheckedFuture(Optional.of(node)));
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(rTx);
        flowReader = FlowReader.getNewInstance(mockDataBroker, 2, 5, true, false, (short)1, (short)2 );
    }

    @Test
    public void testRun() throws Exception {
        flowReader.run();
        Assert.assertEquals(10, flowReader.getFlowCount());
        Assert.assertEquals(FlowCounter.OperationStatus.SUCCESS.status(), flowReader.getReadOpStatus());
    }
}