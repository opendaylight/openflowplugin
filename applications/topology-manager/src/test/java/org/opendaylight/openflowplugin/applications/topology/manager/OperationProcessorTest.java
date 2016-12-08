/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.topology.config.rev161103.TopologyManagerConfig;

@RunWith(MockitoJUnitRunner.class)
public class OperationProcessorTest {


    OperationProcessor processor;

    @Mock
    DataBroker dataBroker;
    @Mock
    BindingTransactionChain transactionChain;
    @Mock
    TransactionChainListener transactionChainListener;
    @Mock
    AsyncTransaction asyncTransaction;
    @Mock
    Throwable throwable;
    @Mock
    TopologyManagerConfig topologyManagerConfig;

    @Before
    public void setUp() {
        Mockito.when(dataBroker.createTransactionChain(Matchers.any(OperationProcessor.class)))
                .thenReturn(transactionChain);
        Mockito.when(topologyManagerConfig.getTopologyId()).thenReturn("openflow:test");
        processor = new OperationProcessor(dataBroker, topologyManagerConfig);
    }

    @Test
    public void onTransactionChainFailedTest() {
        processor.onTransactionChainFailed(transactionChain, asyncTransaction, throwable);
        Mockito.verify(transactionChain).close();
        //dataBroker.createTransactionChain is called 2 time
        // (first time in constructor, second time after old chain has been closed)
        Mockito.verify(dataBroker, times(2)).createTransactionChain(Matchers.any(OperationProcessor.class));
    }

    @Test
    public void closeTest() {
        processor.close();
        Mockito.verify(transactionChain).close();
    }


}
